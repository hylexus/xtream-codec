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

package io.github.hylexus.xtream.codec.core.record;

import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 嵌套 Record 的长度隔离、尾字段和切片跟踪坐标测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class NestedRecordLengthBoundaryTest extends BaseEntityCodecTest {

    @ParameterizedTest
    @MethodSource("boundedPackets")
    void remainingTextStaysInsideDeclaredRegion(boolean tracked, Object packet, String hex, int prefixBytes) {
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        final CodecTracker encodeTracker = tracker(tracked);
        final CodecTracker decodeTracker = tracker(tracked);
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, packet, buffer, encodeTracker);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("55" + hex);
            buffer.writeByte(0x66).readerIndex(1);
            assertThat(entityCodec.decode(XtreamField.ALL_VERSION, packet.getClass(), buffer, decodeTracker)).isEqualTo(packet);
            assertThat(buffer.readerIndex()).isEqualTo(1 + hex.length() / 2);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (tracked) {
                for (final CodecTracker trace : new CodecTracker[]{encodeTracker, decodeTracker}) {
                    final CodecTraceNode body = child(trace.getTrace().getRoot(), "body");
                    assertRange(body, prefixBytes, prefixBytes + 2, "4142");
                    assertRange(child(body, "text"), prefixBytes, prefixBytes + 2, "4142");
                    assertRange(child(trace.getTrace().getRoot(), "tail"), prefixBytes + 2, prefixBytes + 3, "7e");
                    assertThat(trace.getTrace().getDiagnostics()).isEmpty();
                }
            }
        } finally {
            buffer.release();
        }
    }

    static Stream<Arguments> boundedPackets() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, new FixedPacket(new Text("AB"), (short) 126), "41427e", 0),
                Arguments.of(tracked, new ExpressionPacket(new Text("AB"), (short) 126), "41427e", 0),
                Arguments.of(tracked, new PrefixedPacket(new Text("AB"), (short) 126), "0241427e", 1)));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void emptyRegionDoesNotConsumeTail(boolean tracked) {
        final PrefixedPacket packet = new PrefixedPacket(new Text(""), (short) 126);
        final ByteBuf buffer = allocator.buffer();
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, packet, buffer, tracker(tracked));
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("007e");
            assertThat(entityCodec.decode(XtreamField.ALL_VERSION, PrefixedPacket.class, buffer, tracker(tracked)))
                    .isEqualTo(new PrefixedPacket(new Text(null), (short) 126));
            assertThat(buffer.readerIndex()).isEqualTo(2);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, 0", "true, 0", "false, 1", "true, 1"})
    void shortRegionCannotBorrowBytesFromFollowingFields(boolean tracked, int length) {
        final ByteBuf buffer = allocator.buffer().writeByte(0x55).writeByte(length).writeShort(0x1234).writeByte(0x7e).readerIndex(1);
        final CodecTracker trace = tracker(tracked);
        try {
            assertThatThrownBy(() -> entityCodec.decode(XtreamField.ALL_VERSION, NumericPacket.class, buffer, trace))
                    .isInstanceOf(IndexOutOfBoundsException.class).hasMessageContaining("value");
            assertThat(buffer.readerIndex()).isEqualTo(2 + length);
            assertThat(buffer.getUnsignedByte(4)).isEqualTo((short) 126);
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (tracked) {
                final CodecTraceNode body = child(trace.getTrace().getRoot(), "body");
                final CodecTraceNode value = child(body, "value");
                assertThat(value.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(value.getByteStart()).isEqualTo(1);
                assertThat(trace.getTrace().getRoot().getChildren()).extracting(CodecTraceNode::getName).doesNotContain("tail");
                assertThat(trace.getTrace().getDiagnostics()).isNotEmpty();
            }
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void unusedRegionBytesAreSkippedBeforeOuterTail(boolean tracked) {
        final ByteBuf buffer = allocator.buffer().writeByte(0x55).writeBytes(ByteBufUtil.decodeHexDump("031234007e66")).readerIndex(1);
        final CodecTracker trace = tracker(tracked);
        try {
            assertThat(entityCodec.decode(XtreamField.ALL_VERSION, NumericPacket.class, buffer, trace))
                    .isEqualTo(new NumericPacket(new NumberValue(0x1234), (short) 126));
            assertThat(buffer.readerIndex()).isEqualTo(6);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            if (tracked) {
                final CodecTraceNode body = child(trace.getTrace().getRoot(), "body");
                assertRange(body, 1, 4, "123400");
                assertRange(child(body, "value"), 1, 3, "1234");
                assertRange(child(trace.getTrace().getRoot(), "tail"), 4, 5, "7e");
            }
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void nestedSlicesAccumulateCoordinatesAndRestoreOuterPosition(boolean tracked) {
        final DeepPacket packet = new DeepPacket(new PrefixedPacket(new Text("AB"), (short) 126), (short) 127);
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        final CodecTracker trace = tracker(tracked);
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, packet, buffer, tracker(tracked));
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("55040241427e7f");
            buffer.writeByte(0x66).readerIndex(1);
            assertThat(entityCodec.decode(XtreamField.ALL_VERSION, DeepPacket.class, buffer, trace)).isEqualTo(packet);
            assertThat(buffer.readerIndex()).isEqualTo(7);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            if (tracked) {
                final CodecTraceNode outer = child(trace.getTrace().getRoot(), "body");
                final CodecTraceNode inner = child(outer, "body");
                assertRange(outer, 1, 5, "0241427e");
                assertRange(inner, 2, 4, "4142");
                assertRange(child(inner, "text"), 2, 4, "4142");
                assertRange(child(outer, "tail"), 4, 5, "7e");
                assertRange(child(trace.getTrace().getRoot(), "tail"), 5, 6, "7f");
            }
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void unboundedRecordKeepsExistingFieldConsumption(boolean tracked) {
        final ByteBuf buffer = allocator.buffer().writeShort(0x1234).writeByte(0x7e).writeByte(0x66);
        try {
            assertThat(entityCodec.decode(XtreamField.ALL_VERSION, UnboundedPacket.class, buffer, tracker(tracked)))
                    .isEqualTo(new UnboundedPacket(new NumberValue(0x1234), (short) 126));
            assertThat(buffer.readerIndex()).isEqualTo(3);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
        } finally {
            buffer.release();
        }
    }

    private CodecTracker tracker(boolean tracked) {
        return tracked ? new CodecTracker() : null;
    }

    private static CodecTraceNode child(CodecTraceNode parent, String name) {
        return parent.getChildren().stream().filter(node -> name.equals(node.getName())).findFirst().orElseThrow();
    }

    private static void assertRange(CodecTraceNode node, int start, int end, String hex) {
        assertThat(node.getByteStart()).as("%s 起点", node.getPath()).isEqualTo(start);
        assertThat(node.getByteEnd()).as("%s 终点", node.getPath()).isEqualTo(end);
        assertThat(node.getHex()).isEqualTo(hex);
    }

    public record Text(@Preset.RustStyle.str @Nullable String text) {
    }

    public record NumberValue(@Preset.RustStyle.u16 int value) {
    }

    public record FixedPacket(@Preset.RustStyle.struct(length = 2) Text body, @Preset.RustStyle.u8 short tail) {
    }

    public record ExpressionPacket(@Preset.RustStyle.struct(lengthExpression = "2") Text body, @Preset.RustStyle.u8 short tail) {
    }

    public record PrefixedPacket(@Preset.RustStyle.struct(prependLengthFieldType = PrependLengthFieldType.u8) Text body,
                                 @Preset.RustStyle.u8 short tail) {
    }

    public record NumericPacket(@Preset.RustStyle.struct(prependLengthFieldType = PrependLengthFieldType.u8) NumberValue body,
                                @Preset.RustStyle.u8 short tail) {
    }

    public record UnboundedPacket(@Preset.RustStyle.struct NumberValue body, @Preset.RustStyle.u8 short tail) {
    }

    public record DeepPacket(@Preset.RustStyle.struct(prependLengthFieldType = PrependLengthFieldType.u8) PrefixedPacket body,
                             @Preset.RustStyle.u8 short tail) {
    }
}
