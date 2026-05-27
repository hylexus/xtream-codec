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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.mapper.mysql;

import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.dto.Jt808TraceLogDto;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.entity.Jt808RequestTraceLogEntity;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.vo.Jt808TraceLogVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author hylexus
 */
@Mapper
public interface Jt808RequestTraceLogMapperMysql {

    void insert(Jt808RequestTraceLogEntity requestLog);

    long countTraceLog(@Param("dto") Jt808TraceLogDto dto);

    List<Jt808TraceLogVo> listTraceLog(@Param("dto") Jt808TraceLogDto dto);

}
