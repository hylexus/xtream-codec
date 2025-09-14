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

package io.github.hylexus.xtream.codec.core.impl.codec;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.utils.BcdOps;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.Padding;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public final class StringFieldCodecs {

    public static final FieldCodec<String> INSTANCE_UTF8 = new StringFieldCodecUtf8();
    public static final FieldCodec<String> INSTANCE_GBK = new StringFieldCodecGbk();
    public static final FieldCodec<String> INSTANCE_GB_2312 = new StringFieldCodecGb2312();
    public static final FieldCodec<String> INSTANCE_BCD_8421 = new StringFieldCodecBcd8421();
    public static final FieldCodec<String> INSTANCE_HEX = new StringFieldCodecHex();

    private StringFieldCodecs() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public static FieldCodec<Object> createStringCodecAndCastToObject(String charset) {
        final FieldCodec<?> stringCodec = createStringCodec(charset);
        return (FieldCodec<Object>) stringCodec;
    }

    public static FieldCodec<String> createStringCodec(String charset) {
        return switch (charset.toUpperCase()) {
            case XtreamConstants.CHARSET_NAME_GBK -> INSTANCE_GBK;
            case XtreamConstants.CHARSET_NAME_GB_2312 -> INSTANCE_GB_2312;
            case XtreamConstants.CHARSET_NAME_UTF8 -> INSTANCE_UTF8;
            case XtreamConstants.CHARSET_NAME_BCD_8421 -> INSTANCE_BCD_8421;
            case XtreamConstants.CHARSET_NAME_HEX -> INSTANCE_HEX;
            default -> {
                final Charset nomalCharset = Charset.forName(charset);
                yield new StringFieldCodec(nomalCharset);
            }
        };
    }

    public static final class StringFieldCodecBcd8421 implements CharSequenceFieldCodec<String> {
        private StringFieldCodecBcd8421() {
        }

        @Override
        public String deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            return BcdOps.decodeBcd8421AsString(input, length);
        }

        @Override
        public void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, String value) {
            BcdOps.encodeBcd8421StringIntoByteBuf(value, output);
        }

    }

    public static final class StringFieldCodecHex implements CharSequenceFieldCodec<String> {
        private StringFieldCodecHex() {
        }

        @Override
        public String deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final String hexString = FormatUtils.toHexString(input, length);
            input.readerIndex(input.readerIndex() + length);
            return hexString;
        }

        @Override
        public void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, String value) {
            XtreamBytes.writeHexString(output, value);
        }
    }

    public static final class StringFieldCodecUtf8 extends StringFieldCodec {
        private StringFieldCodecUtf8() {
            super(XtreamConstants.CHARSET_UTF8);
        }
    }

    public static final class StringFieldCodecGbk extends StringFieldCodec {
        private StringFieldCodecGbk() {
            super(XtreamConstants.CHARSET_GBK);
        }
    }

    public static final class StringFieldCodecGb2312 extends StringFieldCodec {
        private StringFieldCodecGb2312() {
            super(XtreamConstants.CHARSET_GB_2312);
        }
    }

    public static class StringFieldCodec implements CharSequenceFieldCodec<String> {
        private final Charset charset;

        public StringFieldCodec(Charset charset) {
            this.charset = charset;
        }

        @Override
        public String deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final int finalLength = length < 0
                    ? input.readableBytes() // all remaining
                    : length;
            if (finalLength <= 0) {
                return null;
            }
            return input.readCharSequence(finalLength, charset).toString();
        }

        /**
         * @see <a href="https://github.com/hylexus/xtream-codec/issues/2">https://github.com/hylexus/xtream-codec/issues/2</a>
         */
        @Override
        public void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, String value) {
            final XtreamField xtreamField = propertyMetadata.xtreamFieldAnnotation();
            if (xtreamField.paddingLeft().minEncodedLength() > 0) {
                final Padding padding = xtreamField.paddingLeft();
                XtreamBytes.writeCharSequenceWithLeftPadding(output, value, charset, padding.minEncodedLength(), padding.paddingElement());
            } else if (xtreamField.paddingRight().minEncodedLength() > 0) {
                final Padding padding = xtreamField.paddingRight();
                XtreamBytes.writeCharSequenceWithRightPadding(output, value, charset, padding.minEncodedLength(), padding.paddingElement());
            } else {
                output.writeCharSequence(value, charset);
            }
        }

        public Charset charset() {
            return charset;
        }
    }
}
