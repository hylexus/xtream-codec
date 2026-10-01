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
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 单次执行状态隔离测试。多个线程共用计划，分别使用独立缓冲区和值，
 * 同时验证资源超限和截断失败后缓冲区所有权及调用方读取位置不变。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicExecutionTest {
    private static final DynamicPlan PLAN = DynamicCompiler.compile(DynamicSchema.of(
            DynamicField.field("value", "u16").build()));

    @Test
    void shouldKeepConcurrentValuesAndPositionsIsolated() throws Exception {
        // 同一计划被反复并发执行，前置与尾随字节用来验证相对起点和剩余输入。
        try (var executor = Executors.newFixedThreadPool(8)) {
            final var calls = new ArrayList<Callable<Void>>();
            for (int i = 0; i < 200; i++) {
                final int value = i;
                calls.add(() -> {
                    final ByteBuf buffer = Unpooled.buffer();
                    try {
                        buffer.writeByte(0x7f);
                        assertEquals(16, PLAN.encode(DynamicValues.of(Map.of("value", value)), buffer));
                        buffer.readerIndex(1);
                        buffer.writeByte(0x55);
                        final DynamicDecodeResult result = PLAN.decode(buffer);
                        assertEquals((long) value, result.values().fields().get("value"));
                        assertEquals(16, result.consumedBits());
                        assertEquals(2, result.consumedBytes());
                        assertEquals(1, result.remainingBytes());
                        assertEquals(0x55, buffer.readUnsignedByte());
                        assertEquals(1, buffer.refCnt());
                    } finally {
                        buffer.release();
                    }
                    return null;
                });
            }
            for (var result : executor.invokeAll(calls)) {
                result.get();
            }
        }
    }

    @Test
    void shouldEnforceLimitsAndKeepCallerOwnershipOnFailure() {
        final DynamicLimits limits = new DynamicLimits(1, 1, 10, 5);
        final ByteBuf buffer = Unpooled.buffer();
        try {
            assertThrows(DynamicCodecException.class,
                    () -> PLAN.encode(DynamicValues.of(Map.of("value", 12)), buffer, limits));
            assertEquals(1, buffer.refCnt());
            assertEquals(0, buffer.writerIndex());
            buffer.writeShort(12);
            assertThrows(DynamicCodecException.class, () -> PLAN.decode(buffer, limits));
            assertEquals(1, buffer.refCnt());
            assertEquals(0, buffer.readerIndex());
            assertEquals(12L, PLAN.decode(buffer).values().fields().get("value"));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldReportTruncationAtFieldStartWithoutReleasingInput() {
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{1});
        try {
            final DynamicCodecException error = assertThrows(DynamicCodecException.class, () -> PLAN.decode(buffer));
            assertEquals("$.value", error.fieldPath());
            assertEquals(0, error.bitOffset());
            assertEquals(1, buffer.refCnt());
        } finally {
            buffer.release();
        }
    }
}
