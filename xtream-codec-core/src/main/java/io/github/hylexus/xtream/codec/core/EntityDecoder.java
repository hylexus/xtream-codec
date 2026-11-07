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

package io.github.hylexus.xtream.codec.core;

import io.github.hylexus.xtream.codec.common.bean.BeanMetadata;
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.DefaultDeserializeContext;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.utils.XtreamFieldUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author hylexus
 * @author opencode (AI)
 */
// todo: TypeParameterUnusedInFormals
@SuppressWarnings("TypeParameterUnusedInFormals")
public class EntityDecoder {
    protected final ByteBufAllocator bufferFactory = ByteBufAllocator.DEFAULT;
    protected final BeanMetadataRegistry beanMetadataRegistry;
    private final FieldCodecRegistry fieldCodecRegistry;
    protected final XtreamExpressionFactory expressionFactory;

    public EntityDecoder(BeanMetadataRegistry beanMetadataRegistry) {
        this.beanMetadataRegistry = beanMetadataRegistry;
        this.fieldCodecRegistry = beanMetadataRegistry.getFieldCodecRegistry();
        this.expressionFactory = this.beanMetadataRegistry.expressionFactory();
    }

    public <T> T decode(Class<T> entityClass, ByteBuf source) {
        return this.decode(XtreamField.ALL_VERSION, entityClass, source);
    }

    public <T> T decode(int version, Class<T> entityClass, ByteBuf source) {
        final BeanMetadata beanMetadata = beanMetadataRegistry.getBeanMetadata(entityClass, version);
        final Object containerInstance = beanMetadata.createNewInstanceForDecoding();
        return this.decode(version, source, beanMetadata, containerInstance);
    }

    public <T> T decode(BeanMetadata beanMetadata, ByteBuf source) {
        return this.decode(XtreamField.ALL_VERSION, beanMetadata, source);
    }

    public <T> T decode(int version, BeanMetadata beanMetadata, ByteBuf source) {
        final Object containerInstance = beanMetadata.createNewInstanceForDecoding();
        return decode(version, source, beanMetadata, containerInstance);
    }

    public <T> T decode(ByteBuf source, Object containerInstance) {
        return this.decode(XtreamField.ALL_VERSION, source, containerInstance);
    }

    public <T> T decode(int version, ByteBuf source, Object containerInstance) {
        final BeanMetadata beanMetadata = beanMetadataRegistry.getBeanMetadata(containerInstance.getClass(), version);
        return decode(version, source, beanMetadata, containerInstance);
    }

    public <T> T decode(ByteBuf source, BeanMetadata beanMetadata, Object containerInstance) {
        return decode(XtreamField.ALL_VERSION, source, beanMetadata, containerInstance);
    }

    /**
     * @see BeanMetadata#createNewInstanceForDecoding()
     */
    public <T> T decode(int version, ByteBuf source, BeanMetadata beanMetadata, Object containerInstance) {
        final FieldCodec.DeserializeContext context = new DefaultDeserializeContext(this.bufferFactory, this, containerInstance, version, this.beanMetadataRegistry, null);
        if (beanMetadata.getRawType().isRecord()) {
            return decodeRecord(source, beanMetadata, containerInstance, context, null);
        } else {
            return decodePojo(source, beanMetadata, containerInstance, context, null);
        }
    }

    // region withTracker
    @SuppressWarnings("unused")
    public <T> T decodeWithTracker(Class<T> entityClass, ByteBuf source, CodecTracker tracker) {
        return this.decodeWithTracker(XtreamField.ALL_VERSION, entityClass, source, tracker);
    }

    public <T> T decodeWithTracker(int version, Class<T> entityClass, ByteBuf source, CodecTracker tracker) {
        final BeanMetadata beanMetadata = beanMetadataRegistry.getBeanMetadata(entityClass, version);
        final Object containerInstance = beanMetadata.createNewInstanceForDecoding();
        return this.decodeWithTracker(version, source, beanMetadata, containerInstance, tracker);
    }

