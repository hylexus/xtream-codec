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

package io.github.hylexus.xtream.codec.core.impl.codec.map;

import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField;
import io.github.hylexus.xtream.codec.core.impl.codec.CharSequenceFieldCodec;
import io.github.hylexus.xtream.codec.core.impl.codec.StringFieldCodecs;
import io.github.hylexus.xtream.codec.core.type.CodecCharset;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.hylexus.xtream.codec.common.utils.XtreamAssertions.assertNotBlank;
import static java.util.Objects.requireNonNull;

// todo 移动到 BeanMetadataRegistry ?
public class SimpleMapMetadataRegistry {

    private static final Logger log = LoggerFactory.getLogger(SimpleMapMetadataRegistry.class);
    private static final ConcurrentMap<Integer, ConcurrentMap<Field, MapMeta>> CACHE = new ConcurrentHashMap<>();

    static MapMeta createMapMetadata(BeanMetadataRegistry beanMetadataRegistry, int targetVersion, Field field, XtreamMapField xtreamMapField) {
        final ConcurrentMap<Field, MapMeta> map = CACHE.computeIfAbsent(targetVersion, k -> new ConcurrentHashMap<>());
        return map.computeIfAbsent(field, f -> {
            final MapMeta mapMeta = doCreateMapMetadata(beanMetadataRegistry, targetVersion, xtreamMapField);
            if (log.isDebugEnabled()) {
                log.debug("Field:{}, MapMeta: {}", field.toGenericString(), mapMeta);
            }
            return mapMeta;
        });
    }

    private static MapMeta doCreateMapMetadata(BeanMetadataRegistry beanMetadataRegistry, int targetVersion, XtreamMapField xtreamMapField) {
        final KeyMeta keyMeta = createKeyMeta(targetVersion, xtreamMapField);
        final ValueLengthMeta valueLengthMeta = createValueLengthMeta(targetVersion, xtreamMapField);
        final ValueMeta valueMeta = createValueMeta(beanMetadataRegistry, targetVersion, keyMeta, xtreamMapField.value());
        return new MapMeta(keyMeta, valueLengthMeta, valueMeta);
    }

    private static ValueMeta createValueMeta(BeanMetadataRegistry beanMetadataRegistry, int targetVersion, KeyMeta keyMeta, XtreamMapField.Value value) {
        final EncoderValueMeta encoderValueMeta = createEncoderValueMata(beanMetadataRegistry, targetVersion, keyMeta, value);
        final DecoderValueMeta decoderValueMeta = createDecoderValueMata(beanMetadataRegistry, targetVersion, keyMeta, value);
        return new ValueMeta(encoderValueMeta, decoderValueMeta);
    }

    private static EncoderValueMeta createEncoderValueMata(BeanMetadataRegistry beanMetadataRegistry, int targetVersion, KeyMeta keyMeta, XtreamMapField.Value value) {
        final VersionMatchResult<EncoderCommonParam> matchResult = matchVersion(
                targetVersion,
                Arrays.stream(value.encoder().params()).map(param -> new HasVersions<>(param.version(), param)),
                hasVersion -> {
                    final XtreamMapField.EncoderParam data = hasVersion.data();
                    return new EncoderCommonParam(hasVersion.version(), data.writeNumberAsLittleEndian(), data.charset());
                }
        );
        final EncoderCommonParam commonParam = matchResult.matched() ? matchResult.source() : new EncoderCommonParam(targetVersion, false, XtreamMapField.DEFAULT_CHARSET);
        log.info("matchResult: {}", matchResult);
        final List<ValueMatcherMeta> encoderValueMatchers = createValueMatcherMetaList(beanMetadataRegistry, targetVersion, keyMeta, value.encoder().matchers(), commonParam, null);
        return new EncoderValueMeta(commonParam, encoderValueMatchers);
    }

