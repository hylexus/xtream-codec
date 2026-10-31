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

import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 继承、重载和链式写方法的字段类型匹配测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class BeanUtilsSetterSelectionTest {
    static Stream<Arguments> selections() {
        return Stream.of(
                Arguments.of(StringOverload.class, IntParent.class, "setValue", int.class),
                Arguments.of(WithOverload.class, IntParent.class, "setValue", int.class),
                Arguments.of(BoxedOverload.class, IntParent.class, "setValue", int.class),
                Arguments.of(OverrideChild.class, OverrideChild.class, "setValue", int.class),
                Arguments.of(BroadChild.class, BroadParent.class, "setValue", Number.class),
                Arguments.of(ExactChild.class, ExactChild.class, "setValue", Integer.class),
                Arguments.of(BoxedOnly.class, BoxedOnly.class, "withValue", Integer.class));
    }

    @ParameterizedTest
    @MethodSource("selections")
    void selectsCompatibleMostSpecificSetterAndKeepsSubclassOverride(
            Class<?> type, Class<?> declaringType, String name, Class<?> parameterType) throws ReflectiveOperationException {

        final var properties = BeanUtils.determineBasicProperties(type, element -> true);
        assertThat(properties).hasSize(1);
        assertThat(properties[0].getWriteMethod()).isEqualTo(declaringType.getDeclaredMethod(name, parameterType));
    }

    @Test
    void ignoresIncompatibleMethodSoFieldAccessCanBeUsed() {
        final var properties = BeanUtils.determineBasicProperties(FieldFallback.class, element -> true);
        assertThat(properties).hasSize(1);
        assertThat(properties[0].getWriteMethod()).isNull();
    }

    static Stream<Arguments> decodedTypes() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, StringOverload.class, 0x1234),
                Arguments.of(tracked, WithOverload.class, 0x1234),
                Arguments.of(tracked, BoxedOverload.class, 0x1234),
                Arguments.of(tracked, OverrideChild.class, 0x1235),
                Arguments.of(tracked, BroadChild.class, 0x1234),
                Arguments.of(tracked, ExactChild.class, 0x1234),
                Arguments.of(tracked, BoxedOnly.class, 0x1234),
                Arguments.of(tracked, FieldFallback.class, 0x1234)));
    }

    @ParameterizedTest
    @MethodSource("decodedTypes")
    void decodesThroughSelectedSetterAndReencodesGetterValue(boolean tracked, Class<?> type, int expected)
            throws ReflectiveOperationException {

        final ByteBuf input = Unpooled.buffer().writeByte(0x55).writeShort(0x1234).writeByte(0x66).readerIndex(1);
        final ByteBuf output = Unpooled.buffer().writeByte(0x77);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final Object decoded = EntityCodec.DEFAULT.decode(XtreamField.ALL_VERSION, type, input, tracker);
            assertThat(type.getMethod("getValue").invoke(decoded)).isEqualTo(expected);
            assertThat(input.readerIndex()).isEqualTo(3);
            assertThat(input.readUnsignedByte()).isEqualTo((short) 0x66);
            EntityCodec.DEFAULT.encode(XtreamField.ALL_VERSION, decoded, output, tracked ? new CodecTracker() : null);
            assertThat(output.writerIndex()).isEqualTo(3);
            assertThat(output.getUnsignedByte(0)).isEqualTo((short) 0x77);
            assertThat(output.getUnsignedShort(1)).isEqualTo(expected);
            assertThat(input.refCnt()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
            if (tracker != null) {
                assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
            }
        } finally {
            input.release();
            output.release();
        }
    }

    // 夹具中的重载方法通过反射扫描，错误的候选被调用时应立即暴露问题。
    public static class IntParent {
        @Preset.RustStyle.u16
        private int value;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    public static class StringOverload extends IntParent {
        public void setValue(String value) {
            throw new AssertionError(value);
        }
    }

    public static class WithOverload extends IntParent {
        public WithOverload withValue(String value) {
            throw new AssertionError(value);
        }
    }

    public static class BoxedOverload extends IntParent {
        public void setValue(Integer value) {
            throw new AssertionError(value);
        }
    }

    public static class OverrideChild extends IntParent {
        @Override
        public void setValue(int value) {
            super.setValue(value + 1);
        }
    }

    public static class BroadParent {
        @Preset.RustStyle.u16
        private Integer value = 0;

        public Integer getValue() {
            return value;
        }

        public void setValue(Number value) {
            this.value = value.intValue();
        }
    }

    public static class BroadChild extends BroadParent {
        public void setValue(Object value) {
            throw new AssertionError(value);
        }
    }

    public static class ExactChild extends BroadParent {
        public void setValue(Integer value) {
            super.setValue(value);
        }
    }

    public static class BoxedOnly {
        @Preset.RustStyle.u16
        private int value;

        public int getValue() {
            return value;
        }

        public BoxedOnly withValue(Integer value) {
            this.value = value;
            return this;
        }
    }

    public static class FieldFallback {
        @Preset.RustStyle.u16
        private int value;

        public int getValue() {
            return value;
        }

        public void setValue(String value) {
            throw new AssertionError(value);
        }
    }
}
