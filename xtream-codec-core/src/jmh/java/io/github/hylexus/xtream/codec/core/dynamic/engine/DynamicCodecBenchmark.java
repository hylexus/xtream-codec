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

import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 实体、动态字节与动态位字段编码路径的基准，包含 tracker 对照。
 * 每次调用独立创建并释放 ByteBuf；动态计划在基准状态中预编译，
 * 测量的是执行开销而不是结构编译或缓冲区复用带来的差异。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
@State(Scope.Thread)
public class DynamicCodecBenchmark {
    // 同一负载分别经过旧实体路径和新动态路径，观察字段 Trace 的相对成本。
    private final EntityCodec entityCodec = EntityCodec.DEFAULT;
    private final Sample entity = new Sample(0x1234, 17);
    private final DynamicPlan bytes = DynamicCompiler.compile(DynamicSchema.of(
            DynamicField.field("sequence", "u16").build(),
            DynamicField.field("flags", "u8").build()));
    private final DynamicValues byteValues = DynamicValues.of(Map.of("sequence", 0x1234, "flags", 17));
    private final DynamicPlan bits = DynamicCompiler.compile(DynamicSchema.of(
            DynamicField.field("version", "u3").build(),
            DynamicField.field("flags", "u5").build(),
            DynamicField.field("sequence", "u16").build()));
    private final DynamicValues bitValues = DynamicValues.of(Map.of("version", 5, "flags", 17, "sequence", 0x1234));

    @Benchmark
    public int entity() {
        final ByteBuf buffer = Unpooled.buffer(16);
        try {
            entityCodec.encode(entity, buffer);
            return buffer.writerIndex();
        } finally {
            buffer.release();
        }
    }

    @Benchmark
    public int entityTracked() {
        final ByteBuf buffer = Unpooled.buffer(16);
        try {
            entityCodec.encode(entity, buffer, new CodecTracker());
            return buffer.writerIndex();
        } finally {
            buffer.release();
        }
    }

    @Benchmark
    public long dynamicBytes() {
        return encode(bytes, byteValues, false);
    }

    @Benchmark
    public long dynamicBytesTracked() {
        return encode(bytes, byteValues, true);
    }

    @Benchmark
    public long dynamicBits() {
        return encode(bits, bitValues, false);
    }

    @Benchmark
    public long dynamicBitsTracked() {
        return encode(bits, bitValues, true);
    }

    private static long encode(DynamicPlan plan, DynamicValues values, boolean tracked) {
        final ByteBuf buffer = Unpooled.buffer(16);
        try {
            return tracked ? plan.encode(values, buffer, new CodecTracker()) : plan.encode(values, buffer);
        } finally {
            buffer.release();
        }
    }

    public record Sample(@Preset.RustStyle.u16 int sequence, @Preset.RustStyle.u8 int flags) {
    }
}
