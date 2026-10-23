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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 平台通用应答别名的编解码和工厂方法测试。
 *
 * @author Codex (AI)
 */
class ServerCommonReplyMessageTest extends BaseCodecTest {

    private static Stream<Arguments> packets() {
        return Stream.of(
                Arguments.of(Jt808ProtocolVersion.VERSION_2011, "013912344321", "7e800100050139123443210000006f020002977e"),
                Arguments.of(Jt808ProtocolVersion.VERSION_2013, "013912344323", "7e800100050139123443230000006f020002957e"),
                Arguments.of(Jt808ProtocolVersion.VERSION_2019, "00000000013912344329", "7e8001400501000000000139123443290000006f020002de7e")
        );
    }

    @ParameterizedTest
    @MethodSource("packets")
    void testEncode(Jt808ProtocolVersion version, String terminalId, String hex) {
        assertEquals(hex, encode(ServerCommonReplyMessage.of(0x0200, 111, (byte) 2), version, terminalId));
    }

    @ParameterizedTest
    @MethodSource("packets")
    void testDecode(Jt808ProtocolVersion version, String terminalId, String hex) {
        assertReply(decodeAsEntity(version, ServerCommonReplyMessage.class, hex), 2);
    }

    @Test
    void testPrimitiveFactories() {
        assertReply(ServerCommonReplyMessage.success(111, 0x0200), 0);
        assertReply(ServerCommonReplyMessage.of(0x0200, 111, (byte) 2), 2);
    }

    @Test
    void testRequestFactories() {
        final Jt808Request request = decodeAsRequest("020040000100000000013912344329006f58");
        try {
            assertReply(ServerCommonReplyMessage.success(request), 0);
            assertReply(ServerCommonReplyMessage.of(request, (byte) 2), 2);
        } finally {
            request.release();
        }
        assertEquals(0, request.body().refCnt());
    }

    @Test
    void testRequestEntityFactories() {
        final Jt808Request request = decodeAsRequest("020040000100000000013912344329006f58");
        try {
            final Jt808RequestEntity<?> entity = new Jt808RequestEntity<>(request, new Object());
            assertReply(ServerCommonReplyMessage.success(entity), 0);
            assertReply(ServerCommonReplyMessage.of(entity, (byte) 2), 2);
        } finally {
            request.release();
        }
        assertEquals(0, request.body().refCnt());
    }

    private void assertReply(ServerCommonReplyMessage message, int result) {
        assertEquals(111, message.getClientFlowId());
        assertEquals(0x0200, message.getClientMessageId());
        assertEquals(result, message.getResult());
    }
}

