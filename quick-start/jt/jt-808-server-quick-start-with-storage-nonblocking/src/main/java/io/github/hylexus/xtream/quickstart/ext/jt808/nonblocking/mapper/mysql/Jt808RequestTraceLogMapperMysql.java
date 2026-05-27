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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.mapper.mysql;

import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.dto.Jt808TraceLogDto;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.entity.Jt808RequestTraceLogEntity;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.vo.Jt808TraceLogVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author hylexus
 */
@Mapper
public interface Jt808RequestTraceLogMapperMysql {

    @Select("""
            select *
            from jt_808_request_trace_log
            where id = #{id}
            """)
    Mono<Jt808RequestTraceLogEntity> findById(@Param("id") String id);

    Mono<Void> insert(Jt808RequestTraceLogEntity requestLog);

    Mono<Long> countTraceLog(@Param("dto") Jt808TraceLogDto dto);

    Flux<Jt808TraceLogVo> listTraceLog(@Param("dto") Jt808TraceLogDto dto);

}
