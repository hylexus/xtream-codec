/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.controller.servlet;

import io.github.hylexus.xtream.codec.base.web.utils.XtreamWebUtils;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.domain.dto.Jt1078VideoStreamSubscriberDto;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.exception.WebSocketCloseException;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.utils.Jt1078DashboardUtils;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078RequestPublisher;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078Subscriber;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078Subscription;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl.H264Jt1078SubscriberCreator;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SessionDestroyException;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.util.UriTemplate;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class BuiltinJt1078SubscriptionWebSocketHandlerServlet extends AbstractWebSocketHandler {
    public static final String PATH_PATTERN = "/dashboard-api/jt1078/v1/stream-data/websocket/flv/{sim}/{channel}";
    public static final Scheduler SCHEDULER = Schedulers.newBoundedElastic(10, 1024, "httpSubscriber");
    private static final Logger log = LoggerFactory.getLogger(BuiltinJt1078SubscriptionWebSocketHandlerServlet.class);
    private final Jt1078RequestPublisher publisher;
    private final UriTemplate uriTemplate;
    private static final String ATTR_KEY_SUBSCRIBER = "subscriber";

    public BuiltinJt1078SubscriptionWebSocketHandlerServlet(Jt1078RequestPublisher publisher) {
        this.publisher = publisher;
        this.uriTemplate = new UriTemplate(PATH_PATTERN);
    }

    @Override
    public void afterConnectionEstablished(@Nonnull WebSocketSession session) {
        final String clientIp = XtreamWebUtils.getClientIp(session.getHandshakeHeaders()::getFirst, session.getRemoteAddress())
                .map(XtreamWebUtils::filterClientIp).orElseThrow();
        final Jt1078VideoStreamSubscriberDto dto = Jt1078DashboardUtils.parseJt1078VideoStreamSubscriberDto(session, uriTemplate);
        final Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("clientIp", clientIp);
        final H264Jt1078SubscriberCreator subscriberCreator = Jt1078DashboardUtils.toH264Jt1078SubscriberCreator(dto, metadata);
        final Jt1078Subscriber subscriber = this.publisher.subscribeH264ToFlv(subscriberCreator);
        session.getAttributes().put(ATTR_KEY_SUBSCRIBER, subscriber);

        log.info("FlvSubscriber(WebSocket/{}) created: {}", subscriber.id(), subscriberCreator);
        subscriber
                .dataStream()
                .publishOn(SCHEDULER)
                .doOnError(Throwable.class, throwable -> {
                    if (throwable instanceof Jt1078SessionDestroyException) {
                        log.error("FlvSubscriber(WebSocket/{}) closed(Jt1078SessionDestroyException): {}", subscriber.id(), subscriberCreator);
                    } else if (throwable instanceof TimeoutException) {
                        try {
                            session.close();
                        } catch (IOException ignored) {
                            // ignored
                        }
                        log.error("FlvSubscriber(WebSocket/{}) closed(TimeoutException:{} seconds): {}", subscriber.id(), dto.getTimeout(), subscriberCreator);
                    } else {
                        log.error("FlvSubscriber(WebSocket/{}) closed: {}", subscriber.id(), subscriberCreator, throwable);
                    }
                })
                .onErrorComplete(TimeoutException.class)
                .onErrorComplete(Jt1078SessionDestroyException.class)
                .doFinally(signalType -> {
                    log.info("FlvSubscriber(WebSocket/{}) finished: {}", subscriber.id(), signalType);
                    try {
                        session.close();
                    } catch (IOException ignored) {
                        // ignored
                    }
                })
                .subscribe((Jt1078Subscription subscription) -> {
                    final byte[] data = (byte[]) subscription.payload();
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(new BinaryMessage(data));
                        }
                    } catch (Throwable throwable) {
                        log.error("Error while sending data to flv client", throwable);
                    }
                });
    }

    /**
     * Invoked after the WebSocket connection has been closed by either side, or after a
     * transport error has occurred. Although the session may technically still be open,
     * depending on the underlying implementation, sending messages at this point is
     * discouraged and most likely will not succeed.
     *
     * @throws Exception this method can handle or propagate exceptions; see class-level
     *                   Javadoc for details.
     */
    @Override
    public void afterConnectionClosed(@Nonnull WebSocketSession session, @Nonnull CloseStatus status) throws Exception {
        try (session) {
            final Jt1078Subscriber subscriber = (Jt1078Subscriber) session.getAttributes().get(ATTR_KEY_SUBSCRIBER);
            if (subscriber != null) {
                final InetSocketAddress remoteAddress = session.getRemoteAddress();
                this.publisher.unsubscribe(subscriber.id(), new WebSocketCloseException("remoteAddress: " + remoteAddress));
            }
        }
    }
}
