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
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.hylexus.xtream.codec.core.debugger.CodecDebuggerService;
import io.github.hylexus.xtream.codec.core.debugger.DebugCodecRequest;
import io.github.hylexus.xtream.codec.studio.workspace.WorkspaceData;
import io.github.hylexus.xtream.codec.studio.workspace.WorkspaceException;
import io.github.hylexus.xtream.codec.studio.workspace.WorkspaceService;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Studio 的轻量 HTTP 适配器，默认由运行入口绑定本机地址。
 * 负责版本化 API、请求大小限制和静态资源，编解码与工作区操作分别委托给对应服务。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public final class CodecStudioServer implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(CodecStudioServer.class);
    private final HttpServer server;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final CodecDebuggerService service;
    private final ObjectMapper mapper = new ObjectMapper();
    private final int maxRequestBytes;
    private final int shutdownGraceSeconds;
    private final @Nullable WorkspaceService workspace;

    public CodecStudioServer(InetSocketAddress address, CodecDebuggerService service, int maxRequestBytes) throws IOException {
        this(address, service, maxRequestBytes, null);
    }

    public CodecStudioServer(InetSocketAddress address, CodecDebuggerService service, int maxRequestBytes,
                             @Nullable WorkspaceService workspace) throws IOException {
        this(address, service, maxRequestBytes, workspace, 0);
    }

    /**
     * 创建可配置 HTTP 停机等待时间的服务，工作区生命周期由调用方管理。
     *
     * @since 0.11.0
     */
    public CodecStudioServer(InetSocketAddress address, CodecDebuggerService service, int maxRequestBytes,
                             @Nullable WorkspaceService workspace, int shutdownGraceSeconds) throws IOException {
        if (maxRequestBytes < 1) {
            throw new IllegalArgumentException("请求大小上限必须为正数");
        }
        if (shutdownGraceSeconds < 0) {
            throw new IllegalArgumentException("HTTP 停机等待秒数不能为负数");
        }
        this.service = service;
        this.maxRequestBytes = maxRequestBytes;
        this.shutdownGraceSeconds = shutdownGraceSeconds;
        this.workspace = workspace;
        this.server = HttpServer.create(address, 0);
        server.setExecutor(executor);
        server.createContext("/", this::handle);
    }

    public void start() {
        server.start();
    }

    public int port() {
        return server.getAddress().getPort();
    }

    @Override
    public void close() {
        server.stop(shutdownGraceSeconds);
        executor.close();
    }

    private void handle(HttpExchange exchange) throws IOException {
        // 路由按路径与方法匹配；未知 API 不落入静态资源处理。
        try (exchange) {
            try {
                final String path = exchange.getRequestURI().getPath();
                if (workspace != null && path.startsWith("/api/v1/workspace")) {
                    workspace(exchange, path, workspace);
                    return;
                }
                switch (exchange.getRequestMethod()) {
                    case "GET" -> {
                        switch (path) {
                            case "/api/v1/entities" -> json(exchange, 200, service.entities());
                            case "/api/v1/extensions" -> json(exchange, 200, service.extensions());
                            case String value when value.startsWith("/api/v1/entities/") && value.endsWith("/template") -> {
                                final String id = URLDecoder.decode(value.substring("/api/v1/entities/".length(), value.length() - "/template".length()), StandardCharsets.UTF_8);
                                json(exchange, 200, service.entityTemplate(id));
                            }
                            case String value when value.startsWith("/api/") -> json(exchange, 404, Map.of("error", "接口不存在"));
                            default -> resource(exchange, path);
                        }
                    }
                    case String method when "POST".equals(method) && "/api/v1/codec".equals(path) -> {
                        // 最多读取上限加一字节，既可识别超限也不会无界读取请求体。
                        final byte[] body = exchange.getRequestBody().readNBytes(maxRequestBytes + 1);
                        if (body.length > maxRequestBytes) {
                            json(exchange, 413, Map.of("error", "请求体超过资源上限"));
                        } else {
                            final var response = service.execute(mapper.readValue(body, DebugCodecRequest.class));
                            json(exchange, response.success() ? 200 : 422, response);
                        }
                    }
                    default -> {
                        final boolean api = path.startsWith("/api/");
                        json(exchange, api ? 404 : 405, Map.of("error", api ? "接口不存在" : "不支持的请求方法"));
                    }
                }
            } catch (WorkspaceException exception) {
                json(exchange, exception.status(), Map.of("error", exception.getMessage()));
            } catch (IllegalArgumentException | com.fasterxml.jackson.core.JsonProcessingException exception) {
                json(exchange, 400, Map.of("error", exception.getMessage() == null ? "请求无效" : exception.getMessage()));
            } catch (IOException exception) {
                log.error("HTTP 请求处理失败: {} {}", exchange.getRequestMethod(), exchange.getRequestURI().getPath(), exception);
                json(exchange, 500, Map.of("error", "工作区读取或写入失败，请检查服务器存储"));
            }
        }
    }

    private record DeleteRequest(long expectedRevision, long workspaceRevision) {
    }

    private void workspace(HttpExchange exchange, String path, WorkspaceService workspaceService) throws IOException {
        final boolean entryPath = path.startsWith("/api/v1/workspace/entries/");
        switch (exchange.getRequestMethod()) {
            case "GET" -> {
                switch (path) {
                    case "/api/v1/workspace" -> {
                        final var snapshot = workspaceService.read();
                        // 树只加载元数据，打开文档时再获取输入和运行快照。
                        json(exchange, 200, new WorkspaceData.Snapshot(snapshot.revision(), snapshot.entries().stream()
                                .map(entry -> new WorkspaceData.Entry(entry.id(), entry.parentId(), entry.kind(), entry.name(),
                                        entry.revision(), entry.createdAt(), entry.updatedAt(), null)).toList()));
                    }
                    case "/api/v1/workspace/export" -> json(exchange, 200, workspaceService.export());
                    case String value when value.startsWith("/api/v1/workspace/entries/") -> {
                        final String id = value.substring("/api/v1/workspace/entries/".length());
                        json(exchange, 200, workspaceService.get(id));
                    }
                    default -> json(exchange, 404, Map.of("error", "接口不存在"));
                }
            }
            case "POST" -> {
                switch (path) {
                    case "/api/v1/workspace/import" -> json(exchange, 201, workspaceService.importBackup(body(exchange, WorkspaceData.Backup.class)));
                    case "/api/v1/workspace/entries" -> json(exchange, 201, workspaceService.create(body(exchange, WorkspaceData.Edit.class)));
                    default -> json(exchange, entryPath ? 405 : 404, Map.of("error", entryPath ? "不支持的请求方法" : "接口不存在"));
                }
            }
            case "PUT" -> {
                if (!entryPath) {
                    json(exchange, 404, Map.of("error", "接口不存在"));
                    return;
                }
                final String id = path.substring("/api/v1/workspace/entries/".length());
                json(exchange, 200, workspaceService.update(id, body(exchange, WorkspaceData.Edit.class)));
            }
            case "DELETE" -> {
                if (!entryPath) {
                    json(exchange, 404, Map.of("error", "接口不存在"));
                    return;
                }
                final String id = path.substring("/api/v1/workspace/entries/".length());
                final var request = body(exchange, DeleteRequest.class);
                json(exchange, 200, workspaceService.delete(id, request.expectedRevision(), request.workspaceRevision()));
            }
            default -> json(exchange, entryPath ? 405 : 404, Map.of("error", entryPath ? "不支持的请求方法" : "接口不存在"));
        }
    }

    private <T> T body(HttpExchange exchange, Class<T> type) throws IOException {
        // 备份和文档可能包含 Trace；仍采用有界读取，限制为 20 MiB。
        final int limit = 20 * 1024 * 1024;
        final byte[] bytes = exchange.getRequestBody().readNBytes(limit + 1);
        if (bytes.length > limit) {
            throw new WorkspaceException(413, "工作区请求体超过 20 MiB");
        }
        return mapper.readValue(bytes, type);
    }

    private void json(HttpExchange exchange, int status, Object value) throws IOException {
        send(exchange, status, "application/json; charset=utf-8", mapper.writeValueAsBytes(value));
    }

    private void resource(HttpExchange exchange, String path) throws IOException {
        // 静态文件只从应用资源目录读取，拒绝路径穿越形式的请求。
        if (path.contains("..") || path.contains("\\") || path.indexOf(0) >= 0) {
            json(exchange, 404, Map.of("error", "资源不存在"));
            return;
        }
        final String resource = "/ui" + ("/".equals(path) ? "/index.html" : path);
        try (InputStream input = CodecStudioServer.class.getResourceAsStream(resource)) {
            if (input == null) {
                json(exchange, 404, Map.of("error", "资源不存在"));
                return;
            }
            final String type = resource.endsWith(".html") ? "text/html; charset=utf-8"
                    : resource.endsWith(".js") ? "text/javascript; charset=utf-8"
                    : resource.endsWith(".css") ? "text/css; charset=utf-8"
                    : resource.endsWith(".json") ? "application/schema+json; charset=utf-8" : "application/octet-stream";
            send(exchange, 200, type, input.readAllBytes());
        }
    }

    private static void send(HttpExchange exchange, int status, String type, byte[] bytes) throws IOException {
        // 所有响应设置明确类型并禁止浏览器嗅探，页面只消费预期的资源类型。
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
