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

package io.github.hylexus.xtream.codec.core.annotation.ext;

import io.github.hylexus.xtream.codec.core.tracker.CodecTraceDirection;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNodeKind;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 七种长度字段的独立字节、数值类型、跟踪范围及截断输入测试。
 *
 * @author Codex (AI)
 */
class LengthFieldTypeBoundaryTest {

    static Stream<Arguments> values() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, LengthFieldType.i8, -128L, (byte) -128, "80"),
                Arguments.of(tracked, LengthFieldType.i8, -1L, (byte) -1, "ff"),
                Arguments.of(tracked, LengthFieldType.i8, 0L, (byte) 0, "00"),
                Arguments.of(tracked, LengthFieldType.i8, 127L, (byte) 127, "7f"),
                Arguments.of(tracked, LengthFieldType.u8, 0L, (short) 0, "00"),
                Arguments.of(tracked, LengthFieldType.u8, 127L, (short) 127, "7f"),
                Arguments.of(tracked, LengthFieldType.u8, 128L, (short) 128, "80"),
                Arguments.of(tracked, LengthFieldType.u8, 255L, (short) 255, "ff"),
                Arguments.of(tracked, LengthFieldType.i16, -32768L, (short) -32768, "8000"),
                Arguments.of(tracked, LengthFieldType.i16, -1L, (short) -1, "ffff"),
                Arguments.of(tracked, LengthFieldType.i16, 0L, (short) 0, "0000"),
                Arguments.of(tracked, LengthFieldType.i16, 32767L, (short) 32767, "7fff"),
                Arguments.of(tracked, LengthFieldType.u16, 0L, 0, "0000"),
                Arguments.of(tracked, LengthFieldType.u16, 32767L, 32767, "7fff"),
                Arguments.of(tracked, LengthFieldType.u16, 32768L, 32768, "8000"),
                Arguments.of(tracked, LengthFieldType.u16, 65535L, 65535, "ffff"),
                Arguments.of(tracked, LengthFieldType.i32, -2147483648L, Integer.MIN_VALUE, "80000000"),
                Arguments.of(tracked, LengthFieldType.i32, -1L, -1, "ffffffff"),
                Arguments.of(tracked, LengthFieldType.i32, 0L, 0, "00000000"),
                Arguments.of(tracked, LengthFieldType.i32, 2147483647L, Integer.MAX_VALUE, "7fffffff"),
                Arguments.of(tracked, LengthFieldType.u32, 0L, 0L, "00000000"),
                Arguments.of(tracked, LengthFieldType.u32, 2147483647L, 2147483647L, "7fffffff"),
                Arguments.of(tracked, LengthFieldType.u32, 2147483648L, 2147483648L, "80000000"),
                Arguments.of(tracked, LengthFieldType.u32, 4294967295L, 4294967295L, "ffffffff"),
                Arguments.of(tracked, LengthFieldType.i64, Long.MIN_VALUE, Long.MIN_VALUE, "8000000000000000"),
                Arguments.of(tracked, LengthFieldType.i64, -1L, -1L, "ffffffffffffffff"),
                Arguments.of(tracked, LengthFieldType.i64, 0L, 0L, "0000000000000000"),
                Arguments.of(tracked, LengthFieldType.i64, Long.MAX_VALUE, Long.MAX_VALUE, "7fffffffffffffff")));
    }

    @ParameterizedTest
    @MethodSource("values")
    void preservesExactBytesAndNumericTypesAtNonZeroOffset(boolean tracked, LengthFieldType type,
                                                         long value, Number expected, String hex) {
        final int width = hex.length() / 2;
        final ByteBuf buffer = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker encodeTracker = new CodecTracker();
        final CodecTracker decodeTracker = new CodecTracker();
        try {
            assertThat(type.type().name()).isEqualTo(type.name());
            assertThat(type.type().sizeInBytes()).isEqualTo(width);
            if (tracked) {
                encodeTracker.beginEncode(2, null);
                type.writeToWithTracker(buffer, value, encodeTracker, "size");
                encodeTracker.finishTrace(hex, buffer.writerIndex());
                assertTrace(encodeTracker, CodecTraceDirection.ENCODE, value, hex);
            } else {
                type.writeTo(buffer, value);
            }
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo("55aa" + hex);
            assertThat(buffer.readerIndex()).isEqualTo(1);
            assertThat(buffer.writerIndex()).isEqualTo(2 + width);
            buffer.writeByte(0x66).readerIndex(2);

            final Number decoded;
            if (tracked) {
                decodeTracker.beginDecode(2, null);
                decoded = type.readFromWithTracker(buffer, decodeTracker, "size");
                decodeTracker.finishTrace(hex, buffer.readerIndex());
                assertTrace(decodeTracker, CodecTraceDirection.DECODE, expected, hex);
            } else {
                decoded = type.readFrom(buffer);
            }
            assertThat(decoded).isExactlyInstanceOf(expected.getClass()).isEqualTo(expected);
            assertThat(buffer.readerIndex()).isEqualTo(2 + width);
            assertThat(buffer.writerIndex()).isEqualTo(3 + width);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
        } finally {
            buffer.release();
        }
    }

    static Stream<Arguments> truncatedInputs() {
        return Arrays.stream(LengthFieldType.values()).flatMap(type ->
                IntStream.range(0, type.type().sizeInBytes()).boxed().flatMap(available ->
                        Stream.of(false, true).map(tracked -> Arguments.of(tracked, type, available))));
    }

    @ParameterizedTest
    @MethodSource("truncatedInputs")
    void rejectsEveryTruncatedWidthWithoutConsumingInputOrCreatingSuccessNode(boolean tracked,
                                                                           LengthFieldType type, int available) {
        final String hex = "55aa" + "ab".repeat(available);
        final ByteBuf input = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(hex)).readerIndex(2);
        final CodecTracker tracker = new CodecTracker();
        try {
            if (tracked) {
                tracker.beginDecode(2, null);
            }
            final Throwable failure = catchThrowable(() -> {
                if (tracked) {
                    type.readFromWithTracker(input, tracker, "size");
                } else {
                    type.readFrom(input);
                }
            });
            assertThat(failure).isInstanceOf(IndexOutOfBoundsException.class);
            assertThat(input.readerIndex()).isEqualTo(2);
            assertThat(input.writerIndex()).isEqualTo(2 + available);
            assertThat(ByteBufUtil.hexDump(input, 0, input.writerIndex())).isEqualTo(hex);
            if (tracked) {
                assertThat(tracker.getTrace().getRoot().getChildren()).isEmpty();
                // 读取失败发生在长度节点建立前，由调用方结束根跟踪。
                tracker.recordFailure(failure, input.readerIndex());
                assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic ->
                        assertThat(diagnostic.byteOffset()).isZero());
                assertThat(tracker.isNotTracing()).isTrue();
            }
        } finally {
            input.release();
        }
    }

    private static void assertTrace(CodecTracker tracker, CodecTraceDirection direction, Number value, String hex) {
        final int width = hex.length() / 2;
        assertThat(tracker.getTrace().getDirection()).isEqualTo(direction);
        assertThat(tracker.getTrace().getPayloadHex()).isEqualTo(hex);
        assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
        final CodecTraceNode root = tracker.getTrace().getRoot();
        assertThat(root.getByteStart()).isZero();
        assertThat(root.getByteEnd()).isEqualTo(width);
        assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
        assertThat(root.getChildren()).singleElement().satisfies(node -> {
            assertThat(node.getKind()).isEqualTo(CodecTraceNodeKind.LENGTH_FIELD);
            assertThat(node.getName()).isEqualTo("size");
            assertThat(node.getByteStart()).isZero();
            assertThat(node.getByteEnd()).isEqualTo(width);
            assertThat(node.getHex()).isEqualTo(hex);
            assertThat(node.getValue()).isEqualTo(value);
            assertThat(node.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
        });
        assertThat(tracker.isNotTracing()).isTrue();
    }
}
