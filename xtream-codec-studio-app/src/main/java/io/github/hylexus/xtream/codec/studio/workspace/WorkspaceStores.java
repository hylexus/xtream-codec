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

package io.github.hylexus.xtream.codec.studio.workspace;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ServiceLoader;

/**
 * 启动时选择存储介质。系统属性覆盖环境变量，初始化失败不会回退。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public final class WorkspaceStores {
    private WorkspaceStores() {
    }

    public static WorkspaceStore configured() throws IOException {
        final Path directory = Path.of(setting("xtream.studio.data-dir", "XTREAM_STUDIO_DATA_DIR",
                Path.of(System.getProperty("user.home"), ".xtream-codec", "studio").toString()));
        final String type = setting("xtream.studio.storage", "XTREAM_STUDIO_STORAGE", "file");
        return switch (type) {
            case "file" -> new FileWorkspaceStore(directory);
            case "sqlite" -> new SqliteWorkspaceStore(Path.of(setting("xtream.studio.sqlite-path", "XTREAM_STUDIO_SQLITE_PATH",
                    directory.resolve("workspace.db").toString())));
            default -> {
                final var providers = ServiceLoader.load(WorkspaceStoreProvider.class).stream()
                        .map(ServiceLoader.Provider::get).filter(provider -> provider.type().equals(type)).toList();
                if (providers.size() != 1) {
                    throw new IllegalArgumentException("存储类型必须匹配一个提供者: " + type);
                }
                yield providers.getFirst().open(directory);
            }
        };
    }

    private static String setting(String property, String environment, String fallback) {
        final String value = System.getProperty(property, System.getenv(environment));
        return value == null || value.isBlank() ? fallback : value;
    }
}
