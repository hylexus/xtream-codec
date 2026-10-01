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

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicCondition;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import io.github.hylexus.xtream.codec.core.dynamic.spi.*;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBufUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 节点组合语义回归，使用独立输入检查消费量和错误位置。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicCompositionTest {
    @Test
    void skippedIntegerMustNotSynthesizeReverseViews() {
        final var skip = new DynamicCondition("flag", "eq", "1");
        final var plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("flag", "u8").build(),
                DynamicField.field("source", "u8").when(skip).build(),
                DynamicField.field("view", "derived").ref("source").when(skip)
                        .options(Map.of("bitOffset", 0, "bitWidth", 1)).build()));
        final var buffer = Unpooled.buffer();
        try {
            assertEquals(8, plan.encode(DynamicValues.of(Map.of("flag", 0, "view", 99)), buffer));
            assertEquals("00", ByteBufUtil.hexDump(buffer));
            assertEquals(Map.of("flag", 0L), plan.decode(buffer).values().fields());
        } finally {
            buffer.release();
        }
    }

    @Test
    void extensionNormalizationMustBeDemandDriven() {
        final AtomicInteger writes = new AtomicInteger();
        final AtomicInteger reads = new AtomicInteger();
        final var extension = new DynamicExtension() {
            @Override
            public void validate(Map<String, Object> config) {
            }

            @Override
            public byte[] encode(Object value, Map<String, Object> config) {
                writes.incrementAndGet();
                return new byte[]{((Number) value).byteValue()};
            }

            @Override
            public Object decode(byte[] bytes, Map<String, Object> config) {
                reads.incrementAndGet();
                return (long) Byte.toUnsignedInt(bytes[0]);
            }
        };
        final var registry = new DynamicExtensionRegistry(Map.of("test", extension));
        final var source = DynamicField.field("source", "extension").length(DynamicLength.fixed(1, "byte"))
                .options(Map.of("extensionId", "test")).build();
        final var simple = DynamicCompiler.compile(DynamicSchema.of(source), registry);
        final var dependent = DynamicCompiler.compile(DynamicSchema.of(source,
                DynamicField.field("a", "u8").when(new DynamicCondition("source", "eq", "7")).build(),
                DynamicField.field("b", "u8").when(new DynamicCondition("source", "eq", "7")).build()), registry);
        final var buffer = Unpooled.buffer();
        try {
            simple.encode(DynamicValues.of(Map.of("source", 7)), buffer);
            assertEquals(0, reads.get());
            buffer.clear();
            dependent.encode(DynamicValues.of(Map.of("source", 7, "a", 1, "b", 2)), buffer, new CodecTracker());
            assertEquals("070102", ByteBufUtil.hexDump(buffer));
            assertEquals(1, reads.get());
            assertEquals(2, writes.get());
        } finally {
            buffer.release();
        }
    }

    @Test
    void unknownTlvMustCheckSizeBeforeHexConversion() {
        final var plan = DynamicCompiler.compile(DynamicSchema.of(DynamicField.field("entries", "tlv")
                .length(DynamicLength.fixed(1, "item")).variants(Map.of()).build()));
        final var buffer = Unpooled.buffer();
        try {
            // 无效 HEX 同时超长时应先报告边界，证明没有先调用完整字节转换。
            final var error = assertThrows(DynamicCodecException.class, () -> plan.encode(
                    DynamicValues.of(Map.of("entries", List.of(Map.of("key", 1, "rawHex", "zz".repeat(256))))),
                    buffer, new DynamicLimits(8, 8, 8, 8)));
            assertTrue(error.getMessage().contains("边界"));
            assertEquals("$.entries", error.fieldPath());
            assertEquals(2, buffer.writerIndex());
        } finally {
            buffer.release();
        }
    }

    @Test
    void repeatedBackfillsMustHandleSkippedEndpoints() {
        final var item = DynamicField.field("item", "struct").fields(
                DynamicField.field("flag", "u8").build(),
                DynamicField.field("size", "u8").computedLengthOf("first..last").build(),
                DynamicField.field("first", "u8").when(new DynamicCondition("flag", "eq", "1")).build(),
                DynamicField.field("middle", "u8").build(),
                DynamicField.field("last", "u8").when(new DynamicCondition("flag", "eq", "1")).build()).build();
        final var plan = DynamicCompiler.compile(DynamicSchema.of(DynamicField.field("items", "array")
                .length(DynamicLength.fixed(2, "item")).item(item).build()));
        final var buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("items", List.of(Map.of("flag", 0, "middle", 9),
                    Map.of("flag", 1, "first", 2, "middle", 3, "last", 4)))), buffer);
            assertEquals("0001090103020304", ByteBufUtil.hexDump(buffer));
            final var items = (List<?>) plan.decode(buffer).values().fields().get("items");
            assertEquals(1L, ((Map<?, ?>) items.get(0)).get("size"));
            assertEquals(3L, ((Map<?, ?>) items.get(1)).get("size"));
        } finally {
            buffer.release();
        }
    }

    @Test
    void arrayAndTlvMustHonorItemConditions() {
        final var branch = DynamicField.field("value", "u8").when(new DynamicCondition("flag", "eq", "1")).build();
        for (final var collection : List.of(
                DynamicField.field("items", "array").length(DynamicLength.fixed(1, "item")).item(branch).build(),
                DynamicField.field("items", "tlv").length(DynamicLength.fixed(1, "item")).variants(Map.of("1", branch)).build())) {
            final var plan = DynamicCompiler.compile(DynamicSchema.of(DynamicField.field("flag", "u8").build(), collection));
            final var buffer = Unpooled.wrappedBuffer(new byte[]{0, 1, 1, 42});
            try {
                final var error = assertThrows(DynamicCodecException.class, () -> plan.decode(buffer));
                assertEquals("$.items", error.fieldPath());
                assertEquals(0, buffer.readerIndex());
            } finally {
                buffer.release();
            }
        }
    }

    @Test
    void choiceMustHonorSelectedBranchCondition() {
        final var branch = DynamicField.field("value", "u8")
                .when(new DynamicCondition("flag", "eq", "1")).build();
        final var plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("flag", "u8").build(),
                DynamicField.field("choice", "choice").ref("flag").variants(Map.of("0", branch)).build()));
        final var buffer = Unpooled.wrappedBuffer(new byte[]{0, 42});
        try {
            final var result = plan.decode(buffer);
            assertEquals(Map.of("flag", 0L), result.values().fields());
            assertEquals(8, result.consumedBits());
            assertEquals(1, result.remainingBytes());
        } finally {
            buffer.release();
        }
    }

    @Test
    void paddingArrayMustPreserveNullValue() {
        final var plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("items", "array").length(DynamicLength.fixed(1, "item"))
                        .item(DynamicField.field("padding", "padding").length(DynamicLength.fixed(1, "byte")).build()).build()));
        final var buffer = Unpooled.wrappedBuffer(new byte[]{0});
        try {
            assertEquals(Collections.singletonList(null), plan.decode(buffer).values().fields().get("items"));
            assertEquals(1, buffer.readerIndex());
        } finally {
            buffer.release();
        }
    }

    @Test
    void invalidByteElementMustReportContainingField() {
        final var plan = DynamicCompiler.compile(DynamicSchema.of(DynamicField.field("data", "bytes")
                .length(DynamicLength.fixed(1, "byte")).build()));
        final var buffer = Unpooled.buffer();
        try {
            final var error = assertThrows(DynamicCodecException.class,
                    () -> plan.encode(DynamicValues.of(Map.of("data", List.of("bad"))), buffer));
            assertEquals("$.data", error.fieldPath());
            assertEquals(0, error.bitOffset());
            assertNotNull(error.getCause());
        } finally {
            buffer.release();
        }
    }

    @Test
    void missingStringMustReportFieldLocation() {
        final var plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("name", "string").length(DynamicLength.fixed(1, "byte")).build()));
        final var buffer = Unpooled.buffer();
        try {
            final var error = assertThrows(DynamicCodecException.class,
                    () -> plan.encode(DynamicValues.of(Map.of()), buffer));
            assertEquals("$.name", error.fieldPath());
            assertEquals(0, error.bitOffset());
        } finally {
            buffer.release();
        }
    }
}
