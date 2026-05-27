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

import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.netty.buffer.ByteBuf;

import java.util.StringJoiner;

@SuppressWarnings("NullAway.Init")
public abstract class AbstractCommandValue<T> implements CommandValue<T> {
    protected final int offset;
    protected T value;

    public AbstractCommandValue(int offset) {
        this.offset = offset;
    }

    public AbstractCommandValue(int offset, T value) {
        this.offset = CommandValue.checkOffset(offset);
        this.value = value;
    }

    @Override
    public int offset() {
        return offset;
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public CommandValue<T> readFrom(ByteBuf input) {
        if (input.readableBytes() <= 0) {
            return new EmptyCommandValue<>(offset);
        }
        return this.doReadFrom(input);
    }

    protected abstract CommandValue<T> doReadFrom(ByteBuf input);

    @Override
    public String toString() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
                .add("offset=" + offset)
                .add("value=" + value)
                .toString();
    }

    public static void writeSeparator(ByteBuf output) {
        output.writeCharSequence(";", XtreamConstants.CHARSET_GBK);
    }

}
