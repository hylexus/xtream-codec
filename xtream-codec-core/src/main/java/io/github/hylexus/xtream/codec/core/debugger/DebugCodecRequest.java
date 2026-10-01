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

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;

import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * 版本化 API 的统一请求，模式与方向使用枚举，JSON 中仍使用小写名称。
 * entity 使用目录标识和可选注解版本；dynamic 使用结构定义。
 * 编码读取 {@code values}，解码读取原始 {@code hex}，其余模式专属属性可为空。
 *
 * @param mode      实体或动态模式，不能为空
 * @param direction 编码或解码方向，不能为空
 * @param entityId  宿主实体目录中的标识，仅 entity 模式使用
 * @param version   实体或动态定义的目标协议版本；省略时使用默认版本定义
 * @param schema    动态结构定义，仅 dynamic 模式使用
 * @param values    按字段标识寻址的编码输入，嵌套对象和集合使用 Map/List
 * @param hex       解码输入的原始十六进制报文
 * @author Codex (AI)
 * @since 0.11.0
 */
public record DebugCodecRequest(
        DebugCodecMode mode,
        DebugCodecDirection direction,
        @Nullable String entityId,
        @Nullable Integer version,
        @Nullable DynamicSchema schema,
        @Nullable Map<String, Object> values,
        @Nullable String hex) {
    public DebugCodecRequest {
        if (mode == null) {
            throw new IllegalArgumentException("mode 不能为空");
        }
        if (direction == null) {
            throw new IllegalArgumentException("direction 不能为空");
        }
    }
}
