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

package io.github.hylexus.xtream.codec.ext.jt808.extensions.filter;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.ServerCommonReplyMessage;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.codec.impl.DefaultJt808RequestCombiner;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ServerType;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808MessageBodyProps;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808RequestHeader;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808SubPackageProps;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamResponse;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSession;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamExchange;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class Jt808RequestCombinerFilterTest {

    @ParameterizedTest
    @EnumSource(DownstreamResult.class)
    void shouldKeepFirstPackageFlowIdAndReleaseMergedBody(DownstreamResult result) {
        final var combiner = new DefaultJt808RequestCombiner(ByteBufAllocator.DEFAULT, 32, Duration.ofMinutes(1));
        final var observed = new AtomicReference<Jt808Request>();
        final var failure = new IllegalStateException("测试处理失败");
        final var filter = new Jt808RequestCombinerFilter(combiner, new Jt808RequestLifecycleListener() {
            @Override
            public void afterSubPackageMerged(XtreamExchange exchange, Jt808Request request) {
                observed.set(request);
                if (result == DownstreamResult.LISTENER_ERROR) {
                    throw failure;
                }
            }
        });
        final var first = fragment(1, 42);
        final var second = fragment(2, 43);
        final boolean inOrder = result == DownstreamResult.SUCCESS;
        try {
            filter.filter(exchange(inOrder ? first : second), ignored -> fail("分包尚未收齐")).block();
            final var exchange = exchange(inOrder ? second : first);
            final Mono<Void> handling = Mono.defer(() -> filter.filter(exchange, mergedExchange -> switch (result) {
                case SUCCESS -> Mono.empty();
                case CHAIN_ERROR -> throw failure;
                case CANCELLED -> Mono.never();
                case LISTENER_ERROR -> fail("监听器异常后仍执行了下游");
            }));
            if (result == DownstreamResult.CANCELLED) {
                handling.subscribe().dispose();
            } else if (result == DownstreamResult.SUCCESS) {
                handling.block();
            } else {
                assertSame(failure, assertThrows(IllegalStateException.class, handling::block));
            }
            final var merged = observed.get();
            assertNotNull(merged);
            assertEquals(42, ServerCommonReplyMessage.success(merged).getClientFlowId());
            assertEquals(0, merged.body().refCnt());
        } finally {
            second.release();
            first.release();
            final var merged = observed.get();
            if (merged != null && merged.body().refCnt() > 0) {
                merged.release();
            }
        }
    }

    private static XtreamExchange exchange(Jt808Request request) {
        return new DefaultXtreamExchange(mock(XtreamSession.class), request, mock(XtreamResponse.class));
    }

    private static Jt808Request fragment(int packageNo, int flowId) {
        final var version = Jt808ProtocolVersion.VERSION_2019;
        final var header = new DefaultJt808RequestHeader(version, 0x0200, new DefaultJt808MessageBodyProps(0x2001),
                "00000000000000000001", flowId, new DefaultJt808SubPackageProps(2, packageNo));
        return new DefaultJt808Request(version.versionValue(), Jt808ServerType.INSTRUCTION_SERVER, "request", "trace",
                ByteBufAllocator.DEFAULT, null, XtreamInbound.Type.TCP, Unpooled.buffer(1).writeByte(packageNo), null,
                new InetSocketAddress(InetAddress.getLoopbackAddress(), 12345), header, 0, 0);
    }

    enum DownstreamResult {
        SUCCESS, LISTENER_ERROR, CHAIN_ERROR, CANCELLED
    }
}
