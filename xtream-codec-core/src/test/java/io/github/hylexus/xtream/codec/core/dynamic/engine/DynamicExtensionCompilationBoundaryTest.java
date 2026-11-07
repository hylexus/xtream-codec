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
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchemaJson;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtension;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtensionRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 动态扩展标识、配置校验异常和失败后重新编译的测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicExtensionCompilationBoundaryTest {
    static Stream<Arguments> invalidIds() {
        return Stream.of(false, true).flatMap(json -> Stream.of("missing", "null", "empty", "blank", "number")
                .map(kind -> Arguments.of(json, kind)));
    }

    @ParameterizedTest
    @MethodSource("invalidIds")
    void rejectsInvalidIdsBeforeCallingAnyExtensionMethod(boolean json, String kind) {
        final Map<String, Object> options = new LinkedHashMap<>();
        switch (kind) {
            case "null" -> options.put("extensionId", null);
            case "empty" -> options.put("extensionId", "");
            case "blank" -> options.put("extensionId", " \t\n");
            case "number" -> options.put("extensionId", 1);
            case "missing" -> { }
            default -> throw new IllegalArgumentException(kind);
        }
        final CountingExtension extension = new CountingExtension();
        final DynamicExtensionRegistry registry = new DynamicExtensionRegistry(Map.of("test", extension));
        final DynamicSchema schema = schema(options, json);
        final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class, () -> DynamicCompiler.compile(schema, registry));
        assertThat(error).hasMessageContaining("扩展必须声明非空 extensionId");
        assertLocation(error);
        assertThat(extension.validations).isZero();
        assertThat(extension.encodes).isZero();
        assertThat(extension.decodes).isZero();
        assertHealthy(registry, json, extension);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void preservesTheConfigurationFailureCauseAndCanCompileAgain(boolean json) {
        final CountingExtension extension = new CountingExtension();
        final DynamicExtensionRegistry registry = new DynamicExtensionRegistry(Map.of("test", extension));
        final DynamicSchema schema = schema(Map.of("extensionId", "test", "invalid", true), json);
        final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class, () -> DynamicCompiler.compile(schema, registry));
        assertThat(error).hasMessageContaining("扩展配置无效: 测试扩展配置失败").cause().isSameAs(extension.failure);
        assertLocation(error);
        assertThat(extension.validations).isEqualTo(1);
        assertThat(extension.encodes).isZero();
        assertThat(extension.decodes).isZero();
        assertHealthy(registry, json, extension);
    }

    private static void assertLocation(DynamicCodecException error) {
        assertThat(error.fieldPath()).isEqualTo("$.body.data");
        assertThat(error.bitOffset()).isEqualTo(-1L);
    }

    private static void assertHealthy(DynamicExtensionRegistry registry, boolean json, CountingExtension extension) {
        final int validations = extension.validations;
        final DynamicPlan plan = DynamicCompiler.compile(schema(Map.of("extensionId", "test"), json), registry);
        assertThat(extension.validations).isEqualTo(validations + 1);
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final ByteBuf input = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump("55aa7eab7f66")).readerIndex(2);
        try {
            assertThat(plan.encode(DynamicValues.of(Map.of("head", 126, "body", Map.of("data", 171), "tail", 127)), output)).isEqualTo(24L);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa7eab7f");
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(extension.encodes).isEqualTo(1);
            assertThat(extension.decodes).isZero();
            final DynamicDecodeResult result = plan.decode(input);
            assertThat(result.values().fields()).isEqualTo(Map.of("head", 126L, "body", Map.of("data", 171), "tail", 127L));
            assertThat(result.consumedBits()).isEqualTo(24L);
            assertThat(result.remainingBytes()).isEqualTo(1);
            assertThat(input.readerIndex()).isEqualTo(5);
            assertThat(input.getUnsignedByte(5)).isEqualTo((short) 0x66);
            assertThat(input.refCnt()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
            assertThat(extension.validations).isEqualTo(validations + 1);
            assertThat(extension.encodes).isEqualTo(1);
            assertThat(extension.decodes).isEqualTo(1);
        } finally {
            input.release();
            output.release();
        }
    }

    private static DynamicSchema schema(Map<String, Object> options, boolean json) {
        final DynamicSchema schema = DynamicSchema.of(DynamicField.field("head", "u8").build(),
                DynamicField.field("body", "struct").fields(DynamicField.field("data", "extension")
                        .length(DynamicLength.fixed(1, "byte")).options(options).build()).build(), DynamicField.field("tail", "u8").build());
        return json ? DynamicSchemaJson.read(DynamicSchemaJson.write(schema)) : schema;
    }

    private static class CountingExtension implements DynamicExtension {
        private final IllegalArgumentException failure = new IllegalArgumentException("测试扩展配置失败");
        private int validations;
        private int encodes;
        private int decodes;

        @Override
        public void validate(Map<String, Object> config) {
            validations++;
            if (Boolean.TRUE.equals(config.get("invalid"))) {
                throw failure;
            }
        }

        @Override
        public byte[] encode(Object value, Map<String, Object> config) {
            encodes++;
            return new byte[]{((Number) value).byteValue()};
        }

        @Override
        public Object decode(byte[] bytes, Map<String, Object> config) {
            decodes++;
            return Byte.toUnsignedInt(bytes[0]);
        }
    }
}
