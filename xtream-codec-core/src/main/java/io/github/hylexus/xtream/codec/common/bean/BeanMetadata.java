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

package io.github.hylexus.xtream.codec.common.bean;

import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import io.github.hylexus.xtream.codec.core.utils.XtreamRecordUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.*;

import static java.util.Collections.emptyMap;

/**
 * @author hylexus
 * @author opencode (AI)
 * @author Codex (AI)
 */
public class BeanMetadata {
    private static final Logger log = LoggerFactory.getLogger(BeanMetadata.class);
    private final Class<?> rawType;
    private final Constructor<?> constructor;
    private final List<BeanPropertyMetadata> propertyMetadataList;
    private final ObjectInstantiator instantiator;

    // 源字段名 → 依赖它的派生字段列表（解码内联求值用）
    private final Map<String, List<BeanPropertyMetadata>> derivedBySource;
    // 源字段名 → 唯一的 reverseSource 派生字段（编码内联值替换用，至多一个)
    private final Map<String, BeanPropertyMetadata> reverseDerivedBySource;
    // 属性名 → 在 propertyMetadataList 中的下标（Record 构造器参数数组 定位用）
    private final Map<String, Integer> propertyIndex;
    // Record 声明顺序中的每个构造参数对应的属性列表位置。
    private final int[] recordArgumentIndexes;
    // 缓存是否有派生字段，避免 decode/encode 热路径中反复查询 Map.isEmpty()
    private final boolean hasDerivedFields;
    // 编码长度计划；为空时编码器走普通路径
    private final @Nullable EncodedLengthPlan encodedLengthPlan;

    public BeanMetadata(Class<?> rawType, Constructor<?> constructor, List<BeanPropertyMetadata> propertyMetadataList) {
        this(rawType, constructor, propertyMetadataList, emptyMap(), emptyMap(), null);
    }

    public BeanMetadata(Class<?> rawType, Constructor<?> constructor, List<BeanPropertyMetadata> propertyMetadataList,
                        Map<String, List<BeanPropertyMetadata>> derivedBySource,
                        Map<String, BeanPropertyMetadata> reverseDerivedBySource) {
        this(rawType, constructor, propertyMetadataList, derivedBySource, reverseDerivedBySource, null);
    }

    public BeanMetadata(Class<?> rawType, Constructor<?> constructor, List<BeanPropertyMetadata> propertyMetadataList,
                        Map<String, List<BeanPropertyMetadata>> derivedBySource,
                        Map<String, BeanPropertyMetadata> reverseDerivedBySource,
                        @Nullable EncodedLengthPlan encodedLengthPlan) {
        this.rawType = rawType;
        this.constructor = constructor;
        this.propertyMetadataList = propertyMetadataList;
        this.instantiator = createInstantiator(rawType, constructor);
        this.derivedBySource = derivedBySource;
        this.reverseDerivedBySource = reverseDerivedBySource;
        this.propertyIndex = buildPropertyIndex(propertyMetadataList);
        this.recordArgumentIndexes = rawType.isRecord()
                ? Arrays.stream(rawType.getRecordComponents()).mapToInt(component -> propertyIndex(component.getName())).toArray()
                : new int[0];
        this.hasDerivedFields = !derivedBySource.isEmpty();
        this.encodedLengthPlan = encodedLengthPlan;
    }

