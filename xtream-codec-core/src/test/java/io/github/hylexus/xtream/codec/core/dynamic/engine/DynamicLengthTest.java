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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 长度规则和容器边界的双向测试。
 * 覆盖固定、引用、前缀与剩余长度，以及 item 计数、越界和零进度元素。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicLengthTest {
    @Test
    void shouldUseFixedPrefixReferenceAndRemainingBoundaries() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("length", "u8").build(),
                DynamicField.field("reference", "hex").length(DynamicLength.ref("length", "byte")).build(),
                DynamicField.field("prefix", "string").length(DynamicLength.prefix(8, "byte")).build(),
                DynamicField.field("last", "hex").length(DynamicLength.remaining("byte")).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("length", 2, "reference", "abcd", "prefix", "hi", "last", "55")), buffer);
            assertEquals("02abcd02686955", ByteBufUtil.hexDump(buffer));
            final var result = plan.decode(buffer);
            assertEquals("abcd", result.values().fields().get("reference"));
            assertEquals("hi", result.values().fields().get("prefix"));
            assertEquals("55", result.values().fields().get("last"));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldLimitNestedContainerAndCountedArray() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("size", "u8").build(),
                DynamicField.field("body", "struct").length(DynamicLength.ref("size", "byte")).fields(
                        DynamicField.field("count", "u8").build(),
                        DynamicField.field("items", "array").length(DynamicLength.ref("count", "item"))
                                .item(DynamicField.field("item", "u8").build()).build()).build(),
                DynamicField.field("tail", "u8").build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("size", 3, "body", Map.of("count", 2, "items", List.of(4, 5)), "tail", 9)), buffer);
            assertEquals("0302040509", ByteBufUtil.hexDump(buffer));
            final var decoded = plan.decode(buffer);
            assertEquals(List.of(4L, 5L), ((Map<?, ?>) decoded.values().fields().get("body")).get("items"));
            assertEquals(9L, decoded.values().fields().get("tail"));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldRejectOverrunAndEmptyProgress() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("count", "u8").build(),
                DynamicField.field("items", "array").length(DynamicLength.ref("count", "item"))
                        .item(DynamicField.field("item", "align").alignTo(8).build()).build()));
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{1});
        try {
            final DynamicCodecException error = assertThrows(DynamicCodecException.class, () -> plan.decode(buffer));
            assertEquals("$.items", error.fieldPath());
            assertTrue(error.getMessage().contains("游标"));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldHandleBitLengthAndContainerPrefixes() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("body", "struct").length(DynamicLength.prefix(8, "byte")).fields(
                        DynamicField.field("flag", "u3").build(),
                        DynamicField.field("payload", "bits").bits(5).build()).build(),
                DynamicField.field("items", "array").length(DynamicLength.prefix(8, "item"))
                        .item(DynamicField.field("item", "u8").build()).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("body", Map.of("flag", 5, "payload", "10001"),
                    "items", List.of(1, 2))), buffer);
            assertEquals("01b1020102", ByteBufUtil.hexDump(buffer));
            final var result = plan.decode(buffer);
            assertEquals("10001", ((Map<?, ?>) result.values().fields().get("body")).get("payload"));
            assertEquals(List.of(1L, 2L), result.values().fields().get("items"));
        } finally {
            buffer.release();
        }
    }
}
