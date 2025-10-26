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

package io.github.hylexus.xtream.codec.common.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.lang.reflect.Constructor;
import java.util.List;

@Getter
@Setter
@SuppressWarnings("NullAway")
public class BeanDescriptor {
    private final String rawType;
    private final String constructor;
    private final List<BeanPropertyDescriptor> propertyMetadataList;

    public BeanDescriptor(Class<?> rawType, Constructor<?> constructor, List<BeanPropertyDescriptor> propertyMetadataList) {
        this.rawType = rawType.getName();
        this.constructor = constructor.toGenericString();
        this.propertyMetadataList = propertyMetadataList;
    }

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class BeanPropertyDescriptor {
        private String field;
        private int version;
        private int order;
        private String type;
        private String name;
        private String fieldCodec;
        private int fieldLength;
        private String fieldLengthExtractor;
        private String fieldConditionEvaluator;
        private boolean isRecordComponent;
        private boolean isRecordClass;
        private BeanPropertyMetadata.FiledDataType dataType;
        private String propertyGetter;
        private String propertySetter;
    }

    public static BeanDescriptor of(BeanMetadata metadata) {
        final List<BeanPropertyDescriptor> propertyDescriptorList = metadata.getPropertyMetadataList().stream().map(it -> {
            final String lengthExtractorName;
            final int fieldLength;
            final FieldLengthExtractor lengthExtractor = it.fieldLengthExtractor();
            if (lengthExtractor instanceof FieldLengthExtractor.ConstantFieldLengthExtractor constantFieldLengthExtractor) {
                @SuppressWarnings("all") final int length = constantFieldLengthExtractor.extractFieldLength(null, null, null);
                fieldLength = length;
                lengthExtractorName = FieldLengthExtractor.ConstantFieldLengthExtractor.class.getSimpleName() + "(" + length + ")";
            } else {
                lengthExtractorName = lengthExtractor.getClass().getSimpleName();
                fieldLength = -2;
            }

            return new BeanPropertyDescriptor()
                    .setField(it.field().toGenericString())
                    .setOrder(it.order())
                    .setType(it.rawClass().getName())
                    .setName(it.name())
                    .setFieldCodec(it.fieldCodec().getClass().getSimpleName())
                    .setFieldLength(fieldLength)
                    .setFieldLengthExtractor(lengthExtractorName)
                    .setFieldConditionEvaluator(it.conditionEvaluator().getClass().getSimpleName())
                    .setRecordComponent(it.isRecordComponent())
                    .setRecordClass(it.isRecordClass())
                    .setVersion(it.version())
                    .setDataType(it.dataType())
                    .setPropertyGetter(it.propertyGetter().getClass().getSimpleName())
                    .setPropertySetter(it.propertySetter().getClass().getSimpleName())
                    ;
        }).toList();
        return new BeanDescriptor(metadata.getRawType(), metadata.getConstructor(), propertyDescriptorList);
    }
}
