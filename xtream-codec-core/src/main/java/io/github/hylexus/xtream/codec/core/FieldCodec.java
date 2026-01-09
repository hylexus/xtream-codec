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

import com.fasterxml.jackson.annotation.JsonValue;
import io.github.hylexus.xtream.codec.base.expression.XtreamEvaluationContext;
import io.github.hylexus.xtream.codec.common.bean.BeanMetadata;
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.exception.NotYetImplementedException;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.annotation.NumberSignedness;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.*;
import java.util.Objects;

public interface FieldCodec<T> {

    @JsonValue
    default String jsonValue() {
        return this.getClass().getSimpleName();
    }

    default NumberSignedness signedness() {
        return NumberSignedness.NONE;
    }

    @Nullable T deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length);

    /**
     * 带解码器埋点的反序列化方法
     * <p>
     * 逻辑和 {@link #deserialize(BeanPropertyMetadata, DeserializeContext, ByteBuf, int)} 一致。
     * <p>
     * 不想到处写{@link CodecContext#codecTracker()} 的判空语句，所以单独定义了一个方法。
     *
     * @apiNote 这个方法对性能有一定影响。仅仅用于调试。
     * @see #deserialize(BeanPropertyMetadata, DeserializeContext, ByteBuf, int)
     * @see CodecTracker
     */
    default @Nullable T deserializeWithTracker(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        final int indexBeforeRead = input.readerIndex();
        final T value = this.deserialize(propertyMetadata, context, input, length);
        final String hexString = FormatUtils.toHexString(input, indexBeforeRead, input.readerIndex() - indexBeforeRead);
        final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
        codecTracker.addFieldSpan(codecTracker.getCurrentSpan(), propertyMetadata.name(), value, hexString, this, propertyMetadata.xtreamFieldAnnotation().desc());
        return value;
    }

    void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable T value);

    /**
     * 带编码器埋点的序列化方法
     * <p>
     * 逻辑和 {@link #serialize(BeanPropertyMetadata, SerializeContext, ByteBuf, Object)} 一致。
     * <p>
     * 不想到处写{@link CodecContext#codecTracker()} 的判空语句，所以单独定义了一个方法。
     *
     * @apiNote 这个方法对性能有一定影响。仅仅用于调试。
     * @see #serialize(BeanPropertyMetadata, SerializeContext, ByteBuf, Object)
     * @see CodecTracker
     */
    default void serializeWithTracker(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable T value) {
        final int indexBeforeWrite = output.writerIndex();
        this.serialize(propertyMetadata, context, output, value);
        final String hexString = FormatUtils.toHexString(output, indexBeforeWrite, output.writerIndex() - indexBeforeWrite);
        final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
        codecTracker.addFieldSpan(codecTracker.getCurrentSpan(), propertyMetadata.name(), value, hexString, this, propertyMetadata.xtreamFieldAnnotation().desc());
    }

    default Class<?> underlyingJavaType() {
        throw new NotYetImplementedException();
    }

    interface CodecContext {
        ByteBufAllocator bufferFactory();

        /**
         * 如果正在被解码的实体类型是 {@link Record} 类型，该方法返回 {@link java.util.Map}。
         *
         * @see BeanMetadata#createNewInstanceForDecoding()
         * @see EntityDecoder#decode(int, ByteBuf, BeanMetadata, Object)
         * @see EntityDecoder#decodeWithTracker(int, ByteBuf, BeanMetadata, Object, CodecTracker)
         */
        Object containerInstance();

        XtreamEvaluationContext evaluationContext();

        int version();

        FieldCodecRegistry fieldCodecRegistry();

        BeanMetadataRegistry beanMetadataRegistry();

        /**
         * @see FieldCodec#deserializeWithTracker(BeanPropertyMetadata, DeserializeContext, ByteBuf, int)
         * @see FieldCodec#serializeWithTracker(BeanPropertyMetadata, SerializeContext, ByteBuf, Object)
         */
        @Nullable CodecTracker codecTracker();
    }

    interface DeserializeContext extends CodecContext {
        EntityDecoder entityDecoder();
    }

    interface SerializeContext extends CodecContext {
        EntityEncoder entityEncoder();
    }

    class Placeholder implements FieldCodec<Object> {
        public static final Placeholder INSTANCE = new Placeholder();

        public static <T> FieldCodec<T> createForInternalUse(String errorMessage) {
            return new FieldCodec<>() {
                @Override
                public T deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
                    throw new UnsupportedOperationException(errorMessage);
                }

                @Override
                public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable T value) {
                    throw new UnsupportedOperationException(errorMessage);
                }
            };
        }

        @Override
        public Object deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable Object value) {
            throw new UnsupportedOperationException();
        }
    }

    enum TransientRecordComponentFieldCodec implements FieldCodec<Object> {
        INSTANCE;

        @Override
        public Object deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable Object value) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * @see BeanUtils#createFieldCodecInstance(Class, BeanMetadataRegistry, int)
     */
    @Documented
    @Target({ElementType.CONSTRUCTOR})
    @Retention(RetentionPolicy.RUNTIME)
    @interface FieldCodecCreator {
        boolean ignoreUnknownParameters() default false;
    }

    enum NullFieldCodec implements FieldCodec<Object> {
        INSTANCE;

        @Override
        public Object deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable Object value) {
            throw new UnsupportedOperationException();
        }

    }
}
