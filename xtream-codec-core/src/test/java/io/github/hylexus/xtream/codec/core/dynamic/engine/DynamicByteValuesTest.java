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

package io.github.hylexus.xtream.codec.core.dynamic.engine;

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 字符串、BCD、字节数组和填充的边界测试。
 * 同时验证非法字符不会被替换、短值只按显式规则填充及输入不足的定位。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicByteValuesTest {
    private static DynamicLayout.ByteFormat format(DynamicField field) {
        return ((DynamicLayout.BytesOp) DynamicCompiler.compile(DynamicSchema.of(field)).fields().getFirst().operation()).format();
    }

    @Test
    void shouldCheckByteListSizeBeforeAllocatingOrReadingItems() {
        // 用虚拟列表验证检查顺序，无须真的分配超大输入或依赖内存耗尽来判断。
        final List<Integer> huge = new AbstractList<>() {
            @Override
            public Integer get(int index) {
                throw new AssertionError("超限列表不应读取元素");
            }

            @Override
            public int size() {
                return Integer.MAX_VALUE;
            }
        };
        final DynamicField field = DynamicField.field("data", "bytes").length(DynamicLength.remaining("byte")).build();
        assertThrows(IllegalArgumentException.class, () -> DynamicByteFormat.encode(format(field), huge, 8));
    }

    @Test
    void shouldGrowStringBufferWithinLimitAndHandleEmptyValue() {
        final DynamicField field = DynamicField.field("text", "string").length(DynamicLength.remaining("byte")).build();
        final String text = "中".repeat(40);
        assertEquals(text, DynamicByteFormat.decode(format(field), DynamicByteFormat.encode(format(field), text, 120)));
        assertThrows(IllegalArgumentException.class, () -> DynamicByteFormat.encode(format(field), text, 119));
        assertArrayEquals(new byte[0], DynamicByteFormat.encode(format(field), "", 0));
        assertThrows(IllegalArgumentException.class, () -> DynamicByteFormat.encode(format(field), "a", 0));
    }

    @Test
    void shouldRoundTripMixedByteFields() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("utf8", "string").length(DynamicLength.prefix(8, "byte")).build(),
                DynamicField.field("gbk", "string").charset("GBK").length(DynamicLength.fixed(4, "byte")).build(),
                DynamicField.field("bcd", "bcd").length(DynamicLength.fixed(3, "byte")).build(),
                DynamicField.field("hex", "hex").length(DynamicLength.fixed(2, "byte")).build(),
                DynamicField.field("bytes", "bytes").length(DynamicLength.fixed(2, "byte")).build(),
                DynamicField.field("pad", "padding").length(DynamicLength.fixed(2, "byte")).options(Map.of("padByte", 255)).build()));
        final Map<String, Object> values = Map.of("utf8", "中文", "gbk", "中文", "bcd", "001234", "hex", "abcd", "bytes", List.of(0, 255));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(values), buffer);
            assertEquals("06e4b8ade69687d6d0cec4001234abcd00ffffff", ByteBufUtil.hexDump(buffer));
            final Map<String, Object> decoded = plan.decode(buffer).values().fields();
            values.forEach((key, value) -> assertEquals(value, decoded.get(key)));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldPadAndStripExplicitPadding() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("text", "string").length(DynamicLength.fixed(4, "byte"))
                        .options(Map.of("padByte", 32, "padSide", "left", "stripPadding", true)).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("text", "ab")), buffer);
            assertEquals("20206162", ByteBufUtil.hexDump(buffer));
            assertEquals("ab", plan.decode(buffer).values().fields().get("text"));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldRejectInvalidCharactersAndTruncation() {
        final DynamicPlan text = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("text", "string").charset("US-ASCII").length(DynamicLength.fixed(1, "byte")).build()));
        final DynamicPlan bcd = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("text", "bcd").length(DynamicLength.fixed(1, "byte")).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            assertThrows(DynamicCodecException.class, () -> text.encode(DynamicValues.of(Map.of("text", "中")), buffer));
            assertThrows(DynamicCodecException.class, () -> bcd.encode(DynamicValues.of(Map.of("text", "1a")), buffer));
            assertThrows(DynamicCodecException.class, () -> bcd.decode(buffer));
            buffer.writeByte(0xff);
            assertThrows(DynamicCodecException.class, () -> bcd.decode(buffer));
            assertThrows(DynamicCodecException.class, () -> text.decode(buffer));
        } finally {
            buffer.release();
        }
    }
}
