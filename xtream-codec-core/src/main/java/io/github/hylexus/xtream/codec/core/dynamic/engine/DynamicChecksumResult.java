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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicChecksumAlgorithm;

/**
 * 一次校验字段的核对结果，实际值保留在解码字段中，计算值来自报文的原始字节。
 * 重复元素分别产生结果，按完成核对的顺序收集；条件跳过的校验字段不产生结果。
 *
 * @param fieldPath 校验字段的定义路径
 * @param algorithm 校验算法
 * @param actual    报文中的实际值，以 long 表示最多 32 位的无符号校验值
 * @param expected  根据覆盖区间计算的值
 * @author Codex (AI)
 * @since 0.11.0
 */
public record DynamicChecksumResult(
        String fieldPath,
        DynamicChecksumAlgorithm algorithm,
        long actual,
        long expected) {
    /**
     * 实际值是否与计算值匹配；不依赖是否启用了跟踪器。
     */
    @JsonProperty
    public boolean matched() {
        return actual == expected;
    }
}
