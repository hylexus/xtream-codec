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

package io.github.hylexus.xtream.codec.core.type.simple;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static io.github.hylexus.xtream.codec.core.type.simple.DataFields.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证运行时字段 JSON 的结构、配置和类型错误能通过公开入口被识别。
 *
 * @author Codex (AI)
 */
class DataFieldJsonValidationTest {
    private static final ObjectMapper JSON = new ObjectMapper();

    @ParameterizedTest
    @ValueSource(strings = {"", "{", "[", "{\"type\":", "{\"type\":\"u8\",\"value\":}"})
    void malformedJsonRetainsJacksonFailure(String json) {
        assertInstanceOf(JsonProcessingException.class,
                assertThrows(RuntimeException.class, () -> parseSimpleFieldFromJson(json)).getCause());
        assertInstanceOf(JsonProcessingException.class,
                assertThrows(RuntimeException.class, () -> parseSimpleFieldsFromJson(json)).getCause());
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    void invalidFieldsReportConfigurationOrValueCause(String json, String message) throws Exception {
        for (final InputMode mode : InputMode.values()) {
            assertRejected(json, mode, message);
        }
    }

    static Stream<Arguments> invalidFields() {
        return Stream.of(
                Arguments.of("{\"value\":1}", "type"),
                Arguments.of("{\"type\":\"not.a.DataField\",\"value\":1}", "Unknown type id"),
                Arguments.of("{\"type\":\"java.lang.String\",\"value\":1}", "not subtype"),
                Arguments.of("{\"type\":\"i32\",\"value\":\"abc\"}", "abc"),
                Arguments.of("{\"type\":\"i8\",\"value\":256}", "out of range"),
                Arguments.of("{\"type\":\"dict\",\"valueLengthType\":\"u8\",\"value\":{}}", "keyType is required"),
                Arguments.of("{\"type\":\"dict\",\"keyType\":null,\"valueLengthType\":\"u8\",\"value\":{}}", "keyType is required"),
                Arguments.of("{\"type\":\"dict\",\"keyType\":\"string\",\"valueLengthType\":\"u8\",\"value\":{}}", "Invalid keyType"),
                Arguments.of("{\"type\":\"dict\",\"keyType\":\"u8\",\"value\":{}}", "valueLengthType is required"),
                Arguments.of("{\"type\":\"dict\",\"keyType\":\"u8\",\"valueLengthType\":null,\"value\":{}}", "valueLengthType is required"),
                Arguments.of(dict("u8", "\"u128\"", "{}", ""), "LengthFieldType.u128"),
                Arguments.of(dict("u8", "\"u8\"", "{}", ",\"prependLengthFieldType\":\"u128\""), "PrependLengthFieldType.u128"),
                Arguments.of(dict("u8", "\"u8\"", "{\"abc\":{\"type\":\"u8\",\"value\":1}}", ""), "abc"),
                Arguments.of(dict("i8", "\"u8\"", "{\"128\":{\"type\":\"u8\",\"value\":1}}", ""), "128"),
                Arguments.of(dict("u8", "\"u8\"", "{\"1\":{\"type\":\"missing.field\",\"value\":1}}", ""), "Unknown type id"));
    }

    @ParameterizedTest
    @MethodSource("invalidDictionaryValues")
    void dictionaryRequiresObjectValue(String value, InputMode mode) throws Exception {
        final String json = value.isEmpty()
                ? "{\"type\":\"dict\",\"keyType\":\"u8\",\"valueLengthType\":\"u8\"}"
                : dict("u8", "\"u8\"", value, "");
        final RuntimeException failure = assertRejected(json, mode, "value must be a JSON object");
        assertTrue(hasCause(failure, JsonProcessingException.class), failure::toString);
    }

    static Stream<Arguments> invalidDictionaryValues() {
        return Stream.of("", "null", "[]", "42", "true", "\"text\"")
                .flatMap(value -> Stream.of(InputMode.values()).map(mode -> Arguments.of(value, mode)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"type\":\"u8\",\"value\":1}", "1", "\"text\""})
    void listEntrancesRejectNonArrays(String json) throws Exception {
        final RuntimeException stringFailure = assertThrows(RuntimeException.class, () -> parseSimpleFieldsFromJson(json));
        assertInstanceOf(JsonProcessingException.class, stringFailure.getCause());
        final Object object = JSON.readValue(json, Object.class);
        final IllegalArgumentException objectFailure = assertThrows(IllegalArgumentException.class, () -> parseSimpleFieldsFromObject(object));
        assertTrue(hasCause(objectFailure, JsonProcessingException.class), objectFailure::toString);
    }

    private static String dict(String keyType, String valueLengthType, String value, String extra) {
        return "{\"type\":\"dict\",\"keyType\":\"" + keyType + "\",\"valueLengthType\":" + valueLengthType
                + ",\"value\":" + value + extra + "}";
    }

    private static RuntimeException assertRejected(String json, InputMode mode, String message) throws Exception {
        final Object object = JSON.readValue(json, Object.class);
        final RuntimeException failure = assertThrows(RuntimeException.class, switch (mode) {
            case JSON -> () -> parseSimpleFieldFromJson(json);
            case OBJECT -> () -> parseSimpleFieldFromObject(object);
            case JSON_LIST -> () -> parseSimpleFieldsFromJson("[" + json + "]");
            case OBJECT_LIST -> () -> parseSimpleFieldsFromObject(List.of(object));
        }, mode::name);
        if (mode == InputMode.OBJECT || mode == InputMode.OBJECT_LIST) {
            assertInstanceOf(IllegalArgumentException.class, failure);
        }
        final StringBuilder causes = new StringBuilder();
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            causes.append(cause.getMessage()).append('\n');
        }
        assertTrue(causes.toString().contains(message), () -> mode + ": " + causes);
        return failure;
    }

    private static boolean hasCause(Throwable failure, Class<? extends Throwable> type) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (type.isInstance(cause)) {
                return true;
            }
        }
        return false;
    }

    enum InputMode {
        JSON, OBJECT, JSON_LIST, OBJECT_LIST
    }
}
