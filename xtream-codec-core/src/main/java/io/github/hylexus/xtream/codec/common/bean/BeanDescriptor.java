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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.codec.CharSequenceFieldCodec;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

@SuppressWarnings("NullAway")
public record BeanDescriptor(String rawClass, String constructor, List<BeanPropertyDescriptor> properties) {
    public BeanDescriptor(Class<?> rawType, Constructor<?> constructor, List<BeanPropertyDescriptor> propertyMetadataList) {
        this(rawType.getName(), constructor.toGenericString(), propertyMetadataList);
    }

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class BeanPropertyDescriptor {
        private String name;
        private String type;
        private String field;
        private boolean isRecordComponent;
        private boolean isRecordClass;
        private BeanPropertyMetadata.FiledDataType dataType;
        private GetterDescriptor getter;
        private SetterDescriptor setter;
        private VersionDescriptor version;
        private OrderDescriptor order;
        private FieldCodecDescriptor codec;
        private FieldLengthDescriptor length;
        private FieldConditionDescriptor condition;

        public int getVersionValue() {
            return this.version.value();
        }

        public boolean isVersionDefault() {
            return this.version.isDefault();
        }

        public String getDataTypeName() {
            return this.dataType.name();
        }
    }

    public static BeanDescriptor of(BeanMetadata metadata) {
        final List<BeanPropertyDescriptor> propertyDescriptorList = metadata.getPropertyMetadataList().stream().map(it -> {
            // ...
            return new BeanPropertyDescriptor()
                    .setField(it.field().toGenericString())
                    .setOrder(new OrderDescriptor(it.order(), it.order() == XtreamField.DEFAULT_ORDER))
                    .setType(it.rawClass().getName())
                    .setName(it.name())
                    .setCodec(FieldCodecDescriptor.of(it.fieldCodec()))
                    .setLength(FieldLengthDescriptor.of(it))
                    .setCondition(FieldConditionDescriptor.of(it.conditionEvaluator()))
                    .setRecordComponent(it.isRecordComponent())
                    .setRecordClass(it.isRecordClass())
                    .setVersion(new VersionDescriptor(it.version(), it.version() == XtreamField.ALL_VERSION))
                    .setDataType(it.dataType())
                    .setGetter(GetterDescriptor.of(it.propertyGetter()))
                    .setSetter(SetterDescriptor.of(it.propertySetter()))
                    ;
        }).toList();
        return new BeanDescriptor(metadata.getRawType(), metadata.getConstructor(), propertyDescriptorList);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record SetterDescriptor(
            String rawClass,
            String type,
            String implementation,
            @Nullable Object lambdaMetaFactory,
            @Nullable Object varHandle,
            @Nullable Object methodHandle,
            @Nullable Object reflection) {
        static SetterDescriptor of(BeanPropertyMetadata.PropertySetter setter) {
            final String rawClass = setter.getClass().getName();
            return switch (setter) {
                case PropertySetters.LambdaMetaFactoryMethodPropertySetter lambdaMetaFactoryMethodPropertySetter -> {
                    final Map<String, String> meta = Map.of("method", lambdaMetaFactoryMethodPropertySetter.method().toGenericString());
                    yield new SetterDescriptor(rawClass, "Method", "LambdaMetaFactory", meta, null, null, null);
                }
                case PropertySetters.VarHandleFieldPropertySetter varHandleFieldPropertySetter -> {
                    final Map<String, String> meta = Map.of("field", varHandleFieldPropertySetter.field().toGenericString());
                    yield new SetterDescriptor(rawClass, "Field", "VarHandle", null, meta, null, null);
                }
                case PropertySetters.MethodHandleFieldPropertySetter methodHandleFieldPropertySetter -> {
                    final Map<String, String> meta = Map.of("field", methodHandleFieldPropertySetter.field().toGenericString());
                    yield new SetterDescriptor(rawClass, "Field", "MethodHandle", null, null, meta, null);
                }
                case PropertySetters.MethodHandleMethodPropertySetter methodHandlePropertySetter -> {
                    final Map<String, String> meta = Map.of("method", methodHandlePropertySetter.method().toGenericString());
                    yield new SetterDescriptor(rawClass, "Method", "MethodHandle", null, null, meta, null);
                }
                case PropertySetters.ReflectionFieldPropertySetter reflectionFieldPropertySetter -> {
                    final Map<String, String> meta = Map.of("field", reflectionFieldPropertySetter.field().toGenericString());
                    yield new SetterDescriptor(rawClass, "Field", "Reflection", null, null, null, meta);
                }
                case PropertySetters.ReflectionMethodPropertySetter reflectionMethodPropertySetter -> {
                    final Map<String, String> meta = Map.of("method", reflectionMethodPropertySetter.method().toGenericString());
                    yield new SetterDescriptor(rawClass, "Method", "Reflection", null, null, null, meta);
                }
                default -> new SetterDescriptor(rawClass, "Custom", "Custom", null, null, null, null);
            };
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record GetterDescriptor(
            String rawClass,
            String type,
            String implementation,
            @Nullable Object lambdaMetaFactory,
            @Nullable Object varHandle,
            @Nullable Object methodHandle,
            @Nullable Object reflection) {
        static GetterDescriptor of(BeanPropertyMetadata.PropertyGetter getter) {
            final String rawClass = getter.getClass().getName();
            return switch (getter) {
                case PropertyGetters.LambdaMetaFactoryMethodPropertyGetter lambdaMetaFactoryMethodPropertyGetter -> {
                    final Map<String, String> meta = Map.of("method", lambdaMetaFactoryMethodPropertyGetter.method().toGenericString());
                    yield new GetterDescriptor(rawClass, "Method", "LambdaMetaFactory", meta, null, null, null);
                }
                case PropertyGetters.VarHandleFieldPropertyGetter varHandleFieldPropertyGetter -> {
                    final Map<String, String> meta = Map.of("field", varHandleFieldPropertyGetter.field().toGenericString());
                    yield new GetterDescriptor(rawClass, "Field", "VarHandle", null, meta, null, null);
                }
                case PropertyGetters.MethodHandleFieldPropertyGetter methodHandleFieldPropertyGetter -> {
                    final Map<String, String> meta = Map.of("field", methodHandleFieldPropertyGetter.field().toGenericString());
                    yield new GetterDescriptor(rawClass, "Field", "MethodHandle", null, null, meta, null);
                }
                case PropertyGetters.MethodHandleMethodPropertyGetter methodPropertyGetter -> {
                    final Map<String, String> meta = Map.of("method", methodPropertyGetter.method().toGenericString());
                    yield new GetterDescriptor(rawClass, "Method", "MethodHandle", null, null, meta, null);
                }
                case PropertyGetters.ReflectionFieldPropertyGetter fieldPropertyGetter -> {
                    final Map<String, String> meta = Map.of("field", fieldPropertyGetter.field().toGenericString());
                    yield new GetterDescriptor(rawClass, "Field", "Reflection", null, null, null, meta);
                }
                case PropertyGetters.ReflectionMethodPropertyGetter methodPropertyGetter -> {
                    final Map<String, String> meta = Map.of("method", methodPropertyGetter.method().toGenericString());
                    yield new GetterDescriptor(rawClass, "Method", "Reflection", null, null, null, meta);
                }
                default -> new GetterDescriptor(rawClass, "Custom", "Custom", null, null, null, null);
            };
        }
    }

    private record OrderDescriptor(int value, @JsonProperty("isDefault") boolean isDefault) {
    }

    private record VersionDescriptor(int value, @JsonProperty("isDefault") boolean isDefault) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record FieldConditionDescriptor(
            String rawClass,
            ConditionType type,
            @Nullable SingleValueDescriptor<String> expression,
            @Nullable SingleValueDescriptor<Object> custom) {

        static FieldConditionDescriptor of(FieldConditionEvaluator it) {
            final String name = it.getClass().getName();
            return switch (it) {
                case FieldConditionEvaluator.AlwaysFalseFieldConditionEvaluator ignored -> new FieldConditionDescriptor(name, ConditionType.NEVER, null, null);
                case FieldConditionEvaluator.AlwaysTrueFieldConditionEvaluator ignored -> new FieldConditionDescriptor(name, ConditionType.ALWAYS, null, null);
                case FieldConditionEvaluator.ExpressionFieldConditionEvaluator expression -> new FieldConditionDescriptor(name, ConditionType.EXPRESSION, new SingleValueDescriptor<>(expression.expressionString()), null);
                case FieldConditionEvaluator.CustomFieldConditionEvaluator custom -> new FieldConditionDescriptor(name, ConditionType.CUSTOM, null, new SingleValueDescriptor<>(custom.getClass().getName()));
            };
        }

        enum ConditionType {
            ALWAYS, NEVER, EXPRESSION, CUSTOM;

            @JsonValue
            public String jsonValue() {
                return name().toLowerCase();
            }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record FieldLengthDescriptor(
            FieldLengthType type,
            @Nullable SingleValueDescriptor<String> placeholder,
            @Nullable SingleValueDescriptor<Integer> constant,
            @Nullable SingleValueDescriptor<String> expression,
            @Nullable SingleValueDescriptor<Object> prepend,
            @Nullable SingleValueDescriptor<Object> custom) {

        static FieldLengthDescriptor of(BeanPropertyMetadata it) {
            final FieldLengthExtractor lengthExtractor = it.fieldLengthExtractor();
            return switch (lengthExtractor) {
                case FieldLengthExtractor.PlaceholderFieldLengthExtractor placeholder -> new FieldLengthDescriptor(FieldLengthType.PLACEHOLDER, new SingleValueDescriptor<>(placeholder.msg()), null, null, null, null);
                case FieldLengthExtractor.ConstantFieldLengthExtractor constant -> new FieldLengthDescriptor(FieldLengthType.CONSTANT, null, new SingleValueDescriptor<>(constant.length()), null, null, null);
                case FieldLengthExtractor.ExpressionFieldLengthExtractor expression -> new FieldLengthDescriptor(FieldLengthType.EXPRESSION, null, null, new SingleValueDescriptor<>(expression.expressionString()), null, null);
                case FieldLengthExtractor.PrependFieldLengthExtractor prepend -> new FieldLengthDescriptor(FieldLengthType.PREPEND, null, null, null, new SingleValueDescriptor<>(prepend.prependLengthFieldType().name()), null);
                case FieldLengthExtractor.CustomFieldLengthExtractor custom -> new FieldLengthDescriptor(FieldLengthType.CUSTOM, null, null, null, null, new SingleValueDescriptor<>(custom.getClass().getName()));
            };
        }

        enum FieldLengthType {
            PLACEHOLDER, CONSTANT, EXPRESSION, PREPEND, CUSTOM;

            @JsonValue
            public String jsonValue() {
                return name().toLowerCase();
            }
        }
    }

    private record SingleValueDescriptor<T>(T value) {
    }

    private record FieldCodecDescriptor(String name, @Nullable String encoding, @JsonProperty("isBuiltIn") boolean isBuiltIn) {
        static FieldCodecDescriptor of(FieldCodec<?> fieldCodec) {
            final boolean isBuiltin = fieldCodec.getClass().getName().startsWith("io.github.hylexus.xtream.codec.core.impl.codec");
            final String name = isBuiltin ? fieldCodec.getClass().getSimpleName() : fieldCodec.getClass().getName();
            final String encoding = fieldCodec instanceof CharSequenceFieldCodec<?> charSequenceFieldCodec
                    ? charSequenceFieldCodec.encoding()
                    : null;
            return new FieldCodecDescriptor(name, encoding, isBuiltin);
        }
    }

}
