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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.h264.impl;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.h264.H264Nalu;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultH264DecoderTest {

    private DefaultH264Decoder h264Decoder;

    @BeforeEach
    void setUp() {
        this.h264Decoder = new DefaultH264Decoder(1 << 18);
    }

    @Test
    void test1() {
        final List<H264Nalu> h264Nalus = this.h264Decoder.decode(Unpooled.wrappedBuffer(new byte[]{
                0x00, 0x00, 0x01, 1, 2, 3,
        }));
        assertTrue(h264Nalus.isEmpty());
    }

    @Test
    void test2() {
        final List<H264Nalu> h264Nalus = this.h264Decoder.decode(Unpooled.wrappedBuffer(new byte[]{
                0x00, 0x00, 0x01, 1, 2, 3,
                0x00, 0x00, 0x01, 1, 2, 3,
        }));
        assertEquals(1, h264Nalus.size());
        assertEquals("010203", FormatUtils.toHexString(h264Nalus.getFirst().rbsp()));
    }

    @Test
    void test3() {
        final List<H264Nalu> h264Nalus = this.h264Decoder.decode(Unpooled.wrappedBuffer(new byte[]{
                0x00, 0x00, 0x01, 1, 2, 3,
                0x00, 0x00, 0x00, 0x01, 1, 2, 3,
        }));
        assertEquals(1, h264Nalus.size());
        assertEquals("010203", FormatUtils.toHexString(h264Nalus.getFirst().rbsp()));
    }

    @Test
    void test4() {
        final List<H264Nalu> h264Nalus = this.h264Decoder.decode(Unpooled.wrappedBuffer(new byte[]{
                0x00, 0x00, 0x00, 0x01, 1, 2, 3,
                0x00, 0x00, 0x00, 0x01, 1, 2, 3,
        }));
        assertEquals(1, h264Nalus.size());
        assertEquals("010203", FormatUtils.toHexString(h264Nalus.getFirst().rbsp()));
    }

    @Test
    void test5() {
        final List<H264Nalu> h264Nalus = this.h264Decoder.decode(Unpooled.wrappedBuffer(new byte[]{
                0x00, 0x00, 0x00, 0x01, 1, 2, 3,
                0x00, 0x00, 0x00, 0x01, 4, 5, 6,
                0x00, 0x00, 0x01,
        }));
        assertEquals(1, h264Nalus.size());
        assertEquals("010203", FormatUtils.toHexString(h264Nalus.getFirst().rbsp()));
    }

    @Test
    void test6() {
        final List<H264Nalu> h264Nalus = this.h264Decoder.decode(Unpooled.wrappedBuffer(new byte[]{
                0x00, 0x00, 0x00, 0x01, 1, 2, 3,
                0x00, 0x00, 0x00, 0x01, 4, 5, 6,
                0x00, 0x00, 0x01, 0x7,
        }));
        assertEquals(2, h264Nalus.size());
        assertEquals("010203", FormatUtils.toHexString(h264Nalus.getFirst().rbsp()));
        assertEquals("040506", FormatUtils.toHexString(h264Nalus.getLast().rbsp()));
    }

    @Test
    void test7() {
        final List<H264Nalu> h264Nalus = this.h264Decoder.decode(Unpooled.wrappedBuffer(new byte[]{
                1, 2, 3,
                0x00, 0x00, 0x00, 0x01, 1, 2, 3,
                0x00, 0x00, 0x00, 0x01, 4, 5, 6,
                0x00, 0x00, 0x01, 0x7,
        }));
        assertEquals(2, h264Nalus.size());
        assertEquals("010203", FormatUtils.toHexString(h264Nalus.getFirst().rbsp()));
        assertEquals("040506", FormatUtils.toHexString(h264Nalus.getLast().rbsp()));
    }

}
