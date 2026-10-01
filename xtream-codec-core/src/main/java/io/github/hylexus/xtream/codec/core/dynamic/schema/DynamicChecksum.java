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

import java.util.Objects;

/**
 * 同级字段的连续字节区间校验和；区间包含两端字段，但不能包含校验和字段自身。
 * 算法作用于实际线上字节，包含长度头、填充和已完成的回填值。
 *
 * @param algorithm 校验算法
 * @param range     单个字段标识或 first..last
 * @author Codex (AI)
 * @since 0.11.0
 */
public record DynamicChecksum(DynamicChecksumAlgorithm algorithm, String range) {
    public DynamicChecksum {
        Objects.requireNonNull(algorithm, "algorithm");
        if (!DynamicIdentifiers.isValidRange(range)) {
            throw new IllegalArgumentException("校验和区间必须为字段标识或 first..last");
        }
    }
}

