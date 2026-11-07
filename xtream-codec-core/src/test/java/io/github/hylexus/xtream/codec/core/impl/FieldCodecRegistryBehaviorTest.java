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

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.FieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodecRegistry.CodecDescriptor;
import io.github.hylexus.xtream.codec.core.annotation.NumberEndian;
import io.github.hylexus.xtream.codec.core.annotation.NumberSignedness;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.codec.CharSequenceFieldCodec;
import io.github.hylexus.xtream.codec.core.impl.codec.DelegateBeanMetadataFieldCodec;
import io.github.hylexus.xtream.codec.core.impl.codec.I16FieldCodecs;
import io.github.hylexus.xtream.codec.core.impl.codec.StringFieldCodecs;
import io.github.hylexus.xtream.codec.core.impl.codec.U16FieldCodecs;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldCodecRegistryBehaviorTest {

    private final DefaultFieldCodecRegistry registry = new DefaultFieldCodecRegistry(false);

    @Test
    void emptyRegistryHasNoDescriptorsOrImplicitCodec() {
        assertThat(registry.descriptors()).isEmpty();
        assertThat(registry.getFieldCodec(2, NumberSignedness.SIGNED, "", false, Short.class)).isEmpty();
    }

    @Test
    void descriptorsExposeNumericCharsetAndCustomCodecMetadata() {
        registry.register(U16FieldCodecs.INTEGER_INSTANCE, Integer.class, 2, "", false);
        registry.register(I16FieldCodecs.SHORT_INSTANCE_LE, Short.class, 2, "", true);
        registry.register(StringFieldCodecs.INSTANCE_GB_2312, String.class, -1, "GB2312", false);
        final UserCodec custom = new UserCodec();
        registry.registerPojoFieldCodec(custom, AtomicValue.class);
        assertThat(registry.descriptors()).containsExactly(
                new CodecDescriptor("byte[2] <--> java.lang.Integer(BE)[Unsigned]", U16FieldCodecs.INTEGER_INSTANCE.getClass().getName(),
                        NumberSignedness.UNSIGNED, "NONE", true, NumberEndian.BIG_ENDIAN),
                new CodecDescriptor("byte[2] <--> java.lang.Short(LE)[Signed]", I16FieldCodecs.SHORT_INSTANCE_LE.getClass().getName(),
                        NumberSignedness.SIGNED, "NONE", true, NumberEndian.LITTLE_ENDIAN),
                new CodecDescriptor("byte[n] (gb2312) <--> java.lang.String", StringFieldCodecs.INSTANCE_GB_2312.getClass().getName(),
                        NumberSignedness.NONE, "GB2312", true, NumberEndian.NONE),
                new CodecDescriptor("byte[n] <--> " + AtomicValue.class.getName(), UserCodec.class.getName(),
                        NumberSignedness.NONE, "NONE", false, NumberEndian.NONE));
        assertThat(registry.getFieldCodec(2, NumberSignedness.UNSIGNED, "", false, Integer.class)).containsSame(U16FieldCodecs.INTEGER_INSTANCE);
        assertThat(registry.getFieldCodec(2, NumberSignedness.SIGNED, "", true, Short.class)).containsSame(I16FieldCodecs.SHORT_INSTANCE_LE);
        assertThat(registry.getFieldCodec(-1, NumberSignedness.NONE, "gb2312", false, String.class)).containsSame(StringFieldCodecs.INSTANCE_GB_2312);
    }

    @Test
    void replacementUpdatesLookupAndReusableInstanceWithoutAddingDescriptor() {
        final UserCodec first = new UserCodec();
        final UserCodec second = new UserCodec();
        registry.registerPojoFieldCodec(first, AtomicValue.class);
        registry.registerPojoFieldCodec(second, AtomicValue.class);
        assertThat(registry.getFieldCodecForAtomicDataType(AtomicValue.class)).containsSame(second);
        assertThat(registry.getOrCreateFieldCodec(null, null, UserCodec.class, null, null)).isSameAs(second);
        assertThat(registry.descriptors()).hasSize(1);
        assertThat(first.injectedRegistry).isSameAs(registry);
        assertThat(second.injectedRegistry).isSameAs(registry);
        assertThat(second.injectionCalls).isEqualTo(1);
    }

    @Test
    void explicitDataTypeTakesPriorityOverCodecAndEntityConfiguration() {
        assertThat(registry.getOrCreateFieldCodec(null, XtreamDataType.u16, UserCodec.class, null, AtomicValue.class))
                .isSameAs(XtreamDataType.u16.codec());
    }

    @ParameterizedTest
    @CsvSource({"false, GBK", "true, GBK", "false, UTF-16BE", "true, UTF-16BE"})
    void createsStringCodecWithRequestedCharset(boolean explicitCodec, String charset) {
        final FieldCodec<?> codec = registry.getOrCreateFieldCodec(null, explicitCodec ? null : XtreamDataType.string,
                explicitCodec ? StringFieldCodecs.StringFieldCodec.class : null, charset, null);
        assertThat(codec).isInstanceOf(CharSequenceFieldCodec.class);
        assertThat(((CharSequenceFieldCodec<?>) codec).encoding()).isEqualTo(charset);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void stringCodecRequiresCharset(boolean explicitCodec) {
        assertThatThrownBy(() -> registry.getOrCreateFieldCodec(null, explicitCodec ? null : XtreamDataType.string,
                explicitCodec ? StringFieldCodecs.StringFieldCodec.class : null, null, null))
                .isInstanceOf(NullPointerException.class).hasMessage("StringFieldCodec.charset is null");
    }

    @Test
    void atomicEntityResolutionRequiresRegisteredCodec() {
        final BeanMetadataRegistry beanRegistry = mock(BeanMetadataRegistry.class);
        assertThatThrownBy(() -> registry.getOrCreateFieldCodec(beanRegistry, null, null, null, AtomicValue.class))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining(AtomicValue.class.getName());
        final UserCodec codec = new UserCodec();
        registry.registerPojoFieldCodec(codec, AtomicValue.class);
        assertThat(registry.getOrCreateFieldCodec(beanRegistry, null, null, null, AtomicValue.class)).isSameAs(codec);
    }

    @Test
    void noConcreteConfigurationReturnsNoCodec() {
        assertThat(registry.getOrCreateFieldCodec(null, null, null, null, null)).isNull();
        assertThat(registry.getOrCreateFieldCodec(null, XtreamDataType.placeholder, FieldCodec.Placeholder.class, null, Object.class)).isNull();
    }

    @Test
    void transientMetadataUsesTransientCodecBeforeOtherConfiguration() {
        final BeanPropertyMetadata metadata = metadata(Map.of("codecStrategy", XtreamField.CodecStrategy.TRANSIENT,
                "fieldCodec", UserCodec.class));
        assertThat(registry.getFieldCodec(metadata)).containsSame(FieldCodec.TransientRecordComponentFieldCodec.INSTANCE);
    }

    @Test
    void recordMetadataResolvesNestedEntityDelegate() {
        final BeanPropertyMetadata metadata = metadata(Map.of());
        when(metadata.isRecordClass()).thenReturn(true);
        doReturn(RecordValue.class).when(metadata).rawClass();
        assertThat(registry.getFieldCodec(metadata)).hasValueSatisfying(codec -> assertThat(codec).isInstanceOf(DelegateBeanMetadataFieldCodec.class));
    }

    @Test
    void atomicMetadataResolvesPreviouslyRegisteredInstance() {
        final BeanPropertyMetadata metadata = metadata(Map.of());
        doReturn(AtomicValue.class).when(metadata).rawClass();
        assertThat(registry.getFieldCodec(metadata)).isEmpty();
        final UserCodec codec = new UserCodec();
        registry.registerPojoFieldCodec(codec, AtomicValue.class);
        assertThat(registry.getFieldCodec(metadata)).containsSame(codec);
    }

    private BeanPropertyMetadata metadata(Map<String, Object> attributes) {
        final BeanPropertyMetadata metadata = mock(BeanPropertyMetadata.class);
        when(metadata.xtreamFieldAnnotation()).thenReturn(AnnotationUtils.synthesizeAnnotation(attributes, XtreamField.class, null));
        return metadata;
    }

    static class AtomicValue implements FieldCodecRegistry.AtomicDataType {
    }

    record RecordValue(int value) {
    }

    static class UserCodec extends FieldCodec.Placeholder implements FieldCodecRegistry.FieldCodecRegistryAware {
        private FieldCodecRegistry injectedRegistry;
        private int injectionCalls;

        @Override
        public void setFieldCodecRegistry(FieldCodecRegistry registry) {
            this.injectedRegistry = registry;
            this.injectionCalls++;
        }
    }
}
