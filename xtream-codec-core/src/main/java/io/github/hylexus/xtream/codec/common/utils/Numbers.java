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

import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class Numbers {
    private static final int MASK = 0xFF;

    private Numbers() {
    }

    public static int getBitAt(long value, int index) {
        return (int) ((1L << index & value) >>> index);
    }

    public static long setBitAt(long value, int index) {
        return 1L << index | value;
    }

    public static long resetBitAt(long value, int index) {
        return value & ~(1L << index);
    }

    public static byte[] intTo3Bytes(int value) {
        return new byte[]{
                (byte) ((value >>> 16) & MASK),
                (byte) ((value >>> 8) & MASK),
                (byte) (value & MASK)
        };
    }

    public static Optional<Integer> parseInteger(@Nullable Object input) {
        return switch (input) {
            case null -> Optional.empty();
            case Number number -> Optional.of(number.intValue());
            case String s -> parseInteger(s);
            case Object s -> parseInteger(s.toString());
        };
    }

    public static Optional<Integer> parseInteger(@Nullable String input) {
        try {
            if (input == null) {
                return Optional.empty();
            }
            return Optional.of(Integer.parseInt(input.trim()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<Boolean> parseBoolean(@Nullable Object input) {
        return switch (input) {
            case null -> Optional.empty();
            case Boolean bool -> Optional.of(bool);
            case String s -> parseBoolean(s);
            case Object o -> parseBoolean(o.toString());
        };
    }

    public static Optional<Boolean> parseBoolean(@Nullable String input) {
        try {
            if (input == null) {
                return Optional.empty();
            }
            return Optional.of(Boolean.parseBoolean(input.trim()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
