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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.codec;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.*;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 位置附加项类型分派与相邻记录边界测试。
 *
 * @author Codex (AI)
 */
class BuiltinLocationMessageExtraItemFieldCodecTest extends BaseCodecTest {
    private static final LocalDateTime TIME = LocalDateTime.of(2025, 2, 21, 21, 33, 32);

    @Test
    void testScalarTypes() {
        final Map<Short, Object> items = new LinkedHashMap<>();
        items.put((short) 0x01, 0xffffffffL);
        items.put((short) 0x02, 65535);
        items.put((short) 0x03, 32768);
        items.put((short) 0x04, 1);
        items.put((short) 0x05, new byte[]{0, 0x7d, 0x7e, (byte) 0xff});
        items.put((short) 0x06, (short) -123);
        items.put((short) 0x25, 0x80000000L);
        items.put((short) 0x2a, 65535);
        items.put((short) 0x2b, Integer.MIN_VALUE);
        items.put((short) 0x30, (short) 255);
        items.put((short) 0x31, (short) 128);
        final String hex = "0104ffffffff0202ffff03028000040200010504007d7eff0602ff852504800000002a02ffff2b04800000003001ff310180";
        assertEquals(hex, encodeBody(new LocationItems().setItems(items)));
        final Map<Short, Object> actual = decodeBody(LocationItems.class, hex).getItems();
        assertEquals(items.keySet(), actual.keySet());
        for (final Short key : items.keySet()) {
            if (items.get(key) instanceof byte[] bytes) {
                assertArrayEquals(bytes, (byte[]) actual.get(key));
            } else {
                assertEquals(items.get(key), actual.get(key), "附加项 ID: " + key);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testAdjacentAlarmItems(boolean withTracker) {
        final AlarmIdentifier identifier = new AlarmIdentifier().setTerminalId("ABC1234").setTime(TIME)
                .setSequence((short) 255).setAttachmentCount((short) 2).setReserved((short) 0);
        final Map<Short, Object> items = new LinkedHashMap<>();
        items.put((short) 0x65, new LocationItem0x65().setReservedOffset8(new byte[4])
                .setDatetime("250221213332").setAlarmIdentifier(identifier));
        items.put((short) 0x66, new LocationItem0x66().setDatetime(TIME).setAlarmIdentifier(identifier)
                .setEventItemCount((short) 1).setEventItemList(List.of(new LocationItem0x66.EventItem()
                        .setOffset0((short) 7).setOffset2(0x1234).setOffset4(200).setOffset6(30).setOffset8(80))));
        items.put((short) 0x67, new LocationItem0x67().setDatetime(TIME).setAlarmIdentifier(identifier));
        items.put((short) 0x01, 0xf0000001L);
        final String alarmIdentifier = "41424331323334250221213332ff0200";
        final String hex = "652f" + "00".repeat(23) + "2502212133320000" + alarmIdentifier
                + "6632" + "00".repeat(16) + "2502212133320000" + alarmIdentifier + "0107123400c8001e0050"
                + "6729" + "00".repeat(17) + "2502212133320000" + alarmIdentifier
                + "0104f0000001";
        final ByteBuf encoded = allocator.buffer();
        final ByteBuf input = XtreamBytes.byteBufFromHexString(allocator, hex);
        try {
            final LocationItems decoded;
            if (withTracker) {
                entityCodec.encode(new LocationItems().setItems(items), encoded, new CodecTracker());
                decoded = entityCodec.decode(LocationItems.class, input, new CodecTracker());
            } else {
                entityCodec.encode(new LocationItems().setItems(items), encoded);
                decoded = entityCodec.decode(LocationItems.class, input);
            }
            assertEquals(hex, FormatUtils.toHexString(encoded));
            assertEquals(0, input.readableBytes());
            assertEquals(items.keySet(), decoded.getItems().keySet());
            final LocationItem0x65 item65 = assertInstanceOf(LocationItem0x65.class, decoded.getItems().get((short) 0x65));
            assertEquals("250221213332", item65.getDatetime());
            final LocationItem0x66 item66 = assertInstanceOf(LocationItem0x66.class, decoded.getItems().get((short) 0x66));
            assertEquals(1, item66.getEventItemCount());
            assertEquals(1, item66.getEventItemList().size());
            assertEquals(0x1234, item66.getEventItemList().getFirst().getOffset2());
            final LocationItem0x67 item67 = assertInstanceOf(LocationItem0x67.class, decoded.getItems().get((short) 0x67));
            assertEquals(TIME, item67.getDatetime());
            assertEquals("ABC1234", item67.getAlarmIdentifier().getTerminalId());
            assertEquals(0xf0000001L, decoded.getItems().get((short) 0x01));
        } finally {
            encoded.release();
            input.release();
            assertEquals(0, encoded.refCnt());
            assertEquals(0, input.refCnt());
        }
    }

    @Test
    void testUnknownItemIsRejected() {
        assertThrows(UnsupportedOperationException.class, () -> decodeBody(LocationItems.class, "fe0100"));
        assertThrows(UnsupportedOperationException.class,
                () -> encodeBody(new LocationItems().setItems(Map.of((short) 0xfe, new byte[]{0}))));
    }

    @Test
    void testCustomLocation11Codec() {
        final BuiltinLocationMessageExtraItemFieldCodec.LocationItem0x11FieldCodec codec =
                BuiltinLocationMessageExtraItemFieldCodec.LocationItem0x11FieldCodec.INSTANCE;
        final ByteBuf buffer = allocator.buffer();
        try {
            codec.serialize(null, null, buffer, new LocationItem0x11((short) 0, null));
            codec.serialize(null, null, buffer, new LocationItem0x11((short) 2, 0xffffffffL));
            assertEquals("0002ffffffff", FormatUtils.toHexString(buffer));
            final LocationItem0x11 empty = codec.deserialize(null, null, buffer, 1);
            assertEquals(0, empty.getLocationType());
            assertNull(empty.locationId());
            final LocationItem0x11 nonempty = codec.deserialize(null, null, buffer, 5);
            assertEquals(2, nonempty.getLocationType());
            assertEquals(0xffffffffL, nonempty.locationId());
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
            assertEquals(0, buffer.refCnt());
        }
    }

    public static class LocationItems {
        @Preset.JtStyle.Object(fieldCodec = BuiltinLocationMessageExtraItemFieldCodec.class)
        private Map<Short, Object> items;

        public Map<Short, Object> getItems() {
            return items;
        }

        public LocationItems setItems(Map<Short, Object> items) {
            this.items = items;
            return this;
        }
    }
}