    public <T> T decodeWithTracker(BeanMetadata beanMetadata, ByteBuf source, CodecTracker tracker) {
        return this.decodeWithTracker(XtreamField.ALL_VERSION, beanMetadata, source, tracker);
    }

    public <T> T decodeWithTracker(int version, BeanMetadata beanMetadata, ByteBuf source, CodecTracker tracker) {
        final Object containerInstance = beanMetadata.createNewInstanceForDecoding();
        return decodeWithTracker(version, source, beanMetadata, containerInstance, tracker);
    }

    @SuppressWarnings("unused")
    public <T> T decodeWithTracker(ByteBuf source, Object containerInstance, CodecTracker tracker) {
        return this.decodeWithTracker(XtreamField.ALL_VERSION, source, containerInstance, tracker);
    }

    public <T> T decodeWithTracker(int version, ByteBuf source, Object containerInstance, CodecTracker tracker) {
        final BeanMetadata beanMetadata = beanMetadataRegistry.getBeanMetadata(containerInstance.getClass(), version);
        return decodeWithTracker(version, source, beanMetadata, containerInstance, tracker);
    }

    @SuppressWarnings("unused")
    public <T> T decodeWithTracker(ByteBuf source, BeanMetadata beanMetadata, Object containerInstance, CodecTracker tracker) {
        return decodeWithTracker(XtreamField.ALL_VERSION, source, beanMetadata, containerInstance, tracker);
    }

    /**
     * @see BeanMetadata#createNewInstanceForDecoding()
     */
    public <T> T decodeWithTracker(int version, ByteBuf source, BeanMetadata beanMetadata, Object containerInstance, CodecTracker tracker) {
        Objects.requireNonNull(tracker);
        final int indexBeforeRead = source.readerIndex();
        final boolean rootInvocation = tracker.isNotTracing();
        if (rootInvocation) {
            tracker.beginDecode(indexBeforeRead, beanMetadata.getRawType().getName());
        }
        final FieldCodec.DeserializeContext context = new DefaultDeserializeContext(this.bufferFactory, this, containerInstance, version, this.beanMetadataRegistry, tracker);
        try {
            final T result;
            if (beanMetadata.getRawType().isRecord()) {
                result = decodeRecord(source, beanMetadata, containerInstance, context, tracker);
            } else {
                result = decodePojo(source, beanMetadata, containerInstance, context, tracker);
            }
            if (rootInvocation) {
                final String hexString = FormatUtils.toHexString(source, indexBeforeRead, source.readerIndex() - indexBeforeRead);
                tracker.finishTrace(hexString, source.readerIndex());
            }
            return result;
        } catch (RuntimeException | Error e) {
            if (rootInvocation) {
                tracker.recordFailure(e, source.readerIndex());
            }
            throw e;
        }
    }
    // endregion withTracker

    // ========== 解码主逻辑（内联派生字段求值）==========

