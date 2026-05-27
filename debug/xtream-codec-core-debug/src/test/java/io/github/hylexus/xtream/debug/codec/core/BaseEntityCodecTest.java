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

package io.github.hylexus.xtream.debug.codec.core;

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseEntityCodecTest {

    protected EntityCodec entityCodec;
    protected ByteBufAllocator byteBufAllocator = ByteBufAllocator.DEFAULT;

    protected String encodeAsHexString(Object entity) {
        final ByteBuf buffer = this.byteBufAllocator.buffer();
        try {
            this.entityCodec.encode(entity, buffer);
            return ByteBufUtil.hexDump(buffer);
        } finally {
            buffer.release();
        }
    }

    protected <T> T doDecode(Class<T> cls, String hexString) {
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer().writeBytes(XtreamBytes.decodeHex(hexString));
        try {
            return entityCodec.decode(cls, buffer);
        } finally {
            buffer.release();
            assertEquals(0, buffer.refCnt());
        }
    }
}
