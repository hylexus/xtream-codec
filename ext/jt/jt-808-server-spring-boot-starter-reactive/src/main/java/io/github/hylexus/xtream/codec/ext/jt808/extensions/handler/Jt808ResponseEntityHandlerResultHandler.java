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

package io.github.hylexus.xtream.codec.ext.jt808.extensions.handler;

import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808ResponseEncoder;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageDescriber;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ResponseEntity;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResult;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResultHandler;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;

/**
 * @author hylexus
 */
public class Jt808ResponseEntityHandlerResultHandler implements XtreamHandlerResultHandler {
    protected final Jt808ResponseEncoder jt808ResponseEncoder;
    protected final Jt808RequestLifecycleListener lifecycleListener;

    public Jt808ResponseEntityHandlerResultHandler(Jt808ResponseEncoder jt808ResponseEncoder, Jt808RequestLifecycleListener lifecycleListener) {
        this.jt808ResponseEncoder = jt808ResponseEncoder;
        this.lifecycleListener = lifecycleListener;
    }

    @Override
    public boolean supports(XtreamHandlerResult result) {
        if (result.getReturnType().getParameterType() == Jt808ResponseEntity.class) {
            return true;
        }
        if (result.getReturnType().getGenericType().isEmpty()) {
            return false;
        }
        if (result.getReturnType().getGenericType().getFirst() instanceof ParameterizedType parameterizedType) {
            return parameterizedType.getRawType() == Jt808ResponseEntity.class;
        }
        return false;
    }

    @Override
    public Mono<Void> handleResult(XtreamExchange exchange, XtreamHandlerResult result) {
        return this.adaptReturnValue(result.getReturnValue()).flatMap(responseEntity -> {
            final Jt808Request request = exchange.request().castAs(Jt808Request.class);
            final Jt808MessageDescriber messageDescriber = this.createMessageDescriptor(responseEntity, request);
            final ByteBuf responseBuf = this.jt808ResponseEncoder.encode(responseEntity.getBody(), messageDescriber);
            this.lifecycleListener.beforeResponseSend(request, responseBuf);
            return exchange.response().writeWith(Mono.just(responseBuf));
        });
    }

    protected Jt808MessageDescriber createMessageDescriptor(Jt808ResponseEntity<Object> responseEntity, Jt808Request request) {
        return new Jt808MessageDescriber(responseEntity.getMessageId(), request.header().version(), request.header().terminalId())
                .maxPackageSize(responseEntity.getMaxPackageSize())
                .reversedBit15InHeader(responseEntity.getReversedBit15InHeader())
                .encryptionType(responseEntity.getEncryptionType())
                .flowId(responseEntity.getFlowId());
    }

    protected Mono<Jt808ResponseEntity<Object>> adaptReturnValue(@Nullable Object returnValue) {
        return switch (returnValue) {
            case null -> Mono.error(new IllegalArgumentException("message is null"));
            case Mono<?> mono -> mono.map(Jt808ResponseEntityHandlerResultHandler::cast);
            case Flux<?> ignored -> Mono.error(() -> new IllegalArgumentException("Flux is not supported"));
            case Object object -> Mono.just(cast(object));
        };
    }

    @SuppressWarnings("unchecked")
    static Jt808ResponseEntity<Object> cast(Object returnValue) {
        if (!(returnValue instanceof Jt808ResponseEntity<?>)) {
            throw new IllegalArgumentException("returnValue must be instanceof Jt808ResponseEntity in this case. Actual: " + returnValue.getClass());
        }
        return (Jt808ResponseEntity<Object>) returnValue;
    }
}
