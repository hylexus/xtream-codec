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
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 编译期规则测试：验证引用路径解析、不可变计划、非法字段组合与路径化错误。
 * 长度回填目标属于写入区间，不应被误判为需要提前解析的读取依赖。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicCompilerTest {
    @Test
    void shouldResolveReferencesAndFreezeFieldLists() {
        final List<DynamicField> fields = new ArrayList<>(List.of(
                DynamicField.field("size", "u8").build(),
                DynamicField.field("body", "bytes").length(DynamicLength.ref("size", "byte")).build()));
        final DynamicPlan plan = DynamicCompiler.compile(new DynamicSchema(1, fields));
        fields.clear();
        assertEquals(2, plan.fields().size());
        assertEquals(plan.fields().getFirst().address(),
                ((DynamicLayout.Referenced) ((DynamicLayout.BytesOp) plan.fields().get(1).operation()).length().source()).target());
        assertThrows(UnsupportedOperationException.class, () -> plan.fields().clear());
    }

    @Test
    void shouldKeepOriginalIntegerDefinitionInCompiledPlan() {
        // 执行原语与公开 DSL 分开保存，调用方看到的字段类型始终是 u16。
        final DynamicField definition = DynamicField.field("number", "u16").build();
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(definition));
        assertSame(definition, plan.schema().fields().getFirst());
        assertEquals("u16", plan.schema().fields().getFirst().type());
        final var operation = assertInstanceOf(DynamicLayout.IntegerOp.class, plan.fields().getFirst().operation());
        assertFalse(operation.signed());
        assertEquals(16, operation.bits());
    }

    @Test
    void shouldRejectDuplicateIdsWithPath() {
        final DynamicField field = DynamicField.field("size", "u8").build();
        assertFailure("$.size", "重复", DynamicSchema.of(field, field));
    }

    @Test
    void shouldRejectCyclesAndForwardReferences() {
        assertFailure("$.a", "循环", DynamicSchema.of(
                DynamicField.field("a", "derived").ref("b").options(Map.of("bitOffset", 0, "bitWidth", 1)).build(),
                DynamicField.field("b", "derived").ref("a").options(Map.of("bitOffset", 0, "bitWidth", 1)).build()));
        assertFailure("$.body", "已经可用", DynamicSchema.of(
                DynamicField.field("body", "bytes").length(DynamicLength.ref("size", "byte")).build(),
                DynamicField.field("size", "u8").build()));
    }

    @Test
    void shouldRejectInvalidOrMissingBoundaries() {
        assertThrows(IllegalArgumentException.class, () -> DynamicLength.fixed(2, "word"));
        assertThrows(IllegalArgumentException.class, () -> DynamicField.field("body", "bytes").build());
        assertFailure("$.body", "末尾", DynamicSchema.of(
                DynamicField.field("body", "bytes").length(DynamicLength.remaining("byte")).build(),
                DynamicField.field("tail", "u8").build()));
    }

    @Test
    void shouldRejectUnboundedStructWhoseChildConsumesRemainingInput() {
        assertFailure("$.body", "末尾", DynamicSchema.of(
                DynamicField.field("body", "struct").fields(
                        DynamicField.field("raw", "hex").length(DynamicLength.remaining("byte")).build()).build(),
                DynamicField.field("tail", "u8").build()));
        assertFailure("$.body", "末尾", DynamicSchema.of(
                DynamicField.field("body", "struct").fields(
                        DynamicField.field("nested", "struct").fields(
                                DynamicField.field("raw", "hex")
                                        .length(DynamicLength.remaining("byte")).build()).build()).build(),
                DynamicField.field("tail", "u8").build()));
    }

    @Test
    void shouldAllowRemainingChildWhenStructHasItsOwnBoundary() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("body", "struct").length(DynamicLength.fixed(1, "byte")).fields(
                        DynamicField.field("raw", "hex").length(DynamicLength.remaining("byte")).build()).build(),
                DynamicField.field("tail", "u8").build()));
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{(byte) 0xaa, (byte) 0xff});
        try {
            assertEquals(Map.of("body", Map.of("raw", "aa"), "tail", 255L),
                    plan.decode(buffer).values().fields());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldRejectCountedArrayWhoseItemConsumesRemainingInput() {
        assertFailure("$.items", "剩余", DynamicSchema.of(
                DynamicField.field("items", "array").length(DynamicLength.fixed(2, "item"))
                        .item(DynamicField.field("raw", "hex").length(DynamicLength.remaining("byte")).build()).build()));
        assertFailure("$.items", "末尾", DynamicSchema.of(
                DynamicField.field("items", "array").length(DynamicLength.fixed(1, "item"))
                        .item(DynamicField.field("raw", "hex").length(DynamicLength.remaining("byte")).build()).build(),
                DynamicField.field("tail", "u8").build()));
        assertFailure("$.items", "剩余", DynamicSchema.of(
                DynamicField.field("count", "u8").build(),
                DynamicField.field("items", "array").length(DynamicLength.ref("count", "item"))
                        .item(DynamicField.field("item", "struct").fields(
                                DynamicField.field("raw", "hex")
                                        .length(DynamicLength.remaining("byte")).build()).build()).build()));
    }

    @Test
    void shouldRejectChoiceBranchThatConsumesFollowingField() {
        assertFailure("$.body", "末尾", DynamicSchema.of(
                DynamicField.field("kind", "u8").build(),
                DynamicField.field("body", "choice").ref("kind").variants(Map.of(
                        "1", DynamicField.field("raw", "hex").length(DynamicLength.remaining("byte")).build(),
                        "2", DynamicField.field("small", "u8").build())).build(),
                DynamicField.field("tail", "u8").build()));
    }

    @Test
    void shouldRejectCountedPairSharingEnclosingBoundary() {
        final DynamicField raw = DynamicField.field("raw", "hex").length(DynamicLength.remaining("byte")).build();
        final DynamicField single = DynamicField.field("pairs", "pair").length(DynamicLength.fixed(1, "item"))
                .variants(Map.of("1", raw)).build();
        assertFailure("$.pairs", "末尾", DynamicSchema.of(single, DynamicField.field("tail", "u8").build()));
        assertFailure("$.pairs", "剩余", DynamicSchema.of(
                DynamicField.field("pairs", "pair").length(DynamicLength.fixed(2, "item"))
                        .variants(Map.of("1", raw)).build()));
        assertFailure("$.pairs", "剩余", DynamicSchema.of(
                DynamicField.field("count", "u8").build(),
                DynamicField.field("pairs", "pair").length(DynamicLength.ref("count", "item"))
                        .variants(Map.of("1", DynamicField.field("item", "struct").fields(raw).build())).build()));
    }

    @Test
    void shouldAllowPairWithIndependentBoundaryOrZeroItems() {
        final DynamicField raw = DynamicField.field("raw", "hex").length(DynamicLength.remaining("byte")).build();
        for (final String type : List.of("pair", "tlv", "map")) {
            final DynamicField variant = "pair".equals(type)
                    ? DynamicField.field("item", "struct").length(DynamicLength.fixed(1, "byte")).fields(raw).build()
                    : raw;
            final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                    DynamicField.field("items", type).length(DynamicLength.fixed(2, "item"))
                            .variants(Map.of("1", variant)).build(), DynamicField.field("tail", "u8").build()));
            final ByteBuf buffer = Unpooled.wrappedBuffer("pair".equals(type)
                    ? new byte[]{1, (byte) 0xaa, 1, (byte) 0xbb, (byte) 0xff}
                    : new byte[]{1, 1, (byte) 0xaa, 1, 1, (byte) 0xbb, (byte) 0xff});
            try {
                assertEquals(255L, plan.decode(buffer).values().fields().get("tail"));
                assertEquals(0, buffer.readableBytes());
            } finally {
                buffer.release();
            }
        }
        final DynamicPlan empty = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("pairs", "pair").length(DynamicLength.fixed(0, "item"))
                        .variants(Map.of("1", raw)).build(), DynamicField.field("tail", "u8").build()));
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{(byte) 0xff});
        try {
            assertEquals(Map.of("pairs", List.of(), "tail", 255L), empty.decode(buffer).values().fields());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldRoundTripSingleRemainingPairAtEndOrInsideByteBoundary() {
        final DynamicField raw = DynamicField.field("raw", "hex").length(DynamicLength.remaining("byte")).build();
        for (final boolean bounded : List.of(false, true)) {
            final DynamicField pair = DynamicField.field("pairs", "pair")
                    .length(bounded ? DynamicLength.fixed(2, "byte") : DynamicLength.fixed(1, "item"))
                    .variants(Map.of("1", raw)).build();
            final DynamicPlan plan = DynamicCompiler.compile(bounded
                    ? DynamicSchema.of(pair, DynamicField.field("tail", "u8").build()) : DynamicSchema.of(pair));
            final ByteBuf buffer = Unpooled.buffer();
            try {
                plan.encode(DynamicValues.of(Map.of("pairs", List.of(Map.of("key", 1, "value", "aa")), "tail", 255)), buffer);
                assertEquals(bounded ? "01aaff" : "01aa", io.netty.buffer.ByteBufUtil.hexDump(buffer));
                assertEquals(List.of(Map.of("key", 1L, "value", "aa")), plan.decode(buffer).values().fields().get("pairs"));
                assertEquals(0, buffer.readableBytes());
            } finally {
                buffer.release();
            }
        }
    }

    @Test
    void shouldAllowRemainingElementWhenArrayHasItsOwnByteBoundary() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("items", "array").length(DynamicLength.fixed(1, "byte"))
                        .item(DynamicField.field("raw", "hex")
                                .length(DynamicLength.remaining("byte")).build()).build(),
                DynamicField.field("tail", "u8").build()));
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{(byte) 0xaa, (byte) 0xff});
        try {
            assertEquals(Map.of("items", List.of("aa"), "tail", 255L), plan.decode(buffer).values().fields());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldAllowCountedArrayWhenEachItemHasItsOwnBoundary() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("items", "array").length(DynamicLength.fixed(2, "item"))
                        .item(DynamicField.field("item", "struct").length(DynamicLength.fixed(1, "byte")).fields(
                                DynamicField.field("raw", "hex")
                                        .length(DynamicLength.remaining("byte")).build()).build()).build(),
                DynamicField.field("tail", "u8").build()));
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{(byte) 0xaa, (byte) 0xbb, (byte) 0xff});
        try {
            assertEquals(Map.of("items", List.of(Map.of("raw", "aa"), Map.of("raw", "bb")), "tail", 255L),
                    plan.decode(buffer).values().fields());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldAllowRemainingChoiceBranchInsideBoundedStruct() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("body", "struct").length(DynamicLength.fixed(2, "byte")).fields(
                        DynamicField.field("kind", "u8").build(),
                        DynamicField.field("selected", "choice").ref("kind").variants(Map.of(
                                "1", DynamicField.field("raw", "hex")
                                        .length(DynamicLength.remaining("byte")).build())).build()).build(),
                DynamicField.field("tail", "u8").build()));
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{1, (byte) 0xaa, (byte) 0xff});
        try {
            assertEquals(Map.of("body", Map.of("kind", 1L, "selected", "aa"), "tail", 255L),
                    plan.decode(buffer).values().fields());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldAllowComputedLengthWithoutTreatingItsTargetAsAReadDependency() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("size", "u16").computedLengthOf("body").build(),
                DynamicField.field("body", "bytes").length(DynamicLength.ref("size", "byte")).build()));
        assertEquals(2, plan.fields().size());
    }

    @Test
    void shouldRejectUnsupportedVersionsAndInvalidTypes() {
        assertThrows(IllegalArgumentException.class, () -> new DynamicSchema(2, List.of()));
        assertThrows(IllegalArgumentException.class, () -> DynamicField.field("body", "java.lang.Runtime").build());
        assertThrows(IllegalArgumentException.class, () -> DynamicField.field("body", "bytes")
                .length(DynamicLength.fixed(1, "item")).build());
    }

    private static void assertFailure(String path, String reason, DynamicSchema schema) {
        final DynamicCodecException error = assertThrows(DynamicCodecException.class, () -> DynamicCompiler.compile(schema));
        assertEquals(path, error.fieldPath());
        assertEquals(-1, error.bitOffset());
        assertTrue(error.getMessage().contains(reason), error.getMessage());
    }

    @Test
    void shouldRejectAmbiguousAndInvalidIntegerNames() {
        // 类型名称是整数位宽的唯一来源，旧写法及重复 bits 都需要明确迁移。
        for (final String type : List.of("uint", "int", "u0", "i0", "u65", "i65", "u016", "u999", "u16_le")) {
            assertThrows(IllegalArgumentException.class,
                    () -> DynamicField.field("value", type).build(), type);
        }
        assertThrows(IllegalArgumentException.class, () -> DynamicField.field("value", "u16").bits(16).build());
    }
}
