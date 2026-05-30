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

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.ext.Key;
import io.github.hylexus.xtream.codec.core.annotation.ext.KeyType;
import io.github.hylexus.xtream.codec.core.annotation.ext.ValueLength;
import io.github.hylexus.xtream.codec.core.annotation.ext.ValueMatcher;
import io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField;
import io.github.hylexus.xtream.codec.core.impl.DefaultExtendMetaRegistry;
import io.github.hylexus.xtream.codec.core.impl.codec.CharSequenceFieldCodec;
import io.github.hylexus.xtream.codec.core.impl.codec.StringFieldCodecs;
import io.github.hylexus.xtream.codec.core.impl.domain.KeyMeta;
import io.github.hylexus.xtream.codec.core.impl.domain.ValueLengthMeta;
import io.github.hylexus.xtream.codec.core.type.CodecCharset;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.hylexus.xtream.codec.common.utils.XtreamAssertions.assertNotBlank;
import static java.util.Objects.requireNonNull;

/**
 * 共享元数据工具类，封装 {@link DefaultExtendMetaRegistry} 和
 * {@link io.github.hylexus.xtream.codec.core.impl.codec.map.SimpleMapMetadataRegistry}
 * 之间重复的 Key/ValueLength/ValueMatcher 解析逻辑。
 * <p>
 * 所有方法均为纯函数式静态方法，不持有实例状态。
 */
public final class MetadataUtils {

    private static final Logger log = LoggerFactory.getLogger(MetadataUtils.class);

    private MetadataUtils() {
    }

    // ========================================================================
    // Key / KeyMeta
    // ========================================================================

    public static KeyMeta createKeyMeta(int targetVersion, Key[] keys) {
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

    public static FieldCodec<Object> createKeyCodec(Key key) {
        final KeyType keyType = key.type();
        return switch (keyType) {
            case i8, u8, i16, u16, i32, u32, i64,
                 str_gbk, str_utf8, str_gb_2312 -> keyType.dataType().codec();
            case str -> StringFieldCodecs.createStringCodecAndCastToObject(
                    requireNonNull(key.charset(), Key.class.getName() + ".charset() is required"));
        };
    }

    public static String parseKeyCharset(Key key) {
        final KeyType type = key.type();
        return switch (type) {
            case str_gb_2312 -> XtreamConstants.CHARSET_NAME_GB_2312;
            case str_gbk -> XtreamConstants.CHARSET_NAME_GBK;
            case str_utf8 -> XtreamConstants.CHARSET_NAME_UTF8;
            case str -> key.charset();
            default -> XtreamMapField.DEFAULT_CHARSET;
        };
    }

    // ========================================================================
    // ValueLength / ValueLengthMeta
    // ========================================================================

    public static ValueLengthMeta createValueLengthMeta(int targetVersion, ValueLength[] valueLengths) {
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

    // ========================================================================
    // Version matching utilities
    // ========================================================================

    public static boolean isVersionMatched(int targetVersion, int versionCandidate) {
        return targetVersion == versionCandidate || versionCandidate == XtreamField.ALL_VERSION;
    }

    public static Map<Integer, Integer> findDuplicateVersions(int[] version) {
        final Map<Integer, Integer> countMap = new HashMap<>();
        for (final int num : version) {
            countMap.put(num, countMap.getOrDefault(num, 0) + 1);
        }
        final Map<Integer, Integer> duplicates = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() >= 2) {
                duplicates.put(entry.getKey(), entry.getValue());
            }
        }
        return duplicates;
    }

    // ========================================================================
    // FieldCodec creation
    // ========================================================================

    @Nullable
    @SuppressWarnings("unchecked")
    public static FieldCodec<Object> createValueCodec(
            BeanMetadataRegistry beanMetadataRegistry,
            XtreamDataType valueType,
            Class<? extends FieldCodec<?>> valueCodecClass,
            @Nullable Class<?> valueEntityClass,
            @Nullable String actualCharset) {

        final FieldCodec<?> instance = beanMetadataRegistry.getOrCreateFieldCodec(valueType, valueCodecClass, actualCharset, valueEntityClass);
        return (FieldCodec<Object>) instance;
    }

    // ========================================================================
    // Charset detection
    // ========================================================================

