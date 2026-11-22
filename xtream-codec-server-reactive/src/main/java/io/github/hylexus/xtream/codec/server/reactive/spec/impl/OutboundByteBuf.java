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


package io.github.hylexus.xtream.codec.server.reactive.spec.impl;

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

final class OutboundByteBuf {
    private OutboundByteBuf() {
    }

    static Mono<Void> write(ByteBuf buffer, Function<Mono<ByteBuf>, Mono<Void>> writer) {
        // 编码和出站接管已取用的引用，写入结束时仍未交接的引用由这里释放。
        return Mono.using(
                () -> new AtomicReference<@Nullable ByteBuf>(buffer),
                (AtomicReference<@Nullable ByteBuf> pending) -> writer.apply(Mono.fromSupplier(() -> {
                    @SuppressWarnings("NullAway") final ByteBuf acquired = pending.getAndSet(null);
                    return acquired;
                })),
                (AtomicReference<@Nullable ByteBuf> pending) -> XtreamBytes.releaseBuf(pending.getAndSet(null))
        );
    }
}
