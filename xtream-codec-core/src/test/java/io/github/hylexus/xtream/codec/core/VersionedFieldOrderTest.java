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

package io.github.hylexus.xtream.codec.core;

import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 重复字段注解按协议版本选择编码顺序的回归测试。
 *
 * @author Codex (AI)
 */
class VersionedFieldOrderTest extends BaseEntityCodecTest {

    @ParameterizedTest
    @CsvSource({
            "1, 1122, false", "1, 1122, true",
            "2, 2211, false", "2, 2211, true",
            "3, 2211, false", "3, 2211, true"
    })
    void testEncode(int version, String expectedHex, boolean withTracker) {
        final VersionedMessage entity = new VersionedMessage();
        entity.setValue((short) 0x11);
        entity.setMarker((short) 0x22);
        final ByteBuf buffer = allocator.buffer();
        try {
            entityCodec.encode(version, entity, buffer, withTracker ? new CodecTracker() : null);
            assertEquals(expectedHex, FormatUtils.toHexString(buffer));
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1122, false", "1, 1122, true",
            "2, 2211, false", "2, 2211, true",
            "3, 2211, false", "3, 2211, true"
    })
    void testDecode(int version, String hex, boolean withTracker) {
        final ByteBuf buffer = allocator.buffer().writeBytes(HexFormat.of().parseHex(hex));
        try {
            final VersionedMessage entity = entityCodec.decode(version, VersionedMessage.class, buffer, withTracker ? new CodecTracker() : null);
            assertEquals(0x11, entity.getValue());
            assertEquals(0x22, entity.getMarker());
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }

    @Getter
    @Setter
    public static class VersionedMessage {

        @Preset.JtStyle.Byte(order = 40)
        @Preset.JtStyle.Byte(version = 1, order = 10)
        @Preset.JtStyle.Byte(version = 2, order = 30)
        private short value;

        @Preset.JtStyle.Byte(order = 20)
        private short marker;
    }
}
