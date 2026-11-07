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

package io.github.hylexus.xtream.codec.core.impl.codec.utils;

import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.PaddingType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.ext.Key;
import io.github.hylexus.xtream.codec.core.annotation.ext.KeyType;
import io.github.hylexus.xtream.codec.core.annotation.ext.LengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.ext.ValueLength;
import io.github.hylexus.xtream.codec.core.annotation.ext.ValueMatcher;
import io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField;
import io.github.hylexus.xtream.codec.core.impl.codec.BytesFieldCodecs;
import io.github.hylexus.xtream.codec.core.impl.codec.StringFieldCodecs;
import io.github.hylexus.xtream.codec.core.impl.domain.KeyMeta;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MetadataUtilsTest {

    @Test
    void shouldSelectExactVersionBeforeFallbackAndPreserveStringKeySettings() {
        final Key fallback = key(Map.of("type", KeyType.str, "sizeInBytes", 4, "charset", "US-ASCII",
                "paddingType", PaddingType.LEFT, "paddingElement", (byte) '0'));
        final Key exact = key(Map.of("type", KeyType.u16, "version", new int[]{2}));
        final Key[] keys = {fallback, exact};
        final KeyMeta selected = MetadataUtils.createKeyMeta(2, keys);
        assertEquals(2, selected.version());
        assertEquals(KeyType.u16, selected.type());
        assertEquals(2, selected.sizeInBytes());
        assertSame(XtreamDataType.u16.codec(), selected.codec());

        final KeyMeta other = MetadataUtils.createKeyMeta(3, keys);
        assertEquals(KeyType.str, other.type());
        assertEquals(4, other.sizeInBytes());
        assertEquals("US-ASCII", other.charset());
        assertEquals(PaddingType.LEFT, other.paddingType());
        assertEquals((byte) '0', other.paddingElement());
    }

    @Test
    void shouldRejectMissingVersionAndInvalidStringKeySize() {
        final Key exact = key(Map.of("version", new int[]{1}));
        assertThrows(IllegalArgumentException.class, () -> MetadataUtils.createKeyMeta(2, new Key[]{exact}));
        assertThrows(IllegalArgumentException.class, () -> MetadataUtils.createKeyMeta(1, new Key[0]));
        assertThrows(IllegalArgumentException.class, () ->
                MetadataUtils.createKeyMeta(1, new Key[]{key(Map.of("type", KeyType.str))}));
    }

    @ParameterizedTest
    @CsvSource({"str_gbk, GBK", "str_gb_2312, GB2312", "str_utf8, UTF-8", "str, US-ASCII", "u16, utf-8"})
    void shouldResolveKeyCharset(KeyType type, String expected) {
        final Key key = key(Map.of("type", type, "charset", "US-ASCII"));
        assertEquals(expected, MetadataUtils.parseKeyCharset(key));
        assertNotNull(MetadataUtils.createKeyCodec(key));
    }

    @Test
    void shouldSelectValueLengthByVersionAndRejectMissingMatch() {
        final ValueLength config = AnnotationUtils.synthesizeAnnotation(
                Map.of("version", new int[]{2}, "type", LengthFieldType.u16), ValueLength.class, null);
        assertEquals(LengthFieldType.u16, MetadataUtils.createValueLengthMeta(2, new ValueLength[]{config}).type());
        assertThrows(IllegalArgumentException.class, () ->
                MetadataUtils.createValueLengthMeta(1, new ValueLength[]{config}));
    }

    @ParameterizedTest
    @CsvSource({
            "string_gbk, GBK", "string_gb_2312, GB2312", "string_utf8, UTF-8",
            "string_ascii, ASCII", "string_hex, HEX", "string, US-ASCII"
    })
    void shouldDetectCharsetFromDataType(XtreamDataType type, String expected) {
        assertEquals(expected, MetadataUtils.detectCharset(type, null, "US-ASCII", "UTF-16BE"));
    }

    @Test
    void shouldPreferExplicitCharsetAndResolveCodecCharset() {
        assertEquals("UTF-16BE", MetadataUtils.detectCharset(XtreamDataType.string, null, " ", "UTF-16BE"));
        assertThrows(IllegalArgumentException.class, () ->
                MetadataUtils.detectCharset(XtreamDataType.string, null, "", ""));
        assertNull(MetadataUtils.detectCharset(XtreamDataType.u16, null, "UTF-8", null));
        assertNull(MetadataUtils.detectCharset(XtreamDataType.placeholder, null, "UTF-8", null));
        assertNull(MetadataUtils.detectCharset(XtreamDataType.placeholder, FieldCodec.Placeholder.class, null, null));
        assertNull(MetadataUtils.detectCharset(XtreamDataType.placeholder, BytesFieldCodecs.BytesFieldCodecByteArray.class, null, null));
        assertEquals("UTF-16BE", MetadataUtils.detectCharset(XtreamDataType.placeholder,
                StringFieldCodecs.StringFieldCodec.class, "UTF-16BE", "UTF-8"));
        assertEquals("GBK", MetadataUtils.detectCharset(XtreamDataType.placeholder,
                StringFieldCodecs.StringFieldCodecGbk.class, null, null));
        assertEquals("GB2312", MetadataUtils.detectCharset(XtreamDataType.placeholder,
                StringFieldCodecs.StringFieldCodecGb2312.class, null, null));
        assertEquals("UTF-8", MetadataUtils.detectCharset(XtreamDataType.placeholder,
                StringFieldCodecs.StringFieldCodecUtf8.class, null, null));
        assertEquals("BCD_8421", MetadataUtils.detectCharset(XtreamDataType.placeholder,
                StringFieldCodecs.StringFieldCodecBcd8421.class, null, null));
        assertEquals("HEX", MetadataUtils.detectCharset(XtreamDataType.placeholder,
                StringFieldCodecs.StringFieldCodecHex.class, null, null));
        assertEquals("US-ASCII", MetadataUtils.detectCharset(XtreamDataType.placeholder,
                StringFieldCodecs.StringFieldCodecAscII.class, "", "US-ASCII"));
    }

    @ParameterizedTest
    @EnumSource(KeyType.class)
    void shouldReadDistinctMatcherKeysInDeclarationOrder(KeyType type) throws Exception {
        final ValueMatcher[] matchers = Matchers.class.getDeclaredField("values")
                .getAnnotation(XtreamMapField.class).value().decoder().matchers();
        final int matcherIndex = switch (type) {
            case i8 -> 0;
            case u8 -> 1;
            case i16 -> 2;
            case u16 -> 3;
            case i32 -> 4;
            case u32 -> 5;
            case i64 -> 6;
            default -> 7;
        };
        final List<?> expected = switch (type) {
            case i8 -> List.of((byte) -1, (byte) 2);
            case u8 -> List.of((short) 255, (short) 2);
            case i16 -> List.of((short) -32768, (short) 2);
            case u16 -> List.of(65535, 2);
            case i32 -> List.of(Integer.MIN_VALUE, 2);
            case u32 -> List.of(4294967295L, 2L);
            case i64 -> List.of(Long.MIN_VALUE, 2L);
            default -> List.of("A", "B");
        };
        assertEquals(expected, MetadataUtils.readKey(type, matchers[matcherIndex]));
        assertNull(MetadataUtils.readKey(type, matchers[type == KeyType.i8 ? 1 : 0]));
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 9})
    void shouldRejectEmptyOrConflictingMatcherTypes(int index) throws Exception {
        final ValueMatcher matcher = Matchers.class.getDeclaredField("values")
                .getAnnotation(XtreamMapField.class).value().decoder().matchers()[index];
        assertThrows(IllegalArgumentException.class, () -> MetadataUtils.readKey(KeyType.u8, matcher));
    }

    @Test
    void shouldCountDuplicateVersionsAndMatchFallbackOnlyOnCandidateSide() {
        assertEquals(Map.of(1, 3, 2, 2), MetadataUtils.findDuplicateVersions(new int[]{1, 2, 1, 3, 2, 1}));
        assertEquals(Map.of(), MetadataUtils.findDuplicateVersions(new int[0]));
        assertTrue(MetadataUtils.isVersionMatched(2, 2));
        assertTrue(MetadataUtils.isVersionMatched(2, XtreamField.ALL_VERSION));
        assertFalse(MetadataUtils.isVersionMatched(XtreamField.ALL_VERSION, 2));
        assertFalse(MetadataUtils.isVersionMatched(1, 2));
        assertEquals("标签", MetadataUtils.formateKey("标签"));
        assertEquals("255 (0x00ff)", MetadataUtils.formateKey(255));
        assertEquals("UTF-8", MetadataUtils.firstOr("UTF-8", null));
        assertEquals("GBK", MetadataUtils.firstOr(" ", "GBK"));
        assertThrows(NullPointerException.class, () -> MetadataUtils.firstOr(null, null));
    }

    private static Key key(Map<String, Object> attributes) {
        return AnnotationUtils.synthesizeAnnotation(attributes, Key.class, null);
    }

    static class Matchers {
        @XtreamMapField(
                key = @Key,
                valueLength = @ValueLength(type = LengthFieldType.u8),
                value = @XtreamMapField.Value(decoder = @XtreamMapField.ValueDecoder(matchers = {
                        @ValueMatcher(matchI8 = {-1, 2, -1}),
                        @ValueMatcher(matchU8 = {255, 2, 255}),
                        @ValueMatcher(matchI16 = {-32768, 2, -32768}),
                        @ValueMatcher(matchU16 = {65535, 2, 65535}),
                        @ValueMatcher(matchI32 = {Integer.MIN_VALUE, 2, Integer.MIN_VALUE}),
                        @ValueMatcher(matchU32 = {4294967295L, 2, 4294967295L}),
                        @ValueMatcher(matchI64 = {Long.MIN_VALUE, 2, Long.MIN_VALUE}),
                        @ValueMatcher(matchString = {"A", "B", "A"}),
                        @ValueMatcher,
                        @ValueMatcher(matchI8 = 1, matchU8 = 1)
                })))
        Map<Object, Object> values;
    }
}
