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

package io.github.hylexus.xtream.codec.server.reactive.spec.event.builtin;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEvent;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventPublisher;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventSubscriberInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author hylexus
 */
public abstract class AbstractXtreamEventPublisher implements XtreamEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(AbstractXtreamEventPublisher.class);

    protected final ConcurrentMap<String, XtreamEventSubscriberInfo> subscribers = new ConcurrentHashMap<>();
    protected final ConcurrentMap<Integer, Integer> interestedEventTypes = new ConcurrentHashMap<>();

    protected final Sinks.Many<XtreamEvent> sink;
    protected final XtreamSchedulerRegistry schedulerRegistry;
    protected final Predicate<XtreamEvent.XtreamEventType> defaultPredicate;

    public AbstractXtreamEventPublisher(XtreamSchedulerRegistry schedulerRegistry) {
        this.schedulerRegistry = schedulerRegistry;
        this.sink = Sinks.many().multicast().onBackpressureBuffer(1024, false);
        this.defaultPredicate = eventType -> {
            // 没有订阅者
            if (this.subscribers.isEmpty()) {
                return false;
            }

            final int code = eventType.code();
            // 事件类型中有 ALL(所有类型事件) ==> 无条件发布
            if (this.interestedEventTypes.containsKey(XtreamEvent.DefaultXtreamEventType.ALL.code())) {
                return true;
            }

            // 只发布有必要发布的事件
            final Integer refCount = this.interestedEventTypes.get(code);
            return refCount != null && refCount > 0;
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(XtreamEvent event) {
        final Sinks.EmitResult emitResult = this.sink.tryEmitNext(event);
        if (emitResult.isFailure()) {
            log.error("Failed to publish event {} with result {}", event, emitResult);
        }
    }

    @Override
    public Predicate<XtreamEvent.XtreamEventType> eventPredicate() {
        return this.defaultPredicate;
    }

    @Override
    public Flux<XtreamEvent> subscribe(XtreamEventSubscriberInfo info) {
        // info.interestedEventsCode() 由外部确保返回一个已经计算好的常量(而不是每次调用都计算一次)
        final Set<Integer> interestedEventsCode = info.interestedEventsCode();
        if (interestedEventsCode.isEmpty()) {
            log.error("Interested events code cannot be empty for subscriber: {}", info);
            return Flux.error(new IllegalArgumentException("Interested events code cannot be empty"));
        }

        // 新增订阅者
        final XtreamEventSubscriberInfo old = this.subscribers.putIfAbsent(info.id(), info);
        if (old != null) {
            log.error("Subscriber already exists: {}", info);
            return Flux.error(new IllegalStateException("Subscriber already exists: " + info));
        }

        // 增加事件类型的引用计数
        synchronized (this.interestedEventTypes) {
            for (final Integer code : interestedEventsCode) {
                this.interestedEventTypes.compute(code, (key, count) -> count == null ? 1 : count + 1);
            }
        }

        return sink.asFlux()
                .filter(event -> interestedEventsCode.contains(
                        XtreamEvent.XtreamEventType.ALL.code())
                                 || interestedEventsCode.contains(event.type().code())
                )
                .publishOn(this.schedulerRegistry.eventPublisherScheduler())
                .doFinally(signalType -> {
                    // ...
                    this.unsubscribe(info);
                });
    }

    protected void unsubscribe(XtreamEventSubscriberInfo info) {
        // 移除订阅者
        this.subscribers.remove(info.id());
        // 减少事件类型的引用计数
        synchronized (this.interestedEventTypes) {
            for (final int code : info.interestedEventsCode()) {
                this.interestedEventTypes.compute(code, (key, count) -> {
                    // 引用计数 <= 1  ==> 移除
                    if (count == null || count <= 1) {
                        return null;
                    } else {
                        // 引用计数 - 1
                        return count - 1;
                    }
                });
            }
        }
    }

    @Override
    public void shutdown() {
        this.sink.tryEmitComplete();
    }

    @Override
    public Stream<XtreamEventSubscriberInfo> subscriberView() {
        return this.subscribers.values().stream();
    }

    @Override
    public int subscriberCount() {
        return this.subscribers.size();
    }

}
