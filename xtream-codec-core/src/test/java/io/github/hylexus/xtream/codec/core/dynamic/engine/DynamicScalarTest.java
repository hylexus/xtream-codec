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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 标量范围和线缆字节布局测试。
 * 使用整数边界值及明确的 HEX 断言检查符号、位宽、字节序和浮点表示。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicScalarTest {
    @ParameterizedTest
    @ValueSource(ints = {8, 16, 24, 32, 40, 48, 56, 64})
    void shouldRoundTripIntegerBoundaries(int bits) {
        for (final String endian : new String[]{"big", "little"}) {
            for (final boolean signed : new boolean[]{false, true}) {
                final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                        DynamicField.field("value", (signed ? "i" : "u") + bits).endian(endian).build()));
                final BigInteger min = signed ? BigInteger.ONE.shiftLeft(bits - 1).negate() : BigInteger.ZERO;
                final BigInteger max = BigInteger.ONE.shiftLeft(signed ? bits - 1 : bits).subtract(BigInteger.ONE);
                for (final BigInteger number : new BigInteger[]{min, max, BigInteger.ZERO}) {
                    final ByteBuf buffer = Unpooled.buffer();
                    try {
                        plan.encode(DynamicValues.of(Map.of("value", number.toString())), buffer);
                        assertEquals(bits / 8, buffer.readableBytes());
                        final Object decoded = plan.decode(buffer).values().fields().get("value");
                        assertEquals(number, new BigInteger(decoded.toString()));
                        if (number.abs().bitLength() > 53) {
                            assertInstanceOf(String.class, decoded);
                        }
                    } finally {
                        buffer.release();
                    }
                }
                final ByteBuf buffer = Unpooled.buffer();
                try {
                    assertThrows(DynamicCodecException.class,
                            () -> plan.encode(DynamicValues.of(Map.of("value", min.subtract(BigInteger.ONE))), buffer));
                    assertThrows(DynamicCodecException.class,
                            () -> plan.encode(DynamicValues.of(Map.of("value", max.add(BigInteger.ONE))), buffer));
                } finally {
                    buffer.release();
                }
            }
        }
    }

    @Test
    void shouldUseExpectedIntegerAndFloatByteOrder() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("big", "u16").build(),
                DynamicField.field("little", "u16").endian("little").build(),
                DynamicField.field("float", "float").bits(32).build(),
                DynamicField.field("double", "float").bits(64).endian("little").build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("big", 0x1234, "little", 0x1234, "float", 1.5, "double", -2.25)), buffer);
            assertEquals("123434123fc0000000000000000002c0", ByteBufUtil.hexDump(buffer));
            final Map<String, Object> values = plan.decode(buffer).values().fields();
            assertEquals(1.5, values.get("float"));
            assertEquals(-2.25, values.get("double"));
        } finally {
            buffer.release();
        }
    }
}
