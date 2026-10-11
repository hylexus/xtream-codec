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

package io.github.hylexus.xtream.codec.studio.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 在独立 JVM 中验证发布 JAR，不从项目 classpath 加载应用资源与运行依赖。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class CodecStudioFatJarTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

    @TempDir
    Path directory;

    @ParameterizedTest
    @ValueSource(strings = {"file", "sqlite"})
    void publishedJarShouldServeResourcesAndPersistDocumentsAcrossRestart(String storage) throws Exception {
        final Map<String, String> environment = Map.of("XTREAM_STUDIO_STORAGE", storage,
                "XTREAM_STUDIO_HOST", "0.0.0.0", "XTREAM_STUDIO_SQLITE_PATH", directory.resolve("custom.db").toString());
        final String id;
        try (var studio = launch(environment, List.of())) {
            final String base = studio.awaitAddress();
            final var page = call(base, "GET", "/", "");
            assertEquals(200, page.statusCode());
            assertTrue(page.body().contains("二进制编解码工作台"));
            // 根据 HTML 中的引用检查 Vite 哈希资源，避免测试绑定某次构建的文件名。
            final var scriptMatcher = Pattern.compile("src=\"(/assets/[^\"]+\\.js)\"").matcher(page.body());
            assertTrue(scriptMatcher.find(), page.body());
            final var script = call(base, "GET", scriptMatcher.group(1), "");
            assertEquals(200, script.statusCode());
            assertTrue(script.headers().firstValue("Content-Type").orElseThrow().startsWith("text/javascript"));
            final var styleMatcher = Pattern.compile("href=\"(/assets/[^\"]+\\.css)\"").matcher(page.body());
            assertTrue(styleMatcher.find(), page.body());
            final var style = call(base, "GET", styleMatcher.group(1), "");
            assertEquals(200, style.statusCode());
            assertTrue(style.headers().firstValue("Content-Type").orElseThrow().startsWith("text/css"));
            final var jsonSchema = call(base, "GET", "/dynamic-codec-schema.json", "");
            assertEquals(200, jsonSchema.statusCode());
            assertTrue(jsonSchema.headers().firstValue("Content-Type").orElseThrow().startsWith("application/schema+json"));
            assertTrue(mapper.readTree(jsonSchema.body()).path("$defs").path("field").path("properties").has("generated"));
            final var encoded = call(base, "POST", "/api/v1/codec", """
                    {"mode":"entity","direction":"encode","entityId":"sample","values":{"sequence":4660,"flags":5}}
                    """);
            assertEquals(200, encoded.statusCode(), encoded.body());
            assertEquals("123405", mapper.readTree(encoded.body()).path("hex").asText());
            final var decoded = call(base, "POST", "/api/v1/codec", """
                    {"mode":"entity","direction":"decode","entityId":"sample","hex":"123405"}
                    """);
            assertEquals(200, decoded.statusCode(), decoded.body());
            assertEquals(4660, mapper.readTree(decoded.body()).path("values").path("sequence").asInt());
            final var saved = call(base, "POST", "/api/v1/workspace/entries", """
                    {"parentId":null,"kind":"document","name":"发布验证","expectedRevision":0,"document":{
                      "formatVersion":1,"mode":"dynamic","schemaText":"{","schemaTab":"json","run":null,
                      "draft":{"schema":{"schemaVersion":1,"fields":[]},"values":"{}","hex":"1234","version":"","entityId":""}}}
                    """);
            assertEquals(201, saved.statusCode(), saved.body());
            id = mapper.readTree(saved.body()).path("id").asText();
        }
        try (var studio = launch(environment, List.of())) {
            final var restored = call(studio.awaitAddress(), "GET", "/api/v1/workspace/entries/" + id, "");
            assertEquals(200, restored.statusCode(), restored.body());
            assertEquals("1234", mapper.readTree(restored.body()).path("document").path("draft").path("hex").asText());
        }
        if ("sqlite".equals(storage)) {
            assertTrue(Files.isRegularFile(directory.resolve("custom.db")));
        }
    }

    @Test
    void propertiesAndLegacyArgumentsShouldOverrideEnvironmentAndSupportScanning() throws Exception {
        try (var studio = launch(Map.of("XTREAM_STUDIO_HOST", "invalid.invalid", "XTREAM_STUDIO_PORT", "invalid",
                "XTREAM_STUDIO_STORAGE", "unknown", "XTREAM_STUDIO_LOG_LEVEL", "WARN"), List.of(
                "-Dxtream.studio.host=127.0.0.1", "-Dxtream.studio.port=invalid", "-Dxtream.studio.storage=file",
                "-Dxtream.studio.log-level=INFO", "-Dxtream.studio.data-dir=" + directory.resolve("property-data")),
                "0", "io.github.hylexus.xtream.codec.studio.app")) {
            final var entities = call(studio.awaitAddress(), "GET", "/api/v1/entities", "");
            assertEquals(200, entities.statusCode());
            assertTrue(entities.body().contains("SampleMessage"), entities.body());
        }
        assertTrue(Files.isDirectory(directory.resolve("property-data")));
    }

    @Test
    void startupFailureShouldReleaseTheWorkspaceForRetry() throws Exception {
        try (var socket = new ServerSocket()) {
            socket.bind(new InetSocketAddress("127.0.0.1", 0));
            try (var studio = launch(Map.of("XTREAM_STUDIO_PORT", Integer.toString(socket.getLocalPort())), List.of())) {
                assertTrue(studio.process().waitFor(20, TimeUnit.SECONDS));
                assertNotEquals(0, studio.process().exitValue());
                assertTrue(studio.output().contains("BindException"), studio.output());
            }
        }
        try (var studio = launch(Map.of(), List.of())) {
            assertEquals(200, call(studio.awaitAddress(), "GET", "/api/v1/workspace", "").statusCode());
        }
    }

    @Test
    void warnLoggingShouldSuppressStartupWhileRetainingStartupErrors() throws Exception {
        final int port;
        try (var socket = new ServerSocket()) {
            socket.bind(new InetSocketAddress("127.0.0.1", 0));
            port = socket.getLocalPort();
        }
        try (var studio = launch(Map.of("XTREAM_STUDIO_PORT", Integer.toString(port), "XTREAM_STUDIO_LOG_LEVEL", "WARN"), List.of())) {
            final String base = "http://127.0.0.1:" + port;
            final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
            while (true) {
                assertTrue(studio.process().isAlive(), studio.output());
                try {
                    assertEquals(200, call(base, "GET", "/api/v1/entities", "").statusCode());
                    break;
                } catch (java.io.IOException exception) {
                    assertTrue(System.nanoTime() < deadline, studio.output());
                    Thread.sleep(50);
                }
            }
            assertFalse(studio.output().contains("Xtream Codec Studio:"), studio.output());
        }
        try (var studio = launch(Map.of("XTREAM_STUDIO_PORT", "65536", "XTREAM_STUDIO_LOG_LEVEL", "WARN"), List.of())) {
            assertTrue(studio.process().waitFor(20, TimeUnit.SECONDS));
            assertNotEquals(0, studio.process().exitValue());
            assertTrue(studio.output().contains("ERROR"), studio.output());
            assertTrue(studio.output().contains("xtream.studio.port"), studio.output());
        }
    }

    private StudioProcess launch(Map<String, String> environment, List<String> jvmArguments, String... arguments) throws Exception {
        final var command = new ArrayList<String>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        command.addAll(jvmArguments);
        command.addAll(List.of("-jar", System.getProperty("xtream.studio.test.jar")));
        command.addAll(List.of(arguments));
        final Path log = Files.createTempFile(directory, "studio-", ".log");
        final ProcessBuilder builder = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile());
        builder.environment().keySet().removeIf(key -> key.startsWith("XTREAM_STUDIO_")
                || List.of("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS").contains(key));
        builder.environment().putAll(Map.of("XTREAM_STUDIO_PORT", "0", "XTREAM_STUDIO_HOST", "127.0.0.1",
                "XTREAM_STUDIO_DATA_DIR", directory.resolve("data").toString(), "XTREAM_STUDIO_STORAGE", "file",
                "XTREAM_STUDIO_LOG_LEVEL", "INFO", "XTREAM_STUDIO_SHUTDOWN_GRACE_SECONDS", "1"));
        builder.environment().putAll(environment);
        return new StudioProcess(builder.start(), log);
    }

    private HttpResponse<String> call(String base, String method, String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base + path)).timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json").method(method, body.isEmpty()
                        ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private record StudioProcess(Process process, Path log) implements AutoCloseable {
        String output() throws java.io.IOException {
            return Files.readString(log);
        }

        String awaitAddress() throws Exception {
            final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
            while (System.nanoTime() < deadline) {
                final String output = output();
                assertTrue(process.isAlive(), output);
                final var address = Pattern.compile("Xtream Codec Studio: http://[^\\s]+:(\\d+)").matcher(output);
                if (address.find()) {
                    return "http://127.0.0.1:" + address.group(1);
                }
                Thread.sleep(50);
            }
            throw new AssertionError("发布 JAR 启动超时: " + output());
        }

        @Override
        public void close() throws Exception {
            process.destroy();
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
                fail("发布 JAR 未按时停止: " + output());
            }
        }
    }
}
