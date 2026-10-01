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
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchemaJson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 动态结构文档的可执行示例。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
@ReferencedByDocs("guide/core/dynamic/README.md")
class DynamicQuickStartTest {
    @Test
    void shouldRoundTripImportedSchema() {
        final DynamicSchema schema = DynamicSchema.of(
                DynamicField.field("version", "u3").build(),
                DynamicField.field("flags", "u5").build(),
                DynamicField.field("sequence", "u16").endian("little").build());
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchemaJson.read(DynamicSchemaJson.write(schema)));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("version", 5, "flags", 17, "sequence", 4660)), buffer);
            assertEquals("b13412", ByteBufUtil.hexDump(buffer));
            final DynamicDecodeResult result = plan.decode(buffer);
            assertEquals(Map.of("version", 5L, "flags", 17L, "sequence", 4660L), result.values().fields());
            assertEquals(24, result.consumedBits());
            assertEquals(0, result.remainingBytes());
        } finally {
            buffer.release();
        }
    }
}
