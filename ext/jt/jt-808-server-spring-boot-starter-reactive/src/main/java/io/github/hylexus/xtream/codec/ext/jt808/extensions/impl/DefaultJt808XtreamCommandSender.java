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
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author hylexus
 */
public class DefaultJt808XtreamCommandSender implements Jt808CommandSender {
    private static final Logger log = LoggerFactory.getLogger(DefaultJt808XtreamCommandSender.class);
    protected final ByteBufAllocator bufferFactory;
    protected final Jt808SessionManager sessionManager;
    protected final Jt808ResponseEncoder jt808ResponseEncoder;
    protected final Jt808FlowIdGenerator flowIdGenerator;
    private final ConcurrentMap<Jt808CommandKey, Sinks.One<Object>> pendingResponses = new ConcurrentHashMap<>();
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
        return this.getSessionById(sessionId).flatMap(session -> bodyMono.flatMap(body -> {
            final String terminalId = session.terminalId();
            final Jt808ProtocolVersion version = session.protocolVersion();
            final int flowId = this.flowIdGenerator.nextFlowId();
            final Jt808CommandKey commandKey = Jt808CommandKey.of(terminalId, messageId, flowId);

            return Mono.using(
                    () -> {
                        // 终端可能在写入完成前就回包，先登记等待项才能保留提前到达的应答。
                        final Sinks.One<Object> clientResponse = Sinks.one();
                        if (this.pendingResponses.putIfAbsent(commandKey, clientResponse) != null) {
                            throw new IllegalStateException("A command is already awaiting a response for key " + commandKey);
                        }
                        log.info("[CommandSender][{}] Registered response waiter", commandKey);
                        return clientResponse;
                    },
                    (Sinks.One<Object> clientResponse) -> {
                        final Mono<ByteBuf> buffer = Mono.defer(() -> this.encodeJt808Message(version, terminalId, flowId, body));
                        return this.doWrite(buffer, session)
                                .then(clientResponse.asMono()
                                        .doOnSubscribe(ignored -> log.info("[CommandSender][{}] Waiting for terminal response", commandKey))
                                );
                    },
                    (Sinks.One<Object> response) -> {
                        if (this.pendingResponses.remove(commandKey, response)) {
                            log.info("[CommandSender][{}] Removed response waiter", commandKey);
                        }
                    }
            ).map(value -> {
                @SuppressWarnings("unchecked") final R response = (R) value;
                return response;
            });
        }));
    }

    @Override
    public <R> Mono<R> sendObjectAndWaitingResponseWithTerminalId(String terminalId, Mono<Object> body, int messageId) {
        // 终端可能在创建 Mono 后、订阅前重连。等到订阅时再查找会话，避免向已断开的旧会话下发。
        return Mono.defer(() -> {
            final Optional<Jt808Session> optionalJt808Session = this.sessionManager.findByTerminalId(terminalId);
            if (optionalJt808Session.isEmpty()) {
                return Mono.error(new Jt808SessionNotFoundException(terminalId));
            }
            final Jt808Session jt808Session = optionalJt808Session.get();
            return this.sendObjectAndWaitingResponse(jt808Session.id(), body, messageId);
        });
    }

    @Override
    public void setClientResponse(Jt808CommandKey commandKey, @Nullable Object clientResponse) {
        final Sinks.One<Object> sink = this.pendingResponses.get(commandKey);
        log.info("[CommandSender][{}] Received terminal response (waiterPresent={})", commandKey, sink != null);
        if (sink == null) {
            return;
        }

        // 已取消的等待项会被移除，重复应答由单值 sink 忽略
        if (clientResponse == null) {
            sink.tryEmitEmpty();
        } else {
            sink.tryEmitValue(clientResponse);
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
        return Mono.just(buffer);
    }

    protected Mono<Jt808Session> getSessionById(String sessionId) {
        return this.sessionManager.getSessionById(sessionId).switchIfEmpty(Mono.defer(() -> {
            // ...
            return Mono.error(new IllegalArgumentException("No session found for sessionId: " + sessionId));
        }));
    }

}
