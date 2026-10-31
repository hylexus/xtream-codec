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

package io.github.hylexus.xtream.codec.core.impl;

import io.github.hylexus.xtream.codec.common.bean.BeanMetadata;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * 实体元数据的缓存复用、版本隔离、并发创建与失败重试测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class SimpleBeanMetadataRegistryTest {

    @Test
    void cachesByClassAndVersionAndUsesTheSelectedWireFormat() {
        final SimpleBeanMetadataRegistry registry = registry(true);
        final BeanMetadata first = registry.getBeanMetadata(Versioned.class, 1);
        assertThat(registry.getBeanMetadata(Versioned.class, 1)).isSameAs(first);
        final BeanMetadata second = registry.getBeanMetadata(Versioned.class, 2);
        assertThat(second).isNotSameAs(first);
        assertThat(registry.getBeanMetadata(Versioned.class, 2)).isSameAs(second);
        assertThat(registry.getBeanMetadata(Other.class, 1)).isNotSameAs(first);
        assertThat(registry.beanDescriptors()).hasSize(3);
        final EntityCodec codec = new EntityCodec(registry);
        final ByteBuf buffer = Unpooled.buffer();
        try {
            codec.encode(1, new Versioned(0x12), buffer);
            codec.encode(2, new Versioned(0x3456), buffer);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("123456");
            assertThat(codec.decode(1, Versioned.class, buffer)).isEqualTo(new Versioned(0x12));
            assertThat(codec.decode(2, Versioned.class, buffer)).isEqualTo(new Versioned(0x3456));
            assertThat(buffer.isReadable()).isFalse();
        } finally {
            buffer.release();
        }
    }

    @Test
    void disabledCacheAndSeparateRegistriesDoNotShareMetadata() {
        final SimpleBeanMetadataRegistry uncached = registry(false);
        assertThat(uncached.getBeanMetadata(Versioned.class, 1)).isNotSameAs(uncached.getBeanMetadata(Versioned.class, 1));
        assertThat(uncached.beanDescriptors()).isEmpty();
        final SimpleBeanMetadataRegistry first = registry(true);
        final SimpleBeanMetadataRegistry second = registry(true);
        assertThat(first.getBeanMetadata(Versioned.class, 1)).isNotSameAs(second.getBeanMetadata(Versioned.class, 1));
    }

    @Test
    void concurrentFirstRequestsCreateOneMetadataPerVersion() throws Exception {
        final SimpleBeanMetadataRegistry registry = registry(true);
        final AtomicInteger creations = new AtomicInteger();
        final CountDownLatch ready = new CountDownLatch(8);
        final CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(8)) {
            final List<Future<List<BeanMetadata>>> results = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
                    final List<BeanMetadata> metadata = new ArrayList<>();
                    for (int version = 1; version <= 2; version++) {
                        metadata.add(registry.getBeanMetadata(Versioned.class, version, property -> {
                            creations.incrementAndGet();
                            return registry.createBeanPropertyMetadata(property);
                        }));
                    }
                    return metadata;
                }));
            }
            try {
                assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            } finally {
                start.countDown();
            }
            for (final var result : results) {
                final List<BeanMetadata> metadata = result.get(10, TimeUnit.SECONDS);
                assertThat(metadata.getFirst()).isSameAs(registry.getBeanMetadata(Versioned.class, 1));
                assertThat(metadata.getLast()).isSameAs(registry.getBeanMetadata(Versioned.class, 2));
            }
            assertThat(creations).hasValue(2);
        }
    }

    @Test
    void failedCreationDoesNotPublishPartialMetadataAndCanBeRetried() {
        final SimpleBeanMetadataRegistry registry = registry(true);
        final IllegalArgumentException failure = new IllegalArgumentException("测试元数据创建失败");
        assertThatThrownBy(() -> registry.getBeanMetadata(Versioned.class, 1, property -> {
            throw failure;
        })).isSameAs(failure);
        assertThat(registry.beanDescriptors()).isEmpty();
        final BeanMetadata metadata = registry.getBeanMetadata(Versioned.class, 1);
        assertThat(metadata.getPropertyMetadataList()).hasSize(1);
        assertThat(registry.getBeanMetadata(Versioned.class, 1)).isSameAs(metadata);
        assertThat(registry.beanDescriptors()).hasSize(1);
    }

    private static SimpleBeanMetadataRegistry registry(boolean cache) {
        return new SimpleBeanMetadataRegistry(new DefaultFieldCodecRegistry(), type -> cache);
    }

    public record Versioned(@Preset.RustStyle.u8(version = 1) @Preset.RustStyle.u16(version = 2) int value) {
    }

    public record Other(@Preset.RustStyle.u8 int value) {
    }
}
