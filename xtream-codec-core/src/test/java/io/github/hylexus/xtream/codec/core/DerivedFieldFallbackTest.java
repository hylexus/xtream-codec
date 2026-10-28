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

package io.github.hylexus.xtream.codec.core;

import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.core.annotation.DerivedField;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 派生字段的版本回退、只读逆变换和异常后的解码连续性测试。
 *
 * @author Codex (AI)
 */
class DerivedFieldFallbackTest extends BaseEntityCodecTest {

    @ParameterizedTest
    @MethodSource("versions")
    void exactVersionWinsRegardlessOfAnnotationOrderAndUnmatchedFieldsKeepTheirInitializer(int version, boolean tracked) {
        final VersionedPojo source = new VersionedPojo();
        final VersionedPojo result = roundTrip(version, source, VersionedPojo.class, tracked);
        final String expected = version == 1 || version == 2 ? "exact:7" : "default:7";
        assertThat(result.getDefaultFirst()).isEqualTo(expected);
        assertThat(result.getExactFirst()).isEqualTo(expected);
        assertThat(result.getOnlyVersionOne()).isEqualTo(version == 1 ? "exact:7" : "initial");
        assertThat(result.getSource()).isEqualTo(7L);
        assertThat(result.getTail()).isEqualTo((short) 126);
    }

    static Stream<Arguments> versions() {
        return Stream.of(1, 2, 3, XtreamField.ALL_VERSION)
                .flatMap(version -> Stream.of(Arguments.of(version, false), Arguments.of(version, true)));
    }

    @ParameterizedTest
    @CsvSource({"1, false", "1, true", "3, false", "3, true"})
    void recordDerivedFieldsUseExactOrDefaultVersionWithoutConsumingBytes(int version, boolean tracked) {
        final VersionedRecord source = new VersionedRecord(7L, "ignored", (short) 126);
        assertThat(roundTrip(version, source, VersionedRecord.class, tracked)).isEqualTo(
                new VersionedRecord(7L, version == 1 ? "exact:7" : "default:7", (short) 126));
    }

    @ParameterizedTest
    @MethodSource("versions")
    void unmatchedRecordDerivedComponentsKeepTheirConstructorPositionsAndDefaults(int version, boolean tracked) {
        final VersionOnlyRecord source = new VersionOnlyRecord(7L, "ignored", 99, true, (short) 126);
        assertThat(roundTrip(version, source, VersionOnlyRecord.class, tracked)).isEqualTo(
                new VersionOnlyRecord(7L, version == 1 ? "exact:7" : null,
                        version == 1 ? 7 : 0, version == 1, (short) 126));
    }

