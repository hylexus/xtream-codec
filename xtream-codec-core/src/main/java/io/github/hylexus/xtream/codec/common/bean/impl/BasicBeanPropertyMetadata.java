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

package io.github.hylexus.xtream.codec.common.bean.impl;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.bean.FieldConditionEvaluator;
import io.github.hylexus.xtream.codec.common.bean.FieldLengthExtractor;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamTypes;
import io.github.hylexus.xtream.codec.common.utils.XtreamUtils;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.tracker.PrependLengthFieldSpan;
import io.netty.buffer.ByteBuf;
import lombok.Setter;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;

public class BasicBeanPropertyMetadata implements BeanPropertyMetadata {
    protected final BeanMetadataRegistry beanMetadataRegistry;
    private final String name;
    private final Class<?> type;
    private final FiledDataType filedValueType;
    private final Field field;
    // private final PropertyDescriptor propertyDescriptor;
    private final PropertyGetter propertyGetter;
    private final PropertySetter propertySetter;
    private final int order;
    private final FieldLengthExtractor fieldLengthExtractor;
    private final FieldConditionEvaluator fieldConditionEvaluator;
    protected final XtreamField xtreamField;
    protected final int prependLengthFieldByteCounts;
    protected final PrependLengthFieldType prependLengthFieldType;
    @Setter
    private FieldCodec<?> fieldCodec;

    public BasicBeanPropertyMetadata(BeanMetadataRegistry registry, String name, Class<?> type, Field field, PropertyGetter getter, PropertySetter setter) {
        this.beanMetadataRegistry = registry;
        this.name = name;
        this.type = type;
        this.field = field;
        this.propertyGetter = getter;
        this.propertySetter = setter;
        this.order = initOrder();
        this.filedValueType = XtreamTypes.detectFieldDataType(field);
        this.xtreamField = findAnnotation(XtreamField.class).orElseThrow();
        this.fieldLengthExtractor = detectFieldLengthExtractor(this.xtreamField);
        this.fieldConditionEvaluator = detectFieldConditionalEvaluator(this.xtreamField);
        this.prependLengthFieldByteCounts = this.detectPrependLengthFieldByteCounts(xtreamField);
        this.prependLengthFieldType = PrependLengthFieldType.from(this.prependLengthFieldByteCounts);
    }

    protected int detectPrependLengthFieldByteCounts(XtreamField xtreamField) {
        if (xtreamField.prependLengthFieldType() != PrependLengthFieldType.none) {
            return xtreamField.prependLengthFieldType().getByteCounts();
        }
        return xtreamField.prependLengthFieldLength();
    }

    private FieldConditionEvaluator detectFieldConditionalEvaluator(XtreamField xtreamField) {
        if (XtreamUtils.hasElement(xtreamField.condition())) {
            return new FieldConditionEvaluator.ExpressionFieldConditionEvaluator(xtreamField.condition());
        }
        return FieldConditionEvaluator.AlwaysTrueFieldConditionEvaluator.INSTANCE;
    }

