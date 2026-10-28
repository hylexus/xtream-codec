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

package io.github.hylexus.xtream.codec.core.impl.codec.map;

import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.ext.*;
import io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField;
import io.github.hylexus.xtream.codec.core.impl.codec.map.SimpleMapMetadataRegistry.MapMeta;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimpleMapMetadataRegistryTest extends BaseEntityCodecTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void exactVersionOverridesDefaultRegardlessOfDeclarationOrder(boolean exactFirst) throws Exception {
        final ValueMatcher fallback = matcher(XtreamField.ALL_VERSION, XtreamDataType.u8);
        final ValueMatcher exact = matcher(1, XtreamDataType.u16_le);
        final ValueMatcher[] matchers = exactFirst ? new ValueMatcher[]{exact, fallback} : new ValueMatcher[]{fallback, exact};
        final XtreamMapField config = configuration(
                annotation(XtreamMapField.ValueEncoder.class, Map.of("matchers", matchers)),
                annotation(XtreamMapField.ValueDecoder.class, Map.of("matchers", matchers)));
        final BeanPropertyMetadata property = property(config);
        final var registry = new SimpleMapMetadataRegistry(entityCodec.entityEncoder().getBeanMetadataRegistry());
        final MapMeta v1 = registry.getOrCreateMapMetadata(context(1), property);
        assertThat(v1.valueMeta().encoder().valueMatchersByKey().get(1).valueCodec()).isSameAs(XtreamDataType.u16_le.codec());
        assertThat(v1.valueMeta().decoder().valueMatchersByKey().get(1).valueCodec()).isSameAs(XtreamDataType.u16_le.codec());
        assertThat(registry.getOrCreateMapMetadata(context(1), property)).isSameAs(v1);
        final MapMeta v2 = registry.getOrCreateMapMetadata(context(2), property);
        assertThat(v2).isNotSameAs(v1);
        assertThat(v2.valueMeta().encoder().valueMatchersByKey().get(1).valueCodec()).isSameAs(XtreamDataType.u8.codec());
        assertThat(v2.valueMeta().decoder().valueMatchersByKey().get(1).valueCodec()).isSameAs(XtreamDataType.u8.codec());
    }

    static Stream<Arguments> invalidMatchers() {
        final ValueMatcher generic = matcher(XtreamField.ALL_VERSION, XtreamDataType.u8);
        final ValueMatcher exact = matcher(1, XtreamDataType.u16);
        return Stream.of(false, true).flatMap(encoder -> Stream.of(
                Arguments.of(encoder, new ValueMatcher[]{generic, generic}, "Duplicate valueMatcher"),
                Arguments.of(encoder, new ValueMatcher[]{exact, exact}, "Duplicate valueMatcher"),
                Arguments.of(encoder, new ValueMatcher[]{annotation(ValueMatcher.class, Map.of(
                        "version", new int[]{1, 1}, "matchU16", new int[]{1}, "valueType", XtreamDataType.u8))}, "Duplicate versions"),
                Arguments.of(encoder, new ValueMatcher[]{annotation(ValueMatcher.class, Map.of("matchU16", new int[]{1}))}, "Unsupported valueType")));
    }

    @ParameterizedTest
    @MethodSource("invalidMatchers")
    void rejectsAmbiguousOrUnsupportedMatcherConfiguration(boolean encoder, ValueMatcher[] matchers, String message) throws Exception {
        final XtreamMapField config = configuration(
                annotation(XtreamMapField.ValueEncoder.class, encoder ? Map.of("matchers", matchers) : Map.of()),
                annotation(XtreamMapField.ValueDecoder.class, encoder ? Map.of() : Map.of("matchers", matchers)));
        assertThatThrownBy(() -> metadata(1, config)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(message);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void requiresFallbackForRequestedVersion(boolean absent) throws Exception {
        final FallbackValueMatcher[] fallbacks = absent ? new FallbackValueMatcher[0] : new FallbackValueMatcher[]{
                annotation(FallbackValueMatcher.class, Map.of("version", new int[]{2}, "valueType", XtreamDataType.string_hex))};
        final XtreamMapField config = configuration(annotation(XtreamMapField.ValueEncoder.class, Map.of()),
                annotation(XtreamMapField.ValueDecoder.class, Map.of("fallbackMatchers", fallbacks)));
        assertThatThrownBy(() -> metadata(1, config)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No fallbackValueMatcher found");
    }

    @Test
    void selectsFallbackAndCommonParametersForRequestedVersion() throws Exception {
        final XtreamMapField config = configuration(
                annotation(XtreamMapField.ValueEncoder.class, Map.of("params", new XtreamMapField.EncoderParam[]{
                        annotation(XtreamMapField.EncoderParam.class, Map.of("version", new int[]{1},
                                "charset", "GBK", "writeNumberAsLittleEndian", true))})),
                annotation(XtreamMapField.ValueDecoder.class, Map.of(
                        "params", new ValueDecoderCommonParam[]{annotation(ValueDecoderCommonParam.class,
                                Map.of("version", new int[]{1}, "charset", "GBK"))},
                        "fallbackMatchers", new FallbackValueMatcher[]{
                                annotation(FallbackValueMatcher.class, Map.of("valueType", XtreamDataType.string_hex)),
                                annotation(FallbackValueMatcher.class, Map.of("version", new int[]{1}, "valueType", XtreamDataType.string))})));
        final MapMeta v1 = metadata(1, config);
        assertThat(v1.valueMeta().encoder().commonParam().numberTypeAsLittleEndian()).isTrue();
        assertThat(v1.valueMeta().encoder().commonParam().stringTypeCharset()).isEqualTo("GBK");
        assertThat(v1.valueMeta().decoder().fallbackValueMatcher().charset()).isEqualTo("GBK");
        assertThat(v1.valueMeta().decoder().fallbackValueMatcher().version()).isEqualTo(1);
        final MapMeta v2 = metadata(2, config);
        assertThat(v2.valueMeta().encoder().commonParam().numberTypeAsLittleEndian()).isFalse();
        assertThat(v2.valueMeta().encoder().commonParam().stringTypeCharset()).isEqualTo(XtreamMapField.DEFAULT_CHARSET);
        assertThat(v2.valueMeta().decoder().commonParam().stringTypeCharset()).isEqualTo(XtreamMapField.DEFAULT_CHARSET);
        assertThat(v2.valueMeta().decoder().fallbackValueMatcher().version()).isEqualTo(XtreamField.ALL_VERSION);
        assertThat(v2.valueMeta().decoder().fallbackValueMatcher().codec()).isSameAs(XtreamDataType.string_hex.codec());
    }

    @Test
    void ignoresMatchersForDifferentKeyType() throws Exception {
        final ValueMatcher wrongKey = annotation(ValueMatcher.class, Map.of("matchU8", new short[]{1}, "valueType", XtreamDataType.u16));
        final MapMeta result = metadata(1, configuration(
                annotation(XtreamMapField.ValueEncoder.class, Map.of("matchers", new ValueMatcher[]{wrongKey})),
                annotation(XtreamMapField.ValueDecoder.class, Map.of("matchers", new ValueMatcher[]{wrongKey}))));
        assertThat(result.valueMeta().encoder().valueMatchersByKey()).isEmpty();
        assertThat(result.valueMeta().decoder().valueMatchersByKey()).isEmpty();
    }

    private MapMeta metadata(int version, XtreamMapField configuration) throws Exception {
        return new SimpleMapMetadataRegistry(entityCodec.entityEncoder().getBeanMetadataRegistry())
                .getOrCreateMapMetadata(context(version), property(configuration));
    }

    private BeanPropertyMetadata property(XtreamMapField configuration) throws Exception {
        final BeanPropertyMetadata property = mock(BeanPropertyMetadata.class);
        when(property.field()).thenReturn(MapFieldCodecBoundaryTest.Packet.class.getDeclaredField("entries"));
        when(property.findAnnotation(XtreamMapField.class)).thenReturn(Optional.of(configuration));
        return property;
    }

    private FieldCodec.CodecContext context(int version) {
        final FieldCodec.CodecContext context = mock(FieldCodec.CodecContext.class);
        when(context.version()).thenReturn(version);
        return context;
    }

    private static ValueMatcher matcher(int version, XtreamDataType type) {
        return annotation(ValueMatcher.class, Map.of("version", new int[]{version}, "matchU16", new int[]{1}, "valueType", type));
    }

    private static XtreamMapField configuration(XtreamMapField.ValueEncoder encoder, XtreamMapField.ValueDecoder decoder) {
        return annotation(XtreamMapField.class, Map.of(
                "key", new Key[]{annotation(Key.class, Map.of("type", KeyType.u16))},
                "valueLength", new ValueLength[]{annotation(ValueLength.class, Map.of("type", LengthFieldType.u16))},
                "value", annotation(XtreamMapField.Value.class, Map.of("encoder", encoder, "decoder", decoder))));
    }

    private static <A extends Annotation> A annotation(Class<A> type, Map<String, Object> attributes) {
        return AnnotationUtils.synthesizeAnnotation(attributes, type, null);
    }
}
