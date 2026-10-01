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

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicBitOrder;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicCondition;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 回填区间包含条件字段和嵌套容器的测试。
 * 断言回填的是实际出现字段的字节数，且编码阶段可引用尚待完成的长度字段。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicComputedLengthTest {
    @Test
    void shouldKeepBitOrderAfterPatchingLengthInsideSharedByte() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("size", "u8").computedLengthOf("a")
                        .options(Map.of("lengthUnit", "bit")).build(),
                DynamicField.field("a", "u3").bitOrder(DynamicBitOrder.LSB0).build(),
                DynamicField.field("b", "u5").bitOrder(DynamicBitOrder.LSB0).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("a", 5, "b", 17)), buffer);
            assertEquals("038d", ByteBufUtil.hexDump(buffer));
            assertEquals(Map.of("size", 3L, "a", 5L, "b", 17L), plan.decode(buffer).values().fields());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldPatchActualEncodedLengthAcrossFields() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("size", "u16").computedLengthOf("first..last").build(),
                DynamicField.field("first", "u8").build(),
                DynamicField.field("last", "struct").fields(
                        DynamicField.field("value", "u16").build()).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("first", 1, "last", Map.of("value", 0x1234))), buffer);
            assertEquals("0003011234", ByteBufUtil.hexDump(buffer));
            assertEquals(3L, plan.decode(buffer).values().fields().get("size"));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldResolveLengthReferenceToPendingValueDuringEncoding() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("size", "u8").computedLengthOf("payload").build(),
                DynamicField.field("payload", "hex").length(DynamicLength.ref("size", "byte")).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("payload", "aabbcc")), buffer);
            assertEquals("03aabbcc", ByteBufUtil.hexDump(buffer));
            assertEquals("aabbcc", plan.decode(buffer).values().fields().get("payload"));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldMeasureOnlyPresentConditionalFields() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("size", "u8").computedLengthOf("first..last").build(),
                DynamicField.field("flags", "u8").build(),
                DynamicField.field("first", "u8").build(),
                DynamicField.field("optional", "u8")
                        .when(new DynamicCondition("flags", "bitSet", "1")).build(),
                DynamicField.field("last", "struct").fields(
                        DynamicField.field("number", "u16").build()).build()));
        for (int flags : new int[]{0, 1}) {
            final ByteBuf buffer = Unpooled.buffer();
            try {
                plan.encode(DynamicValues.of(Map.of("flags", flags, "first", 7, "optional", 9,
                        "last", Map.of("number", 0x1234))), buffer);
                assertEquals(flags == 0 ? "0300071234" : "040107091234", ByteBufUtil.hexDump(buffer));
                assertEquals(flags == 0 ? 3L : 4L, plan.decode(buffer).values().fields().get("size"));
            } finally {
                buffer.release();
            }
        }
    }
}
