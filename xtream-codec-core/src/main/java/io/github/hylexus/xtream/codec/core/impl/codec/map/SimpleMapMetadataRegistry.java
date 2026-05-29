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

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.ext.FallbackValueMatcher;
import io.github.hylexus.xtream.codec.core.annotation.ext.ValueDecoderCommonParam;
import io.github.hylexus.xtream.codec.core.annotation.ext.ValueMatcher;
import io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField;
import io.github.hylexus.xtream.codec.core.impl.codec.utils.HasVersion;
import io.github.hylexus.xtream.codec.core.impl.codec.utils.HasVersions;
import io.github.hylexus.xtream.codec.core.impl.codec.utils.MetadataUtils;
import io.github.hylexus.xtream.codec.core.impl.codec.utils.VersionMatchResult;
import io.github.hylexus.xtream.codec.core.impl.domain.FallbackValueMatcherMeta;
import io.github.hylexus.xtream.codec.core.impl.domain.KeyMeta;
import io.github.hylexus.xtream.codec.core.impl.domain.ValueLengthMeta;
import io.github.hylexus.xtream.codec.core.impl.domain.ValueMatcherMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

// todo 移动到 BeanMetadataRegistry ?
@ApiStatus.Internal
public class SimpleMapMetadataRegistry {

    private static final Logger log = LoggerFactory.getLogger(SimpleMapMetadataRegistry.class);

    private final BeanMetadataRegistry beanMetadataRegistry;
    // targetVersion -> field -> MapMeta
    private final ConcurrentMap<Integer, ConcurrentMap<Field, MapMeta>> cache = new ConcurrentHashMap<>();

    public SimpleMapMetadataRegistry(BeanMetadataRegistry beanMetadataRegistry) {
        this.beanMetadataRegistry = requireNonNull(beanMetadataRegistry);
    }

    MapMeta getOrCreateMapMetadata(FieldCodec.CodecContext codecContext, BeanPropertyMetadata propertyMetadata) {
        final int targetVersion = codecContext.version();
        final Field field = propertyMetadata.field();
        final ConcurrentMap<Field, MapMeta> map = cache.computeIfAbsent(targetVersion, k -> new ConcurrentHashMap<>());
        return map.computeIfAbsent(field, f -> {
            try {
                final XtreamMapField xtreamMapField = propertyMetadata.findAnnotation(XtreamMapField.class).orElseThrow();
                final MapMeta mapMeta = doCreateMapMetadata(targetVersion, xtreamMapField);
                if (log.isDebugEnabled()) {
                    log.debug("Field:{}, MapMeta: {}", field.toGenericString(), mapMeta);
                }
                return mapMeta;
            } catch (Exception e) {
                log.error("Error while creating MapMeta\n===> Field: {}", field.toGenericString(), e);
                throw e;
            }
        });
    }

    private MapMeta doCreateMapMetadata(int targetVersion, XtreamMapField xtreamMapField) {
        final KeyMeta keyMeta = createKeyMeta(targetVersion, xtreamMapField);
        final ValueLengthMeta valueLengthMeta = createValueLengthMeta(targetVersion, xtreamMapField);
        final ValueMeta valueMeta = createValueMeta(targetVersion, keyMeta, xtreamMapField.value());
        return new MapMeta(keyMeta, valueLengthMeta, valueMeta);
    }

    private ValueMeta createValueMeta(int targetVersion, KeyMeta keyMeta, XtreamMapField.Value value) {
        final EncoderValueMeta encoderValueMeta = createEncoderValueMata(targetVersion, keyMeta, value);
        final DecoderValueMeta decoderValueMeta = createDecoderValueMata(targetVersion, keyMeta, value);
        return new ValueMeta(encoderValueMeta, decoderValueMeta);
    }

    private EncoderValueMeta createEncoderValueMata(int targetVersion, KeyMeta keyMeta, XtreamMapField.Value value) {
        final VersionMatchResult<EncoderCommonParam> matchResult = HasVersions.matchVersion(
                targetVersion,
                Arrays.stream(value.encoder().params()).map(param -> new HasVersions<>(param.version(), param)),
                hasVersion -> {
                    final XtreamMapField.EncoderParam data = hasVersion.data();
                    return new EncoderCommonParam(hasVersion.version(), data.writeNumberAsLittleEndian(), data.charset());
                }
        );
        final EncoderCommonParam commonParam = matchResult.matched() ? matchResult.source() : new EncoderCommonParam(targetVersion, false, XtreamMapField.DEFAULT_CHARSET);
        log.debug("EncoderCommonParam: {}", matchResult);
        final List<ValueMatcherMeta> encoderValueMatchers = createValueMatcherMetaList(targetVersion, keyMeta, value.encoder().matchers(), commonParam, null);
        return new EncoderValueMeta(commonParam, encoderValueMatchers);
    }

