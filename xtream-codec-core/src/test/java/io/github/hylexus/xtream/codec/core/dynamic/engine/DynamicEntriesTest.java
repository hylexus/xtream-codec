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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 有序键值条目、未知标签和长度截断测试。
 * 通过重复键和未知标签验证原始条目顺序及 HEX 回退值能被重新编码。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicEntriesTest {
    @ParameterizedTest
    @ValueSource(strings = {"tlv", "map", "pair"})
    void shouldIsolateConditionalValuesInRepeatedEntryVariants(String type) {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("items", type).length(DynamicLength.fixed(2, "item"))
                        .variants(Map.of("1", DynamicField.field("item", "struct").fields(
                                DynamicField.field("flag", "u8").build(),
                                DynamicField.field("a", "u8")
                                        .when(new DynamicCondition("flag", "eq", "1")).build(),
                                DynamicField.field("b", "u8")
                                        .when(new DynamicCondition("a", "ne", "0")).build()).build())).build(),
                DynamicField.field("tail", "u16").build()));
        final List<Map<String, Object>> entries = List.of(
                Map.of("key", 1, "value", Map.of("flag", 1, "a", 7, "b", 9)),
                Map.of("key", 1, "value", Map.of("flag", 0)));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("items", entries, "tail", 0x0bee)), buffer);
            assertEquals("pair".equals(type) ? "0101070901000bee" : "01030107090101000bee",
                    ByteBufUtil.hexDump(buffer));
            final Map<String, Object> decoded = plan.decode(buffer).values().fields();
            assertEquals(List.of(
                    Map.of("key", 1L, "value", Map.of("flag", 1L, "a", 7L, "b", 9L)),
                    Map.of("key", 1L, "value", Map.of("flag", 0L))), decoded.get("items"));
            assertEquals(0x0beeL, decoded.get("tail"));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldPreserveUnknownAndDuplicateTlvEntries() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("items", "tlv").length(DynamicLength.remaining("byte"))
                        .options(Map.of("keyBits", 8, "lengthBits", 8))
                        .variants(Map.of("1", DynamicField.field("text", "string")
                                .length(DynamicLength.remaining("byte")).build())).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            final List<Map<String, Object>> entries = List.of(
                    Map.of("key", 1, "value", "A"),
                    Map.of("key", 99, "rawHex", "aabb"),
                    Map.of("key", 1, "value", "B"));
            plan.encode(DynamicValues.of(Map.of("items", entries)), buffer);
            assertEquals("0101416302aabb010142", ByteBufUtil.hexDump(buffer));
            final List<?> decoded = (List<?>) plan.decode(buffer).values().fields().get("items");
            assertEquals(3, decoded.size());
            assertEquals(Map.of("key", 99L, "rawHex", "aabb"), decoded.get(1));
            assertEquals(Map.of("key", 1L, "value", "A"), decoded.get(0));
            assertEquals(Map.of("key", 1L, "value", "B"), decoded.get(2));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldEncodePairAndMapWithOrderedEntries() {
        final DynamicPlan pair = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("pairs", "pair").length(DynamicLength.fixed(2, "item"))
                        .variants(Map.of("1", DynamicField.field("number", "u8").build())).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            pair.encode(DynamicValues.of(Map.of("pairs", List.of(
                    Map.of("key", 1, "value", 3), Map.of("key", 1, "value", 4)))), buffer);
            assertEquals("01030104", ByteBufUtil.hexDump(buffer));
            assertEquals(2, ((List<?>) pair.decode(buffer).values().fields().get("pairs")).size());
        } finally {
            buffer.release();
        }

        final DynamicPlan map = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("entries", "map").length(DynamicLength.fixed(2, "item"))
                        .variants(Map.of("7", DynamicField.field("value", "u8").build())).build()));
        final ByteBuf mapBuffer = Unpooled.buffer();
        try {
            map.encode(DynamicValues.of(Map.of("entries", List.of(
                    Map.of("key", 7, "value", 1), Map.of("key", 7, "value", 2)))), mapBuffer);
            assertEquals("070101070102", ByteBufUtil.hexDump(mapBuffer));
            assertEquals(List.of(Map.of("key", 7L, "value", 1L), Map.of("key", 7L, "value", 2L)),
                    map.decode(mapBuffer).values().fields().get("entries"));
        } finally {
            mapBuffer.release();
        }
    }

    @Test
    void shouldRejectTruncatedTlvValue() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("items", "tlv").length(DynamicLength.remaining("byte"))
                        .variants(Map.of()).build()));
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{1, 3, 9});
        try {
            final DynamicCodecException error = assertThrows(DynamicCodecException.class, () -> plan.decode(buffer));
            assertEquals("$.items", error.fieldPath());
        } finally {
            buffer.release();
        }
    }
}
