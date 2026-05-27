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

import io.github.hylexus.xtream.codec.base.web.annotation.ClientIp;
import io.github.hylexus.xtream.codec.base.web.domain.vo.PageableVo;
import io.github.hylexus.xtream.codec.base.web.exception.XtreamHttpException;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto.Jt808SessionQueryDto;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.Jt808DashboardSimpleValues;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.vo.Jt808SessionVo;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808AttachmentSessionManager;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionManager;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionManager;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author hylexus
 */
@RestController
@RequestMapping("/dashboard-api/jt808/v1/session")
public class BuiltinJt808DashboardSessionController {
    private final Jt808SessionManager sessionManager;
    private final Jt808AttachmentSessionManager attachmentSessionManager;

    public BuiltinJt808DashboardSessionController(Jt808SessionManager sessionManager, Jt808AttachmentSessionManager attachmentSessionManager) {
        this.sessionManager = sessionManager;
        this.attachmentSessionManager = attachmentSessionManager;
    }

    @GetMapping("/instruction-sessions")
    public PageableVo<Jt808SessionVo> instructionSessionList(Jt808SessionQueryDto dto) {
        return this.doSearch(dto, this.sessionManager);
    }

    @GetMapping("/attachment-sessions")
    public PageableVo<Jt808SessionVo> attachmentSessionList(Jt808SessionQueryDto dto) {
        return this.doSearch(dto, this.attachmentSessionManager);
    }

    @DeleteMapping("/instruction-session/{sessionId}")
    public Mono<Map<String, Boolean>> deleteInstructionSession(@PathVariable("sessionId") String sessionId, @ClientIp String clientIp) {
        return this.closeSession(sessionId, this.sessionManager, clientIp);
    }

    @DeleteMapping("/attachment-session/{sessionId}")
    public Mono<Map<String, Boolean>> deleteAttachmentSession(@PathVariable("sessionId") String sessionId, @ClientIp String clientIp) {
        return this.closeSession(sessionId, this.attachmentSessionManager, clientIp);
    }

    private Mono<Map<String, Boolean>> closeSession(String sessionId, XtreamSessionManager<Jt808Session> manager, String clientIp) {
        return manager.getSessionById(sessionId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(XtreamHttpException.notFound("No session found with sessionId: " + sessionId))))
                .map(session -> {
                    final boolean closed = manager.closeSessionById(sessionId, new Jt808DashboardSimpleValues.Jt808DashboardSessionCloseReason("ClosedByDashboardApi", clientIp));
                    return Map.of("closed", closed);
                });
    }

    private PageableVo<Jt808SessionVo> doSearch(Jt808SessionQueryDto dto, XtreamSessionManager<Jt808Session> manager) {
        final Predicate<Jt808Session> filter = createFilter(dto);
        final long total = manager.count(filter);
        if (total <= 0) {
            return PageableVo.empty();
        }
        final List<Jt808SessionVo> list = manager
                .list(dto.getPageNumber(), dto.getPageSize(), filter, this::convertToVo)
                .toList();
        return PageableVo.of(total, list);
    }

    Predicate<Jt808Session> createFilter(Jt808SessionQueryDto dto) {
        Predicate<Jt808Session> predicate = it -> true;
        if (StringUtils.hasText(dto.getTerminalId())) {
            predicate = predicate.and(it -> it.terminalId().toLowerCase().contains(dto.getTerminalId().toLowerCase()));
        }
        if (dto.getServerType() != null) {
            predicate = predicate.and(it -> it.role() == dto.getServerType());
        }
        if (dto.getProtocolVersion() != null) {
            predicate = predicate.and(it -> it.protocolVersion() == dto.getProtocolVersion());
        }
        if (dto.getProtocolType() != null) {
            predicate = predicate.and(it -> it.type() == dto.getProtocolType());
        }
        return predicate;
    }

    Jt808SessionVo convertToVo(Jt808Session it) {
        return new Jt808SessionVo()
                .setId(it.id())
                .setTerminalId(it.terminalId())
                .setServerType(it.role())
                .setProtocolVersion(it.protocolVersion())
                .setProtocolType(it.type())
                .setCreationTime(it.creationTime())
                .setLastCommunicateTime(it.lastCommunicateTime())
                ;
    }
}
