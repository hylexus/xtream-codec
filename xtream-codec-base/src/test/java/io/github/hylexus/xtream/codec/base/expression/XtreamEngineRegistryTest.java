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

package io.github.hylexus.xtream.codec.base.expression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XtreamEngineRegistryTest {

    private XtreamEngineRegistry registry;

    @BeforeEach
    void setUp() {
        // 不自动注册默认引擎
        registry = new XtreamEngineRegistry(false);
    }

    @Test
    void testConstructorWithDefaults() {
        final XtreamEngineRegistry defaultRegistry = new XtreamEngineRegistry(true);

        // 验证默认引擎是否已注册
        assertNotNull(defaultRegistry.getEngine(XtreamExpressionEngine.EngineId.SpEL));
        assertNotNull(defaultRegistry.getEngine(XtreamExpressionEngine.EngineId.AVIATOR));
        assertNotNull(defaultRegistry.getEngine(XtreamExpressionEngine.EngineId.MVEL));
    }

    @Test
    void testRegisterEngine() {
        SpelXtreamExpressionEngine engine = new SpelXtreamExpressionEngine();
        registry.register(engine);

        XtreamExpressionEngine retrieved = registry.getEngine(XtreamExpressionEngine.EngineId.SpEL);
        assertSame(engine, retrieved);
    }

    @Test
    void testGetEngineNotFound() {
        assertThrows(IllegalArgumentException.class, () -> registry.getEngine(() -> "not-exists"));
    }

    @Test
    void testUnregisterEngine() {
        final SpelXtreamExpressionEngine engine = new SpelXtreamExpressionEngine();
        registry.register(engine);

        // 确认引擎已注册
        assertNotNull(registry.getEngine(XtreamExpressionEngine.EngineId.SpEL));

        // 注销引擎
        registry.unregister(XtreamExpressionEngine.EngineId.SpEL);

        // 确认引擎已被注销
        assertThrows(IllegalArgumentException.class, () -> registry.getEngine(XtreamExpressionEngine.EngineId.SpEL));
    }

}
