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

package io.github.hylexus.xtream.codec.core.tracker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hylexus.xtream.codec.core.type.wrapper.*;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 跟踪值的类型、无符号字节、摘要边界及视图 JSON 测试。
 *
 * @author Codex (AI)
 */
class CodecTraceValueRendererTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    // StringBuilder 是非 String 的字符序列输入，用于验证渲染器保留实际类型。
    @SuppressWarnings("UnnecessaryStringBuilder")
    static Stream<Arguments> scalars() {
        return Stream.of(
                Arguments.of("中文", "中文", "\"中文\""),
                Arguments.of(new StringBuilder("文本"), "文本", "\"文本\""),
                Arguments.of(Byte.MIN_VALUE, "-128", "-128"),
                Arguments.of(Short.MIN_VALUE, "-32768", "-32768"),
                Arguments.of(Integer.MIN_VALUE, "-2147483648", "-2147483648"),
                Arguments.of(Long.MAX_VALUE, "9223372036854775807", "9223372036854775807"),
                Arguments.of(new BigInteger("18446744073709551615"), "18446744073709551615", "18446744073709551615"),
                Arguments.of(new BigDecimal("1.2500"), "1.2500", "1.2500"),
                Arguments.of(1.5f, "1.5", "1.5"),
                Arguments.of(1.5d, "1.5", "1.5"),
                Arguments.of(true, "true", "true"),
                Arguments.of(false, "false", "false"),
                Arguments.of(State.READY, "已就绪", "\"READY\""));
    }

    @ParameterizedTest
    @MethodSource("scalars")
    void preservesScalarTypesWhileRenderingSummary(Object input, String summary, String json) throws Exception {
        final Object value = CodecTraceValueRenderer.toJsonValue(input);
        assertThat(value).isSameAs(input);
        assertThat(CodecTraceValueRenderer.toSummary(input)).isEqualTo(summary);
        assertThat(objectMapper.writeValueAsString(value)).isEqualTo(json);
    }

    static Stream<Arguments> wrappers() {
        return Stream.of(
                Arguments.of(new U8Wrapper((short) 0), (short) 0, "0", "0"),
                Arguments.of(new U8Wrapper((short) -1), (short) 255, "255", "255"),
                Arguments.of(new U16Wrapper(0), 0, "0", "0"),
                Arguments.of(new U16Wrapper(-1), 65535, "65535", "65535"),
                Arguments.of(new U32Wrapper(0L), 0L, "0", "0"),
                Arguments.of(new U32Wrapper(-1L), 4294967295L, "4294967295", "4294967295"),
                Arguments.of(new I8Wrapper(Byte.MIN_VALUE), Byte.MIN_VALUE, "-128", "-128"),
                Arguments.of(new I8Wrapper(Byte.MAX_VALUE), Byte.MAX_VALUE, "127", "127"),
                Arguments.of(new I16Wrapper(Short.MIN_VALUE), Short.MIN_VALUE, "-32768", "-32768"),
                Arguments.of(new I16Wrapper(Short.MAX_VALUE), Short.MAX_VALUE, "32767", "32767"),
                Arguments.of(new I32Wrapper(Integer.MIN_VALUE), Integer.MIN_VALUE, "-2147483648", "-2147483648"),
                Arguments.of(new I32Wrapper(Integer.MAX_VALUE), Integer.MAX_VALUE, "2147483647", "2147483647"),
                Arguments.of(new StringWrapperGbk("中文"), "中文", "中文", "\"中文\""),
                Arguments.of(new StringWrapperUtf8("中文😀"), "中文😀", "中文😀", "\"中文😀\""),
                Arguments.of(new StringWrapperBcd("001234"), "001234", "001234", "\"001234\""),
                Arguments.of(new StringWrapperGbk(""), "", "", "\"\""),
                Arguments.of(new StringWrapperUtf8(""), "", "", "\"\""),
                Arguments.of(new StringWrapperBcd(""), "", "", "\"\""),
                Arguments.of(new StringWrapperGbk(), null, null, "null"),
                Arguments.of(new StringWrapperUtf8(), null, null, "null"),
                Arguments.of(new StringWrapperBcd(), null, null, "null"));
    }

    @ParameterizedTest
    @MethodSource("wrappers")
    void rendersWrapperValuesInsteadOfTheirDebugStrings(DataWrapper<?> wrapper, @Nullable Object expected,
                                                        @Nullable String summary, String json) throws Exception {
        final Object value = CodecTraceValueRenderer.toJsonValue(wrapper);
        assertThat(value).isEqualTo(expected);
        assertThat(objectMapper.writeValueAsString(value)).isEqualTo(json);
        assertThat(CodecTraceValueRenderer.toSummary(wrapper)).isEqualTo(summary);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void snapshotsUnsignedBytesWithoutAliasingTheInput(boolean wrapped) throws Exception {
        final byte[] bytes = {0, 127, (byte) 128, (byte) 255};
        final Object input = wrapped ? new BytesDataWrapper(bytes) : bytes;
        final Object rendered = CodecTraceValueRenderer.toJsonValue(input);
        assertThat(rendered).isEqualTo(List.of(0, 127, 128, 255));
        assertThat(objectMapper.writeValueAsString(rendered)).isEqualTo("[0,127,128,255]");
        assertThat(CodecTraceValueRenderer.toSummary(input)).isEqualTo("[0, 127, 128, 255]");
        assertThat(bytes).containsExactly(0, 127, (byte) 128, (byte) 255);
        bytes[0] = 9;
        assertThat(rendered).isEqualTo(List.of(0, 127, 128, 255));
        assertThat(CodecTraceValueRenderer.toJsonValue(input)).isEqualTo(List.of(9, 127, 128, 255));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rendersEmptyBytesAsEmptyArray(boolean wrapped) throws Exception {
        final Object input = wrapped ? new BytesDataWrapper(new byte[0]) : new byte[0];
        assertThat(CodecTraceValueRenderer.toJsonValue(input)).isEqualTo(List.of());
        assertThat(CodecTraceValueRenderer.toSummary(input)).isEqualTo("[]");
        assertThat(objectMapper.writeValueAsString(CodecTraceValueRenderer.toJsonValue(input))).isEqualTo("[]");
    }

    static Stream<Arguments> otherValues() {
        return Stream.of(
                Arguments.of(new DisplayValue("设备 A"), "设备 A", "\"设备 A\""),
                Arguments.of(List.of(1, 2), "[1, 2]", "\"[1, 2]\""),
                Arguments.of(Map.of("status", "ok"), "{status=ok}", "\"{status=ok}\""));
    }

    @ParameterizedTest
    @MethodSource("otherValues")
    void usesTextFallbackForOtherValues(Object input, String text, String json) throws Exception {
        assertThat(CodecTraceValueRenderer.toJsonValue(input)).isEqualTo(text);
        assertThat(CodecTraceValueRenderer.toSummary(input)).isEqualTo(text);
        assertThat(objectMapper.writeValueAsString(CodecTraceValueRenderer.toJsonValue(input))).isEqualTo(json);
    }

    static Stream<Arguments> summaries() {
        return Stream.of(
                Arguments.of("x".repeat(119), "x".repeat(119)),
                Arguments.of("x".repeat(120), "x".repeat(120)),
                Arguments.of("x".repeat(121), "x".repeat(120) + "..."),
                Arguments.of("文".repeat(119), "文".repeat(119)),
                Arguments.of("文".repeat(120), "文".repeat(120)),
                Arguments.of("文".repeat(121), "文".repeat(120) + "..."));
    }

    @ParameterizedTest
    @MethodSource("summaries")
    void truncatesOnlySummaryAndKeepsFullViewValue(String input, String expected) throws Exception {
        assertThat(CodecTraceValueRenderer.toSummary(input)).isEqualTo(expected);
        final CodecTraceView view = view(input);
        final CodecTraceView.Node node = view.root().children().getFirst();
        assertThat(node.value()).isEqualTo(input);
        assertThat(node.valueSummary()).isEqualTo(expected);
        final JsonNode json = objectMapper.readTree(objectMapper.writeValueAsBytes(view)).path("root").path("children").get(0);
        assertThat(json.path("value").textValue()).isEqualTo(input);
        assertThat(json.path("valueSummary").textValue()).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void measuresByteSummaryAfterUnsignedArrayFormatting(boolean wrapped) {
        final Object exact = wrapped ? new BytesDataWrapper(new byte[40]) : new byte[40];
        final Object longer = wrapped ? new BytesDataWrapper(new byte[41]) : new byte[41];
        assertThat(CodecTraceValueRenderer.toSummary(exact)).isEqualTo("[" + "0, ".repeat(39) + "0]");
        assertThat(CodecTraceValueRenderer.toSummary(longer)).isEqualTo("[" + "0, ".repeat(39) + "0,...");
    }

    static Stream<Arguments> viewValues() {
        return Stream.of(
                Arguments.of(new byte[]{0, 127, (byte) 128, (byte) 255}, "[0,127,128,255]", "[0, 127, 128, 255]"),
                Arguments.of(new BytesDataWrapper(new byte[]{(byte) 255}), "[255]", "[255]"),
                Arguments.of(new U32Wrapper(-1L), "4294967295", "4294967295"),
                Arguments.of(true, "true", "true"),
                Arguments.of(State.READY, "\"READY\"", "已就绪"),
                Arguments.of(new DisplayValue("设备 A"), "\"设备 A\"", "设备 A"));
    }

    @ParameterizedTest
    @MethodSource("viewValues")
    void exportsTypedValuesThroughTraceView(Object input, String expectedJson, String summary) throws Exception {
        final CodecTraceView view = view(input);
        final JsonNode json = objectMapper.readTree(objectMapper.writeValueAsBytes(view));
        final JsonNode field = json.path("root").path("children").get(0);
        assertThat(field.path("value")).isEqualTo(objectMapper.readTree(expectedJson));
        assertThat(field.path("valueSummary").textValue()).isEqualTo(summary);
        assertThat(json.path("nodes").get(1).path("value")).isEqualTo(field.path("value"));
    }

    @Test
    void keepsExistingByteViewIndependentOfLaterInputChanges() {
        final byte[] input = {(byte) 255};
        final CodecTraceView view = view(input);
        input[0] = 1;
        assertThat(view.root().children().getFirst().value()).isEqualTo(List.of(255));
        assertThat(view.root().children().getFirst().valueSummary()).isEqualTo("[255]");
    }

    @Test
    void clearsValueAndSummaryWhenNodeIsReplacedWithNull() throws Exception {
        assertThat(CodecTraceValueRenderer.toJsonValue(null)).isNull();
        assertThat(CodecTraceValueRenderer.toSummary(null)).isNull();
        final CodecTrace trace = new CodecTrace();
        final CodecTraceNode node = new CodecTraceNode(CodecTraceNodeKind.FIELD, "value", trace.getRoot());
        trace.getRoot().addChild(node);
        node.setValue("x".repeat(121));
        assertThat(node.getValueSummary()).isEqualTo("x".repeat(120) + "...");
        node.setValue("short");
        assertThat(node.getValueSummary()).isEqualTo("short");
        node.setValue(null);
        final CodecTraceView view = CodecTraceView.from(trace);
        assertThat(view.root().children().getFirst().value()).isNull();
        assertThat(view.root().children().getFirst().valueSummary()).isNull();
        final JsonNode field = objectMapper.readTree(objectMapper.writeValueAsBytes(view)).path("root").path("children").get(0);
        assertThat(field.path("value").isNull()).isTrue();
        assertThat(field.path("valueSummary").isNull()).isTrue();
    }

    private static CodecTraceView view(Object input) {
        final CodecTrace trace = new CodecTrace();
        trace.getRoot().addChild(new CodecTraceNode(CodecTraceNodeKind.FIELD, "value", trace.getRoot()).setValue(input));
        return CodecTraceView.from(trace);
    }

    private enum State {
        READY;

        @Override
        public String toString() {
            return "已就绪";
        }
    }

    private record DisplayValue(String text) {
        @Override
        public String toString() {
            return text;
        }
    }
}
