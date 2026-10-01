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
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 连续位字段与混合字节字段的双向测试。
 * 使用跨字节位段比较两种位序，并检查整字节字段前的对齐和填充值约束。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicBitsTest {
    @Test
    void shouldEncodeCrossByteFieldsWithBothBitOrders() {
        for (final String order : new String[]{"msb0", "lsb0"}) {
            final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                    DynamicField.field("a", "u3").bitOrder(order).build(),
                    DynamicField.field("b", "u5").bitOrder(order).build(),
                    DynamicField.field("c", "u12").bitOrder(order).build(),
                    DynamicField.field("align", "align").alignTo(8).bitOrder(order).fillBit(0).build(),
                    DynamicField.field("tail", "u8").build()));
            final ByteBuf buffer = Unpooled.buffer();
            try {
                plan.encode(DynamicValues.of(Map.of("a", 5, "b", 17, "c", 0xabc, "tail", 0x55)), buffer);
                assertEquals("msb0".equals(order) ? "b1abc055" : "8dbc0a55", ByteBufUtil.hexDump(buffer));
                final Map<String, Object> result = plan.decode(buffer).values().fields();
                assertEquals(5L, result.get("a"));
                assertEquals(17L, result.get("b"));
                assertEquals(0xabcL, result.get("c"));
                assertEquals(0x55L, result.get("tail"));
            } finally {
                buffer.release();
            }
        }
    }

    @Test
    void shouldRejectWrongAlignmentPaddingAndUnalignedByteFields() {
        final DynamicPlan aligned = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("a", "u3").build(),
                DynamicField.field("align", "align").alignTo(8).fillBit(1).build()));
        final DynamicPlan unaligned = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("a", "u3").build(),
                DynamicField.field("b", "u8").build()));
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{(byte) 0xa0, 1});
        try {
            final DynamicCodecException padding = assertThrows(DynamicCodecException.class, () -> aligned.decode(buffer));
            assertEquals("$.align", padding.fieldPath());
            assertEquals(3, padding.bitOffset());
            final DynamicCodecException alignment = assertThrows(DynamicCodecException.class, () -> unaligned.decode(buffer));
            assertEquals("$.b", alignment.fieldPath());
            assertEquals(3, alignment.bitOffset());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldSupportBitStringsWiderThanLong() {
        final String bits = "10100101".repeat(10);
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("value", "bits").bits(80).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("value", bits)), buffer);
            assertEquals("a5".repeat(10), ByteBufUtil.hexDump(buffer));
            assertEquals(bits, plan.decode(buffer).values().fields().get("value"));
        } finally {
            buffer.release();
        }
    }
}
