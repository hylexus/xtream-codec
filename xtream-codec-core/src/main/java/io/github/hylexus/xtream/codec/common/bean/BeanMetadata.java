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

package io.github.hylexus.xtream.codec.common.bean;

import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.lang.reflect.Constructor;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class BeanMetadata {
    private final Class<?> rawType;
    private final Constructor<?> constructor;
    private final List<BeanPropertyMetadata> propertyMetadataList;

    public BeanMetadata(Class<?> rawType, Constructor<?> constructor, List<BeanPropertyMetadata> propertyMetadataList) {
        this.rawType = rawType;
        this.constructor = constructor;
        this.propertyMetadataList = propertyMetadataList;
    }

    public <T> T createNewRecordInstance(Object[] filedValues) {
        final Object newInstance = BeanUtils.createNewInstance(this.getConstructor(), filedValues);
        @SuppressWarnings("unchecked") final T casted = (T) newInstance;
        return casted;
    }

    public Object createNewInstance() {
        if (this.rawType.isRecord()) {
            return null;
        }
        return BeanUtils.createNewInstance(this.getConstructor(), (Object[]) null);
    }

}
