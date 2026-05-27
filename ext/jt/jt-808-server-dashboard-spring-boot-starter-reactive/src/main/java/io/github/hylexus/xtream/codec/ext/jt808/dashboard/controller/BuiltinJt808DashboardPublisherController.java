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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hylexus.xtream.codec.base.web.domain.dto.PageableDto;
import io.github.hylexus.xtream.codec.base.web.domain.vo.PageableVo;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventPublisher;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hylexus
 */
@RestController
@RequestMapping("/dashboard-api/jt808/v1/event-publisher")
public class BuiltinJt808DashboardPublisherController {

    private final XtreamEventPublisher eventPublisher;

    public BuiltinJt808DashboardPublisherController(XtreamEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @GetMapping("/subscribers")
    public PageableVo<EventPublisherInfo> list(@Validated PageableDto dto) {
        final int total = this.eventPublisher.subscriberCount();
        final List<EventPublisherInfo> list = this.eventPublisher.subscriberView()
                .skip(dto.getOffset())
                .limit(dto.getPageSize())
                .map(it -> {
                    final Set<EventDescription> events = it.interestedEvents().stream().map(e -> new EventDescription(e.code(), e.description())).collect(Collectors.toSet());
                    return new EventPublisherInfo(it.id(), events, it.createdAt(), it.metadata());
                })
                .toList();
        return PageableVo.of(total, list);
    }

    public record EventPublisherInfo(
            String id,
            Set<EventDescription> interestedEvents,
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8") Instant createdAt,
            Map<String, Object> metadata) {
    }

    public record EventDescription(int code, String description) {
    }
}