    private DecoderValueMeta createDecoderValueMata(int targetVersion, KeyMeta keyType, XtreamMapField.Value value) {
        final VersionMatchResult<DecoderCommonParam> matchResult = HasVersions.matchVersion(
                targetVersion,
                Arrays.stream(value.decoder().params()).map(param -> new HasVersions<>(param.version(), param)),
                hasVersion -> {
                    final ValueDecoderCommonParam data = hasVersion.data();
                    return new DecoderCommonParam(hasVersion.version(), data.charset());
                }
        );
        final DecoderCommonParam commonParam = matchResult.matched()
                ? matchResult.source()
                : new DecoderCommonParam(targetVersion, XtreamMapField.DEFAULT_CHARSET);
        log.debug("DecoderCommonParam: {}", matchResult);
        final FallbackValueMatcherMeta fallbackValueMatcherMeta = createFallbackValueMatcherMeta(targetVersion, value.decoder().fallbackMatchers(), commonParam);
        final List<ValueMatcherMeta> decoderValueMatchers = createValueMatcherMetaList(targetVersion, keyType, value.decoder().matchers(), null, commonParam);
        return new DecoderValueMeta(commonParam, fallbackValueMatcherMeta, decoderValueMatchers);
    }

    private static Map<Object, ValueMatcherMeta> valueMatcherToMap(List<ValueMatcherMeta> valueMatchers) {
        final Map<Object, ValueMatcherMeta> valueMatchersByKey = new LinkedHashMap<>();
        valueMatchers
                .stream()
                .collect(Collectors.groupingBy(ValueMatcherMeta::key))
                .forEach((key, valueList) -> {
                    valueMatchersByKey.put(key, valueList.getFirst());
                    if (valueList.size() > 1) {
                        throw new IllegalArgumentException("duplicated @ValueMatcher.key = " + key + ": " + valueList);
                    }
                });
        return valueMatchersByKey;
    }

    private FallbackValueMatcherMeta createFallbackValueMatcherMeta(int targetVersion, FallbackValueMatcher[] matchers, DecoderCommonParam commonParam) {
        if (matchers.length == 0) {
            throw new IllegalArgumentException("No fallbackValueMatcher found");
        }
        final VersionMatchResult<FallbackValueMatcher> matchResult = HasVersions.matchVersion(
                targetVersion,
                Arrays.stream(matchers).map(it -> new HasVersions<>(it.version(), it)),
                HasVersion::data
        );
        log.debug("Fallback targetVersion={}, {}", targetVersion, matchResult);
        if (!matchResult.matched()) {
            throw new IllegalArgumentException("No fallbackValueMatcher found");
        }
        final int matchedVersion = matchResult.version();
        final FallbackValueMatcher matcher = matchResult.source();
        final String actualCharset = detectCharset(matcher, commonParam.stringTypeCharset());
        final FieldCodec<Object> valueCodec = MetadataUtils.createValueCodec(this.beanMetadataRegistry, matcher.valueType(), matcher.valueCodec(), null, actualCharset);
        return new FallbackValueMatcherMeta(matchedVersion, matcher.valueType(), requireNonNull(valueCodec), actualCharset);
    }

    @Nullable
    static String detectCharset(FallbackValueMatcher matcher, String fallbackCharset) {
        return MetadataUtils.detectCharset(matcher.valueType(), matcher.valueCodec(), matcher.charset(), fallbackCharset);
    }

    @Nullable
    static String detectCharset(
            ValueMatcher matcher,
            @Nullable EncoderCommonParam encoderCommonParam,
            @Nullable DecoderCommonParam decoderCommonParam) {

        final String fallbackCharset = encoderCommonParam != null
                ? encoderCommonParam.stringTypeCharset()
                : requireNonNull(decoderCommonParam, "Only one of DecoderCommonParam and EncoderCommonParam can be null.").stringTypeCharset();
        return MetadataUtils.detectCharset(matcher.valueType(), matcher.valueCodec(), matcher.charset(), fallbackCharset);
    }

    private KeyMeta createKeyMeta(int targetVersion, XtreamMapField xtreamMapField) {
        return MetadataUtils.createKeyMeta(targetVersion, xtreamMapField.key());
    }

    private ValueLengthMeta createValueLengthMeta(int targetVersion, XtreamMapField xtreamMapField) {
        return MetadataUtils.createValueLengthMeta(targetVersion, xtreamMapField.valueLength());
    }

