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

package io.github.hylexus.xtream.codec.ext.jt808.codec.impl;

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.common.utils.XtreamUtils;
import io.github.hylexus.xtream.codec.ext.jt808.spec.*;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.netty.NettyInbound;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultJt808RequestDecoderTest {
    final ByteBufAllocator allocator = XtreamUtils.DEFAULT_BUFFER_FACTORY;
    NettyInbound nettyInbound;
    DefaultJt808RequestDecoder decoder;

    @BeforeEach
    void setUp() {
        this.nettyInbound = mock(NettyInbound.class);
        when(nettyInbound.withConnection(any())).thenReturn(nettyInbound);
        this.decoder = new DefaultJt808RequestDecoder(new DefaultJt808BytesProcessor(allocator), new Jt808MessageEncryptionHandler.NoOps(), new DefaultJt808RequestCombiner(allocator, 100, Duration.ofSeconds(100)));
    }

    @Test
    void test() {
        final String hexString = "02004086010000000001893094655200E4000000000000000101D907F2073D336C000000000000211124114808010400000026030200003001153101002504000000001404000000011504000000FA160400000000170200001803000000EA10FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF02020000EF0400000000F31B01711800000000000000000000000000000000000000000000000056";
        final Jt808Request request = doDecode(hexString);
        assertEquals(0x0200, request.header().messageId());
        assertEquals(Jt808ProtocolVersion.VERSION_2019, request.header().version());
        assertEquals("00000000018930946552", request.header().terminalId());
        assertEquals(228, request.header().flowId());
        final Jt808RequestHeader.Jt808MessageBodyProps bodyProps = request.header().messageBodyProps();
        assertEquals(16518, bodyProps.intValue());
        assertEquals(134, bodyProps.messageBodyLength());
        assertFalse(bodyProps.hasSubPackage());
        assertEquals(0, bodyProps.encryptionType());
    }

    private Jt808Request doDecode(String hexString) {
        final ByteBuf payload = XtreamBytes.byteBufFromHexString(allocator, hexString);
        return decoder.decode(Jt808ServerType.INSTRUCTION_SERVER, null, allocator, nettyInbound, XtreamRequest.Type.TCP, payload, null, null);
    }
}
