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
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * Record 默认值、条件跳过及空容器隔离测试。
 *
 * @author Codex (AI)
 */
class XtreamFieldDefaultsTest extends BaseEntityCodecTest {

    @ParameterizedTest
    @MethodSource("primitiveDefaults")
    void bothNullStrategiesReturnCorrectlyTypedPrimitiveZero(Class<?> type, Object expected) {
        for (final XtreamField.Nulls strategy : XtreamField.Nulls.values()) {
            final Object value = XtreamFieldUtils.createDefaultValueForNulls(strategy, type);
            assertThat(value).isExactlyInstanceOf(expected.getClass()).isEqualTo(expected);
        }
    }

    static Stream<Arguments> primitiveDefaults() {
        return Stream.of(
                Arguments.of(boolean.class, false), Arguments.of(byte.class, (byte) 0),
                Arguments.of(short.class, (short) 0), Arguments.of(int.class, 0),
                Arguments.of(long.class, 0L), Arguments.of(float.class, 0f),
                Arguments.of(double.class, 0d), Arguments.of(char.class, '\0'));
    }

    @ParameterizedTest
    @MethodSource("referenceTypes")
    void asNullPreservesNullForReferenceTypes(Class<?> type) {
        assertThat(XtreamFieldUtils.createDefaultValueForNulls(XtreamField.Nulls.AS_NULL, type)).isNull();
    }

    static Stream<Class<?>> referenceTypes() {
        return Stream.of(String.class, CharSequence.class, Collection.class, List.class,
                Set.class, Map.class, Integer.class, byte[].class);
    }

    @ParameterizedTest
    @MethodSource("emptyDefaults")
    void asEmptyCreatesAssignableEmptyValues(Class<?> type, Object expected) {
        final Object first = XtreamFieldUtils.createDefaultValueForNulls(XtreamField.Nulls.AS_EMPTY, type);
        final Object second = XtreamFieldUtils.createDefaultValueForNulls(XtreamField.Nulls.AS_EMPTY, type);
        assertThat(first).isInstanceOf(type).isEqualTo(expected);
        if (first instanceof Collection<?> || first instanceof Map<?, ?>) {
            assertThat(first).isNotSameAs(second);
        }
    }

