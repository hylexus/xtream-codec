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

package io.github.hylexus.xtream.codec.core.impl.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ValueMatcherMetas(
        List<ValueMatcherMeta> valueMatchers,
        Map<Object, ValueMatcherMeta> valueMatchersByKey
) {
    public ValueMatcherMetas(List<ValueMatcherMeta> valueMatchers) {
        this(valueMatchers, valueMatcherToMap(valueMatchers));
    }

    private static Map<Object, ValueMatcherMeta> valueMatcherToMap(List<ValueMatcherMeta> valueMatchers) {
        final Map<Object, ValueMatcherMeta> valueMatchersByKey = new LinkedHashMap<>();
        valueMatchers
                .stream()
                .collect(Collectors.groupingBy(ValueMatcherMeta::key))
                .forEach((key, valueList) -> {
                    valueMatchersByKey.put(key, valueList.getFirst());
                    if (valueList.size() > 1) {
                        throw new IllegalArgumentException("duplicated @ValueMatcher.key = " + key + ": " + valueList);
                    }
                });
        return valueMatchersByKey;
    }
}
