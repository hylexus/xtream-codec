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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicBitOrder;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicCondition;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicConditionOperator;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicEndian;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicExpression;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicExpressionOperator;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicFieldType;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLengthUnit;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicPaddingSide;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchemaJson;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JSON 交换合同测试：字段顺序、显示名称与稳定标识应在往返后保留，
 * 值映射与结构独立，并拒绝未知 JSON 属性以暴露定义拼写错误。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicSchemaJsonTest {
    @Test
    void shouldPublishSchemaForEveryPublicFieldProperty() throws Exception {
        final var schema = new ObjectMapper().readTree(DynamicSchemaJson.jsonSchema());
        assertEquals("https://json-schema.org/draft/2020-12/schema", schema.path("$schema").asText());
        final var properties = schema.path("$defs").path("field").path("properties");
        assertEquals(DynamicField.class.getRecordComponents().length, properties.size());
        for (final var component : DynamicField.class.getRecordComponents()) {
            assertTrue(properties.has(component.getName()), component.getName());
        }
    }

    @Test
    void shouldRoundTripOrderedFieldsAndKeepIdsIndependentOfDisplayNames() {
        final DynamicSchema schema = DynamicSchema.of(
                DynamicField.field("first", "u3").name("标志").build(),
                DynamicField.field("second", "u5").name("标志").build(),
                DynamicField.field("body", "struct").fields(
                        DynamicField.field("first", "bytes").length(DynamicLength.ref("second", "byte")).build()
                ).build()
        );

        final String json = DynamicSchemaJson.write(schema);
        final DynamicSchema restored = DynamicSchemaJson.read(json);

        assertEquals(schema, restored);
        assertEquals(List.of("first", "second", "body"), restored.fields().stream().map(DynamicField::id).toList());
        assertEquals("标志", restored.fields().get(0).name());
        assertEquals(restored.fields().get(0).name(), restored.fields().get(1).name());
        assertEquals("first", restored.fields().get(2).fields().getFirst().id());
        assertThrows(UnsupportedOperationException.class, () -> restored.fields().clear());
    }

    @Test
    void shouldKeepValueOrderSeparateFromSchema() throws Exception {
        final Map<String, Object> input = new LinkedHashMap<>();
        input.put("first", 1);
        input.put("second", 2);
        final DynamicValues values = DynamicValues.of(input);
        input.put("first", 3);

        assertEquals(List.of("first", "second"), List.copyOf(values.fields().keySet()));
        assertEquals(1, values.fields().get("first"));
        assertThrows(UnsupportedOperationException.class, () -> values.fields().put("third", 4));

        final ObjectMapper mapper = new ObjectMapper();
        final DynamicValues restored = mapper.readValue(mapper.writeValueAsString(values), DynamicValues.class);
        assertEquals(values, restored);
    }

    @Test
    void shouldRejectUnknownJsonProperties() {
        assertThrows(IllegalArgumentException.class, () -> DynamicSchemaJson.read("""
                {"schemaVersion":1,"fields":[],"unknown":true}
                """));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"1field", "a.b", "a-b", "a b", "字段", "field\n"})
    void shouldRejectInvalidIdsBeforeCompilation(String id) throws Exception {
        // Builder 和 JSON 反序列化均调用规范构造器，不需要等到选择版本或编译计划才失败。
        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> DynamicField.field(id, "u8").build());
        assertTrue(error.getMessage().contains("字段 id"));
        final String json = "{\"schemaVersion\":1,\"fields\":[{\"id\":"
                + new ObjectMapper().writeValueAsString(id) + ",\"type\":\"u8\"}]}";
        assertThrows(IllegalArgumentException.class, () -> DynamicSchemaJson.read(json));
    }

    @ParameterizedTest
    @ValueSource(strings = {"_", "_field1", "messageId", "A0_z"})
    void shouldPreserveValidIdsAcrossConstructionAndJson(String id) {
        final DynamicSchema schema = DynamicSchema.of(DynamicField.field(id, "u8").build());
        assertEquals(id, schema.fields().getFirst().id());
        assertEquals(schema, DynamicSchemaJson.read(DynamicSchemaJson.write(schema)));
        assertEquals(id, DynamicField.field("size", "u8").computedLengthOf(id).build().computedLengthOf());
        assertEquals(id + ".." + id, DynamicField.field("size", "u8")
                .computedLengthOf(id + ".." + id).build().computedLengthOf());
        assertEquals(Map.of(id, "count"), DynamicExpression.engine("SpEL", "1", Map.of(id, "count")).bindings());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1field", "a.b", "a-b", "a b", "字段", "field\n"})
    void shouldRejectInvalidIdentifiersAcrossBindingsAndLengthRanges(String id) {
        assertThrows(IllegalArgumentException.class,
                () -> DynamicExpression.engine("SpEL", "1", Map.of(id, "count")));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicField.field("size", "u8").computedLengthOf("first.." + id).build());
        assertThrows(IllegalArgumentException.class,
                () -> DynamicField.field("size", "u8").computedLengthOf(id + "..last").build());
    }

    @Test
    void shouldRejectMalformedComputedLengthRange() {
        for (final String range : List.of("first..", "..last", "first...last", "first..last..more")) {
            assertThrows(IllegalArgumentException.class,
                    () -> DynamicField.field("size", "u8").computedLengthOf(range).build());
        }
    }

    @Test
    void shouldKeepEnumValuesAsDslStrings() {
        final DynamicSchema schema = DynamicSchema.of(
                DynamicField.field("flags", "u16").endian(DynamicEndian.LITTLE).bitOrder(DynamicBitOrder.LSB0).build(),
                DynamicField.field("data", DynamicFieldType.BYTES).length(DynamicLength.fixed(2, DynamicLengthUnit.BYTE))
                        .options(Map.of("padSide", DynamicPaddingSide.LEFT, "lengthUnit", DynamicLengthUnit.BIT))
                        .when(new DynamicCondition("flags", DynamicConditionOperator.BIT_SET, "1")).build(),
                DynamicField.field("sum", "derived").expression(DynamicExpression.op(DynamicExpressionOperator.ADD,
                        DynamicExpression.ref("flags"), DynamicExpression.value(1))).build());
        final String json = DynamicSchemaJson.write(schema);
        assertTrue(json.contains("\"endian\":\"little\""));
        assertTrue(json.contains("\"bitOrder\":\"lsb0\""));
        assertTrue(json.contains("\"unit\":\"byte\""));
        assertTrue(json.contains("\"op\":\"bitSet\""));
        assertTrue(json.contains("\"op\":\"add\""));
        assertTrue(json.contains("\"padSide\":\"left\""));
        assertTrue(json.contains("\"lengthUnit\":\"bit\""));
        assertEquals(schema, DynamicSchemaJson.read(json));
    }

    @Test
    void shouldRejectInvalidEnumValuesFromJson() {
        assertThrows(IllegalArgumentException.class, () -> DynamicSchemaJson.read("""
                {"schemaVersion":1,"fields":[{"id":"number","type":"u8","endian":"middle"}]}
                """));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"id\":\"x\",\"type\":\"u3\",\"endian\":\"little\"}",
            "{\"id\":\"x\",\"type\":\"float\",\"bits\":16}",
            "{\"id\":\"x\",\"type\":\"align\",\"alignTo\":3}",
            "{\"id\":\"x\",\"type\":\"u8\",\"fillBit\":2}",
            "{\"id\":\"x\",\"type\":\"array\"}",
            "{\"id\":\"x\",\"type\":\"u8\",\"charset\":\"UTF-8\"}",
            "{\"id\":\"x\",\"type\":\"bytes\",\"length\":{\"fixed\":-1,\"unit\":\"byte\"}}",
            "{\"id\":\"x\",\"type\":\"bytes\",\"length\":{\"fixed\":1,\"ref\":\"n\",\"unit\":\"byte\"}}",
            "{\"id\":\"x\",\"type\":\"bytes\",\"length\":{\"prefixBits\":7,\"unit\":\"byte\"}}",
            "{\"id\":\"x\",\"type\":\"bytes\",\"length\":{\"fixed\":3,\"unit\":\"bit\"}}",
            "{\"id\":\"x\",\"type\":\"u8\",\"options\":{\"keyBits\":8.5}}",
            "{\"id\":\"x\",\"type\":\"u8\",\"options\":{\"padByte\":256}}",
            "{\"id\":\"x\",\"type\":\"u8\",\"versions\":[1,1]}",
            "{\"id\":\"x\",\"type\":\"u8\",\"when\":{\"ref\":\"n\",\"op\":\"gt\",\"value\":\"abc\"}}",
            "{\"id\":\"x\",\"type\":\"derived\",\"expression\":{\"op\":\"add\",\"args\":[{\"value\":1}]}}"
    })
    void shouldRejectLocalDefinitionErrorsDuringJsonImport(String fieldJson) {
        // JSON 导入也必须调用模型构造校验，无需版本选择或编译即可拒绝这些局部错误。
        assertThrows(IllegalArgumentException.class,
                () -> DynamicSchemaJson.read("{\"schemaVersion\":1,\"fields\":[" + fieldJson + "]}"));
    }
}
