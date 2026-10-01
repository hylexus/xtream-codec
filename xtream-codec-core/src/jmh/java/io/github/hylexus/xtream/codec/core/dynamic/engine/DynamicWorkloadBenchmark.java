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

import io.github.hylexus.xtream.codec.core.dynamic.schema.*;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.openjdk.jmh.annotations.*;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 双向工作负载基准，复用缓冲区并单独测量计划编译。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class DynamicWorkloadBenchmark {
    // JMH 在执行基准前通过 @Param 和 @Setup 初始化状态；仅豁免构造期初始化检查。
    @Param({"integer", "bits", "bytes", "string", "array", "tlv", "backfill", "expression"})
    @SuppressWarnings("NullAway.Init")
    public String workload;
    @Param({"false", "true"})
    public boolean tracked;
    @SuppressWarnings("NullAway.Init")
    private DynamicSchema schema;
    @SuppressWarnings("NullAway.Init")
    private DynamicPlan plan;
    @SuppressWarnings("NullAway.Init")
    private DynamicValues values;
    @SuppressWarnings("NullAway.Init")
    private ByteBuf buffer;
    @SuppressWarnings("NullAway.Init")
    private byte[] encoded;

    @Setup
    public void setup() {
        final DynamicField field;
        final Object value;
        switch (workload) {
            case "bytes", "string" -> {
                field = DynamicField.field("value", workload).length(DynamicLength.fixed(4096, "byte")).build();
                value = "bytes".equals(workload) ? new byte[4096] : "a".repeat(4096);
            }
            case "array" -> {
                field = DynamicField.field("value", "array").length(DynamicLength.fixed(32, "item"))
                        .item(DynamicField.field("item", "struct").fields(DynamicField.field("n", "u16").build()).build()).build();
                value = Collections.nCopies(32, Map.of("n", 1234));
            }
            case "tlv" -> {
                field = DynamicField.field("value", "tlv").length(DynamicLength.fixed(8, "item"))
                        .variants(Map.of("1", DynamicField.field("n", "u16").build())).build();
                value = Collections.nCopies(8, Map.of("key", 1, "value", 1234));
            }
            default -> {
                field = DynamicField.field("value", "u32").build();
                value = 123456;
            }
        }
        schema = switch (workload) {
            case "bits" -> DynamicSchema.of(DynamicField.field("a", "u3").build(),
                    DynamicField.field("b", "u5").build(), field);
            case "backfill" -> DynamicSchema.of(DynamicField.field("length", "u16").computedLengthOf("value").build(), field);
            case "expression" -> DynamicSchema.of(field, DynamicField.field("sum", "derived")
                    .expression(DynamicExpression.op("add", DynamicExpression.ref("value"), DynamicExpression.value(2))).build());
            default -> DynamicSchema.of(field);
        };
        values = DynamicValues.of("bits".equals(workload) ? Map.of("a", 3, "b", 17, "value", value) : Map.of("value", value));
        plan = DynamicCompiler.compile(schema);
        buffer = Unpooled.buffer(8192);
        plan.encode(values, buffer);
        encoded = new byte[buffer.readableBytes()];
        buffer.getBytes(0, encoded);
    }

    @TearDown
    public void close() {
        buffer.release();
    }

    @Benchmark
    public long encode() {
        buffer.clear();
        return plan.encode(values, buffer, tracked ? new CodecTracker() : null);
    }

    @Benchmark
    public DynamicDecodeResult decode() {
        buffer.clear().writeBytes(encoded);
        return plan.decode(buffer, tracked ? new CodecTracker() : null);
    }

    @Benchmark
    public DynamicPlan compile() {
        return DynamicCompiler.compile(schema);
    }
}
