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

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 编译阶段按协议版本选择同一标识的有效定义，保留调用方提交的原始字段。
 * 整数名称对应的执行布局由计划字段保存，不改写 DSL 定义。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
final class DynamicFieldSelection {
    private DynamicFieldSelection() {
    }

    static List<DynamicField> select(List<DynamicField> fields, String parent, int version) {
        // 首次出现的位置决定字段顺序，后续版本覆盖只改变布局，不移动字段。
        // <id, <version, field>>
        final Map<String, Map<Integer, DynamicField>> candidates = new LinkedHashMap<>();
        for (final DynamicField field : fields) {
            final List<Integer> versions = field.versions() == null ? List.of(DynamicSchema.ALL_VERSION) : field.versions();
            final Map<Integer, DynamicField> definitions = candidates.computeIfAbsent(field.id(), ignored -> new LinkedHashMap<>());
            for (final int v : versions) {
                // 拒绝重叠定义，避免同版本布局随配置顺序静默变化。
                if (definitions.putIfAbsent(v, field) != null) {
                    throw error(parent + "." + field.id(), "重复的字段版本定义: " + v);
                }
            }
        }
        final List<DynamicField> selected = new ArrayList<>();
        for (final Map<Integer, DynamicField> definitions : candidates.values()) {
            final DynamicField field = definitions.getOrDefault(version, definitions.get(DynamicSchema.ALL_VERSION));
            if (field != null) {
                selected.add(field);
            }
        }
        return List.copyOf(selected);
    }

    private static DynamicCodecException error(String path, String message) {
        return new DynamicCodecException(path, -1, message);
    }
}
