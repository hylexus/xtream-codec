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

package io.github.hylexus.xtream.codec.server.reactive.spec.event;

import io.github.hylexus.xtream.codec.server.reactive.spec.event.builtin.DefaultXtreamEvent;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * 事件发布器
 *
 * @author hylexus
 */
public interface XtreamEventPublisher {

    /**
     * 不建议直接使用这个方法，而是使用 {@link #publishIfNecessary(XtreamEvent.XtreamEventType, Supplier)}。
     */
    void publish(XtreamEvent event);

    /**
     * 在某些场景下，要发送的事件的 生产逻辑 可能比较昂贵。
     * <p>
     * 该方法在 {@link #eventPredicate()} 的执行结果为 {@code true} 时才会调用 {@code payloadSupplier} 生产事件，并发送事件。
     *
     * @see #eventPredicate()
     */
    default <T> void publishIfNecessary(XtreamEvent.XtreamEventType eventType, Supplier<T> payloadSupplier) {
        if (this.eventPredicate().test(eventType)) {
            final Object payload = payloadSupplier.get();
            this.publish(DefaultXtreamEvent.of(eventType, payload));
        }
    }

    Predicate<XtreamEvent.XtreamEventType> eventPredicate();

    Flux<XtreamEvent> subscribe(XtreamEventSubscriberInfo subscriberInfo);

    default Flux<XtreamEvent> subscribeAll(Map<String, Object> description) {
        return this.subscribe(XtreamEventSubscriberInfo.allEventsSubscriber(description));
    }

    void shutdown();

    Stream<XtreamEventSubscriberInfo> subscriberView();

    int subscriberCount();
}