    private List<ValueMatcherMeta> createValueMatcherMetaList(
            int targetVersion,
            KeyMeta keyMeta,
            ValueMatcher[] valueMatchers,
            @Nullable EncoderCommonParam encoderCommonParam,
            @Nullable DecoderCommonParam decoderCommonParam) {

        final Map<Object, ValueMatcherMeta> resultMap = new LinkedHashMap<>();
        Arrays.stream(valueMatchers)
                .flatMap(matcher -> {
                    final int[] version = matcher.version();
                    final Map<Integer, Integer> duplicateVersions = MetadataUtils.findDuplicateVersions(version);
                    if (!duplicateVersions.isEmpty()) {
                        final String tips = duplicateVersions.entrySet().stream().map(it -> "version: " + it.getKey() + ", times: " + it.getValue()).collect(Collectors.joining("\n- ", "- ", ""));
                        throw new IllegalArgumentException("Duplicate versions [@" + ValueMatcher.class.getName() + "]\n" + tips);
                    }
                    return Arrays.stream(version).mapToObj(v -> new HasVersion<>(v, matcher));
                })
                .filter(hasVersion -> MetadataUtils.isVersionMatched(targetVersion, hasVersion.version()))
                .forEach(hasVersion -> {
                    final ValueMatcher matcher = hasVersion.data();
                    final String actualCharset = detectCharset(matcher, encoderCommonParam, decoderCommonParam);
                    final FieldCodec<Object> valueCodec = MetadataUtils.createValueCodec(this.beanMetadataRegistry, matcher.valueType(), matcher.valueCodec(), matcher.valueEntity(), actualCharset);
                    if (valueCodec == null) {
                        throw new IllegalArgumentException("Unsupported valueType: " + matcher.valueType());
                    }

                    final List<?> keyList = MetadataUtils.readKey(keyMeta.type(), matcher);
                    if (keyList == null) {
                        return;
                    }
                    for (Object key : keyList) {
                        final ValueMatcherMeta newMatcher = new ValueMatcherMeta(hasVersion.version(), key, matcher.valueType(), valueCodec, actualCharset);

                        final ValueMatcherMeta existedMatcher = resultMap.get(key);
                        if (existedMatcher == null) {
                            resultMap.put(key, newMatcher);
                        } else {
                            final int existedVersion = existedMatcher.version();
                            final int newVersion = newMatcher.version();
                            if (existedVersion == XtreamField.ALL_VERSION) {
                                if (newVersion == XtreamField.ALL_VERSION) {
                                    throw new IllegalArgumentException("Duplicate valueMatcher. Key: " + MetadataUtils.formateKey(key) + "\n"
                                                                       + "- " + existedMatcher + "\n"
                                                                       + "- " + newMatcher);
                                } else {
                                    resultMap.put(key, newMatcher);
                                }
                            } else {
                                if (newVersion != XtreamField.ALL_VERSION) {
                                    throw new IllegalArgumentException("Duplicate valueMatcher. Key: " + MetadataUtils.formateKey(key) + "\n"
                                                                       + "- " + existedMatcher + "\n"
                                                                       + "- " + newMatcher);
                                }
                            }
                        }
                    }
                });

        return List.copyOf(resultMap.values());
    }

    // ========================================================================
    // Inner Records (SimpleMap-specific)
    // ========================================================================

    record DecoderCommonParam(int version, String stringTypeCharset) {
        @SuppressWarnings("redundent")
        DecoderCommonParam {
        }
    }

    record EncoderCommonParam(int version, boolean numberTypeAsLittleEndian, String stringTypeCharset) {
        @SuppressWarnings("redundent")
        EncoderCommonParam {
        }
    }

    record MapMeta(
            KeyMeta keyMeta,
            ValueLengthMeta valueLengthMeta,
            ValueMeta valueMeta) {
        @SuppressWarnings("redundent")
        MapMeta {
        }
    }

    record ValueMeta(
            EncoderValueMeta encoder,
            DecoderValueMeta decoder) {

        @SuppressWarnings("redundent")
        public ValueMeta {
        }
    }

    record EncoderValueMeta(
            EncoderCommonParam commonParam,
            List<ValueMatcherMeta> encoderValueMatchers,
            Map<Object, ValueMatcherMeta> valueMatchersByKey) {

        @SuppressWarnings("redundent")
        public EncoderValueMeta {
        }

        public EncoderValueMeta(EncoderCommonParam commonParam, List<ValueMatcherMeta> encoderValueMatchers) {
            this(commonParam, encoderValueMatchers, valueMatcherToMap(encoderValueMatchers));
        }
    }

    record DecoderValueMeta(
            DecoderCommonParam commonParam,
            FallbackValueMatcherMeta fallbackValueMatcher,
            List<ValueMatcherMeta> decoderValueMatchers,
            Map<Object, ValueMatcherMeta> valueMatchersByKey) {

        @SuppressWarnings("redundent")
        public DecoderValueMeta {
        }

        public DecoderValueMeta(DecoderCommonParam commonParam, FallbackValueMatcherMeta fallbackValueMatcher, List<ValueMatcherMeta> decoderValueMatchers) {
            this(commonParam, fallbackValueMatcher, decoderValueMatchers, valueMatcherToMap(decoderValueMatchers));
        }
    }
}
