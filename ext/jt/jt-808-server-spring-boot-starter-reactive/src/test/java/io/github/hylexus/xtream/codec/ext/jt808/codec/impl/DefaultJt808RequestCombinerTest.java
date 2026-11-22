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

package io.github.hylexus.xtream.codec.ext.jt808.codec.impl;

import com.github.benmanes.caffeine.cache.RemovalCause;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.ext.jt808.boot.configuration.XtreamExtJt808ServerAutoConfiguration;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestCombiner;
import io.github.hylexus.xtream.codec.ext.jt808.processor.BuiltinJt808ServerDefaultEnvironmentPostProcessor;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ServerType;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808MessageBodyProps;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808RequestHeader;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808SubPackageProps;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class DefaultJt808RequestCombinerTest {

    @Test
    void closingContextReleasesIncompleteGroupsAndPreservesMergedBody() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(XtreamExtJt808ServerAutoConfiguration.class))
                .withInitializer(context -> new BuiltinJt808ServerDefaultEnvironmentPostProcessor()
                        .postProcessEnvironment(context.getEnvironment(), new SpringApplication()))
                .withPropertyValues(
                        "jt808-server.instruction-server.tcp-server.enabled=false",
                        "jt808-server.instruction-server.udp-server.enabled=false",
                        "jt808-server.attachment-server.tcp-server.enabled=false",
                        "jt808-server.attachment-server.udp-server.enabled=false"
                )
                .run(context -> {
                    final var combiner = context.getBean(Jt808RequestCombiner.class);
                    final var incomplete = fragment(0x0704, 1, 10, 0x11);
                    final var first = fragment(0x0200, 1, 42, 0x21);
                    final var second = fragment(0x0200, 2, 43, 0x22);
                    final var incompleteBody = incomplete.body();
                    final var firstBody = first.body();
                    final var secondBody = second.body();
                    try {
                        final Jt808Request merged;
                        try {
                            assertNull(combiner.tryMergeSubPackage(incomplete));
                            assertNull(combiner.tryMergeSubPackage(first));
                            merged = combiner.tryMergeSubPackage(second);
                        } finally {
                            incomplete.release();
                            first.release();
                            second.release();
                        }
                        assertNotNull(merged);
                        try {
                            assertEquals(1, incompleteBody.refCnt());
                            context.close();
                            assertEquals(0, incompleteBody.refCnt());
                            assertEquals(1, firstBody.refCnt());
                            assertEquals(1, secondBody.refCnt());
                            assertEquals(0x2122, merged.body().readUnsignedShort());
                        } finally {
                            merged.release();
                        }
                        assertEquals(0, firstBody.refCnt());
                        assertEquals(0, secondBody.refCnt());
                    } finally {
                        context.close();
                        XtreamBytes.releaseBuf(incompleteBody);
                        XtreamBytes.releaseBuf(firstBody);
                        XtreamBytes.releaseBuf(secondBody);
                    }
                });
    }

    @ParameterizedTest
    @EnumSource(value = RemovalCause.class, names = {"EXPIRED", "SIZE"})
    void releasesIncompleteGroupAndMergesNextGroupAfterRemoval(RemovalCause cause) {
        final var ttl = Duration.ofMinutes(1);
        final var combiner = new DefaultJt808RequestCombiner(ByteBufAllocator.DEFAULT, 1, ttl);
        final var cache = combiner.cache.cache;
        final var expiry = cache.policy().expireAfterWrite().orElseThrow();
        final var eviction = cache.policy().eviction().orElseThrow();
        try {
            final var incomplete = fragment(0x0200, 1, 10, 0x11);
            final var cachedBody = incomplete.body();
            try {
                assertNull(combiner.tryMergeSubPackage(incomplete));
                assertEquals(2, cachedBody.refCnt());
            } finally {
                incomplete.release();
            }
            assertEquals(1, cachedBody.refCnt());

            if (cause == RemovalCause.EXPIRED) {
                expiry.setExpiresAfter(Duration.ZERO);
            } else {
                eviction.setMaximum(0);
            }
            cache.cleanUp();
            await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> assertEquals(0, cachedBody.refCnt()));

            expiry.setExpiresAfter(ttl);
            eviction.setMaximum(1);
            assertNextGroupMerges(combiner);
        } finally {
            eviction.setMaximum(0);
            cache.cleanUp();
        }
    }

    private static void assertNextGroupMerges(DefaultJt808RequestCombiner combiner) {
        final var first = fragment(0x0200, 1, 42, 0x21);
        final var second = fragment(0x0200, 2, 43, 0x22);
        final var firstBody = first.body();
        final var secondBody = second.body();
        try {
            assertNull(combiner.tryMergeSubPackage(second));
            final var merged = combiner.tryMergeSubPackage(first);
            assertNotNull(merged);
            try {
                assertEquals(42, merged.header().flowId());
                assertEquals(2, merged.body().readableBytes());
                assertEquals(0x2122, merged.body().readUnsignedShort());
            } finally {
                merged.release();
            }
        } finally {
            first.release();
            second.release();
        }
        assertEquals(0, firstBody.refCnt());
        assertEquals(0, secondBody.refCnt());
    }

    private static Jt808Request fragment(int messageId, int packageNo, int flowId, int value) {
        final var version = Jt808ProtocolVersion.VERSION_2019;
        final var header = new DefaultJt808RequestHeader(version, messageId, new DefaultJt808MessageBodyProps(0x2001),
                "00000000000000000001", flowId, new DefaultJt808SubPackageProps(2, packageNo));
        return new DefaultJt808Request(version.versionValue(), Jt808ServerType.INSTRUCTION_SERVER, "request", "trace",
                ByteBufAllocator.DEFAULT, null, XtreamInbound.Type.TCP, Unpooled.buffer(1).writeByte(value), null,
                new InetSocketAddress(InetAddress.getLoopbackAddress(), 12345), header, 0, 0);
    }
}
