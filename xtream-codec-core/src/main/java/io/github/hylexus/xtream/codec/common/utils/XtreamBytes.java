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

package io.github.hylexus.xtream.codec.common.utils;

import io.github.hylexus.xtream.codec.core.annotation.PaddingType;
import io.github.hylexus.xtream.codec.core.type.PaddingConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ReferenceCounted;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author hylexus
 */
public class XtreamBytes {

    private static final char[] DIGITS_HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private static final byte[] HEX_DECODE_TABLE = new byte[256];

    static {
        // -1 表示无效字符
        Arrays.fill(HEX_DECODE_TABLE, (byte) -1);
        for (int i = 0; i < 10; i++) {
            HEX_DECODE_TABLE['0' + i] = (byte) i;
        }
        for (int i = 0; i < 6; i++) {
            HEX_DECODE_TABLE['A' + i] = (byte) (10 + i);
            HEX_DECODE_TABLE['a' + i] = (byte) (10 + i);
        }
    }

    public static String randomString(int length) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }

    public static void releaseBufList(@Nullable Collection<? extends ReferenceCounted> components) {
        if (components == null || components.isEmpty()) {
            return;
        }
        for (final ReferenceCounted component : components) {
            releaseBuf(component);
        }
    }

    public static void releaseBuf(@Nullable ReferenceCounted objects) {
        if (objects == null) {
            return;
        }
        if (objects.refCnt() > 0) {
            objects.release();
        }
    }

    public static ByteBuf byteBufFromHexString(ByteBufAllocator allocator, @Nullable String hexString) {
        final ByteBuf buffer = allocator.buffer();
        return byteBufFromHexString(buffer, hexString);
    }

    public static ByteBuf byteBufFromHexString(ByteBuf buffer, @Nullable String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return buffer;
        }

        final String trimmed = hexString.trim();
        final int len = trimmed.length();
        if (len == 0) {
            return buffer;
        }

        if ((len & 1) != 0) {
            throw new IllegalArgumentException("Hex string must have even length: \"" + hexString + "\"");
        }

        for (int i = 0; i < len; i += 2) {
            final char highChar = trimmed.charAt(i);
            final char lowChar = trimmed.charAt(i + 1);

            final int high = HEX_DECODE_TABLE[highChar] & 0xFF;
            final int low = HEX_DECODE_TABLE[lowChar] & 0xFF;

            if (high > 15 || low > 15) {
                throw new IllegalArgumentException("Invalid hex character at index " + i + ": \"" + hexString + "\"");
            }

            buffer.writeByte((byte) ((high << 4) | low));
        }
        return buffer;
    }

    public static String encodeHex(ByteBuf byteBuf) {
        final StringBuilder builder = new StringBuilder();
        int readableBytes = byteBuf.readableBytes();
        for (int i = 0; i < readableBytes; i++) {
            final byte b = byteBuf.getByte(i);
            builder.append(DIGITS_HEX[(0xF0 & b) >>> 4])
                    .append(DIGITS_HEX[0x0F & b]);
        }
        return builder.toString();
    }

    public static String encodeHex(ByteBuf byteBuf, int start, int length) {
        final StringBuilder builder = new StringBuilder();

        for (int i = start; i < length; i++) {
            final byte b = byteBuf.getByte(i);
            builder.append(DIGITS_HEX[(0xF0 & b) >>> 4])
                    .append(DIGITS_HEX[0x0F & b]);
        }
        return builder.toString();
    }

    public static byte[] decodeHex(String hex) {
        return decodeHex(hex.toCharArray());
    }

    /**
     * 将十六进制字符数组解码为字节数组
     *
     * @param data 十六进制字符数组，必须为偶数长度，仅包含 [0-9a-fA-F]
     * @return 解码后的字节数组
     * @throws IllegalArgumentException 如果输入为 null、长度为奇数或包含非法字符
     */
    public static byte[] decodeHex(char @Nullable [] data) {
        if (data == null) {
            throw new IllegalArgumentException("Input char array must not be null");
        }

        final int len = data.length;
        if ((len & 1) != 0) {
            throw new IllegalArgumentException("字符个数应该为偶数，实际长度: " + len);
        }

        if (len == 0) {
            return new byte[0];
        }

        // len / 2
        final byte[] out = new byte[len >> 1];

        for (int i = 0, j = 0; j < len; i++) {
            final char hiChar = data[j++];
            final char loChar = data[j++];

            final int high = HEX_DECODE_TABLE[hiChar] & 0xFF;
            final int low = HEX_DECODE_TABLE[loChar] & 0xFF;

            if (high > 15 || low > 15) {
                // 确定是哪个位置出错
                final char badChar = high > 15 ? hiChar : loChar;
                final int badIndex = high > 15 ? j - 2 : j - 1;
                throw new IllegalArgumentException("非法的十六进制字符 '" + badChar + "', 位置: " + badIndex);
            }

            out[i] = (byte) ((high << 4) | low);
        }

        return out;
    }

    public static byte[] readBytes(ByteBuf byteBuf, int length) {
        final byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    public static byte[] prefixZero(byte[] bytes, int length) {
        if (bytes.length >= length) {
            return bytes;
        }
        final byte[] newBytes = new byte[length];
        System.arraycopy(bytes, 0, newBytes, length - bytes.length, bytes.length);
        return newBytes;
    }

    public static byte[] getBytes(ByteBuf byteBuf, int startIndex, int length) {
        final byte[] bytes = new byte[length];
        byteBuf.getBytes(startIndex, bytes, 0, length);
        return bytes;
    }

    public static byte[] getBytes(ByteBuf byteBuf) {
        return getBytes(byteBuf, byteBuf.readerIndex(), byteBuf.readableBytes());
    }

    public static int getWord(ByteBuf byteBuf, int start) {
        return byteBuf.getUnsignedShort(start);
    }

    public static String getBcd(ByteBuf byteBuf, int startIndex, int length) {
        final byte[] bytes = getBytes(byteBuf, startIndex, length);
        return BcdOps.bcd2StringV2(bytes);
    }

    public static String getString(ByteBuf byteBuf, int length, Charset charset) {
        final CharSequence charSequence = byteBuf.getCharSequence(0, length, charset);
        return charSequence.toString();
    }

    public static short readU8(ByteBuf readable) {
        return readable.readUnsignedByte();
    }

    public static int readU16(ByteBuf readable) {
        return readable.readUnsignedShort();
    }

    public static String readBcd(ByteBuf readable, int length) {
        return BcdOps.decodeBcd8421AsString(readable, length);
    }

    public static void writeBcd8421(ByteBuf buffer, String bcdString, PaddingConfig paddingConfig) {
        final PaddingType paddingType = paddingConfig.paddingType();
        switch (paddingType) {
            case null -> BcdOps.encodeBcd8421StringIntoByteBuf(bcdString, buffer);
            case NONE -> BcdOps.encodeBcd8421StringIntoByteBuf(bcdString, buffer);
            case LEFT -> writeBcd8421WithLeftPadding(buffer, bcdString, paddingConfig.minEncodedLength(), paddingConfig.paddingElement());
            case RIGHT -> writeBcd8421WithRightPadding(buffer, bcdString, paddingConfig.minEncodedLength(), paddingConfig.paddingElement());
        }
    }

    public static void writeBcd8421WithLeftPadding(ByteBuf buffer, String bcdString, int minLength, byte paddingElement) {
        final int writerIndex = buffer.writerIndex();
        BcdOps.encodeBcd8421StringIntoByteBuf(bcdString, buffer);
        final int valueLength = buffer.writerIndex() - writerIndex;
        final int delta = minLength - valueLength;
        if (delta > 0) {
            // 重置写指针 从头开始覆盖
            buffer.writerIndex(writerIndex);
            writePadding(buffer, paddingElement, delta);
            BcdOps.encodeBcd8421StringIntoByteBuf(bcdString, buffer);
        }
    }

    public static void writeBcd8421WithRightPadding(ByteBuf buffer, String bcdString, int minLength, byte paddingElement) {
        final int writerIndex = buffer.writerIndex();
        BcdOps.encodeBcd8421StringIntoByteBuf(bcdString, buffer);
        final int valueLength = buffer.writerIndex() - writerIndex;
        final int delta = minLength - valueLength;
        if (delta > 0) {
            // 尾部追加填充元素
            writePadding(buffer, paddingElement, delta);
        }
    }

    public static void writeCharSequence(ByteBuf buffer, CharSequence charSequence, Charset charset, PaddingConfig paddingConfig) {
        final PaddingType paddingType = paddingConfig.paddingType();
        switch (paddingType) {
            case null -> buffer.writeCharSequence(charSequence, charset);
            case NONE -> buffer.writeCharSequence(charSequence, charset);
            case LEFT -> writeCharSequenceWithLeftPadding(buffer, charSequence, charset, paddingConfig.minEncodedLength(), paddingConfig.paddingElement());
            case RIGHT -> writeCharSequenceWithRightPadding(buffer, charSequence, charset, paddingConfig.minEncodedLength(), paddingConfig.paddingElement());
        }
    }

    public static void writeCharSequenceWithLeftPadding(ByteBuf buffer, CharSequence charSequence, Charset charset, int minLength, byte paddingElement) {
        final int writerIndex = buffer.writerIndex();
        final int valueLength = buffer.writeCharSequence(charSequence, charset);
        final int delta = minLength - valueLength;
        if (delta > 0) {
            // 重置写指针 从头开始覆盖
            buffer.writerIndex(writerIndex);
            writePadding(buffer, paddingElement, delta);
            buffer.writeCharSequence(charSequence, charset);
        }
    }

    public static void writeCharSequenceWithRightPadding(ByteBuf buffer, CharSequence charSequence, Charset charset, int minLength, byte paddingElement) {
        final int valueLength = buffer.writeCharSequence(charSequence, charset);
        final int delta = minLength - valueLength;
        if (delta > 0) {
            // 尾部追加填充元素
            writePadding(buffer, paddingElement, delta);
        }
    }

    private static void writePadding(ByteBuf buffer, byte paddingElement, int delta) {
        for (int i = 0; i < delta; i++) {
            buffer.writeByte(paddingElement);
        }
    }

    public static ByteBuf writeWord(ByteBuf byteBuf, int value) {
        return byteBuf.writeShort(value);
    }

    public static ByteBuf writeHexString(ByteBuf byteBuf, @Nullable String value) {
        if (value == null || value.isEmpty()) {
            return byteBuf;
        }

        // 去除可能的空格
        value = value.trim();

        if ((value.length() & 1) != 0) {
            throw new IllegalArgumentException("Hex string must have even length: " + value);
        }

        final int len = value.length();
        for (int i = 0; i < len; i += 2) {
            final int high = HEX_DECODE_TABLE[value.charAt(i)] & 0xFF;
            final int low = HEX_DECODE_TABLE[value.charAt(i + 1)] & 0xFF;
            if (high > 15 || low > 15) {
                throw new IllegalArgumentException("Invalid hex char in: " + value);
            }
            byteBuf.writeByte((byte) ((high << 4) | low));
        }

        return byteBuf;
    }

    public static void writeHexString(ByteBuf byteBuf, String value, PaddingConfig paddingConfig) {
        final PaddingType paddingType = paddingConfig.paddingType();
        switch (paddingType) {
            case null -> writeHexString(byteBuf, value);
            case NONE -> writeHexString(byteBuf, value);
            case LEFT -> writeHexStringWithLeftPadding(byteBuf, value, paddingConfig.minEncodedLength(), paddingConfig.paddingElement());
            case RIGHT -> writeHexStringWithRightPadding(byteBuf, value, paddingConfig.minEncodedLength(), paddingConfig.paddingElement());
        }
    }

    public static void writeHexStringWithLeftPadding(ByteBuf buffer, String hexString, int minLength, byte paddingElement) {
        final int writerIndex = buffer.writerIndex();
        writeHexString(buffer, hexString);
        final int valueLength = buffer.writerIndex() - writerIndex;
        final int delta = minLength - valueLength;
        if (delta > 0) {
            // 重置写指针 从头开始覆盖
            buffer.writerIndex(writerIndex);
            writePadding(buffer, paddingElement, delta);
            writeHexString(buffer, hexString);
        }
    }

    public static void writeHexStringWithRightPadding(ByteBuf buffer, String hexString, int minLength, byte paddingElement) {
        final int writerIndex = buffer.writerIndex();
        writeHexString(buffer, hexString);
        final int valueLength = buffer.writerIndex() - writerIndex;
        final int delta = minLength - valueLength;
        if (delta > 0) {
            // 尾部追加填充元素
            writePadding(buffer, paddingElement, delta);
        }
    }

    public static ByteBuf writeBcd(ByteBuf byteBuf, String value) {
        BcdOps.encodeBcd8421StringIntoByteBuf(value, byteBuf);
        return byteBuf;
    }

    /**
     * 给 {@code input} 末尾填充 {@code placement} 直到长度为 {@code maxLength}
     *
     * @param input     待填充的字节数组
     * @param maxLength 填充后的长度
     * @param placement 要填充的字节
     * @return 填充后的字节数组
     */
    public static byte[] appendSuffixIfNecessary(byte[] input, int maxLength, byte placement) {
        if (input.length >= maxLength) {
            return input;
        }
        final byte[] newBytes = new byte[maxLength];
        System.arraycopy(input, 0, newBytes, 0, input.length);
        Arrays.fill(newBytes, input.length, maxLength, placement);
        return newBytes;
    }

    /**
     * 移除 {@code input} 末尾的 {@code b}
     *
     * @param input 待处理的字符串
     * @param b     要移除的默认字节
     */
    public static String trimTailing(String input, byte b) {
        final byte[] bytes = input.getBytes();
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == b) {
            i--;
        }
        return input.substring(0, i + 1);
    }

    public static int gbkByteCount(String str) {
        return str.getBytes(XtreamConstants.CHARSET_GBK).length;
    }

}
