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

import io.github.hylexus.xtream.codec.base.expression.SpelXtreamExpressionEngine;
import io.github.hylexus.xtream.codec.common.bean.*;
import io.github.hylexus.xtream.codec.common.bean.impl.*;
import io.github.hylexus.xtream.codec.core.*;
import io.github.hylexus.xtream.codec.core.annotation.DerivedField;
import io.github.hylexus.xtream.codec.core.annotation.EncodedLength;
import io.github.hylexus.xtream.codec.core.annotation.NumberEndian;
import io.github.hylexus.xtream.codec.core.annotation.NumberSignedness;
import io.github.hylexus.xtream.codec.core.annotation.XtreamEntity;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.codec.NumberFieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import io.github.hylexus.xtream.codec.core.utils.ReflectionUtils;
import io.github.hylexus.xtream.codec.core.utils.XtreamFieldUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.MergedAnnotations;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author hylexus
 * @author opencode (AI)
 * @author Codex (AI)
 */
public class SimpleBeanMetadataRegistry implements BeanMetadataRegistry {
    private static final Logger log = LoggerFactory.getLogger(SimpleBeanMetadataRegistry.class);
    // <class, <version, metadata>>
    protected final ConcurrentMap<Class<?>, ConcurrentMap<Integer, BeanMetadata>> multiVersionCache = new ConcurrentHashMap<>();
    protected final FieldCodecRegistry fieldCodecRegistry;
    protected final XtreamCacheableClassPredicate cacheableClassPredicate;
    protected final XtreamExpressionFactory xtreamExpressionFactory;

    public SimpleBeanMetadataRegistry(FieldCodecRegistry fieldCodecRegistry, XtreamCacheableClassPredicate cacheableClassPredicate) {
        this(fieldCodecRegistry, cacheableClassPredicate, new DefaultXtreamExpressionFactory(new SpelXtreamExpressionEngine()));
    }

    public SimpleBeanMetadataRegistry(FieldCodecRegistry fieldCodecRegistry, XtreamCacheableClassPredicate cacheableClassPredicate, XtreamExpressionFactory xtreamExpressionFactory) {
        this.fieldCodecRegistry = fieldCodecRegistry;
        this.cacheableClassPredicate = cacheableClassPredicate;
        this.xtreamExpressionFactory = xtreamExpressionFactory;
    }

    @Override
    public FieldCodecRegistry getFieldCodecRegistry() {
        return fieldCodecRegistry;
    }

    @Override
    public XtreamExpressionFactory expressionFactory() {
        return this.xtreamExpressionFactory;
    }

    @Override
    public Stream<BeanDescriptor> beanDescriptors() {
        return this.multiVersionCache.values()
                .stream()
                .flatMap(it -> {
                    // ...
                    return it.values().stream().map(BeanDescriptor::of);
                })
                .sorted(Comparator.comparing(BeanDescriptor::rawClass));
    }

    @Override
    public BeanMetadata getBeanMetadata(Class<?> beanClass, int version, Function<PropertyInfo, BeanPropertyMetadata> creator) {
        if (!this.cacheableClassPredicate.test(beanClass)) {
            return this.doGetMetadata(beanClass, version, creator);
        }

        Map<Integer, BeanMetadata> metadataVersionMapping = multiVersionCache.get(beanClass);
        if (metadataVersionMapping != null) {
            final BeanMetadata beanMetadata = metadataVersionMapping.get(version);
            if (beanMetadata != null) {
                return beanMetadata;
            }
        }

        synchronized (SimpleBeanMetadataRegistry.class) {
            if ((metadataVersionMapping = multiVersionCache.get(beanClass)) != null) {
                final BeanMetadata beanMetadata = metadataVersionMapping.get(version);
                if (beanMetadata != null) {
                    return beanMetadata;
                }
            }
            final BeanMetadata beanMetadata = this.doGetMetadata(beanClass, version, creator);
            final Map<Integer, BeanMetadata> versionMappings = multiVersionCache.computeIfAbsent(beanClass, k -> new ConcurrentHashMap<>());
            versionMappings.put(version, beanMetadata);
            return beanMetadata;
        }

    }

