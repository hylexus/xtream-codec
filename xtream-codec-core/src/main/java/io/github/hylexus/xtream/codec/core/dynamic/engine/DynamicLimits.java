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


/**
 * 单次执行的资源上限，字节数只统计本次调用使用的缓冲区区间。
 * 输入和输出上限分别防止异常长度读取及编码时缓冲区无限扩张；
 * 集合项数与嵌套深度限制用于约束合法但过于复杂的结构。
 *
 * @param maxInputBytes  单次可解码的最大输入字节数
 * @param maxOutputBytes 单次可编码的最大输出字节数
 * @param maxItems       单次执行可访问的最大集合元素数
 * @param maxDepth       字段结构允许的最大执行嵌套深度
 * @author Codex (AI)
 * @since 0.11.0
 */
public record DynamicLimits(
        int maxInputBytes,
        int maxOutputBytes,
        int maxItems,
        int maxDepth
) {
    /**
     * 适用于常规报文的默认限制；可按业务报文大小替换。
     */
    public static final DynamicLimits DEFAULT = new DynamicLimits(16 * 1024 * 1024, 16 * 1024 * 1024, 100000, 64);

    public DynamicLimits {
        if (maxInputBytes < 0 || maxOutputBytes < 0 || maxItems < 0 || maxDepth < 1) {
            throw new IllegalArgumentException("动态编解码资源上限无效");
        }
    }
}
