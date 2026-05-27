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

package io.github.hylexus.xtream.codec.base.expression;


import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
// 预热阶段：预热 3 轮，每轮持续 1 秒
@Warmup(iterations = 3, time = 1)
// 正式测量阶段：测量 5 轮，每轮持续 1 秒
@Measurement(iterations = 5, time = 1)
// fork 1 个独立 JVM 进行基准测试
@Fork(1)
// 测试类实例在整个 benchmark 过程中共享
// @State(Scope.Benchmark)
@State(Scope.Thread)
@SuppressWarnings("all")
public class XtreamExpressionEngineBenchmark {

    // Engines
    XtreamExpressionEngine spelEngine;
    XtreamExpressionEngine aviatorEngine;
    XtreamExpressionEngine mvelEngine;

    // Expressions
    XtreamExpression spelExpr;
    XtreamExpression aviatorExpr;
    XtreamExpression mvelExpr;

    // Contexts
    XtreamEvaluationContext spelCtx;
    XtreamEvaluationContext aviatorCtx;
    XtreamEvaluationContext mvelCtx;


    @Setup(Level.Iteration)
    public void setup() {
        spelEngine = new SpelXtreamExpressionEngine();
        aviatorEngine = new AviatorXtreamExpressionEngine();
        mvelEngine = new MvelXtreamExpressionEngine();

        spelExpr = spelEngine.createExpression("#self + 100");
        aviatorExpr = aviatorEngine.createExpression("self + 100");
        mvelExpr = mvelEngine.createExpression("self + 100");

        spelCtx = spelEngine.createEvaluationContext(0);
        aviatorCtx = aviatorEngine.createEvaluationContext(0);
        mvelCtx = mvelEngine.createEvaluationContext(0);
    }

    @Benchmark
    public int spel() {
        int sum = 0;
        for (int i = 0; i < 1000; i++) {
            sum += spelExpr.evaluate(spelCtx, Integer.class).intValue();
        }
        return sum;
    }

    @Benchmark
    public int aviator() {
        int sum = 0;
        for (int i = 0; i < 1000; i++) {
            sum += aviatorExpr.evaluate(aviatorCtx, Number.class).intValue();
        }
        return sum;
    }

    @Benchmark
    public int mvel() {
        int sum = 0;
        for (int i = 0; i < 1000; i++) {
            sum += mvelExpr.evaluate(mvelCtx, Number.class).intValue();
        }
        return sum;
    }

}
