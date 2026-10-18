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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hylexus.xtream.codec.base.annotation.ReferencedByDocs;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchemaJson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 动态 DSL 文档的完整报文示例，使用文档直接导入的 JSON 资源验证独立给出的线上字节。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
@ReferencedByDocs({"guide/core/dynamic/quick-start.md", "guide/core/dynamic/examples.md"})
class DynamicDocumentationTest {
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(DeserializationFeature.USE_LONG_FOR_INTS);

    @Test
    void shouldEncodeAndDecodeTutorial() throws IOException {
        assertExample(plan("quick-start", DynamicSchema.ALL_VERSION), """
                {"version":5,"flags":17,"payload":{"count":2,"items":[
                  {"code":1,"value":4660},{"code":2,"value":22136}]}}
                """, "b10702011234025678", """
                {"version":5,"flags":17,"length":7,"payload":{"count":2,"items":[
                  {"code":1,"value":4660},{"code":2,"value":22136}]}}
                """);
    }

    @Test
    void shouldEncodeAndDecodeText() throws IOException {
        final String json = """
                {"sequence":4660,"name":"中","terminal":"001234","raw":"aabb"}
                """;
        assertExample(plan("text", DynamicSchema.ALL_VERSION), json, "3412d6d00000000000123402aabb", json);
    }

    @Test
    void shouldBoundEachVariableRecord() throws IOException {
        assertExample(plan("records", DynamicSchema.ALL_VERSION), """
                {"payload":{"records":[{"code":1,"text":"A"},{"code":2,"text":"BC"}]}}
                """, "000a020002014100030242437e", """
                {"payloadLength":10,"payload":{"count":2,"records":[
                  {"code":1,"text":"A"},{"code":2,"text":"BC"}]},"tail":126}
                """);
    }

    @Test
    void shouldUseSelectedAndDefaultBranches() throws IOException {
        final DynamicPlan plan = plan("choice", DynamicSchema.ALL_VERSION);
        final String temperature = """
                {"flags":4,"kind":1,"extra":4660,"body":{"temperature":235}}
                """;
        assertExample(plan, temperature, "0401123400eb", temperature);
        final String text = """
                {"flags":0,"kind":2,"body":"OK"}
                """;
        assertExample(plan, text, "0002024f4b", text);
        final String unknown = """
                {"flags":0,"kind":99,"body":"aabb"}
                """;
        assertExample(plan, unknown, "0063aabb", unknown);
    }

    @Test
    void shouldKeepDuplicateAndUnknownTlvEntries() throws IOException {
        final String json = """
                {"extras":[{"key":1,"value":4660},{"key":99,"rawHex":"aabb"},
                  {"key":1,"value":7},{"key":2,"value":{"note":"A"}}]}
                """;
        assertExample(plan("tlv", DynamicSchema.ALL_VERSION), json, "04010212346302aabb01020007020141", json);
    }

    @Test
    void shouldRejectUnknownPairKey() throws IOException {
        final DynamicPlan pair = DynamicCompiler.compile(DynamicSchemaJson.read("""
                {"schemaVersion":1,"fields":[{"id":"items","type":"pair",
                  "length":{"fixed":1,"unit":"item"},"variants":{}}]}
                """));
        final ByteBuf input = Unpooled.wrappedBuffer(new byte[]{99, 0});
        try {
            assertThrows(DynamicCodecException.class, () -> pair.decode(input));
            assertEquals(0, input.readerIndex());
        } finally {
            input.release();
        }
    }

    @Test
    void shouldComposeBitViewsAndAlign() throws IOException {
        assertExample(plan("bits", DynamicSchema.ALL_VERSION), """
                {"low":17,"high":5,"sequence":"101"}
                """, "b1a0", """
                {"source":177,"low":17,"high":5,"sequence":"101","align":null}
                """);
    }

    @Test
    void shouldGenerateWireValuesAfterLengthBackfill() throws IOException {
        final DynamicDecodeResult result = assertExample(plan("generated", DynamicSchema.ALL_VERSION), """
                {"items":[16,32]}
                """, "7e0200021020307e", """
                {"start":126,"count":2,"length":2,"items":[16,32],"checksum":48,"end":126}
                """);
        assertEquals(1, result.checksums().size());
        assertTrue(result.checksums().getFirst().matched());
    }

    @Test
    void shouldReportOrRejectDamagedChecksum() throws IOException {
        final DynamicPlan plan = plan("generated", DynamicSchema.ALL_VERSION);
        final ByteBuf input = Unpooled.wrappedBuffer(HexFormat.of().parseHex("7e0200021020317e"));
        try {
            final DynamicDecodeResult result = plan.decode(input);
            assertEquals(49L, result.values().fields().get("checksum"));
            assertEquals(49L, result.checksums().getFirst().actual());
            assertEquals(48L, result.checksums().getFirst().expected());
            assertFalse(result.checksums().getFirst().matched());
            assertEquals(0, input.readableBytes());
            input.readerIndex(0);
            assertThrows(DynamicCodecException.class, () -> plan.decode(input, DynamicDecodeOptions.STRICT));
            assertEquals(0, input.readerIndex());
        } finally {
            input.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"2013,03000200030004", "2019,0003000200030004"})
    void shouldSelectProtocolVersion(int version, String hex) throws IOException {
        assertExample(plan("versions", version), """
                {"items":[2,3,4]}
                """, hex, """
                {"count":3,"items":[2,3,4]}
                """);
    }

    private static DynamicPlan plan(String name, int version) throws IOException {
        try (InputStream input = DynamicDocumentationTest.class.getResourceAsStream("/dynamic/docs/" + name + ".json")) {
            assertNotNull(input);
            return DynamicCompiler.compile(DynamicSchemaJson.read(new String(input.readAllBytes(), StandardCharsets.UTF_8)), version);
        }
    }

    private static Map<String, Object> values(String json) throws IOException {
        return MAPPER.readValue(json, new TypeReference<>() {
        });
    }

    private static DynamicDecodeResult assertExample(DynamicPlan plan, String input, String hex, String decoded) throws IOException {
        final ByteBuf output = Unpooled.buffer();
        // 使用独立给出的 HEX 解码，避免仅靠编码与解码相互验证。
        final ByteBuf knownInput = Unpooled.wrappedBuffer(HexFormat.of().parseHex(hex));
        try {
            assertEquals((long) hex.length() * 4, plan.encode(DynamicValues.of(values(input)), output));
            assertEquals(hex, ByteBufUtil.hexDump(output));
            final DynamicDecodeResult result = plan.decode(knownInput);
            assertEquals(values(decoded), result.values().fields());
            assertEquals((long) hex.length() * 4, result.consumedBits());
            assertEquals(hex.length() / 2, result.consumedBytes());
            assertEquals(0, result.remainingBytes());
            assertEquals(0, knownInput.readableBytes());
            return result;
        } finally {
            output.release();
            knownInput.release();
        }
    }
}