    private static Map<String, Integer> buildPropertyIndex(List<BeanPropertyMetadata> list) {
        final Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            idx.put(list.get(i).name(), i);
        }
        return Collections.unmodifiableMap(idx);
    }

    // todo: TypeParameterUnusedInFormals
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public <T> T createNewRecordInstance(@Nullable Object[] filedValues) {
        final @Nullable Object[] constructorValues;
        if (this.rawType.isRecord()) {
            constructorValues = new Object[this.recordArgumentIndexes.length];
            for (int i = 0; i < constructorValues.length; i++) {
                constructorValues[i] = filedValues[this.recordArgumentIndexes[i]];
            }
        } else {
            constructorValues = filedValues;
        }
        final Object newInstance = this.instantiator.newInstanceIgnoreException(constructorValues);
        // final Object newInstance = BeanUtils.createNewInstance(this.getConstructor(), filedValues);
        @SuppressWarnings("unchecked") final T casted = (T) newInstance;
        return casted;
    }

    /**
     * @deprecated Use {@link #createNewInstanceForDecoding()} instead.
     */
    @Deprecated(since = "0.2.0", forRemoval = true)
    @Nullable
    public Object createNewInstance() {
        if (this.rawType.isRecord()) {
            return null;
        }
        return BeanUtils.createNewInstance(this.getConstructor(), (Object[]) null);
    }

    /**
     * 解码时创建实体类实例
     * <h3 color="red">注意</h3>
     * 对于 {@link Record} 类型的实体类:
     * <li>解码过程中无法直接创建实例，临时使用 {@link java.util.Map} 作为容器类，用于临时存储属性值，以便在 {@code SpEL} 中读取属性值</li>
     * <li>各个属性都解码完成后，使用 {@code Canonical 构造器} 创建实例</li>
     *
     * @see XtreamField#condition()
     * @see XtreamField#lengthExpression()
     * @see XtreamField#iterationTimesExpression()
     * @see XtreamRecordUtils#findCanonicalRecordConstructor(Class)
     * @since 0.2.0
     */
    public Object createNewInstanceForDecoding() {
        if (this.rawType.isRecord()) {
            return new LinkedHashMap<String, Object>();
        }
        return this.instantiator.newInstanceIgnoreException((Object[]) null);
    }

    private ObjectInstantiator createInstantiator(Class<?> rawType, Constructor<?> constructor) {
        if (rawType.isRecord()) {
            try {
                final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(rawType, MethodHandles.lookup());
                final MethodHandle mh = lookup.unreflectConstructor(constructor);
                return mh::invokeWithArguments;
            } catch (Throwable e) {
                log.error("Failed to create MethodHandle for constructor, falling back to reflection.", e);
                return args -> {
                    try {
                        return constructor.newInstance(args);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                };
            }
        } else {
            // 对于非 record 类，继续使用传统的反射
            return args -> {
                try {
                    return constructor.newInstance(args);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    @FunctionalInterface
    private interface ObjectInstantiator {

        @SuppressWarnings("checkstyle:NoWhitespaceBefore")
        Object newInstance(@Nullable Object @Nullable ... args) throws Throwable;

        @SuppressWarnings("checkstyle:NoWhitespaceBefore")
        default Object newInstanceIgnoreException(@Nullable Object @Nullable ... args) {
            try {
                return this.newInstance(args);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Class<?> getRawType() {
        return rawType;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public List<BeanPropertyMetadata> getPropertyMetadataList() {
        return propertyMetadataList;
    }

    /**
     * @return 是否有任意 {@code @DerivedField} 字段；无派生字段时编解码可直接跳过内联逻辑
     * @since 0.6.0
     */
    public boolean hasDerivedFields() {
        return this.hasDerivedFields;
    }

    /**
     * @return 是否有任意 {@code @EncodedLength} 字段；无编码长度字段时编码可直接跳过追踪逻辑
     * @since 0.7.0
     */
    public boolean hasEncodedLengthField() {
        return this.encodedLengthPlan != null;
    }

    /**
     * @return 编码长度计划；无 {@code @EncodedLength} 字段时返回 {@code null}
     * @since 0.7.0
     */
    public @Nullable EncodedLengthPlan getEncodedLengthPlan() {
        return this.encodedLengthPlan;
    }

    /**
     * @return 源字段名 → 依赖它的派生字段列表；解码时源字段求值后立即计算派生值
     * @since 0.6.0
     */
    public Map<String, List<BeanPropertyMetadata>> getDerivedBySource() {
        return this.derivedBySource;
    }

    /**
     * @return 源字段名 → 唯一的 reverseSource 派生字段；编码时用派生字段的 Getter + 逆变换替代源字段的值
     * @since 0.6.0
     */
    public Map<String, BeanPropertyMetadata> getReverseDerivedBySource() {
        return this.reverseDerivedBySource;
    }

    /**
     * @param propertyName 属性名
     * @return 该属性在 {@link #getPropertyMetadataList()} 中的下标
     * @since 0.6.0
     */
    public int propertyIndex(String propertyName) {
        final Integer idx = this.propertyIndex.get(propertyName);
        if (idx == null) {
            throw new IllegalArgumentException("Unknown property: " + propertyName);
        }
        return idx;
    }

}
