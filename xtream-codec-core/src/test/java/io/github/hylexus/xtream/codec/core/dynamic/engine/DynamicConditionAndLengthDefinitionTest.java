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
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicCondition;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicConditionOperator;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicExpression;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLengthUnit;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchemaJson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 条件和长度定义在 Java 与 JSON 入口的拒绝规则及合法编解码测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicConditionAndLengthDefinitionTest {
    static Stream<Arguments> invalidConditions() {
        return Stream.of(false, true).flatMap(json -> Stream.of(
                Arguments.of(json, "missingRef", "条件引用不能为空"),
                Arguments.of(json, "blankRef", "条件引用不能为空"),
                Arguments.of(json, "missingOp", "条件运算符不能为空"),
                Arguments.of(json, "missingValue", "条件值不能为空"),
                Arguments.of(json, "expressionRef", "条件表达式与简写条件互斥"),
                Arguments.of(json, "expressionOp", "条件表达式与简写条件互斥"),
                Arguments.of(json, "expressionValue", "条件表达式与简写条件互斥")));
    }

    @ParameterizedTest
    @MethodSource("invalidConditions")
    void rejectsIncompleteAndConflictingConditions(boolean json, String kind, String message) throws Exception {
        final Map<String, Object> definition = new LinkedHashMap<>();
        if (kind.startsWith("expression")) {
            definition.put("expression", Map.of("value", true));
            definition.put(switch (kind) {
                case "expressionRef" -> "ref";
                case "expressionOp" -> "op";
                default -> "value";
            }, kind.equals("expressionRef") ? "flag" : kind.equals("expressionOp") ? "eq" : "1");
        } else {
            definition.put("ref", kind.equals("blankRef") ? " \t\n" : "flag");
            definition.put("op", "eq");
            definition.put("value", "1");
            if (!kind.equals("blankRef")) {
                definition.remove(switch (kind) {
                    case "missingRef" -> "ref";
                    case "missingOp" -> "op";
                    default -> "value";
                });
            }
        }
        final String text = json(definition, true);
        final IllegalArgumentException error = catchThrowableOfType(IllegalArgumentException.class, () -> {
            if (json) {
                DynamicSchemaJson.read(text);
            } else {
                new DynamicCondition((String) definition.get("ref"), definition.containsKey("op") ? DynamicConditionOperator.EQ : null,
                        (String) definition.get("value"), definition.containsKey("expression") ? DynamicExpression.value(true) : null);
            }
        });
        assertRejected(error, json, message);
    }

    static Stream<Arguments> invalidLengths() {
        return Stream.of(false, true).flatMap(json -> Stream.of(
                Arguments.of(json, "missingUnit", "长度单位不能为空"),
                Arguments.of(json, "blankRef", "长度引用不能为空"),
                Arguments.of(json, "missingSource", "length 必须且只能指定一种来源"),
                Arguments.of(json, "conflictingSources", "length 必须且只能指定一种来源"),
                Arguments.of(json, "expressionAndFixed", "length 必须且只能指定一种来源"),
                Arguments.of(json, "remainingItems", "剩余长度不能使用 item 单位")));
    }

    @ParameterizedTest
    @MethodSource("invalidLengths")
    void rejectsInvalidUnitsReferencesAndSources(boolean json, String kind, String message) throws Exception {
        final Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("unit", kind.equals("remainingItems") ? "item" : "byte");
        switch (kind) {
            case "missingUnit" -> {
                definition.remove("unit");
                definition.put("fixed", 2);
            }
            case "blankRef" -> definition.put("ref", " \t\n");
            case "conflictingSources" -> {
                definition.put("fixed", 2);
                definition.put("ref", "flag");
            }
            case "expressionAndFixed" -> {
                definition.put("fixed", 2);
                definition.put("expression", Map.of("value", 2));
            }
            case "remainingItems" -> definition.put("remaining", true);
            case "missingSource" -> definition.put("remaining", false);
            default -> throw new IllegalArgumentException(kind);
        }
        final String text = json(definition, false);
        final IllegalArgumentException error = catchThrowableOfType(IllegalArgumentException.class, () -> {
            if (json) {
                DynamicSchemaJson.read(text);
            } else {
                final DynamicLengthUnit unit = definition.containsKey("unit") ? DynamicLengthUnit.from((String) definition.get("unit")) : null;
                new DynamicLength((Integer) definition.get("fixed"), (String) definition.get("ref"), null,
                        (Boolean) definition.get("remaining"), unit, definition.containsKey("expression") ? DynamicExpression.value(2) : null);
            }
        });
        assertRejected(error, json, message);
    }

    static Stream<Arguments> validConditions() {
        return Stream.of(false, true).flatMap(json -> Stream.of("eq", "ne", "expression").map(kind -> Arguments.of(json, kind)));
    }

    @ParameterizedTest
    @MethodSource("validConditions")
    void legalConditionsControlActualBytes(boolean json, String kind) {
        final DynamicCondition condition = kind.equals("expression") ? DynamicCondition.expression(DynamicExpression.value(true))
                : new DynamicCondition("flag", kind, "1");
        final DynamicSchema schema = DynamicSchema.of(DynamicField.field("flag", "u8").build(),
                DynamicField.field("data", "u8").when(condition).build(), DynamicField.field("tail", "u8").build());
        final boolean present = !kind.equals("ne");
        assertRoundTrip(schema, json, Map.of("flag", 1, "data", 0xab, "tail", 127),
                present ? Map.of("flag", 1L, "data", 0xabL, "tail", 127L) : Map.of("flag", 1L, "tail", 127L),
                present ? "01ab7f" : "017f", true);
    }

    static Stream<Arguments> validLengths() {
        return Stream.of(false, true).flatMap(json -> Stream.of("fixed", "ref", "prefix", "remainingBytes", "remainingBits")
                .map(kind -> Arguments.of(json, kind)));
    }

    @ParameterizedTest
    @MethodSource("validLengths")
    void legalLengthSourcesPreserveBoundariesAndContents(boolean json, String kind) {
        final DynamicLength length = switch (kind) {
            case "fixed" -> new DynamicLength(2, null, null, null, "byte");
            case "ref" -> DynamicLength.ref("flag", "byte");
            case "prefix" -> DynamicLength.prefix(8, "byte");
            case "remainingBytes" -> DynamicLength.remaining("byte");
            default -> DynamicLength.remaining("bit");
        };
        final boolean remaining = kind.startsWith("remaining");
        final List<DynamicField> fields = new ArrayList<>(List.of(DynamicField.field("flag", "u8").build(),
                DynamicField.field("data", "hex").length(length).build()));
        if (!remaining) {
            fields.add(DynamicField.field("tail", "u8").build());
        }
        assertRoundTrip(DynamicSchema.of(fields.toArray(DynamicField[]::new)), json,
                remaining ? Map.of("flag", 2, "data", "1234") : Map.of("flag", 2, "data", "1234", "tail", 127),
                remaining ? Map.of("flag", 2L, "data", "1234") : Map.of("flag", 2L, "data", "1234", "tail", 127L),
                remaining ? "021234" : kind.equals("prefix") ? "020212347f" : "0212347f", !remaining);
    }

    private static void assertRejected(IllegalArgumentException error, boolean json, String message) {
        if (json) {
            assertThat(error).hasMessage("无效的动态结构 JSON").hasRootCauseMessage(message);
        } else {
            assertThat(error).hasMessage(message);
        }
    }

    private static String json(Map<String, Object> definition, boolean condition) throws Exception {
        return new ObjectMapper().writeValueAsString(Map.of("schemaVersion", 1, "fields", List.of(
                Map.of("id", "flag", "type", "u8"),
                Map.of("id", "data", "type", condition ? "u8" : "hex", condition ? "when" : "length", definition))));
    }

    private static void assertRoundTrip(DynamicSchema schema, boolean json, Map<String, Object> supplied,
                                        Map<String, Object> expected, String wire, boolean trailing) {
        final DynamicPlan plan = DynamicCompiler.compile(json ? DynamicSchemaJson.read(DynamicSchemaJson.write(schema)) : schema);
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final ByteBuf input = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump("55aa" + wire + (trailing ? "66" : ""))).readerIndex(2);
        try {
            assertThat(plan.encode(DynamicValues.of(supplied), output)).isEqualTo(wire.length() * 4L);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + wire);
            assertThat(output.readerIndex()).isEqualTo(1);
            final DynamicDecodeResult result = plan.decode(input);
            assertThat(result.values().fields()).isEqualTo(expected);
            assertThat(result.consumedBits()).isEqualTo(wire.length() * 4L);
            assertThat(result.remainingBytes()).isEqualTo(trailing ? 1 : 0);
            assertThat(input.readerIndex()).isEqualTo(2 + wire.length() / 2);
            if (trailing) {
                assertThat(input.getUnsignedByte(input.readerIndex())).isEqualTo((short) 0x66);
            }
            assertThat(input.refCnt()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
        } finally {
            input.release();
            output.release();
        }
    }
}
