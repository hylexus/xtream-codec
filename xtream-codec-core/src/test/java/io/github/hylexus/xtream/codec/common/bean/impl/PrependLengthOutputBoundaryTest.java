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

package io.github.hylexus.xtream.codec.common.bean.impl;

import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.XtreamCacheableClassPredicate;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.DefaultFieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.impl.SimpleBeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNodeKind;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 实体前置长度占位及内容写入失败时的部分输出、跟踪状态和恢复测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class PrependLengthOutputBoundaryTest {
    private final SimpleBeanMetadataRegistry registry = new SimpleBeanMetadataRegistry(
            new DefaultFieldCodecRegistry(), new XtreamCacheableClassPredicate.Default());
    private final EntityCodec codec = new EntityCodec(registry);

    static Stream<Arguments> failures() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(false, true).flatMap(content -> Stream.of(
                Arguments.of(tracked, content, new BytesU8(), 1, false),
                Arguments.of(tracked, content, new BytesU16(), 2, false),
                Arguments.of(tracked, content, new BoxedU8(), 1, true),
                Arguments.of(tracked, content, new BoxedU16(), 2, true))));
    }

    @ParameterizedTest
    @MethodSource("failures")
    void capacityFailurePreservesPlaceholderAndStopsTail(boolean tracked, boolean content, Packet packet, int prefixBytes, boolean boxed) {
        packet.head = 126;
        packet.tail = 127;
        System.arraycopy(boxed ? new Byte[]{0x12, 0x34, 0x56} : new byte[]{0x12, 0x34, 0x56}, 0, packet.body(), 0, 3);
        final var metadata = registry.getBeanMetadata(packet.getClass(), XtreamField.ALL_VERSION);
        final int capacity = 2 + 1 + (content ? prefixBytes + 1 : prefixBytes - 1);
        final int written = 1 + (content ? prefixBytes + (boxed ? 1 : 0) : 0);
        final String partial = "7e" + (content ? "00".repeat(prefixBytes) + (boxed ? "12" : "") : "");
        final ByteBuf output = Unpooled.buffer(capacity, capacity).writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final IndexOutOfBoundsException error = catchThrowableOfType(IndexOutOfBoundsException.class,
                    () -> codec.encode(XtreamField.ALL_VERSION, packet, output, tracker));
            assertThat(error).hasMessageContaining("maxCapacity");
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + partial);
            assertThat(output.writerIndex()).isEqualTo(2 + written);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
            assertThat(packet.tailReads).isZero();
            assertBody(packet, boxed);
            if (tracker != null) {
                assertFailure(tracker, content, prefixBytes, written);
            }

            final String wire = "7e" + (prefixBytes == 1 ? "03" : "0003") + "1234567f";
            final ByteBuf healthy = Unpooled.buffer(2 + wire.length() / 2, 2 + wire.length() / 2).writeShort(0x55aa).readerIndex(1);
            final CodecTracker encoder = tracked ? new CodecTracker() : null;
            final CodecTracker decoder = tracked ? new CodecTracker() : null;
            try {
                codec.encode(XtreamField.ALL_VERSION, packet, healthy, encoder);
                assertThat(ByteBufUtil.hexDump(healthy, 0, healthy.writerIndex())).isEqualTo("55aa" + wire);
                assertThat(healthy.readerIndex()).isEqualTo(1);
                assertThat(healthy.writerIndex()).isEqualTo(healthy.maxCapacity());
                assertThat(packet.tailReads).isEqualTo(1);
                final ByteBuf input = Unpooled.buffer().writeShort(0x55aa).writeBytes(ByteBufUtil.decodeHexDump(wire)).writeByte(0x66).readerIndex(2);
                try {
                    final Packet decoded = codec.decode(XtreamField.ALL_VERSION, packet.getClass(), input, decoder);
                    assertThat(decoded.head).isEqualTo(126);
                    assertThat(decoded.tail).isEqualTo(127);
                    assertBody(decoded, boxed);
                    assertThat(input.readerIndex()).isEqualTo(2 + wire.length() / 2);
                    assertThat(input.getUnsignedByte(input.readerIndex())).isEqualTo((short) 0x66);
                    assertThat(input.refCnt()).isEqualTo(1);
                } finally {
                    input.release();
                }
                assertSuccess(encoder, wire, prefixBytes);
                assertSuccess(decoder, wire, prefixBytes);
                assertThat(registry.getBeanMetadata(packet.getClass(), XtreamField.ALL_VERSION)).isSameAs(metadata);
                assertThat(healthy.refCnt()).isEqualTo(1);
                assertBody(packet, boxed);
            } finally {
                healthy.release();
            }
        } finally {
            output.release();
        }
    }

    private static void assertBody(Packet packet, boolean boxed) {
        assertThat(packet.body()).isEqualTo(boxed ? new Byte[]{0x12, 0x34, 0x56} : new byte[]{0x12, 0x34, 0x56});
    }

    private static void assertFailure(CodecTracker tracker, boolean content, int prefixBytes, int written) {
        final CodecTraceNode root = tracker.getTrace().getRoot();
        assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
        assertThat(root.getChildren()).extracting(CodecTraceNode::getName)
                .containsExactly(content ? new String[]{"head", "prependLengthField", "body"} : new String[]{"head", "prependLengthField"});
        final CodecTraceNode head = root.getChildren().getFirst();
        assertThat(head.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
        assertThat(head.getByteStart()).isZero();
        assertThat(head.getByteEnd()).isEqualTo(1);
        assertThat(head.getHex()).isEqualTo("7e");
        final CodecTraceNode prefix = root.getChildren().get(1);
        assertThat(prefix.getKind()).isEqualTo(CodecTraceNodeKind.LENGTH_FIELD);
        assertThat(prefix.getStatus()).isEqualTo(CodecTraceStatus.STARTED);
        assertThat(prefix.getByteStart()).isEqualTo(1);
        assertThat(prefix.getByteEnd()).isNull();
        assertThat(prefix.getValue()).isNull();
        assertThat(prefix.getHex()).isNull();
        final CodecTraceNode failed = content ? root.getChildren().getLast() : root;
        if (content) {
            assertThat(failed.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
            assertThat(failed.getByteStart()).isEqualTo(1 + prefixBytes);
            assertThat(failed.getByteEnd()).isEqualTo(written);
        }
        assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
            assertThat(diagnostic.nodeId()).isEqualTo(failed.getId());
            assertThat(diagnostic.byteOffset()).isEqualTo(written);
            assertThat(diagnostic.exceptionType()).isEqualTo(IndexOutOfBoundsException.class.getName());
        });
        assertThat(tracker.isNotTracing()).isTrue();
    }

    private static void assertSuccess(@Nullable CodecTracker tracker, String wire, int prefixBytes) {
        if (tracker != null) {
            final CodecTraceNode root = tracker.getTrace().getRoot();
            assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
            assertThat(root.getByteEnd()).isEqualTo(wire.length() / 2);
            assertThat(root.getChildren()).extracting(CodecTraceNode::getName).containsExactly("head", "prependLengthField", "body", "tail");
            assertThat(root.getChildren()).allSatisfy(node -> assertThat(node.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS));
            final CodecTraceNode prefix = root.getChildren().get(1);
            assertThat(prefix.getByteStart()).isEqualTo(1);
            assertThat(prefix.getByteEnd()).isEqualTo(1 + prefixBytes);
            assertThat(prefix.getHex()).isEqualTo(prefixBytes == 1 ? "03" : "0003");
            assertThat(((Number) prefix.getValue()).intValue()).isEqualTo(3);
            final CodecTraceNode body = root.getChildren().get(2);
            assertThat(body.getByteStart()).isEqualTo(1 + prefixBytes);
            assertThat(body.getByteEnd()).isEqualTo(4 + prefixBytes);
            assertThat(body.getHex()).isEqualTo("123456");
            assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
            assertThat(tracker.isNotTracing()).isTrue();
        }
    }

    public abstract static class Packet {
        @Preset.RustStyle.u8(order = 0)
        public int head = -1;
        @Preset.RustStyle.u8(order = 2)
        private int tail = -1;
        int tailReads;

        public int getTail() {
            tailReads++;
            return tail;
        }

        abstract Object body();
    }

    public static class BytesU8 extends Packet {
        @Preset.RustStyle.byte_array(order = 1, prependLengthFieldType = PrependLengthFieldType.u8)
        public byte[] body = new byte[3];

        @Override
        Object body() {
            return body;
        }
    }

    public static class BytesU16 extends Packet {
        @Preset.RustStyle.byte_array(order = 1, prependLengthFieldType = PrependLengthFieldType.u16)
        public byte[] body = new byte[3];

        @Override
        Object body() {
            return body;
        }
    }

    public static class BoxedU8 extends Packet {
        @Preset.RustStyle.byte_array(order = 1, prependLengthFieldType = PrependLengthFieldType.u8)
        public Byte[] body = new Byte[3];

        @Override
        Object body() {
            return body;
        }
    }

    public static class BoxedU16 extends Packet {
        @Preset.RustStyle.byte_array(order = 1, prependLengthFieldType = PrependLengthFieldType.u16)
        public Byte[] body = new Byte[3];

        @Override
        Object body() {
            return body;
        }
    }
}
