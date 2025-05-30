/*
 * Copyright 2024 the original author or authors.
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

import io.github.hylexus.xtream.codec.base.web.domain.vo.PageableVo;
import io.github.hylexus.xtream.codec.base.web.utils.XtreamWebUtils;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.domain.dto.Jt1078SessionQueryDto;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.domain.value.Jt1078DashboardSimpleValues;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.domain.vo.Jt1078SessionVo;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078Session;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SessionManager;
import org.springframework.http.HttpHeaders;
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
public class BuiltinJt1078DashboardSessionController {
    private final Jt1078SessionManager sessionManager;

    public BuiltinJt1078DashboardSessionController(Jt1078SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @GetMapping("/sessions")
    public PageableVo<Jt1078SessionVo> sessionList(Jt1078SessionQueryDto dto) {
        final Predicate<Jt1078Session> filter = createFilter(dto);
        final long total = this.sessionManager.count(filter);
        final List<Jt1078SessionVo> list = this.sessionManager.list(dto.getPageNumber(), dto.getPageSize(), filter, this::convertToVo)
                .toList();
        return PageableVo.of(total, list);
    }

    @DeleteMapping("/session/{sessionId}")
    public Object closeSession(@PathVariable("sessionId") String sessionId, @RequestHeader HttpHeaders httpHeaders) {
        final String clientIp = XtreamWebUtils.getClientIp(httpHeaders::getFirst).orElse("unknown");
        final boolean success = sessionManager.closeSessionById(
                sessionId,
                new Jt1078DashboardSimpleValues.Jt1078DashboardSessionCloseReason("ClosedByDashboardApi", clientIp)
        );
        return Map.of(
                "success", success,
                "id", sessionId
        );
    }

    Predicate<Jt1078Session> createFilter(Jt1078SessionQueryDto dto) {
        Predicate<Jt1078Session> predicate = it -> true;
        if (StringUtils.hasText(dto.getTerminalId())) {
            predicate = predicate.and(it -> it.terminalId().toLowerCase().contains(dto.getTerminalId().toLowerCase()));
        }
        if (dto.getProtocolType() != null) {
            predicate = predicate.and(it -> it.type() == dto.getProtocolType());
        }
        return predicate;
    }

    Jt1078SessionVo convertToVo(Jt1078Session it) {
        return new Jt1078SessionVo()
                .setId(it.id())
                .setTerminalId(it.terminalId())
                .setProtocolType(it.type())
                .setCreationTime(it.creationTime())
                .setLastCommunicateTime(it.lastCommunicateTime())
                ;
    }
}
