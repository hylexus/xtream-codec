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

import java.util.Map;
import java.util.Properties;

/**
 * 独立应用的网络与 HTTP 停机配置。系统属性优先于环境变量，位置端口参数保持兼容。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
record CodecStudioConfiguration(String host, int port, int shutdownGraceSeconds) {

    CodecStudioConfiguration {
        if (host.isBlank()) {
            throw new IllegalArgumentException("xtream.studio.host 不能为空");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("xtream.studio.port 必须在 0–65535 之间");
        }
        if (shutdownGraceSeconds < 0) {
            throw new IllegalArgumentException("xtream.studio.shutdown-grace-seconds 不能为负数");
        }
    }

    static CodecStudioConfiguration from(String[] args) {
        return from(args, System.getProperties(), System.getenv());
    }

    static CodecStudioConfiguration from(String[] args, Properties properties, Map<String, String> environment) {
        final String host = setting(properties, environment, "xtream.studio.host", "XTREAM_STUDIO_HOST", "127.0.0.1");
        final String port = args.length > 0 ? args[0]
                : setting(properties, environment, "xtream.studio.port", "XTREAM_STUDIO_PORT", "8765");
        final String grace = setting(properties, environment, "xtream.studio.shutdown-grace-seconds",
                "XTREAM_STUDIO_SHUTDOWN_GRACE_SECONDS", "5");
        return new CodecStudioConfiguration(host, integer("xtream.studio.port", port),
                integer("xtream.studio.shutdown-grace-seconds", grace));
    }

    private static String setting(Properties properties, Map<String, String> environment,
                                  String property, String variable, String fallback) {
        final String value = properties.getProperty(property, environment.get(variable));
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static int integer(String property, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(property + " 必须为整数", exception);
        }
    }
}
