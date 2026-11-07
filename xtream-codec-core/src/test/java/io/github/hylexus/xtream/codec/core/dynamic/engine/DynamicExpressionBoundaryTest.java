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

import io.github.hylexus.xtream.codec.base.expression.AbstractXtreamEvaluationContext;
import io.github.hylexus.xtream.codec.base.expression.CustomXtreamExpression;
import io.github.hylexus.xtream.codec.base.expression.CustomXtreamExpressionEngine;
import io.github.hylexus.xtream.codec.base.expression.XtreamExpressionEngineRegistry;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicExpression;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtensionRegistry;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 通过动态计划验证表达式结果的资源边界、只读快照及错误位置。
 *
 * @author Codex (AI)
 */
class DynamicExpressionBoundaryTest {

    @ParameterizedTest
    @MethodSource("invalidResults")
    void rejectsInvalidEngineResultsWithLocationAndUncommittedReadIndex(Object value, String message) {
        final DynamicPlan plan = returning(value);
        for (final boolean tracked : new boolean[]{false, true}) {
            assertRejected(plan, message, tracked);
        }
    }

    static Stream<Arguments> invalidResults() {
        return Stream.of(
                Arguments.of(Double.NaN, "有限数值"),
                Arguments.of(Double.POSITIVE_INFINITY, "有限数值"),
                Arguments.of(Double.NEGATIVE_INFINITY, "有限数值"),
                Arguments.of(Float.NaN, "有限数值"),
                Arguments.of(Float.POSITIVE_INFINITY, "有限数值"),
                Arguments.of(Float.NEGATIVE_INFINITY, "有限数值"),
                Arguments.of(BigInteger.ONE.shiftLeft(4096), "整数超过资源上限"),
                Arguments.of(BigInteger.ONE.shiftLeft(4096).negate().subtract(BigInteger.ONE), "整数超过资源上限"),
                Arguments.of(nestedLists(65), "嵌套过深"),
                Arguments.of(Map.of(1, "value"), "对象键必须为字符串"),
                Arguments.of(new byte[]{1}, "不支持的类型"));
    }

    @ParameterizedTest
    @MethodSource("validResults")
    void preservesSupportedScalarResultsAndExactIntegerBoundaries(@Nullable Object value, @Nullable Object expected) {
        final DynamicPlan plan = returning(value);
        for (final boolean tracked : new boolean[]{false, true}) {
            final Map<String, Object> fields = decodeAfterEncoding(plan, tracked);
            assertTrue(fields.containsKey("result"));
            assertEquals(expected, fields.get("result"));
        }
    }

