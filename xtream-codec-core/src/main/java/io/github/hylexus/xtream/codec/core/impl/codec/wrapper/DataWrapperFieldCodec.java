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
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.core.impl.codec.AbstractFieldCodec;
import io.github.hylexus.xtream.codec.core.type.wrapper.BytesDataWrapper;
import io.github.hylexus.xtream.codec.core.type.wrapper.DataWrapper;
import io.netty.buffer.ByteBuf;

/**
 * @author hylexus
 * @deprecated Use {@link io.github.hylexus.xtream.codec.core.impl.codec.DataWrapperFieldCodes.DataWrapperFieldCodec} instead.
 */
@SuppressWarnings("rawtypes")
@Deprecated
public class DataWrapperFieldCodec extends AbstractFieldCodec<DataWrapper> {

    /**
     * @deprecated Use {@link io.github.hylexus.xtream.codec.core.impl.codec.DataWrapperFieldCodes#INSTANCE} instead.
     */
    @Deprecated
    public static final DataWrapperFieldCodec INSTANCE = new DataWrapperFieldCodec();

    private DataWrapperFieldCodec() {
    }

    @Override
    protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, DataWrapper value) {
        value.writeTo(output);
    }

    @Override
    public DataWrapper deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        final byte[] bytes = XtreamBytes.readBytes(input, length);
        return new BytesDataWrapper(bytes);
    }
}
