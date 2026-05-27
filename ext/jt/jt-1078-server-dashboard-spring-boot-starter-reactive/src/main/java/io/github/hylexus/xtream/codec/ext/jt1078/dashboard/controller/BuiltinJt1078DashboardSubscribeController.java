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

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.controller;

import io.github.hylexus.xtream.codec.base.web.annotation.ClientIp;
import io.github.hylexus.xtream.codec.base.web.domain.vo.PageableVo;
import io.github.hylexus.xtream.codec.base.web.exception.XtreamHttpException;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.domain.dto.Jt1078SubscriberQueryDto;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078SubscriberDescriptor;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078SubscriberManager;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author hylexus
 */
@RestController
@RequestMapping("/dashboard-api/jt1078/v1")
public class BuiltinJt1078DashboardSubscribeController {
    private final Jt1078SubscriberManager subscriberManager;

    public BuiltinJt1078DashboardSubscribeController(Jt1078SubscriberManager subscriberManager) {
        this.subscriberManager = subscriberManager;
    }

    @GetMapping("/subscribers")
    public PageableVo<Jt1078SubscriberDescriptor> subscribers(Jt1078SubscriberQueryDto dto) {
        final long total = this.subscriberManager.count(it -> true);
        final List<Jt1078SubscriberDescriptor> list = this.subscriberManager.list()
                .filter(createFilter(dto))
                .skip(dto.getOffset())
                .limit(dto.getPageSize())
                .toList();
        return PageableVo.of(total, list);
    }

    @GetMapping("/subscriber/{id}")
    public Jt1078SubscriberDescriptor subscriber(@PathVariable("id") String id) {
        return this.subscriberManager.list().filter(it -> it.id().equals(id))
                .findFirst()
                .orElseThrow(() -> XtreamHttpException.notFound("Subscriber not found"));
    }

    @DeleteMapping("/subscriber/{id}")
    public Map<String, Object> closeSubscriber(@PathVariable("id") String id, @ClientIp String clientIp) {
        this.subscriberManager.closeSubscriber(id);
        return Map.of("id", id);
    }

    static Predicate<Jt1078SubscriberDescriptor> createFilter(Jt1078SubscriberQueryDto dto) {
        Predicate<Jt1078SubscriberDescriptor> predicate = it -> true;
        if (StringUtils.hasText(dto.getSim())) {
            predicate = predicate.and(it -> it.rawSim().toLowerCase().contains(dto.getSim().trim().toLowerCase()));
        }
        return predicate;
    }

}
