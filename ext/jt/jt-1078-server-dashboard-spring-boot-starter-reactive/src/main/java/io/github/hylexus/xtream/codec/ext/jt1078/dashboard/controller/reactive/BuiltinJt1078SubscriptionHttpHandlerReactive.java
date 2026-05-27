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

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.controller.reactive;

import io.github.hylexus.xtream.codec.base.web.annotation.ClientIp;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.domain.dto.Jt1078VideoStreamSubscriberDto;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.utils.Jt1078DashboardUtils;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078RequestPublisher;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078Subscriber;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl.H264Jt1078SubscriberCreator;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SessionDestroyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Controller
public class BuiltinJt1078SubscriptionHttpHandlerReactive {

    private static final Logger log = LoggerFactory.getLogger(BuiltinJt1078SubscriptionHttpHandlerReactive.class);
    private final Jt1078RequestPublisher publisher;
    public static final Scheduler SCHEDULER = Schedulers.newBoundedElastic(10, 1024, "httpSubscriber");

    public BuiltinJt1078SubscriptionHttpHandlerReactive(Jt1078RequestPublisher publisher) {
        this.publisher = publisher;
    }

    @RequestMapping(
            value = "/dashboard-api/jt1078/v1/stream-data/http/flv/{sim}/{channel}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<Flux<byte[]>> subscribeFlvStream(@Validated Jt1078VideoStreamSubscriberDto dto, @ClientIp String clientIp) {
        final Flux<byte[]> flvStream = this.doSubscribeFlvStream(dto, clientIp);
        return ResponseEntity.ok().body(flvStream);
    }

    private Flux<byte[]> doSubscribeFlvStream(Jt1078VideoStreamSubscriberDto params, String clientIp) {
        final Map<String, Object> metadata = new HashMap<>();
        metadata.put("clientIp", clientIp);
        final H264Jt1078SubscriberCreator subscriberCreator = Jt1078DashboardUtils.toH264Jt1078SubscriberCreator(params, metadata);
        final Jt1078Subscriber subscriber = this.publisher.subscribeH264ToFlv(subscriberCreator);

        log.info("FlvSubscriber(Http/{}) created: {}", subscriber.id(), subscriberCreator);
        return subscriber
                .dataStream()
                .publishOn(SCHEDULER)
                .doOnError(Throwable.class, throwable -> {
                    if (throwable instanceof Jt1078SessionDestroyException) {
                        log.error("FlvSubscriber(Http/{}) closed(Jt1078SessionDestroyException): {}", subscriber.id(), subscriberCreator);
                    } else if (throwable instanceof TimeoutException) {
                        log.error("FlvSubscriber(Http/{}) closed(TimeoutException:{} seconds): {}", subscriber.id(), params.getTimeout(), subscriberCreator);
                    } else {
                        log.error("FlvSubscriber(Http/{}) closed: {}", subscriber.id(), subscriberCreator, throwable);
                    }
                })
                .onErrorComplete(Jt1078SessionDestroyException.class)
                .onErrorComplete(TimeoutException.class)
                .map(it -> (byte[]) it.payload())
                .doFinally(signalType -> {
                    // ...
                    log.info("FlvSubscriber(Http/{}) finished: {}", subscriber.id(), signalType);
                });
    }

}
