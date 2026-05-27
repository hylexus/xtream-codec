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
import io.github.hylexus.xtream.debug.ext.jt1078.model.dto.DemoVideoStreamSubscriberDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * H.264 --> FLV
 */
@Controller
public class FlvStreamSubscriberDemoHttp {
    private static final Logger log = LoggerFactory.getLogger(FlvStreamSubscriberDemoHttp.class);
    private final Jt1078RequestPublisher publisher;
    public static final Scheduler SCHEDULER = Schedulers.newBoundedElastic(10, 1024, "httpSubscriber");

    public FlvStreamSubscriberDemoHttp(Jt1078RequestPublisher publisher) {
        this.publisher = publisher;
    }

    @GetMapping("/debug-api/jt1078/stream-data/http/flv/{sim}/{channel}")
    public ResponseEntity<Flux<byte[]>> handle(
            ServerWebExchange exchange,
            @Validated DemoVideoStreamSubscriberDto params) {
        log.info("New FlvSubscriber(Http) created(Http): {}", params);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
        // exchange.getResponse().getHeaders().setContentType(MediaType.valueOf("video/x-flv"));
        final Flux<byte[]> flvStream = this.subscribeFlvStream(params, exchange);
        return ResponseEntity.ok().body(flvStream);
    }

    private Flux<byte[]> subscribeFlvStream(DemoVideoStreamSubscriberDto params, ServerWebExchange exchange) {
        final int timeout = params.getTimeout();
        final H264Jt1078SubscriberCreator subscriberCreator = H264Jt1078SubscriberCreator.builder()
                .rawSim(params.getSim())
                .channelNumber(params.getChannel())
                .timeout(Duration.ofSeconds(timeout))
                .h264Meta(new H264Jt1078SubscriberCreator.H264Meta(params.getNaluDecoderRingBufferSize()))
                .hasAudio(params.isHasAudio())
                .hasVideo(params.isHasVideo())
                .build();

        return this.publisher.subscribeH264ToFlvStream(subscriberCreator)
                .publishOn(SCHEDULER)
                .onErrorComplete(Jt1078SessionDestroyException.class)
                .onErrorComplete(TimeoutException.class)
                .doOnError(Jt1078SessionDestroyException.class, e -> {
                    // ...
                    log.error("FlvSubscriber(Http) 取消订阅(Session销毁)");
                })
                .doOnError(TimeoutException.class, e -> {
                    // ...
                    log.error("FlvSubscriber(Http) 取消订阅(超时, {} 秒)", timeout);
                })
                .doOnError(Throwable.class, e -> {
                    // ...
                    log.error(e.getMessage(), e);
                })
                .doOnNext(subscription -> {
                    final byte[] payload = (byte[]) subscription.payload();
                    log.info("FlvSubscriber(Http) outbound: {}", FormatUtils.toHexString(payload));
                })
                .map(it -> (byte[]) it.payload())
                .doFinally(signalType -> {
                    // ...
                    log.info("FlvSubscriber(Http) finished({})", signalType);
                });
    }
}
