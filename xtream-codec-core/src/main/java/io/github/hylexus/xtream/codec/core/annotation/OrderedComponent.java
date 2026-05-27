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

package io.github.hylexus.xtream.codec.core.annotation;

import java.util.Comparator;
import java.util.List;

/**
 * @author hylexus
 */
public interface OrderedComponent {
    int BUILTIN_COMPONENT_PRECEDENCE = 1000000;
    int DEFAULT_PRECEDENCE = 0;
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    default int order() {
        return DEFAULT_PRECEDENCE;
    }

    static <T extends OrderedComponent> List<T> sort(List<T> components) {
        return components.stream()
                .sorted(Comparator.comparingInt(OrderedComponent::order))
                .toList();
    }
}