    @SuppressWarnings("unchecked")
    private <T> T decodeRecord(ByteBuf source, BeanMetadata beanMetadata, Object containerInstance,
                               FieldCodec.DeserializeContext context, @Nullable CodecTracker tracker) {

        @SuppressWarnings("unchecked") final Map<String, @Nullable Object> instanceProperties = (Map<String, Object>) containerInstance;
        final List<BeanPropertyMetadata> propertyMetadataList = beanMetadata.getPropertyMetadataList();
        final @Nullable Object[] filedValues = new Object[propertyMetadataList.size()];
        final boolean useTracker = tracker != null;

        for (int i = 0; i < propertyMetadataList.size(); i++) {
            final BeanPropertyMetadata propertyMetadata = propertyMetadataList.get(i);
            if (propertyMetadata.isDerived()) {
                continue;
            }
            final Object fieldValue;
            if (propertyMetadata.conditionEvaluator().evaluate(context)) {
                fieldValue = useTracker
                        ? propertyMetadata.decodePropertyValueWithTracker(context, source)
                        : propertyMetadata.decodePropertyValue(context, source);
            } else {
                final XtreamField fieldAnnotation = propertyMetadata.xtreamFieldAnnotation();
                if (fieldAnnotation instanceof XtreamFieldUtils.XtreamTransientFieldProxy proxy) {
                    fieldValue = proxy.defaultValueForNulls();
                } else {
                    fieldValue = XtreamFieldUtils.createDefaultValueForNulls(fieldAnnotation.nulls(), propertyMetadata.rawClass());
                }
            }
            filedValues[i] = fieldValue;
            instanceProperties.put(propertyMetadata.name(), fieldValue);
            context.evaluationContext().setVariable(propertyMetadata.name(), fieldValue);

            // 内联求值：源字段解码后立即计算依赖它的派生字段值
            if (beanMetadata.hasDerivedFields()) {
                XtreamFieldUtils.applyDerivedFieldsInline(fieldValue, propertyMetadata.name(), beanMetadata, (derived, derivedVal) -> {
                    final int idx = beanMetadata.propertyIndex(derived.name());
                    filedValues[idx] = derivedVal;
                    instanceProperties.put(derived.name(), derivedVal);
                    context.evaluationContext().setVariable(derived.name(), derivedVal);
                });
            }
        }
        // 派生转换失败、源值为空或返回空值时，基本类型构造参数使用对应的零值。
        for (int i = 0; i < propertyMetadataList.size(); i++) {
            final BeanPropertyMetadata propertyMetadata = propertyMetadataList.get(i);
            if (propertyMetadata.isDerived() && filedValues[i] == null && propertyMetadata.rawClass().isPrimitive()) {
                filedValues[i] = XtreamFieldUtils.createDefaultValueForNulls(XtreamField.Nulls.AS_NULL, propertyMetadata.rawClass());
            }
        }
        return beanMetadata.createNewRecordInstance(filedValues);
    }

    private <T> T decodePojo(ByteBuf source, BeanMetadata beanMetadata, Object containerInstance,
                             FieldCodec.DeserializeContext context, @Nullable CodecTracker tracker) {
        final List<BeanPropertyMetadata> propertyMetadataList = beanMetadata.getPropertyMetadataList();
        final boolean useTracker = tracker != null;

        for (final BeanPropertyMetadata propertyMetadata : propertyMetadataList) {
            if (propertyMetadata.isDerived()) {
                continue;
            }
            if (propertyMetadata.conditionEvaluator().evaluate(context)) {
                final Object fieldValue = useTracker
                        ? propertyMetadata.decodePropertyValueWithTracker(context, source)
                        : propertyMetadata.decodePropertyValue(context, source);
                propertyMetadata.setProperty(containerInstance, fieldValue);
                context.evaluationContext().setVariable(propertyMetadata.name(), fieldValue);

                // 内联求值：源字段解码后立即计算依赖它的派生字段值
                if (beanMetadata.hasDerivedFields()) {
                    XtreamFieldUtils.applyDerivedFieldsInline(fieldValue, propertyMetadata.name(), beanMetadata, (derived, derivedVal) -> {
                        derived.setProperty(containerInstance, derivedVal);
                        context.evaluationContext().setVariable(derived.name(), derivedVal);
                    });
                }
            } else {
                context.evaluationContext().setVariable(propertyMetadata.name(), null);
            }
        }
        @SuppressWarnings("unchecked") final T typed = (T) containerInstance;
        return typed;
    }

    @SuppressWarnings("redundent")
    public BeanMetadataRegistry getBeanMetadataRegistry() {
        return beanMetadataRegistry;
    }

    @SuppressWarnings("redundent")
    public FieldCodecRegistry getFieldCodecRegistry() {
        return fieldCodecRegistry;
    }

    public XtreamExpressionFactory expressionFactory() {
        return this.expressionFactory;
    }

}
