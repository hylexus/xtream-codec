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

import io.github.hylexus.xtream.codec.core.annotation.XtreamField;

import java.util.List;

/**
 * 与编解码值分离的版本化二进制结构定义。字段顺序就是线上读写顺序，
 * 也决定引用字段何时可见；调整顺序可能改变报文布局和引用的合法性。
 * {@code schemaVersion} 用于定义格式演进，与具体私有协议的版本号无关。
 *
 * @param schemaVersion 动态 DSL 的格式版本
 * @param fields        顶层有序字段列表
 * @author Codex (AI)
 * @since 0.11.0
 */
public record DynamicSchema(
        int schemaVersion,
        List<DynamicField> fields
) {
    public static final int CURRENT_VERSION = 1;

    /**
     * 默认协议版本，与 XtreamField.ALL_VERSION 的取值和兜底语义一致。
     */
    public static final int ALL_VERSION = XtreamField.ALL_VERSION;

    public DynamicSchema {
        if (schemaVersion != CURRENT_VERSION) {
            throw new IllegalArgumentException("不支持的 schemaVersion: " + schemaVersion);
        }
        // 计划会复用此定义，顶层字段顺序必须在构造后保持稳定。
        fields = List.copyOf(fields);
    }

    public static DynamicSchema of(DynamicField... fields) {
        return new DynamicSchema(CURRENT_VERSION, List.of(fields));
    }
}
