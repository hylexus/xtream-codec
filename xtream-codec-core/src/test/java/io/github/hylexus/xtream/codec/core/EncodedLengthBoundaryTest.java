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
import io.github.hylexus.xtream.codec.core.annotation.EncodedLength;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNodeKind;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 编码长度的容量边界、偏移与条件组合测试。
 *
 * @author Codex (AI)
 */
class EncodedLengthBoundaryTest extends BaseEntityCodecTest {

    static Stream<Arguments> lengths() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(1, 2).flatMap(width -> Stream.of(
                Arguments.of(tracked, width, 0), Arguments.of(tracked, width, width == 1 ? 255 : 65535))));
    }

    @ParameterizedTest
    @MethodSource("lengths")
    void backfillsZeroAndMaximumLengthWithoutChangingSurroundingBytes(boolean tracked, int width, int length) {
        final byte[] payload = new byte[length];
        Arrays.fill(payload, (byte) 0xab);
        final Object source = packet(width, payload);
        final String lengthHex = length == 0 ? "00".repeat(width) : "ff".repeat(width);
        final String expected = lengthHex + "ab".repeat(length) + "7e";
        final ByteBuf output = allocator.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = new CodecTracker();
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, source, output, tracked ? tracker : null);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + expected);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.writerIndex()).isEqualTo(2 + width + length + 1);
            assertThat(width == 1 ? ((U8Packet) source).bodyLength() : ((U16Packet) source).bodyLength()).isEqualTo(19);
            if (tracked) {
                assertLengthTrace(tracker, length, width, lengthHex);
                assertThat(tracker.getTrace().getPayloadHex()).isEqualTo(expected);
            }
            output.readerIndex(2);
            final Object decoded = entityCodec.decode(source.getClass(), output);
            assertThat(decoded).usingRecursiveComparison().ignoringFields("bodyLength").isEqualTo(source);
            assertThat(width == 1 ? ((U8Packet) decoded).bodyLength() : ((U16Packet) decoded).bodyLength()).isEqualTo(length);
            assertThat(output.readerIndex()).isEqualTo(output.writerIndex());
        } finally {
            output.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, 1, 256", "true, 1, 256", "false, 2, 65536", "true, 2, 65536"})
    void rejectsOverflowBeforeWritingTailWithoutWrappingLength(boolean tracked, int width, int length) {
        final byte[] payload = new byte[length];
        Arrays.fill(payload, (byte) 0xab);
        final ByteBuf output = allocator.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = new CodecTracker();
        try {
            assertThatThrownBy(() -> entityCodec.encode(XtreamField.ALL_VERSION, packet(width, payload), output,
                    tracked ? tracker : null)).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("exceeds max value").hasMessageContaining(Integer.toString(length));
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex()))
                    .isEqualTo("55aa" + "00".repeat(width) + "ab".repeat(length));
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.writerIndex()).isEqualTo(2 + width + length);
            if (tracked) {
                assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
                    assertThat(diagnostic.exceptionType()).isEqualTo(IllegalArgumentException.class.getName());
                    assertThat(diagnostic.byteOffset()).isEqualTo(width + length);
                });
                assertThat(tracker.isNotTracing()).isTrue();
            }
        } finally {
            output.release();
        }
    }

    @ParameterizedTest
    @CsvSource({
            "false, 0, 0, 00007e", "true, 0, 0, 00007e",
            "false, 0, 1, 000112347f7e", "true, 0, 1, 000112347f7e",
            "false, 1, 0, 010000007e", "true, 1, 0, 010000007e",
            "false, 1, 1, 0101000312347f7e", "true, 1, 1, 0101000312347f7e"
    })
    void measuresOnlyPresentFieldsAndAllowsLengthFieldToBeSkipped(boolean tracked, int enabled, int include, String expected) {
        final ConditionalPacket source = new ConditionalPacket(enabled, include, 19, 0x1234, (short) 0x7f, 0x7e);
        final ByteBuf output = allocator.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = new CodecTracker();
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, source, output, tracked ? tracker : null);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + expected);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(source.bodyLength()).isEqualTo(19);
            if (tracked) {
                assertThat(tracker.getTrace().getPayloadHex()).isEqualTo(expected);
                assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
                if (enabled == 0) {
                    assertThat(tracker.getTrace().getRoot().getChildren()).extracting("name").doesNotContain("bodyLength");
                } else {
                    assertThat(tracker.getTrace().getRoot().getChildren()).filteredOn("name", "bodyLength")
                            .singleElement().satisfies(field -> {
                                assertThat(field.getKind()).isEqualTo(CodecTraceNodeKind.LENGTH_FIELD);
                                assertThat(field.getValue()).isEqualTo(include == 0 ? 0 : 3);
                                assertThat(field.getHex()).isEqualTo(include == 0 ? "0000" : "0003");
                                assertThat(field.getByteStart()).isEqualTo(2);
                                assertThat(field.getByteEnd()).isEqualTo(4);
                            });
                }
            }
            output.readerIndex(2);
            final ConditionalPacket decoded = entityCodec.decode(ConditionalPacket.class, output);
            assertThat(decoded).isEqualTo(new ConditionalPacket(enabled, include, enabled == 0 ? 0 : include * 3,
                    include == 0 ? null : 0x1234, include == 0 ? null : (short) 0x7f, 0x7e));
            assertThat(output.readerIndex()).isEqualTo(output.writerIndex());
        } finally {
            output.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void nullFieldsLeaveEmptyRangeAtEndOfEntity(boolean tracked) {
        final ByteBuf output = allocator.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = new CodecTracker();
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, new NullablePacket(19, null, null), output, tracked ? tracker : null);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa0000");
            assertThat(output.readerIndex()).isEqualTo(1);
            if (tracked) {
                assertLengthTrace(tracker, 0, 2, "0000");
                assertThat(tracker.getTrace().getRoot().getChildren()).hasSize(1);
            }
        } finally {
            output.release();
        }
    }

    private static Object packet(int width, byte[] payload) {
        return width == 1 ? new U8Packet(19, payload, 0x7e) : new U16Packet(19, payload, 0x7e);
    }

    private static void assertLengthTrace(CodecTracker tracker, int length, int width, String lengthHex) {
        assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
        assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
        assertThat(tracker.getTrace().getRoot().getChildren()).filteredOn("name", "bodyLength")
                .singleElement().satisfies(field -> {
                    assertThat(field.getKind()).isEqualTo(CodecTraceNodeKind.LENGTH_FIELD);
                    assertThat(field.getValue()).isEqualTo(length);
                    assertThat(field.getHex()).isEqualTo(lengthHex);
                    assertThat(field.getByteStart()).isZero();
                    assertThat(field.getByteEnd()).isEqualTo(width);
                });
    }

    public record U8Packet(
            @Preset.RustStyle.u8 @EncodedLength(until = "tail") int bodyLength,
            @Preset.RustStyle.byte_array(lengthExpression = "bodyLength") byte[] payload,
            @Preset.RustStyle.u8 int tail) {
    }

    public record U16Packet(
            @Preset.RustStyle.u16 @EncodedLength(until = "tail") int bodyLength,
            @Preset.RustStyle.byte_array(lengthExpression = "bodyLength") byte[] payload,
            @Preset.RustStyle.u8 int tail) {
    }

    public record ConditionalPacket(
            @Preset.RustStyle.u8 int enabled,
            @Preset.RustStyle.u8 int include,
            @Preset.RustStyle.u16(condition = "enabled == 1") @EncodedLength(from = "first", until = "tail") int bodyLength,
            @Preset.RustStyle.u16(condition = "include == 1") Integer first,
            @Preset.RustStyle.u8(condition = "include == 1") Short second,
            @Preset.RustStyle.u8 int tail) {
    }

    public record NullablePacket(
            @Preset.RustStyle.u16 @EncodedLength int bodyLength,
            @Preset.RustStyle.u16 Integer first,
            @Preset.RustStyle.u8 Short second) {
    }
}
