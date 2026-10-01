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

import io.github.hylexus.xtream.codec.base.expression.*;
import io.github.hylexus.xtream.codec.core.dynamic.schema.*;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtensionRegistry;
import io.netty.buffer.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 自动生成线上值与默认分支的兼容性、作用域和数值范围测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicGeneratedTest {
    @Test
    void shouldGenerateConstantsCountsAndWireExpressions() {
        final DynamicSchema schema = DynamicSchema.of(
                DynamicField.field("start", "u8").generated(DynamicGeneratedValue.constant(0x7e)).build(),
                DynamicField.field("count", "u8").generated(DynamicGeneratedValue.countOf("items")).build(),
                DynamicField.field("twice", "u8").generated(DynamicGeneratedValue.expression(
                        DynamicExpression.op("*", DynamicExpression.ref("count"), DynamicExpression.value(2)))).build(),
                DynamicField.field("items", "array").length(DynamicLength.ref("count", "item"))
                        .item(DynamicField.field("item", "u8").build()).build(),
                DynamicField.field("end", "hex").length(DynamicLength.fixed(1, "byte"))
                        .generated(DynamicGeneratedValue.constant("7e")).build());
        assertEquals(schema, DynamicSchemaJson.read(DynamicSchemaJson.write(schema)));
        final DynamicPlan plan = DynamicCompiler.compile(schema);
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("items", List.of(10, 11), "start", 0, "count", 99)), buffer);
            assertEquals("7e02040a0b7e", ByteBufUtil.hexDump(buffer));
            buffer.setByte(0, 0x55);
            assertEquals(Map.of("start", 85L, "count", 2L, "twice", 4L, "items", List.of(10L, 11L), "end", "7e"),
                    plan.decode(buffer).values().fields());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldUseExistingExpressionEnginesAndRejectForwardReferences() {
        final var engine = new SpelXtreamExpressionEngine();
        final var registry = new XtreamExpressionEngineRegistry(false);
        registry.register(engine);
        final var schema = DynamicSchema.of(DynamicField.field("n", "u8").build(),
                DynamicField.field("v", "u8").generated(DynamicGeneratedValue.expression(
                        DynamicExpression.engine(engine.id().value(), "#n + 1", Map.of("n", "n")))).build());
        assertThrows(DynamicCodecException.class, () -> DynamicCompiler.compile(schema));
        final var plan = DynamicCompiler.compile(schema, new DynamicCompileOptions(
                DynamicSchema.ALL_VERSION, DynamicExtensionRegistry.EMPTY, registry));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("n", 7)), buffer);
            assertEquals("0708", ByteBufUtil.hexDump(buffer));
        } finally {
            buffer.release();
        }
        assertThrows(DynamicCodecException.class, () -> DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("v", "u8").generated(DynamicGeneratedValue.expression(DynamicExpression.ref("n"))).build(),
                DynamicField.field("n", "u8").build())));
    }

    @Test
    void shouldRejectConflictingSourcesInvalidTargetsAndOverflow() {
        assertThrows(IllegalArgumentException.class, () -> new DynamicGeneratedValue(1, "items", null, null));
        assertThrows(IllegalArgumentException.class, () -> DynamicField.field("n", "u8")
                .computedLengthOf("body").generated(DynamicGeneratedValue.constant(1)).build());
        assertThrows(DynamicCodecException.class, () -> DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("n", "u8").generated(DynamicGeneratedValue.countOf("missing")).build())));
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("n", "u8").generated(DynamicGeneratedValue.constant(256)).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            assertThrows(DynamicCodecException.class, () -> plan.encode(DynamicValues.of(Map.of()), buffer));
            assertEquals(0, buffer.writerIndex());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldChooseDefaultAndPreserveBoundaryRules() {
        final DynamicField fallback = DynamicField.field("unknown", "hex").length(DynamicLength.fixed(1, "byte")).build();
        final DynamicField choice = DynamicField.field("body", "choice").ref("kind")
                .variants(Map.of("1", DynamicField.field("known", "u8").build())).defaultVariant(fallback).build();
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("kind", "u8").build(), choice, DynamicField.field("tail", "u8").build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("kind", 99, "body", "ab", "tail", 255)), buffer);
            assertEquals("63abff", ByteBufUtil.hexDump(buffer));
            assertEquals(Map.of("kind", 99L, "body", "ab", "tail", 255L), plan.decode(buffer).values().fields());
        } finally {
            buffer.release();
        }
        assertEquals(choice, DynamicSchemaJson.read(DynamicSchemaJson.write(DynamicSchema.of(choice))).fields().getFirst());
        assertThrows(DynamicCodecException.class, () -> DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("kind", "u8").build(),
                DynamicField.field("body", "choice").ref("kind").variants(Map.of())
                        .defaultVariant(DynamicField.field("raw", "hex").length(DynamicLength.remaining("byte")).build()).build(),
                DynamicField.field("tail", "u8").build())));
    }

    @Test
    void shouldKeepOriginalConstructorAndFreezeConstantLists() {
        final DynamicField old = new DynamicField("n", null, "u8", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null);
        assertEquals(DynamicField.field("n", "u8").build(), old);
        final List<Integer> source = new ArrayList<>(List.of(1, 2));
        final var generated = DynamicGeneratedValue.constant(source);
        source.clear();
        assertEquals(List.of(1, 2), generated.constant());
    }

    @Test
    void shouldIsolateGeneratedCountsWithinRepeatedStructures() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("groups", "array").length(DynamicLength.fixed(2, "item"))
                        .item(DynamicField.field("group", "struct").fields(
                                DynamicField.field("count", "u8").generated(DynamicGeneratedValue.countOf("items")).build(),
                                DynamicField.field("items", "array").length(DynamicLength.ref("count", "item"))
                                        .item(DynamicField.field("item", "u8").build()).build()).build()).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("groups", List.of(
                    Map.of("items", List.of(10)), Map.of("items", List.of(11, 12))))), buffer);
            assertEquals("010a020b0c", ByteBufUtil.hexDump(buffer));
            assertEquals(List.of(Map.of("count", 1L, "items", List.of(10L)),
                    Map.of("count", 2L, "items", List.of(11L, 12L))), plan.decode(buffer).values().fields().get("groups"));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldApplyDefaultBranchVersionAndOwnCondition() {
        final DynamicField choice = DynamicField.field("body", "choice").ref("kind").variants(Map.of())
                .defaultVariant(DynamicField.field("fallback", "u8").versions(2)
                        .when(new DynamicCondition("kind", "eq", "99")).build()).build();
        final DynamicSchema schema = DynamicSchema.of(DynamicField.field("kind", "u8").build(), choice);
        final DynamicPlan version2 = DynamicCompiler.compile(schema, 2);
        final ByteBuf buffer = Unpooled.buffer();
        try {
            version2.encode(DynamicValues.of(Map.of("kind", 99, "body", 7)), buffer);
            assertEquals(Map.of("kind", 99L, "body", 7L), version2.decode(buffer).values().fields());
            buffer.clear();
            version2.encode(DynamicValues.of(Map.of("kind", 98)), buffer);
            assertEquals(Map.of("kind", 98L), version2.decode(buffer).values().fields());
            buffer.clear().writeByte(99).writeByte(7);
            assertThrows(DynamicCodecException.class, () -> DynamicCompiler.compile(schema, 1).decode(buffer));
            assertEquals(0, buffer.readerIndex());
        } finally {
            buffer.release();
        }
    }
}
