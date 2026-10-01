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

package io.github.hylexus.xtream.codec.core.dynamic.spi;

import java.util.Map;

/**
 * 宿主注册的双向字节扩展，适用于日期时间、业务格式和自定义转换。
 * 结构定义仅引用注册标识，不携带实现类名；扩展由宿主显式提供。
 * 实现必须可并发调用，不得保存请求状态；解码输入已经受字段边界限制。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public interface DynamicExtension {
    /**
     * 在结构编译阶段检查扩展配置，拒绝运行时才会失败的定义。
     */
    void validate(Map<String, Object> config);

    /**
     * 将 JSON 可表达的字段值转换为字节，再由执行器处理长度和填充。
     */
    byte[] encode(Object value, Map<String, Object> config);

    /**
     * 将字段边界内的字节转换为 JSON 可表达的值。
     */
    Object decode(byte[] bytes, Map<String, Object> config);
}
