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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.impl.postgres;

import cn.mybatis.mp.datasource.routing.DS;
import io.github.hylexus.xtream.codec.base.web.domain.vo.PageableVo;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageDescriptionRegistry;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.converter.Jt808EntityConverter;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.dto.Jt808TraceLogDto;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.entity.Jt808RequestTraceLogEntity;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.entity.Jt808ResponseTraceLogEntity;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.event.Jt808EventPayloads;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.vo.Jt808TraceLogVo;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.mapper.postgres.Jt808RequestTraceLogMapperPostgres;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.mapper.postgres.Jt808ResponseTraceLogMapperPostgres;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.TraceLogService;

import java.util.List;

@DS("trace-log-postgres")
public class TraceLogServicePostgresImpl implements TraceLogService {
    private final Jt808MessageDescriptionRegistry messageDescriptionRegistry;
    private final Jt808RequestTraceLogMapperPostgres requestTraceLogMapper;
    private final Jt808ResponseTraceLogMapperPostgres responseTraceLogMapper;

    public TraceLogServicePostgresImpl(Jt808MessageDescriptionRegistry messageDescriptionRegistry, Jt808RequestTraceLogMapperPostgres requestTraceLogMapper, Jt808ResponseTraceLogMapperPostgres responseTraceLogMapper) {
        this.messageDescriptionRegistry = messageDescriptionRegistry;
        this.requestTraceLogMapper = requestTraceLogMapper;
        this.responseTraceLogMapper = responseTraceLogMapper;
    }

    @Override
    public void afterRequestDecode(Jt808EventPayloads.Jt808ReceiveEvent event) {
        final Jt808RequestTraceLogEntity entity = Jt808EntityConverter.toRequestLogEntity(event);
        this.requestTraceLogMapper.insert(entity);
    }

    @Override
    public void beforeResponseSend(Jt808EventPayloads.Jt808SendEvent event) {
        final Jt808ResponseTraceLogEntity entity = Jt808EntityConverter.toResponseLogEntity(event);
        this.responseTraceLogMapper.insert(entity);
    }

    @Override
    public PageableVo<Jt808TraceLogVo> listTraceLog(Jt808TraceLogDto dto) {
        // noinspection Duplicates
        final long total = this.requestTraceLogMapper.countTraceLog(dto);
        if (total <= 0) {
            return PageableVo.empty();
        }
        final List<Jt808TraceLogVo> voList = this.requestTraceLogMapper.listTraceLog(dto);
        Jt808EntityConverter.setMessageDescription(voList, this.messageDescriptionRegistry::getDescription, "未知");
        return PageableVo.of(total, voList);
    }
}
