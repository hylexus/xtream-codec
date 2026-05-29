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

import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.ExtendMetaRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.ext.*;
import io.github.hylexus.xtream.codec.core.annotation.pair.XtreamPairFieldSequence;
import io.github.hylexus.xtream.codec.core.annotation.tlv.XtreamTLVFieldSequence;
import io.github.hylexus.xtream.codec.core.impl.codec.utils.HasVersion;
import io.github.hylexus.xtream.codec.core.impl.codec.utils.HasVersions;
import io.github.hylexus.xtream.codec.core.impl.codec.utils.MetadataUtils;
import io.github.hylexus.xtream.codec.core.impl.codec.utils.VersionMatchResult;
import io.github.hylexus.xtream.codec.core.impl.domain.*;
import io.github.hylexus.xtream.codec.core.type.Pair;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class DefaultExtendMetaRegistry implements ExtendMetaRegistry {
    private static final Logger log = LoggerFactory.getLogger(DefaultExtendMetaRegistry.class);
    private final BeanMetadataRegistry beanMetadataRegistry;

    private final ConcurrentMap<Integer, ConcurrentMap<Field, XtreamTLVFieldSequenceMeta>> tlvFieldSequenceMetaCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, ConcurrentMap<Field, XtreamPairFieldSequenceMeta>> pairFieldSequenceMetaCache = new ConcurrentHashMap<>();
    private static volatile @Nullable ExtendMetaRegistry INSTANCE;

    public static synchronized ExtendMetaRegistry getInstance(BeanMetadataRegistry beanMetadataRegistry) {
        if (INSTANCE == null) {
            INSTANCE = new DefaultExtendMetaRegistry(beanMetadataRegistry);
        }
        return requireNonNull(INSTANCE);
    }

    public DefaultExtendMetaRegistry(BeanMetadataRegistry beanMetadataRegistry) {
        this.beanMetadataRegistry = beanMetadataRegistry;
    }

    @Override
    public XtreamPairFieldSequenceMeta getXtreamPairFieldSequenceMeta(int targetVersion, Field field) {
        final ConcurrentMap<Field, XtreamPairFieldSequenceMeta> map = pairFieldSequenceMetaCache.computeIfAbsent(targetVersion, k -> new ConcurrentHashMap<>());
        return map.computeIfAbsent(field, f -> {
            final XtreamPairFieldSequence xtreamPairFieldSequence = AnnotatedElementUtils.getMergedAnnotation(f, XtreamPairFieldSequence.class);
            if (xtreamPairFieldSequence == null) {
                throw new IllegalArgumentException("@" + XtreamPairFieldSequence.class.getSimpleName() + " annotation is required.\n--> Field: " + f.toGenericString());
            }
            try {
                final XtreamPairFieldSequence.Decoder decoderConfig = xtreamPairFieldSequence.decoder();
                final KeyMeta keyMeta = this.createKeyMeta(targetVersion, field, decoderConfig.key());

                final XtreamPairFieldSequence.Value valueConfig = decoderConfig.value();
                final ValueMatcherMetas valueMatcherMetas = this.createValueMatcherMetas(keyMeta.type(), targetVersion, f, valueConfig.commonParams(), valueConfig.matchers(), valueMatcherMeta -> {
                    if (valueMatcherMeta.length() <= 0) {
                        throw new IllegalArgumentException("""
                                
                                ===> @%s.length() must be greater than 0 when target element is <%s>
                                     %s
                                """.stripTrailing().formatted(ValueMatcher.class.getSimpleName(), Pair.class.getName(), valueMatcherMeta));
                    }
                    return valueMatcherMeta;
                });
                return new XtreamPairFieldSequenceMeta(new XtreamPairFieldSequenceMeta.DecoderMeta(
                        keyMeta,
                        valueMatcherMetas
                ));
            } catch (Exception e) {
                log.error("Error while creating XtreamPairFieldSequenceMeta\n===> Field: {}", field.toGenericString(), e);
                throw e;
            }
        });
    }

    @Override
    public XtreamTLVFieldSequenceMeta getXtreamTlvFieldSequenceMeta(int targetVersion, Field field) {
        final ConcurrentMap<Field, XtreamTLVFieldSequenceMeta> map = tlvFieldSequenceMetaCache.computeIfAbsent(targetVersion, k -> new ConcurrentHashMap<>());
        return map.computeIfAbsent(field, f -> {
            final XtreamTLVFieldSequence xtreamTlvFieldSequence = AnnotatedElementUtils.getMergedAnnotation(f, XtreamTLVFieldSequence.class);
            if (xtreamTlvFieldSequence == null) {
                throw new IllegalArgumentException("@" + XtreamTLVFieldSequence.class.getSimpleName() + " annotation is required.\n--> Field: " + f.toGenericString());
            }
            try {
                final XtreamTLVFieldSequence.Decoder decoderConfig = xtreamTlvFieldSequence.decoder();
                final KeyMeta keyMeta = this.createKeyMeta(targetVersion, field, decoderConfig.tag());

                final ValueLengthMeta valueLengthMeta = this.createValueLengthMeta(targetVersion, field, decoderConfig.length());

                final XtreamTLVFieldSequence.Value valueConfig = decoderConfig.value();
                final ValueMatcherMetas valueMatcherMetas = this.createValueMatcherMetas(keyMeta.type(), targetVersion, field, valueConfig.commonParams(), valueConfig.matchers(), Function.identity());
                final FallbackValueMatcherMeta fallbackValueMatcherMeta = this.createFallbackValueMatcherMeta(targetVersion, field, valueConfig.fallbackMatchers());
                return new XtreamTLVFieldSequenceMeta(new XtreamTLVFieldSequenceMeta.DecoderMeta(
                        keyMeta,
                        valueLengthMeta,
                        valueMatcherMetas,
                        fallbackValueMatcherMeta
                ));
            } catch (Exception e) {
                log.error("Error while creating XtreamTLVFieldSequenceMeta\n===> Field: {}", field.toGenericString(), e);
                throw e;
            }
        });
    }

    // ========================================================================
    // FallbackValueMatcher
    // ========================================================================

    FallbackValueMatcherMeta createFallbackValueMatcherMeta(int targetVersion, Field field, FallbackValueMatcher[] fallbackValueMatchers) {
        try {
            return this.doCreateFallbackValueMatcherMeta(targetVersion, fallbackValueMatchers);
        } catch (Exception e) {
            log.error("Error while creating FallbackValueMatcherMeta\n===> Field: {}", field.toGenericString(), e);
            throw e;
        }
    }

    private FallbackValueMatcherMeta doCreateFallbackValueMatcherMeta(int targetVersion, FallbackValueMatcher[] fallbackValueMatchers) {
        final VersionMatchResult<FallbackValueMatcher> matchResult = HasVersions.matchVersion(
                targetVersion,
                Arrays.stream(fallbackValueMatchers).map(it -> new HasVersions<>(it.version(), it)),
                HasVersion::data
        );
        final FallbackValueMatcher fallbackValueMatcher = matchResult.getIfAvailable();
        if (fallbackValueMatcher == null) {
            throw new IllegalArgumentException("No `[" + FallbackValueMatcher.class.getName() + "]` found for target version " + targetVersion);
        }
        final String detectedCharset = MetadataUtils.detectCharset(fallbackValueMatcher.valueType(), fallbackValueMatcher.valueCodec(), fallbackValueMatcher.charset(), null);
        final FieldCodec<Object> valueCodec = MetadataUtils.createValueCodec(this.beanMetadataRegistry, fallbackValueMatcher.valueType(), fallbackValueMatcher.valueCodec(), null, detectedCharset);
        if (valueCodec == null) {
            throw new IllegalArgumentException("Unsupported valueType: " + fallbackValueMatcher.valueType());
        }
        return new FallbackValueMatcherMeta(
                matchResult.version(),
                fallbackValueMatcher.valueType(),
                valueCodec,
                detectedCharset,
                fallbackValueMatcher.desc()
        );
    }

    // ========================================================================
    // ValueMatcher
    // ========================================================================

    ValueMatcherMetas createValueMatcherMetas(KeyType keyType, int targetVersion, Field field, ValueDecoderCommonParam[] commonParams, ValueMatcher[] valueMatchers, Function<ValueMatcherMeta, ValueMatcherMeta> mapper) {
        try {
            return this.doCreateValueDecoderMeta(keyType, targetVersion, commonParams, valueMatchers, mapper);
        } catch (Exception e) {
            log.error("Error while creating ValueMatcherMetas\n===> Field: {}", field.toGenericString(), e);
            throw e;
        }
    }

    private ValueMatcherMetas doCreateValueDecoderMeta(KeyType keyType, int targetVersion, ValueDecoderCommonParam[] commonParams, ValueMatcher[] valueMatchers, Function<ValueMatcherMeta, ValueMatcherMeta> mapper) {
        final ValueDecoderCommonParam commonParam = HasVersions.matchVersion(
                targetVersion,
                Arrays.stream(commonParams).map(it -> new HasVersions<>(it.version(), it)),
                HasVersion::data
        ).getIfAvailable();
        final Map<Object, ValueMatcherMeta> resultMap = new HashMap<>();
        final String fallbackCharset = commonParam == null ? null : commonParam.charset();

        Arrays.stream(valueMatchers)
                .flatMap(valueMatcher -> {
                    final int[] version = valueMatcher.version();
                    final Map<Integer, Integer> duplicateVersions = MetadataUtils.findDuplicateVersions(version);
                    if (!duplicateVersions.isEmpty()) {
                        final String tips = duplicateVersions.entrySet().stream().map(it -> "version: " + it.getKey() + ", times: " + it.getValue()).collect(Collectors.joining("\n- ", "- ", ""));
                        throw new IllegalArgumentException("Duplicate versions [@" + ValueMatcher.class.getName() + "]\n" + tips);
                    }
                    return Arrays.stream(version).mapToObj(v -> new HasVersion<>(v, valueMatcher));
                })
                .filter(hasVersion -> MetadataUtils.isVersionMatched(targetVersion, hasVersion.version()))
                .forEach(hasVersion -> {
                    final ValueMatcher matcher = hasVersion.data();
                    final String detectedCharset = MetadataUtils.detectCharset(matcher.valueType(), matcher.valueCodec(), matcher.charset(), fallbackCharset);
                    final String finalCharset = detectedCharset != null ? detectedCharset : fallbackCharset;
                    final FieldCodec<Object> valueCodec = MetadataUtils.createValueCodec(this.beanMetadataRegistry, matcher.valueType(), matcher.valueCodec(), matcher.valueEntity(), finalCharset);
                    if (valueCodec == null) {
                        throw new IllegalArgumentException("Unsupported valueType: " + matcher.valueType());
                    }

                    final List<?> keyList = MetadataUtils.readKey(keyType, matcher);
                    if (keyList == null) {
                        return;
                    }
                    for (Object key : keyList) {
                        final ValueMatcherMeta newMatcher = new ValueMatcherMeta(hasVersion.version(), key, matcher.length(), matcher.valueType(), valueCodec, matcher.valueEntity(), finalCharset, matcher.desc());
                        final ValueMatcherMeta existedMatcher = resultMap.get(key);
                        if (existedMatcher == null) {
                            resultMap.put(key, mapper.apply(newMatcher));
                        } else {
                            final int existedVersion = existedMatcher.version();
                            final int newVersion = newMatcher.version();
                            if (existedVersion == XtreamField.ALL_VERSION) {
                                if (newVersion == XtreamField.ALL_VERSION) {
                                    throw new IllegalArgumentException("Duplicate valueMatcher. Key: " + MetadataUtils.formateKey(key) + "\n"
                                            + "- " + existedMatcher + "\n"
                                            + "- " + newMatcher);
                                } else {
                                    resultMap.put(key, mapper.apply(newMatcher));
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

        final List<ValueMatcherMeta> list = resultMap.values().stream().toList();
        return new ValueMatcherMetas(list);
    }

    // ========================================================================
    // Delegates to MetadataUtils
    // ========================================================================

    KeyMeta createKeyMeta(int targetVersion, Field field, Key[] keys) {
        try {
            return MetadataUtils.createKeyMeta(targetVersion, keys);
        } catch (Exception e) {
            log.error("Error while creating KeyMeta\n===> Field: {}", field.toGenericString(), e);
            throw e;
        }
    }

    ValueLengthMeta createValueLengthMeta(int targetVersion, Field field, ValueLength[] valueLengths) {
        try {
            return MetadataUtils.createValueLengthMeta(targetVersion, valueLengths);
        } catch (Exception e) {
            log.error("Error while creating ValueLengthMeta\n===> Field: {}", field.toGenericString(), e);
            throw e;
        }
    }
}
