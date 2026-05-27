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

package io.github.hylexus.xtream.codec.ext.jt1078.codec;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.*;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.impl.DefaultJt1078RequestHeader;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.impl.DefaultJt1078SessionManager;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionIdGenerator;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.UdpSessionIdleStateCheckerProps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class Jt1078ByteToMessageDecoderTest {

    @Test
    void test() {
        final Jt1078ByteToMessageDecoder decoder = createDecoder();
        final int[] seq = {0};
        forEachLine("mock-data/raw-stream/sim-length-6-h264-adpcma.txt", byteBuf -> {
            final ArrayList<Object> resultList = new ArrayList<>();
            decoder.decode(null, byteBuf.skipBytes(4).slice(), resultList);
            final Jt1078Request request = (Jt1078Request) resultList.getFirst();
            System.out.println(request);
            final int i = seq[0]++;
            final Jt1078RequestHeader header = request.header();
            assertEquals(i, header.sequenceNumber());
            assertEquals(6, header.simLength());
            assertEquals("13800138999", header.convertedSim());
            assertEquals(2, header.channelNumber());
            assertTrue(header.msgBodyLength() <= 950);
        });
    }

    @Test
    void test2() {
        final Jt1078ByteToMessageDecoder decoder = createDecoder();
        final int[] seq = {0};
        forEachLine("mock-data/raw-stream/sim-length-10-h264-g711a.txt", byteBuf -> {
            final ArrayList<Object> resultList = new ArrayList<>();
            decoder.decode(null, byteBuf.skipBytes(4).slice(), resultList);
            final Jt1078Request request = (Jt1078Request) resultList.getFirst();
            System.out.println(request);
            final int i = seq[0]++;
            final Jt1078RequestHeader header = request.header();
            assertEquals(i, header.sequenceNumber());
            assertEquals(10, header.simLength());
            assertEquals("20232609559", header.convertedSim());
            assertEquals(1, header.channelNumber());
            assertTrue(header.msgBodyLength() <= 950);
            assertEquals(1, resultList.size());
        });
    }

    @Test
    void test3() {
        final DefaultJt1078RequestHeader header = new DefaultJt1078RequestHeader();
        header.offset4(Jt1078RequestHeader.generateOffset4());
        header.m((byte) 1);
        header.payloadType(Jt1078PayloadType.H264);
        header.sequenceNumber(123);
        header.rawSim("013800138999");
        header.channelNumber((short) 2);
        header.dataType(Jt1078DataType.VIDEO_B);
        header.subPackageIdentifier(Jt1078SubPackageIdentifier.ATOMIC);
        header.timestamp(111111L);
        header.lastIFrameInterval(33);
        header.lastFrameInterval(44);
        header.msgBodyLength(5);

        final ByteBuf byteBuf = header.encode(true).writeBytes(new byte[]{1, 2, 3, 4, 5});
        try {
            assertEquals("3031636481e2007b0138001389990220000000000001b2070021002c00050102030405", FormatUtils.toHexString(byteBuf));

            final Jt1078ByteToMessageDecoder decoder = createDecoder();
            final List<Object> packageInfoList = new ArrayList<>();
            decoder.decode(null, byteBuf.skipBytes(4).slice(), packageInfoList);
            assertEquals(1, packageInfoList.size());
            final Jt1078Request request = (Jt1078Request) packageInfoList.getFirst();
            assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, XtreamBytes.getBytes(request.body()));
            assertHeaderEquals(header, request.header());
        } finally {
            assertEquals(2, byteBuf.refCnt());
            XtreamBytes.releaseBuf(byteBuf);
            XtreamBytes.releaseBuf(byteBuf);
            assertEquals(0, byteBuf.refCnt());
        }
    }

    private void assertHeaderEquals(DefaultJt1078RequestHeader expected, Jt1078RequestHeader actual) {
        assertEquals(expected.offset4(), actual.offset4());
        assertEquals(expected.m(), actual.m());
        assertEquals(expected.payloadType(), actual.payloadType());
        assertEquals(expected.sequenceNumber(), actual.sequenceNumber());
        assertEquals(6, actual.simLength());
        assertEquals(expected.rawSim(), actual.rawSim());
        assertEquals("13800138999", actual.convertedSim());
        assertEquals(expected.channelNumber(), actual.channelNumber());
        assertEquals(expected.dataType(), actual.dataType());
        assertEquals(expected.subPackageIdentifier(), actual.subPackageIdentifier());
        assertEquals(expected.timestamp(), actual.timestamp());
        assertEquals(expected.lastIFrameInterval(), actual.lastIFrameInterval());
        assertEquals(expected.lastFrameInterval(), actual.lastFrameInterval());
        assertEquals(expected.msgBodyLength(), actual.msgBodyLength());
    }

    private static Jt1078ByteToMessageDecoder createDecoder() {
        return new Jt1078ByteToMessageDecoder(
                Jt1078SimConverter.DEFAULT,
                new NettyConnectionMock(),
                new DefaultJt1078SessionManager(
                        false,
                        new UdpSessionIdleStateCheckerProps(),
                        new XtreamSessionIdGenerator.DefalutXtreamSessionIdGenerator()
                )
        );
    }

    static void forEachLine(String resourceName, Consumer<ByteBuf> consumer) {
        try (
                final InputStream inputStream = Jt1078ByteToMessageDecoderTest.class.getClassLoader().getResourceAsStream(resourceName);
                final BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (line.startsWith("//") || line.startsWith("#")) {
                    continue;
                }
                final ByteBuf byteBuf = XtreamBytes.byteBufFromHexString(ByteBufAllocator.DEFAULT, line);
                try {
                    consumer.accept(byteBuf);
                } finally {
                    assertEquals(2, byteBuf.refCnt());
                    XtreamBytes.releaseBuf(byteBuf);
                    XtreamBytes.releaseBuf(byteBuf);
                    assertEquals(0, byteBuf.refCnt());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
