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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 一次执行的字段值，按字段标识寻址。struct 使用嵌套对象，array 使用有序数组，
 * 大于 JSON 安全整数范围的数值使用十进制字符串，以免前端读取时丢失精度。
 * 顶层映射保留调用方的插入顺序，输出顺序仍以结构定义为准。
 *
 * @param fields 按稳定字段标识寻址的顶层值映射
 * @author Codex (AI)
 * @since 0.11.0
 */
public record DynamicValues(Map<String, Object> fields) {
    public DynamicValues {
        fields = Collections.unmodifiableMap(new LinkedHashMap<>(fields));
    }

    public static DynamicValues of(Map<String, Object> fields) {
        return new DynamicValues(fields);
    }
}
