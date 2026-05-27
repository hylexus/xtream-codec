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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.impl;

import io.github.hylexus.xtream.codec.base.web.exception.XtreamHttpException;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto.LinkDataDto;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.events.Jt808DashboardEventPayloads;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.events.Jt808DashboardEventType;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.Jt808DashboardEventService;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEvent;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventPublisher;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventSubscriberInfo;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DefaultJt808DashboardEventService implements Jt808DashboardEventService {

    private final XtreamEventPublisher eventPublisher;

    public DefaultJt808DashboardEventService(XtreamEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Flux<ServerSentEvent<Object>> linkData(String clientIp, LinkDataDto dto) {
        final Set<XtreamEvent.XtreamEventType> interestedEvents = this.convertToXtreamEventTypes(dto);
        return this.eventPublisher.subscribe(
                        XtreamEventSubscriberInfo.of(
                                interestedEvents,
                                Map.of("clientIp", clientIp, "source", "DashboardApi")
                        )
                )
                // .filter(filter)
                .filter(this.createTerminalIdFilter(dto))
                .map(event -> {
                    // ...
                    return ServerSentEvent.builder()
                            .event(String.valueOf(event.type().code()))
                            .data(event.payload())
                            .comment(event.type().description())
                            .build();
                });
    }

    private Predicate<XtreamEvent> createTerminalIdFilter(LinkDataDto dto) {
        return it -> {
            if (it.payload() instanceof Jt808DashboardEventPayloads.HasTerminalId hasTerminalId) {
                return hasTerminalId.match(dto.getTerminalId());
            }
            return false;
        };
    }

    Set<XtreamEvent.XtreamEventType> convertToXtreamEventTypes(LinkDataDto dto) {
        if (CollectionUtils.isEmpty(dto.getEventCodes())) {
            return Set.of(XtreamEvent.XtreamEventType.ALL);
        }
        return dto.getEventCodes().stream().map(code -> {
            // ...
            return XtreamEvent.DefaultXtreamEventType.of(code)
                    .or(() -> Jt808DashboardEventType.of(code))
                    .orElseThrow(() -> XtreamHttpException.badRequest("Invalid event code: " + code));
        }).collect(Collectors.toSet());
    }
}
