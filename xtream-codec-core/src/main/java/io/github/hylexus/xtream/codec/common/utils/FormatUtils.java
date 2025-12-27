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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

/**
 * Copied from jt-framework.
 *
 * @author hylexus
 */
public final class FormatUtils {
    private FormatUtils() {
    }

    public static String toBinaryString(int i) {
        return Integer.toBinaryString(i);
    }

    public static String toBinaryString(int i, int minLen) {
        final String binaryString = Integer.toBinaryString(i);
        final int prefixLen = Math.max(minLen - binaryString.length(), 0);
        return "0".repeat(prefixLen) + binaryString;
    }

    public static String toHexString(byte i) {
        return Integer.toHexString(i & 0xFF);
    }

    public static String toHexString(byte i, int minLen) {
        final String hexString = Integer.toHexString(i & 0xFF);
        final int prefixLen = Math.max(minLen - hexString.length(), 0);
        return "0".repeat(prefixLen) + hexString;
    }

    public static String toHexString(int i) {
        return Integer.toHexString(i);
    }

    public static String toHexString(long l) {
        return Long.toHexString(l);
    }

    public static String toHexString(int i, int minLen) {
        final String hexString = Integer.toHexString(i);
        final int prefixLen = Math.max(minLen - hexString.length(), 0);
        return "0".repeat(prefixLen) + hexString;
    }

    public static String toHexString(long l, int minLen) {
        final String hexString = Long.toHexString(l);
        final int prefixLen = Math.max(minLen - hexString.length(), 0);
        return "0".repeat(prefixLen) + hexString;
    }

    public static String toHexString(ByteBuf byteBuf, int from, int length) {
        return ByteBufUtil.hexDump(byteBuf, from, length);
    }

    public static String toHexString(ByteBuf byteBuf, int length) {
        return ByteBufUtil.hexDump(byteBuf, byteBuf.readerIndex(), length);
    }

    public static String toHexString(ByteBuf byteBuf) {
        return ByteBufUtil.hexDump(byteBuf);
    }

    public static String toHexString(byte[] bytes) {
        return ByteBufUtil.hexDump(bytes);
    }

    public static String toString(ByteBuf byteBuf, int from, int length, String delimiter) {
        final StringBuilder sb = new StringBuilder();
        for (int i = from; i < length; i++) {
            sb.append(byteBuf.getByte(i)).append(delimiter);
        }
        return sb.toString();
    }

}
