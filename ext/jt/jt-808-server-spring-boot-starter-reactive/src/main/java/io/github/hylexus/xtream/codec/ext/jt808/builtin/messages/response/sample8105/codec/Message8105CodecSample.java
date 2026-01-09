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
 * 当前类是从 <a href="https://github.com/SmallChi/JT808/blob/e79d18c3b465f5c450a1d6ba0edc6d9d51c7b13b/src/JT808.Protocol/MessageBody/JT808_0x8105.cs#L69">JT808_0x8105.cs#L69</a> 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on <a href="https://github.com/SmallChi/JT808/blob/e79d18c3b465f5c450a1d6ba0edc6d9d51c7b13b/src/JT808.Protocol/MessageBody/JT808_0x8105.cs#L69">JT808_0x8105.cs#L69</a>.
 *
 * @author hylexus
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
        int offset = 0;
        for (final CommandValue<Object> commandValue : sortedValue) {
            // 不连续的 offset ==> 填充 ";"
            while (offset != commandValue.offset()) {
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
        if (input.readableBytes() <= 0) {
            return null;
        }
        final int to = input.writerIndex();
        int from = input.readerIndex();
        int offset = 0;
        final List<CommandValue<Object>> result = new ArrayList<>();
        while (from <= to) {
            // 查找下一个分隔符
            final int indexOf = input.indexOf(from, to, (byte) ';');
            if (indexOf < 0) {
                break;
            }
            // 第一个就是分隔符 ==> 空值
            if (indexOf == 0) {
                result.add(new EmptyCommandValue<>(offset));
                from++;
            } else {
                // 切片
                final ByteBuf slice = input.slice(from, indexOf - from);
                // 通过偏移量创建对应的命令值实例
                @SuppressWarnings("unchecked") final CommandValue<Object> commandValue1 = (CommandValue<Object>) createCommandValueInstance(offset, slice);
                result.add(commandValue1);
                from += (indexOf - from) + 1;
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
