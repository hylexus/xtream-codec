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
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class BeanMetadata {
    private static final Logger log = LoggerFactory.getLogger(BeanMetadata.class);
    private final Class<?> rawType;
    private final Constructor<?> constructor;
    private final List<BeanPropertyMetadata> propertyMetadataList;
    private final ObjectInstantiator instantiator;

    public BeanMetadata(Class<?> rawType, Constructor<?> constructor, List<BeanPropertyMetadata> propertyMetadataList) {
        this.rawType = rawType;
        this.constructor = constructor;
        this.propertyMetadataList = propertyMetadataList;
        this.instantiator = createInstantiator(rawType, constructor);
    }

    public <T> T createNewRecordInstance(Object[] filedValues) {
        final Object newInstance = this.instantiator.newInstanceIgnoreException(filedValues);
        // final Object newInstance = BeanUtils.createNewInstance(this.getConstructor(), filedValues);
        @SuppressWarnings("unchecked") final T casted = (T) newInstance;
        return casted;
    }

    /**
     * @deprecated Use {@link #createNewInstanceWithNoArgsConstructor()} instead.
     */
    @Deprecated(since = "0.2.0", forRemoval = true)
    @Nullable
    public Object createNewInstance() {
        if (this.rawType.isRecord()) {
            return null;
        }
        return BeanUtils.createNewInstance(this.getConstructor(), (Object[]) null);
    }

    public Object createNewInstanceWithNoArgsConstructor() {
        if (this.rawType.isRecord()) {
            return null;
        }
        return this.instantiator.newInstanceIgnoreException((Object[]) null);
    }

    private ObjectInstantiator createInstantiator(Class<?> rawType, Constructor<?> constructor) {
        if (rawType.isRecord()) {
            try {
                final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(rawType, MethodHandles.lookup());
                final MethodHandle mh = lookup.unreflectConstructor(constructor);
                return mh::invokeWithArguments;
            } catch (Throwable e) {
                log.error("Failed to create MethodHandle for constructor, falling back to reflection.", e);
                return args -> {
                    try {
                        return constructor.newInstance(args);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                };
            }
        } else {
            // 对于非 record 类，继续使用传统的反射
            return args -> {
                try {
                    return constructor.newInstance(args);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    @FunctionalInterface
    private interface ObjectInstantiator {

        @SuppressWarnings("checkstyle:NoWhitespaceBefore")
        Object newInstance(@Nullable Object @Nullable ... args) throws Throwable;

        @SuppressWarnings("checkstyle:NoWhitespaceBefore")
        default Object newInstanceIgnoreException(@Nullable Object @Nullable ... args) {
            try {
                return this.newInstance(args);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
