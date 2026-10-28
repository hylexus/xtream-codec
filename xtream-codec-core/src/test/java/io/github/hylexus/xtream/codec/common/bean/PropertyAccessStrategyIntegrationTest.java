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

import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.core.annotation.XtreamEntity;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 通过可区分的字段与方法访问结果验证实体注解的策略优先级。
 *
 * @author Codex (AI)
 */
class PropertyAccessStrategyIntegrationTest extends BaseEntityCodecTest {

    @ParameterizedTest
    @MethodSource("strategies")
    void fieldStrategiesOverrideClassStrategiesAndAutoInheritsOrSelectsAccessors(
            Class<?> type, String hex, Map<String, Integer> initial, Map<String, Integer> decoded) throws Exception {
        for (final boolean tracked : new boolean[]{false, true}) {
            final Object source = type.getConstructor().newInstance();
            final ByteBuf buffer = allocator.buffer().writeByte(0x55);
            try {
                entityCodec.encode(XtreamField.ALL_VERSION, source, buffer, tracked ? new CodecTracker() : null);
                assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("55" + hex);
                assertFields(source, initial);
                buffer.writeByte(0x66).readerIndex(1);
                final Object result = entityCodec.decode(XtreamField.ALL_VERSION, type, buffer, tracked ? new CodecTracker() : null);
                assertFields(result, decoded);
                assertThat(buffer.readerIndex()).isEqualTo(hex.length() / 2 + 1);
                assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            } finally {
                buffer.release();
            }
        }
    }

    static Stream<Arguments> strategies() {
        return Stream.of(
                Arguments.of(FieldStrategyEntity.class, "0718191a",
                        Map.of("inherited", 7, "reflection", 8, "methodHandle", 9, "lambda", 10),
                        Map.of("inherited", 7, "reflection", 56, "methodHandle", 57, "lambda", 58)),
                Arguments.of(MethodStrategyEntity.class, "1708",
                        Map.of("inherited", 7, "direct", 8), Map.of("inherited", 55, "direct", 8)),
                Arguments.of(AutoStrategyEntity.class, "1708",
                        Map.of("method", 7, "fieldOnly", 8), Map.of("method", 55, "fieldOnly", 8)));
    }

    private static void assertFields(Object instance, Map<String, Integer> expected) throws Exception {
        for (final Map.Entry<String, Integer> entry : expected.entrySet()) {
            final Field field = instance.getClass().getDeclaredField(entry.getKey());
            field.setAccessible(true);
            assertThat(field.get(instance)).as(entry.getKey()).isEqualTo(entry.getValue());
        }
    }

    @XtreamEntity(propertyAccessStrategy = PropertyAccessStrategy.VAR_HANDLE)
    public static class FieldStrategyEntity {
        @Preset.RustStyle.u8
        private int inherited = 7;
        @Preset.RustStyle.u8(propertyAccessStrategy = PropertyAccessStrategy.REFLECTION)
        private int reflection = 8;
        @Preset.RustStyle.u8(propertyAccessStrategy = PropertyAccessStrategy.METHOD_HANDLE)
        private int methodHandle = 9;
        @Preset.RustStyle.u8(propertyAccessStrategy = PropertyAccessStrategy.LAMBDA_META_FACTORY)
        private int lambda = 10;

        public int getInherited() {
            return inherited + 16;
        }

        public void setInherited(int value) {
            inherited = value + 32;
        }

        public int getReflection() {
            return reflection + 16;
        }

        public void setReflection(int value) {
            reflection = value + 32;
        }

        public int getMethodHandle() {
            return methodHandle + 16;
        }

        public void setMethodHandle(int value) {
            methodHandle = value + 32;
        }

        public int getLambda() {
            return lambda + 16;
        }

        public void setLambda(int value) {
            lambda = value + 32;
        }
    }

    @XtreamEntity(propertyAccessStrategy = PropertyAccessStrategy.REFLECTION)
    public static class MethodStrategyEntity {
        @Preset.RustStyle.u8
        private int inherited = 7;
        @Preset.RustStyle.u8(propertyAccessStrategy = PropertyAccessStrategy.VAR_HANDLE)
        private int direct = 8;

        public int getInherited() {
            return inherited + 16;
        }

        public void setInherited(int value) {
            inherited = value + 32;
        }

        public int getDirect() {
            return direct + 16;
        }

        public void setDirect(int value) {
            direct = value + 32;
        }
    }

    @XtreamEntity
    public static class AutoStrategyEntity {
        @Preset.RustStyle.u8
        private int method = 7;
        // 通过字段访问器读写及反射断言验证 AUTO 策略，故意不提供 getter/setter。
        @Preset.RustStyle.u8
        @SuppressWarnings("UnusedVariable")
        private int fieldOnly = 8;

        public int getMethod() {
            return method + 16;
        }

        public void setMethod(int value) {
            method = value + 32;
        }
    }
}
