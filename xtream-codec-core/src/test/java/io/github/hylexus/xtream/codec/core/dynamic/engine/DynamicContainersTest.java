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
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNodeKind;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 嵌套结构、数组及 Trace 树测试。
 * 除值往返外，还检查元素层级，防止嵌套容器的父节点或边界串扰。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicContainersTest {
    @ParameterizedTest
    @CsvSource({"eq,7", "ne,0", "gt,0", "gte,7", "lt,8", "lte,7", "bitSet,1", "bitClear,8"})
    void shouldNotReuseConditionalValuesAcrossArrayItems(String operator, String expected) {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("items", "array").length(DynamicLength.fixed(2, "item"))
                        .item(DynamicField.field("item", "struct").fields(
                                DynamicField.field("flag", "u8").build(),
                                DynamicField.field("a", "u8")
                                        .when(new DynamicCondition("flag", "eq", "1")).build(),
                                DynamicField.field("b", "u8")
                                        .when(new DynamicCondition("a", operator, expected)).build()).build()).build(),
                DynamicField.field("tail", "u16").build()));
        final Map<String, Object> values = Map.of(
                "items", List.of(Map.of("flag", 1, "a", 7, "b", 9), Map.of("flag", 0)),
                "tail", 0x0bee);
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{1, 7, 9, 0, 0x0b, (byte) 0xee});
        try {
            final CodecTracker tracker = new CodecTracker();
            final Map<String, Object> decoded = plan.decode(buffer, tracker).values().fields();
            assertEquals(List.of(Map.of("flag", 1L, "a", 7L, "b", 9L), Map.of("flag", 0L)),
                    decoded.get("items"));
            assertEquals(0x0beeL, decoded.get("tail"));
            assertEquals(0, buffer.readableBytes());
            // 第一项包含三个字段，第二项只包含 flag；被跳过字段不会复用上一项的 Trace。
            final var items = tracker.toTraceView().root().children().getFirst().children();
            assertEquals(3, items.getFirst().children().size());
            assertEquals(1, items.get(1).children().size());
        } finally {
            buffer.release();
        }
        final ByteBuf encoded = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(values), encoded);
            assertEquals("010709000bee", ByteBufUtil.hexDump(encoded));
        } finally {
            encoded.release();
        }
    }

    @Test
    void shouldRoundTripNestedItemsAndTraceHierarchy() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("header", "struct").fields(
                        DynamicField.field("count", "u8").build()).build(),
                DynamicField.field("items", "array").length(DynamicLength.ref("header.count", "item"))
                        .item(DynamicField.field("item", "struct").fields(
                                DynamicField.field("code", "u8").build(),
                                DynamicField.field("value", "u16").build()).build()).build()));
        final Map<String, Object> values = Map.of("header", Map.of("count", 2),
                "items", List.of(Map.of("code", 1, "value", 0x1234), Map.of("code", 2, "value", 0x5678)));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            final CodecTracker tracker = new CodecTracker();
            plan.encode(DynamicValues.of(values), buffer, tracker);
            final var root = tracker.toTraceView().root();
            assertEquals(2, root.children().size());
            assertEquals("count", root.children().get(0).children().getFirst().name());
            assertEquals(2, root.children().get(1).children().size());
            assertEquals(CodecTraceNodeKind.COLLECTION_ITEM, root.children().get(1).children().getFirst().kind());
            assertEquals("code", root.children().get(1).children().getFirst().children().getFirst().name());
            final var decoded = plan.decode(buffer).values().fields();
            assertEquals(List.of(Map.of("code", 1L, "value", 0x1234L),
                    Map.of("code", 2L, "value", 0x5678L)), decoded.get("items"));
        } finally {
            buffer.release();
        }
    }
}