    private static DecoderValueMeta createDecoderValueMata(BeanMetadataRegistry beanMetadataRegistry, int targetVersion, KeyMeta keyType, XtreamMapField.Value value) {
        final VersionMatchResult<DecoderCommonParam> matchResult = matchVersion(
                targetVersion,
                Arrays.stream(value.decoder().params()).map(param -> new HasVersions<>(param.version(), param)),
                hasVersion -> {
                    final XtreamMapField.DecoderParam data = hasVersion.data();
                    return new DecoderCommonParam(hasVersion.version(), data.charset());
                }
        );
        final DecoderCommonParam commonParam = matchResult.matched()
                ? matchResult.source()
                : new DecoderCommonParam(targetVersion, XtreamMapField.DEFAULT_CHARSET);
        final FallbackValueMatcherMeta fallbackValueMatcherMeta = createFallbackValueMatcherMeta(beanMetadataRegistry, targetVersion, value.decoder().fallbackMatchers(), commonParam);
        final List<ValueMatcherMeta> decoderValueMatchers = createValueMatcherMetaList(beanMetadataRegistry, targetVersion, keyType, value.decoder().matchers(), null, commonParam);
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

    private static FallbackValueMatcherMeta createFallbackValueMatcherMeta(BeanMetadataRegistry beanMetadataRegistry, int targetVersion, XtreamMapField.FallbackValueMatcher[] matchers, DecoderCommonParam commonParam) {
        if (matchers.length == 0) {
            throw new IllegalArgumentException("No fallbackValueMatcher found");
        }
        final VersionMatchResult<XtreamMapField.FallbackValueMatcher> matchResult = matchVersion(
                targetVersion,
                Arrays.stream(matchers).map(it -> new HasVersions<>(it.version(), it)),
                HasVersion::data
        );
        log.info("Fallback targetVersion={}, {}", targetVersion, matchResult);
        if (!matchResult.matched()) {
            throw new IllegalArgumentException("No fallbackValueMatcher found");
        }
        final int matchedVersion = matchResult.version();
        final XtreamMapField.FallbackValueMatcher matcher = matchResult.source();
        final String actualCharset = detectCharset(matcher, commonParam.stringTypeCharset());
        final FieldCodec<Object> valueCodec = createValueCodec(beanMetadataRegistry, targetVersion, actualCharset, matcher);
        return new FallbackValueMatcherMeta(matchedVersion, matcher.valueType(), requireNonNull(valueCodec), actualCharset);
    }

    @Nullable
    private static FieldCodec<Object> createValueCodec(
            BeanMetadataRegistry beanMetadataRegistry,
            int version,
            @Nullable String actualCharset,
            XtreamMapField.ValueMatcher matcher) {

        return createValueCodec(beanMetadataRegistry, version,
                matcher.valueType(), matcher.valueCodec(), matcher.valueEntity(), actualCharset);
    }

    @Nullable
    private static FieldCodec<Object> createValueCodec(
            BeanMetadataRegistry beanMetadataRegistry,
            int version,
            @Nullable String actualCharset,
            XtreamMapField.FallbackValueMatcher matcher) {

        return createValueCodec(beanMetadataRegistry, version,
                matcher.valueType(), matcher.valueCodecClass(), null, actualCharset);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static FieldCodec<Object> createValueCodec(
            BeanMetadataRegistry beanMetadataRegistry,
            int version,
            XtreamDataType valueType,
            Class<? extends FieldCodec<?>> valueCodecClass,
            @Nullable Class<?> valueEntityClass,
            @Nullable String actualCharset) {

        final FieldCodec<?> instance = beanMetadataRegistry.getOrCreateFieldCodec(version, valueType, valueCodecClass, actualCharset, valueEntityClass);
        return (FieldCodec<Object>) instance;
    }

    private static String firstOr(@Nullable String charset, @Nullable String fallbackCharset) {
        if (StringUtils.hasText(charset)) {
            return charset;
        }
        return requireNonNull(fallbackCharset);
    }

    @Nullable
    static String detectCharset(XtreamMapField.FallbackValueMatcher matcher, String fallbackCharset) {
        return detectCharset(matcher.valueType(), matcher.valueCodecClass(), matcher.charset(), fallbackCharset);
    }

    @Nullable
    static String detectCharset(
            XtreamMapField.ValueMatcher matcher,
            @Nullable EncoderCommonParam encoderCommonParam,
            @Nullable DecoderCommonParam decoderCommonParam) {

        final String fallbackCharset = encoderCommonParam != null
                ? encoderCommonParam.stringTypeCharset()
                : requireNonNull(decoderCommonParam, "Only one of DecoderCommonParam and EncoderCommonParam can be null.").stringTypeCharset();
        return detectCharset(matcher.valueType(), matcher.valueCodec(), matcher.charset(), fallbackCharset);
    }

    @Nullable
    static String detectCharset(
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

    private static FieldCodec<Object> createKeyCodec(XtreamMapField.Key key) {
        final XtreamMapField.KeyType keyType = key.type();
        return switch (keyType) {
            case i8, u8, i16, u16, i32, u32, i64,
                 str_gbk, str_utf8, str_gb_2312 -> keyType.type().codec();
            case str -> StringFieldCodecs.createStringCodecAndCastToObject(requireNonNull(key.charset(), XtreamMapField.Key.class.getName() + ".charset() is required"));
        };
    }

    private static Object readKey(KeyMeta keyMeta, XtreamMapField.ValueMatcher valueMatcher) {
        final XtreamMapField.KeyType keyType = keyMeta.type();
        return switch (keyType) {
            case i8 -> valueMatcher.matchI8();
            case u8 -> valueMatcher.matchU8();
            case i16 -> valueMatcher.matchI16();
            case u16 -> valueMatcher.matchU16();
            case i32 -> valueMatcher.matchI32();
            case u32 -> valueMatcher.matchU32();
            case i64 -> valueMatcher.matchI64();
            case str_gbk, str_gb_2312, str_utf8, str -> assertNotBlank(valueMatcher.matchString(), "key cannot be empty");
        };
    }

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

    private static List<ValueMatcherMeta> createValueMatcherMetaList(
            BeanMetadataRegistry beanMetadataRegistry,
            int targetVersion,
            KeyMeta keyMeta,
            XtreamMapField.ValueMatcher[] valueMatchers,
            @Nullable EncoderCommonParam encoderCommonParam,
            @Nullable DecoderCommonParam decoderCommonParam) {

        final Map<Object, List<XtreamMapField.ValueMatcher>> historyValueMatchersByVersion = new HashMap<>();
        final List<ValueMatcherMeta> results = Arrays.stream(valueMatchers)
                .flatMap(matcher -> {
                    final int[] version = matcher.version();
                    final Map<Integer, Integer> duplicateVersions = findDuplicateVersions(version);
                    if (!duplicateVersions.isEmpty()) {
                        final String tips = duplicateVersions.entrySet().stream().map(it -> "version: " + it.getKey() + ", times: " + it.getValue()).collect(Collectors.joining("\n- ", "- ", ""));
                        throw new IllegalArgumentException("Duplicate versions [@" + XtreamMapField.ValueMatcher.class.getName() + "]\n" + tips);
                    }
                    return Arrays.stream(version).mapToObj(v -> new HasVersion<>(v, matcher));
                })
                .filter(hasVersion -> isVersionMatched(targetVersion, hasVersion.version()))
                .map(hasVersion -> {
                    final XtreamMapField.ValueMatcher matcher = hasVersion.data();
                    final String actualCharset = detectCharset(matcher, encoderCommonParam, decoderCommonParam);
                    final FieldCodec<Object> valueCodec = createValueCodec(beanMetadataRegistry, targetVersion, actualCharset, matcher);
                    if (valueCodec == null) {
                        throw new IllegalArgumentException("Unsupported valueType: " + matcher.valueType());
                    }

                    final Object key = readKey(keyMeta, matcher);
                    final ValueMatcherMeta valueMatcherMeta = new ValueMatcherMeta(hasVersion.version(), key, matcher.valueType(), valueCodec, actualCharset);

                    historyValueMatchersByVersion.computeIfAbsent(key, k -> new ArrayList<>()).add(matcher);

                    return valueMatcherMeta;
                }).toList();

        historyValueMatchersByVersion.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .findFirst()
                .ifPresent(entry -> {
                    final Object key = entry.getKey();
                    final List<XtreamMapField.ValueMatcher> annotations = entry.getValue();
                    final String error = annotations.stream().map(Object::toString).collect(Collectors.joining("\n\t"));
                    log.warn("Duplicate valueMatchers. Key: {}", key);
                    throw new IllegalArgumentException("Duplicate valueMatchers." + "\nKey: " + key + "\nComponents:\n\t" + error);
                });
        return results;
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

    private static KeyMeta createKeyMeta(int targetVersion, XtreamMapField xtreamMapField) {
        final VersionMatchResult<XtreamMapField.Key> matchResult = matchVersion(
                targetVersion,
                Arrays.stream(xtreamMapField.key()).map(it -> new HasVersions<>(it.version(), it)),
                HasVersion::data
        );
        log.info("KEY: targetVersion={}, {}", targetVersion, matchResult);
        if (matchResult.matched()) {
            final XtreamMapField.Key key = requireNonNull(matchResult.source());

            final int sizeInBytes = key.type().type().sizeInBytes() <= 0
                    ? key.sizeInBytes()
                    : key.type().type().sizeInBytes();
            if (sizeInBytes <= 0) {
                throw new IllegalArgumentException("Invalid sizeInBytes: " + sizeInBytes);
            }
            final FieldCodec<Object> keyCodec = createKeyCodec(key);
            return new KeyMeta(targetVersion, key.type(), sizeInBytes, key.charset(), keyCodec);
        }

        throw new IllegalArgumentException("No `[" + XtreamMapField.Key.class.getName() + "]` found for target version " + targetVersion);
    }

    private static ValueLengthMeta createValueLengthMeta(int targetVersion, XtreamMapField xtreamMapField) {
        final VersionMatchResult<XtreamMapField.ValueLength> matchResult = matchVersion(
                targetVersion,
                Arrays.stream(xtreamMapField.valueLength()).map(it -> new HasVersions<>(it.version(), it)),
                HasVersion::data);

        log.info("ValueLength: targetVersion={}, {}", targetVersion, matchResult);
        if (matchResult.matched()) {
            final XtreamMapField.ValueLength valueLengthType = matchResult.source();
            return new ValueLengthMeta(targetVersion, valueLengthType.type());
        }
        throw new IllegalArgumentException("No value length type found for target version " + targetVersion);
    }

    private static boolean isVersionMatched(int targetVersion, int versionCandidate) {
        return targetVersion == versionCandidate || versionCandidate == XtreamField.ALL_VERSION;
    }


    record HasVersions<S>(int[] version, S source) {

        @SuppressWarnings("redundent")
        HasVersions {
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", HasVersions.class.getSimpleName() + "[", "]")
                    .add("version=" + Arrays.toString(version))
                    .add("source=" + source)
                    .toString();
        }
    }

    record HasVersion<S>(int version, S data) {
        @SuppressWarnings("redundent")
        HasVersion {
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", HasVersion.class.getSimpleName() + "[", "]")
                    .add("version=" + version)
                    .add("data=" + data)
                    .toString();
        }
    }

    private record VersionMatchResult<S>(boolean matched, int version, @Nullable S source) {
        @SuppressWarnings("redundent")
        private VersionMatchResult {
        }

        @Override
        public S source() {
            if (this.source == null) {
                throw new IllegalStateException("source cannot be null");
            }
            return source;
        }
    }

    static <S, T> VersionMatchResult<T> matchVersion(int targetVersion, Stream<HasVersions<S>> stream, Function<HasVersion<S>, T> mapper) {
        final Map<Integer, List<HasVersion<S>>> group = stream
                .flatMap(it -> {
                    final int[] version = it.version();
                    if (version.length > 1) {
                        if (Arrays.binarySearch(version, XtreamMapField.ALL_VERSION) >= 0) {
                            throw new IllegalArgumentException(
                                    "Cannot use `XtreamMapField.ALL_VERSION` with other versions\n"
                                    + "Components:\n\n" + it.source());
                        }
                    }
                    return Arrays.stream(version)
                            .mapToObj(v -> new HasVersion<>(v, it.source()));
                })
                .collect(Collectors.groupingBy(HasVersion::version));

        for (Map.Entry<Integer, List<HasVersion<S>>> entry : group.entrySet()) {
            final int v = entry.getKey();
            final List<HasVersion<S>> list = entry.getValue();
            if (list.size() > 1) {
                throw new IllegalArgumentException(
                        "Duplicate version(" + list.getFirst().data().getClass().getSimpleName()
                        + ").\nKey: " + v
                        + "\nComponents: \n\t" + list.stream().limit(5).map(HasVersion::data).map(Object::toString).collect(Collectors.joining("\n\t")));
            }
        }

        final List<HasVersion<S>> matchedList = group.get(targetVersion);
        if (matchedList != null) {
            final HasVersion<S> first = matchedList.getFirst();
            return new VersionMatchResult<>(true, first.version(), mapper.apply(first));
        }

        if (targetVersion == XtreamField.ALL_VERSION) {
            return new VersionMatchResult<>(false, 0, null);
        }

        final List<HasVersion<S>> defaultList = group.get(XtreamMapField.ALL_VERSION);
        if (defaultList != null) {
            final HasVersion<S> first = defaultList.getFirst();
            return new VersionMatchResult<>(true, first.version(), mapper.apply(first));
        }

        return new VersionMatchResult<>(false, 0, null);
    }

    record MapMeta(
            KeyMeta keyMeta,
            ValueLengthMeta valueLengthMeta,
            ValueMeta valueMeta) {
        @SuppressWarnings("redundent")
        MapMeta {
        }
    }

    record KeyMeta(
            int version,
            XtreamMapField.KeyType type,
            int sizeInBytes,
            String charset,
            FieldCodec<Object> codec
    ) {
        @SuppressWarnings("redundent")
        KeyMeta {
        }
    }

    record ValueLengthMeta(
            int version,
            XtreamMapField.ValueLengthType type,
            FieldCodec<Object> codec) {

        public ValueLengthMeta(int version, XtreamMapField.ValueLengthType type) {
            this(version, type, type.type().codec());
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

    record ValueMatcherMeta(
            int version,
            Object key,
            XtreamDataType valueType,
            FieldCodec<Object> valueCodec,
            @Nullable String charset) {

        @SuppressWarnings("redundent")
        public ValueMatcherMeta {
        }
    }

    record FallbackValueMatcherMeta(
            int version,
            XtreamDataType valueType,
            FieldCodec<Object> codec,
            @Nullable String charset) {

        @SuppressWarnings("redundent")
        FallbackValueMatcherMeta {
        }
    }
}
