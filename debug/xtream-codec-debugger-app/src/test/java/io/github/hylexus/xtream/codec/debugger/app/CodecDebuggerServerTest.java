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

package io.github.hylexus.xtream.codec.debugger.app;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.debugger.CodecDebuggerService;
import io.github.hylexus.xtream.codec.core.debugger.DebugEntityCatalog;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 独立 HTTP 路由与页面资源的集成测试。
 * 服务绑定临时本机端口，检查静态页面、版本化 API、请求上限和实体目录限制。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class CodecDebuggerServerTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void shouldServeVersionedApiAndProtocolAgnosticPage() throws Exception {
        final CodecDebuggerService service = new CodecDebuggerService(
                new DebugEntityCatalog(Map.of("sample", SampleMessage.class)));
        try (CodecDebuggerServer server = new CodecDebuggerServer(new InetSocketAddress("127.0.0.1", 0), service, 4096)) {
            server.start();
            final String base = "http://127.0.0.1:" + server.port();
            final var page = get(base + "/");
            assertEquals(200, page.statusCode());
            assertTrue(page.body().contains("二进制编解码工作台"));
            // 根据 HTML 中的引用检查 Vite 哈希资源，避免测试绑定某次构建的文件名。
            final var scriptMatcher = java.util.regex.Pattern.compile("src=\"(/assets/[^\"]+\\.js)\"").matcher(page.body());
            assertTrue(scriptMatcher.find());
            final var script = get(base + scriptMatcher.group(1));
            assertEquals(200, script.statusCode());
            assertTrue(script.headers().firstValue("Content-Type").orElseThrow().startsWith("text/javascript"));
            final var styleMatcher = java.util.regex.Pattern.compile("href=\"(/assets/[^\"]+\\.css)\"").matcher(page.body());
            assertTrue(styleMatcher.find());
            final var style = get(base + styleMatcher.group(1));
            assertEquals(200, style.statusCode());
            assertTrue(style.headers().firstValue("Content-Type").orElseThrow().startsWith("text/css"));
            assertEquals(404, get(base + "/assets/missing.js").statusCode());
            final var jsonSchema = get(base + "/dynamic-codec-schema.json");
            assertEquals(200, jsonSchema.statusCode());
            assertTrue(jsonSchema.headers().firstValue("Content-Type").orElseThrow().startsWith("application/schema+json"));
            assertTrue(mapper.readTree(jsonSchema.body()).path("$defs").path("field").path("properties").has("generated"));
            assertEquals(1, mapper.readTree(get(base + "/api/v1/entities").body()).size());
            final var template = get(base + "/api/v1/entities/sample/template");
            assertEquals(200, template.statusCode());
            assertTrue(mapper.readTree(template.body()).isObject());
            assertEquals(0, mapper.readTree(template.body()).get("sequence").asInt());

            final DynamicSchema schema = DynamicSchema.of(DynamicField.field("number", "u8").build());
            final String encode = mapper.writeValueAsString(Map.of("mode", "dynamic", "direction", "encode",
                    "schema", schema, "values", Map.of("number", 127)));
            final var encoded = post(base + "/api/v1/codec", encode);
            assertEquals(200, encoded.statusCode());
            assertEquals("7f", mapper.readTree(encoded.body()).get("hex").asText());
            final String decode = mapper.writeValueAsString(Map.of("mode", "dynamic", "direction", "decode",
                    "schema", schema, "hex", "7f01"));
            final var decoded = post(base + "/api/v1/codec", decode);
            assertEquals(200, decoded.statusCode());
            assertEquals("01", mapper.readTree(decoded.body()).get("remainingHex").asText());
            final String entityEncode = "{\"mode\":\"entity\",\"direction\":\"encode\",\"entityId\":\"sample\",\"values\":{\"sequence\":4660,\"flags\":5}}";
            final var encodedEntity = post(base + "/api/v1/codec", entityEncode);
            assertEquals(200, encodedEntity.statusCode());
            assertEquals("123405", mapper.readTree(encodedEntity.body()).get("hex").asText());
            final var decodedEntity = post(base + "/api/v1/codec",
                    "{\"mode\":\"entity\",\"direction\":\"decode\",\"entityId\":\"sample\",\"hex\":\"123405\"}");
            assertEquals(200, decodedEntity.statusCode());
            assertEquals(4660, mapper.readTree(decodedEntity.body()).get("values").get("sequence").asInt());
            assertEquals(404, get(base + "/api/v1/missing").statusCode());
        }
    }

    @Test
    void shouldLimitRequestAndRejectUnknownEntity() throws Exception {
        try (CodecDebuggerServer server = new CodecDebuggerServer(new InetSocketAddress("127.0.0.1", 0),
                new CodecDebuggerService(new DebugEntityCatalog(Map.of())), 256)) {
            server.start();
            final String base = "http://127.0.0.1:" + server.port();
            assertEquals(413, post(base + "/api/v1/codec", "x".repeat(257)).statusCode());
            final var invalidValues = post(base + "/api/v1/codec",
                    "{\"mode\":\"entity\",\"direction\":\"encode\",\"entityId\":\"sample\",\"values\":[1,2]}");
            assertEquals(400, invalidValues.statusCode());
            final var response = post(base + "/api/v1/codec",
                    "{\"mode\":\"entity\",\"direction\":\"decode\",\"entityId\":\"java.lang.Runtime\",\"hex\":\"\"}");
            assertEquals(422, response.statusCode());
            assertTrue(response.body().contains("未知"));
        }
    }

    @Test
    void shouldRejectInvalidOrMissingModeAndDirectionAsRequestErrors() throws Exception {
        try (final var server = new CodecDebuggerServer(new InetSocketAddress("127.0.0.1", 0),
                new CodecDebuggerService(new DebugEntityCatalog(Map.of())), 4096)) {
            server.start();
            final String url = "http://127.0.0.1:" + server.port() + "/api/v1/codec";
            for (final String request : List.of(
                    "{\"mode\":\"unknown\",\"direction\":\"encode\"}",
                    "{\"mode\":\"entity\",\"direction\":\"unknown\"}",
                    "{\"direction\":\"encode\"}",
                    "{\"mode\":\"entity\"}",
                    "{\"mode\":null,\"direction\":\"encode\"}",
                    "{\"mode\":\"entity\",\"direction\":null}",
                    "{\"mode\":1,\"direction\":\"encode\"}",
                    "{\"mode\":\"entity\",\"direction\":1}")) {
                final var response = post(url, request);
                assertEquals(400, response.statusCode(), request);
                assertTrue(mapper.readTree(response.body()).hasNonNull("error"), response.body());
            }
        }
    }

    @Test
    void defaultHttpApplicationMustRejectEngineExpressions() throws Exception {
        try (final var server = new CodecDebuggerServer(new InetSocketAddress("127.0.0.1", 0),
                new CodecDebuggerService(new DebugEntityCatalog(Map.of())), 4096)) {
            server.start();
            final String base = "http://127.0.0.1:" + server.port();
            final String request = """
                    {"mode":"dynamic","direction":"decode","hex":"","schema":{"schemaVersion":1,"fields":[
                    {"id":"result","type":"derived","expression":{"engine":"SpEL","source":"1 + 2","bindings":{}}}
                    ]}}
                    """;
            final var response = post(base + "/api/v1/codec", request);
            assertEquals(422, response.statusCode());
            assertEquals("$.result", mapper.readTree(response.body()).get("error").get("fieldPath").asText());
            assertEquals(404, post(base + "/api/v1/expression-engines", "{}").statusCode());
        }
    }

    private HttpResponse<String> get(String url) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String url, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
}