    @Nullable
    public static String detectCharset(
            XtreamDataType valueType,
            @Nullable Class<? extends FieldCodec<?>> codecClass,
            @Nullable String charset,
            @Nullable String commonCharset) {

        if (!valueType.isPlaceholder()) {
            final CodecCharset codecCharset = valueType.codecCharset();
            return switch (codecCharset) {
                case DYNAMIC -> assertNotBlank(firstOr(charset, commonCharset), "charset is empty");
                case UTF_8, ASCII,
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

    public static String firstOr(@Nullable String charset, @Nullable String fallbackCharset) {
        if (StringUtils.hasText(charset)) {
            return charset;
        }
        return requireNonNull(fallbackCharset);
    }

    // ========================================================================
    // Key reading from ValueMatcher
    // ========================================================================

    @Nullable
    public static List<?> readKey(KeyType keyType, ValueMatcher valueMatcher) {
        checkExclusive(valueMatcher);
        return switch (keyType) {
            case i8 -> readI8Key(valueMatcher);
            case u8 -> readU8Key(valueMatcher);
            case i16 -> readI16Key(valueMatcher);
            case u16 -> readU16Key(valueMatcher);
            case i32 -> readI32Key(valueMatcher);
            case u32 -> readU32Key(valueMatcher);
            case i64 -> readI64(valueMatcher);
            case str_gbk, str_gb_2312, str_utf8, str -> readStringKey(valueMatcher);
        };
    }

    public static String formateKey(Object key) {
        if (key instanceof Number number) {
            return number + " (0x" + FormatUtils.toHexString(number.longValue(), 4) + ")";
        }
        return key.toString();
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

    @Nullable
    private static List<String> readStringKey(ValueMatcher valueMatcher) {
        final List<String> results = new ArrayList<>();
        for (String v : valueMatcher.matchString()) {
            if (results.contains(v)) {
                log.warn("String-Matcher Duplicated. valeu={}, @ValueMatcher={}", v, valueMatcher);
                continue;
            }
            results.add(v);
        }
        return results.isEmpty() ? null : results;
    }

    @Nullable
    private static List<Byte> readI8Key(ValueMatcher valueMatcher) {
        final List<Byte> results = new ArrayList<>();
        for (byte v : valueMatcher.matchI8()) {
            if (results.contains(v)) {
                log.warn("I8-Matcher Duplicated. valeu={}, @ValueMatcher={}", v, valueMatcher);
                continue;
            }
            results.add(v);
        }
        return results.isEmpty() ? null : results;
    }

    @Nullable
    private static List<Short> readU8Key(ValueMatcher valueMatcher) {
        final List<Short> results = new ArrayList<>();
        for (short v : valueMatcher.matchU8()) {
            if (results.contains(v)) {
                log.warn("U8-Matcher Duplicated. valeu={}, @ValueMatcher={}", v, valueMatcher);
                continue;
            }
            results.add(v);
        }
        return results.isEmpty() ? null : results;
    }

    @Nullable
    private static List<Short> readI16Key(ValueMatcher valueMatcher) {
        final List<Short> results = new ArrayList<>();
        for (short v : valueMatcher.matchI16()) {
            if (results.contains(v)) {
                log.warn("I16-Matcher Duplicated. valeu={}, @ValueMatcher={}", v, valueMatcher);
                continue;
            }
            results.add(v);
        }
        return results.isEmpty() ? null : results;
    }

    @Nullable
    private static List<Integer> readU16Key(ValueMatcher valueMatcher) {
        final List<Integer> results = new ArrayList<>();
        for (int v : valueMatcher.matchU16()) {
            if (results.contains(v)) {
                log.warn("U16-Matcher Duplicated. valeu={}, @ValueMatcher={}", v, valueMatcher);
                continue;
            }
            results.add(v);
        }
        return results.isEmpty() ? null : results;
    }

    @Nullable
    private static List<Integer> readI32Key(ValueMatcher valueMatcher) {
        final List<Integer> results = new ArrayList<>();
        for (int v : valueMatcher.matchI32()) {
            if (results.contains(v)) {
                log.warn("I32-Matcher Duplicated. valeu={}, @ValueMatcher={}", v, valueMatcher);
                continue;
            }
            results.add(v);
        }
        return results.isEmpty() ? null : results;
    }

    @Nullable
    private static List<Long> readU32Key(ValueMatcher valueMatcher) {
        final List<Long> results = new ArrayList<>();
        for (long v : valueMatcher.matchU32()) {
            if (results.contains(v)) {
                log.warn("U32-Matcher Duplicated. valeu={}, @ValueMatcher={}", formateKey(v), valueMatcher);
                continue;
            }
            results.add(v);
        }
        return results.isEmpty() ? null : results;
    }

    @Nullable
    private static List<Long> readI64(ValueMatcher valueMatcher) {
        final List<Long> results = new ArrayList<>();
        for (long v : valueMatcher.matchI64()) {
            if (results.contains(v)) {
                log.warn("I64-Matcher Duplicated. valeu={}, @ValueMatcher={}", v, valueMatcher);
                continue;
            }
            results.add(v);
        }
        return results.isEmpty() ? null : results;
    }
}