    @ParameterizedTest
    @CsvSource({"1, false", "1, true", "2, false", "2, true"})
    void failedOrNullPrimitiveDerivedValuesUseTypedDefaultsWithoutBlockingFollowingFields(int version, boolean tracked) {
        final PrimitiveDerivedRecord source = new PrimitiveDerivedRecord(7L,
                (byte) 9, (short) 9, 9, 9L, 9F, 9D, 'x', true, "read:7", (short) 126);
        assertThat(roundTrip(version, source, PrimitiveDerivedRecord.class, tracked)).isEqualTo(
                new PrimitiveDerivedRecord(7L, (byte) 0, (short) 0, 0, 0L, 0F, 0D, '\0', false, "read:7", (short) 126));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void absentSourceLeavesPrimitiveDerivedComponentsAtTheirDefaults(boolean tracked) {
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        try {
            final MissingSourceRecord source = new MissingSourceRecord(null, 99, true, (short) 126);
            entityCodec.encode(1, source, buffer, tracker(tracked));
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("557e");
            buffer.writeByte(0x66).readerIndex(1);
            assertThat(entityCodec.decode(1, MissingSourceRecord.class, buffer, tracker(tracked)))
                    .isEqualTo(new MissingSourceRecord(null, 0, false, (short) 126));
            assertThat(buffer.readerIndex()).isEqualTo(2);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, false", "false, true", "true, false", "true, true"})
    void nullDerivedValueOrUnsupportedWriteFallsBackToSourceWithoutMutatingInstance(boolean tracked, boolean populated) {
        final ReadOnlyPojo source = new ReadOnlyPojo();
        source.setView(populated ? "client value" : null);
        final ReadOnlyPojo result = roundTrip(1, source, ReadOnlyPojo.class, tracked);
        assertThat(source.getSource()).isEqualTo(7L);
        assertThat(source.getView()).isEqualTo(populated ? "client value" : null);
        assertThat(result.getSource()).isEqualTo(7L);
        assertThat(result.getView()).isEqualTo("read:7");
        assertThat(result.getTail()).isEqualTo((short) 126);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void otherWriteErrorsPropagateWithoutFallingBackToSource(boolean tracked) {
        final ReadOnlyPojo source = new ReadOnlyPojo();
        source.setView("invalid");
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        try {
            assertThatThrownBy(() -> entityCodec.encode(2, source, buffer, tracker(tracked)))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("派生值无法编码");
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("55");
            assertThat(source.getSource()).isEqualTo(7L);
            assertThat(source.getView()).isEqualTo("invalid");
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void readFailurePreservesPojoInitializerAndAllowsOtherDerivedAndFollowingFields(boolean tracked) {
        final FailedReadPojo result = decode(FailedReadPojo.class, tracked);
        assertThat(result.getSource()).isEqualTo(7L);
        assertThat(result.getBroken()).isEqualTo("initial");
        assertThat(result.getHealthy()).isEqualTo("read:7");
        assertThat(result.getTail()).isEqualTo((short) 126);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void readFailureLeavesRecordComponentNullAndAllowsOtherDerivedAndFollowingFields(boolean tracked) {
        assertThat(decode(FailedReadRecord.class, tracked)).isEqualTo(
                new FailedReadRecord(7L, null, "read:7", (short) 126));
    }

    private <T> T roundTrip(int version, T source, Class<T> type, boolean tracked) {
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        try {
            entityCodec.encode(version, source, buffer, tracker(tracked));
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("55077e");
            buffer.writeByte(0x66).readerIndex(1);
            final T result = entityCodec.decode(version, type, buffer, tracker(tracked));
            assertThat(buffer.readerIndex()).isEqualTo(3);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            return result;
        } finally {
            buffer.release();
        }
    }

    private <T> T decode(Class<T> type, boolean tracked) {
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{0x55, 7, 126, 0x66}).readerIndex(1);
        try {
            final T result = entityCodec.decode(XtreamField.ALL_VERSION, type, buffer, tracker(tracked));
            assertThat(buffer.readerIndex()).isEqualTo(3);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            return result;
        } finally {
            buffer.release();
        }
    }

    private static @Nullable CodecTracker tracker(boolean tracked) {
        return tracked ? new CodecTracker() : null;
    }

    @Getter
    @Setter
    public static class VersionedPojo {
        @Preset.RustStyle.u8
        private long source = 7;
        @DerivedField(source = "source", using = DefaultTransformer.class)
        @DerivedField(source = "source", using = ExactTransformer.class, version = {1, 2})
        private String defaultFirst;
        @DerivedField(source = "source", using = ExactTransformer.class, version = {1, 2})
        @DerivedField(source = "source", using = DefaultTransformer.class)
        private String exactFirst;
        @DerivedField(source = "source", using = ExactTransformer.class, version = 1)
        private String onlyVersionOne = "initial";
        @Preset.RustStyle.u8
        private short tail = 126;
    }

    public record VersionedRecord(
            @Preset.RustStyle.u8 long source,
            @DerivedField(source = "source", using = DefaultTransformer.class)
            @DerivedField(source = "source", using = ExactTransformer.class, version = 1) String view,
            @Preset.RustStyle.u8 short tail) {
    }

    public record VersionOnlyRecord(
            @Preset.RustStyle.u8 long source,
            @DerivedField(source = "source", using = ExactTransformer.class, version = 1) @Nullable String view,
            @DerivedField(source = "source", using = IntegerTransformer.class, version = 1) int number,
            @DerivedField(source = "source", using = BooleanTransformer.class, version = 1) boolean flag,
            @Preset.RustStyle.u8 short tail) {
    }

    public record MissingSourceRecord(
            @Preset.RustStyle.u8(condition = "false") @Nullable Long source,
            @DerivedField(source = "source", using = IntegerTransformer.class) int number,
            @DerivedField(source = "source", using = BooleanTransformer.class) boolean flag,
            @Preset.RustStyle.u8 short tail) {
    }

    public record PrimitiveDerivedRecord(
            @Preset.RustStyle.u8 long source,
            @DerivedField(source = "source", using = FailedPrimitiveTransformer.class, version = 1)
            @DerivedField(source = "source", using = NullPrimitiveTransformer.class) byte byteValue,
            @DerivedField(source = "source", using = FailedPrimitiveTransformer.class, version = 1)
            @DerivedField(source = "source", using = NullPrimitiveTransformer.class) short shortValue,
            @DerivedField(source = "source", using = FailedPrimitiveTransformer.class, version = 1)
            @DerivedField(source = "source", using = NullPrimitiveTransformer.class) int intValue,
            @DerivedField(source = "source", using = FailedPrimitiveTransformer.class, version = 1)
            @DerivedField(source = "source", using = NullPrimitiveTransformer.class) long longValue,
            @DerivedField(source = "source", using = FailedPrimitiveTransformer.class, version = 1)
            @DerivedField(source = "source", using = NullPrimitiveTransformer.class) float floatValue,
            @DerivedField(source = "source", using = FailedPrimitiveTransformer.class, version = 1)
            @DerivedField(source = "source", using = NullPrimitiveTransformer.class) double doubleValue,
            @DerivedField(source = "source", using = FailedPrimitiveTransformer.class, version = 1)
            @DerivedField(source = "source", using = NullPrimitiveTransformer.class) char charValue,
            @DerivedField(source = "source", using = FailedPrimitiveTransformer.class, version = 1)
            @DerivedField(source = "source", using = NullPrimitiveTransformer.class) boolean booleanValue,
            @DerivedField(source = "source", using = ReadOnlyTransformer.class) String healthy,
            @Preset.RustStyle.u8 short tail) {
    }

    @Getter
    @Setter
    public static class ReadOnlyPojo {
        @Preset.RustStyle.u8
        private long source = 7;
        @DerivedField(source = "source", using = ReadOnlyTransformer.class, reverseSource = true)
        @DerivedField(source = "source", using = FailedWriteTransformer.class, reverseSource = true, version = 2)
        private String view;
        @Preset.RustStyle.u8
        private short tail = 126;
    }

    @Getter
    @Setter
    public static class FailedReadPojo {
        @Preset.RustStyle.u8
        private long source;
        @DerivedField(source = "source", using = FailedReadTransformer.class)
        private String broken = "initial";
        @DerivedField(source = "source", using = ReadOnlyTransformer.class)
        private String healthy;
        @Preset.RustStyle.u8(condition = "#healthy == 'read:7'")
        private short tail;
    }

    public record FailedReadRecord(
            @Preset.RustStyle.u8 long source,
            @DerivedField(source = "source", using = FailedReadTransformer.class) @Nullable String broken,
            @DerivedField(source = "source", using = ReadOnlyTransformer.class) String healthy,
            @Preset.RustStyle.u8(condition = "#healthy == 'read:7'") short tail) {
    }

    public static class DefaultTransformer implements FieldTransformer<Long, String> {
        @Override
        public String read(@Nullable Long source) {
            return "default:" + source;
        }
    }

    public static class ExactTransformer implements FieldTransformer<Long, String> {
        @Override
        public String read(@Nullable Long source) {
            return "exact:" + source;
        }
    }

    public static class IntegerTransformer implements FieldTransformer<Long, Integer> {
        @Override
        public Integer read(@Nullable Long source) {
            return source == null ? 0 : source.intValue();
        }
    }

    public static class BooleanTransformer implements FieldTransformer<Long, Boolean> {
        @Override
        public Boolean read(@Nullable Long source) {
            return source != null && source != 0;
        }
    }

    public static class FailedPrimitiveTransformer implements FieldTransformer<Long, Object> {
        @Override
        public Object read(@Nullable Long source) {
            throw new IllegalStateException("基本类型派生值无法计算");
        }
    }

    public static class NullPrimitiveTransformer implements FieldTransformer<Long, Object> {
        @Override
        public @Nullable Object read(@Nullable Long source) {
            return null;
        }
    }

    public static class ReadOnlyTransformer implements FieldTransformer<Long, String> {
        @Override
        public String read(@Nullable Long source) {
            return "read:" + source;
        }
    }

    public static class FailedReadTransformer implements FieldTransformer<Long, String> {
        @Override
        public String read(@Nullable Long source) {
            throw new IllegalStateException("派生值无法计算");
        }
    }

    public static class FailedWriteTransformer extends ReadOnlyTransformer {
        @Override
        public Long write(@Nullable String derived) {
            throw new IllegalArgumentException("派生值无法编码");
        }
    }
}
