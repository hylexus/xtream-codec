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

package io.github.hylexus.xtream.codec.core;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.annotation.NumberEndian;
import io.github.hylexus.xtream.codec.core.annotation.NumberSignedness;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

public interface FieldCodecRegistry {

    Optional<FieldCodec<?>> getFieldCodec(BeanPropertyMetadata propertyMetadata);

    Optional<FieldCodec<?>> getFieldCodec(int sizeInBytes, NumberSignedness signedness, @Nullable String charset, boolean littleEndian, Class<?> targetType);

    /**
     * @see AtomicDataType
     */
    default Optional<FieldCodec<?>> getFieldCodecForAtomicDataType(Class<?> targetType) {
        return this.getFieldCodec(-1, NumberSignedness.NONE, null, false, targetType);
    }

    @Nullable
    FieldCodec<?> getOrCreateFieldCodec(
            @Nullable BeanMetadataRegistry beanMetadataRegistry,
            @Nullable XtreamDataType targetType,
            @Nullable Class<? extends FieldCodec<?>> codecClass,
            @Nullable String charset,
            @Nullable Class<?> targetEntityClass);

    void register(FieldCodec<?> fieldCodec, Class<?> targetType, int sizeInBytes, String charset, boolean littleEndian);

    default void registerPojoFieldCodec(FieldCodec<?> fieldCodec, Class<?> targetType) {
        this.register(fieldCodec, targetType, -1, "", false);
    }

    default Optional<FieldCodec<Object>> getFieldCodecAndCastToObject(int sizeInBytes, NumberSignedness signedness, @Nullable String charset, boolean littleEndian, Class<?> targetType) {
        return this.getFieldCodec(sizeInBytes, signedness, charset, littleEndian, targetType).map(it -> {
            @SuppressWarnings("unchecked") final FieldCodec<Object> cast = (FieldCodec<Object>) it;
            return cast;
        });
    }

    interface FieldCodecRegistryAware {
        void setFieldCodecRegistry(FieldCodecRegistry registry);
    }

    /**
     * @since 0.4.0
     */
    @ApiStatus.AvailableSince("0.4.0")
    Stream<CodecDescriptor> descriptors();

    record CodecDescriptor(
            String key,
            String rawClassName,
            NumberSignedness signedness,
            String charset,
            boolean isBuiltin,
            NumberEndian endian
    ) {
        @SuppressWarnings("unused")
        public CodecDescriptor {
        }
    }

    /**
     * @see #getFieldCodecForAtomicDataType(Class)
     */
    interface AtomicDataType {
    }

}
