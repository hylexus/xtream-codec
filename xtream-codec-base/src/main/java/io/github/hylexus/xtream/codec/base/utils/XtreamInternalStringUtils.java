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

package io.github.hylexus.xtream.codec.base.utils;

import org.jetbrains.annotations.ApiStatus;
import org.springframework.util.StringUtils;

/**
 * 当前类是从 {@link StringUtils org.springframework.util.StringUtils} 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on {@link StringUtils org.springframework.util.StringUtils}.
 * <p>
 * <h4>这个类存在的原因</h4>
 * 为了 <a href="https://github.com/hylexus/jt-framework">jt-framework</a> 适配 xtream-codec-core，详情见: <a href="https://github.com/hylexus/xtream-codec/issues/11">issue#11</a>
 *
 * @see StringUtils org.springframework.util.StringUtils
 * @see <a href="https://github.com/hylexus/xtream-codec/issues/11">issue#11</a>
 * @since 0.5.0-rc.1
 */
@ApiStatus.Internal
@ApiStatus.AvailableSince("0.5.0-rc.1")
public final class XtreamInternalStringUtils {
    private XtreamInternalStringUtils() {
        throw new UnsupportedOperationException("No instance.");
    }

    /**
     * 从 spring 的 StringUtils 复制过来的。
     *
     * @see StringUtils#uncapitalizeAsProperty(String)
     */
    public static String uncapitalizeAsProperty(String str) {
        if (!StringUtils.hasLength(str) || (str.length() > 1 && Character.isUpperCase(str.charAt(0)) && Character.isUpperCase(str.charAt(1)))) {
            return str;
        }
        return changeFirstCharacterCase(str, false);
    }

    private static String changeFirstCharacterCase(String str, @SuppressWarnings("SameParameterValue") boolean capitalize) {
        if (!StringUtils.hasLength(str)) {
            return str;
        }

        char baseChar = str.charAt(0);
        char updatedChar;
        if (capitalize) {
            updatedChar = Character.toUpperCase(baseChar);
        } else {
            updatedChar = Character.toLowerCase(baseChar);
        }
        if (baseChar == updatedChar) {
            return str;
        }

        char[] chars = str.toCharArray();
        chars[0] = updatedChar;
        return new String(chars);
    }
}
