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

import io.github.hylexus.xtream.codec.base.annotation.ReferencedByDocs;
import io.github.hylexus.xtream.codec.base.expression.*;
import io.github.hylexus.xtream.codec.core.dynamic.schema.*;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtensionRegistry;
import io.netty.buffer.*;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 宿主显式启用 SpEL，并在定义中绑定先前的字段。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
@ReferencedByDocs("guide/core/dynamic/README.md")
class DynamicExpressionQuickStartTest {
    @Test
    void compileOnceAndExecuteBothDirections() {
        final var engines = new XtreamExpressionEngineRegistry(false);
        engines.register(new SpelXtreamExpressionEngine());
        final var options = new DynamicCompileOptions(DynamicSchema.ALL_VERSION, DynamicExtensionRegistry.EMPTY, engines);
        final var schema = DynamicSchemaJson.read("""
                {"schemaVersion":1,"fields":[
                  {"id":"count","type":"u8"},
                  {"id":"payload","type":"hex",
                   "when":{"expression":{"engine":"SpEL","source":"#n > 0","bindings":{"n":"count"}}},
                   "length":{"unit":"byte","expression":{"engine":"SpEL","source":"#n","bindings":{"n":"count"}}}},
                  {"id":"sum","type":"derived",
                   "expression":{"engine":"SpEL","source":"#n + 2","bindings":{"n":"count"}}}
                ]}
                """);
        final DynamicPlan plan = DynamicCompiler.compile(schema, options);
        final ByteBuf buffer = Unpooled.buffer();
        try {
            assertEquals(24, plan.encode(DynamicValues.of(Map.of("count", 2, "payload", "aabb")), buffer));
            assertEquals("02aabb", ByteBufUtil.hexDump(buffer));
            final var decoded = plan.decode(buffer);
            assertEquals(Map.of("count", 2L, "payload", "aabb", "sum", 4L), decoded.values().fields());
            assertEquals(3, decoded.consumedBytes());
        } finally {
            buffer.release();
        }
    }
}