    static Stream<Arguments> emptyDefaults() {
        return Stream.of(
                Arguments.of(String.class, ""), Arguments.of(CharSequence.class, ""),
                Arguments.of(Collection.class, List.of()), Arguments.of(List.class, List.of()),
                Arguments.of(Set.class, Set.of()), Arguments.of(Map.class, Map.of()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void implicitTransientPrimitivesUseZeroWithoutConsumingBytes(boolean tracked) {
        final PrimitiveRecord source = new PrimitiveRecord((short) 1, true, (byte) 2, (short) 3,
                4, 5L, 6f, 7d, 'x', (short) 126);
        final PrimitiveRecord result = roundTrip(1, source, PrimitiveRecord.class, tracked);
        assertThat(result).isEqualTo(new PrimitiveRecord((short) 1, false, (byte) 0, (short) 0,
                0, 0L, 0f, 0d, '\0', (short) 126));
        assertThat(source.booleanValue()).isTrue();
        assertThat(source.charValue()).isEqualTo('x');
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void conditionalPrimitiveComponentsUseZeroWhenSkipped(boolean tracked) {
        final ConditionalRecord source = new ConditionalRecord((short) 1, (byte) 2, (short) 3, 4, 5L, 6f, 7d, (short) 126);
        assertThat(roundTrip(1, source, ConditionalRecord.class, tracked)).isEqualTo(
                new ConditionalRecord((short) 1, (byte) 0, (short) 0, 0, 0L, 0f, 0d, (short) 126));
    }

    @ParameterizedTest
    @CsvSource({"false, 1", "true, 1", "false, 2", "true, 2"})
    void explicitTransientComponentsHonorVersionedNullStrategy(boolean tracked, int version) {
        final EmptyRecord source = new EmptyRecord((short) 1, "ignored", List.of("source"),
                Set.of("source"), Map.of("key", "source"), (short) 126);
        final EmptyRecord result = roundTrip(version, source, EmptyRecord.class, tracked);
        if (version == 1) {
            assertThat(result.text()).isNull();
            assertThat(result.items()).isNull();
            assertThat(result.tags()).isNull();
            assertThat(result.attributes()).isNull();
        } else {
            assertThat(result.text()).isEmpty();
            assertThat(result.items()).isEmpty();
            assertThat(result.tags()).isEmpty();
            assertThat(result.attributes()).isEmpty();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void emptyContainersAreMutableAndIndependentAcrossDecodes(boolean tracked) {
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{1, 126, 1, 126});
        try {
            final EmptyRecord first = entityCodec.decode(2, EmptyRecord.class, buffer, tracker(tracked));
            final EmptyRecord second = entityCodec.decode(2, EmptyRecord.class, buffer, tracker(tracked));
            assertThat(first.items()).isNotSameAs(second.items());
            assertThat(first.tags()).isNotSameAs(second.tags());
            assertThat(first.attributes()).isNotSameAs(second.attributes());
            first.items().add("first");
            first.tags().add("first");
            first.attributes().put("key", "first");
            assertThat(second.items()).isEmpty();
            assertThat(second.tags()).isEmpty();
            assertThat(second.attributes()).isEmpty();
            assertThat(buffer.readerIndex()).isEqualTo(4);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void skippedPojoFieldRetainsItsInitializer(boolean tracked) {
        final InitializedPojo result = roundTrip(1, new InitializedPojo(), InitializedPojo.class, tracked);
        assertThat(result.getDetail()).isEqualTo(99);
    }

    private <T> T roundTrip(int version, T source, Class<T> type, boolean tracked) {
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        try {
            entityCodec.encode(version, source, buffer, tracker(tracked));
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("55017e");
            buffer.writeByte(0x66).readerIndex(1);
            final T result = entityCodec.decode(version, type, buffer, tracker(tracked));
            assertThat(buffer.readerIndex()).isEqualTo(3);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            return result;
        } finally {
            buffer.release();
        }
    }

    private static CodecTracker tracker(boolean tracked) {
        return tracked ? new CodecTracker() : null;
    }

    public record PrimitiveRecord(
            @Preset.RustStyle.u8 short header,
            boolean booleanValue, byte byteValue, short shortValue, int intValue,
            long longValue, float floatValue, double doubleValue, char charValue,
            @Preset.RustStyle.u8 short tail) {
    }

    public record ConditionalRecord(
            @Preset.RustStyle.u8 short header,
            @Preset.RustStyle.i8(condition = "false") byte byteValue,
            @Preset.RustStyle.i16(condition = "false") short shortValue,
            @Preset.RustStyle.i32(condition = "false") int intValue,
            @Preset.RustStyle.i64(condition = "false") long longValue,
            @Preset.RustStyle.f32(condition = "false") float floatValue,
            @Preset.RustStyle.f64(condition = "false") double doubleValue,
            @Preset.RustStyle.u8 short tail) {
    }

    public record EmptyRecord(
            @Preset.RustStyle.u8 short header,
            @Preset.RustStyle.transient_rc(version = 1, nulls = XtreamField.Nulls.AS_NULL)
            @Preset.RustStyle.transient_rc(version = 2, nulls = XtreamField.Nulls.AS_EMPTY) String text,
            @Preset.RustStyle.transient_rc(version = 1, nulls = XtreamField.Nulls.AS_NULL)
            @Preset.RustStyle.transient_rc(version = 2, nulls = XtreamField.Nulls.AS_EMPTY) List<String> items,
            @Preset.RustStyle.transient_rc(version = 1, nulls = XtreamField.Nulls.AS_NULL)
            @Preset.RustStyle.transient_rc(version = 2, nulls = XtreamField.Nulls.AS_EMPTY) Set<String> tags,
            @Preset.RustStyle.transient_rc(version = 1, nulls = XtreamField.Nulls.AS_NULL)
            @Preset.RustStyle.transient_rc(version = 2, nulls = XtreamField.Nulls.AS_EMPTY) Map<String, String> attributes,
            @Preset.RustStyle.u8 short tail) {
    }

    @Getter
    @Setter
    public static class InitializedPojo {
        @Preset.RustStyle.u8
        private short header = 1;
        @Preset.RustStyle.u16(condition = "false")
        private int detail = 99;
        @Preset.RustStyle.u8
        private short tail = 126;
    }
}
