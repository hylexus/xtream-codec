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

package io.github.hylexus.xtream.codec.core.impl.codec.utils;

import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record HasVersions<T>(int[] version, T source) {

    public static <S, T> VersionMatchResult<T> matchVersion(int targetVersion, Stream<HasVersions<S>> stream, Function<HasVersion<S>, T> mapper) {
        final Map<Integer, List<HasVersion<S>>> group = stream
                .flatMap(it -> {
                    final int[] version = it.version();
                    if (version.length > 1) {
                        if (Arrays.binarySearch(version, XtreamMapField.ALL_VERSION) >= 0) {
                            throw new IllegalArgumentException(
                                    "Cannot use `XtreamMapField.ALL_VERSION` with other versions\n"
                                            + "Components:\n\n" + it.source());
                        }
                    }
                    return Arrays.stream(version)
                            .mapToObj(v -> new HasVersion<>(v, it.source()));
                })
                .collect(Collectors.groupingBy(HasVersion::version));

        for (Map.Entry<Integer, List<HasVersion<S>>> entry : group.entrySet()) {
            final int v = entry.getKey();
            final List<HasVersion<S>> list = entry.getValue();
            if (list.size() > 1) {
                throw new IllegalArgumentException(
                        "Duplicate version(" + list.getFirst().data().getClass().getSimpleName()
                                + ").\nKey: " + (v == XtreamField.ALL_VERSION ? v + "(XtreamField.ALL_VERSION)" : v)
                                + "\nComponents: \n\t" + list.stream().limit(5).map(HasVersion::data).map(Object::toString).collect(Collectors.joining("\n\t")));
            }
        }

        final List<HasVersion<S>> matchedList = group.get(targetVersion);
        if (matchedList != null) {
            final HasVersion<S> first = matchedList.getFirst();
            return new VersionMatchResult<>(true, first.version(), mapper.apply(first));
        }

        // if (targetVersion == XtreamField.ALL_VERSION) {
        //     return new VersionMatchResult<>(false, 0, null);
        // }

        final List<HasVersion<S>> defaultList = group.get(XtreamMapField.ALL_VERSION);
        if (defaultList != null) {
            final HasVersion<S> first = defaultList.getFirst();
            return new VersionMatchResult<>(true, first.version(), mapper.apply(first));
        }

        return new VersionMatchResult<>(false, 0, null);
    }

}
