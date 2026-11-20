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

package io.github.hylexus.xtream.loadtest.jt808;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

// 独立构造和检查线上的协议字节，避免服务端自身编解码互相掩盖错误。
final class WireTerminal implements AutoCloseable {
    private final int version;
    private final Socket tcp;
    private final DatagramSocket udp;
    private final int port;

    WireTerminal(int version, boolean useUdp, int port) throws IOException {
        this.version = version;
        this.port = port;
        if (useUdp) {
            udp = new DatagramSocket(new InetSocketAddress("127.0.0.1", 0));
            udp.setSoTimeout(3000);
            tcp = null;
        } else {
            tcp = new Socket();
            tcp.connect(new InetSocketAddress("127.0.0.1", port), 3000);
            tcp.setSoTimeout(3000);
            udp = null;
        }
    }

    String terminalId() {
        return version == 2019 ? "00000000013800000001" : "013800000001";
    }

    int localPort() {
        return tcp != null ? tcp.getLocalPort() : udp.getLocalPort();
    }

    void send(int messageId, int flowId, byte[] body) throws IOException {
        sendRaw(frame(messageId, flowId, body, false));
    }

    void sendRaw(byte[] bytes) throws IOException {
        if (tcp != null) {
            tcp.getOutputStream().write(bytes);
            tcp.getOutputStream().flush();
        } else {
            udp.send(new DatagramPacket(bytes, bytes.length, new InetSocketAddress("127.0.0.1", port)));
        }
    }

    Reply exchange(int messageId, int flowId, byte[] body) throws IOException {
        send(messageId, flowId, body);
        return receive();
    }

    Reply receive() throws IOException {
        final byte[] wire;
        if (tcp != null) {
            final var input = tcp.getInputStream();
            assertEquals(0x7e, input.read());
            final var bytes = new ByteArrayOutputStream();
            int value;
            while ((value = input.read()) != 0x7e) {
                if (value < 0) {
                    throw new IOException("应答帧未完成即断开");
                }
                bytes.write(value);
            }
            wire = bytes.toByteArray();
        } else {
            final var packet = new DatagramPacket(new byte[8192], 8192);
            udp.receive(packet);
            assertEquals(0x7e, packet.getData()[0] & 0xff);
            assertEquals(0x7e, packet.getData()[packet.getLength() - 1] & 0xff);
            wire = Arrays.copyOfRange(packet.getData(), 1, packet.getLength() - 1);
        }
        final var decoded = new ByteArrayOutputStream();
        for (int i = 0; i < wire.length; i++) {
            final int value = wire[i] & 0xff;
            if (value == 0x7d) {
                final int next = wire[++i] & 0xff;
                assertTrue(next == 1 || next == 2);
                decoded.write(next == 1 ? 0x7d : 0x7e);
            } else {
                decoded.write(value);
            }
        }
        final byte[] bytes = decoded.toByteArray();
        int checksum = 0;
        for (byte value : bytes) {
            checksum ^= value & 0xff;
        }
        assertEquals(0, checksum);
        final int properties = word(bytes, 2);
        final int headerSize = (properties & 0x4000) == 0 ? 12 : 17;
        assertEquals(version == 2019, headerSize == 17);
        assertEquals(headerSize + (properties & 0x3ff) + 1, bytes.length);
        final int phoneStart = headerSize == 17 ? 5 : 4;
        final int phoneEnd = headerSize == 17 ? 15 : 10;
        assertEquals(terminalId(), HexFormat.of().formatHex(Arrays.copyOfRange(bytes, phoneStart, phoneEnd)));
        return new Reply(word(bytes, 0), Arrays.copyOfRange(bytes, headerSize, bytes.length - 1));
    }

    byte[] frame(int messageId, int flowId, byte[] body, boolean corruptChecksum) throws IOException {
        final var raw = new ByteArrayOutputStream();
        final var output = new DataOutputStream(raw);
        output.writeShort(messageId);
        output.writeShort(body.length | (version == 2019 ? 0x4000 : 0));
        if (version == 2019) {
            output.writeByte(1);
        }
        output.write(HexFormat.of().parseHex(terminalId()));
        output.writeShort(flowId);
        output.write(body);
        int checksum = 0;
        for (byte value : raw.toByteArray()) {
            checksum ^= value & 0xff;
        }
        raw.write(checksum ^ (corruptChecksum ? 1 : 0));
        final var wire = new ByteArrayOutputStream();
        wire.write(0x7e);
        for (byte value : raw.toByteArray()) {
            switch (value & 0xff) {
                case 0x7d -> {
                    wire.write(0x7d);
                    wire.write(1);
                }
                case 0x7e -> {
                    wire.write(0x7d);
                    wire.write(2);
                }
                default -> wire.write(value & 0xff);
            }
        }
        wire.write(0x7e);
        return wire.toByteArray();
    }

    byte[] registration() throws IOException {
        final var bytes = new ByteArrayOutputStream();
        final var output = new DataOutputStream(bytes);
        output.writeShort(1);
        output.writeShort(2);
        output.write(padded("MAKER", version == 2019 ? 11 : 5));
        output.write(padded("MODEL", version == 2019 ? 30 : version == 2013 ? 20 : 8));
        output.write(padded("TERM001", version == 2019 ? 30 : 7));
        output.writeByte(1);
        output.write("TEST01".getBytes(StandardCharsets.US_ASCII));
        return bytes.toByteArray();
    }

    byte[] authentication(String code) throws IOException {
        final var bytes = new ByteArrayOutputStream();
        final byte[] auth = code.getBytes(StandardCharsets.US_ASCII);
        if (version == 2019) {
            bytes.write(auth.length);
        }
        bytes.write(auth);
        if (version == 2019) {
            bytes.write(padded("123456789012345", 15));
            bytes.write(padded("1.0", 20));
        }
        return bytes.toByteArray();
    }

    static byte[] location() throws IOException {
        final var bytes = new ByteArrayOutputStream();
        final var output = new DataOutputStream(bytes);
        output.writeInt(0);
        output.writeInt(3);
        output.writeInt(31_230_400);
        output.writeInt(121_473_700);
        output.writeShort(10);
        output.writeShort(300);
        output.writeShort(90);
        output.write(HexFormat.of().parseHex("260101010203"));
        // 里程附加项同时覆盖两种转义字节。
        output.writeByte(1);
        output.writeByte(4);
        output.writeInt(0x007d7e00);
        return bytes.toByteArray();
    }

    static byte[] batch(int declaredCount) throws IOException {
        final var bytes = new ByteArrayOutputStream();
        final var output = new DataOutputStream(bytes);
        output.writeShort(declaredCount);
        output.writeByte(0);
        for (int i = 0; i < 2; i++) {
            final byte[] location = location();
            output.writeShort(location.length);
            output.write(location);
        }
        return bytes.toByteArray();
    }

    private static byte[] padded(String value, int length) {
        return Arrays.copyOf(value.getBytes(StandardCharsets.US_ASCII), length);
    }

    static int word(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xff) << 8) | (bytes[offset + 1] & 0xff);
    }

    @Override
    public void close() throws IOException {
        if (tcp != null) {
            tcp.close();
        } else {
            udp.close();
        }
    }

    record Reply(int messageId, byte[] body) {
        void assertCommon(int requestId, int flowId, int result) {
            assertEquals(0x8001, messageId);
            assertEquals(5, body.length);
            assertEquals(flowId, word(body, 0));
            assertEquals(requestId, word(body, 2));
            assertEquals(result, body[4] & 0xff);
        }
    }
}
