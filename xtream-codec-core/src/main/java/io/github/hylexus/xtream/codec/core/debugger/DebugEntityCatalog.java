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

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.github.hylexus.xtream.codec.core.utils.XtreamFieldUtils;

import java.util.*;

/**
 * 宿主构建的实体白名单。HTTP 层只使用目录标识，不能按请求内容加载类。
 * 宿主可显式传入类型映射，也可仅扫描指定包中的注解实体。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public final class DebugEntityCatalog {
    private final Map<String, Class<?>> entities;

    public DebugEntityCatalog(Map<String, Class<?>> entities) {
        this.entities = Collections.unmodifiableMap(new LinkedHashMap<>(entities));
    }

    public Class<?> resolve(String id) {
        final Class<?> type = entities.get(id);
        if (type == null) {
            throw new IllegalArgumentException("未知的实体目录标识: " + id);
        }
        return type;
    }

    public List<Entry> entries() {
        return entities.entrySet().stream()
                .map(entry -> new Entry(entry.getKey(), entry.getValue().getSimpleName(), entry.getValue().getName()))
                .toList();
    }

    public static DebugEntityCatalog scan(String... allowedPackages) {
        // 拒绝空包或不含层级的包名，避免意外将整个 classpath 暴露到调试目录。
        if (allowedPackages.length == 0 || Arrays.stream(allowedPackages).anyMatch(pkg ->
                pkg == null || pkg.isBlank() || !pkg.contains("."))) {
            throw new IllegalArgumentException("扫描必须指定明确且受限的包");
        }
        final Map<String, Class<?>> entities = new LinkedHashMap<>();
        try (ScanResult scan = new ClassGraph().acceptPackages(allowedPackages).enableClassInfo().scan()) {
            // 仅展示实际带编解码字段的具体类，按类名排序保证目录顺序稳定。
            scan.getAllClasses()
                    .stream()
                    .filter(info -> !info.isAbstract() && !info.isInterface())
                    .sorted(Comparator.comparing(ClassInfo::getName))
                    .forEach(info -> {
                        final Class<?> type = info.loadClass();
                        if (Arrays.stream(type.getDeclaredFields()).anyMatch(field -> !XtreamFieldUtils.getOrEmpty(field).isEmpty())) {
                            entities.put(type.getName(), type);
                        }
                    });
        }
        return new DebugEntityCatalog(entities);
    }

    public record Entry(String id, String name, String className) {
    }
}
