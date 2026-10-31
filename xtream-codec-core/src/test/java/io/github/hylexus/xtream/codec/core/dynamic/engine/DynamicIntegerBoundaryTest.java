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

package io.github.hylexus.xtream.codec.core.dynamic.engine;

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 精确整数转换与 JSON 安全整数临界值的公开入口测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicIntegerBoundaryTest {
    private static final DynamicPlan PLAN = DynamicCompiler.compile(DynamicSchema.of(
            DynamicField.field("head", "u8").build(), DynamicField.field("value", "i64").build(),
            DynamicField.field("tail", "u8").build()));

    static Stream<Arguments> exactIntegers() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, 9007199254740991L, 9007199254740991L, "001fffffffffffff"),
                Arguments.of(tracked, -9007199254740991L, -9007199254740991L, "ffe0000000000001"),
                Arguments.of(tracked, 9007199254740992L, "9007199254740992", "0020000000000000"),
                Arguments.of(tracked, -9007199254740992L, "-9007199254740992", "ffe0000000000000"),
                Arguments.of(tracked, new BigDecimal("7.000"), 7L, "0000000000000007"),
                Arguments.of(tracked, BigInteger.valueOf(7), 7L, "0000000000000007"),
                Arguments.of(tracked, "7", 7L, "0000000000000007")));
    }

    @ParameterizedTest
    @MethodSource("exactIntegers")
    void preservesIntegerPrecisionAndSwitchesJsonTypeAtTheExactThreshold(boolean tracked, Object input, Object expected, String hex) {
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        try {
            PLAN.encode(values(input), buffer, tracked ? new CodecTracker() : null);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("557e" + hex + "7f");
            buffer.writeByte(0x66).readerIndex(1);
            final DynamicDecodeResult result = PLAN.decode(buffer, tracked ? new CodecTracker() : null);
            assertThat(result.values().fields().get("value")).isExactlyInstanceOf(expected.getClass()).isEqualTo(expected);
            assertThat(result.consumedBytes()).isEqualTo(10);
            assertThat(result.remainingBytes()).isEqualTo(1);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
        } finally {
            buffer.release();
        }
    }

    static Stream<Arguments> invalidIntegers() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                new BigDecimal("1.5"), "1.0", "", "0x10", "invalid", 1D, 1F,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, true, null)
                .map(value -> Arguments.of(tracked, value)));
    }

    @ParameterizedTest
    @MethodSource("invalidIntegers")
    void rejectsNonIntegerInputsWithoutWritingTheValueOrTail(boolean tracked, @Nullable Object input) {
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                    () -> PLAN.encode(values(input), buffer, tracker));
            assertThat(error).isNotNull();
            assertThat(error.fieldPath()).isEqualTo("$.value");
            assertThat(error.bitOffset()).isEqualTo(8);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("557e");
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (tracker != null) {
                assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(tracker.getTrace().getDiagnostics()).isNotEmpty();
            }
        } finally {
            buffer.release();
        }
    }

    private static DynamicValues values(@Nullable Object value) {
        final Map<String, Object> values = new HashMap<>();
        values.put("head", 126);
        values.put("value", value);
        values.put("tail", 127);
        return DynamicValues.of(values);
    }
}
