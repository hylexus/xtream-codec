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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AbstractXtreamEvaluationContextTest {

    @Test
    void testRootObjectSetViaConstructor() {
        AbstractXtreamEvaluationContext ctx = new AbstractXtreamEvaluationContext("rootValue");
        Map<String, Object> vars = ctx.getVariables();
        assertEquals("rootValue", vars.get(XtreamEvaluationContext.ROOT_OBJECT_KEY));
    }

    @Test
    void testNullRootObjectNotInVariables() {
        AbstractXtreamEvaluationContext ctx = new AbstractXtreamEvaluationContext(null);
        Map<String, Object> vars = ctx.getVariables();
        assertFalse(vars.containsKey(XtreamEvaluationContext.ROOT_OBJECT_KEY));
    }

    @Test
    void testSetAndGetVariable() {
        AbstractXtreamEvaluationContext ctx = new AbstractXtreamEvaluationContext(null);
        ctx.setVariable("key1", "value1");
        ctx.setVariable("key2", 42);
        assertEquals("value1", ctx.getVariables().get("key1"));
        assertEquals(42, ctx.getVariables().get("key2"));
    }

    @Test
    void testSetVariableNullRemoves() {
        AbstractXtreamEvaluationContext ctx = new AbstractXtreamEvaluationContext(null);
        ctx.setVariable("key", "value");
        assertTrue(ctx.getVariables().containsKey("key"));

        ctx.setVariable("key", null);
        assertFalse(ctx.getVariables().containsKey("key"));
    }

    @Test
    void testSetVariableOverwrite() {
        AbstractXtreamEvaluationContext ctx = new AbstractXtreamEvaluationContext(null);
        ctx.setVariable("key", "old");
        ctx.setVariable("key", "new");
        assertEquals("new", ctx.getVariables().get("key"));
    }

    @Test
    void testGetVariablesReturnsLiveMap() {
        AbstractXtreamEvaluationContext ctx = new AbstractXtreamEvaluationContext(null);
        ctx.setVariable("a", 1);
        Map<String, Object> vars = ctx.getVariables();
        ctx.setVariable("b", 2);
        // The returned map should reflect the new variable
        assertTrue(vars.containsKey("b"));
    }
}
