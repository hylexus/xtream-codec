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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.impl.logging;

import io.github.hylexus.xtream.codec.base.web.domain.vo.PageableVo;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.dto.Jt808TraceLogDto;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.event.Jt808EventPayloads;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.vo.Jt808TraceLogVo;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.TraceLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * @author hylexus
 */
public class TraceLogServiceLoggingImpl implements TraceLogService {

    private static final Logger log = LoggerFactory.getLogger(TraceLogServiceLoggingImpl.class);

    @Override
    public Mono<Boolean> afterRequestDecode(Jt808EventPayloads.Jt808ReceiveEvent event) {
        log.info("==> traceLog:onPackageReceived {}", event);
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> beforeResponseSend(Jt808EventPayloads.Jt808SendEvent event) {
        log.info("<== traceLog:onPackageSent {}", event);
        return Mono.just(true);
    }

    @Override
    public Mono<PageableVo<Jt808TraceLogVo>> listTraceLog(Jt808TraceLogDto dto) {
        return Mono.error(new UnsupportedOperationException());
    }

}
