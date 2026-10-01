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

import io.github.hylexus.xtream.codec.base.expression.*;
import io.github.hylexus.xtream.codec.core.dynamic.schema.*;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtensionRegistry;
import io.netty.buffer.*;
import org.openjdk.jmh.annotations.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 分开测量宿主表达式的计划编译和复用执行，包括每次求值的上下文成本。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class DynamicEngineExpressionBenchmark {
    // JMH 在执行基准前通过 @Param 和 @Setup 初始化状态；仅豁免构造期初始化检查。
    @Param({"SpEL", "Aviator", "MVEL"})
    @SuppressWarnings("NullAway.Init")
    public String engineName;
    @SuppressWarnings("NullAway.Init")
    private DynamicSchema schema;
    @SuppressWarnings("NullAway.Init")
    private DynamicCompileOptions options;
    @SuppressWarnings("NullAway.Init")
    private DynamicPlan plan;
    @SuppressWarnings("NullAway.Init")
    private ByteBuf buffer;
    @SuppressWarnings("NullAway.Init")
    private DynamicValues values;

    @Setup
    public void setup() {
        final XtreamExpressionEngine engine = switch (engineName) {
            case "SpEL" -> new SpelXtreamExpressionEngine();
            case "Aviator" -> new AviatorXtreamExpressionEngine();
            case "MVEL" -> new MvelXtreamExpressionEngine();
            default -> throw new IllegalArgumentException(engineName);
        };
        final var registry = new XtreamExpressionEngineRegistry(false);
        registry.register(engine);
        options = new DynamicCompileOptions(DynamicSchema.ALL_VERSION, DynamicExtensionRegistry.EMPTY, registry);
        schema = DynamicSchema.of(DynamicField.field("value", "u32").build(),
                DynamicField.field("sum", "derived").expression(DynamicExpression.engine(engine.id().value(),
                        ("SpEL".equals(engineName) ? "#n" : "n") + " + 2", Map.of("n", "value"))).build());
        plan = DynamicCompiler.compile(schema, options);
        values = DynamicValues.of(Map.of("value", 123456));
        buffer = Unpooled.buffer(8);
    }

    @TearDown
    public void close() {
        buffer.release();
    }

    @Benchmark
    public DynamicPlan compile() {
        return DynamicCompiler.compile(schema, options);
    }

    @Benchmark
    public long encode() {
        buffer.clear();
        return plan.encode(values, buffer);
    }

    @Benchmark
    public DynamicDecodeResult decode() {
        buffer.clear().writeInt(123456);
        return plan.decode(buffer);
    }
}
