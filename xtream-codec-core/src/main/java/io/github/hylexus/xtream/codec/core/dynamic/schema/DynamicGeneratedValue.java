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

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

/**
 * 线上字段的编码生成规则，四种来源必须且只能选择一种。
 * 固定值、计数与表达式不约束解码值；校验和在解码时报告实际线上字节的核对结果，
 * 是否拒绝不匹配的报文由单次解码选项决定。
 *
 * @param constant   固定的 JSON 值
 * @param countOf    同级集合字段标识，允许指向后续集合
 * @param expression 仅引用前序字段的表达式
 * @param checksum   字节区间校验和配置
 * @author Codex (AI)
 * @since 0.11.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DynamicGeneratedValue(
        @Nullable Object constant,
        @Nullable String countOf,
        @Nullable DynamicExpression expression,
        @Nullable DynamicChecksum checksum) {
    public DynamicGeneratedValue {
        final int sources = (constant == null ? 0 : 1)
                + (countOf == null ? 0 : 1)
                + (expression == null ? 0 : 1)
                + (checksum == null ? 0 : 1);
        if (sources != 1) {
            throw new IllegalArgumentException("generated 必须且只能声明 constant、countOf、expression 或 checksum");
        }
        constant = DynamicData.freezeValue(constant);
        if (countOf != null && !DynamicIdentifiers.isValid(countOf)) {
            throw new IllegalArgumentException("countOf 必须为同级字段标识");
        }
    }

    public static DynamicGeneratedValue constant(Object value) {
        return new DynamicGeneratedValue(value, null, null, null);
    }

    public static DynamicGeneratedValue countOf(String id) {
        return new DynamicGeneratedValue(null, id, null, null);
    }

    public static DynamicGeneratedValue expression(DynamicExpression value) {
        return new DynamicGeneratedValue(null, null, value, null);
    }

    public static DynamicGeneratedValue checksum(DynamicChecksum value) {
        return new DynamicGeneratedValue(null, null, null, value);
    }
}
