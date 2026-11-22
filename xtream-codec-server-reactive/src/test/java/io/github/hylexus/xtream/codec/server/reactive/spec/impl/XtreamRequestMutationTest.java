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

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchangeDecorator;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import static io.github.hylexus.xtream.codec.server.reactive.spec.impl.ReactiveTestFixtures.ADDRESS;
import static org.junit.jupiter.api.Assertions.*;

class XtreamRequestMutationTest {
    private static final InetSocketAddress OTHER_ADDRESS = new InetSocketAddress(InetAddress.getLoopbackAddress(), 9001);

    @Test
    void requestBuilderAppliesVersionAndAddressWithoutChangingTheOriginal() {
        final var original = ReactiveTestFixtures.exchange().request();
        final var built = original.mutate().version(81).remoteAddress(OTHER_ADDRESS).build();

        assertEquals(81, built.version());
        assertEquals(OTHER_ADDRESS, built.remoteAddress());
        assertEquals(7, original.version());
        assertEquals(ADDRESS, original.remoteAddress());
        assertSame(original.payload(), built.payload());
        assertEquals(original.requestId(), built.requestId());
    }

    @Test
    void replacingThePayloadReleasesItOnlyWhenRequested() {
        final var first = Unpooled.buffer(1).writeByte(22);
        final var second = Unpooled.buffer(1).writeByte(22);
        final var last = Unpooled.buffer(1).writeByte(33);
        try {
            final var builder = request(first).mutate();
            builder.payload(second);
            assertEquals(0, first.refCnt());

            final var newest = builder.payload(last, false).build();
            assertEquals(1, second.refCnt());
            assertSame(last, newest.payload());
            assertEquals(33, newest.payload().getUnsignedByte(0));
            newest.release();
            assertEquals(0, last.refCnt());
        } finally {
            releaseIfNecessary(first);
            releaseIfNecessary(second);
            releaseIfNecessary(last);
        }
    }

    @Test
    void keepingTheSamePayloadDoesNotReleaseTheOnlyReference() {
        final var payload = Unpooled.buffer(1).writeByte(42);
        try {
            final var built = request(payload).mutate().payload(payload).build();

            assertSame(payload, built.payload());
            assertEquals(1, payload.refCnt());
            assertEquals(42, built.payload().readUnsignedByte());
        } finally {
            releaseIfNecessary(payload);
        }
    }

    @Test
    void repeatedExchangeRequestMutationsComposeOnTheCurrentRequest() {
        final var original = ReactiveTestFixtures.exchange();
        final var replacement = new DefaultXtreamRequest(3, "replacement", ByteBufAllocator.DEFAULT, null, XtreamInbound.Type.UDP,
                Unpooled.EMPTY_BUFFER, null, OTHER_ADDRESS);

        final var mutated = original.mutate()
                .request(replacement)
                .request(builder -> builder.remoteAddress(ADDRESS))
                .request(builder -> builder.version(8))
                .build();

        assertEquals("replacement", mutated.request().requestId());
        assertEquals(XtreamInbound.Type.UDP, mutated.request().type());
        assertEquals(8, mutated.request().version());
        assertEquals(ADDRESS, mutated.request().remoteAddress());
        assertEquals(7, original.request().version());
        assertSame(original.session(), mutated.session());
        assertSame(original.attributes(), mutated.attributes());
    }

    @Test
    void mutatedResponseSuppliesTheExchangeAllocatorAndKeepsOtherContext() {
        final var original = ReactiveTestFixtures.exchange();
        original.attributes().put("trace", "value");
        final var allocator = new UnpooledByteBufAllocator(false);
        final var replacement = new DefaultXtreamResponse(allocator, null, XtreamInbound.Type.TCP, ADDRESS);

        final var mutated = original.mutate().response(replacement).build();

        assertSame(replacement, mutated.response());
        assertSame(allocator, mutated.bufferFactory());
        assertSame(original.request(), mutated.request());
        assertSame(original.attributes(), mutated.attributes());
        assertEquals("value", mutated.attributes().get("trace"));
    }

    @Test
    void requestOnlyMutationPreservesTheDelegateAllocatorOverride() {
        final var allocator = new UnpooledByteBufAllocator(false);
        final var original = new XtreamExchangeDecorator(ReactiveTestFixtures.exchange(), null, null) {
            @Override
            public ByteBufAllocator bufferFactory() {
                return allocator;
            }
        };

        assertSame(allocator, original.mutate().request(builder -> builder.remoteAddress(OTHER_ADDRESS)).build().bufferFactory());
    }

    private static DefaultXtreamRequest request(ByteBuf payload) {
        return new DefaultXtreamRequest(7, "request", ByteBufAllocator.DEFAULT, null, XtreamInbound.Type.TCP, payload, null, ADDRESS);
    }

    private static void releaseIfNecessary(ByteBuf payload) {
        if (payload.refCnt() > 0) {
            payload.release();
        }
    }
}
