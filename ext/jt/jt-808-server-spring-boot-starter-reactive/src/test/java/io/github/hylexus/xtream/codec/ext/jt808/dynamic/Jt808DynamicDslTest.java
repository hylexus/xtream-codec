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

package io.github.hylexus.xtream.codec.ext.jt808.dynamic;

import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicCodecException;
import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicCompiler;
import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicDecodeResult;
import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicDecodeOptions;
import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicPlan;
import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicValues;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchemaJson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 使用独立 JSON DSL 编解码完整的 JT/T 808 消息头、消息体和校验字节。
 * 样例来自 808 扩展现有测试及 Dashboard 示例；输入均已去掉两侧 7e，且不需要反转义。
 * 多版本样例通过 generated.checksum 在编码时自动生成、解码时核对校验码；其余样例使用普通校验字段。
 * 测试还独立确认已知报文的 XOR 值，避免只依赖同一计划的编解码往返结果。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class Jt808DynamicDslTest {
    private static final String MESSAGE_8003_2013 = "800300090139123443230000006f030002000300049d";
    private static final String MESSAGE_8003_2019 = "8003400a01000000000139123443290000006f0003000200030004d5";
    private static final String MESSAGE_0200_2019 = "02004026010000000001391234432900000000006f0000001601dc9a"
            + "0707456246007b004200032501292233440202006401040000000c41";
    private static final String MESSAGE_0704_2019 = "0704403f01000000000139123443290000000200001c0000000100000001"
            + "01d907f2073d336c029a00210059241029213903001c0000000100000001"
            + "01d907f3073d336d03090043005a241029213903f8";

    @Test
    void shouldEncode2013RetransmissionRequestFromValues() throws IOException {
        // 直接提供业务值，验证编码不依赖同一计划先执行解码。
        assertEncoded("8003-2013.json", MESSAGE_8003_2013, Map.of(
                "messageId", 0x8003,
                "bodyProps", 0x0009,
                "terminalId", "013912344323",
                "serialNo", 0,
                "body", Map.of(
                        "originalMessageFlowId", 111,
                        "packageCount", 3,
                        "packageIdList", List.of(2, 3, 4)),
                "checksum", 0x9d));
    }

    @Test
    void shouldEncode2019RetransmissionRequestFromValues() throws IOException {
        assertEncoded("8003-2019.json", MESSAGE_8003_2019, Map.of(
                "messageId", 0x8003,
                "bodyProps", 0x400a,
                "protocolVersion", 1,
                "terminalId", "00000000013912344329",
                "serialNo", 0,
                "body", Map.of(
                        "originalMessageFlowId", 111,
                        "packageCount", 3,
                        "packageIdList", List.of(2, 3, 4)),
                "checksum", 0xd5));
    }

    @Test
    void shouldEncodeLocationReportWithTlvFromValues() throws IOException {
        assertEncoded("0200-2019.json", MESSAGE_0200_2019, Map.of(
                "messageId", 0x0200,
                "bodyProps", 0x4026,
                "protocolVersion", 1,
                "terminalId", "00000000013912344329",
                "serialNo", 0,
                "body", Map.of(
                        "alarmFlag", 111,
                        "status", 22,
                        "latitude", 31234567,
                        "longitude", 121987654,
                        "altitude", 123,
                        "speed", 66,
                        "direction", 3,
                        "time", "250129223344",
                        "extraItems", List.of(
                                Map.of("key", 2, "value", 100),
                                Map.of("key", 1, "value", 12))),
                "checksum", 0x41));
    }

    @Test
    void shouldEncodeBatchLocationReportFromValues() throws IOException {
        assertEncoded("0704-2019.json", MESSAGE_0704_2019, Map.of(
                "messageId", 0x0704,
                "bodyProps", 0x403f,
                "protocolVersion", 1,
                "terminalId", "00000000013912344329",
                "serialNo", 0,
                "body", Map.of(
                        "count", 2,
                        "type", 0,
                        "itemList", List.of(
                                Map.of("locationData", Map.of(
                                        "alarmFlag", 1, "status", 1,
                                        "latitude", 31000562, "longitude", 121451372,
                                        "altitude", 666, "speed", 33, "direction", 89,
                                        "time", "241029213903")),
                                Map.of("locationData", Map.of(
                                        "alarmFlag", 1, "status", 1,
                                        "latitude", 31000563, "longitude", 121451373,
                                        "altitude", 777, "speed", 67, "direction", 90,
                                        "time", "241029213903")))),
                "checksum", 0xf8));
    }

    @Test
    void shouldDescribe2013RetransmissionRequestWithLegacyHeader() throws IOException {
        // 2013 版终端号占 6 字节，0x8003 的重传包数量占 1 字节。
        final DynamicDecodeResult result = roundTrip("8003-2013.json", MESSAGE_8003_2013, 0x8003);
        final Map<String, Object> values = result.values().fields();
        final Map<?, ?> body = body(result);

        assertEquals(9L, values.get("bodyLength"));
        assertEquals("013912344323", values.get("terminalId"));
        assertEquals(111L, body.get("originalMessageFlowId"));
        assertEquals(3L, body.get("packageCount"));
        assertEquals(List.of(2L, 3L, 4L), body.get("packageIdList"));
    }

    @Test
    void shouldDescribe2019RetransmissionRequestWithCountedArray() throws IOException {
        // 2019 版消息头多了协议版本号，终端号变为 10 字节，包数量变为 2 字节。
        final DynamicDecodeResult result = roundTrip("8003-2019.json", MESSAGE_8003_2019, 0x8003);
        final Map<String, Object> values = result.values().fields();
        final Map<?, ?> body = body(result);

        assertEquals(1L, values.get("protocolVersion"));
        assertEquals("00000000013912344329", values.get("terminalId"));
        assertEquals(10L, values.get("bodyLength"));
        assertEquals(111L, body.get("originalMessageFlowId"));
        assertEquals(3L, body.get("packageCount"));
        assertEquals(List.of(2L, 3L, 4L), body.get("packageIdList"));
    }

    @Test
    void shouldDescribeLocationReportWithKnownTlvItems() throws IOException {
        // 消息体前 28 字节为固定定位字段；剩余 10 字节为保留原始顺序的 TLV 附加信息。
        final DynamicDecodeResult result = roundTrip("0200-2019.json", MESSAGE_0200_2019, 0x0200);
        final Map<?, ?> body = body(result);

        assertEquals(38L, result.values().fields().get("bodyLength"));
        assertEquals(111L, body.get("alarmFlag"));
        assertEquals(22L, body.get("status"));
        assertEquals(31234567L, body.get("latitude"));
        assertEquals(121987654L, body.get("longitude"));
        assertEquals(123L, body.get("altitude"));
        assertEquals(66L, body.get("speed"));
        assertEquals(3L, body.get("direction"));
        assertEquals("250129223344", body.get("time"));
        assertEquals(
                List.of(
                        Map.of("key", 2L, "value", 100L),
                        Map.of("key", 1L, "value", 12L)
                ),
                body.get("extraItems")
        );
    }

    @Test
    void shouldDescribeBatchLocationReportWithLengthPrefixedItems() throws IOException {
        // 每条定位记录前的 u16 是该条记录的字节长度，不计入记录自身的 28 字节。
        final DynamicDecodeResult result = roundTrip("0704-2019.json", MESSAGE_0704_2019, 0x0704);
        final Map<?, ?> body = body(result);
        final List<?> items = (List<?>) body.get("itemList");
        final Map<?, ?> first = (Map<?, ?>) ((Map<?, ?>) items.get(0)).get("locationData");
        final Map<?, ?> second = (Map<?, ?>) ((Map<?, ?>) items.get(1)).get("locationData");

        assertEquals(63L, result.values().fields().get("bodyLength"));
        assertEquals(2L, body.get("count"));
        assertEquals(0L, body.get("type"));
        assertEquals(2, items.size());
        assertEquals(31000562L, first.get("latitude"));
        assertEquals(121451372L, first.get("longitude"));
        assertEquals(31000563L, second.get("latitude"));
        assertEquals(121451373L, second.get("longitude"));
        assertEquals("241029213903", first.get("time"));
        assertEquals("241029213903", second.get("time"));
    }

    @Test
    void shouldRejectHeaderBodyLengthThatExceedsTheActualBody() throws IOException {
        final DynamicPlan plan = loadPlan("8003-2019.json");
        // 把消息体属性从 0x400a 改成 0x400b，数组内容仍只有 10 字节。
        final ByteBuf input = Unpooled.wrappedBuffer(HexFormat.of().parseHex(
                MESSAGE_8003_2019.replaceFirst("400a", "400b")));
        try {
            final DynamicCodecException error = assertThrows(DynamicCodecException.class, () -> plan.decode(input));
            assertEquals("$.body", error.fieldPath());
            assertEquals(0, input.readerIndex());
        } finally {
            input.release();
        }
    }

    @Test
    void shouldReuseOneSchemaFor2013And2019WithoutRuntimeVersionMatching() throws IOException {
        // 相同终端号与包数量标识按外部协议版本选择布局，版本参数不写入报文。
        try (InputStream input = Jt808DynamicDslTest.class.getResourceAsStream("/dynamic/jt808/8003-multi-version.json")) {
            assertNotNull(input);
            final var schema = DynamicSchemaJson.read(new String(input.readAllBytes(), StandardCharsets.UTF_8));
            final DynamicPlan plan2013 = DynamicCompiler.compile(schema, 2013);
            final DynamicPlan plan2019 = DynamicCompiler.compile(schema, 2019);
            for (int i = 0; i < 3; i++) {
                assertEquals(9L, roundTrip(plan2013, MESSAGE_8003_2013, 0x8003).values().fields().get("bodyLength"));
                assertEquals(10L, roundTrip(plan2019, MESSAGE_8003_2019, 0x8003).values().fields().get("bodyLength"));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {2011, 2013, 2019})
    void shouldGenerateBodyLengthPackageCountAndChecksumFromBusinessValues(int version) throws IOException {
        final DynamicPlan plan = loadPlan("8003-multi-version.json", version);
        // 只提供业务值，长度、数量、版本标识和校验码由定义自动生成。
        final Map<String, Object> values = retransmissionValues(version);
        final String expectedHex = version == 2019 ? MESSAGE_8003_2019 : MESSAGE_8003_2013;
        assertEncoded(plan, expectedHex, values);
        final DynamicDecodeResult decoded = roundTrip(plan, expectedHex, 0x8003);
        assertEquals(version == 2019 ? 10L : 9L, decoded.values().fields().get("bodyLength"));
        assertEquals(version == 2019 ? 1L : 0L, decoded.values().fields().get("versionFlag"));
        assertEquals(0L, decoded.values().fields().get("reserved"));
        assertFalse(decoded.values().fields().containsKey("bodyProps"));
        assertEquals(3L, body(decoded).get("packageCount"));
        assertTrue(decoded.checksums().getFirst().matched());
    }

    @ParameterizedTest
    @ValueSource(ints = {2011, 2013, 2019})
    void shouldComposeBodyPropertiesFromDerivedInputs(int version) throws IOException {
        final DynamicPlan plan = loadPlan("8003-multi-version-derived.json", version);
        final Map<String, Object> values = retransmissionValues(version);
        values.put("bodyLength", version == 2019 ? 10 : 9);
        values.put("versionFlag", version == 2019 ? 1 : 0);
        // 不提供源整数 bodyProps；保留位也省略，由反向合成补零。
        final String expectedHex = version == 2019 ? MESSAGE_8003_2019 : MESSAGE_8003_2013;
        assertEncoded(plan, expectedHex, values);
        final DynamicDecodeResult decoded = roundTrip(plan, expectedHex, 0x8003);
        assertEquals(version == 2019 ? 0x400aL : 9L, decoded.values().fields().get("bodyProps"));
        assertEquals(version == 2019 ? 10L : 9L, decoded.values().fields().get("bodyLength"));
        assertEquals(3L, body(decoded).get("packageCount"));
        assertTrue(decoded.checksums().getFirst().matched());
    }

    @ParameterizedTest
    @ValueSource(ints = {2011, 2013, 2019})
    void shouldKeepPropertyBitsWhenEncodingSubpackages(int version) throws IOException {
        // 仅验证属性位与长度回填，消息体保持样例原始字节。
        final String expectedHex = version == 2019
                ? "8003740a0100000000013912344329000000020001006f0003000200030004e2"
                : "80033409013912344323000000020001006f03000200030004aa";
        for (final String filename : List.of("8003-multi-version.json", "8003-multi-version-derived.json")) {
            final DynamicPlan plan = loadPlan(filename, version);
            final Map<String, Object> values = retransmissionValues(version);
            values.put("hasSubpackage", 1);
            values.put("encryption", 5);
            values.put("subpackage", Map.of("total", 2, "index", 1));
            if (filename.equals("8003-multi-version-derived.json")) {
                values.put("bodyLength", version == 2019 ? 10 : 9);
                values.put("versionFlag", version == 2019 ? 1 : 0);
            }
            assertEncoded(plan, expectedHex, values);
            final DynamicDecodeResult decoded = roundTrip(plan, expectedHex, 0x8003);
            assertEquals(1L, decoded.values().fields().get("hasSubpackage"));
            assertEquals(5L, decoded.values().fields().get("encryption"));
            assertEquals(version == 2019 ? 10L : 9L, decoded.values().fields().get("bodyLength"));
            assertEquals(Map.of("total", 2L, "index", 1L), decoded.values().fields().get("subpackage"));
            assertTrue(decoded.checksums().getFirst().matched());
        }
    }

    @Test
    void shouldRejectMessageBodyLengthBeyondTenBits() throws IOException {
        // 2019 版消息体有 4 字节固定内容，每个包 ID 占 2 字节：509 项为 1022 字节，510 项为 1024 字节。
        final DynamicPlan plan = loadPlan("8003-multi-version.json", 2019);
        final Map<String, Object> values = retransmissionValues(2019);
        for (final int count : new int[]{509, 510}) {
            values.put("body", Map.of("originalMessageFlowId", 111, "packageIdList", Collections.nCopies(count, 2)));
            final ByteBuf output = Unpooled.buffer();
            try {
                if (count == 510) {
                    final DynamicCodecException error = assertThrows(DynamicCodecException.class,
                            () -> plan.encode(DynamicValues.of(values), output));
                    assertTrue(error.getMessage().contains("回填长度超出整数位宽"));
                } else {
                    plan.encode(DynamicValues.of(values), output);
                    assertEquals(0x43fe, output.getUnsignedShort(2));
                    final DynamicDecodeResult decoded = plan.decode(output, DynamicDecodeOptions.STRICT);
                    assertEquals(1022L, decoded.values().fields().get("bodyLength"));
                    assertEquals(1L, decoded.values().fields().get("versionFlag"));
                    assertEquals(509L, body(decoded).get("packageCount"));
                    assertTrue(decoded.checksums().getFirst().matched());
                }
            } finally {
                output.release();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {2011, 2013, 2019})
    void shouldRejectCorruptedHeaderBodyOrChecksum(int version) throws IOException {
        final DynamicPlan plan = loadPlan("8003-multi-version.json", version);
        final String hex = version == 2019 ? MESSAGE_8003_2019 : MESSAGE_8003_2013;
        final byte[] bytes = HexFormat.of().parseHex(hex);
        // 分别修改消息 ID、消息体中最后一个包 ID、校验字节；三处都应被同一校验范围发现。
        for (final int index : new int[]{0, bytes.length - 2, bytes.length - 1}) {
            final ByteBuf input = Unpooled.wrappedBuffer(bytes.clone());
            try {
                input.setByte(index, input.getUnsignedByte(index) ^ 1);
                final DynamicDecodeResult decoded = plan.decode(input);
                assertEquals(bytes.length, input.readerIndex());
                assertEquals(1, decoded.checksums().size());
                assertFalse(decoded.checksums().getFirst().matched());
                assertEquals("$.checksum", decoded.checksums().getFirst().fieldPath());
                assertEquals(decoded.values().fields().get("checksum"), decoded.checksums().getFirst().actual());
                input.readerIndex(0);
                final DynamicCodecException error = assertThrows(DynamicCodecException.class,
                        () -> plan.decode(input, DynamicDecodeOptions.STRICT));
                assertEquals("$.checksum", error.fieldPath());
                assertTrue(error.getMessage().contains("校验和不匹配"));
                assertEquals(0, input.readerIndex());
            } finally {
                input.release();
            }
        }
    }

    private static DynamicDecodeResult roundTrip(String schemaFile, String hex, int messageId) throws IOException {
        return roundTrip(loadPlan(schemaFile), hex, messageId);
    }

    private static DynamicDecodeResult roundTrip(DynamicPlan plan, String hex, int messageId) {
        final byte[] bytes = HexFormat.of().parseHex(hex);
        final ByteBuf input = Unpooled.wrappedBuffer(bytes);
        final ByteBuf output = Unpooled.buffer();
        try {
            // 同一份 DSL 完整消费消息头、消息体和末尾校验字节，再从字段值还原相同的原始字节。
            final DynamicDecodeResult decoded = plan.decode(input);
            assertEquals(bytes.length, decoded.consumedBytes());
            assertEquals(bytes.length * 8L, decoded.consumedBits());
            assertEquals(0, decoded.remainingBytes());
            assertEquals(bytes.length, input.readerIndex());
            assertEquals((long) messageId, decoded.values().fields().get("messageId"));
            assertEquals((long) Byte.toUnsignedInt(bytes[bytes.length - 1]), decoded.values().fields().get("checksum"));

            int xor = 0;
            for (int i = 0; i < bytes.length - 1; i++) {
                xor ^= Byte.toUnsignedInt(bytes[i]);
            }
            assertEquals(Byte.toUnsignedInt(bytes[bytes.length - 1]), xor);

            assertEquals(bytes.length * 8L, plan.encode(decoded.values(), output));
            assertEquals(hex, ByteBufUtil.hexDump(output));
            return decoded;
        } finally {
            input.release();
            output.release();
        }
    }

    private static void assertEncoded(String schemaFile, String expectedHex, Map<String, Object> values) throws IOException {
        assertEncoded(loadPlan(schemaFile), expectedHex, values);
    }

    private static void assertEncoded(DynamicPlan plan, String expectedHex, Map<String, Object> values) {
        final ByteBuf output = Unpooled.buffer();
        try {
            assertEquals(expectedHex.length() * 4L, plan.encode(DynamicValues.of(values), output));
            assertEquals(expectedHex, ByteBufUtil.hexDump(output));
        } finally {
            output.release();
        }
    }

    private static Map<?, ?> body(DynamicDecodeResult result) {
        return (Map<?, ?>) result.values().fields().get("body");
    }

    private static Map<String, Object> retransmissionValues(int version) {
        final Map<String, Object> values = new LinkedHashMap<>(Map.of(
                "messageId", 0x8003,
                "hasSubpackage", 0,
                "encryption", 0,
                "terminalId", version == 2019 ? "00000000013912344329" : "013912344323",
                "serialNo", 0,
                "body", Map.of("originalMessageFlowId", 111, "packageIdList", List.of(2, 3, 4))));
        if (version == 2019) {
            values.put("protocolVersion", 1);
        }
        return values;
    }

    private static DynamicPlan loadPlan(String filename) throws IOException {
        return loadPlan(filename, DynamicSchema.ALL_VERSION);
    }

    private static DynamicPlan loadPlan(String filename, int version) throws IOException {
        final String path = "/dynamic/jt808/" + filename;
        try (InputStream input = Jt808DynamicDslTest.class.getResourceAsStream(path)) {
            assertNotNull(input, "找不到 DSL 文件: " + path);
            return DynamicCompiler.compile(DynamicSchemaJson.read(new String(input.readAllBytes(), StandardCharsets.UTF_8)), version);
        }
    }
}
