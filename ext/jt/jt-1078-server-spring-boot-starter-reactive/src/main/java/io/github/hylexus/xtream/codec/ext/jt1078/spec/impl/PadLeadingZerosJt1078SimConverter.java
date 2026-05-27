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

package io.github.hylexus.xtream.codec.ext.jt1078.spec.impl;

import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SimConverter;

/**
 * 添加前导零，按照如下顺序转换：
 * <ol>
 * <li>{@code sim.length() == 12} : 原样返回({@code BCD[6]})</li>
 * <li>{@code sim.length() == 20}: 原样返回({@code BCD[10]})</li>
 * <li>{@code sim.length() < 12}: 前补 0, 补足 12 位({@code BCD[6]})</li>
 * <li>{@code sim.length() < 20}: 前补 0, 补足 20 位({@code BCD[10]})</li>
 * <li>其他情况: 抛异常</li>
 * </ol>
 *
 * @author hylexus
 */
public class PadLeadingZerosJt1078SimConverter implements Jt1078SimConverter {

    @Override
    public String convert(String original) {
        return paddingIfNecessary(original);
    }

    public static String paddingIfNecessary(String original) {
        // BCD[6] ==> 12
        // BCD[10] ==> 20
        final int simLength = original.length();
        if (simLength == 12 || simLength == 20) {
            return original;
        }
        if (simLength < 12) {
            return "0".repeat(12 - simLength) + original;
        }
        if (simLength < 20) {
            return "0".repeat(20 - simLength) + original;
        }
        // 不应该执行到这里，除非 SIM 传错了
        throw new IllegalArgumentException("Invalid sim length. sim=`" + original + "`");
    }
}
