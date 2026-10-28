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
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicChecksum;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicChecksumAlgorithm;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicExpression;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicGeneratedValue;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchemaJson;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Java 构造和 JSON 导入应采用相同的动态字段局部校验规则。
 *
 * @author Codex (AI)
 */
class DynamicFieldValidationBoundaryTest {

    @ParameterizedTest
    @MethodSource("invalidOptions")
    void rejectsOptionTypesAndOutOfRangeValuesBeforeCompilation(String key, Object value, String message) throws Exception {
        final Map<String, Object> options = Map.of(key, value);
        assertRejected(DynamicField.field("value", "u8").options(options),
                Map.of("type", "u8", "options", options), message);
    }

    static Stream<Arguments> invalidOptions() {
        return Stream.of(
                Arguments.of("keyBits", "8", "keyBits 必须为 8、16 或 32"),
                Arguments.of("keyBits", 0, "keyBits 必须为 8、16 或 32"),
                Arguments.of("keyBits", 64, "keyBits 必须为 8、16 或 32"),
                Arguments.of("keyBits", 8.5, "keyBits 必须为 8、16 或 32"),
                Arguments.of("lengthBits", true, "lengthBits 必须为 8、16 或 32"),
                Arguments.of("lengthBits", 0, "lengthBits 必须为 8、16 或 32"),
                Arguments.of("lengthBits", 64, "lengthBits 必须为 8、16 或 32"),
                Arguments.of("lengthBits", 16.5, "lengthBits 必须为 8、16 或 32"),
                Arguments.of("padByte", -1, "padByte 必须为 0..255 的整数"),
                Arguments.of("padByte", 256, "padByte 必须为 0..255 的整数"),
                Arguments.of("padByte", 0.5, "padByte 必须为 0..255 的整数"),
                Arguments.of("padByte", "0", "padByte 必须为 0..255 的整数"),
                Arguments.of("padSide", "middle", "padSide 必须为 left 或 right"),
                Arguments.of("padSide", 0, "padSide 必须为 left 或 right"),
                Arguments.of("stripPadding", "true", "stripPadding 必须为布尔值"),
                Arguments.of("stripPadding", 0, "stripPadding 必须为布尔值"),
                Arguments.of("lengthUnit", "item", "回填长度单位必须为 bit 或 byte"),
                Arguments.of("lengthUnit", "BYTE", "回填长度单位必须为 bit 或 byte"),
                Arguments.of("lengthUnit", 0, "回填长度单位必须为 bit 或 byte"),
                Arguments.of("bitOffset", -1, "bitOffset 必须为有效的非负整数"),
                Arguments.of("bitOffset", 0.5, "bitOffset 必须为有效的非负整数"),
                Arguments.of("bitOffset", "0", "bitOffset 必须为有效的非负整数"),
                Arguments.of("bitWidth", 0, "bitWidth 必须为有效的非负整数"),
                Arguments.of("bitWidth", -1, "bitWidth 必须为有效的非负整数"),
                Arguments.of("bitWidth", 1.5, "bitWidth 必须为有效的非负整数"),
                Arguments.of("bitWidth", "1", "bitWidth 必须为有效的非负整数"));
    }

    @ParameterizedTest
    @MethodSource("validOptions")
    void acceptsOptionEndpointsAndPreservesThemThroughJson(String key, Object value) {
        final DynamicField field = DynamicField.field("value", "u8").options(Map.of(key, value)).build();
        assertEquals(Map.of(key, value), field.options());
        assertRoundTrip(field);
    }

    static Stream<Arguments> validOptions() {
        return Stream.of(
                Arguments.of("keyBits", 8), Arguments.of("keyBits", 16), Arguments.of("keyBits", 32),
                Arguments.of("lengthBits", 8), Arguments.of("lengthBits", 16), Arguments.of("lengthBits", 32),
                Arguments.of("keyBits", 8.0), Arguments.of("padByte", 0), Arguments.of("padByte", 255),
                Arguments.of("padSide", "left"), Arguments.of("padSide", "right"),
                Arguments.of("stripPadding", true), Arguments.of("stripPadding", false),
                Arguments.of("lengthUnit", "bit"), Arguments.of("lengthUnit", "byte"),
                Arguments.of("bitOffset", 0), Arguments.of("bitWidth", 1));
    }

    @ParameterizedTest
    @MethodSource("invalidDefinitions")
    void rejectsInvalidCharsetDerivedPropertiesAndGeneratedCombinations(
            DynamicField.Builder builder, Map<String, Object> properties, String message) throws Exception {
        assertRejected(builder, properties, message);
    }

