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

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.controller.reactive;

import io.github.hylexus.xtream.codec.base.web.utils.XtreamWebUtils;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.domain.dto.Jt1078VideoStreamSubscriberDto;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.utils.Jt1078DashboardUtils;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078RequestPublisher;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078Subscriber;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl.H264Jt1078SubscriberCreator;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SessionDestroyException;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class BuiltinJt1078SubscriptionWebSocketHandlerReactive implements WebSocketHandler {
    public static final String PATH_PATTERN = "/dashboard-api/jt1078/v1/stream-data/websocket/flv/{sim}/{channel}";
    public static final Scheduler SCHEDULER = Schedulers.newBoundedElastic(10, 1024, "httpSubscriber");
    private static final Logger log = LoggerFactory.getLogger(BuiltinJt1078SubscriptionWebSocketHandlerReactive.class);
    private final Jt1078RequestPublisher publisher;
    private final UriTemplate uriTemplate;

    public BuiltinJt1078SubscriptionWebSocketHandlerReactive(Jt1078RequestPublisher publisher) {
        this.publisher = publisher;
        this.uriTemplate = new UriTemplate(PATH_PATTERN);
    }

    @Override
    @Nonnull
    public Mono<Void> handle(@Nonnull WebSocketSession session) {
        final String clientIp = XtreamWebUtils.getClientIp(session.getHandshakeInfo().getHeaders()::getFirst, session.getHandshakeInfo().getRemoteAddress())
                .map(XtreamWebUtils::filterClientIp).orElseThrow();
        final Jt1078VideoStreamSubscriberDto dto = Jt1078DashboardUtils.parseJt1078VideoStreamSubscriberDto(session, uriTemplate);

        // websocket inbound
        final Mono<Void> input = session.receive().then();

        // websocket outbound: FLV 数据流
        final Mono<Void> flvStream = this.subscribeFlvStream(session, clientIp, dto);

        return Mono.zip(input, flvStream).then();
    }

    private Mono<Void> subscribeFlvStream(WebSocketSession session, String clientIp, Jt1078VideoStreamSubscriberDto params) {
        final Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("clientIp", clientIp);
        final H264Jt1078SubscriberCreator subscriberCreator = Jt1078DashboardUtils.toH264Jt1078SubscriberCreator(params, metadata);
        final Jt1078Subscriber subscriber = this.publisher.subscribeH264ToFlv(subscriberCreator);

        log.info("FlvSubscriber(WebSocket/{}) created: {}", subscriber.id(), subscriberCreator);

        return subscriber
                .dataStream()
                .publishOn(SCHEDULER)
                .doOnError(Throwable.class, throwable -> {
                    if (throwable instanceof Jt1078SessionDestroyException) {
                        log.error("FlvSubscriber(WebSocket/{}) closed(Jt1078SessionDestroyException): {}", subscriber.id(), subscriberCreator);
                    } else if (throwable instanceof TimeoutException) {
                        log.error("FlvSubscriber(WebSocket/{}) closed(TimeoutException:{} seconds): {}", subscriber.id(), params.getTimeout(), subscriberCreator);
                    } else {
                        log.error("FlvSubscriber(WebSocket/{}) closed: {}", subscriber.id(), subscriberCreator, throwable);
                    }
                })
                .onErrorComplete(Jt1078SessionDestroyException.class)
                .onErrorComplete(TimeoutException.class)
                .flatMap(subscription -> {
                    final byte[] data = (byte[]) subscription.payload();
                    final WebSocketMessage webSocketMessage = session.binaryMessage(factory -> factory.wrap(data));
                    return session.send(Mono.just(webSocketMessage));
                })
                .doFinally(signalType -> {
                    // ...
                    log.info("FlvSubscriber(WebSocket/{}) finished: {}", subscriber.id(), signalType);
                })
                .then();
    }
}
