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

import java.util.Objects;

/**
 * 单次解码的资源限制和校验和处理策略，同一编译计划可按调用选择不同策略。
 * 默认只报告校验结果；严格模式遇到校验不匹配时失败，且不提交输入游标。
 * 两种模式都不会放宽结构边界、输入完整性或资源限制。
 *
 * @param limits          本次解码的资源限制
 * @param strictChecksums 是否拒绝校验和不匹配的报文
 * @author Codex (AI)
 * @since 0.11.0
 */
public record DynamicDecodeOptions(DynamicLimits limits, boolean strictChecksums) {
    public static final DynamicDecodeOptions DEFAULT = new DynamicDecodeOptions(DynamicLimits.DEFAULT, false);
    public static final DynamicDecodeOptions STRICT = new DynamicDecodeOptions(DynamicLimits.DEFAULT, true);

    public DynamicDecodeOptions {
        Objects.requireNonNull(limits, "limits");
    }

    /**
     * 替换资源限制，保留当前校验和处理策略。
     */
    public DynamicDecodeOptions withLimits(DynamicLimits value) {
        return new DynamicDecodeOptions(value, strictChecksums);
    }
}
