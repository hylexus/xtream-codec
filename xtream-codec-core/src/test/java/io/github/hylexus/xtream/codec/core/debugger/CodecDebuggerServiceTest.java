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

package io.github.hylexus.xtream.codec.core.debugger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicChecksum;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicChecksumAlgorithm;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicGeneratedValue;
import io.github.hylexus.xtream.codec.core.type.Preset;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 服务目录、四个编解码方向和失败 Trace 测试。
 * 不启动 HTTP 容器即可验证通用服务合同，保证业务能力能移植到其他宿主。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class CodecDebuggerServiceTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final CodecDebuggerService service = new CodecDebuggerService(
            new DebugEntityCatalog(Map.of("sample", Sample.class)));

    @Test
    void shouldRoundTripEnumRequestsWithExistingLowercaseJsonNames() throws Exception {
        final var modes = Map.of(DebugCodecMode.ENTITY, "entity", DebugCodecMode.DYNAMIC, "dynamic");
        final var directions = Map.of(DebugCodecDirection.ENCODE, "encode", DebugCodecDirection.DECODE, "decode");
        for (final var mode : modes.entrySet()) {
            for (final var direction : directions.entrySet()) {
                final var request = new DebugCodecRequest(mode.getKey(), direction.getKey(), null, null, null, null, null);
                final String json = mapper.writeValueAsString(request);
                final var tree = mapper.readTree(json);
                assertEquals(mode.getValue(), tree.get("mode").asText());
                assertEquals(direction.getValue(), tree.get("direction").asText());
                assertEquals(request, mapper.readValue(json, DebugCodecRequest.class));
            }
        }
    }

    @Test
    void shouldRequireExplicitExpressionEngineAndPreserveVersionAndTrace() {
        final var registry = new io.github.hylexus.xtream.codec.base.expression.XtreamExpressionEngineRegistry(false);
        registry.register(new io.github.hylexus.xtream.codec.base.expression.SpelXtreamExpressionEngine());
        final var configuration = new io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicCompileOptions(
                2013, io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtensionRegistry.EMPTY, registry);
        final var enabled = new CodecDebuggerService(new DebugEntityCatalog(Map.of()), new Jackson2DebugEntityValueMapper(mapper), configuration,
                io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicLimits.DEFAULT);
        final var schema = DynamicSchema.of(
                DynamicField.field("number", "u8").build(),
                DynamicField.field("number", "u16").versions(2019).build(),
                DynamicField.field("next", "derived").expression(
                        io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicExpression.engine(
                                "SpEL", "#n + 1", Map.of("n", "number"))).build());
        final var request = new DebugCodecRequest(DebugCodecMode.DYNAMIC, DebugCodecDirection.ENCODE, null, 2019, schema,
                Map.of("number", 17), null);
        assertFalse(service.execute(request).success());
        final var encoded = enabled.execute(request);
        assertTrue(encoded.success(), String.valueOf(encoded.error()));
        assertEquals("0011", encoded.hex());
        assertEquals(18L, encoded.values().get("next"));
        final var decoded = enabled.execute(new DebugCodecRequest(DebugCodecMode.DYNAMIC, DebugCodecDirection.DECODE, null, 2019, schema, null, encoded.hex()));
        assertEquals(encoded.values(), decoded.values());
        final var failing = DynamicSchema.of(DynamicField.field("prefix", "u8").build(),
                DynamicField.field("bad", "derived").expression(
                        io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicExpression.engine("SpEL", "1 / 0", Map.of())).build());
        final var failed = enabled.execute(new DebugCodecRequest(DebugCodecMode.DYNAMIC, DebugCodecDirection.DECODE, null, null, failing, null, "01"));
        assertFalse(failed.success());
        assertEquals("$.bad", failed.error().fieldPath());
        assertEquals(8L, failed.error().bitOffset());
        assertFalse(failed.trace().diagnostics().isEmpty());
    }

    @Test
    void shouldEncodeAndDecodeRegisteredEntityWithoutHttp() {
        final var encoded = service.execute(new DebugCodecRequest(DebugCodecMode.ENTITY, DebugCodecDirection.ENCODE, "sample", null, null,
                Map.of("number", 0x1234), null));
        assertTrue(encoded.success(), String.valueOf(encoded.error()));
        assertEquals("1234", encoded.hex());
        final var decoded = service.execute(new DebugCodecRequest(DebugCodecMode.ENTITY, DebugCodecDirection.DECODE, "sample", null, null, null, "123455"));
        assertTrue(decoded.success(), String.valueOf(decoded.error()));
        assertEquals(0x1234, decoded.values().get("number"));
        assertEquals(16, decoded.consumedBits());
        assertEquals("55", decoded.remainingHex());
        assertFalse(decoded.trace().nodes().isEmpty());
    }

    @Test
    void shouldEncodeAndDecodeDynamicStructureAndKeepPartialTrace() {
        final DynamicSchema schema = DynamicSchema.of(
                DynamicField.field("first", "u8").build(),
                DynamicField.field("second", "u16").build());
        final var encoded = service.execute(new DebugCodecRequest(DebugCodecMode.DYNAMIC, DebugCodecDirection.ENCODE, null, null, schema,
                Map.of("first", 1, "second", 0x1234), null));
        assertTrue(encoded.success());
        assertEquals("011234", encoded.hex());
        final var decoded = service.execute(new DebugCodecRequest(DebugCodecMode.DYNAMIC, DebugCodecDirection.DECODE, null, null, schema, null, encoded.hex()));
        assertTrue(decoded.success());
        assertEquals(0x1234L, decoded.values().get("second"));
        final var failed = service.execute(new DebugCodecRequest(DebugCodecMode.DYNAMIC, DebugCodecDirection.DECODE, null, null, schema, null, "0112"));
        assertFalse(failed.success());
        assertEquals("$.second", failed.error().fieldPath());
        assertEquals(8L, failed.error().bitOffset());
        assertEquals(1L, failed.trace().root().children().getFirst().value());
    }

    @Test
    void shouldRejectUnknownEntitiesAndLimitScanning() {
        final var failed = service.execute(new DebugCodecRequest(DebugCodecMode.ENTITY, DebugCodecDirection.DECODE, "java.lang.Runtime", null, null, null, ""));
        assertFalse(failed.success());
        assertTrue(failed.error().message().contains("未知"));
        assertThrows(IllegalArgumentException.class, () -> DebugEntityCatalog.scan());
        assertThrows(IllegalArgumentException.class, () -> DebugEntityCatalog.scan(""));
        final var catalog = DebugEntityCatalog.scan("io.github.hylexus.xtream.codec.core.debugger");
        assertTrue(catalog.entries().stream().anyMatch(entry -> entry.className().equals(Sample.class.getName())));
        assertTrue(catalog.entries().stream().allMatch(entry -> entry.className().startsWith("io.github.hylexus.xtream.codec.core.debugger.")));
    }

    @Test
    void shouldUseRequestedVersionInDynamicMode() {
        // 相同字段标识使用不同宽度；验证请求中的 version 同时影响编码和解码。
        final DynamicSchema schema = DynamicSchema.of(
                DynamicField.field("number", "u8").build(),
                DynamicField.field("number", "u16").versions(2019).build());
        for (final Integer version : new Integer[]{null, 2013, 2019}) {
            final var encoded = service.execute(new DebugCodecRequest(DebugCodecMode.DYNAMIC, DebugCodecDirection.ENCODE, null, version, schema,
                    Map.of("number", 17), null));
            assertTrue(encoded.success(), String.valueOf(encoded.error()));
            assertEquals(Integer.valueOf(2019).equals(version) ? "0011" : "11", encoded.hex());
            final var decoded = service.execute(new DebugCodecRequest(DebugCodecMode.DYNAMIC, DebugCodecDirection.DECODE, null, version, schema, null, encoded.hex()));
            assertTrue(decoded.success(), String.valueOf(decoded.error()));
            assertEquals(17L, decoded.values().get("number"));
            assertEquals("", decoded.remainingHex());
        }
    }

    @Test
    void shouldRoundTripBitFieldsComputedLengthAndNestedItems() {
        final DynamicSchema schema = DynamicSchema.of(
                DynamicField.field("version", "u3").build(),
                DynamicField.field("flags", "u5").build(),
                DynamicField.field("length", "u8").computedLengthOf("payload").build(),
                DynamicField.field("payload", "struct").length(DynamicLength.ref("length", "byte")).fields(
                        DynamicField.field("count", "u8").build(),
                        DynamicField.field("items", "array").length(DynamicLength.ref("count", "item"))
                                .item(DynamicField.field("item", "struct").fields(
                                        DynamicField.field("code", "u8").build(),
                                        DynamicField.field("value", "u16").build()).build()).build()).build());
        final Map<String, Object> input = Map.of("version", 5, "flags", 17,
                "payload", Map.of("count", 2, "items", List.of(
                        Map.of("code", 1, "value", 0x1234), Map.of("code", 2, "value", 0x5678))));
        final var encoded = service.execute(new DebugCodecRequest(DebugCodecMode.DYNAMIC, DebugCodecDirection.ENCODE, null, null, schema, input, null));
        assertTrue(encoded.success(), String.valueOf(encoded.error()));
        assertEquals("b10702011234025678", encoded.hex());
        assertEquals(7L, encoded.values().get("length"));
        assertEquals(7L, encoded.trace().root().children().get(2).value());
        final var decoded = service.execute(new DebugCodecRequest(DebugCodecMode.DYNAMIC, DebugCodecDirection.DECODE, null, null, schema, null, encoded.hex()));
        assertTrue(decoded.success(), String.valueOf(decoded.error()));
        assertEquals(encoded.values(), decoded.values());
        assertEquals(0L, decoded.trace().root().children().getFirst().bitStart());
    }

    @Test
    void shouldReturnCorruptedMessageWithChecksumResultsAndWarnings() {
        final DynamicSchema schema = DynamicSchema.of(
                DynamicField.field("body", "u8").build(),
                DynamicField.field("check", "u8").generated(DynamicGeneratedValue.checksum(
                        new DynamicChecksum(DynamicChecksumAlgorithm.XOR8, "body"))).build());
        final var decoded = service.execute(new DebugCodecRequest(DebugCodecMode.DYNAMIC, DebugCodecDirection.DECODE, null, null, schema, null, "030155"));
        assertTrue(decoded.success());
        assertNull(decoded.error());
        assertEquals(3L, decoded.values().get("body"));
        assertEquals(1L, decoded.values().get("check"));
        assertEquals("55", decoded.remainingHex());
        assertEquals(1, decoded.checksums().size());
        assertFalse(decoded.checksums().getFirst().matched());
        assertEquals(3L, decoded.checksums().getFirst().expected());
        assertEquals("WARNING", decoded.trace().diagnostics().getFirst().level());
        // HTTP JSON 中也必须包含核对结果和匹配状态，页面不必依赖异常或解析警告文本。
        final var json = mapper.valueToTree(decoded);
        assertFalse(json.get("checksums").get(0).get("matched").asBoolean());
        assertEquals("$.check", json.get("checksums").get(0).get("fieldPath").asText());
        final var encoded = service.execute(new DebugCodecRequest(DebugCodecMode.DYNAMIC, DebugCodecDirection.ENCODE, null, null, schema,
                Map.of("body", 3), null));
        assertTrue(encoded.success());
        assertEquals("0303", encoded.hex());
        assertTrue(encoded.checksums().getFirst().matched());
    }

    @Test
    void shouldPreserveNestedValuesNullAndLargeIntegersFromJson() throws Exception {
        final String json = """
                {"mode":"dynamic","direction":"encode","schema":{"schemaVersion":1,"fields":[
                  {"id":"number","type":"u64"},
                  {"id":"payload","type":"struct","fields":[
                    {"id":"items","type":"array","length":{"fixed":2,"unit":"item"},"item":{"id":"item","type":"u8"}},
                    {"id":"padding","type":"padding","length":{"fixed":1,"unit":"byte"}}
                  ]}
                ]},"values":{"number":18446744073709551615,"payload":{"items":[1,2],"padding":null}}}
                """;
        final var request = mapper.readValue(json, DebugCodecRequest.class);
        assertEquals(new BigInteger("18446744073709551615"), request.values().get("number"));
        final var inputPayload = assertInstanceOf(Map.class, request.values().get("payload"));
        assertEquals(List.of(1, 2), inputPayload.get("items"));
        assertTrue(inputPayload.containsKey("padding"));
        assertNull(inputPayload.get("padding"));
        final var encoded = service.execute(request);
        assertTrue(encoded.success(), String.valueOf(encoded.error()));
        assertEquals("ffffffffffffffff010200", encoded.hex());
        // 动态输出保留 JSON 安全整数规则；Map 不会让 HTTP 客户端丢失大整数精度。
        assertEquals("18446744073709551615", encoded.values().get("number"));
        final var outputPayload = assertInstanceOf(Map.class, encoded.values().get("payload"));
        assertEquals(List.of(1L, 2L), outputPayload.get("items"));
        assertTrue(outputPayload.containsKey("padding"));
        assertNull(outputPayload.get("padding"));
        final var decoded = service.execute(new DebugCodecRequest(DebugCodecMode.DYNAMIC, DebugCodecDirection.DECODE, null, null,
                request.schema(), null, encoded.hex()));
        assertTrue(decoded.success(), String.valueOf(decoded.error()));
        assertEquals(encoded.values(), decoded.values());
    }

    @Test
    void shouldUseHostObjectMapperForEntityTemplatesAndBothDirections() {
        final var hostMapper = mapper.copy().addMixIn(Sample.class, SampleMixin.class);
        final var configured = new CodecDebuggerService(new DebugEntityCatalog(Map.of("sample", Sample.class)),
                new Jackson2DebugEntityValueMapper(hostMapper), io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicCompileOptions.DEFAULT,
                io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicLimits.DEFAULT);
        assertEquals(Map.of("number", 0), service.entityTemplate("sample"));
        assertEquals(Map.of("wireNumber", 0), configured.entityTemplate("sample"));
        final var encoded = configured.execute(new DebugCodecRequest(DebugCodecMode.ENTITY, DebugCodecDirection.ENCODE, "sample", null, null,
                Map.of("wireNumber", 0x1234), null));
        assertTrue(encoded.success(), String.valueOf(encoded.error()));
        assertEquals("1234", encoded.hex());
        assertEquals(Map.of("wireNumber", 0x1234), encoded.values());
        final var decoded = configured.execute(new DebugCodecRequest(DebugCodecMode.ENTITY, DebugCodecDirection.DECODE, "sample", null, null, null, encoded.hex()));
        assertTrue(decoded.success(), String.valueOf(decoded.error()));
        assertEquals(encoded.values(), decoded.values());
    }

    @Test
    void shouldUseCustomValueMapperWithoutJacksonForTemplatesAndBothDirections() {
        final DebugEntityValueMapper valueMapper = new DebugEntityValueMapper() {
            @Override
            public <T> T toEntity(Map<String, Object> values, Class<T> entityType) {
                final var sample = new Sample();
                sample.setNumber(assertInstanceOf(Number.class, values.get("wireNumber")).intValue());
                return entityType.cast(sample);
            }

            @Override
            public Map<String, Object> toValues(Object entity) {
                return Map.of("wireNumber", assertInstanceOf(Sample.class, entity).getNumber());
            }
        };
        final var configured = new CodecDebuggerService(new DebugEntityCatalog(Map.of("sample", Sample.class)),
                valueMapper, io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicCompileOptions.DEFAULT,
                io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicLimits.DEFAULT);
        assertEquals(Map.of("wireNumber", 0), configured.entityTemplate("sample"));
        final var encoded = configured.execute(new DebugCodecRequest(DebugCodecMode.ENTITY, DebugCodecDirection.ENCODE,
                "sample", null, null, Map.of("wireNumber", 0x1234), null));
        assertTrue(encoded.success(), String.valueOf(encoded.error()));
        assertEquals("1234", encoded.hex());
        assertEquals(Map.of("wireNumber", 0x1234), encoded.values());
        final var decoded = configured.execute(new DebugCodecRequest(DebugCodecMode.ENTITY, DebugCodecDirection.DECODE,
                "sample", null, null, null, "123455"));
        assertTrue(decoded.success(), String.valueOf(decoded.error()));
        assertEquals(encoded.values(), decoded.values());
        assertEquals(16, decoded.consumedBits());
        assertEquals("55", decoded.remainingHex());
    }

    @Test
    void shouldReportConversionFailuresWithoutAffectingDynamicMode() {
        final DebugEntityValueMapper rejecting = new DebugEntityValueMapper() {
            @Override
            public <T> T toEntity(Map<String, Object> values, Class<T> entityType) {
                throw new IllegalArgumentException("输入转换失败");
            }

            @Override
            public Map<String, Object> toValues(Object entity) {
                throw new IllegalArgumentException("输出转换失败");
            }
        };
        final var configured = new CodecDebuggerService(new DebugEntityCatalog(Map.of("sample", Sample.class)),
                rejecting, io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicCompileOptions.DEFAULT,
                io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicLimits.DEFAULT);
        final var templateError = assertThrows(IllegalArgumentException.class, () -> configured.entityTemplate("sample"));
        assertEquals("输出转换失败", templateError.getMessage());
        final var failedEncode = configured.execute(new DebugCodecRequest(DebugCodecMode.ENTITY, DebugCodecDirection.ENCODE,
                "sample", null, null, Map.of("number", 0x1234), null));
        assertFalse(failedEncode.success());
        assertEquals("输入转换失败", assertInstanceOf(DebugCodecResponse.Error.class, failedEncode.error()).message());
        assertEquals("", failedEncode.hex());
        assertEquals(0, failedEncode.consumedBits());
        final var failedDecode = configured.execute(new DebugCodecRequest(DebugCodecMode.ENTITY, DebugCodecDirection.DECODE,
                "sample", null, null, null, "123455"));
        assertFalse(failedDecode.success());
        assertEquals("输出转换失败", assertInstanceOf(DebugCodecResponse.Error.class, failedDecode.error()).message());
        assertEquals(16, failedDecode.consumedBits());
        assertEquals("55", failedDecode.remainingHex());
        assertFalse(failedDecode.trace().nodes().isEmpty());
        final var schema = DynamicSchema.of(DynamicField.field("number", "u8").build());
        final var dynamicEncode = configured.execute(new DebugCodecRequest(DebugCodecMode.DYNAMIC, DebugCodecDirection.ENCODE,
                null, null, schema, Map.of("number", 127), null));
        assertTrue(dynamicEncode.success(), String.valueOf(dynamicEncode.error()));
        assertEquals("7f", dynamicEncode.hex());
        final var dynamicDecode = configured.execute(new DebugCodecRequest(DebugCodecMode.DYNAMIC, DebugCodecDirection.DECODE,
                null, null, schema, null, dynamicEncode.hex()));
        assertTrue(dynamicDecode.success(), String.valueOf(dynamicDecode.error()));
        assertEquals(dynamicEncode.values(), dynamicDecode.values());
    }

    abstract static class SampleMixin {
        @JsonProperty("wireNumber")
        abstract int getNumber();
    }

    public static class Sample {
        @Preset.RustStyle.u16(order = 0)
        private int number;

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }
}
