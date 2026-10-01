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

package io.github.hylexus.xtream.codec.core.dynamic.engine;

import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicLayout.ByteFormat;

import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.*;

/**
 * 字节标量的值转换。此类只处理值与字节数组之间的映射；
 * 长度来源、容器边界及缓冲区游标由执行器管理。
 * 字符串使用严格的字符集编码与解码，拒绝非法或不可映射字符。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
final class DynamicByteFormat {
    private DynamicByteFormat() {
    }

    static byte[] encode(ByteFormat field, @Nullable Object value, int maxBytes) {
        if (value == null && !"padding".equals(field.type())) {
            throw new IllegalArgumentException("缺少必需字段值");
        }
        // BCD 按十进制数字对组成一个字节，HEX 则允许完整的 0..f 半字节。
        return switch (field.type()) {
            case "string" -> encodeString(field, (String) Objects.requireNonNull(value), maxBytes);
            case "hex" -> {
                final String text = (String) Objects.requireNonNull(value);
                requireSize(text.length() / 2L, maxBytes);
                yield HexFormat.of().parseHex(text);
            }
            case "bcd" -> {
                final String text = (String) Objects.requireNonNull(value);
                requireSize(text.length() / 2L, maxBytes);
                if (!text.matches("(?:[0-9]{2})*")) {
                    throw new IllegalArgumentException("BCD 必须为偶数位十进制字符");
                }
                yield HexFormat.of().parseHex(text);
            }
            case "bytes" -> {
                if (value instanceof byte[] bytes) {
                    requireSize(bytes.length, maxBytes);
                    yield bytes;
                }
                if (!(value instanceof List<?> list)) {
                    throw new IllegalArgumentException("bytes 值必须为字节数组或 0..255 数组");
                }
                requireSize(list.size(), maxBytes);
                final byte[] bytes = new byte[list.size()];
                for (int i = 0; i < bytes.length; i++) {
                    final int item = DynamicNumbers.requireInteger(list.get(i), "$", -1).intValueExact();
                    if (item < 0 || item > 255) {
                        throw new IllegalArgumentException("字节值超出 0..255");
                    }
                    bytes[i] = (byte) item;
                }
                yield bytes;
            }
            case "padding" -> new byte[0];
            default -> throw new IllegalArgumentException("不是字节标量");
        };
    }

    static @Nullable Object decode(ByteFormat field, byte[] bytes) {
        // 去填充必须发生在字符集解码前，否则填充字节可能构成非法字符序列。
        if (field.strip()) {
            final int pad = field.padByte();
            final boolean left = field.left();
            int start = 0;
            int end = bytes.length;
            if (left) {
                while (start < end && Byte.toUnsignedInt(bytes[start]) == pad) {
                    start++;
                }
            } else {
                while (end > start && Byte.toUnsignedInt(bytes[end - 1]) == pad) {
                    end--;
                }
            }
            bytes = Arrays.copyOfRange(bytes, start, end);
        }
        return switch (field.type()) {
            case "string" -> decodeString(field, bytes);
            case "hex" -> HexFormat.of().formatHex(bytes);
            case "bcd" -> {
                final String result = HexFormat.of().formatHex(bytes);
                if (!result.matches("[0-9]*")) {
                    throw new IllegalArgumentException("BCD 包含非法半字节");
                }
                yield result;
            }
            case "bytes" -> {
                final List<Integer> result = new ArrayList<>(bytes.length);
                for (final byte value : bytes) {
                    result.add(Byte.toUnsignedInt(value));
                }
                yield List.copyOf(result);
            }
            case "padding" -> {
                final int pad = field.padByte();
                for (final byte value : bytes) {
                    if (Byte.toUnsignedInt(value) != pad) {
                        throw new IllegalArgumentException("填充字节不匹配");
                    }
                }
                yield null;
            }
            default -> throw new IllegalArgumentException("不是字节标量");
        };
    }

    static byte[] pad(ByteFormat field, byte[] bytes, int length) {
        // 固定边界禁止静默截断；短值必须显式声明填充字节和方向。
        if (length < bytes.length) {
            throw new IllegalArgumentException("值的字节长度超过字段边界");
        }
        if (length == bytes.length) {
            return bytes;
        }
        if (!"padding".equals(field.type()) && !field.paddingAllowed()) {
            throw new IllegalArgumentException("值的字节长度不足且未声明 padByte");
        }
        final byte[] result = new byte[length];
        Arrays.fill(result, (byte) field.padByte());
        System.arraycopy(bytes, 0, result, field.left() ? length - bytes.length : 0, bytes.length);
        return result;
    }

    private static void requireSize(long size, int maxBytes) {
        if (size > maxBytes) {
            throw new IllegalArgumentException("值的字节长度超过字段或输出边界");
        }
    }

    private static Charset charset(ByteFormat field) {
        return field.charset();
    }

    private static byte[] encodeString(ByteFormat field, String value, int maxBytes) {
        // 逐步扩容且不超过字段边界；大输入不会在边界检查前生成完整编码结果。
        try {
            final CharsetEncoder encoder = charset(field)
                    .newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            final CharBuffer input = CharBuffer.wrap(value);
            ByteBuffer buffer = ByteBuffer.allocate(Math.min(maxBytes, Math.max(32, value.length())));
            while (true) {
                final CoderResult result = encoder.encode(input, buffer, true);
                if (result.isUnderflow()) {
                    break;
                }
                if (result.isError()) {
                    result.throwException();
                }
                buffer = grow(buffer, maxBytes);
            }
            while (true) {
                final CoderResult result = encoder.flush(buffer);
                if (result.isUnderflow()) {
                    break;
                }
                if (result.isError()) {
                    result.throwException();
                }
                buffer = grow(buffer, maxBytes);
            }
            buffer.flip();
            final byte[] result = new byte[buffer.remaining()];
            buffer.get(result);
            return result;
        } catch (CharacterCodingException exception) {
            throw new IllegalArgumentException("字符串包含字符集无法编码的字符", exception);
        }
    }

    private static ByteBuffer grow(ByteBuffer buffer, int maxBytes) {
        if (buffer.capacity() == maxBytes) {
            throw new IllegalArgumentException("值的字节长度超过字段或输出边界");
        }
        final int capacity = (int) Math.min(maxBytes, Math.max((long) buffer.capacity() + 1, buffer.capacity() * 2L));
        final ByteBuffer larger = ByteBuffer.allocate(capacity);
        buffer.flip();
        larger.put(buffer);
        return larger;
    }

    private static String decodeString(ByteFormat field, byte[] value) {
        try {
            return charset(field).newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(value)).toString();
        } catch (CharacterCodingException exception) {
            throw new IllegalArgumentException("输入包含非法字符编码", exception);
        }
    }
}
