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

package io.github.hylexus.xtream.codec.core.impl.codec.wrapper;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.type.wrapper.StringWrapperUtf8;
import io.netty.buffer.ByteBuf;

public class StringWrapperUtf8FieldCodec extends BaseDataWrapperFieldCodec<StringWrapperUtf8> {
    public static final StringWrapperUtf8FieldCodec INSTANCE = new StringWrapperUtf8FieldCodec();

    private StringWrapperUtf8FieldCodec() {
    }

    @Override
    public StringWrapperUtf8 deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        final CharSequence value = input.readCharSequence(length, XtreamConstants.CHARSET_UTF8);
        return new StringWrapperUtf8(value.toString());
    }

}