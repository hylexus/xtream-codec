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

package io.github.hylexus.xtream.codec.ext;

import jakarta.annotation.Nonnull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;

import java.util.function.Consumer;

public class MockNettyInbound implements NettyInbound {
    public MockNettyInbound() {
    }

    @Nonnull
    @Override
    public ByteBufFlux receive() {
        return ByteBufFlux.fromInbound(Mono.empty());
    }

    @Nonnull
    @Override
    public Flux<?> receiveObject() {
        return Flux.empty();
    }

    @Nonnull
    @Override
    public NettyInbound withConnection(@Nonnull Consumer<? super Connection> consumer) {
        return this;
    }
}
