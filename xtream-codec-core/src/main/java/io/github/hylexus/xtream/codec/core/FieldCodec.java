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

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.exception.NotYetImplementedException;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.annotation.NumberSignedness;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import org.springframework.expression.EvaluationContext;

public interface FieldCodec<T> {

    default NumberSignedness signedness() {
        return NumberSignedness.NONE;
    }

    T deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length);

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
    default T deserializeWithTracker(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        final int indexBeforeRead = input.readerIndex();
        final T value = this.deserialize(propertyMetadata, context, input, length);
        final String hexString = FormatUtils.toHexString(input, indexBeforeRead, input.readerIndex() - indexBeforeRead);
        context.codecTracker().addFieldSpan(context.codecTracker().getCurrentSpan(), propertyMetadata.name(), value, hexString, this, propertyMetadata.xtreamFieldAnnotation().desc());
        return value;
    }

    void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, T value);

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
    default void serializeWithTracker(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, T value) {
        final int indexBeforeWrite = output.writerIndex();
        this.serialize(propertyMetadata, context, output, value);
        final String hexString = FormatUtils.toHexString(output, indexBeforeWrite, output.writerIndex() - indexBeforeWrite);
        context.codecTracker().addFieldSpan(context.codecTracker().getCurrentSpan(), propertyMetadata.name(), value, hexString, this, propertyMetadata.xtreamFieldAnnotation().desc());
    }

    default Class<?> underlyingJavaType() {
        throw new NotYetImplementedException();
    }

    interface CodecContext {

        Object containerInstance();

        EvaluationContext evaluationContext();

        int version();

        CodecTracker codecTracker();

    }

    interface DeserializeContext extends CodecContext {
        EntityDecoder entityDecoder();
    }

    interface SerializeContext extends CodecContext {

        EntityEncoder entityEncoder();

    }

    class Placeholder implements FieldCodec<Object> {

        @Override
        public Object deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Object value) {
            throw new UnsupportedOperationException();
        }
    }
}
