/*
 * Copyright (c) 2024 xtream-codec
 * xtream-codec is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package io.github.hylexus.xtream.codec.core.impl.codec;

import io.netty.buffer.ByteBuf;

public class I8FieldCodec extends AbstractFieldCodec<Byte> {
    public I8FieldCodec() {
    }

    @Override
    public Byte deserialize(DeserializeContext context, ByteBuf input, int length) {
        return input.readByte();
    }

    @Override
    protected void doSerialize(SerializeContext context, ByteBuf output, Byte value) {
        output.writeByte(value);
    }

    @Override
    public Class<?> underlyingJavaType() {
        return Byte.class;
    }
}
