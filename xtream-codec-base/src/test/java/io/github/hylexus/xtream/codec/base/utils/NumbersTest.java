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

package io.github.hylexus.xtream.codec.base.utils;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NumbersTest {

    // region getBitAt

    @Test
    void testGetBitAtZero() {
        // 0b1010 = 10
        assertEquals(0, Numbers.getBitAt(0b1010, 0));
        assertEquals(1, Numbers.getBitAt(0b1010, 1));
        assertEquals(0, Numbers.getBitAt(0b1010, 2));
        assertEquals(1, Numbers.getBitAt(0b1010, 3));
    }

    @Test
    void testGetBitAtAllZeros() {
        for (int i = 0; i < 64; i++) {
            assertEquals(0, Numbers.getBitAt(0L, i));
        }
    }

    @Test
    void testGetBitAtAllOnes() {
        long allOnes = -1L; // 0xFFFFFFFFFFFFFFFF
        for (int i = 0; i < 64; i++) {
            assertEquals(1, Numbers.getBitAt(allOnes, i));
        }
    }

    @Test
    void testGetBitAtHighBits() {
        // bit 63 set
        long value = Long.MIN_VALUE; // 0x8000000000000000
        assertEquals(1, Numbers.getBitAt(value, 63));
        assertEquals(0, Numbers.getBitAt(value, 62));
    }

    // endregion

    // region setBitAt

    @Test
    void testSetBitAt() {
        assertEquals(0b0001, Numbers.setBitAt(0, 0));
        assertEquals(0b0010, Numbers.setBitAt(0, 1));
        assertEquals(0b0100, Numbers.setBitAt(0, 2));
        assertEquals(0b1000, Numbers.setBitAt(0, 3));
    }

    @Test
    void testSetBitAtAlreadySet() {
        // setting a bit that's already 1 should not change anything
        assertEquals(0b1010, Numbers.setBitAt(0b1010, 1));
        assertEquals(0b1010, Numbers.setBitAt(0b1010, 3));
    }

    @Test
    void testSetBitAtHighBit() {
        long result = Numbers.setBitAt(0L, 63);
        assertEquals(Long.MIN_VALUE, result);
    }

    // endregion

    // region resetBitAt

    @Test
    void testResetBitAt() {
        assertEquals(0b1000, Numbers.resetBitAt(0b1010, 1));
        assertEquals(0b0010, Numbers.resetBitAt(0b1010, 3));
    }

    @Test
    void testResetBitAtAlreadyZero() {
        // resetting a bit that's already 0 should not change anything
        assertEquals(0b1010, Numbers.resetBitAt(0b1010, 0));
        assertEquals(0b1010, Numbers.resetBitAt(0b1010, 2));
    }

    @Test
    void testSetAndResetRoundTrip() {
        long original = 0L;
        long withBit = Numbers.setBitAt(original, 5);
        assertEquals(1, Numbers.getBitAt(withBit, 5));
        long reset = Numbers.resetBitAt(withBit, 5);
        assertEquals(original, reset);
    }

    // endregion

    // region intTo3Bytes

    @Test
    void testIntTo3BytesZero() {
        byte[] result = Numbers.intTo3Bytes(0);
        assertArrayEquals(new byte[]{0, 0, 0}, result);
    }

    @Test
    void testIntTo3BytesMaxThreeBytes() {
        // 0xFFFFFF = 16777215
        byte[] result = Numbers.intTo3Bytes(0xFFFFFF);
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, result);
    }

    @Test
    void testIntTo3Bytes() {
        // 0x010203
        byte[] result = Numbers.intTo3Bytes(0x010203);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03}, result);
    }

    @Test
    void testIntTo3BytesTruncatesHighByte() {
        // 0xFF010203 — the high byte (0xFF) should be discarded
        byte[] result = Numbers.intTo3Bytes(0xFF010203);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03}, result);
    }

    // endregion

    // region parseInteger(Object)

    @Test
    void testParseIntegerFromNull() {
        assertEquals(Optional.empty(), Numbers.parseInteger((Object) null));
    }

    @Test
    void testParseIntegerFromNumber() {
        assertEquals(Optional.of(42), Numbers.parseInteger((Object) 42));
        assertEquals(Optional.of(42), Numbers.parseInteger((Object) 42L));
        assertEquals(Optional.of(42), Numbers.parseInteger((Object) 42.9));
    }

    @Test
    void testParseIntegerFromString() {
        assertEquals(Optional.of(123), Numbers.parseInteger((Object) "123"));
        assertEquals(Optional.of(-1), Numbers.parseInteger((Object) "-1"));
    }

    @Test
    void testParseIntegerFromStringWithWhitespace() {
        assertEquals(Optional.of(42), Numbers.parseInteger((Object) "  42  "));
    }

    @Test
    void testParseIntegerFromInvalidString() {
        assertEquals(Optional.empty(), Numbers.parseInteger((Object) "abc"));
        assertEquals(Optional.empty(), Numbers.parseInteger((Object) ""));
    }

    @Test
    void testParseIntegerFromArbitraryObject() {
        // triggers: case Object s -> parseInteger(s.toString())
        Object obj = new Object() {
            @Override
            public String toString() {
                return "99";
            }
        };
        assertEquals(Optional.of(99), Numbers.parseInteger(obj));
    }

    @Test
    void testParseIntegerFromArbitraryObjectInvalid() {
        Object obj = new Object();
        assertEquals(Optional.empty(), Numbers.parseInteger(obj));
    }

    // endregion

    // region parseInteger(String)

    @Test
    void testParseIntegerString() {
        assertEquals(Optional.of(0), Numbers.parseInteger("0"));
        assertEquals(Optional.of(Integer.MAX_VALUE), Numbers.parseInteger(String.valueOf(Integer.MAX_VALUE)));
        assertEquals(Optional.of(Integer.MIN_VALUE), Numbers.parseInteger(String.valueOf(Integer.MIN_VALUE)));
    }

    @Test
    void testParseIntegerStringInvalid() {
        assertEquals(Optional.empty(), Numbers.parseInteger("not a number"));
        assertEquals(Optional.empty(), Numbers.parseInteger("12.34"));
    }

    @Test
    void testParseIntegerStringNull() {
        // null.trim() throws NullPointerException, triggering the catch branch
        assertEquals(Optional.empty(), Numbers.parseInteger((String) null));
    }

    // endregion

    // region parseBoolean(Object)

    @Test
    void testParseBooleanFromNull() {
        assertEquals(Optional.empty(), Numbers.parseBoolean((Object) null));
    }

    @Test
    void testParseBooleanFromBoolean() {
        assertEquals(Optional.of(true), Numbers.parseBoolean(true));
        assertEquals(Optional.of(false), Numbers.parseBoolean(false));
    }

    @Test
    void testParseBooleanFromString() {
        assertEquals(Optional.of(true), Numbers.parseBoolean((Object) "true"));
        assertEquals(Optional.of(false), Numbers.parseBoolean((Object) "false"));
        assertEquals(Optional.of(false), Numbers.parseBoolean((Object) "anything"));
    }

    @Test
    void testParseBooleanFromStringWithWhitespace() {
        assertEquals(Optional.of(true), Numbers.parseBoolean((Object) "  true  "));
    }

    @Test
    void testParseBooleanFromArbitraryObject() {
        // triggers: case Object o -> parseBoolean(o.toString())
        Object obj = new Object() {
            @Override
            public String toString() {
                return "true";
            }
        };
        assertEquals(Optional.of(true), Numbers.parseBoolean(obj));
    }

    @Test
    void testParseBooleanFromArbitraryObjectFalse() {
        Object obj = new Object();
        assertEquals(Optional.of(false), Numbers.parseBoolean(obj));
    }

    // endregion

    // region parseBoolean(String)

    @Test
    void testParseBooleanString() {
        assertEquals(Optional.of(true), Numbers.parseBoolean("true"));
        assertEquals(Optional.of(true), Numbers.parseBoolean("TRUE"));
        assertEquals(Optional.of(false), Numbers.parseBoolean("false"));
        assertEquals(Optional.of(false), Numbers.parseBoolean("other"));
    }

    @Test
    void testParseBooleanStringNull() {
        // null.trim() throws NullPointerException, triggering the catch branch
        assertEquals(Optional.empty(), Numbers.parseBoolean((String) null));
    }

    // endregion
}
