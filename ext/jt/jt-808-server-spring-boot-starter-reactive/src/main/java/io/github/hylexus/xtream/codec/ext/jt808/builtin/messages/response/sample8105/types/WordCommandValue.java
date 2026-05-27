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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.sample8105.types;

import io.netty.buffer.ByteBuf;

public class WordCommandValue extends AbstractCommandValue<Integer> {
    public WordCommandValue(int offset) {
        super(offset);
    }

    public WordCommandValue(int offset, Integer value) {
        super(offset, value);
    }

    @Override
    public void writeTo(ByteBuf output) {
        output.writeShort(value());
        writeSeparator(output);
    }

    @Override
    public WordCommandValue doReadFrom(ByteBuf input) {
        this.value = input.readUnsignedShort();
        return this;
    }
}