    /**
     * @see FieldCodec#deserialize(BeanPropertyMetadata, FieldCodec.DeserializeContext, ByteBuf, int) StringFieldCodec#deserialize
     * @see SequenceBeanPropertyMetadata#decodePropertyValue(FieldCodec.DeserializeContext, ByteBuf) SequenceBeanPropertyMetadata#decodePropertyValue
     * @see NestedBeanPropertyMetadata#decodePropertyValue(FieldCodec.DeserializeContext, ByteBuf) NestedBeanPropertyMetadata#decodePropertyValue
     * @see MapBeanPropertyMetadata#decodePropertyValue(FieldCodec.DeserializeContext, ByteBuf) MapBeanPropertyMetadata#decodePropertyValue
     */
    protected FieldLengthExtractor detectFieldLengthExtractor(XtreamField xtreamField) {
        if (xtreamField.prependLengthFieldType() != PrependLengthFieldType.none) {
            return new FieldLengthExtractor.PrependFieldLengthExtractor(xtreamField.prependLengthFieldType());
        }

        if (xtreamField.prependLengthFieldLength() > 0) {
            return new FieldLengthExtractor.PrependFieldLengthExtractor(xtreamField.prependLengthFieldLength());
        }

        if (xtreamField.length() > 0) {
            return new FieldLengthExtractor.ConstantFieldLengthExtractor(xtreamField.length());
        }

        if (XtreamUtils.hasElement(xtreamField.lengthExpression())) {
            return new FieldLengthExtractor.ExpressionFieldLengthExtractor(xtreamField);
        }

        return XtreamTypes.getDefaultSizeInBytes(this.rawClass())
                .map(FieldLengthExtractor.ConstantFieldLengthExtractor::new)
                .map(FieldLengthExtractor.class::cast)
                .orElseGet(() -> {
                    final FiledDataType filedDataType = XtreamTypes.detectFieldDataType(this.rawClass());
                    return switch (filedDataType) {
                        case sequence, struct, map -> new FieldLengthExtractor.ConstantFieldLengthExtractor(-2);
                        case basic -> {
                            // String 类型: 没指定长度 ==> 读取剩余所有字节
                            if (this.rawClass() == String.class) {
                                yield new FieldLengthExtractor.ConstantFieldLengthExtractor(-2);
                            }
                            yield this.placeholderFieldLengthExtractor();
                        }
                        default -> this.placeholderFieldLengthExtractor();
                    };
                });
    }

    private FieldLengthExtractor.PlaceholderFieldLengthExtractor placeholderFieldLengthExtractor() {
        return new FieldLengthExtractor.PlaceholderFieldLengthExtractor("Did you forget to specify length() / lengthExpression() for Field: [ " + this.field + " ]");
    }

    protected int initOrder() {
        return this.findAnnotation(XtreamField.class).map(XtreamField::order).orElse(0);
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Class<?> rawClass() {
        return this.type;
    }

    @Override
    public FiledDataType dataType() {
        return this.filedValueType;
    }

    @Override
    public Field field() {
        return this.field;
    }

    @Override
    public XtreamField xtreamFieldAnnotation() {
        return this.xtreamField;
    }

    @Override
    public PropertyGetter propertyGetter() {
        return this.propertyGetter;
    }

    @Override
    public PropertySetter propertySetter() {
        return this.propertySetter;
    }

    @Override
    public FieldCodec<?> fieldCodec() {
        return this.fieldCodec;
    }

    @Override
    public FieldLengthExtractor fieldLengthExtractor() {
        return this.fieldLengthExtractor;
    }

    @Override
    public FieldConditionEvaluator conditionEvaluator() {
        return this.fieldConditionEvaluator;
    }

    @Override
    public int order() {
        return this.order;
    }

    @Override
    public <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationClass) {
        // todo cache...
        final A mergedAnnotation = AnnotatedElementUtils.getMergedAnnotation(this.field(), annotationClass);
        return Optional.ofNullable(mergedAnnotation);
    }

    public Object decodePropertyValue(FieldCodec.DeserializeContext context, ByteBuf input) {
        int rb = input.readableBytes();
        if (rb == 0) {
            return null;
        }
        final int length = this.fieldLengthExtractor.extractFieldLength(context, context.evaluationContext(), input);
        if (rb >= length) {
            return fieldCodec().deserialize(this, context, input, length);
        } else {
            return null;
        }
    }

    @Override
    public void setProperty(Object instance, Object value) {
        if (value != null) {
            BeanPropertyMetadata.super.setProperty(instance, value);
        }
    }

