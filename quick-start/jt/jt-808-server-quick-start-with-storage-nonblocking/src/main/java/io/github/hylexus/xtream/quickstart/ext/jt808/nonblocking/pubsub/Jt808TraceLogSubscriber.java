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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.pubsub;

import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventPublisher;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventSubscriberInfo;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.event.Jt808EventPayloads;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.values.Jt808EventType;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.TraceLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author hylexus
 */
@Component
public class Jt808TraceLogSubscriber {
    private static final Logger log = LoggerFactory.getLogger(Jt808TraceLogSubscriber.class);

    private static final AtomicBoolean SUBSCRIBED = new AtomicBoolean(false);

    private final XtreamEventPublisher eventPublisher;
    private final TraceLogService traceLogService;

    public Jt808TraceLogSubscriber(XtreamEventPublisher eventPublisher, TraceLogService traceLogService) {
        this.eventPublisher = eventPublisher;
        this.traceLogService = traceLogService;
    }

    public void subscribe() {
        if (SUBSCRIBED.compareAndSet(false, true)) {
            final XtreamEventSubscriberInfo subscriberInfo = XtreamEventSubscriberInfo.of(
                    Set.of(
                            Jt808EventType.RECEIVE_PACKAGE,
                            Jt808EventType.SEND_PACKAGE
                    ),
                    Map.of(
                            "source", "jt-808-server-quick-start-with-storage",
                            "desc", "订阅报文并写入数据库"
                    )
            );
            this.eventPublisher.subscribe(subscriberInfo)
                    .publishOn(Schedulers.parallel())
                    .flatMap(event -> {
                        // ...
                        return switch (event.payload()) {
                            case Jt808EventPayloads.Jt808ReceiveEvent receiveEvent -> this.traceLogService.afterRequestDecode(receiveEvent);
                            case Jt808EventPayloads.Jt808SendEvent sendEvent -> this.traceLogService.beforeResponseSend(sendEvent);
                            // 其他消息 ==> 忽略
                            default -> Mono.empty();
                        };
                    }).subscribe();

            log.info("Jt808TraceLogSubscriber started...");
        }
    }
}
