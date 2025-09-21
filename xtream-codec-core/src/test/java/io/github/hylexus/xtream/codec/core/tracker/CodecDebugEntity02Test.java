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
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodecDebugEntity02Test {

    EntityCodec entityCodec = EntityCodec.DEFAULT;
    final LocalDateTime time = LocalDateTime.of(2014, 10, 21, 19, 51, 9);

    @Test
    void test() {
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        try {
            final CodecTracker encodeTracker = new CodecTracker();
            final CodecDebugEntity02 original = createEntity();
            this.entityCodec.encode(original, buffer, encodeTracker);
            // encodeTracker.visit();
            doCompare(encodeTracker, original);

            final CodecTracker decodeTracker = new CodecTracker();
            final CodecDebugEntity02 decoded = this.entityCodec.decode(CodecDebugEntity02.class, buffer, decodeTracker);
            // decodeTracker.visit();
            doCompare(decodeTracker, original);
            doCompare(decodeTracker, decoded);
        } finally {
            XtreamBytes.releaseBuf(buffer);
            assertEquals(0, buffer.refCnt());
        }
    }

    private void doCompare(CodecTracker tracker, CodecDebugEntity02 original) {
        final RootSpan rootSpan = tracker.getRootSpan();
        assertEquals(9, rootSpan.getChildren().size());
        assertEquals(original.getAlarmFlag(), ((BasicFieldSpan) rootSpan.getChildren().get(0)).getValue());
        assertEquals(original.getStatus(), ((BasicFieldSpan) rootSpan.getChildren().get(1)).getValue());
        assertEquals(original.getLatitude(), ((BasicFieldSpan) rootSpan.getChildren().get(2)).getValue());
        assertEquals(original.getLongitude(), ((BasicFieldSpan) rootSpan.getChildren().get(3)).getValue());
        assertEquals(original.getAltitude(), ((BasicFieldSpan) rootSpan.getChildren().get(4)).getValue());
        assertEquals(original.getSpeed(), ((BasicFieldSpan) rootSpan.getChildren().get(5)).getValue());
        assertEquals(original.getDirection(), ((BasicFieldSpan) rootSpan.getChildren().get(6)).getValue());
        assertEquals(original.getTime(), ((BasicFieldSpan) rootSpan.getChildren().get(7)).getValue());
        final MapFieldSpan mapFieldSpan = (MapFieldSpan) rootSpan.getChildren().get(8);
        assertEquals(2, mapFieldSpan.getChildren().size());
    }

    CodecDebugEntity02 createEntity() {
        final CodecDebugEntity02 entity = new CodecDebugEntity02();
        entity.setAlarmFlag(123L);
        entity.setStatus(222);
        entity.setLatitude(31000562);
        entity.setLongitude(121451372);
        entity.setAltitude(666);
        entity.setSpeed(78);
        entity.setDirection(0);
        entity.setTime(time);

        final Map<Short, Object> extraItems = new LinkedHashMap<>();
        extraItems.put((short) 0x01, 111L);
        extraItems.put((short) 0x02, 222);
        entity.setExtraItems(extraItems);
        return entity;
    }
}
