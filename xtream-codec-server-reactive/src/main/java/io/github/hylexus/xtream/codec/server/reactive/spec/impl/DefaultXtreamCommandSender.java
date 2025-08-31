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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl;

import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamCommandSender;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSession;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author hylexus
 */
public class DefaultXtreamCommandSender implements XtreamCommandSender<XtreamSession> {

    protected final ByteBufAllocator bufferFactory;
    protected final XtreamSessionManager<XtreamSession> sessionManager;
    protected final EntityCodec entityCodec;

    public DefaultXtreamCommandSender(ByteBufAllocator bufferFactory, XtreamSessionManager<XtreamSession> sessionManager, EntityCodec entityCodec) {
        this.bufferFactory = bufferFactory;
        this.sessionManager = sessionManager;
        this.entityCodec = entityCodec;
    }

    @Override
    public ByteBufAllocator bufferFactory() {
        return this.bufferFactory;
    }

    @Override
    public XtreamSessionManager<XtreamSession> sessionManager() {
        return this.sessionManager;
    }

    @Override
    public Mono<Void> sendByteBuf(String sessionId, Publisher<? extends ByteBuf> data) {
        return getSession(sessionId)
                .flatMap(session -> {
                    // ...
                    return session.writeWith(data).then();
                });
    }

    @Override
    public Mono<Void> sendObject(int version, String sessionId, Publisher<Object> data) {
        return this.getSession(sessionId).flatMap(session -> {
            // ...
            return Flux.from(data).map(d -> this.doEncode(version, d)).flatMap(byteBuf -> {
                // ...
                return session.writeWith(Mono.just(byteBuf));
            }).then();
        }).then();
    }

    protected ByteBuf doEncode(int version, Object message) {
        if (message instanceof ByteBuf byteBuf) {
            return byteBuf;
        }
        final ByteBuf buffer = this.bufferFactory.buffer();
        try {
            this.entityCodec.encode(version, message, buffer);
        } catch (Throwable e) {
            buffer.release();
            throw new RuntimeException(e);
        }
        return buffer;
    }

    protected Mono<XtreamSession> getSession(String sessionId) {
        return this.sessionManager.getSessionById(sessionId)
                .switchIfEmpty(Mono.defer(() -> {
                    // ... todo exception
                    return Mono.error(new IllegalArgumentException("No session found for sessionId: " + sessionId));
                }));
    }
}
