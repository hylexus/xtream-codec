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

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author hylexus
 */
public interface XtreamEventSubscriberInfo {

    /**
     * 确保全局唯一
     */
    String id();

    Instant createdAt();

    /**
     * 可选的描述信息
     */
    default Map<String, Object> metadata() {
        return Collections.emptyMap();
    }

    Set<XtreamEvent.XtreamEventType> interestedEvents();

    Set<Integer> interestedEventsCode();

    /**
     * 订阅所有事件
     */
    static XtreamEventSubscriberInfo allEventsSubscriber(Map<String, Object> description) {
        return new DefaultXtreamEventSubscriberInfo(
                UUID.randomUUID().toString().replace("-", ""),
                Set.of(XtreamEvent.DefaultXtreamEventType.ALL),
                description
        );
    }

    static XtreamEventSubscriberInfo of(Set<XtreamEvent.XtreamEventType> interestedEvents, Map<String, Object> metadata) {
        return of(UUID.randomUUID().toString().replace("-", ""), interestedEvents, metadata);
    }

    static XtreamEventSubscriberInfo of(String id, Set<XtreamEvent.XtreamEventType> interestedEvents, Map<String, Object> metadata) {
        return new XtreamEventSubscriberInfo.DefaultXtreamEventSubscriberInfo(
                id,
                interestedEvents,
                metadata
        );
    }

    record DefaultXtreamEventSubscriberInfo(
            String id,
            Set<XtreamEvent.XtreamEventType> interestedEvents,
            Set<Integer> interestedEventsCode,
            Instant createdAt,
            Map<String, Object> metadata
    ) implements XtreamEventSubscriberInfo {

        public DefaultXtreamEventSubscriberInfo(
                String id,
                Set<XtreamEvent.XtreamEventType> interestedEvents,
                Map<String, Object> metadata) {
            this(
                    id,
                    interestedEvents, interestedEvents.stream().map(XtreamEvent.XtreamEventType::code).collect(Collectors.toSet()), Instant.now(),
                    metadata
            );
        }

    }

}
