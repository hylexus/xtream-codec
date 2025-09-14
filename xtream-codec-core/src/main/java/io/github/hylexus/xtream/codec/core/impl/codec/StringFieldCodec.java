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

/**
 * @deprecated Use {@link StringFieldCodecs.StringFieldCodec} instead. Will be removed in 1.0.0.
 */
@Deprecated(forRemoval = true, since = "0.1.0")
public class StringFieldCodec implements FieldCodec<String> {
    /**
     * @deprecated Use {@link StringFieldCodecs#INSTANCE_BCD_8421} instead.
     */
    @Deprecated
    public static final FieldCodec<String> INSTANCE_BCD_8421 = InternalBcdFieldCodec.INSTANCE;
    /**
     * @deprecated Use {@link StringFieldCodecs#INSTANCE_UTF8} instead.
     */
    @Deprecated
    public static final FieldCodec<String> INSTANCE_UTF8 = InternalSimpleStringFieldCodec.INSTANCE_UTF8;
    /**
     * @deprecated Use {@link StringFieldCodecs#INSTANCE_GBK} instead.
     */
    @Deprecated
    public static final FieldCodec<String> INSTANCE_GBK = InternalSimpleStringFieldCodec.INSTANCE_GBK;
    /**
     * @deprecated Use {@link StringFieldCodecs#INSTANCE_GB_2312} instead.
     */
    @Deprecated
    public static final FieldCodec<String> INSTANCE_GB_2312 = InternalSimpleStringFieldCodec.INSTANCE_GB_2312;
    /**
     * @deprecated Use {@link StringFieldCodecs#INSTANCE_HEX} instead.
     */
    @Deprecated
    public static final FieldCodec<String> INSTANCE_HEX = InternalHexStringFieldCodec.INSTANCE;
    protected final String charset;

    private final FieldCodec<String> delegate;

    public StringFieldCodec(String charset) {
        this.charset = charset;
        this.delegate = createStringCodec(charset);
    }

    @Override
    public String deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        final int finalLength = length < 0
                ? input.readableBytes() // all remaining
                : length;
        if (finalLength <= 0) {
            return null;
        }
        return delegate.deserialize(propertyMetadata, context, input, finalLength);
    }

    @Override
    public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, String value) {
        if (value != null) {
            delegate.serialize(propertyMetadata, context, output, value);
        }
    }

    @Override
    public Class<?> underlyingJavaType() {
        return String.class;
    }

    public String getCharset() {
        return charset;
    }

    @Override
    public String toString() {
        return "StringFieldCodec{"
               + "charset='" + charset + '\''
               + '}';
    }


    /**
     * @deprecated Use {@link StringFieldCodecs#createStringCodecAndCastToObject(String)} instead.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static FieldCodec<Object> createStringCodecAndCastToObject(String charset) {
        final FieldCodec<?> stringCodec = createStringCodec(charset);
        return (FieldCodec<Object>) stringCodec;
    }


    /**
     * @deprecated Use {@link StringFieldCodecs#createStringCodec(String)} instead.
     */
    @Deprecated
    public static FieldCodec<String> createStringCodec(String charset) {
        return switch (charset.toUpperCase()) {
            case XtreamConstants.CHARSET_NAME_GBK -> StringFieldCodec.INSTANCE_GBK;
            case XtreamConstants.CHARSET_NAME_GB_2312 -> StringFieldCodec.INSTANCE_GB_2312;
            case XtreamConstants.CHARSET_NAME_UTF8 -> StringFieldCodec.INSTANCE_UTF8;
            case XtreamConstants.CHARSET_NAME_BCD_8421 -> StringFieldCodec.INSTANCE_BCD_8421;
            case XtreamConstants.CHARSET_NAME_HEX -> StringFieldCodec.INSTANCE_HEX;
            default -> {
                final Charset nomalCharset = Charset.forName(charset);
                yield new InternalSimpleStringFieldCodec(nomalCharset);
            }
        };
    }

    /**
     * @deprecated Use {@link StringFieldCodecs.StringFieldCodec} instead. Will be removed in 1.0.0.
     */
    @Deprecated(forRemoval = true, since = "0.1.0")
    public static class InternalSimpleStringFieldCodec extends AbstractFieldCodec<String> {
        private static final FieldCodec<String> INSTANCE_GBK = new InternalSimpleStringFieldCodec(XtreamConstants.CHARSET_GBK);
        private static final FieldCodec<String> INSTANCE_GB_2312 = new InternalSimpleStringFieldCodec(XtreamConstants.CHARSET_GB_2312);
        private static final FieldCodec<String> INSTANCE_UTF8 = new InternalSimpleStringFieldCodec(XtreamConstants.CHARSET_UTF8);
        private final Charset charset;

        public InternalSimpleStringFieldCodec(Charset charset) {
            this.charset = charset;
        }

        @Override
        public String deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            return input.readCharSequence(length, charset).toString();
        }

        /**
         * @see <a href="https://github.com/hylexus/xtream-codec/issues/2">https://github.com/hylexus/xtream-codec/issues/2</a>
         */
        @Override
        protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, String value) {
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

        public Charset getCharset() {
            return charset;
        }
    }

    /**
     * @deprecated Use {@link StringFieldCodecs.StringFieldCodec} instead. Will be removed in 1.0.0.
     */
    @Deprecated(forRemoval = true, since = "0.1.0")
    public static class InternalBcdFieldCodec extends AbstractFieldCodec<String> {
        public static final InternalBcdFieldCodec INSTANCE = new InternalBcdFieldCodec(XtreamConstants.CHARSET_NAME_BCD_8421);
        protected final String charset;

        public InternalBcdFieldCodec(String charset) {
            this.charset = charset;
        }

        @Override
        public String deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            return BcdOps.decodeBcd8421AsString(input, length);
        }

        @Override
        protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, String value) {
            BcdOps.encodeBcd8421StringIntoByteBuf(value, output);
        }

    }

    /**
     * @deprecated Use {@link StringFieldCodecs.StringFieldCodec} instead. Will be removed in 1.0.0.
     */
    @Deprecated(forRemoval = true, since = "0.1.0")
    public static class InternalHexStringFieldCodec extends AbstractFieldCodec<String> {
        public static final InternalHexStringFieldCodec INSTANCE = new InternalHexStringFieldCodec();

        @Override
        public String deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final String hexString = FormatUtils.toHexString(input, length);
            input.readerIndex(input.readerIndex() + length);
            return hexString;
        }

        @Override
        protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, String value) {
            XtreamBytes.writeHexString(output, value);
        }

    }
}
