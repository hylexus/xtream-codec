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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XtreamExpressionEngineTest {

    static XtreamExpressionEngineRegistry registry;

    @BeforeAll
    static void init() {
        registry = new XtreamExpressionEngineRegistry();
    }

    @Test
    void testSpel() {
        final XtreamExpressionEngine spel = registry.getEngine(XtreamExpressionEngine.XtreamExpressionEngineId.SPEL);
        final XtreamEvaluationContext ctx = spel.createEvaluationContext(100);
        ctx.setVariable("a", 200);
        assertEquals(101, doEvaluate(spel.createExpression("#self + 1"), ctx, Integer.class));
        assertEquals(201, doEvaluate(spel.createExpression("#a + 1"), ctx, Integer.class));
        assertEquals(201L, doEvaluate(spel.createExpression("#a + 1"), ctx, Long.class));
        assertNull(doEvaluate(spel.createExpression("#a > 0 ? null : 111"), ctx, Object.class));
    }

    @Test
    void testAviator() {
        final XtreamExpressionEngine aviator = registry.getEngine(XtreamExpressionEngine.XtreamExpressionEngineId.AVIATOR);
        final XtreamEvaluationContext ctx = aviator.createEvaluationContext(100);
        ctx.setVariable("a", 200);
        assertEquals(101, doEvaluate(aviator.createExpression("self + 1"), ctx, Number.class).intValue());
        assertEquals(201, doEvaluate(aviator.createExpression("a + 1"), ctx, Number.class).intValue());
        assertEquals(201L, doEvaluate(aviator.createExpression("a + 1"), ctx, Long.class));
        // 注意这里是 nil 不是 null
        assertNull(doEvaluate(aviator.createExpression("a > 0 ? nil : 111"), ctx, Object.class));
    }

    @Test
    void testMvelVariables() {
        final XtreamExpressionEngine mvel = registry.getEngine(XtreamExpressionEngine.XtreamExpressionEngineId.MVEL);
        final XtreamEvaluationContext ctx = mvel.createEvaluationContext(100);
        ctx.setVariable("a", 200);
        assertEquals(101, doEvaluate(mvel.createExpression("self + 1"), ctx, Integer.class));
        assertEquals(201, doEvaluate(mvel.createExpression("a + 1"), ctx, Integer.class));
        assertEquals(201L, doEvaluate(mvel.createExpression("a + 1"), ctx, Long.class));
        assertNull(doEvaluate(mvel.createExpression("a > 0 ? null : 111"), ctx, Object.class));
    }

    private <T> T doEvaluate(XtreamExpression expression, XtreamEvaluationContext context, Class<T> expectedType) {
        return expression.evaluate(context, expectedType);
    }
}