    @Override
    public BeanMetadata getBeanMetadata(Class<?> beanClass, int version) {
        return this.getBeanMetadata(beanClass, version, this::createBeanPropertyMetadata);
    }

    public BasicBeanPropertyMetadata createBeanPropertyMetadata(PropertyInfo pi) {
        final BeanUtils.BasicPropertyDescriptor basicPropertyDescriptor = (BeanUtils.BasicPropertyDescriptor) pi.propertyDescriptor();

        final BeanPropertyMetadata.PropertyAccessor propertyAccessor = PropertyAccessorFactory.createPropertyAccessor(pi);
        return new BasicBeanPropertyMetadata(
                this,
                basicPropertyDescriptor.getName(),
                basicPropertyDescriptor.getPropertyType(),
                pi.version(),
                pi.xtreamField(),
                basicPropertyDescriptor.getField(),
                // pd,
                propertyAccessor.getter(),
                propertyAccessor.setter()
        );
    }

    @XtreamEntity(propertyAccessStrategy = PropertyAccessStrategy.AUTO)
    private static class Placeholder {
    }

    public BeanMetadata doGetMetadata(Class<?> beanClass, int version, Function<PropertyInfo, BeanPropertyMetadata> creator) {
        // final BeanInfo beanInfo = BeanUtils.getBeanInfo(beanClass, this.cacheableClassPredicate, field -> AnnotatedElementUtils.findMergedAnnotation(field, XtreamField.class) != null);
        final BeanUtils.XtreamSimpleBeanInfo beanInfo = BeanUtils.getBeanInfo(beanClass, this.cacheableClassPredicate, field -> {
            final MergedAnnotations annotations = MergedAnnotations.from(field);
            return annotations.isPresent(XtreamField.class) || annotations.isPresent(DerivedField.class) || annotations.isPresent(EncodedLength.class);
        });
        final XtreamEntity xtreamEntityAnnotation = ReflectionUtils.findMergedAnnotationAndSynthesize(beanClass, XtreamEntity.class, () -> Placeholder.class.getAnnotation(XtreamEntity.class));
        final PropertyAccessStrategy classStrategy = xtreamEntityAnnotation.propertyAccessStrategy();
        final ArrayList<BeanPropertyMetadata> pdList = new ArrayList<>();
        final boolean isRecordClass = beanInfo.isRecordClass();
        final Map<String, String> reverseSourceMap = new HashMap<>();

        for (final BeanUtils.BasicPropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            final Field field = pd.getField();
            final boolean hasEncodedLength = MergedAnnotations.from(field).isPresent(EncodedLength.class);

            // 1) 检查 @XtreamField
            final List<XtreamField> xtreamFieldAnnotations;
            if (isRecordClass) {
                final RecordComponent recordComponent = Objects.requireNonNull(pd.getRecordComponent());
                xtreamFieldAnnotations = XtreamFieldUtils.getOrDefault(recordComponent);
            } else {
                xtreamFieldAnnotations = XtreamFieldUtils.getOrEmpty(field);
            }
            final XtreamField xtreamFieldAnnotation = matchVersion(version, xtreamFieldAnnotations);

            // 2) 检查 @DerivedField（支持 @Repeatable + 版本匹配）
            final List<DerivedField> allDerivedFields = XtreamFieldUtils.resolveDerivedFieldAnnotations(field);
            final DerivedField derivedField = allDerivedFields.isEmpty()
                    ? null
                    : XtreamFieldUtils.matchDerivedFieldVersion(version, allDerivedFields);

            if (xtreamFieldAnnotation == null && derivedField == null && hasEncodedLength) {
                throw new IllegalArgumentException("@EncodedLength field [" + pd.getName() + "] in class " + beanClass.getName() + " must also carry an @XtreamField-compatible wire format annotation");
            }

            if (xtreamFieldAnnotation != null) {
                // @XtreamField 优先：即使同时有 @DerivedField，也按普通字段处理
                if (derivedField != null) {
                    log.warn("Both @DerivedField and @XtreamField present on field [{}] in class [{}]; @XtreamField takes precedence, @DerivedField ignored",
                            pd.getName(), beanClass.getName());
                }
                final BeanPropertyMetadata basicPropertyMetadata = creator.apply(new PropertyInfo(xtreamEntityAnnotation, pd, xtreamFieldAnnotation, version));
                // 瞬态字段或自定义 FieldCodec 无需继续解析内部结构
                if (xtreamFieldAnnotation.codecStrategy() == XtreamField.CodecStrategy.TRANSIENT
                        || basicPropertyMetadata.fieldCodec() != FieldCodec.NullFieldCodec.INSTANCE) {
                    pdList.add(basicPropertyMetadata);
                } else {
                    if (basicPropertyMetadata.dataType() == BeanPropertyMetadata.FiledDataType.basic) {
                        pdList.add(basicPropertyMetadata);
                    } else if (basicPropertyMetadata.dataType() == BeanPropertyMetadata.FiledDataType.struct) {
                        final BeanMetadata nestedMetadata = doGetMetadata(pd.getPropertyType(), version, creator);
                        final NestedBeanPropertyMetadata metadata = new NestedBeanPropertyMetadata(this, nestedMetadata, basicPropertyMetadata, basicPropertyMetadata.fieldLengthExtractor());
                        pdList.add(metadata);
                    } else if (basicPropertyMetadata.dataType() == BeanPropertyMetadata.FiledDataType.sequence) {
                        final List<Class<?>> genericClass = getGenericClass(basicPropertyMetadata.field());
                        final BeanMetadata valueMetadata = doGetMetadata(genericClass.getFirst(), version, creator);
                        final NestedBeanPropertyMetadata metadata = new NestedBeanPropertyMetadata(this, valueMetadata, basicPropertyMetadata, new FieldLengthExtractor.ConstantFieldLengthExtractor(-2));
                        final SequenceBeanPropertyMetadata seqMetadata = new SequenceBeanPropertyMetadata(this, basicPropertyMetadata, metadata);
                        pdList.add(seqMetadata);
                    } else if (basicPropertyMetadata.dataType() == BeanPropertyMetadata.FiledDataType.map) {
                        pdList.add(new MapBeanPropertyMetadata(basicPropertyMetadata, fieldCodecRegistry, this));
                        throw new IllegalStateException("Deprecated MapBeanPropertyMetadata. Use " + XtreamMapField.class.getSimpleName() + " instead");
                    } else if (basicPropertyMetadata.dataType() == BeanPropertyMetadata.FiledDataType.dynamic) {
                        pdList.add(basicPropertyMetadata);
                    } else {
                        throw new IllegalStateException("Cannot determine dataType for " + basicPropertyMetadata.field());
                    }
                }
            } else if (derivedField != null) {
                // 仅有 @DerivedField，无 @XtreamField → 衍生字段
                final String fieldName = pd.getName();
                if (derivedField.reverseSource()) {
                    final String existing = reverseSourceMap.put(derivedField.source(), fieldName);
                    if (existing != null) {
                        throw new IllegalArgumentException(
                                "Multiple derived fields with reverseSource=true for the same source [" + derivedField.source()
                                + "]: [" + existing + "] and [" + fieldName + "] in class " + beanClass.getName()
                        );
                    }
                }
                final BeanPropertyMetadata.PropertyAccessor accessor = PropertyAccessorFactory.createPropertyAccessor(classStrategy, pd);
                final DerivedBeanPropertyMetadata derivedMetadata = new DerivedBeanPropertyMetadata(
                        this,
                        fieldName,
                        field.getType(),
                        version,
                        derivedField,
                        field,
                        accessor.getter(),
                        accessor.setter()
                );
                pdList.add(derivedMetadata);
            } else if (isRecordClass && !allDerivedFields.isEmpty()) {
                // 未匹配版本的派生组件仍需占据 Record 构造参数的位置。
                final XtreamField transientField = XtreamFieldUtils.createTransientFieldProxy(field);
                pdList.add(creator.apply(new PropertyInfo(xtreamEntityAnnotation, pd, transientField, version)));
            }
        }

        pdList.sort(Comparator.comparing(BeanPropertyMetadata::order));

        // 3) 检测 @EncodedLength、校验并构建计划
        final EncodedLengthPlan encodedLengthPlan = this.processEncodedLengthFields(pdList, beanClass);

        // 4) 构建 derivedBySource 和 reverseDerivedBySource 索引表
        final Map<String, List<BeanPropertyMetadata>> derivedBySource = new LinkedHashMap<>();
        final Map<String, BeanPropertyMetadata> reverseDerivedBySource = new LinkedHashMap<>();
        for (final BeanPropertyMetadata pm : pdList) {
            if (!pm.isDerived()) {
                continue;
            }
            final String source = pm.derivedSource();
            if (source == null) {
                continue;
            }
            derivedBySource.computeIfAbsent(source, k -> new ArrayList<>()).add(pm);
            if (pm.reverseSource()) {
                reverseDerivedBySource.put(source, pm);
            }
        }

        return new BeanMetadata(
                beanInfo.getBeanDescriptor().getBeanClass(),
                BeanUtils.getConstructor(beanInfo),
                pdList, derivedBySource, reverseDerivedBySource, encodedLengthPlan);
    }

