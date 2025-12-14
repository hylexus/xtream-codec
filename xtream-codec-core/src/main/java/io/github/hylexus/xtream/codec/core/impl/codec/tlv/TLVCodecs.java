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

package io.github.hylexus.xtream.codec.core.impl.codec.tlv;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.tracker.NestedFieldSpan;
import io.github.hylexus.xtream.codec.core.type.TLV;
import io.github.hylexus.xtream.codec.core.type.FieldLength;
import io.github.hylexus.xtream.codec.core.type.simple.DataField;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class TLVCodecs {

    public static final TLVCodec INSTANCE = new TLVCodec();

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public static class TLVCodec implements FieldCodec<TLV> {
        private TLVCodec() {
        }

        @Override
        public @Nullable TLV deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            throw new UnsupportedOperationException("TLV codec does not support deserialization");
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable TLV value) {
            if (value == null) {
                return;
            }

            final DataField.DictKey tag = value.tag();

            // 1. 编码 tag
            tag.encode(output);

            // Length 是 sealed interface 并且只有一个实现类，所以强转是安全的
            final FieldLength.DefaultFieldLength valueLength = (FieldLength.DefaultFieldLength) value.length();
            final ByteBuf temp = context.bufferFactory().buffer();
            try {
                // 2. 编码 value
                context.entityEncoder().encode(value.value(), temp);
                valueLength.setValue(temp.readableBytes());

                // 3. 编码 value 长度
                valueLength.type().writeTo(output, valueLength.value());

                // 2. 编码 value
                output.writeBytes(temp);
            } finally {
                temp.release();
            }
        }

        @Override
        public void serializeWithTracker(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable TLV value) {
            if (value == null) {
                return;
            }
            final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
            final NestedFieldSpan nestedFieldSpan = codecTracker.startNewNestedFieldSpan("TLV", "Tag-Length-Value", "TLV", this.getClass().getSimpleName());
            final int indexBeforeWrite = output.writerIndex();
            final DataField.DictKey tag = value.tag();

            // 1. 编码 tag
            tag.encodeWithTracker(output, codecTracker);

            // Length 是 sealed interface 并且只有一个实现类，所以强转是安全的
            final FieldLength.DefaultFieldLength valueLength = (FieldLength.DefaultFieldLength) value.length();
            final ByteBuf temp = context.bufferFactory().buffer();
            try {
                // 2. 编码 value
                context.entityEncoder().encodeWithTracker(value.value(), temp, codecTracker);
                valueLength.setValue(temp.readableBytes());

                // 3. 编码 value 长度
                valueLength.type().writeToWithTracker(output, valueLength.value(), codecTracker);

                // 2. 编码 value
                output.writeBytes(temp);
            } finally {
                temp.release();
            }
            nestedFieldSpan.setHexString(FormatUtils.toHexString(output, indexBeforeWrite, output.writerIndex() - indexBeforeWrite));
            codecTracker.finishCurrentSpan();
        }

    }

}
