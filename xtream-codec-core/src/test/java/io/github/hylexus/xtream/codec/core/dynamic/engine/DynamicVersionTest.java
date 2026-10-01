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

import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchemaJson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 外部协议版本的编译选择测试。版本不占用报文字节，计划确定后可重复并发执行。
 * 覆盖精确版本覆盖默认布局、递归选择以及版本筛选后的引用和长度校验。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicVersionTest {
    @Test
    void shouldPreferExactVersionAndPreserveLogicalFieldOrder() {
        final DynamicSchema schema = DynamicSchema.of(
                DynamicField.field("number", "u8").build(),
                DynamicField.field("tail", "u8").build(),
                DynamicField.field("number", "u16").versions(2019, 2023).build(),
                DynamicField.field("extra", "u8").versions(2023).build());
        final DynamicSchema restored = DynamicSchemaJson.read(DynamicSchemaJson.write(schema));
        assertEquals(schema, restored);
        assertEquals(XtreamField.ALL_VERSION, DynamicSchema.ALL_VERSION);
        assertWire(DynamicCompiler.compile(restored), Map.of("number", 17, "tail", 34), "1122");
        assertWire(DynamicCompiler.compile(restored, 2013), Map.of("number", 17, "tail", 34), "1122");
        assertWire(DynamicCompiler.compile(restored, 2019), Map.of("number", 17, "tail", 34), "001122");
        assertWire(DynamicCompiler.compile(restored, 2023), Map.of("number", 17, "tail", 34, "extra", 51), "00112233");
        // 明确声明 ALL_VERSION 与省略 versions 的默认定义等价。
        assertWire(DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("number", "u8").versions(DynamicSchema.ALL_VERSION).build()), 7), Map.of("number", 17), "11");
    }

    @Test
    void shouldPropagateVersionIntoArraysChoicesAndTlvValues() {
        final DynamicField legacy = DynamicField.field("number", "u8").build();
        final DynamicField modern = DynamicField.field("number", "u16").versions(2).build();
        final DynamicSchema schema = DynamicSchema.of(
                DynamicField.field("kind", "u8").build(),
                DynamicField.field("items", "array").length(DynamicLength.fixed(1, "item"))
                        .item(DynamicField.field("item", "struct").fields(legacy, modern).build()).build(),
                DynamicField.field("body", "choice").ref("kind").variants(Map.of(
                        "1", DynamicField.field("branch", "struct").fields(legacy, modern).build())).build(),
                DynamicField.field("extras", "tlv").length(DynamicLength.remaining("byte")).variants(Map.of(
                        "1", DynamicField.field("entry", "struct").fields(legacy, modern).build())).build());
        final Map<String, Object> values = Map.of("kind", 1, "items", List.of(Map.of("number", 17)),
                "body", Map.of("number", 34), "extras", List.of(Map.of("key", 1, "value", Map.of("number", 51))));
        assertWire(DynamicCompiler.compile(schema, 1), values, "011122010133");
        assertWire(DynamicCompiler.compile(schema, 2), values, "010011002201020033");
    }

    @Test
    void shouldReuseVersionPlansConcurrentlyWithoutChangingTheSourceDefinition() {
        final DynamicSchema schema = DynamicSchema.of(
                DynamicField.field("number", "u8").build(),
                DynamicField.field("number", "u16").versions(2).build());
        final DynamicPlan legacy = DynamicCompiler.compile(schema, 1);
        final DynamicPlan modern = DynamicCompiler.compile(schema, 2);
        assertSame(schema, modern.schema());
        assertEquals(2, modern.version());
        assertEquals("u16", modern.schema().fields().get(1).type());
        assertNull(modern.schema().fields().get(1).bits());
        IntStream.range(0, 64).parallel().forEach(index -> {
            assertWire(legacy, Map.of("number", 17), "11");
            assertWire(modern, Map.of("number", 17), "0011");
        });
    }

    @Test
    void shouldRejectOverlappingVersionsAndReferencesToExcludedFields() {
        final DynamicSchema overlap = DynamicSchema.of(
                DynamicField.field("number", "u8").versions(1, 2).build(),
                DynamicField.field("number", "u16").versions(2).build());
        assertEquals("$.number", assertThrows(DynamicCodecException.class,
                () -> DynamicCompiler.compile(overlap, 1)).fieldPath());
        assertThrows(IllegalArgumentException.class, () -> DynamicField.field("number", "u8").versions().build());
        final DynamicSchema missing = DynamicSchema.of(
                DynamicField.field("size", "u8").versions(2).build(),
                DynamicField.field("body", "bytes").length(DynamicLength.ref("size", "byte")).build());
        assertEquals("$.body", assertThrows(DynamicCodecException.class,
                () -> DynamicCompiler.compile(missing, 1)).fieldPath());
        assertDoesNotThrow(() -> DynamicCompiler.compile(missing, 2));
    }

    @Test
    void shouldResolveComputedLengthAfterSelectingTheVersion() {
        final DynamicSchema schema = DynamicSchema.of(
                DynamicField.field("size", "u8").computedLengthOf("body").build(),
                DynamicField.field("body", "struct").length(DynamicLength.ref("size", "byte")).fields(
                        DynamicField.field("number", "u8").build(),
                        DynamicField.field("number", "u16").versions(2).build()).build());
        assertWire(DynamicCompiler.compile(schema, 1), Map.of("body", Map.of("number", 17)), "0111");
        assertWire(DynamicCompiler.compile(schema, 2), Map.of("body", Map.of("number", 17)), "020011");
    }

    private static void assertWire(DynamicPlan plan, Map<String, Object> values, String expectedHex) {
        // 固定的 HEX 断言同时约束版本布局、字段顺序和数值；再次编码检查解码值的可复用性。
        final ByteBuf buffer = Unpooled.buffer();
        final ByteBuf reencoded = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(values), buffer);
            assertEquals(expectedHex, ByteBufUtil.hexDump(buffer));
            final DynamicDecodeResult decoded = plan.decode(buffer);
            assertEquals(0, decoded.remainingBytes());
            plan.encode(decoded.values(), reencoded);
            assertEquals(expectedHex, ByteBufUtil.hexDump(reencoded));
        } finally {
            buffer.release();
            reencoded.release();
        }
    }
}
