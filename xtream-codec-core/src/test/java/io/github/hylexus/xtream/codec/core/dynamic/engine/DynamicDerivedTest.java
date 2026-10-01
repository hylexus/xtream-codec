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

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 派生位段、源整数和反向编码测试。
 * 验证派生节点不重复消费报文，并拒绝源整数与用户填写位段相互矛盾的输入。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicDerivedTest {
    @Test
    void shouldKeepUnsigned64BitDerivedValueAsDecimalString() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("source", "u64").build(),
                DynamicField.field("view", "derived").ref("source")
                        .options(Map.of("bitOffset", 0, "bitWidth", 64)).build()));
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{
                -1, -1, -1, -1, -1, -1, -1, -1});
        try {
            final Map<String, Object> expected = Map.of(
                    "source", "18446744073709551615",
                    "view", "18446744073709551615");
            assertEquals(expected, plan.decode(buffer).values().fields());
        } finally {
            buffer.release();
        }
        final ByteBuf encoded = Unpooled.buffer();
        try {
            // 只提供派生视图时也应无损合成完整 u64。
            plan.encode(DynamicValues.of(Map.of("view", "18446744073709551615")), encoded);
            assertEquals("ffffffffffffffff", ByteBufUtil.hexDump(encoded));
            assertEquals("18446744073709551615", plan.decode(encoded).values().fields().get("view"));
        } finally {
            encoded.release();
        }
    }

    @Test
    void shouldReadViewsWithoutConsumingAndComposeSourceDuringEncoding() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("source", "u8").build(),
                DynamicField.field("low", "derived").ref("source").options(Map.of("bitOffset", 0, "bitWidth", 5)).build(),
                DynamicField.field("high", "derived").ref("source").options(Map.of("bitOffset", 5, "bitWidth", 3)).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("low", 17, "high", 5)), buffer);
            assertEquals("b1", ByteBufUtil.hexDump(buffer));
            final CodecTracker tracker = new CodecTracker();
            final var result = plan.decode(buffer, tracker);
            assertEquals(8, result.consumedBits());
            assertEquals(Map.of("source", 177L, "low", 17L, "high", 5L), result.values().fields());
            final var nodes = tracker.toTraceView().root().children();
            assertEquals(3L, nodes.get(1).bitStart());
            assertEquals(8L, nodes.get(1).bitEnd());
            assertEquals(0L, nodes.get(2).bitStart());
            assertEquals(3L, nodes.get(2).bitEnd());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldRejectConflictingSourceAndView() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("source", "u8").build(),
                DynamicField.field("low", "derived").ref("source").options(Map.of("bitOffset", 0, "bitWidth", 3)).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            assertThrows(DynamicCodecException.class,
                    () -> plan.encode(DynamicValues.of(Map.of("source", 1, "low", 2)), buffer));
        } finally {
            buffer.release();
        }
    }
}
