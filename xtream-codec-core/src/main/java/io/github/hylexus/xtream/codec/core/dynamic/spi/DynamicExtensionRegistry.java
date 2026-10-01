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
 * 不可变扩展白名单，只按宿主注册标识解析，不根据结构定义加载类。
 * 同一个注册表可供多个编译计划复用；注册表不持有单次请求的输入或输出状态。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public record DynamicExtensionRegistry(Map<String, DynamicExtension> extensions) {
    public static final DynamicExtensionRegistry EMPTY = new DynamicExtensionRegistry(Map.of());

    public DynamicExtensionRegistry {
        extensions = Map.copyOf(extensions);
    }
}
