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

import io.github.hylexus.xtream.codec.base.expression.XtreamExpressionEngineRegistry;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtensionRegistry;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * 编译时的宿主配置。默认仅支持受控表达式，不自动注册字符串表达式引擎。
 * 注册表在编译期间应保持稳定，生成的计划绑定具体实例。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public record DynamicCompileOptions(
        int version,
        DynamicExtensionRegistry extensions,
        @Nullable XtreamExpressionEngineRegistry expressionEngines) {

    public static final DynamicCompileOptions DEFAULT = new DynamicCompileOptions(DynamicSchema.ALL_VERSION, DynamicExtensionRegistry.EMPTY, null);

    public DynamicCompileOptions {
        Objects.requireNonNull(extensions);
    }

    public DynamicCompileOptions withVersion(int version) {
        return new DynamicCompileOptions(version, extensions, expressionEngines);
    }
}
