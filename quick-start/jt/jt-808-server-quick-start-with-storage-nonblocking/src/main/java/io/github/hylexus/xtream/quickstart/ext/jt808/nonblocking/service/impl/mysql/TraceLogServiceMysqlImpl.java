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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.impl.mysql;

import io.github.hylexus.xtream.codec.base.web.domain.vo.PageableVo;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageDescriptionRegistry;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.converter.Jt808EntityConverter;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.dto.Jt808TraceLogDto;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.entity.Jt808RequestTraceLogEntity;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.entity.Jt808ResponseTraceLogEntity;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.event.Jt808EventPayloads;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.vo.Jt808TraceLogVo;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.mapper.mysql.Jt808RequestTraceLogMapperMysql;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.mapper.mysql.Jt808ResponseTraceLogMapperMysql;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.TraceLogService;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.utils.DatabaseRouter;
import reactor.core.publisher.Mono;

/**
 * @author hylexus
 */
public class TraceLogServiceMysqlImpl implements TraceLogService {
    private final Jt808MessageDescriptionRegistry messageDescriptionRegistry;
    private final Jt808RequestTraceLogMapperMysql requestTraceLogMapper;
    private final Jt808ResponseTraceLogMapperMysql responseTraceLogMapper;

    public TraceLogServiceMysqlImpl(
            Jt808MessageDescriptionRegistry messageDescriptionRegistry,
            Jt808RequestTraceLogMapperMysql requestTraceLogMapper,
            Jt808ResponseTraceLogMapperMysql responseTraceLogMapper) {
        this.messageDescriptionRegistry = messageDescriptionRegistry;
        this.requestTraceLogMapper = requestTraceLogMapper;
        this.responseTraceLogMapper = responseTraceLogMapper;
    }

    @Override
    public Mono<Boolean> afterRequestDecode(Jt808EventPayloads.Jt808ReceiveEvent event) {
        final Jt808RequestTraceLogEntity entity = Jt808EntityConverter.toRequestLogEntity(event);
        // return this.traceLogMapper.insert(entity).then(Mono.just(true));
        final Mono<Void> insertOperation = this.requestTraceLogMapper.insert(entity);
        return DatabaseRouter.TRACE_LOG_MYSQL.executeMono(insertOperation)
                .then(Mono.just(true));
    }

    @Override
    public Mono<Boolean> beforeResponseSend(Jt808EventPayloads.Jt808SendEvent event) {
        final Jt808ResponseTraceLogEntity entity = Jt808EntityConverter.toResponseLogEntity(event);
        return DatabaseRouter.TRACE_LOG_MYSQL.executeMono(this.responseTraceLogMapper.insert(entity))
                .then(Mono.just(true));
    }

    @Override
    public Mono<PageableVo<Jt808TraceLogVo>> listTraceLog(Jt808TraceLogDto dto) {
        return DatabaseRouter.TRACE_LOG_MYSQL.executeMono(this.requestTraceLogMapper.countTraceLog(dto)).flatMap(total -> {
            if (total <= 0) {
                return Mono.just(PageableVo.empty());
            }
            return DatabaseRouter.TRACE_LOG_MYSQL.executeFlux(this.requestTraceLogMapper.listTraceLog(dto)).collectList().map(voList -> {
                Jt808EntityConverter.setMessageDescription(voList, this.messageDescriptionRegistry::getDescription, "未知");
                return PageableVo.of(total, voList);
            });
        });
    }

}
