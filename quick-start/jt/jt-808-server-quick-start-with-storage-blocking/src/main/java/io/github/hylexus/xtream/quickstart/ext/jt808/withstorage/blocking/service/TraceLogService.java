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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service;

import io.github.hylexus.xtream.codec.base.web.domain.vo.PageableVo;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.dto.Jt808TraceLogDto;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.event.Jt808EventPayloads;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.vo.Jt808TraceLogVo;

/**
 * @author hylexus
 */
public interface TraceLogService {

    void afterRequestDecode(Jt808EventPayloads.Jt808ReceiveEvent event);

    void beforeResponseSend(Jt808EventPayloads.Jt808SendEvent event);

    PageableVo<Jt808TraceLogVo> listTraceLog(Jt808TraceLogDto dto);

}
