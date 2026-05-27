/*
 * Copyright 2024-present the original author or authors.
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

package io.github.hylexus.xtream.debug.ext.jt1078.controller;


import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078RequestPublisher;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl.H264Jt1078SubscriberCreator;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SessionDestroyException;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SessionManager;
import io.github.hylexus.xtream.debug.ext.jt1078.model.dto.DemoVideoStreamSubscriberDto;
import io.github.hylexus.xtream.debug.ext.jt1078.pubsub.LocalFlvFileDebugSubscriberV2;
import io.github.hylexus.xtream.debug.ext.jt1078.utils.WebSocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * H.264 --> FLV
 */
public class FlvStreamSubscriberDemoWebSocket implements WebSocketHandler {
    public static final String PATH_PATTERN = "/debug-api/jt1078/stream-data/websocket/flv/{sim}/{channel}";
    private static final Logger log = LoggerFactory.getLogger(FlvStreamSubscriberDemoWebSocket.class);
    private final Jt1078RequestPublisher publisher;
    private final UriTemplate uriTemplate;
    public static final Scheduler SCHEDULER = Schedulers.newBoundedElastic(10, 1024, "websocketSubscriber");
    private final Jt1078SessionManager sessionManager;
    private final LocalFlvFileDebugSubscriberV2 localFlvFileDebugSubscriberV2;

    public FlvStreamSubscriberDemoWebSocket(Jt1078RequestPublisher publisher, Jt1078SessionManager sessionManager) {
        this.publisher = publisher;
        this.sessionManager = sessionManager;
        this.uriTemplate = new UriTemplate(PATH_PATTERN);
        this.localFlvFileDebugSubscriberV2 = new LocalFlvFileDebugSubscriberV2(publisher);
    }

    @NonNull
    @Override
    public Mono<Void> handle(@NonNull WebSocketSession session) {

        final DemoVideoStreamSubscriberDto params = WebSocketUtils.createForReactiveSession(session, this.uriTemplate);
        log.info("New FlvSubscriber(WebSocket) publisher created: {}", params);

        // websocket inbound
        final Mono<Void> input = session.receive()
                .doOnNext(message -> {
                    log.info("Receive webSocket msg, webSocketSessionId={}, payload={}", session.getId(), message.getPayloadAsText());
                })
                .then();

        // FLV 数据流
        // websocket outbound
        final Mono<Void> flvStream = this.subscribeFlvStream(session, params);

        return Mono.zip(input, flvStream).doFinally(signalType -> {
            // ...
            log.info("FlvSubscriber(WebSocket) finished({})", signalType);
        }).then();
    }


    private Mono<Void> subscribeFlvStream(WebSocketSession session, DemoVideoStreamSubscriberDto params) {
        // localFlvFileDebugSubscriberV2.subscribe(params.getSim(), params.getChannel());
        final H264Jt1078SubscriberCreator h264Jt1078SubscriberCreator = H264Jt1078SubscriberCreator.builder()
                .rawSim(params.getSim())
                .channelNumber(params.getChannel())
                .timeout(Duration.ofSeconds(params.getTimeout()))
                .h264Meta(new H264Jt1078SubscriberCreator.H264Meta(params.getNaluDecoderRingBufferSize()))
                .hasAudio(params.isHasAudio())
                .hasVideo(params.isHasVideo())
                .build();

        return this.publisher
                .subscribeH264ToFlvStream(h264Jt1078SubscriberCreator)
                .doOnNext(subscription -> {
                    // log.info("FlvSubscriber(WebSocket) inbound: {}", FormatUtils.toHexString(subscription.payload()));
                })
                // .publishOn(SCHEDULER)
                .doOnNext(subscription -> {
                    // log.info("FlvSubscriber(WebSocket) inbound: {}", FormatUtils.toHexString(subscription.payload()));
                })
                .onErrorComplete(Jt1078SessionDestroyException.class)
                .onErrorComplete(TimeoutException.class)
                .doOnError(Jt1078SessionDestroyException.class, e -> {
                    log.error("FlvSubscriber(WebSocket) 取消订阅(Session销毁)");
                })
                .doOnError(TimeoutException.class, e -> {
                    log.error("FlvSubscriber(WebSocket) 取消订阅(超时, {} 秒)", params.getTimeout());
                })
                .doOnError(Throwable.class, e -> {
                    log.error(e.getMessage(), e);
                })
                .flatMap(subscription -> {
                    final byte[] data = (byte[]) subscription.payload();

                    log.debug("FlvSubscriber(WebSocket) outbound: {}", FormatUtils.toHexString(data));

                    final WebSocketMessage webSocketMessage = session.binaryMessage(factory -> factory.wrap(data));
                    return Mono.just(webSocketMessage);
                })
                .flatMap(it -> session.send(Mono.just(it)))
                .then();
    }

}
