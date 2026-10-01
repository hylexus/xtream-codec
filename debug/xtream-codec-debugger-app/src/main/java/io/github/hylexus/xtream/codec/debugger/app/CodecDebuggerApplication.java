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

import io.github.hylexus.xtream.codec.core.debugger.CodecDebuggerService;
import io.github.hylexus.xtream.codec.core.debugger.DebugEntityCatalog;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;

/**
 * 不依赖 Spring Boot 的独立运行入口。
 * 默认仅绑定本机地址并注册协议无关示例实体；传入包名时改为扫描指定目录。
 * HTTP 层只是调试服务的一个适配器，业务逻辑仍位于可嵌入模块。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public final class CodecDebuggerApplication {

    public static void main(String[] args) throws Exception {
        // 首个参数为端口，其余参数为允许扫描的 Java 包名。
        final int port = args.length == 0 ? 8765 : Integer.parseInt(args[0]);
        final DebugEntityCatalog catalog = args.length > 1
                ? DebugEntityCatalog.scan(Arrays.copyOfRange(args, 1, args.length))
                : new DebugEntityCatalog(Map.of("sample", SampleMessage.class));
        final CodecDebuggerServer server = new CodecDebuggerServer(
                new InetSocketAddress("127.0.0.1", port),
                new CodecDebuggerService(catalog),
                2 * 1024 * 1024
        );
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
        server.start();
        System.out.println("通用编解码调试工具: http://127.0.0.1:" + server.port());
    }
}
