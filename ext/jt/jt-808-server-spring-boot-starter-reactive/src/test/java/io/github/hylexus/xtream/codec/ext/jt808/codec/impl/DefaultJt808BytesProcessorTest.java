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

package io.github.hylexus.xtream.codec.ext.jt808.codec.impl;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808BytesProcessor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author hylexus
 */
class DefaultJt808BytesProcessorTest {
    Jt808BytesProcessor jt808BytesProcessor;
    ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;

    @BeforeEach
    void setUp() {
        this.jt808BytesProcessor = new DefaultJt808BytesProcessor(allocator);
    }

    @Test
    public void testDoEscapeForReceive() {
        assertEquals("00000000", escapeForReceive("00000000"));

        assertEquals("7e0000", escapeForReceive("7d020000"));

        assertEquals("7e7d", escapeForReceive("7d027d01"));

        assertEquals("7e000102037e04057d", escapeForReceive("7d02000102037d0204057d01"));

        assertEquals("0102000102037e04057e09097d7e01", escapeForReceive("0102000102037d0204057d0209097d017d0201"));

        assertEquals("7e00000000", escapeForReceive("7d0200000000"));

        assertEquals("7d2200000000", escapeForReceive("7d2200000000"));
    }

    @Test
    public void testDoEscapeForSend() {
        assertEquals("00000000", escapeForSend("00000000"));

        assertEquals("7d020000", escapeForSend("7e0000"));

        assertEquals("7d027d01", escapeForSend("7e7d"));

        assertEquals("7d02000102037d0204057d01", escapeForSend("7e000102037e04057d"));

        assertEquals("0102000102037d0204057d0209097d017d0201", escapeForSend("0102000102037e04057e09097d7e01"));

        assertEquals("7d0200000000", escapeForSend("7e00000000"));

        assertEquals("7d012200000000", escapeForSend("7d2200000000"));
    }

    String escapeForReceive(String hex) {
        final ByteBuf payload = XtreamBytes.byteBufFromHexString(allocator, hex);
        final ByteBuf byteBuf = this.jt808BytesProcessor.doEscapeForReceive(payload);
        try {
            return FormatUtils.toHexString(byteBuf);
        } finally {
            XtreamBytes.releaseBuf(byteBuf);
            XtreamBytes.releaseBuf(payload);
            assertEquals(0, payload.refCnt());
            assertEquals(0, byteBuf.refCnt());
        }
    }

    String escapeForSend(String hex) {
        final ByteBuf payload = XtreamBytes.byteBufFromHexString(allocator, hex);
        final ByteBuf byteBuf = this.jt808BytesProcessor.doEscapeForSend(payload);
        try {
            return FormatUtils.toHexString(byteBuf);
        } finally {
            XtreamBytes.releaseBuf(byteBuf);
            XtreamBytes.releaseBuf(payload);
            assertEquals(0, payload.refCnt());
            assertEquals(0, byteBuf.refCnt());
        }
    }
}
