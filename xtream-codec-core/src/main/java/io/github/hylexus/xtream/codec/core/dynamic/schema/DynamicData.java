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

package io.github.hylexus.xtream.codec.core.dynamic.schema;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * JSON 结构配置的深度快照，阻止调用方修改已经编译的定义。
 * 只接受 JSON 能表示的标量、对象和数组；保留对象的字段顺序，
 * 并递归复制嵌套集合，避免浅拷贝遗漏 {@code options} 中的可变值。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
final class DynamicData {
    private DynamicData() {
    }

    static Map<String, Object> freeze(Map<String, Object> source) {
        final Map<String, Object> result = new LinkedHashMap<>();
        for (final Map.Entry<String, Object> entry : source.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            final Object frozenValue = freezeValue(value);
            result.put(Objects.requireNonNull(key), frozenValue);
        }
        return Collections.unmodifiableMap(result);
    }

    static @Nullable Object freezeValue(@Nullable Object value) {
        // JSON 标量本身不可变；容器必须逐层复制并包装为只读视图。
        return switch (value) {
            case null -> null;
            // Java 调用方可用枚举配置内置选项；快照仍保留 JSON 字符串，确保往返后的模型相等。
            case DynamicPaddingSide side -> side.value();
            case DynamicLengthUnit unit -> unit.value();
            case String string -> string;
            case Boolean bool -> bool;
            case Integer integer -> integer;
            case Long number -> number;
            case Double number -> number;
            case Float number -> number;
            case Short number -> number;
            case Byte number -> number;
            case BigInteger number -> number;
            case BigDecimal number -> number;
            case Map<?, ?> map -> {
                final Map<String, Object> result = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    final String key = (String) entry.getKey();
                    final Object item = entry.getValue();
                    final Object frozenValue = freezeValue(item);
                    result.put(key, frozenValue);
                }
                yield Collections.unmodifiableMap(result);
            }
            case List<?> list -> {
                final List<Object> result = new ArrayList<>();
                for (final Object item : list) {
                    final Object frozenValue = freezeValue(item);
                    result.add(frozenValue);
                }
                yield Collections.unmodifiableList(result);
            }
            default -> throw new IllegalArgumentException("结构配置只接受 JSON 值: " + value.getClass().getName());
        };
    }
}
