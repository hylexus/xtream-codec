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

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicCondition;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 条件、版本和判别值分支的布局测试。
 * 同一结构分别编码和解码，确认条件字段与 choice 分支使用一致的前序值。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicChoiceTest {
    @Test
    void shouldUseVersionAndDiscriminatorInBothDirections() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("version", "u8").build(),
                DynamicField.field("kind", "u8").build(),
                DynamicField.field("legacy", "u8")
                        .when(new DynamicCondition("version", "lt", "2")).build(),
                DynamicField.field("modern", "u16")
                        .when(new DynamicCondition("version", "gte", "2")).build(),
                DynamicField.field("body", "choice").ref("kind").variants(Map.of(
                        "1", DynamicField.field("small", "u8").build(),
                        "2", DynamicField.field("large", "u16").build())).build()));
        for (int version : new int[]{1, 2}) {
            final ByteBuf buffer = Unpooled.buffer();
            try {
                final Map<String, Object> values = version == 1
                        ? Map.of("version", 1, "kind", 1, "legacy", 7, "body", 3)
                        : Map.of("version", 2, "kind", 2, "modern", 0x1234, "body", 0x5678);
                plan.encode(DynamicValues.of(values), buffer);
                assertEquals(version == 1 ? "01010703" : "020212345678", ByteBufUtil.hexDump(buffer));
                final Map<String, Object> decoded = plan.decode(buffer).values().fields();
                assertEquals(version == 1, decoded.containsKey("legacy"));
                assertEquals(version == 2, decoded.containsKey("modern"));
                assertEquals(version == 1 ? 3L : 0x5678L, decoded.get("body"));
            } finally {
                buffer.release();
            }
        }
    }

    @Test
    void shouldApplyBitMaskCondition() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("flags", "u8").build(),
                DynamicField.field("optional", "u8")
                        .when(new DynamicCondition("flags", "bitSet", "4")).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("flags", 4, "optional", 9)), buffer);
            assertEquals("0409", ByteBufUtil.hexDump(buffer));
            assertEquals(9L, plan.decode(buffer).values().fields().get("optional"));
        } finally {
            buffer.release();
        }
    }
}
