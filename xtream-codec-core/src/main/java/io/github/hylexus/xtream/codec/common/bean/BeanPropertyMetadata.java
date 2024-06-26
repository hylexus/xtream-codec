/*
 * Copyright (c) 2024 xtream-codec
 * xtream-codec is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package io.github.hylexus.xtream.codec.common.bean;

import io.github.hylexus.xtream.codec.core.ContainerInstanceFactory;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.netty.buffer.ByteBuf;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Optional;

public interface BeanPropertyMetadata {

    static <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationClass, AnnotatedElement targetClass) {
        final A mergedAnnotation = AnnotatedElementUtils.getMergedAnnotation(targetClass, annotationClass);
        return Optional.ofNullable(mergedAnnotation);
    }

    <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationClass);

    String name();

    Class<?> rawClass();

    FiledDataType dataType();

    Field field();

    default ContainerInstanceFactory containerInstanceFactory() {
        return ContainerInstanceFactory.PLACEHOLDER;
    }

    PropertyGetter propertyGetter();

    PropertySetter propertySetter();

    FieldCodec<?> fieldCodec();

    FieldLengthExtractor fieldLengthExtractor();

    FieldConditionEvaluator conditionEvaluator();

    int order();

    Object decodePropertyValue(FieldCodec.DeserializeContext context, ByteBuf input);

    void encodePropertyValue(FieldCodec.SerializeContext context, ByteBuf output, Object value);

    default void setProperty(Object instance, Object value) {
        this.propertySetter().setProperty(this, instance, value);
    }

    default Object getProperty(Object instance) {
        return this.propertyGetter().getProperty(this, instance);
    }

    enum FiledDataType {
        basic,
        nested,
        sequence,
        map,
        unknown
    }

    interface PropertySetter {
        void setProperty(BeanPropertyMetadata metadata, Object instance, Object value);
    }

    interface PropertyGetter {
        Object getProperty(BeanPropertyMetadata metadata, Object instance);
    }

}
