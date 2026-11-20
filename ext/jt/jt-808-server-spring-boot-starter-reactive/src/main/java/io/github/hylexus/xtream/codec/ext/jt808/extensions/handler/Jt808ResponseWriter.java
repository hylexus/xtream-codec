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
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamResponse;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

final class Jt808ResponseWriter {
    private Jt808ResponseWriter() {
    }

    static Mono<Void> write(
            XtreamResponse response, Jt808Request request,
            Jt808RequestLifecycleListener listener, Supplier<ByteBuf> encode) {

        return Mono.using(
                () -> new AtomicReference<@Nullable ByteBuf>(encode.get()),
                (AtomicReference<@Nullable ByteBuf> pending) -> {
                    listener.beforeResponseSend(request, Objects.requireNonNull(pending.get()));
                    // 关闭的连接可以拒绝发送而不订阅。只有发送器取走缓冲区时才交接所有权。
                    return response.writeWith(Mono.fromSupplier(() -> {
                        @SuppressWarnings("NullAway") final ByteBuf bufferToSend = pending.getAndSet(null);
                        return bufferToSend;
                    }));
                },
                (AtomicReference<@Nullable ByteBuf> pending) -> {
                    final ByteBuf remainingBuffer = pending.getAndSet(null);
                    if (remainingBuffer != null) {
                        ReferenceCountUtil.release(remainingBuffer);
                    }
                }
        );
    }
}
