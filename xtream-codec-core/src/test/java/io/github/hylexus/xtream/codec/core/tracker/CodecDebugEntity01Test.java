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

package io.github.hylexus.xtream.codec.core.tracker;

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class CodecDebugEntity01Test {
    EntityCodec entityCodec = EntityCodec.DEFAULT;

    @Test
    void test() {
        final CodecDebugEntity01 original = createEntity();

        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        try {
            final CodecTracker encodeTracker = new CodecTracker();
            this.entityCodec.encode(original, buffer, encodeTracker);
            // encodeTracker.visit();

            doCompare(encodeTracker, original);

            final CodecTracker decodeTracker = new CodecTracker();
            final CodecDebugEntity01 decoded = this.entityCodec.decode(CodecDebugEntity01.class, buffer, decodeTracker);
            // decodeTracker.visit();

            doCompare(decodeTracker, original);
            doCompare(decodeTracker, decoded);
        } finally {
            XtreamBytes.releaseBuf(buffer);
            assertEquals(0, buffer.refCnt());
        }
    }

    private static CodecDebugEntity01 createEntity() {
        final LocalDateTime startTime = LocalDateTime.of(2021, 4, 12, 14, 30, 3);
        final LocalDateTime endTime = LocalDateTime.of(2021, 4, 12, 14, 30, 4);
        return new CodecDebugEntity01()
                .setMultimediaType((short) 2)
                .setChannelId((short) 3)
                .setEventItemCode((short) 0)
                .setStartTime(startTime)
                .setEndTime(endTime);
    }

    private void doCompare(CodecTracker tracker, CodecDebugEntity01 entity) {
        final RootSpan rootSpan = tracker.getRootSpan();
        assertEquals(5, rootSpan.getChildren().size());

        for (final BaseSpan span : rootSpan.getChildren()) {
            assertEquals(0, span.getChildren().size());
            assertInstanceOf(BasicFieldSpan.class, span);
        }

        assertEquals(entity.getMultimediaType(), ((BasicFieldSpan) rootSpan.getChildren().get(0)).getValue());
        assertEquals(entity.getChannelId(), ((BasicFieldSpan) rootSpan.getChildren().get(1)).getValue());
        assertEquals(entity.getEventItemCode(), ((BasicFieldSpan) rootSpan.getChildren().get(2)).getValue());
        assertEquals(entity.getStartTime(), ((BasicFieldSpan) rootSpan.getChildren().get(3)).getValue());
        assertEquals(entity.getEndTime(), ((BasicFieldSpan) rootSpan.getChildren().get(4)).getValue());
    }

}