    protected @Nullable XtreamField matchVersion(int targetVersion, List<XtreamField> xtreamFieldAnnotations) {
        return XtreamFieldUtils.matchVersion(targetVersion, xtreamFieldAnnotations);
    }

    private List<Class<?>> getGenericClass(Field field) {
        final Type genericType = field.getGenericType();
        final List<Class<?>> list = new ArrayList<>();
        if (genericType instanceof ParameterizedType parameterizedType) {
            for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
                // ignore WildcardType
                // ?, ? extends Number, or ? super Integer
                if (actualTypeArgument instanceof WildcardType) {
                    continue;
                }
                list.add((Class<?>) actualTypeArgument);
            }
        }
        return list;
    }

    // region @EncodedLength 处理

    /**
     * 扫描 {@code pdList} 中的 {@link EncodedLength @EncodedLength} 字段，
     * 校验引用字段存在性、编码顺序、字段格式，并构建编码长度计划。
     */
    private @Nullable EncodedLengthPlan processEncodedLengthFields(List<BeanPropertyMetadata> pdList, Class<?> beanClass) {
        // 字段名 → 排序后位置索引 的查找表
        final Map<String, Integer> positionIndex = new LinkedHashMap<>();
        for (int i = 0; i < pdList.size(); i++) {
            positionIndex.put(pdList.get(i).name(), i);
        }

        EncodedLengthPlan plan = null;
        for (int i = 0; i < pdList.size(); i++) {
            final BeanPropertyMetadata pm = pdList.get(i);
            final EncodedLength ann = pm.findAnnotation(EncodedLength.class).orElse(null);
            if (ann == null) {
                continue;
            }
            if (pm.isDerived()) {
                throw new IllegalArgumentException("@EncodedLength is not supported on @DerivedField field [" + pm.name() + "] in class " + beanClass.getName());
            }
            if (plan != null) {
                throw new IllegalArgumentException("Multiple @EncodedLength fields are not supported in class " + beanClass.getName());
            }

            final String from = ann.from();
            final String until = ann.until();
            final int fromIdx = from.isEmpty() ? -1 : positionIndex.getOrDefault(from, -1);
            final int untilIdx = until.isEmpty() ? -1 : positionIndex.getOrDefault(until, -1);
            final int byteCount = validateEncodedLengthFieldFormat(pm, beanClass);

            validateEncodedLengthRefs(from, until, pm.name(), i, positionIndex, beanClass.getName());

            final EncodedLengthBeanPropertyMetadata encodedLengthBeanPropertyMetadata = new EncodedLengthBeanPropertyMetadata(
                    this, pm.name(), pm.rawClass(), pm.version(),
                    pm.xtreamFieldAnnotation(), pm.field(),
                    pm.propertyGetter(), pm.propertySetter()
            );
            pdList.set(i, encodedLengthBeanPropertyMetadata);
            plan = new EncodedLengthPlan(i, fromIdx, untilIdx, EncodedLengthPlan.writer(byteCount));
        }
        return plan;
    }

    /**
     * 校验 {@code @EncodedLength(from, until)} 的引用字段存在且编码顺序正确。
     */
    private static void validateEncodedLengthRefs(String from, String until, String fieldName, int fieldPos, Map<String, Integer> positionIndex, String className) {
        if (!from.isEmpty()) {
            checkFieldExists(from, positionIndex, className);
            // 确保 from 出现在当前位置之后
            checkPositionAfter(from, fieldPos, positionIndex, "@EncodedLength field [" + fieldName + "] must appear before from field [" + from + "] in encoding order in class " + className);
        }
        if (!until.isEmpty()) {
            checkFieldExists(until, positionIndex, className);
            // 确保 until 出现在当前位置之后
            checkPositionAfter(until, fieldPos, positionIndex, "@EncodedLength field [" + fieldName + "] must appear before until field [" + until + "] in encoding order in class " + className);
        }
        if (!from.isEmpty() && !until.isEmpty()) {
            // until 不能和 from 相同
            if (from.equals(until)) {
                throw new IllegalArgumentException("@EncodedLength(from=\"" + from + "\", until=\"" + until + "\") must have different values in class " + className);
            }
            // 确保 until 出现在 from 之后
            checkPositionAfter(until, positionIndex.getOrDefault(from, -1), positionIndex, "@EncodedLength(from=\"" + from + "\", until=\"" + until + "\") requires from field to appear before until field in encoding order in class " + className);
        }
    }

    private static int validateEncodedLengthFieldFormat(BeanPropertyMetadata pm, Class<?> beanClass) {
        final int byteCount = pm.xtreamFieldAnnotation().length();
        if (byteCount != 1 && byteCount != 2 && byteCount != 4) {
            throw new IllegalArgumentException("@EncodedLength field [" + pm.name() + "] in class " + beanClass.getName() + " must use u8/u16/u32 wire format");
        }
        if (pm.xtreamFieldAnnotation().signedness() != NumberSignedness.UNSIGNED) {
            throw new IllegalArgumentException("@EncodedLength field [" + pm.name() + "] in class " + beanClass.getName() + " must use unsigned integer wire format");
        }
        if (pm.fieldCodec() instanceof NumberFieldCodec numberFieldCodec) {
            final NumberEndian endian = numberFieldCodec.endian();
            if (byteCount > 1 && endian != NumberEndian.BIG_ENDIAN) {
                throw new IllegalArgumentException("@EncodedLength field [" + pm.name() + "] in class " + beanClass.getName() + " must use big-endian u16/u32 wire format");
            }
        }
        return byteCount;
    }

    private static void checkFieldExists(String fieldName, Map<String, Integer> positionIndex, String className) {
        if (!positionIndex.containsKey(fieldName)) {
            throw new IllegalArgumentException("@EncodedLength references non-existent field [" + fieldName + "] in class " + className);
        }
    }

    private static void checkPositionAfter(String fieldName, int minPos, Map<String, Integer> positionIndex, String errorMsg) {
        if (positionIndex.getOrDefault(fieldName, -1) <= minPos) {
            throw new IllegalArgumentException(errorMsg);
        }
    }

    // endregion @EncodedLength 处理

}