    static Stream<Arguments> invalidDefinitions() {
        final Map<String, Object> nullOffset = new LinkedHashMap<>();
        nullOffset.put("bitOffset", null);
        nullOffset.put("bitWidth", 1);
        final Map<String, Object> nullWidth = new LinkedHashMap<>();
        nullWidth.put("bitOffset", 0);
        nullWidth.put("bitWidth", null);
        final Map<String, Object> length = Map.of("fixed", 1, "unit", "byte");
        final Map<String, Object> expression = Map.of("value", 1);
        final DynamicGeneratedValue checksum = DynamicGeneratedValue.checksum(
                new DynamicChecksum(DynamicChecksumAlgorithm.CRC32, "body"));
        final Map<String, Object> checksumJson = Map.of("checksum", Map.of("algorithm", "crc32", "range", "body"));
        return Stream.of(
                Arguments.of(DynamicField.field("value", "string").length(DynamicLength.fixed(1, "byte")).charset("xtream-invalid-charset"),
                        Map.of("type", "string", "length", length, "charset", "xtream-invalid-charset"), "不支持的字符集"),
                Arguments.of(DynamicField.field("value", "string").length(DynamicLength.fixed(1, "byte")).charset("UTF 8"),
                        Map.of("type", "string", "length", length, "charset", "UTF 8"), "不支持的字符集"),
                Arguments.of(DynamicField.field("value", "derived"),
                        Map.of("type", "derived"), "派生字段必须声明 ref 或 expression"),
                Arguments.of(DynamicField.field("value", "derived").ref("source"),
                        Map.of("type", "derived", "ref", "source"), "派生位段必须声明 bitOffset 和 bitWidth"),
                Arguments.of(DynamicField.field("value", "derived").ref("source").options(Map.of("bitOffset", 0)),
                        Map.of("type", "derived", "ref", "source", "options", Map.of("bitOffset", 0)), "派生位段必须声明 bitOffset 和 bitWidth"),
                Arguments.of(DynamicField.field("value", "derived").ref("source").options(Map.of("bitWidth", 1)),
                        Map.of("type", "derived", "ref", "source", "options", Map.of("bitWidth", 1)), "派生位段必须声明 bitOffset 和 bitWidth"),
                Arguments.of(DynamicField.field("value", "derived").ref("source").options(nullOffset),
                        Map.of("type", "derived", "ref", "source", "options", nullOffset), "派生位段必须声明 bitOffset 和 bitWidth"),
                Arguments.of(DynamicField.field("value", "derived").ref("source").options(nullWidth),
                        Map.of("type", "derived", "ref", "source", "options", nullWidth), "派生位段必须声明 bitOffset 和 bitWidth"),
                Arguments.of(DynamicField.field("value", "derived").ref("source").expression(DynamicExpression.value(1)),
                        Map.of("type", "derived", "ref", "source", "expression", expression), "expression 仅用于派生字段"),
                Arguments.of(DynamicField.field("value", "u8").expression(DynamicExpression.value(1)),
                        Map.of("type", "u8", "expression", expression), "expression 仅用于派生字段"),
                Arguments.of(DynamicField.field("value", "u8").generated(DynamicGeneratedValue.constant(1)).computedLengthOf("body"),
                        Map.of("type", "u8", "generated", Map.of("constant", 1), "computedLengthOf", "body"), "generated 与 computedLengthOf 互斥"),
                Arguments.of(DynamicField.field("value", "u8").generated(DynamicGeneratedValue.countOf("items")).computedLengthOf("body"),
                        Map.of("type", "u8", "generated", Map.of("countOf", "items"), "computedLengthOf", "body"), "generated 与 computedLengthOf 互斥"),
                Arguments.of(DynamicField.field("value", "u8").generated(DynamicGeneratedValue.expression(DynamicExpression.value(1))).computedLengthOf("body"),
                        Map.of("type", "u8", "generated", Map.of("expression", expression), "computedLengthOf", "body"), "generated 与 computedLengthOf 互斥"),
                Arguments.of(DynamicField.field("value", "i8").generated(DynamicGeneratedValue.countOf("items")),
                        Map.of("type", "i8", "generated", Map.of("countOf", "items")), "计数和校验和只能用于无符号整数"),
                Arguments.of(DynamicField.field("value", "i32").generated(checksum),
                        Map.of("type", "i32", "generated", checksumJson), "计数和校验和只能用于无符号整数"),
                Arguments.of(DynamicField.field("value", "u8").generated(checksum),
                        Map.of("type", "u8", "generated", checksumJson), "校验和字段位宽必须等于算法位宽"),
                Arguments.of(DynamicField.field("value", "struct").fields().generated(DynamicGeneratedValue.constant(1)),
                        Map.of("type", "struct", "fields", new Object[0], "generated", Map.of("constant", 1)), "固定值与生成表达式只能用于线上标量"));
    }

    @ParameterizedTest
    @MethodSource("validDefinitions")
    void acceptsValidDerivedAndGeneratedDefinitions(DynamicField.Builder builder) {
        assertRoundTrip(assertDoesNotThrow(builder::build));
    }

    static Stream<DynamicField.Builder> validDefinitions() {
        return Stream.of(
                DynamicField.field("value", "string").length(DynamicLength.fixed(0, "byte")).charset("UTF-8"),
                DynamicField.field("value", "derived").ref("source").options(Map.of("bitOffset", 0, "bitWidth", 1)),
                DynamicField.field("value", "derived").expression(DynamicExpression.value(1)),
                DynamicField.field("value", "u8").generated(DynamicGeneratedValue.countOf("items")),
                DynamicField.field("value", "u32").generated(DynamicGeneratedValue.checksum(
                        new DynamicChecksum(DynamicChecksumAlgorithm.CRC32, "body"))),
                DynamicField.field("value", "i8").generated(DynamicGeneratedValue.constant(-1)),
                DynamicField.field("value", "u8").generated(DynamicGeneratedValue.expression(DynamicExpression.value(1))));
    }

    private static void assertRejected(DynamicField.Builder builder, Map<String, Object> properties, String message) throws Exception {
        final IllegalArgumentException buildError = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(buildError.getMessage().contains(message), buildError::getMessage);
        final Map<String, Object> field = new LinkedHashMap<>();
        field.put("id", "value");
        field.putAll(properties);
        final String json = new ObjectMapper().writeValueAsString(Map.of("schemaVersion", 1, "fields", new Object[]{field}));
        final IllegalArgumentException importError = assertThrows(IllegalArgumentException.class, () -> DynamicSchemaJson.read(json));
        assertNotNull(importError.getCause());
        assertTrue(importError.getCause().getMessage().contains(message), importError.getCause()::getMessage);
    }

    private static void assertRoundTrip(DynamicField field) {
        final DynamicSchema schema = DynamicSchema.of(field);
        assertEquals(schema, DynamicSchemaJson.read(DynamicSchemaJson.write(schema)));
    }
}