    static Stream<Arguments> validResults() {
        final BigInteger maximum = BigInteger.ONE.shiftLeft(4096).subtract(BigInteger.ONE);
        final BigInteger minimum = BigInteger.ONE.shiftLeft(4096).negate();
        return Stream.of(
                Arguments.of(null, null), Arguments.of("text", "text"), Arguments.of(true, true),
                Arguments.of((byte) 1, (byte) 1), Arguments.of((short) 2, (short) 2), Arguments.of(3, 3),
                Arguments.of(4L, 4L), Arguments.of(1.25f, 1.25f), Arguments.of(2.5d, 2.5d),
                Arguments.of(new BigDecimal("1.234"), new BigDecimal("1.234")),
                Arguments.of(BigInteger.valueOf(5), 5L),
                Arguments.of(maximum, maximum.toString()), Arguments.of(minimum, minimum.toString()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void copiesNestedResultsRecursivelyAndConvertsLargeIntegersWithoutLosingPrecision(boolean tracked) {
        final BigInteger large = new BigInteger("18446744073709551615");
        final List<Object> items = new ArrayList<>(Arrays.asList(large, Long.MAX_VALUE, null));
        final Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("items", items);
        final Map<String, Object> source = new LinkedHashMap<>();
        source.put("nested", nested);
        final Map<?, ?> result = (Map<?, ?>) decodeAfterEncoding(returning(source), tracked).get("result");
        final Map<?, ?> frozenNested = (Map<?, ?>) result.get("nested");
        final List<?> frozenItems = (List<?>) frozenNested.get("items");

        items.clear();
        nested.clear();
        source.clear();
        assertEquals(Arrays.asList("18446744073709551615", "9223372036854775807", null), frozenItems);
        assertEquals(List.of("nested"), List.copyOf(result.keySet()));
        assertThrows(UnsupportedOperationException.class, result::clear);
        assertThrows(UnsupportedOperationException.class, frozenNested::clear);
        assertThrows(UnsupportedOperationException.class, frozenItems::clear);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void acceptsExactlySixtyFourNestedContainers(boolean tracked) {
        Object value = decodeAfterEncoding(returning(nestedLists(64)), tracked).get("result");
        for (int depth = 0; depth < 64; depth++) {
            final List<?> list = assertInstanceOf(List.class, value);
            assertEquals(1, list.size());
            value = list.getFirst();
        }
        assertEquals(true, value);
    }

    @ParameterizedTest
    @MethodSource("invalidArithmetic")
    void rejectsOversizedOperandsResultsAndInvalidShiftDistances(DynamicExpression expression, String message) {
        final DynamicPlan plan = compile(expression, DynamicCompileOptions.DEFAULT);
        for (final boolean tracked : new boolean[]{false, true}) {
            assertRejected(plan, message, tracked);
        }
    }

    static Stream<Arguments> invalidArithmetic() {
        return Stream.of(
                Arguments.of(binary("add", "0".repeat(1236), 1), "整数超过资源上限"),
                Arguments.of(binary("add", "9".repeat(1235), 1), "整数超过资源上限"),
                Arguments.of(binary("add", BigInteger.ONE.shiftLeft(4096).subtract(BigInteger.ONE), 1), "整数超过资源上限"),
                Arguments.of(binary("mul", BigInteger.ONE.shiftLeft(4095), 2), "整数超过资源上限"),
                Arguments.of(binary("shl", 1, -1), "移位数量必须为 0..64"),
                Arguments.of(binary("shl", 1, 65), "移位数量必须为 0..64"),
                Arguments.of(binary("shr", 1, -1), "移位数量必须为 0..64"),
                Arguments.of(binary("shr", 1, 65), "移位数量必须为 0..64"));
    }

    @ParameterizedTest
    @CsvSource({"shl, 0, 1", "shl, 64, 18446744073709551616", "shr, 0, 1", "shr, 64, 0"})
    void acceptsShiftDistanceEndpoints(String operator, int distance, String expected) {
        final DynamicPlan plan = compile(binary(operator, 1, distance), DynamicCompileOptions.DEFAULT);
        assertEquals(expected, String.valueOf(decodeAfterEncoding(plan, false).get("result")));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void acceptsIntegerTextAtTheCharacterLimit(boolean tracked) {
        final DynamicPlan plan = compile(binary("add", "0".repeat(1235), 1), DynamicCompileOptions.DEFAULT);
        assertEquals(1L, decodeAfterEncoding(plan, tracked).get("result"));
    }

    private static DynamicExpression binary(String operator, Object left, Object right) {
        return DynamicExpression.op(operator, DynamicExpression.value(left), DynamicExpression.value(right));
    }

    private static Object nestedLists(int count) {
        Object value = true;
        for (int depth = 0; depth < count; depth++) {
            value = List.of(value);
        }
        return value;
    }

    private static DynamicPlan returning(@Nullable Object value) {
        final CustomXtreamExpression expression = mock(CustomXtreamExpression.class);
        when(expression.evaluate(any(), isNull())).thenReturn(value);
        final CustomXtreamExpressionEngine engine = mock(CustomXtreamExpressionEngine.class);
        when(engine.id()).thenReturn(() -> "fixed");
        when(engine.createExpression("result")).thenReturn(expression);
        when(engine.createEvaluationContext(null)).thenAnswer(invocation -> new AbstractXtreamEvaluationContext(null));
        final XtreamExpressionEngineRegistry registry = new XtreamExpressionEngineRegistry(false);
        registry.register(engine);
        return compile(DynamicExpression.engine("fixed", "result", Map.of()),
                new DynamicCompileOptions(DynamicSchema.ALL_VERSION, DynamicExtensionRegistry.EMPTY, registry));
    }

    private static DynamicPlan compile(DynamicExpression expression, DynamicCompileOptions options) {
        return DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("prefix", "u8").build(),
                DynamicField.field("result", "derived").expression(expression).build()), options);
    }

    private static Map<String, Object> decodeAfterEncoding(DynamicPlan plan, boolean tracked) {
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        try {
            assertEquals(8, plan.encode(DynamicValues.of(Map.of("prefix", 7)), buffer, tracked ? new CodecTracker() : null));
            assertEquals("5507", ByteBufUtil.hexDump(buffer));
            buffer.writeByte(0x66).readerIndex(1);
            final DynamicDecodeResult decoded = plan.decode(buffer, tracked ? new CodecTracker() : null);
            assertEquals(8, decoded.consumedBits());
            assertEquals(2, buffer.readerIndex());
            assertEquals(0x66, buffer.getUnsignedByte(2));
            return decoded.values().fields();
        } finally {
            buffer.release();
        }
    }

    private static void assertRejected(DynamicPlan plan, String message, boolean tracked) {
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        try {
            final CodecTracker encodeTracker = tracked ? new CodecTracker() : null;
            final DynamicCodecException encodeError = assertThrows(DynamicCodecException.class,
                    () -> plan.encode(DynamicValues.of(Map.of("prefix", 7)), buffer, encodeTracker));
            assertLocation(encodeError, message, encodeTracker);
            assertEquals(0x55, buffer.getUnsignedByte(0));

            buffer.clear().writeBytes(new byte[]{0x55, 7, 0x66}).readerIndex(1);
            final CodecTracker decodeTracker = tracked ? new CodecTracker() : null;
            final DynamicCodecException decodeError = assertThrows(DynamicCodecException.class,
                    () -> plan.decode(buffer, decodeTracker));
            assertLocation(decodeError, message, decodeTracker);
            assertEquals(1, buffer.readerIndex());
            assertEquals("0766", ByteBufUtil.hexDump(buffer));
            assertEquals(1, buffer.refCnt());
        } finally {
            buffer.release();
        }
    }

    private static void assertLocation(DynamicCodecException error, String message, @Nullable CodecTracker tracker) {
        assertEquals("$.result", error.fieldPath());
        assertEquals(8, error.bitOffset());
        assertTrue(error.getMessage().contains(message), error::getMessage);
        if (tracker != null) {
            assertTrue(tracker.getTrace().getDiagnostics().stream()
                    .anyMatch(diagnostic -> "ERROR".equals(diagnostic.level()) && Long.valueOf(8).equals(diagnostic.bitOffset())));
        }
    }
}
