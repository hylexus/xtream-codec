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

package io.github.hylexus.xtream.quickstart.ext.jt1078.nonblocking;

import io.github.hylexus.xtream.codec.base.web.domain.values.XtreamApiErrorCode;
import io.github.hylexus.xtream.codec.base.web.domain.values.XtreamApiProblemDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Controller
@RestControllerAdvice
public class Jt1078ServerQuickStartNonblockingAppGlobalWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(Jt1078ServerQuickStartNonblockingAppGlobalWebExceptionHandler.class);

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<XtreamApiProblemDetails> handleThrowable(Throwable ex) {
        log.error(ex.getMessage(), ex);
        final XtreamApiProblemDetails problemDetails = XtreamApiProblemDetails.newBuilderFrom(XtreamApiErrorCode.SERVER_ERROR)
                .detail(ex.getMessage())
                .build();
        return problemDetails.toResponseEntity();
    }

}
