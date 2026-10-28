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

package io.github.hylexus.xtream.codec.common.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class XtreamBitOperatorConditionalTest {

    static Stream<Arguments> conditions() {
        final List<Operation> operations = List.of(
                new Operation("set", 0, Long.MIN_VALUE,
                        (op, condition) -> op.setIf(63, condition),
                        (op, condition) -> op.setIf(63, condition),
                        (op, condition) -> op.setIf(63, condition)),
                new Operation("reset", Long.MIN_VALUE, 0,
                        (op, condition) -> op.resetIf(63, condition),
                        (op, condition) -> op.resetIf(63, condition),
                        (op, condition) -> op.resetIf(63, condition)),
                new Operation("setRange", 0, 0xe000000000000000L,
                        (op, condition) -> op.setRangeIf(61, 3, condition),
                        (op, condition) -> op.setRangeIf(61, 3, condition),
                        (op, condition) -> op.setRangeIf(61, 3, condition)),
                new Operation("resetRange", -1, 0x1fffffffffffffffL,
                        (op, condition) -> op.resetRangeIf(61, 3, condition),
                        (op, condition) -> op.resetRangeIf(61, 3, condition),
                        (op, condition) -> op.resetRangeIf(61, 3, condition)),
                new Operation("setWithStatusTrue", 1, Long.MIN_VALUE + 1,
                        (op, condition) -> op.setWithStatusIf(63, true, condition),
                        (op, condition) -> op.setWithStatusIf(63, true, condition),
                        (op, condition) -> op.setWithStatusIf(63, true, condition)),
                new Operation("setWithStatusFalse", Long.MIN_VALUE + 1, 1,
                        (op, condition) -> op.setWithStatusIf(63, false, condition),
                        (op, condition) -> op.setWithStatusIf(63, false, condition),
                        (op, condition) -> op.setWithStatusIf(63, false, condition)));
        return operations.stream().flatMap(operation -> Stream.of(false, true).flatMap(condition ->
                Stream.of(false, true).map(mutable -> Arguments.of(operation, condition, mutable))));
    }

    @ParameterizedTest(name = "{0}, condition={1}, mutable={2}")
    @MethodSource("conditions")
    void booleanConditionControlsMutation(Operation operation, boolean condition, boolean mutable) {
        final XtreamBitOperator original = operator(mutable, operation.initial());
        final XtreamBitOperator result = operation.booleanForm().apply(original, condition);
        assertResult(operation, condition, mutable, original, result);
    }

    @ParameterizedTest(name = "{0}, condition={1}, mutable={2}")
    @MethodSource("conditions")
    void supplierIsEvaluatedOnceAndControlsMutation(Operation operation, boolean condition, boolean mutable) {
        final AtomicInteger calls = new AtomicInteger();
        final XtreamBitOperator original = operator(mutable, operation.initial());
        final XtreamBitOperator result = operation.supplierForm().apply(original, () -> {
            calls.incrementAndGet();
            return condition;
        });
        assertResult(operation, condition, mutable, original, result);
        assertThat(result.value()).isEqualTo(condition ? operation.changed() : operation.initial());
        assertThat(calls.get()).isEqualTo(1);
    }

    @ParameterizedTest(name = "{0}, condition={1}, mutable={2}")
    @MethodSource("conditions")
    void predicateReceivesCurrentValueOnceAndControlsMutation(Operation operation, boolean condition, boolean mutable) {
        final AtomicInteger calls = new AtomicInteger();
        final XtreamBitOperator original = operator(mutable, operation.initial());
        final XtreamBitOperator result = operation.predicateForm().apply(original, value -> {
            calls.incrementAndGet();
            assertThat(value).isEqualTo(operation.initial());
            return condition;
        });
        assertResult(operation, condition, mutable, original, result);
        assertThat(result.value()).isEqualTo(condition ? operation.changed() : operation.initial());
        assertThat(calls.get()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void fullRangeIncludesHighestBitAndPreservesUnsignedValues(boolean mutable) {
        final XtreamBitOperator original = operator(mutable, 0);
        final XtreamBitOperator all = original.setRange(0, 64);
        assertThat(all.value()).isEqualTo(-1L);
        assertThat(all.get(63)).isEqualTo(1);
        assertThat(all.rangedLongValue(63, 1)).isEqualTo(1);
        assertThat(all.rangedLongValue(0, 64)).isEqualTo(-1L);
        assertThat(all.rangedUnsignedIntValue(0, 32)).isEqualTo(4294967295L);
        assertThat(all.rangedUnsignedLongValue(0, 64)).isEqualTo(new BigInteger("18446744073709551615"));
        assertThat(original.value()).isEqualTo(mutable ? -1L : 0L);
        final XtreamBitOperator empty = all.resetRange(0, 64);
        assertThat(empty.value()).isZero();
        assertThat(all.value()).isEqualTo(mutable ? 0L : -1L);
        if (mutable) {
            assertThat(empty).isSameAs(original);
        } else {
            assertThat(all).isNotSameAs(original);
            assertThat(empty).isNotSameAs(all);
        }
    }

    @ParameterizedTest
    @CsvSource({"false, -1", "true, -1", "false, 64", "true, 64"})
    void invalidBitOffsetsFailWithoutMutation(boolean mutable, int offset) {
        final XtreamBitOperator original = operator(mutable, 0x55);
        assertThatThrownBy(() -> original.set(offset)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> original.reset(offset)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> original.setWithStatus(offset, true)).isInstanceOf(IllegalArgumentException.class);
        assertThat(original.value()).isEqualTo(0x55);
    }

    @ParameterizedTest
    @CsvSource({"false, 0, 0", "true, 0, 0", "false, 0, -1", "true, 0, -1",
            "false, 63, 2", "true, 63, 2", "false, 64, 1", "true, 64, 1", "false, -1, 1", "true, -1, 1"})
    void invalidRangesFailWithoutMutation(boolean mutable, int offset, int length) {
        final XtreamBitOperator original = operator(mutable, 0x55);
        assertThatThrownBy(() -> original.setRange(offset, length)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> original.resetRange(offset, length)).isInstanceOf(IllegalArgumentException.class);
        assertThat(original.value()).isEqualTo(0x55);
    }

    private XtreamBitOperator operator(boolean mutable, long value) {
        return mutable ? XtreamBitOperator.mutable(value) : XtreamBitOperator.immutable(value);
    }

    private void assertResult(Operation operation, boolean condition, boolean mutable, XtreamBitOperator original, XtreamBitOperator result) {
        assertThat(result.value()).isEqualTo(condition ? operation.changed() : operation.initial());
        assertThat(original.value()).isEqualTo(condition && mutable ? operation.changed() : operation.initial());
        if (mutable || !condition) {
            assertThat(result).isSameAs(original);
        } else {
            assertThat(result).isNotSameAs(original);
        }
    }

    record Operation(
            String name,
            long initial,
            long changed,
            BiFunction<XtreamBitOperator, Boolean, XtreamBitOperator> booleanForm,
            BiFunction<XtreamBitOperator, Supplier<Boolean>, XtreamBitOperator> supplierForm,
            BiFunction<XtreamBitOperator, Predicate<Long>, XtreamBitOperator> predicateForm) {

        @Override
        public String toString() {
            return name;
        }
    }
}
