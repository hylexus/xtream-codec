/*
 * MIT License
 *
 * Copyright (c) 2018 SmallChi(Koike)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Project modifications:
 * Modifications Copyright 2024-present the xtream-codec authors.
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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.sample8105.codec;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.sample8105.types.*;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 上游 MIT 全文保留于文件头及 licenses/source/；固定比较快照和改造范围见 THIRD-PARTY-SOURCE-NOTICES.txt。
 * <p>
 * 当前类是从 <a href="https://github.com/SmallChi/JT808/blob/e79d18c3b465f5c450a1d6ba0edc6d9d51c7b13b/src/JT808.Protocol/MessageBody/JT808_0x8105.cs#L69">JT808_0x8105.cs#L69</a> 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on <a href="https://github.com/SmallChi/JT808/blob/e79d18c3b465f5c450a1d6ba0edc6d9d51c7b13b/src/JT808.Protocol/MessageBody/JT808_0x8105.cs#L69">JT808_0x8105.cs#L69</a>.
 *
 * @author hylexus
 * @author Codex (AI)
 * @see <a href="https://github.com/SmallChi/JT808/blob/e79d18c3b465f5c450a1d6ba0edc6d9d51c7b13b/src/JT808.Protocol/MessageBody/JT808_0x8105.cs#L69">https://github.com/SmallChi/JT808/blob/e79d18c3b465f5c450a1d6ba0edc6d9d51c7b13b/src/JT808.Protocol/MessageBody/JT808_0x8105.cs#L69</a>
 */
public class Message8105CodecSample implements FieldCodec<List<CommandValue<Object>>> {

    @Override
    public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable List<CommandValue<Object>> value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        // 按照偏移量排序
        final List<CommandValue<Object>> sortedValue = value.stream().sorted(Comparator.comparingInt(CommandValue::offset)).toList();
        // 范围: [0,12]
        CommandValue.checkOffset(sortedValue.getFirst().offset());
        CommandValue.checkOffset(sortedValue.getLast().offset());
        for (int i = 1; i < sortedValue.size(); i++) {
            if (sortedValue.get(i - 1).offset() == sortedValue.get(i).offset()) {
                throw new IllegalArgumentException("Duplicate offset: " + sortedValue.get(i).offset());
            }
        }
        int offset = 0;
        for (final CommandValue<Object> commandValue : sortedValue) {
            // 不连续的 offset ==> 填充 ";"
            while (offset < commandValue.offset()) {
                AbstractCommandValue.writeSeparator(output);
                offset++;
            }
            commandValue.writeTo(output);
            offset++;
        }
        // 剩余的空值 ==> 使用 ";" 填充
        while (offset <= 12) {
            AbstractCommandValue.writeSeparator(output);
            offset++;
        }
    }

    @Override
    public @Nullable List<CommandValue<Object>> deserialize(BeanPropertyMetadata propertyMetadata, FieldCodec.DeserializeContext context, ByteBuf input, int length) {
        final ByteBuf slice = length < 0 ? input : input.readSlice(length);
        if (!slice.isReadable()) {
            return null;
        }
        int offset = 0;
        final List<CommandValue<Object>> result = new ArrayList<>();
        while (slice.isReadable()) {
            CommandValue.checkOffset(offset);
            // 第一个就是分隔符 ==> 空值
            if (slice.getByte(slice.readerIndex()) == ';') {
                result.add(new EmptyCommandValue<>(offset));
                slice.skipBytes(1);
            } else {
                // 数值字段按固定宽度读取，避免把数值中的 0x3b 当成分隔符
                final int valueLength = switch (offset) {
                    // BYTE
                    case 0 -> 1;
                    // WORD
                    case 5, 6, 12 -> 2;
                    default -> slice.indexOf(slice.readerIndex(), slice.writerIndex(), (byte) ';') - slice.readerIndex();
                };
                if (valueLength < 0
                        || slice.readableBytes() <= valueLength
                        || slice.getByte(slice.readerIndex() + valueLength) != ';') {
                    throw new IllegalArgumentException("Missing separator at offset: " + offset);
                }
                // 通过偏移量创建对应的命令值实例
                @SuppressWarnings("unchecked") final CommandValue<Object> commandValue = (CommandValue<Object>) createCommandValueInstance(offset, slice.readSlice(valueLength));
                result.add(commandValue);
                slice.skipBytes(1);
            }
            offset++;
        }
        return result;
    }

    CommandValue<?> createCommandValueInstance(int offset, ByteBuf input) {
        return switch (offset) {
            case 0 -> new ByteCommandValue(offset).readFrom(input);
            case 1, 2, 3, 4, 7, 8, 9, 10, 11 -> new StringCommandValue(offset).readFrom(input);
            case 5, 6, 12 -> new WordCommandValue(offset).readFrom(input);
            default -> throw new IllegalArgumentException("Invalid offset: " + offset);
        };
    }
}
