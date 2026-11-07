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

import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.FieldCodec.FieldCodecCreator;
import io.github.hylexus.xtream.codec.core.FieldCodecRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class FieldCodecConstructionTest extends BaseEntityCodecTest {

    private final BeanMetadataRegistry beanRegistry = entityCodec.entityEncoder().getBeanMetadataRegistry();

    @Test
    void createsCodecUsingSinglePrivateNoArgConstructor() {
        assertThat(BeanUtils.createFieldCodecInstance(NoArgCodec.class, beanRegistry)).isInstanceOf(NoArgCodec.class);
    }

    @Test
    void injectsRegistriesIntoSingleConstructorInParameterOrder() {
        final ConstructorInjectedCodec codec = (ConstructorInjectedCodec) BeanUtils.createFieldCodecInstance(ConstructorInjectedCodec.class, beanRegistry);
        assertThat(codec.beanRegistry).isSameAs(beanRegistry);
        assertThat(codec.fieldRegistry).isSameAs(beanRegistry.getFieldCodecRegistry());
    }

    @Test
    void selectsAnnotatedConstructorAndInjectsAwareCallbackThroughRegistry() {
        final SelectedCodec codec = (SelectedCodec) beanRegistry.getFieldCodecRegistry()
                .getOrCreateFieldCodec(beanRegistry, null, SelectedCodec.class, null, null);
        assertThat(codec.beanRegistry).isSameAs(beanRegistry);
        assertThat(codec.constructorRegistry).isSameAs(beanRegistry.getFieldCodecRegistry());
        assertThat(codec.awareRegistry).isSameAs(codec.constructorRegistry);
        assertThat(codec.awareCalls).isEqualTo(1);
    }

    @Test
    void ignoresOnlyUnknownParametersWhenExplicitlyConfigured() {
        final IgnoringUnknownCodec codec = (IgnoringUnknownCodec) BeanUtils.createFieldCodecInstance(IgnoringUnknownCodec.class, beanRegistry);
        assertThat(codec.unknown).isNull();
        assertThat(codec.beanRegistry).isSameAs(beanRegistry);
        assertThat(codec.fieldRegistry).isSameAs(beanRegistry.getFieldCodecRegistry());
    }

    static Stream<Arguments> invalidConstructors() {
        return Stream.of(
                Arguments.of(UnknownParameterCodec.class, "Cannot create FieldCodec instance"),
                Arguments.of(StrictAnnotatedCodec.class, "Cannot create FieldCodec instance"),
                Arguments.of(UnselectedCodec.class, "More than 1 Constructor found"),
                Arguments.of(AmbiguousCodec.class, "More than 1 @FieldCodecCreator found"),
                Arguments.of(CodecInterface.class, "No Constructor found"));
    }

    @ParameterizedTest
    @MethodSource("invalidConstructors")
    void rejectsUnsupportedOrAmbiguousConstructors(Class<?> type, String message) {
        assertThatThrownBy(() -> BeanUtils.createFieldCodecInstance(type, beanRegistry))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining(message).hasMessageContaining(type.getName());
    }

    @Test
    void retainsConstructorFailureAsRootCause() {
        assertThatThrownBy(() -> BeanUtils.createFieldCodecInstance(ThrowingCodec.class, beanRegistry))
                .isInstanceOf(RuntimeException.class).hasCauseInstanceOf(InvocationTargetException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class).hasRootCauseMessage("测试构造失败");
    }

    // 以下夹具的私有构造器通过反射调用或校验，参数签名用于验证构造器选择和依赖注入。
    private static class NoArgCodec extends FieldCodec.Placeholder {
        private NoArgCodec() {
        }
    }

    private static class ConstructorInjectedCodec extends FieldCodec.Placeholder {
        private final FieldCodecRegistry fieldRegistry;
        private final BeanMetadataRegistry beanRegistry;

        @SuppressWarnings("UnusedMethod")
        private ConstructorInjectedCodec(FieldCodecRegistry fieldRegistry, BeanMetadataRegistry beanRegistry) {
            this.fieldRegistry = fieldRegistry;
            this.beanRegistry = beanRegistry;
        }
    }

    private static class SelectedCodec extends FieldCodec.Placeholder implements FieldCodecRegistry.FieldCodecRegistryAware {
        private final BeanMetadataRegistry beanRegistry;
        private final FieldCodecRegistry constructorRegistry;
        private FieldCodecRegistry awareRegistry;
        private int awareCalls;

        private SelectedCodec() {
            throw new AssertionError("不应选择未标注的构造器");
        }

        @FieldCodecCreator
        @SuppressWarnings("UnusedMethod")
        private SelectedCodec(BeanMetadataRegistry beanRegistry, FieldCodecRegistry fieldRegistry) {
            this.beanRegistry = beanRegistry;
            this.constructorRegistry = fieldRegistry;
        }

        @Override
        public void setFieldCodecRegistry(FieldCodecRegistry registry) {
            this.awareRegistry = registry;
            this.awareCalls++;
        }
    }

    private static class IgnoringUnknownCodec extends FieldCodec.Placeholder {
        private final BeanMetadataRegistry beanRegistry;
        private final String unknown;
        private final FieldCodecRegistry fieldRegistry;

        @FieldCodecCreator(ignoreUnknownParameters = true)
        @SuppressWarnings("UnusedMethod")
        private IgnoringUnknownCodec(BeanMetadataRegistry beanRegistry, String unknown, FieldCodecRegistry fieldRegistry) {
            this.beanRegistry = beanRegistry;
            this.unknown = unknown;
            this.fieldRegistry = fieldRegistry;
        }
    }

    private static class UnknownParameterCodec extends FieldCodec.Placeholder {
        @SuppressWarnings({"UnusedMethod", "UnusedVariable"})
        private UnknownParameterCodec(String unknown) {
            throw new AssertionError("未知参数应在调用构造器前拒绝");
        }
    }

    private static class StrictAnnotatedCodec extends FieldCodec.Placeholder {
        @FieldCodecCreator
        @SuppressWarnings({"UnusedMethod", "UnusedVariable"})
        private StrictAnnotatedCodec(String unknown) {
            throw new AssertionError("注解默认不忽略未知参数");
        }
    }

    private static class UnselectedCodec extends FieldCodec.Placeholder {
        private UnselectedCodec() {
        }

        @SuppressWarnings({"UnusedMethod", "UnusedVariable"})
        private UnselectedCodec(BeanMetadataRegistry registry) {
        }
    }

    private static class AmbiguousCodec extends FieldCodec.Placeholder {
        @FieldCodecCreator
        private AmbiguousCodec() {
        }

        @FieldCodecCreator
        @SuppressWarnings({"UnusedMethod", "UnusedVariable"})
        private AmbiguousCodec(BeanMetadataRegistry registry) {
        }
    }

    private interface CodecInterface extends FieldCodec<Object> {
    }

    private static class ThrowingCodec extends FieldCodec.Placeholder {
        private ThrowingCodec() {
            throw new IllegalArgumentException("测试构造失败");
        }
    }
}
