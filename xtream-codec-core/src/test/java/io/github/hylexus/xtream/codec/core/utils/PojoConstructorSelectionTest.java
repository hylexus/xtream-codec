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

package io.github.hylexus.xtream.codec.core.utils;

import io.github.hylexus.xtream.codec.common.exception.BeanIntrospectionException;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.XtreamCacheableClassPredicate;
import io.github.hylexus.xtream.codec.core.annotation.XtreamEntityCreator;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.DefaultFieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.impl.SimpleBeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * POJO 构造器的默认选择、注解优先级、继承签名映射和错误测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class PojoConstructorSelectionTest {

    private final SimpleBeanMetadataRegistry registry = new SimpleBeanMetadataRegistry(
            new DefaultFieldCodecRegistry(), new XtreamCacheableClassPredicate.Default());
    private final EntityCodec codec = new EntityCodec(registry);

    static Stream<Arguments> noArgTypes() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, SingleDefault.class, "single"),
                Arguments.of(tracked, MultipleDefault.class, "default"),
                Arguments.of(tracked, PrivateCreator.class, "private"),
                Arguments.of(tracked, InheritedDefault.class, "inherited"),
                Arguments.of(tracked, MissingSignature.class, "fallback"),
                Arguments.of(tracked, LocalOverride.class, "local")));
    }

    @ParameterizedTest
    @MethodSource("noArgTypes")
    void decodesWithSelectedConstructorAndReencodesFields(boolean tracked, Class<? extends ValueBean> type, String marker)
            throws NoSuchMethodException {

        final var metadata = registry.getBeanMetadata(type, XtreamField.ALL_VERSION);
        assertThat(metadata.getConstructor()).isEqualTo(type.getDeclaredConstructor());
        final ByteBuf input = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump("55123466")).readerIndex(1);
        final ByteBuf output = Unpooled.buffer().writeByte(0x77);
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        try {
            final ValueBean decoded = codec.decode(XtreamField.ALL_VERSION, type, input, decoder);
            assertThat(decoded).isInstanceOf(type);
            assertThat(decoded.construction).isEqualTo(marker);
            assertThat(decoded.getValue()).isEqualTo(0x1234);
            assertThat(input.readerIndex()).isEqualTo(3);
            assertThat(input.getUnsignedByte(3)).isEqualTo((short) 0x66);
            codec.encode(XtreamField.ALL_VERSION, decoded, output, encoder);
            assertThat(ByteBufUtil.hexDump(output)).isEqualTo("771234");
            assertThat(registry.getBeanMetadata(type, XtreamField.ALL_VERSION)).isSameAs(metadata);
            assertThat(input.refCnt()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
            if (decoder != null && encoder != null) {
                assertThat(decoder.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertThat(decoder.getTrace().getDiagnostics()).isEmpty();
                assertThat(encoder.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertThat(encoder.getTrace().getDiagnostics()).isEmpty();
            }
        } finally {
            input.release();
            output.release();
        }
    }

    @ParameterizedTest
    @ValueSource(classes = {LocalParameterCreator.class, InheritedParameterCreator.class})
    void selectsAnnotatedParameterSignatureBeforeDefaultConstructor(Class<?> type) throws ReflectiveOperationException {
        final var metadata = registry.getBeanMetadata(type, XtreamField.ALL_VERSION);
        assertThat(metadata.getConstructor()).isEqualTo(type.getDeclaredConstructor(String.class));
        assertThat(metadata.getConstructor().getDeclaringClass()).isEqualTo(type);
        final ValueBean created = (ValueBean) metadata.getConstructor().newInstance("selected");
        assertThat(created.construction).isEqualTo("selected");
    }

    @Test
    void rejectsClassWithoutPublicConstructorEvenWhenPrivateConstructorIsAnnotated() {
        assertThatThrownBy(() -> registry.getBeanMetadata(PrivateOnly.class, XtreamField.ALL_VERSION))
                .isInstanceOf(BeanIntrospectionException.class)
                .hasMessageContaining("No constructor found").hasMessageContaining(PrivateOnly.class.getName());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void preservesConstructorFailureAndDoesNotConsumeInput(boolean tracked) {
        final ByteBuf input = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump("55123466")).readerIndex(1);
        try {
            assertThatThrownBy(() -> codec.decode(XtreamField.ALL_VERSION, ThrowingDefault.class, input, tracked ? new CodecTracker() : null))
                    .isInstanceOf(RuntimeException.class).hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .hasRootCauseMessage("测试构造失败");
            assertThat(input.readerIndex()).isEqualTo(1);
            assertThat(ByteBufUtil.hexDump(input, 0, input.writerIndex())).isEqualTo("55123466");
            assertThat(input.refCnt()).isEqualTo(1);
        } finally {
            input.release();
        }
    }

    // 以下构造器通过元数据扫描和反射调用，参数签名及可见性均为测试输入。
    public static class ValueBean {
        @Preset.RustStyle.u16
        private int value;
        final String construction;

        protected ValueBean(String construction) {
            this.construction = construction;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    public static class SingleDefault extends ValueBean {
        public SingleDefault() {
            super("single");
        }
    }

    public static class MultipleDefault extends ValueBean {
        public MultipleDefault() {
            super("default");
        }

        public MultipleDefault(int value) {
            super("other:" + value);
        }
    }

    public static class PrivateCreator extends ValueBean {
        @XtreamEntityCreator
        private PrivateCreator() {
            super("private");
        }

        public PrivateCreator(int value) {
            super("int:" + value);
        }

        public PrivateCreator(String value) {
            super(value);
        }
    }

    public static class DefaultParent extends ValueBean {
        @XtreamEntityCreator
        protected DefaultParent() {
            super("parent");
        }

        protected DefaultParent(String marker) {
            super(marker);
        }
    }

    public static class InheritedDefault extends DefaultParent {
        public InheritedDefault() {
            super("inherited");
        }

        public InheritedDefault(int value) {
            super("other:" + value);
        }
    }

    public static class ParameterParent extends ValueBean {
        @XtreamEntityCreator
        protected ParameterParent(String marker) {
            super(marker);
        }
    }

    public static class MissingSignature extends ParameterParent {
        public MissingSignature() {
            super("fallback");
        }

        public MissingSignature(int value) {
            super("other:" + value);
        }
    }

    public static class LocalOverride extends ParameterParent {
        @XtreamEntityCreator
        public LocalOverride() {
            super("local");
        }

        public LocalOverride(String marker) {
            super(marker);
        }
    }

    public static class LocalParameterCreator extends ValueBean {
        public LocalParameterCreator() {
            super("default");
        }

        @XtreamEntityCreator
        public LocalParameterCreator(String marker) {
            super(marker);
        }
    }

    public static class InheritedParameterCreator extends ParameterParent {
        public InheritedParameterCreator() {
            super("default");
        }

        public InheritedParameterCreator(String marker) {
            super(marker);
        }
    }

    public static class PrivateOnly extends ValueBean {
        @XtreamEntityCreator
        private PrivateOnly() {
            super("private-only");
        }
    }

    public static class ThrowingDefault extends ValueBean {
        public ThrowingDefault() {
            super("throwing");
            throw new IllegalArgumentException("测试构造失败");
        }
    }
}
