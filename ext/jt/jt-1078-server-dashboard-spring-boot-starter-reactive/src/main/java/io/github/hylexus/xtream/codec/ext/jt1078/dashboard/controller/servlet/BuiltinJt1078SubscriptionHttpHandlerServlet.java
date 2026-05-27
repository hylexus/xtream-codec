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

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.controller.servlet;

import io.github.hylexus.xtream.codec.base.web.annotation.ClientIp;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.domain.dto.Jt1078VideoStreamSubscriberDto;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.utils.Jt1078DashboardUtils;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078RequestPublisher;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078Subscriber;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078Subscription;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl.H264Jt1078SubscriberCreator;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SessionDestroyException;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Controller
public class BuiltinJt1078SubscriptionHttpHandlerServlet {

    private static final Logger log = LoggerFactory.getLogger(BuiltinJt1078SubscriptionHttpHandlerServlet.class);
    private final Jt1078RequestPublisher publisher;
    public static final Scheduler SCHEDULER = Schedulers.newBoundedElastic(10, 1024, "httpSubscriber");

    public BuiltinJt1078SubscriptionHttpHandlerServlet(Jt1078RequestPublisher publisher) {
        this.publisher = publisher;
    }

    @RequestMapping(
            value = "/dashboard-api/jt1078/v1/stream-data/http/flv/{sim}/{channel}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseBodyEmitter subscribeFlvStream(@Validated Jt1078VideoStreamSubscriberDto dto, @ClientIp String clientIp) {
        final ResponseBodyEmitter sseEmitter = new ResponseBodyEmitter(0L);
        this.doSubscribeFlvStream(dto, clientIp, sseEmitter);
        return sseEmitter;
    }

    private void doSubscribeFlvStream(Jt1078VideoStreamSubscriberDto dto, String clientIp, ResponseBodyEmitter sseEmitter) {
        final Map<String, Object> metadata = new HashMap<>();
        metadata.put("clientIp", clientIp);
        final H264Jt1078SubscriberCreator subscriberCreator = Jt1078DashboardUtils.toH264Jt1078SubscriberCreator(dto, metadata);
        final Jt1078Subscriber subscriber = this.publisher.subscribeH264ToFlv(subscriberCreator);
        sseEmitter.onCompletion(() -> this.publisher.unsubscribe(subscriber.id()));

        log.info("FlvSubscriber(Http/{}) created: {}", subscriber.id(), subscriberCreator);

        subscriber.dataStream()
                .publishOn(SCHEDULER)
                .doOnError(Throwable.class, throwable -> {
                    if (throwable instanceof Jt1078SessionDestroyException) {
                        log.error("FlvSubscriber(Http/{}) closed(Jt1078SessionDestroyException): {}", subscriber.id(), subscriberCreator);
                        sseEmitter.complete();
                    } else if (throwable instanceof TimeoutException) {
                        log.error("FlvSubscriber(Http/{}) closed(TimeoutException:{} seconds): {}", subscriber.id(), dto.getTimeout(), subscriberCreator);
                        sseEmitter.complete();
                    } else {
                        log.error("FlvSubscriber(Http/{}) closed: {}", subscriber.id(), subscriberCreator, throwable);
                        sseEmitter.completeWithError(throwable);
                    }
                })
                .onErrorComplete(TimeoutException.class)
                .onErrorComplete(Jt1078SessionDestroyException.class)
                .doFinally(signalType -> {
                    log.info("FlvSubscriber(Http/{}) finished: {}", subscriber.id(), signalType);
                    sseEmitter.complete();
                })
                .subscribe((Jt1078Subscription subscription) -> {
                    final byte[] payload = (byte[]) subscription.payload();
                    try {
                        sseEmitter.send(payload, MediaType.APPLICATION_OCTET_STREAM);
                    } catch (Throwable throwable) {
                        if (throwable instanceof IllegalStateException) {
                            final String message = throwable.getMessage();
                            // FIXME ResponseBodyEmitter has already completed
                            if (message == null || !message.contains("ResponseBodyEmitter has already completed")) {
                                log.error("FlvSubscriber(Http/{}) closed: {}", subscriber.id(), subscriberCreator, throwable);
                            }
                            sseEmitter.complete();
                        } else if (throwable instanceof ClientAbortException) {
                            // 播放器端断开
                            log.info("FlvSubscriber(Http/{}) closed(cancel): {} ", subscriber.id(), subscriberCreator);
                            sseEmitter.complete();
                        } else {
                            log.error("Error while sending data to flv client", throwable);
                            sseEmitter.completeWithError(throwable);
                        }
                    }
                });
    }
}
