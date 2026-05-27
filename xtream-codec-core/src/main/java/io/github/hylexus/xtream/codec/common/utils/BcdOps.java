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

package io.github.hylexus.xtream.codec.common.utils;

import io.netty.buffer.ByteBuf;

/**
 * Copied from jt-framework.
 *
 * @author hylexus
 */
public abstract class BcdOps {

    public static String decodeBcd8421AsString(ByteBuf source, final int length) {
        final StringBuilder builder = new StringBuilder(length << 2);
        for (int i = 0; i < length; i++) {
            final byte value = source.readByte();
            builder.append((value & 0xf0) >>> 4);
            builder.append(value & 0x0f);
        }
        return builder.toString();
    }

    public static String decodeBcd8421AsString(final byte[] source, final int start, final int end) {
        assert start < end : "start < end";

        final int length = end - start;
        final StringBuilder builder = new StringBuilder(length << 2);
        for (int i = start; i < end; i++) {
            builder.append((source[i] & 0xf0) >>> 4);
            builder.append(source[i] & 0x0f);
        }
        return builder.toString();
    }

    public static void encodeBcd8421StringIntoByteBuf(final String str, ByteBuf target) {
        // odd -> 0 + str
        final String source = (str.length() & 1) == 1
                ? "0" + str
                : str;

        final int length = source.length() >> 1;

        final byte[] sourceBytes = source.getBytes();
        for (int i = 0; i < length; i++) {

            byte high = asciiToBcd(sourceBytes[i << 1]);
            byte low = asciiToBcd(sourceBytes[(i << 1) + 1]);

            byte value = (byte) ((high << 4) | low);

            target.writeByte(value);
        }
    }

    public static byte[] encodeBcd8421AsBytes(final String str) {
        // odd -> 0 + str
        final String source = (str.length() & 1) == 1
                ? "0" + str
                : str;

        final byte[] result = new byte[source.length() >> 1];
        final byte[] sourceBytes = source.getBytes();
        for (int i = 0; i < result.length; i++) {

            byte high = asciiToBcd(sourceBytes[i << 1]);
            byte low = asciiToBcd(sourceBytes[(i << 1) + 1]);

            result[i] = (byte) ((high << 4) | low);
        }
        return result;
    }

    private static byte asciiToBcd(final byte asc) {
        if ((asc >= '0') && (asc <= '9')) {
            return (byte) (asc - '0');
        } else if ((asc >= 'A') && (asc <= 'F')) {
            return (byte) (asc - 'A' + 10);
        } else if ((asc >= 'a') && (asc <= 'f')) {
            return (byte) (asc - 'a' + 10);
        } else {
            return (byte) (asc - 48);
        }
    }

    public static String bcd2StringV2(byte[] bytes) {
        return bcd2StringV2(bytes, 0, bytes.length);
    }

    public static String bcd2StringV2(byte[] bytes, int start, int end) {
        assert start < end : "start < end";

        int length = end - start;
        StringBuilder builder = new StringBuilder(length << 2);

        for (int i = start; i < end; ++i) {
            builder.append((bytes[i] & 240) >>> 4);
            builder.append(bytes[i] & 15);
        }

        return builder.toString();
    }
}
