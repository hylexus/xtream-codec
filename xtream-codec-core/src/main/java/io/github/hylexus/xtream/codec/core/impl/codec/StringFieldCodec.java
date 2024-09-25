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

import io.github.hylexus.xtream.codec.common.utils.BcdOps;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class StringFieldCodec implements FieldCodec<String> {
    protected final String charset;

    private final FieldCodec<String> delegate;

    public StringFieldCodec(String charset) {
        this.charset = charset;
        this.delegate = initDelegateCodec(charset);
    }

    @Override
    public String deserialize(DeserializeContext context, ByteBuf input, int length) {
        final int finalLength = length < 0
                ? input.readableBytes() // all remaining
                : length;
        return delegate.deserialize(context, input, finalLength);
    }

    @Override
    public void serialize(SerializeContext context, ByteBuf output, String value) {
        delegate.serialize(context, output, value);
    }

    @Override
    public Class<?> underlyingJavaType() {
        return String.class;
    }

    @Override
    public String toString() {
        return "StringFieldCodec{"
                + "charset='" + charset + '\''
                + '}';
    }

    public static FieldCodec<String> initDelegateCodec(String charset) {
        if (charset.equalsIgnoreCase("bcd_8421")) {
            return new InternalBcdFieldCodec(charset);
        }
        final Charset nomalCharset = Charset.forName(charset);
        return new InternalSimpleStringFieldCodec(nomalCharset);
    }

    static class InternalSimpleStringFieldCodec extends AbstractFieldCodec<String> {

        private final Charset charset;

        public InternalSimpleStringFieldCodec(Charset charset) {
            this.charset = charset;
        }

        @Override
        public String deserialize(DeserializeContext context, ByteBuf input, int length) {
            return input.readCharSequence(length, charset).toString();
        }

        @Override
        protected void doSerialize(SerializeContext context, ByteBuf output, String value) {
            output.writeCharSequence(value, charset);
        }
    }

    static class InternalBcdFieldCodec extends AbstractFieldCodec<String> {
        protected final String charset;

        public InternalBcdFieldCodec(String charset) {
            this.charset = charset;
        }

        @Override
        public String deserialize(DeserializeContext context, ByteBuf input, int length) {
            return BcdOps.decodeBcd8421AsString(input, length);
        }

        @Override
        protected void doSerialize(SerializeContext context, ByteBuf output, String value) {
            BcdOps.encodeBcd8421StringIntoByteBuf(value, output);
        }

    }
}
