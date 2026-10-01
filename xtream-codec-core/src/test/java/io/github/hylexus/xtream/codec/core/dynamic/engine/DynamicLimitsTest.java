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
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 资源上限与嵌套失败位置测试。
 * 确认超长声明在分配内容数组之前失败，集合计数和嵌套深度都遵守单次执行限制。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicLimitsTest {
    @Test
    void shouldRejectHugeDeclaredLengthBeforeAllocating() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("size", "u32").build(),
                DynamicField.field("body", "bytes").length(DynamicLength.ref("size", "byte")).build()));
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{0x7f, -1, -1, -1});
        try {
            final CodecTracker tracker = new CodecTracker();
            final DynamicCodecException error = assertThrows(DynamicCodecException.class, () -> plan.decode(buffer, tracker));
            assertEquals("$.body", error.fieldPath());
            assertEquals(32, error.bitOffset());
            assertEquals(1, tracker.toTraceView().diagnostics().size());
            assertEquals(1, buffer.refCnt());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldLimitTotalCollectionItemsAndNesting() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("items", "array").length(DynamicLength.fixed(2, "item"))
                        .item(DynamicField.field("item", "struct").fields(
                                DynamicField.field("value", "u8").build()).build()).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            final DynamicValues values = DynamicValues.of(Map.of("items", List.of(Map.of("value", 1), Map.of("value", 2))));
            assertThrows(DynamicCodecException.class,
                    () -> plan.encode(values, buffer, new DynamicLimits(10, 10, 1, 10)));
            buffer.clear();
            assertThrows(DynamicCodecException.class,
                    () -> plan.encode(values, buffer, new DynamicLimits(10, 10, 10, 1)));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldRejectOversizedByteValuesBeforeWriting() {
        final DynamicPlan stringPlan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("text", "string").length(DynamicLength.fixed(6, "byte")).build()));
        final DynamicPlan bytesPlan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("data", "bytes").length(DynamicLength.fixed(1, "byte")).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            stringPlan.encode(DynamicValues.of(Map.of("text", "中文")), buffer,
                    new DynamicLimits(10, 6, 10, 10));
            assertEquals(6, buffer.readableBytes());
            buffer.clear();

            final DynamicCodecException stringError = assertThrows(DynamicCodecException.class,
                    () -> stringPlan.encode(DynamicValues.of(Map.of("text", "中文")), buffer,
                            new DynamicLimits(10, 5, 10, 10)));
            assertEquals("$.text", stringError.fieldPath());
            assertEquals(0, buffer.writerIndex());

            final DynamicCodecException bytesError = assertThrows(DynamicCodecException.class,
                    () -> bytesPlan.encode(DynamicValues.of(Map.of("data", List.of(1, 2))), buffer));
            assertEquals("$.data", bytesError.fieldPath());
            assertEquals(0, buffer.writerIndex());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldLocateNestedTruncatedBitField() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("header", "u8").build(),
                DynamicField.field("body", "struct").fields(
                        DynamicField.field("first", "u3").build(),
                        DynamicField.field("second", "u12").build(),
                        DynamicField.field("align", "align").alignTo(8).build()).build()));
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{1, (byte) 0xa0});
        try {
            final CodecTracker tracker = new CodecTracker();
            final DynamicCodecException error = assertThrows(DynamicCodecException.class, () -> plan.decode(buffer, tracker));
            assertEquals("$.body.second", error.fieldPath());
            assertEquals(11, error.bitOffset());
            assertEquals(1L, tracker.toTraceView().root().children().getFirst().value());
            assertEquals(5L, tracker.toTraceView().root().children().get(1).children().getFirst().value());
        } finally {
            buffer.release();
        }
    }
}