    @Override
    public Object decodePropertyValueWithTracker(FieldCodec.DeserializeContext context, ByteBuf input) {
        if (this.fieldLengthExtractor instanceof FieldLengthExtractor.PrependFieldLengthExtractor) {
            final PrependLengthFieldSpan prependLengthFieldSpan = context.codecTracker().addPrependLengthFieldSpan(
                    context.codecTracker().getCurrentSpan(), "prependLengthField", null, null, prependLengthFieldType.name(), "前置长度字段"
            );
            final int indexBeforeRead = input.readerIndex();
            final int length = this.fieldLengthExtractor.extractFieldLength(context, context.evaluationContext(), input);
            final String hexString = FormatUtils.toHexString(input, indexBeforeRead, input.readerIndex() - indexBeforeRead);
            prependLengthFieldSpan.setValue(length).setHexString(hexString);
            return fieldCodec().deserializeWithTracker(this, context, input, length);
        } else {
            final int length = this.fieldLengthExtractor.extractFieldLength(context, context.evaluationContext(), input);
            return fieldCodec().deserializeWithTracker(this, context, input, length);
        }
    }

    @Override
    public void encodePropertyValue(FieldCodec.SerializeContext context, ByteBuf output, Object value) {
        if (this.prependLengthFieldByteCounts <= 0) {
            this.doEncode(context, output, value);
        } else {
            final int lengthFieldWriterIndex = output.writerIndex();
            // 写入长度字段占位符
            prependLengthFieldType.writeTo(output, 0);
            final int beforeEncode = output.writerIndex();

            this.doEncode(context, output, value);

            final int afterEncode = output.writerIndex();
            final int byteCounts = afterEncode - beforeEncode;

            output.writerIndex(lengthFieldWriterIndex);
            // 写入长度字段
            prependLengthFieldType.writeTo(output, byteCounts);
            output.writerIndex(afterEncode);
        }
    }

    @Override
    public void encodePropertyValueWithTracker(FieldCodec.SerializeContext context, ByteBuf output, Object value) {
        if (this.prependLengthFieldByteCounts <= 0) {
            this.doEncodeWithTracker(context, output, value);
        } else {
            final PrependLengthFieldSpan prependLengthFieldSpan = context.codecTracker().addPrependLengthFieldSpan(
                    context.codecTracker().getCurrentSpan(), "prependLengthField", null, null, prependLengthFieldType.name(), "前置长度字段"
            );
            final int lengthFieldWriterIndex = output.writerIndex();
            // 写入长度字段占位符
            prependLengthFieldType.writeTo(output, 0);
            final int beforeEncode = output.writerIndex();

            this.doEncodeWithTracker(context, output, value);

            final int afterEncode = output.writerIndex();
            final int byteCounts = afterEncode - beforeEncode;

            output.writerIndex(lengthFieldWriterIndex);
            // 写入长度字段
            prependLengthFieldType.writeTo(output, byteCounts);
            final String hexString = FormatUtils.toHexString(output, lengthFieldWriterIndex, output.writerIndex() - lengthFieldWriterIndex);
            prependLengthFieldSpan.setValue(byteCounts).setHexString(hexString);
            output.writerIndex(afterEncode);
        }
    }

    @Override
    public BeanMetadataRegistry beanMetadataRegistry() {
        return this.beanMetadataRegistry;
    }

    protected void doEncode(FieldCodec.SerializeContext serializeContext, ByteBuf output, Object value) {
        @SuppressWarnings("unchecked") final FieldCodec<Object> codec = (FieldCodec<Object>) fieldCodec();
        codec.serialize(this, serializeContext, output, value);
    }

    protected void doEncodeWithTracker(FieldCodec.SerializeContext serializeContext, ByteBuf output, Object value) {
        @SuppressWarnings("unchecked") final FieldCodec<Object> codec = (FieldCodec<Object>) fieldCodec();
        codec.serializeWithTracker(this, serializeContext, output, value);
    }

    @Override
    public String toString() {
        return "SimpleBeanPropertyMetadata{"
               + "name='" + name + '\''
               + ", type=" + type
               + ", filedValueType=" + filedValueType
               + ", field=" + field
               + ", fieldCodec=" + fieldCodec
               + ", propertyGetter=" + propertyGetter
               + ", propertySetter=" + propertySetter
               + ", order=" + order
               + ", fieldLengthExtractor=" + fieldLengthExtractor
               + ", xtreamField=" + xtreamField
               + '}';
    }

}
