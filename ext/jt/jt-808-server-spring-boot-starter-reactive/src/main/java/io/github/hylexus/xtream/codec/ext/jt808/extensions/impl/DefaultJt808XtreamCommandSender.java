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

package io.github.hylexus.xtream.codec.ext.jt808.extensions.impl;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808ResponseEncoder;
import io.github.hylexus.xtream.codec.ext.jt808.exception.Jt808SessionNotFoundException;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.Jt808CommandSender;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808FlowIdGenerator;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionManager;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author hylexus
 */
public class DefaultJt808XtreamCommandSender implements Jt808CommandSender {
    private static final Logger log = LoggerFactory.getLogger(DefaultJt808XtreamCommandSender.class);
    protected final ByteBufAllocator bufferFactory;
    protected final Jt808SessionManager sessionManager;
    protected final Jt808ResponseEncoder jt808ResponseEncoder;
    protected final Jt808FlowIdGenerator flowIdGenerator;
    private final Map<Jt808CommandKey, MonoSink<Object>> sinkMap = new HashMap<>();
    protected final Jt808RequestLifecycleListener requestLifecycleListener;

    public DefaultJt808XtreamCommandSender(ByteBufAllocator bufferFactory, Jt808SessionManager sessionManager, Jt808ResponseEncoder jt808ResponseEncoder, Jt808FlowIdGenerator flowIdGenerator, Jt808RequestLifecycleListener requestLifecycleListener) {
        this.bufferFactory = bufferFactory;
        this.sessionManager = sessionManager;
        this.jt808ResponseEncoder = jt808ResponseEncoder;
        this.flowIdGenerator = flowIdGenerator;
        this.requestLifecycleListener = requestLifecycleListener;
    }

    @Override
    public ByteBufAllocator bufferFactory() {
        return this.bufferFactory;
    }

    @Override
    public XtreamSessionManager<Jt808Session> sessionManager() {
        return this.sessionManager;
    }

    @Override
    public Mono<Void> sendByteBuf(String sessionId, Publisher<? extends ByteBuf> data) {
        return this.getSessionById(sessionId).flatMap(session -> {
            // ...
            return this.doWrite(data, session);
        });
    }

    @Override
    public Mono<Void> sendObject(String sessionId, Mono<Object> bodyMono) {
        return this.getSessionById(sessionId).flatMap(session -> {
            final String terminalId = session.terminalId();
            final Jt808ProtocolVersion version = session.protocolVersion();
            return bodyMono.flatMap(body -> {
                final Mono<ByteBuf> buffer = this.encodeJt808Message(version, terminalId, -1, body);
                return this.doWrite(buffer, session);
            });
        });
    }

    @Override
    public <R> Mono<R> sendObjectAndWaitingResponse(String sessionId, Mono<Object> bodyMono, int messageId) {
        return this.getSessionById(sessionId).flatMap(session -> {
            final String terminalId = session.terminalId();
            final Jt808ProtocolVersion version = session.protocolVersion();
            final int flowId = this.flowIdGenerator.nextFlowId();
            final Jt808CommandKey commandKey = Jt808CommandKey.of(terminalId, messageId, flowId);

            final Mono<R> mono = Mono.create(sink -> {
                // ...
                sinkMap.put(commandKey, sink);
            }).map(it -> {
                @SuppressWarnings("unchecked") R r = (R) it;
                return r;
            }).doFinally(signalType -> {
                // ...
                sinkMap.remove(commandKey);
            });

            return bodyMono.flatMap(body -> {
                final Mono<ByteBuf> buffer = this.encodeJt808Message(version, terminalId, flowId, body);
                return this.doWrite(buffer, session);
            }).then(mono).doFinally(signalType -> {
                // ...
                sinkMap.remove(commandKey);
            });
        });
    }

    @Override
    public <R> Mono<R> sendObjectAndWaitingResponseWithTerminalId(String terminalId, Mono<Object> body, int messageId) {
        final Optional<Jt808Session> optionalJt808Session = this.sessionManager.findByTerminalId(terminalId);
        if (optionalJt808Session.isEmpty()) {
            return Mono.error(new Jt808SessionNotFoundException(terminalId));
        }
        final Jt808Session jt808Session = optionalJt808Session.get();
        return this.sendObjectAndWaitingResponse(jt808Session.id(), body, messageId);
    }

    @Override
    public void setClientResponse(Jt808CommandKey commandKey, Object clientResponse) {
        final MonoSink<Object> sink = this.sinkMap.get(commandKey);
        if (sink != null) {
            sink.success(clientResponse);
        }
    }

    protected Mono<Void> doWrite(Publisher<? extends ByteBuf> data, Jt808Session session) {
        return switch (data) {
            case Mono<? extends ByteBuf> mono -> session.writeWith(mono.doOnNext(command -> this.requestLifecycleListener.beforeCommandSend(session, command)));
            case Flux<? extends ByteBuf> flux -> session.writeWith(flux.doOnNext(command -> this.requestLifecycleListener.beforeCommandSend(session, command)));
            default -> throw new IllegalArgumentException("Unsupported data type: " + data.getClass());
        };
    }


    protected Mono<ByteBuf> encodeJt808Message(Jt808ProtocolVersion version, String terminalId, int flowId, Object body) {
        final Jt808ResponseBody annotation = AnnotatedElementUtils.getMergedAnnotation(body.getClass(), Jt808ResponseBody.class);
        if (annotation == null) {
            return Mono.error(new IllegalArgumentException("No @Jt808ResponseBody annotation found on class: " + body.getClass().getName()));
        }
        final ByteBuf buffer;
        if (flowId >= 0) {
            buffer = this.jt808ResponseEncoder.encode(body, version, terminalId, flowId, annotation);
        } else {
            buffer = this.jt808ResponseEncoder.encode(body, version, terminalId, annotation);
        }
        log.info("command sender ::: {}", FormatUtils.toHexString(buffer));
        return Mono.just(buffer);
    }

    protected Mono<Jt808Session> getSessionById(String sessionId) {
        return this.sessionManager.getSessionById(sessionId).switchIfEmpty(Mono.defer(() -> {
            // ...
            return Mono.error(new IllegalArgumentException("No session found for sessionId: " + sessionId));
        }));
    }

}
