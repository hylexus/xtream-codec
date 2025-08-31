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

import io.github.hylexus.xtream.codec.common.bean.BeanMetadata;
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.bean.FieldLengthExtractor;
import io.github.hylexus.xtream.codec.common.bean.impl.BasicBeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.bean.impl.MapBeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.bean.impl.NestedBeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.bean.impl.SequenceBeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.FieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.XtreamCacheableClassPredicate;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.codec.RuntimeTypeFieldCodec;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import io.github.hylexus.xtream.codec.core.utils.ReflectionUtils;
import org.springframework.core.annotation.MergedAnnotations;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.function.Function;

/**
 * @author hylexus
 */
public class SimpleBeanMetadataRegistry implements BeanMetadataRegistry {
    // <class, <version, metadata>>
    protected final Map<Class<?>, Map<Integer, BeanMetadata>> multiVersionCache = new HashMap<>();
    protected final FieldCodecRegistry fieldCodecRegistry;
    protected final XtreamCacheableClassPredicate cacheableClassPredicate;

    public SimpleBeanMetadataRegistry(FieldCodecRegistry fieldCodecRegistry, XtreamCacheableClassPredicate cacheableClassPredicate) {
        this.fieldCodecRegistry = fieldCodecRegistry;
        this.cacheableClassPredicate = cacheableClassPredicate;
    }

    @Override
    public FieldCodecRegistry getFieldCodecRegistry() {
        return fieldCodecRegistry;
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
            final Map<Integer, BeanMetadata> versionMappings = multiVersionCache.computeIfAbsent(beanClass, k -> new HashMap<>());
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
        final BasicBeanPropertyMetadata metadata = new BasicBeanPropertyMetadata(
                this,
                basicPropertyDescriptor.getName(),
                basicPropertyDescriptor.getPropertyType(),
                pi.version(),
                pi.xtreamField(), basicPropertyDescriptor.getField(),
                // pd,
                BeanUtils.createGetter(basicPropertyDescriptor),
                BeanUtils.createSetter(basicPropertyDescriptor)
        );
        metadata.setFieldCodec(detectFieldCodec(metadata));
        return metadata;
    }

    public BeanMetadata doGetMetadata(Class<?> beanClass, int version, Function<PropertyInfo, BeanPropertyMetadata> creator) {
        // final BeanInfo beanInfo = BeanUtils.getBeanInfo(beanClass, this.cacheableClassPredicate, field -> AnnotatedElementUtils.findMergedAnnotation(field, XtreamField.class) != null);
        final BeanInfo beanInfo = BeanUtils.getBeanInfo(beanClass, this.cacheableClassPredicate, field -> MergedAnnotations.from(field).isPresent(XtreamField.class));
        final BeanDescriptor beanDescriptor = beanInfo.getBeanDescriptor();
        final ArrayList<BeanPropertyMetadata> pdList = new ArrayList<>();
        for (final PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            final BeanUtils.BasicPropertyDescriptor basicPropertyDescriptor = (BeanUtils.BasicPropertyDescriptor) pd;
            final Field field = basicPropertyDescriptor.getField();
            final List<XtreamField> xtreamFieldAnnotations = ReflectionUtils.findXtreamFieldAnnotations(field);
            for (final XtreamField xtreamFieldAnnotation : xtreamFieldAnnotations) {
                final int[] allVersions = xtreamFieldAnnotation.version();
                final boolean versionMatched = isVersionMatched(version, allVersions);
                if (!versionMatched) {
                    continue;
                }
                final BeanPropertyMetadata basicPropertyMetadata = creator.apply(new PropertyInfo(pd, xtreamFieldAnnotation, version));
                if (basicPropertyMetadata.fieldCodec() != null) {
                    // 用户自定义 FieldCodec
                    pdList.add(basicPropertyMetadata);
                    continue;
                }
                if (basicPropertyMetadata.dataType() == BeanPropertyMetadata.FiledDataType.basic) {
                    pdList.add(basicPropertyMetadata);
                } else if (basicPropertyMetadata.dataType() == BeanPropertyMetadata.FiledDataType.struct) {
                    final BeanMetadata nestedMetadata = doGetMetadata(pd.getPropertyType(), version, creator);
                    final NestedBeanPropertyMetadata metadata = new NestedBeanPropertyMetadata(this, nestedMetadata, basicPropertyMetadata, null);
                    pdList.add(metadata);
                } else if (basicPropertyMetadata.dataType() == BeanPropertyMetadata.FiledDataType.sequence) {
                    final List<Class<?>> genericClass = getGenericClass(basicPropertyMetadata.field());
                    final BeanMetadata valueMetadata = doGetMetadata(genericClass.getFirst(), version, creator);
                    final NestedBeanPropertyMetadata metadata = new NestedBeanPropertyMetadata(this, valueMetadata, basicPropertyMetadata, new FieldLengthExtractor.ConstantFieldLengthExtractor(-2));
                    final SequenceBeanPropertyMetadata seqMetadata = new SequenceBeanPropertyMetadata(this, basicPropertyMetadata, metadata);
                    pdList.add(seqMetadata);
                } else if (basicPropertyMetadata.dataType() == BeanPropertyMetadata.FiledDataType.map) {
                    final MapBeanPropertyMetadata mapMedata = new MapBeanPropertyMetadata(basicPropertyMetadata, fieldCodecRegistry, this);
                    pdList.add(mapMedata);
                } else if (basicPropertyMetadata.dataType() == BeanPropertyMetadata.FiledDataType.dynamic) {
                    pdList.add(basicPropertyMetadata);
                } else {
                    throw new IllegalStateException("Cannot determine dataType for " + basicPropertyMetadata.field());
                }
            }
        }
        pdList.sort(Comparator.comparing(BeanPropertyMetadata::order));
        return new BeanMetadata(beanInfo.getBeanDescriptor().getBeanClass(), BeanUtils.getConstructor(beanDescriptor), pdList);
    }

    protected boolean isVersionMatched(int targetVersion, int[] versionCandidates) {
        boolean foundDefault = false;

        for (int v : versionCandidates) {
            if (v == targetVersion) {
                // 一旦发现目标版本，立即成功
                return true;
            } else if (v == XtreamField.DEFAULT_VERSION) {
                // 仅记录默认版本存在
                foundDefault = true;
            }
        }

        // 无目标版本时，依赖默认版本兜底
        return foundDefault;
    }

    private List<Class<?>> getGenericClass(Field field) {
        final Type genericType = field.getGenericType();
        final List<Class<?>> list = new ArrayList<>();
        if (genericType instanceof ParameterizedType) {
            for (Type actualTypeArgument : ((ParameterizedType) genericType).getActualTypeArguments()) {
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

    public FieldCodec<?> detectFieldCodec(BasicBeanPropertyMetadata metadata) {
        return this.fieldCodecRegistry.getFieldCodec(metadata).orElseGet(() -> {
            // ...
            return switch (metadata.dataType()) {
                case struct, sequence, map -> null;
                case dynamic -> RuntimeTypeFieldCodec.INSTANCE;
                default -> throw new IllegalStateException("Cannot determine FieldCodec for Field [" + metadata.field() + "]");
            };
        });
    }
}
