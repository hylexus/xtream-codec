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

import io.github.hylexus.xtream.codec.core.debugger.CodecDebuggerService;
import io.github.hylexus.xtream.codec.core.debugger.DebugEntityCatalog;
import io.github.hylexus.xtream.codec.studio.workspace.WorkspaceService;
import io.github.hylexus.xtream.codec.studio.workspace.WorkspaceStores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;

/**
 * 不依赖 Spring Boot 的独立运行入口。
 * 默认绑定本机地址，可通过系统属性与环境变量配置；传入包名时改为扫描指定目录。
 * HTTP 层只是调试服务的一个适配器，业务逻辑仍位于可嵌入模块。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public final class CodecStudioApplication {
    private static final Logger log = LoggerFactory.getLogger(CodecStudioApplication.class);

    public static void main(String[] args) {
        try {
            start(args);
        } catch (Exception exception) {
            log.error("Xtream Codec Studio 启动失败", exception);
            System.exit(1);
        }
    }

    private static void start(String[] args) throws Exception {
        // 首个参数为端口，其余参数为允许扫描的 Java 包名。
        final CodecStudioConfiguration configuration = CodecStudioConfiguration.from(args);
        final DebugEntityCatalog catalog = args.length > 1
                ? DebugEntityCatalog.scan(Arrays.copyOfRange(args, 1, args.length))
                : new DebugEntityCatalog(Map.of("sample", SampleMessage.class));
        final CodecDebuggerService service = new CodecDebuggerService(catalog);
        final var store = WorkspaceStores.configured();
        try {
            final WorkspaceService workspace = new WorkspaceService(store);
            final CodecStudioServer server = new CodecStudioServer(
                    new InetSocketAddress(configuration.host(), configuration.port()),
                    service, 2 * 1024 * 1024, workspace, configuration.shutdownGraceSeconds());
            final Thread shutdown = new Thread(() -> close(server, workspace), "codec-studio-shutdown");
            try {
                Runtime.getRuntime().addShutdownHook(shutdown);
                server.start();
            } catch (RuntimeException exception) {
                Runtime.getRuntime().removeShutdownHook(shutdown);
                server.close();
                throw exception;
            }
            log.info("Xtream Codec Studio: http://{}:{}", configuration.host(), server.port());
        } catch (Exception exception) {
            try (store) {
                throw exception;
            }
        }
    }

    private static void close(CodecStudioServer server, WorkspaceService workspace) {
        log.info("正在停止 Xtream Codec Studio");
        try {
            server.close();
        } catch (RuntimeException exception) {
            log.error("HTTP 服务关闭失败", exception);
        } finally {
            try {
                workspace.close();
            } catch (java.io.IOException exception) {
                log.error("工作区关闭失败", exception);
            }
        }
    }
}
