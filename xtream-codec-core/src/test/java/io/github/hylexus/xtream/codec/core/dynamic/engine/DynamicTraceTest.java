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
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 位范围、字节范围和失败诊断合同测试。
 * 比较启用与关闭 tracker 的字节和值，确认跟踪不会改变执行结果且保留部分失败节点。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicTraceTest {
    private static final DynamicPlan PLAN = DynamicCompiler.compile(DynamicSchema.of(
            DynamicField.field("a", "u3").build(),
            DynamicField.field("b", "u5").build(),
            DynamicField.field("c", "u12").build(),
            DynamicField.field("align", "align").alignTo(8).build()));

    @Test
    void shouldKeepTrackedAndUntrackedBytesAndValuesIdentical() {
        final ByteBuf production = Unpooled.buffer();
        final ByteBuf tracked = Unpooled.buffer();
        try {
            final DynamicValues values = DynamicValues.of(Map.of("a", 5, "b", 17, "c", 0xabc));
            final CodecTracker tracker = new CodecTracker();
            PLAN.encode(values, production);
            PLAN.encode(values, tracked, tracker);
            assertEquals(ByteBufUtil.hexDump(production), ByteBufUtil.hexDump(tracked));
            final var nodes = tracker.toTraceView().root().children();
            assertEquals(0L, nodes.get(0).bitStart());
            assertEquals(3L, nodes.get(0).bitEnd());
            assertEquals(3L, nodes.get(1).bitStart());
            assertEquals(8L, nodes.get(1).bitEnd());
            assertEquals(0, nodes.get(0).byteStart());
            assertEquals(1, nodes.get(0).byteEnd());
            assertEquals(0, nodes.get(1).byteStart());
            assertEquals(1, nodes.get(1).byteEnd());
            assertEquals(1, nodes.get(2).byteStart());
            assertEquals(3, nodes.get(2).byteEnd());
            assertTrue(tracker.toTraceView().nodeIdsByByteOffset().get(0).contains(nodes.get(0).id()));
            assertTrue(tracker.toTraceView().nodeIdsByByteOffset().get(0).contains(nodes.get(1).id()));
            assertEquals(PLAN.decode(production), PLAN.decode(tracked, new CodecTracker()));
        } finally {
            production.release();
            tracked.release();
        }
    }

    @Test
    void shouldPreserveCompletedNodesAndBitFailureOffset() {
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{(byte) 0xb1, (byte) 0xab});
        try {
            final CodecTracker tracker = new CodecTracker();
            final DynamicCodecException error = assertThrows(DynamicCodecException.class, () -> PLAN.decode(buffer, tracker));
            assertEquals("$.c", error.fieldPath());
            assertEquals(8, error.bitOffset());
            final var view = tracker.toTraceView();
            assertEquals(CodecTraceStatus.ERROR, view.root().status());
            assertEquals(CodecTraceStatus.SUCCESS, view.root().children().get(0).status());
            assertEquals(CodecTraceStatus.SUCCESS, view.root().children().get(1).status());
            assertEquals(CodecTraceStatus.ERROR, view.root().children().get(2).status());
            assertEquals(8L, view.diagnostics().getFirst().bitOffset());
            assertEquals("b1ab", view.payloadHex());
        } finally {
            buffer.release();
        }
    }
}
