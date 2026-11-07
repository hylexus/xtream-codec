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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hylexus.xtream.codec.base.expression.SpelXtreamExpressionEngine;
import io.github.hylexus.xtream.codec.common.bean.impl.BasicBeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.DefaultFieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.impl.DefaultXtreamExpressionFactory;
import io.github.hylexus.xtream.codec.core.impl.SimpleBeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 真实实体的元数据描述、访问实现和 JSON 输出约定测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class BeanDescriptorTest {
    private final ObjectMapper mapper = new ObjectMapper();

    static Stream<Arguments> accessStrategies() {
        return Stream.of(
                Arguments.of(PropertyAccessStrategy.AUTO, MethodPojo.class, "Method", "LambdaMetaFactory", "lambdaMetaFactory"),
                Arguments.of(PropertyAccessStrategy.LAMBDA_META_FACTORY, MethodPojo.class, "Method", "LambdaMetaFactory", "lambdaMetaFactory"),
                Arguments.of(PropertyAccessStrategy.METHOD_HANDLE, MethodPojo.class, "Method", "MethodHandle", "methodHandle"),
                Arguments.of(PropertyAccessStrategy.REFLECTION, MethodPojo.class, "Method", "Reflection", "reflection"),
                Arguments.of(PropertyAccessStrategy.VAR_HANDLE, MethodPojo.class, "Field", "VarHandle", "varHandle"),
                Arguments.of(PropertyAccessStrategy.AUTO, FieldPojo.class, "Field", "VarHandle", "varHandle"),
                Arguments.of(PropertyAccessStrategy.LAMBDA_META_FACTORY, FieldPojo.class, "Field", "MethodHandle", "methodHandle"),
                Arguments.of(PropertyAccessStrategy.METHOD_HANDLE, FieldPojo.class, "Field", "MethodHandle", "methodHandle"),
                Arguments.of(PropertyAccessStrategy.REFLECTION, FieldPojo.class, "Field", "Reflection", "reflection"),
                Arguments.of(PropertyAccessStrategy.VAR_HANDLE, FieldPojo.class, "Field", "VarHandle", "varHandle"));
    }

    @ParameterizedTest
    @MethodSource("accessStrategies")
    void exportsActualPropertyAccessImplementationsAndOnlyTheirActiveJsonBranch(
            PropertyAccessStrategy strategy, Class<?> type, String accessType, String implementation, String jsonBranch) throws Exception {

        final SimpleBeanMetadataRegistry registry = registry();
        final BeanMetadata metadata = registry.getBeanMetadata(type, XtreamField.ALL_VERSION, property -> {
            final BeanUtils.BasicPropertyDescriptor pd = (BeanUtils.BasicPropertyDescriptor) property.propertyDescriptor();
            final var accessor = PropertyAccessorFactory.createPropertyAccessor(strategy, pd);
            return new BasicBeanPropertyMetadata(registry, pd.getName(), pd.getPropertyType(), property.version(),
                    property.xtreamField(), pd.getField(), accessor.getter(), accessor.setter());
        });
        final BeanDescriptor descriptor = registry.beanDescriptors().findFirst().orElseThrow();
        assertThat(descriptor.rawClass()).isEqualTo(type.getName());
        assertThat(descriptor.type()).isEqualTo(BeanDescriptor.BeanType.CLASS);
        assertThat(descriptor.constructor()).isEqualTo(type.getConstructor().toGenericString());
        assertThat(descriptor.properties()).singleElement().satisfies(property -> {
            assertThat(property.getName()).isEqualTo("value");
            assertThat(property.getType()).isEqualTo("int");
            assertThat(property.isRecordComponent()).isFalse();
            assertThat(property.isRecordClass()).isFalse();
            assertThat(property.getDataTypeName()).isEqualTo("basic");
            assertThat(property.getVersionValue()).isEqualTo(XtreamField.ALL_VERSION);
            assertThat(property.isVersionDefault()).isTrue();
            assertThat(property.getOrder()).isEqualTo(new BeanDescriptor.OrderDescriptor(-1, true));
            assertThat(property.getGetter().type()).isEqualTo(accessType);
            assertThat(property.getSetter().type()).isEqualTo(accessType);
            assertThat(property.getGetter().implementation()).isEqualTo(implementation);
            assertThat(property.getSetter().implementation()).isEqualTo(implementation);
        });
        final JsonNode json = json(descriptor);
        assertThat(json.get("type").asText()).isEqualTo("CLASS");
        final JsonNode property = json.get("properties").get(0);
        assertThat(property.get("name").asText()).isEqualTo("value");
        assertThat(property.get("field").asText()).isEqualTo(type.getDeclaredField("value").toGenericString());
        assertThat(property.get("version").get("isDefault").asBoolean()).isTrue();
        assertThat(property.get("order").get("isDefault").asBoolean()).isTrue();
        assertThat(property.get("getter").get("rawClass").asText()).isEqualTo(metadata.getPropertyMetadataList().getFirst().propertyGetter().getClass().getName());
        assertThat(property.get("setter").get("rawClass").asText()).isEqualTo(metadata.getPropertyMetadataList().getFirst().propertySetter().getClass().getName());
        final boolean method = accessType.equals("Method");
        final String getter = method ? type.getMethod("getValue").toGenericString() : type.getDeclaredField("value").toGenericString();
        final String setter = method ? type.getMethod("setValue", int.class).toGenericString() : type.getDeclaredField("value").toGenericString();
        assertAccessorJson(property.get("getter"), jsonBranch, method ? "method" : "field", getter);
        assertAccessorJson(property.get("setter"), jsonBranch, method ? "method" : "field", setter);
    }

    @ParameterizedTest
    @CsvSource({"1,7,1", "2,3,2"})
    void distinguishesRecordComponentsAndSelectedVersionMetadata(int version, int order, int byteLength) throws Exception {
        final SimpleBeanMetadataRegistry registry = registry();
        registry.getBeanMetadata(VersionedRecord.class, version);
        final BeanDescriptor descriptor = registry.beanDescriptors().findFirst().orElseThrow();
        assertThat(descriptor.type()).isEqualTo(BeanDescriptor.BeanType.RECORD);
        assertThat(descriptor.constructor()).isEqualTo(VersionedRecord.class.getConstructor(int.class).toGenericString());
        final var property = descriptor.properties().getFirst();
        assertThat(property.isRecordComponent()).isTrue();
        assertThat(property.isRecordClass()).isFalse();
        assertThat(property.getVersion()).isEqualTo(new BeanDescriptor.VersionDescriptor(version, false));
        assertThat(property.getOrder()).isEqualTo(new BeanDescriptor.OrderDescriptor(order, false));
        assertThat(property.getLength().constant()).isEqualTo(new BeanDescriptor.SingleValueDescriptor<>(byteLength));
        assertThat(property.getGetter().implementation()).isEqualTo("LambdaMetaFactory");
        assertThat(property.getSetter().type()).isEqualTo("Custom");
        assertThat(property.getSetter().rawClass()).isEqualTo(PropertySetters.RecordReadOnlyPropertySetter.class.getName());
        final JsonNode output = json(descriptor);
        assertThat(output.get("type").asText()).isEqualTo("RECORD");
        final JsonNode component = output.get("properties").get(0);
        assertThat(component.get("recordComponent").asBoolean()).isTrue();
        assertThat(component.get("recordClass").asBoolean()).isFalse();
        assertThat(component.get("versionValue").asInt()).isEqualTo(version);
        assertThat(component.get("versionDefault").asBoolean()).isFalse();
        assertThat(component.get("setter").size()).isEqualTo(3);
        assertThat(component.get("length").get("constant").get("value").asInt()).isEqualTo(byteLength);
    }

    static Stream<Arguments> rules() {
        return Stream.of(
                Arguments.of("fixed", "constant", "constant", "always", "GBK", true),
                Arguments.of("expression", "expression", "expression", "always", "UTF-8", true),
                Arguments.of("prefix", "prepend", "prepend", "expression", "UTF-8", true),
                Arguments.of("skipped", "constant", "constant", "never", null, false),
                Arguments.of("unknown", "constant", "constant", "always", null, true),
                Arguments.of("custom", "custom", "custom", "custom", null, false),
                Arguments.of("utf16", "constant", "constant", "always", "UTF-16BE", true));
    }

    @ParameterizedTest
    @MethodSource("rules")
    void preservesLengthConditionCharsetAndCustomCodecDescriptionsInJson(
            String name, String lengthType, String lengthBranch, String conditionType, @Nullable String charset, boolean builtIn) throws Exception {

        final SimpleBeanMetadataRegistry registry = new SimpleBeanMetadataRegistry(new DefaultFieldCodecRegistry(), type -> true, new RuleFactory());
        registry.getBeanMetadata(RulesPojo.class, XtreamField.ALL_VERSION);
        final BeanDescriptor descriptor = registry.beanDescriptors().findFirst().orElseThrow();
        assertThat(descriptor.properties()).extracting(BeanDescriptor.BeanPropertyDescriptor::getName)
                .containsExactly("fixed", "expression", "prefix", "skipped", "unknown", "custom", "utf16");
        final var property = descriptor.properties().stream().filter(it -> it.getName().equals(name)).findFirst().orElseThrow();
        assertThat(property.getCodec().isBuiltIn()).isEqualTo(builtIn);
        assertThat(property.getCodec().encoding()).isEqualTo(charset);
        assertThat(property.getDesc()).isEqualTo(name);
        final JsonNode beanJson = json(descriptor);
        final JsonNode value = beanJson.get("properties").get(property.getOrder().value() - 1);
        assertThat(value.get("length").get("type").asText()).isEqualTo(lengthType);
        assertThat(value.get("length").size()).isEqualTo(2);
        assertThat(value.get("length").has(lengthBranch)).isTrue();
        assertThat(value.get("condition").get("type").asText()).isEqualTo(conditionType);
        assertThat(value.get("codec").get("isBuiltIn").asBoolean()).isEqualTo(builtIn);
        assertThat(value.get("codec").has("builtIn")).isFalse();
        assertThat(value.get("desc").asText()).isEqualTo(name);
        if (charset == null) {
            assertThat(value.get("codec").get("encoding").isNull()).isTrue();
        } else {
            assertThat(value.get("codec").get("encoding").asText()).isEqualTo(charset);
        }
        switch (name) {
            case "fixed" -> {
                assertThat(value.get("length").get("constant").get("value").asInt()).isEqualTo(2);
                assertThat(value.get("codec").get("name").asText()).isEqualTo("StringFieldCodecGbk");
            }
            case "expression" -> assertThat(value.get("length").get("expression").get("value").asText()).isEqualTo("#size");
            case "prefix" -> {
                assertThat(value.get("length").get("prepend").get("value").asText()).isEqualTo("u16");
                assertThat(value.get("condition").get("expression").get("value").asText()).isEqualTo("true");
                assertThat(value.get("condition").has("custom")).isFalse();
            }
            case "unknown" -> assertThat(value.get("length").get("constant").get("value").asInt()).isEqualTo(-1);
            case "custom" -> {
                assertThat(value.get("length").get("custom").get("value").asText()).isEqualTo(CUSTOM_LENGTH.getClass().getName());
                assertThat(value.get("condition").get("custom").get("value").asText()).isEqualTo(CUSTOM_CONDITION.getClass().getName());
                assertThat(value.get("condition").has("expression")).isFalse();
                assertThat(value.get("codec").get("name").asText()).isEqualTo(CustomTextCodec.class.getName());
            }
            case "utf16" -> {
                assertThat(value.get("length").get("constant").get("value").asInt()).isEqualTo(2);
                assertThat(value.get("codec").get("name").asText()).isEqualTo("StringFieldCodec");
            }
            default -> assertThat(value.get("condition").size()).isEqualTo(2);
        }
    }

    @Test
    void exportsEveryCachedVersionAndSortsByEntityClassName() {
        final SimpleBeanMetadataRegistry registry = registry();
        registry.getBeanMetadata(VersionedRecord.class, 2);
        registry.getBeanMetadata(FieldPojo.class, XtreamField.ALL_VERSION);
        registry.getBeanMetadata(VersionedRecord.class, 1);
        final List<BeanDescriptor> descriptors = registry.beanDescriptors().toList();
        assertThat(descriptors).extracting(BeanDescriptor::rawClass).containsExactly(
                FieldPojo.class.getName(), VersionedRecord.class.getName(), VersionedRecord.class.getName());
        assertThat(descriptors.stream().filter(it -> it.type() == BeanDescriptor.BeanType.RECORD)
                .map(it -> it.properties().getFirst().getVersionValue())).containsExactlyInAnyOrder(1, 2);
    }

    private static SimpleBeanMetadataRegistry registry() {
        return new SimpleBeanMetadataRegistry(new DefaultFieldCodecRegistry(), type -> true);
    }

    private JsonNode json(BeanDescriptor descriptor) throws Exception {
        return mapper.readTree(mapper.writeValueAsString(descriptor));
    }

    private static void assertAccessorJson(JsonNode accessor, String branch, String memberKind, String signature) {
        assertThat(accessor.size()).isEqualTo(4);
        assertThat(accessor.get(branch).size()).isEqualTo(1);
        assertThat(accessor.get(branch).get(memberKind).asText()).isEqualTo(signature);
    }

    public static class MethodPojo {
        @Preset.RustStyle.u8
        private int value;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    public static class FieldPojo {
        @Preset.RustStyle.u8
        public int value;
    }

    public record VersionedRecord(
            @Preset.RustStyle.u8(version = 1, order = 7)
            @Preset.RustStyle.u16(version = 2, order = 3) int value) {
    }

    public static class RulesPojo {
        @Preset.JtStyle.Str(order = 1, length = 2, desc = "fixed")
        public String fixed;

        @Preset.JtStyle.Str(order = 2, lengthExpression = "#size", charset = "UTF-8", desc = "expression")
        public String expression;

        @Preset.JtStyle.Str(order = 3, prependLengthFieldType = PrependLengthFieldType.u16, condition = "true", charset = "UTF-8", desc = "prefix")
        public String prefix;

        @XtreamField(order = 4, codecStrategy = XtreamField.CodecStrategy.TRANSIENT, desc = "skipped")
        public int skipped;

        @Preset.JtStyle.Bytes(order = 5, desc = "unknown")
        public byte[] unknown;

        @Preset.JtStyle.Str(order = 6, fieldCodec = CustomTextCodec.class, desc = "custom")
        public String custom;

        @Preset.JtStyle.Str(order = 7, charset = "UTF-16BE", length = 2, desc = "utf16")
        public String utf16;
    }

    private static final FieldLengthExtractor.CustomFieldLengthExtractor CUSTOM_LENGTH = (context, evaluationContext, input) -> 2;
    private static final FieldConditionEvaluator.CustomFieldConditionEvaluator CUSTOM_CONDITION = context -> true;

    private static class RuleFactory extends DefaultXtreamExpressionFactory {
        RuleFactory() {
            super(new SpelXtreamExpressionEngine());
        }

        @Override
        public @Nullable FieldLengthExtractor createFieldLengthExtractor(XtreamField field) {
            return field.desc().equals("custom") ? CUSTOM_LENGTH : super.createFieldLengthExtractor(field);
        }

        @Override
        public @Nullable FieldConditionEvaluator createFieldConditionEvaluator(XtreamField field) {
            return field.desc().equals("custom") ? CUSTOM_CONDITION : super.createFieldConditionEvaluator(field);
        }
    }

    public static class CustomTextCodec implements FieldCodec<String> {
        @Override
        public String deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            return input.readCharSequence(length, StandardCharsets.UTF_16BE).toString();
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable String value) {
            if (value != null) {
                output.writeCharSequence(value, StandardCharsets.UTF_16BE);
            }
        }
    }
}
