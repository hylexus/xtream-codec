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

package io.github.hylexus.xtream.codec.core.type;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;

import java.util.Optional;

public sealed interface FieldLength permits FieldLength.DefaultFieldLength {

    LengthType type();

    // value 没有暴露 set 方法；
    // 使用方也没必要手动 setValue
    // 编解码器会负责设置 value
    // 定义为 long 类型是兼容性考虑：u32, i64(虽然协议中不太可能用这么大范围的长度表示某个字段的字节数)
    long value();

    default Optional<Long> getValue() {
        if (this.initialized()) {
            return Optional.of(this.value());
        }
        return Optional.empty();
    }

    boolean initialized();

    enum LengthType {
        i8, u8, i16, u16, i32, u32, i64;

        public void writeToWithTracker(ByteBuf output, long value, CodecTracker codecTracker) {
            final int writerIndex = output.writerIndex();
            this.writeTo(output, value);
            final String hexString = FormatUtils.toHexString(output, writerIndex, output.writerIndex() - writerIndex);
            codecTracker.addFieldSpan(codecTracker.getCurrentSpan(), "length", value, hexString, this.getDeclaringClass().getSimpleName(), "");
        }

        public void writeTo(ByteBuf output, long value) {
            switch (this) {
                case i8, u8 -> output.writeByte((int) value);
                case i16, u16 -> output.writeShort((int) value);
                case i32, u32 -> output.writeInt((int) value);
                case i64 -> output.writeLong(value);
                default -> throw new IllegalArgumentException("Unsupported length type: " + this);
            }
        }
    }

    // 只有这个一个实现类
    final class DefaultFieldLength implements FieldLength {
        private final LengthType type;
        private long value;
        private boolean initialized = false;

        public DefaultFieldLength(LengthType type) {
            this.type = type;
        }

        @Override
        public long value() {
            return this.value;
        }

        public void setValue(long value) {
            this.value = value;
            this.initialized = true;
        }

        @Override
        public boolean initialized() {
            return this.initialized;
        }

        @Override
        public LengthType type() {
            return this.type;
        }
    }
}
