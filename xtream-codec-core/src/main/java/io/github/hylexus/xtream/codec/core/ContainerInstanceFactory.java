/*
 * Copyright 2024 the original author or authors.
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

package io.github.hylexus.xtream.codec.core;

import io.github.hylexus.xtream.codec.common.exception.NotYetImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public interface ContainerInstanceFactory {

    Object create();

    ContainerInstanceFactory PLACEHOLDER = PlaceholderContainerInstanceFactory.INSTANCE;

    enum PlaceholderContainerInstanceFactory implements ContainerInstanceFactory {
        INSTANCE;

        @Override
        public Object create() {
            throw new NotYetImplementedException();
        }
    }

    // region java.util.Map
    class HashMapContainerInstanceFactory implements ContainerInstanceFactory {

        @Override
        public Object create() {
            return new HashMap<>();
        }
    }

    class LinkedHashMapContainerInstanceFactory implements ContainerInstanceFactory {

        @Override
        public Object create() {
            return new LinkedHashMap<>();
        }
    }
    // endregion java.util.Map

    // region java.util.List
    class ArrayListContainerInstanceFactory implements ContainerInstanceFactory {

        @Override
        public Object create() {
            return new ArrayList<>();
        }
    }

    class LinkedListContainerInstanceFactory implements ContainerInstanceFactory {

        @Override
        public Object create() {
            return new LinkedList<>();
        }
    }
    // endregion java.util.List
}
