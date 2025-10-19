/*
 * Copyright 2024 the original author or authors.
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

package io.github.hylexus.xtream.codec.common.bean;

import org.openjdk.jmh.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * PropertySetter 各种实现的性能对比
 *
 * @author hylexus
 * @see PropertyGetters
 */
// 平均耗时
@BenchmarkMode(Mode.AverageTime)
// 结果输出的时间单位
@OutputTimeUnit(TimeUnit.NANOSECONDS)
// 预热阶段：预热 3 轮，每轮持续 1 秒
@Warmup(iterations = 3, time = 1)
// 正式测量阶段：测量 5 轮，每轮持续 1 秒
@Measurement(iterations = 5, time = 1)
// fork 1 个独立 JVM 进行基准测试
@Fork(1)
// 测试类实例在整个 benchmark 过程中共享
// @State(Scope.Benchmark)
@State(Scope.Thread)
@SuppressWarnings({"unused", "NullAway"})
public class PropertySetterBenchmark {
    Person beanInstance;

    // getter 实例
    BeanPropertyMetadata.PropertySetter forMethodViaReflectionInstance;
    BeanPropertyMetadata.PropertySetter forFieldViaReflectionInstance;
    BeanPropertyMetadata.PropertySetter forMethodViaMethodHandleInstance;
    BeanPropertyMetadata.PropertySetter forFieldViaMethodHandleInstance;
    BeanPropertyMetadata.PropertySetter forMethodViaLambdaMetaFactoryInstance;
    BeanPropertyMetadata.PropertySetter forFieldViaVarHandleInstance;
    BeanPropertyMetadata beanPropertyMetadata = null;

    @Setup(Level.Iteration)
    public void setup() throws Exception {
        beanInstance = new Person(null, 100000);
        final Method setName = Person.class.getMethod("setName", String.class);

        final Field nameField = Person.class.getDeclaredField("name");

        // 1. Reflection
        forMethodViaReflectionInstance = PropertySetters.forMethodViaReflection(setName);
        forFieldViaReflectionInstance = PropertySetters.forFieldViaReflection(nameField);

        // 2. MethodHandle
        forMethodViaMethodHandleInstance = PropertySetters.forMethodViaMethodHandle(setName);
        forFieldViaMethodHandleInstance = PropertySetters.forFieldViaMethodHandle(nameField);

        // 3. LambdaMetaFactory (方法)
        forMethodViaLambdaMetaFactoryInstance = PropertySetters.forMethodViaLambdaMetaFactory(setName);

        // 4. VarHandle (字段)
        forFieldViaVarHandleInstance = PropertySetters.forFieldViaVarHandle(nameField);
    }

    @Benchmark
    public void forFieldViaReflection() {
        forFieldViaReflectionInstance.setProperty(beanPropertyMetadata, beanInstance, "test");
    }

    @Benchmark
    public void forMethodViaReflection() {
        forMethodViaReflectionInstance.setProperty(beanPropertyMetadata, beanInstance, "test");
    }

    @Benchmark
    public void forMethodViaMethodHandle() {
        forMethodViaMethodHandleInstance.setProperty(beanPropertyMetadata, beanInstance, "test");
    }

    @Benchmark
    public void forFieldViaMethodHandle() {
        forFieldViaMethodHandleInstance.setProperty(beanPropertyMetadata, beanInstance, "test");
    }

    @Benchmark
    public void forMethodViaLambdaMetaFactory() {
        forMethodViaLambdaMetaFactoryInstance.setProperty(beanPropertyMetadata, beanInstance, "test");
    }

    @Benchmark
    public void forFieldViaVarHandle() {
        forFieldViaVarHandleInstance.setProperty(beanPropertyMetadata, beanInstance, "test");
    }

}
