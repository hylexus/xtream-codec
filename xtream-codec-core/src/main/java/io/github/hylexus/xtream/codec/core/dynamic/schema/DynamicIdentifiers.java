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

import org.jspecify.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * 字段标识、表达式绑定名和回填区间端点共用的标识符规则。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
final class DynamicIdentifiers {
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private DynamicIdentifiers() {
    }

    static boolean isValid(@Nullable String value) {
        return value != null && IDENTIFIER.matcher(value).matches();
    }

    static boolean isValidRange(String value) {
        final int separator = value.indexOf("..");
        if (separator < 0) {
            return isValid(value);
        }
        return isValid(value.substring(0, separator)) && isValid(value.substring(separator + 2));
    }
}
