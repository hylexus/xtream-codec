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

import io.github.hylexus.xtream.codec.common.bean.BeanMetadata;
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import org.jspecify.annotations.Nullable;

import java.beans.PropertyDescriptor;
import java.util.function.Function;

public interface BeanMetadataRegistry {

    record PropertyInfo(PropertyDescriptor propertyDescriptor, XtreamField xtreamField, int version) {
    }

    default BeanMetadata getBeanMetadata(Class<?> beanClass) {
        return this.getBeanMetadata(beanClass, XtreamField.DEFAULT_VERSION);
    }

    BeanMetadata getBeanMetadata(Class<?> beanClass, int version);

    default BeanMetadata getBeanMetadata(Class<?> beanClass, Function<PropertyInfo, BeanPropertyMetadata> creator) {
        return this.getBeanMetadata(beanClass, XtreamField.DEFAULT_VERSION, creator);
    }

    BeanMetadata getBeanMetadata(Class<?> beanClass, int version, Function<PropertyInfo, BeanPropertyMetadata> creator);

    FieldCodecRegistry getFieldCodecRegistry();

    @Nullable
    default FieldCodec<?> getOrCreateFieldCodec(
            int version,
            @Nullable XtreamDataType targetType,
            @Nullable Class<? extends FieldCodec<?>> codecClass,
            @Nullable String charset,
            @Nullable Class<?> targetEntityClass) {

        return this.getFieldCodecRegistry()
                .getOrCreateFieldCodec(version, this, targetType, codecClass, charset, targetEntityClass);
    }

}
