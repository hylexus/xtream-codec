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

package io.github.hylexus.xtream.codec.core.impl;

import io.github.hylexus.xtream.codec.core.XtreamCacheableClassPredicate;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.ext.*;
import io.github.hylexus.xtream.codec.core.annotation.pair.XtreamPairFieldSequence;
import io.github.hylexus.xtream.codec.core.annotation.tlv.XtreamTLVFieldSequence;
import io.github.hylexus.xtream.codec.core.impl.domain.ValueMatcherMetas;
import io.github.hylexus.xtream.codec.core.type.Pair;
import io.github.hylexus.xtream.codec.core.type.TLV;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class DefaultExtendMetaRegistryTest {
    DefaultExtendMetaRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DefaultExtendMetaRegistry(new SimpleBeanMetadataRegistry(new DefaultFieldCodecRegistry(true), new XtreamCacheableClassPredicate.Default()));
    }

    @Test
    void pairMetadataCachesByFieldAndVersionAndSelectsExactMatcher() throws Exception {
        final Field field = field("pairs");
        final var v1 = registry.getXtreamPairFieldSequenceMeta(1, field);
        assertThat(registry.getXtreamPairFieldSequenceMeta(1, field)).isSameAs(v1);
        assertThat(v1.decoder().key().type()).isEqualTo(KeyType.u16);
        assertThat(v1.decoder().valueMatchers().valueMatchersByKey().get(1).valueCodec()).isSameAs(XtreamDataType.u16_le.codec());
        assertThat(v1.decoder().valueMatchers().valueMatchersByKey().get(1).length()).isEqualTo(2);
        final var v2 = registry.getXtreamPairFieldSequenceMeta(2, field);
        assertThat(v2).isNotSameAs(v1);
        assertThat(v2.decoder().valueMatchers().valueMatchersByKey().get(1).valueCodec()).isSameAs(XtreamDataType.u8.codec());
        assertThat(registry.getXtreamPairFieldSequenceMeta(1, field("otherPairs"))).isNotSameAs(v1);
    }

    @Test
    void tlvMetadataCachesByFieldAndVersionAndSelectsFallback() throws Exception {
        final Field field = field("tlvs");
        final var v1 = registry.getXtreamTlvFieldSequenceMeta(1, field);
        assertThat(registry.getXtreamTlvFieldSequenceMeta(1, field)).isSameAs(v1);
        assertThat(v1.decoder().length().type()).isEqualTo(LengthFieldType.u16);
        assertThat(v1.decoder().fallbackValueMatcher().codec()).isSameAs(XtreamDataType.string_utf8.codec());
        final var v2 = registry.getXtreamTlvFieldSequenceMeta(2, field);
        assertThat(v2).isNotSameAs(v1);
        assertThat(v2.decoder().fallbackValueMatcher().codec()).isSameAs(XtreamDataType.string_hex.codec());
        assertThat(registry.getXtreamTlvFieldSequenceMeta(1, field("otherTlvs"))).isNotSameAs(v1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rejectsFieldsWithoutRequiredSequenceAnnotation(boolean tlv) throws Exception {
        final Field field = field("unannotated");
        assertThatThrownBy(() -> {
            if (tlv) {
                registry.getXtreamTlvFieldSequenceMeta(1, field);
            } else {
                registry.getXtreamPairFieldSequenceMeta(1, field);
            }
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(tlv ? "XtreamTLVFieldSequence" : "XtreamPairFieldSequence")
                .hasMessageContaining(field.toGenericString());
    }

    @ParameterizedTest
    @CsvSource({"false, missingKey, Key", "true, missingKey, Key", "true, missingLength, ValueLength",
            "false, missingPairLength, length() must be greater than 0", "false, zeroPairLength, length() must be greater than 0"})
    void rejectsMissingVersionConfigurationAndInvalidPairLength(boolean tlv, String name, String message) throws Exception {
        assertThatThrownBy(() -> {
            if (tlv) {
                registry.getXtreamTlvFieldSequenceMeta(1, field(name));
            } else {
                registry.getXtreamPairFieldSequenceMeta(1, field(name));
            }
        }).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(message);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void exactMatcherWinsRegardlessOfDeclarationOrder(boolean exactFirst) throws Exception {
        final ValueMatcher fallback = matcher(XtreamField.ALL_VERSION, XtreamDataType.u8);
        final ValueMatcher exact = matcher(1, XtreamDataType.u16);
        final ValueMatcher[] matchers = exactFirst ? new ValueMatcher[]{exact, fallback} : new ValueMatcher[]{fallback, exact};
        final ValueMatcherMetas result = matchers(matchers, new ValueDecoderCommonParam[0]);
        assertThat(result.valueMatchers()).hasSize(1);
        assertThat(result.valueMatchersByKey().get(1).version()).isEqualTo(1);
        assertThat(result.valueMatchersByKey().get(1).valueCodec()).isSameAs(XtreamDataType.u16.codec());
    }

    static Stream<Arguments> invalidMatchers() {
        final ValueMatcher fallback = matcher(XtreamField.ALL_VERSION, XtreamDataType.u8);
        final ValueMatcher exact = matcher(1, XtreamDataType.u16);
        return Stream.of(
                Arguments.of(new ValueMatcher[]{fallback, fallback}, "Duplicate valueMatcher"),
                Arguments.of(new ValueMatcher[]{exact, exact}, "Duplicate valueMatcher"),
                Arguments.of(new ValueMatcher[]{annotation(ValueMatcher.class, Map.of("version", new int[]{1, 1},
                        "matchU16", new int[]{1}, "valueType", XtreamDataType.u8))}, "Duplicate versions"),
                Arguments.of(new ValueMatcher[]{annotation(ValueMatcher.class, Map.of("matchU16", new int[]{1}))}, "Unsupported valueType"));
    }

    @ParameterizedTest
    @MethodSource("invalidMatchers")
    void rejectsDuplicateAndUnsupportedMatchers(ValueMatcher[] matchers, String message) {
        assertThatThrownBy(() -> matchers(matchers, new ValueDecoderCommonParam[0]))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining(message);
    }

    @Test
    void ignoresOtherKeyTypesAndVersions() throws Exception {
        final ValueMatcher[] matchers = {
                annotation(ValueMatcher.class, Map.of("matchU8", new short[]{1}, "valueType", XtreamDataType.u8)),
                annotation(ValueMatcher.class, Map.of("version", new int[]{2}, "matchU16", new int[]{1}))
        };
        assertThat(matchers(matchers, new ValueDecoderCommonParam[0]).valueMatchersByKey()).isEmpty();
    }

    @Test
    void matcherCharsetUsesCommonParametersAndAllowsExplicitOverride() throws Exception {
        final ValueDecoderCommonParam[] params = {annotation(ValueDecoderCommonParam.class, Map.of("charset", "GBK"))};
        final ValueMatcher[] matchers = {
                matcher(XtreamField.ALL_VERSION, XtreamDataType.string),
                annotation(ValueMatcher.class, Map.of("matchU16", new int[]{2}, "valueType", XtreamDataType.string, "charset", "US-ASCII")),
                annotation(ValueMatcher.class, Map.of("matchU16", new int[]{3}, "valueType", XtreamDataType.string_utf8))
        };
        final var result = matchers(matchers, params).valueMatchersByKey();
        assertThat(result.get(1).charset()).isEqualTo("GBK");
        assertThat(result.get(2).charset()).isEqualTo("US-ASCII");
        assertThat(result.get(3).charset()).isEqualTo("UTF-8");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void requiresFallbackForRequestedVersion(boolean absent) throws Exception {
        final FallbackValueMatcher[] fallbacks = absent ? new FallbackValueMatcher[0] : new FallbackValueMatcher[]{
                annotation(FallbackValueMatcher.class, Map.of("version", new int[]{2}, "valueType", XtreamDataType.u8))};
        assertThatThrownBy(() -> registry.createFallbackValueMatcherMeta(1, field("tlvs"), fallbacks))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("FallbackValueMatcher").hasMessageContaining("target version 1");
    }

    @Test
    void rejectsFallbackWithoutSupportedCodec() {
        assertThatThrownBy(() -> registry.createFallbackValueMatcherMeta(1, field("tlvs"),
                new FallbackValueMatcher[]{annotation(FallbackValueMatcher.class, Map.of())}))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unsupported valueType");
    }

    private ValueMatcherMetas matchers(ValueMatcher[] matchers, ValueDecoderCommonParam[] params) throws Exception {
        return registry.createValueMatcherMetas(KeyType.u16, 1, field("tlvs"), params, matchers, Function.identity());
    }

    private static ValueMatcher matcher(int version, XtreamDataType type) {
        return annotation(ValueMatcher.class, Map.of("version", new int[]{version}, "matchU16", new int[]{1}, "valueType", type));
    }

    private static <A extends Annotation> A annotation(Class<A> type, Map<String, Object> attributes) {
        return AnnotationUtils.synthesizeAnnotation(attributes, type, null);
    }

    private static Field field(String name) throws Exception {
        return Fields.class.getDeclaredField(name);
    }

    static class Fields {
        @XtreamPairFieldSequence(decoder = @XtreamPairFieldSequence.Decoder(key = @Key(type = KeyType.u16),
                value = @XtreamPairFieldSequence.Value(matchers = {
                        @ValueMatcher(matchU16 = 1, valueType = XtreamDataType.u8, length = 1),
                        @ValueMatcher(version = 1, matchU16 = 1, valueType = XtreamDataType.u16_le, length = 2)
                })))
        List<Pair> pairs;

        @XtreamPairFieldSequence(decoder = @XtreamPairFieldSequence.Decoder(key = @Key,
                value = @XtreamPairFieldSequence.Value(matchers = {})))
        List<Pair> otherPairs;

        @XtreamTLVFieldSequence(decoder = @XtreamTLVFieldSequence.Decoder(tag = @Key(type = KeyType.u16),
                length = @ValueLength(type = LengthFieldType.u16), value = @XtreamTLVFieldSequence.Value(matchers = {}, fallbackMatchers = {
                        @FallbackValueMatcher(valueType = XtreamDataType.string_hex, desc = "未知标签"),
                        @FallbackValueMatcher(version = 1, valueType = XtreamDataType.string_utf8)
                })))
        List<TLV> tlvs;

        @XtreamTLVFieldSequence(decoder = @XtreamTLVFieldSequence.Decoder(tag = @Key,
                length = @ValueLength(type = LengthFieldType.u8), value = @XtreamTLVFieldSequence.Value(matchers = {})))
        List<TLV> otherTlvs;

        List<Object> unannotated;

        @XtreamPairFieldSequence(decoder = @XtreamPairFieldSequence.Decoder(key = @Key(version = 2),
                value = @XtreamPairFieldSequence.Value(matchers = {})))
        @XtreamTLVFieldSequence(decoder = @XtreamTLVFieldSequence.Decoder(tag = @Key(version = 2),
                length = @ValueLength(type = LengthFieldType.u8), value = @XtreamTLVFieldSequence.Value(matchers = {})))
        List<Object> missingKey;

        @XtreamTLVFieldSequence(decoder = @XtreamTLVFieldSequence.Decoder(tag = @Key,
                length = @ValueLength(version = 2, type = LengthFieldType.u8), value = @XtreamTLVFieldSequence.Value(matchers = {})))
        List<TLV> missingLength;

        @XtreamPairFieldSequence(decoder = @XtreamPairFieldSequence.Decoder(key = @Key,
                value = @XtreamPairFieldSequence.Value(matchers = @ValueMatcher(matchU8 = 1, valueType = XtreamDataType.u8))))
        List<Pair> missingPairLength;

        @XtreamPairFieldSequence(decoder = @XtreamPairFieldSequence.Decoder(key = @Key,
                value = @XtreamPairFieldSequence.Value(matchers = @ValueMatcher(matchU8 = 1, valueType = XtreamDataType.u8, length = 0))))
        List<Pair> zeroPairLength;
    }
}
