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

package io.github.hylexus.xtream.codec.core.impl;

import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.ExtendMetaRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.ext.*;
import io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField;
import io.github.hylexus.xtream.codec.core.annotation.pair.XtreamPairFieldSequence;
import io.github.hylexus.xtream.codec.core.annotation.tlv.XtreamTLVFieldSequence;
import io.github.hylexus.xtream.codec.core.impl.codec.CharSequenceFieldCodec;
import io.github.hylexus.xtream.codec.core.impl.codec.StringFieldCodecs;
import io.github.hylexus.xtream.codec.core.impl.codec.utils.HasVersion;
import io.github.hylexus.xtream.codec.core.impl.codec.utils.HasVersions;
import io.github.hylexus.xtream.codec.core.impl.codec.utils.VersionMatchResult;
import io.github.hylexus.xtream.codec.core.impl.domain.*;
import io.github.hylexus.xtream.codec.core.type.CodecCharset;
import io.github.hylexus.xtream.codec.core.type.Pair;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.hylexus.xtream.codec.common.utils.XtreamAssertions.assertNotBlank;
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
        final String detectedCharset = detectCharset(fallbackValueMatcher.valueType(), fallbackValueMatcher.valueCodec(), fallbackValueMatcher.charset(), null);
        final FieldCodec<Object> valueCodec = createValueCodec(targetVersion, fallbackValueMatcher.valueType(), fallbackValueMatcher.valueCodec(), null, detectedCharset);
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
                    final Map<Integer, Integer> duplicateVersions = findDuplicateVersions(version);
                    if (!duplicateVersions.isEmpty()) {
                        final String tips = duplicateVersions.entrySet().stream().map(it -> "version: " + it.getKey() + ", times: " + it.getValue()).collect(Collectors.joining("\n- ", "- ", ""));
                        throw new IllegalArgumentException("Duplicate versions [@" + ValueMatcher.class.getName() + "]\n" + tips);
                    }
                    return Arrays.stream(version).mapToObj(v -> new HasVersion<>(v, valueMatcher));
                })
                .filter(hasVersion -> isVersionMatched(targetVersion, hasVersion.version()))
                .forEach(hasVersion -> {
                    final ValueMatcher matcher = hasVersion.data();
                    final String detectedCharset = detectCharset(matcher.valueType(), matcher.valueCodec(), matcher.charset(), fallbackCharset);
                    final String finalCharset = detectedCharset != null ? detectedCharset : fallbackCharset;
                    final FieldCodec<Object> valueCodec = createValueCodec(targetVersion, matcher.valueType(), matcher.valueCodec(), matcher.valueEntity(), finalCharset);
                    if (valueCodec == null) {
                        throw new IllegalArgumentException("Unsupported valueType: " + matcher.valueType());
                    }

                    final Object key = readKey(keyType, matcher);
                    if (key == null) {
                        return;
                    }
                    final ValueMatcherMeta newMatcher = new ValueMatcherMeta(hasVersion.version(), key, matcher.length(), matcher.valueType(), valueCodec, matcher.valueEntity(), finalCharset, matcher.desc());
                    final ValueMatcherMeta existedMatcher = resultMap.get(key);
                    if (existedMatcher == null) {
                        resultMap.put(key, mapper.apply(newMatcher));
                    } else {
                        final int existedVersion = existedMatcher.version();
                        final int newVersion = newMatcher.version();
                        if (existedVersion == XtreamField.ALL_VERSION) {
                            if (newVersion == XtreamField.ALL_VERSION) {
                                throw new IllegalArgumentException("Duplicate valueMatcher. Key: " + key + "\n"
                                        + "- " + existedMatcher + "\n"
                                        + "- " + newMatcher);
                            } else {
                                resultMap.put(key, mapper.apply(newMatcher));
                            }
                        } else {
                            if (newVersion != XtreamField.ALL_VERSION) {
                                throw new IllegalArgumentException("Duplicate valueMatcher. Key: " + key + "\n"
                                        + "- " + existedMatcher + "\n"
                                        + "- " + newMatcher);
                            }
                        }
                    }
                });

        final List<ValueMatcherMeta> list = resultMap.values().stream().toList();
        return new ValueMatcherMetas(list);
    }

    ValueLengthMeta createValueLengthMeta(int targetVersion, Field field, ValueLength[] valueLengths) {
        try {
            return doCreateValueLengthMeta(targetVersion, valueLengths);
        } catch (Exception e) {
            log.error("Error while creating ValueLengthMeta\n===> Field: {}", field.toGenericString(), e);
            throw e;
        }
    }

    private static ValueLengthMeta doCreateValueLengthMeta(int targetVersion, ValueLength[] valueLengths) {
        final VersionMatchResult<ValueLength> matchResult = HasVersions.matchVersion(
                targetVersion,
                Arrays.stream(valueLengths).map(it -> new HasVersions<>(it.version(), it)),
                HasVersion::data
        );
        if (matchResult.matched()) {
            final ValueLength valueLengthType = matchResult.source();
            return new ValueLengthMeta(targetVersion, valueLengthType.type());
        }
        throw new IllegalArgumentException("No `[" + ValueLength.class.getName() + "]` found for target version " + targetVersion);
    }

    KeyMeta createKeyMeta(int targetVersion, Field field, Key[] keys) {
        try {
            return this.doCreateKeyMeta(targetVersion, keys);
        } catch (Exception e) {
            log.error("Error while creating KeyMeta\n===> Field: {}", field.toGenericString(), e);
            throw e;
        }
    }

    private KeyMeta doCreateKeyMeta(int targetVersion, Key[] keys) {
        final VersionMatchResult<Key> matchResult = HasVersions.matchVersion(
                targetVersion,
                Arrays.stream(keys).map(it -> new HasVersions<>(it.version(), it)),
                HasVersion::data
        );
        if (!matchResult.matched()) {
            throw new IllegalArgumentException("No `[" + Key.class.getName() + "]` found for target version " + targetVersion);
        }
        final Key key = matchResult.source();
        final int sizeInBytes = key.type().dataType().sizeInBytes() <= 0
                ? key.sizeInBytes()
                : key.type().dataType().sizeInBytes();
        if (sizeInBytes <= 0) {
            throw new IllegalArgumentException("Invalid sizeInBytes: " + sizeInBytes + "\n\n---> " + key);
        }
        final FieldCodec<Object> keyCodec = createKeyCodec(key);
        final String keyCharset = parseKeyCharset(key);
        return new KeyMeta(matchResult.version(), key.type(), sizeInBytes, keyCharset, key.paddingType(), key.paddingElement(), keyCodec);
    }

    private static FieldCodec<Object> createKeyCodec(Key key) {
        final KeyType keyType = key.type();
        return switch (keyType) {
            case i8, u8, i16, u16, i32, u32, i64,
                 str_gbk, str_utf8, str_gb_2312 -> keyType.dataType().codec();
            case str -> StringFieldCodecs.createStringCodecAndCastToObject(requireNonNull(key.charset(), Key.class.getName() + ".charset() is required"));
        };
    }

    static String parseKeyCharset(Key key) {
        final KeyType type = key.type();
        return switch (type) {
            case str_gb_2312 -> XtreamConstants.CHARSET_NAME_GB_2312;
            case str_gbk -> XtreamConstants.CHARSET_NAME_GBK;
            case str_utf8 -> XtreamConstants.CHARSET_NAME_UTF8;
            case str -> key.charset();
            default -> XtreamMapField.DEFAULT_CHARSET;
        };
    }

    private static boolean isVersionMatched(int targetVersion, int versionCandidate) {
        return targetVersion == versionCandidate || versionCandidate == XtreamField.ALL_VERSION;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private FieldCodec<Object> createValueCodec(
            int version,
            XtreamDataType valueType,
            Class<? extends FieldCodec<?>> valueCodecClass,
            @Nullable Class<?> valueEntityClass,
            @Nullable String actualCharset) {

        final FieldCodec<?> instance = this.beanMetadataRegistry.getOrCreateFieldCodec(version, valueType, valueCodecClass, actualCharset, valueEntityClass);
        return (FieldCodec<Object>) instance;
    }

    static @Nullable String detectCharset(
            XtreamDataType valueType,
            @Nullable Class<? extends FieldCodec<?>> codecClass,
            @Nullable String charset,
            @Nullable String commonCharset) {

        if (!valueType.isPlaceholder()) {
            final CodecCharset codecCharset = valueType.codecCharset();
            return switch (codecCharset) {
                case DYNAMIC -> assertNotBlank(firstOr(charset, commonCharset), "charset is empty");
                case UTF_8,
                     GBK, GB_2312,
                     BCD_8421, HEX -> codecCharset.charsetName();
                case UNSUPPORTED -> null;
            };
        }

        if (codecClass != null && !Objects.equals(FieldCodec.Placeholder.class, codecClass)) {
            if (StringFieldCodecs.StringFieldCodec.class.equals(codecClass)) {
                return assertNotBlank(firstOr(charset, commonCharset), "charset is empty");
            }
            if (CharSequenceFieldCodec.class.isAssignableFrom(codecClass)) {
                if (StringFieldCodecs.StringFieldCodecGbk.class.equals(codecClass)) {
                    return XtreamConstants.CHARSET_NAME_GBK;
                } else if (StringFieldCodecs.StringFieldCodecGb2312.class.equals(codecClass)) {
                    return XtreamConstants.CHARSET_NAME_GB_2312;
                } else if (StringFieldCodecs.StringFieldCodecUtf8.class.equals(codecClass)) {
                    return XtreamConstants.CHARSET_NAME_UTF8;
                } else if (StringFieldCodecs.StringFieldCodecBcd8421.class.equals(codecClass)) {
                    return XtreamConstants.CHARSET_NAME_BCD_8421;
                } else if (StringFieldCodecs.StringFieldCodecHex.class.equals(codecClass)) {
                    return XtreamConstants.CHARSET_NAME_HEX;
                } else {
                    return assertNotBlank(firstOr(charset, commonCharset), "charset is empty");
                }
            }
        }
        return null;
    }

    private static String firstOr(@Nullable String charset, @Nullable String fallbackCharset) {
        if (StringUtils.hasText(charset)) {
            return charset;
        }
        return requireNonNull(fallbackCharset);
    }

    static Map<Integer, Integer> findDuplicateVersions(int[] version) {
        final Map<Integer, Integer> countMap = new HashMap<>();

        for (final int num : version) {
            countMap.put(num, countMap.getOrDefault(num, 0) + 1);
        }

        // 过滤出出现次数 >= 2 的（即重复的）
        final Map<Integer, Integer> duplicates = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() >= 2) {
                duplicates.put(entry.getKey(), entry.getValue());
            }
        }

        return duplicates;
    }

    private static @Nullable Object readKey(KeyType keyType, ValueMatcher valueMatcher) {
        checkExclusive(valueMatcher);
        return switch (keyType) {
            // todo 这里先取第一个 后续优化支持多个值
            case i8 -> valueMatcher.matchI8().length > 0 ? valueMatcher.matchI8()[0] : null;
            case u8 -> valueMatcher.matchU8().length > 0 ? valueMatcher.matchU8()[0] : null;
            case i16 -> valueMatcher.matchI16().length > 0 ? valueMatcher.matchI16()[0] : null;
            case u16 -> valueMatcher.matchU16().length > 0 ? valueMatcher.matchU16()[0] : null;
            case i32 -> valueMatcher.matchI32().length > 0 ? valueMatcher.matchI32()[0] : null;
            case u32 -> valueMatcher.matchU32().length > 0 ? valueMatcher.matchU32()[0] : null;
            case i64 -> valueMatcher.matchI64().length > 0 ? valueMatcher.matchI64()[0] : null;
            case str_gbk, str_gb_2312, str_utf8, str -> valueMatcher.matchString().length > 0 ? valueMatcher.matchString() : null;
        };
    }

    private static void checkExclusive(ValueMatcher vm) {
        final Map<String, Integer> fields = new LinkedHashMap<>();
        fields.put("matchI8", vm.matchI8().length);
        fields.put("matchU8", vm.matchU8().length);
        fields.put("matchI16", vm.matchI16().length);
        fields.put("matchU16", vm.matchU16().length);
        fields.put("matchI32", vm.matchI32().length);
        fields.put("matchU32", vm.matchU32().length);
        fields.put("matchI64", vm.matchI64().length);
        fields.put("matchString", vm.matchString().length);

        final Set<String> set = fields.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if (set.isEmpty()) {
            throw new IllegalArgumentException("""
                    
                    ValueMatcher must specify exactly ONE match type. But none were set.
                    Expected one of: %s
                    \t==> %s
                    """.stripTrailing().formatted(String.join(", ", fields.keySet()), vm));
        }
        if (set.size() > 1) {
            throw new IllegalArgumentException("""
                    
                    ValueMatcher must specify exactly ONE match type. But multiple were set.
                    Conflicting attributes: %s
                    ONLY ONE of them should be specified.
                    \t==> %s
                    """.stripTrailing().formatted(String.join(", ", set), vm));
        }
    }

}
