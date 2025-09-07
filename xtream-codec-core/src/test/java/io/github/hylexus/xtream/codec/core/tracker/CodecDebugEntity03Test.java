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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CodecDebugEntity03Test {
    EntityCodec entityCodec = EntityCodec.DEFAULT;

    @Test
    void test() {
        final CodecDebugEntity03 original = createEntity();

        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        try {
            final CodecTracker encodeTracker = new CodecTracker();
            this.entityCodec.encode(original, buffer, encodeTracker);
            // encodeTracker.visit();
            doCompare(encodeTracker, original);

            final CodecTracker decodeTracker = new CodecTracker();
            final CodecDebugEntity03 decoded = this.entityCodec.decode(CodecDebugEntity03.class, buffer, decodeTracker);
            // decodeTracker.visit();
            doCompare(decodeTracker, original);
            doCompare(decodeTracker, decoded);
        } finally {
            XtreamBytes.releaseBuf(buffer);
            assertEquals(0, buffer.refCnt());
        }
    }

    private static void doCompare(CodecTracker tracker, CodecDebugEntity03 original) {
        final RootSpan rootSpan = tracker.getRootSpan();
        assertEquals(3, rootSpan.getChildren().size());
        assertEquals(original.getFlowId(), ((BasicFieldSpan) rootSpan.getChildren().get(0)).getValue());
        assertEquals(original.getMultimediaDataItemCount(), ((BasicFieldSpan) rootSpan.getChildren().get(1)).getValue());
        assertInstanceOf(CollectionFieldSpan.class, rootSpan.getChildren().get(2));
        assertEquals(2, rootSpan.getChildren().get(2).getChildren().size());
    }

    private CodecDebugEntity03 createEntity() {
        final LocalDateTime time = LocalDateTime.of(2021, 9, 27, 15, 30, 33);
        return new CodecDebugEntity03()
                .setFlowId(111)
                .setMultimediaDataItemCount(2)
                .setItemList(List.of(
                                new CodecDebugEntity03.Item()
                                        .setMultimediaId(1)
                                        .setMultimediaType((short) 0)
                                        .setChannelId((short) 22)
                                        .setEventItemCode((short) 0)
                                        .setLocation(new CodecDebugEntity02()
                                                .setAlarmFlag(1)
                                                .setStatus(1)
                                                .setLatitude(31000563L)
                                                .setLongitude(121451373L)
                                                .setAltitude(777)
                                                .setSpeed(67)
                                                .setDirection(90)
                                                .setTime(time)
                                        ),
                                new CodecDebugEntity03.Item()
                                        .setMultimediaId(1)
                                        .setMultimediaType((short) 0)
                                        .setChannelId((short) 22)
                                        .setEventItemCode((short) 0)
                                        .setLocation(new CodecDebugEntity02()
                                                .setAlarmFlag(1)
                                                .setStatus(1)
                                                .setLatitude(31000565L)
                                                .setLongitude(121451375L)
                                                .setAltitude(777)
                                                .setSpeed(67)
                                                .setDirection(90)
                                                .setTime(time))
                        )

                );
    }

}
