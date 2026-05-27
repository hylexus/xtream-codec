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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.impl.composite;

import io.github.hylexus.xtream.codec.base.web.domain.vo.PageableVo;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.dto.Jt808TraceLogDto;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.event.Jt808EventPayloads;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.vo.Jt808TraceLogVo;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.TraceLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author hylexus
 */
public class TraceLogServiceComposite implements TraceLogService {
    private static final Logger log = LoggerFactory.getLogger(TraceLogServiceComposite.class);
    private final List<TraceLogService> delegates;

    public TraceLogServiceComposite(List<TraceLogService> delegates) {
        this.delegates = delegates;
    }

    @Override
    public void afterRequestDecode(Jt808EventPayloads.Jt808ReceiveEvent event) {
        for (final TraceLogService service : this.delegates) {
            try {
                service.afterRequestDecode(event);
            } catch (Throwable e) {
                log.error("error occurred while do traceLog", e);
            }
        }
    }

    @Override
    public void beforeResponseSend(Jt808EventPayloads.Jt808SendEvent event) {
        for (final TraceLogService service : this.delegates) {
            try {
                service.beforeResponseSend(event);
            } catch (Throwable e) {
                log.error("error occurred while do traceLog", e);
            }
        }
    }

    @Override
    public PageableVo<Jt808TraceLogVo> listTraceLog(Jt808TraceLogDto dto) {
        throw new UnsupportedOperationException();
    }

}
