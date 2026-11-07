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

package io.github.hylexus.xtream.codec.core.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.hylexus.xtream.codec.core.type.wrapper.*;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 调试 JSON 序列化的类型、数值边界及容器输出测试。
 *
 * @author Codex (AI)
 */
class XtreamCodecDebugJsonSerializerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    static Stream<Arguments> wrappers() {
        return Stream.of(
                Arguments.of(new U8Wrapper((short) 0), "0"),
                Arguments.of(new U8Wrapper((short) 128), "128"),
                Arguments.of(new U8Wrapper((short) 255), "255"),
                Arguments.of(new U8Wrapper((short) -1), "255"),
                Arguments.of(new U16Wrapper(0), "0"),
                Arguments.of(new U16Wrapper(32768), "32768"),
                Arguments.of(new U16Wrapper(65535), "65535"),
                Arguments.of(new U16Wrapper(-1), "65535"),
                Arguments.of(new U32Wrapper(0L), "0"),
                Arguments.of(new U32Wrapper(2147483648L), "2147483648"),
                Arguments.of(new U32Wrapper(4294967295L), "4294967295"),
                Arguments.of(new U32Wrapper(-1L), "4294967295"),
                Arguments.of(new I8Wrapper(Byte.MIN_VALUE), "-128"),
                Arguments.of(new I8Wrapper((byte) 0), "0"),
                Arguments.of(new I8Wrapper(Byte.MAX_VALUE), "127"),
                Arguments.of(new I16Wrapper(Short.MIN_VALUE), "-32768"),
                Arguments.of(new I16Wrapper((short) 0), "0"),
                Arguments.of(new I16Wrapper(Short.MAX_VALUE), "32767"),
                Arguments.of(new I32Wrapper(Integer.MIN_VALUE), "-2147483648"),
                Arguments.of(new I32Wrapper(0), "0"),
                Arguments.of(new I32Wrapper(Integer.MAX_VALUE), "2147483647"),
                Arguments.of(new StringWrapperGbk("中文"), "\"中文\""),
                Arguments.of(new StringWrapperUtf8("中文😀"), "\"中文😀\""),
                Arguments.of(new StringWrapperBcd("001234"), "\"001234\""),
                Arguments.of(new StringWrapperGbk(""), "\"\""),
                Arguments.of(new StringWrapperUtf8(""), "\"\""),
                Arguments.of(new StringWrapperBcd(""), "\"\""),
                Arguments.of(new StringWrapperGbk(), "null"),
                Arguments.of(new StringWrapperUtf8(), "null"),
                Arguments.of(new StringWrapperBcd(), "null"),
                Arguments.of(new StringWrapperUtf8("引号\"、反斜杠\\、换行\n"), "\"引号\\\"、反斜杠\\\\、换行\\n\""));
    }

    @ParameterizedTest
    @MethodSource("wrappers")
    void serializesWrappersAtRootAndInsideObjectsAndLists(DataWrapper<?> value, String json) throws Exception {
        assertThat(objectMapper.writeValueAsString(value)).isEqualTo(json);
        assertThat(objectMapper.writeValueAsString(List.of(value, "after")))
                .isEqualTo("[" + json + ",\"after\"]");
        assertThat(objectMapper.readTree(objectMapper.writeValueAsString(new WrapperValue(value, "after"))))
                .isEqualTo(objectMapper.readTree("{\"value\":" + json + ",\"tail\":\"after\"}"));
    }

    static Stream<Arguments> bytes() {
        return Stream.of(
                Arguments.of(new byte[0], "[]"),
                Arguments.of(new byte[]{0, 127, (byte) 128, (byte) 255}, "[0,127,-128,-1]"));
    }

    @ParameterizedTest
    @MethodSource("bytes")
    void serializesRawAndWrappedBytesAsSignedNumberArrays(byte[] value, String json) throws Exception {
        final byte[] original = value.clone();
        final BytesDataWrapper wrapper = new BytesDataWrapper(value);
        final String expected = "{\"value\":" + json + ",\"tail\":\"after\"}";

        assertThat(objectMapper.writeValueAsString(wrapper)).isEqualTo(json);
        assertThat(objectMapper.readTree(objectMapper.writeValueAsString(new BytesValue(value, "after"))))
                .isEqualTo(objectMapper.readTree(expected));
        assertThat(objectMapper.readTree(objectMapper.writeValueAsString(new WrapperValue(wrapper, "after"))))
                .isEqualTo(objectMapper.readTree(expected));
        assertThat(objectMapper.writeValueAsString(List.of(wrapper, "after")))
                .isEqualTo("[" + json + ",\"after\"]");
        assertThat(value).containsExactly(original);
    }

    @Test
    void serializesNullRawByteFieldWithoutLosingFollowingProperty() throws Exception {
        final String json = objectMapper.writeValueAsString(new BytesValue(null, "after"));
        assertThat(objectMapper.readTree(json))
                .isEqualTo(objectMapper.readTree("{\"value\":null,\"tail\":\"after\"}"));
    }

    static Stream<Arguments> otherValues() {
        return Stream.of(
                Arguments.of(null, "null"),
                Arguments.of(42, "42"),
                Arguments.of(true, "true"),
                Arguments.of(false, "false"),
                Arguments.of("中文", "\"中文\""),
                Arguments.of(List.of(1, 2), "[1,2]"),
                Arguments.of(Map.of("status", "ok"), "{\"status\":\"ok\"}"),
                Arguments.of(new Payload("设备 A"), "{\"name\":\"设备 A\"}"));
    }

    @ParameterizedTest
    @MethodSource("otherValues")
    void delegatesOtherAnnotatedValuesToJackson(@Nullable Object value, String json) throws Exception {
        final String actual = objectMapper.writeValueAsString(new DebugValue(value, "after"));
        assertThat(objectMapper.readTree(actual))
                .isEqualTo(objectMapper.readTree("{\"value\":" + json + ",\"tail\":\"after\"}"));
    }

    public record WrapperValue(DataWrapper<?> value, String tail) {
    }

    public record BytesValue(
            @JsonSerialize(using = XtreamCodecDebugJsonSerializer.class) byte @Nullable [] value,
            String tail) {
    }

    public record DebugValue(
            @JsonSerialize(using = XtreamCodecDebugJsonSerializer.class) @Nullable Object value,
            String tail) {
    }

    public record Payload(String name) {
    }
}
