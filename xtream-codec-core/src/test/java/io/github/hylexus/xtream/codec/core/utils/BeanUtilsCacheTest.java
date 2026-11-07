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

package io.github.hylexus.xtream.codec.core.utils;

import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.impl.DefaultFieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.impl.SimpleBeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

/**
 * 内省缓存的过滤器隔离、禁用策略和失败重试测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class BeanUtilsCacheTest {

    @Test
    void sameClassAndFilterReuseBeanInfoWhileClassesStaySeparate() {
        final Predicate<AnnotatedElement> filter = element -> true;
        final var first = BeanUtils.getBeanInfo(ReusablePojo.class, type -> true, filter);
        assertThat(first.getPropertyDescriptors()).extracting(BeanUtils.BasicPropertyDescriptor::getName).containsExactly("value");
        assertThat(BeanUtils.getBeanInfo(ReusablePojo.class, type -> true, filter)).isSameAs(first);
        assertThat(BeanUtils.getBeanInfo(OtherPojo.class, type -> true, filter)).isNotSameAs(first);
    }

    @Test
    void filtersForSameClassDoNotReuseEachOthersDescriptors() {
        final Predicate<AnnotatedElement> firstOnly = element -> ((Field) element).getName().equals("first");
        final Predicate<AnnotatedElement> secondOnly = element -> ((Field) element).getName().equals("second");
        final var first = BeanUtils.getBeanInfo(FilteredPojo.class, type -> true, firstOnly);
        final var second = BeanUtils.getBeanInfo(FilteredPojo.class, type -> true, secondOnly);
        final var all = BeanUtils.getBeanInfo(FilteredPojo.class, type -> true, element -> true);
        final var empty = BeanUtils.getBeanInfo(FilteredPojo.class, type -> true, element -> false);
        assertThat(first.getPropertyDescriptors()).extracting(BeanUtils.BasicPropertyDescriptor::getName).containsExactly("first");
        assertThat(second.getPropertyDescriptors()).extracting(BeanUtils.BasicPropertyDescriptor::getName).containsExactly("second");
        assertThat(all.getPropertyDescriptors()).extracting(BeanUtils.BasicPropertyDescriptor::getName).containsExactly("first", "second");
        assertThat(empty.getPropertyDescriptors()).isEmpty();
        assertThat(BeanUtils.getBeanInfo(FilteredPojo.class, type -> true, firstOnly)).isSameAs(first);
        assertThat(BeanUtils.getBeanInfo(FilteredPojo.class, type -> true, secondOnly)).isSameAs(second);
    }

    @Test
    void disabledCacheBypassesExistingEntryWithoutReplacingIt() {
        final AtomicInteger visits = new AtomicInteger();
        final Predicate<AnnotatedElement> filter = element -> {
            visits.incrementAndGet();
            return true;
        };
        final var cached = BeanUtils.getBeanInfo(BypassedPojo.class, type -> true, filter);
        final var first = BeanUtils.getBeanInfo(BypassedPojo.class, type -> false, filter);
        final var second = BeanUtils.getBeanInfo(BypassedPojo.class, type -> false, filter);
        assertThat(first).isNotSameAs(cached).isNotSameAs(second);
        assertThat(second).isNotSameAs(cached);
        assertThat(visits).hasValue(3);
        assertThat(BeanUtils.getBeanInfo(BypassedPojo.class, type -> true, filter)).isSameAs(cached);
        assertThat(visits).hasValue(3);
    }

    @Test
    void disabledCacheDoesNotPopulateFutureCachedRequests() {
        final Predicate<AnnotatedElement> filter = element -> true;
        final var first = BeanUtils.getBeanInfo(UncachedPojo.class, type -> false, filter);
        final var second = BeanUtils.getBeanInfo(UncachedPojo.class, type -> false, filter);
        final var cached = BeanUtils.getBeanInfo(UncachedPojo.class, type -> true, filter);
        assertThat(cached).isNotSameAs(first).isNotSameAs(second);
        assertThat(BeanUtils.getBeanInfo(UncachedPojo.class, type -> true, filter)).isSameAs(cached);
    }

    @Test
    void failedFilterDoesNotPublishPartialBeanInfoAndCanBeRetried() {
        final AtomicBoolean fail = new AtomicBoolean(true);
        final IllegalArgumentException failure = new IllegalArgumentException("测试字段过滤失败");
        final Predicate<AnnotatedElement> filter = element -> {
            if (((Field) element).getName().equals("second") && fail.getAndSet(false)) {
                throw failure;
            }
            return true;
        };
        assertThatThrownBy(() -> BeanUtils.getBeanInfo(RetriedPojo.class, type -> true, filter)).isSameAs(failure);
        final var result = BeanUtils.getBeanInfo(RetriedPojo.class, type -> true, filter);
        assertThat(result.getPropertyDescriptors()).extracting(BeanUtils.BasicPropertyDescriptor::getName).containsExactly("first", "second");
        assertThat(BeanUtils.getBeanInfo(RetriedPojo.class, type -> true, filter)).isSameAs(result);
    }

    @Test
    void earlierCustomFilterCannotHideFieldsFromEntityCodec() {
        BeanUtils.getBeanInfo(CodecPojo.class, type -> true, element -> ((Field) element).getName().equals("first"));
        final EntityCodec codec = new EntityCodec(new SimpleBeanMetadataRegistry(new DefaultFieldCodecRegistry(), type -> true));
        final ByteBuf buffer = Unpooled.buffer().writeByte(3).writeShort(0x1234).writeByte(0x66);
        try {
            final CodecPojo result = codec.decode(CodecPojo.class, buffer);
            assertThat(result.first).isEqualTo((short) 3);
            assertThat(result.second).isEqualTo(0x1234);
            assertThat(buffer.readerIndex()).isEqualTo(3);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
        } finally {
            buffer.release();
        }
    }

    // 字段只供内省扫描读取，使用独立类型避免静态缓存造成测试顺序依赖。
    public static class ReusablePojo {
        public int value;
    }

    public static class OtherPojo {
        public int value;
    }

    public static class FilteredPojo {
        public int first;
        public int second;
    }

    public static class BypassedPojo {
        public int value;
    }

    public static class UncachedPojo {
        public int value;
    }

    public static class RetriedPojo {
        public int first;
        public int second;
    }

    public static class CodecPojo {
        @Preset.RustStyle.u8
        public short first;
        @Preset.RustStyle.u16
        public int second;
    }
}
