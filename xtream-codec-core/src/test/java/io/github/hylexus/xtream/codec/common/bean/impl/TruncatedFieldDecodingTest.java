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

import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNodeKind;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class TruncatedFieldDecodingTest extends BaseEntityCodecTest {

    static Stream<Arguments> truncatedFields() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, FixedRecord.class, "", 1),
                Arguments.of(tracked, FixedRecord.class, "12", 1),
                Arguments.of(tracked, PrimitiveRecord.class, "", 1),
                Arguments.of(tracked, FixedPojo.class, "", 1),
                Arguments.of(tracked, FixedPojo.class, "12", 1),
                Arguments.of(tracked, TextRecord.class, "", 1),
                Arguments.of(tracked, TextRecord.class, "41", 1),
                Arguments.of(tracked, BytesRecord.class, "", 1),
                Arguments.of(tracked, BytesRecord.class, "41", 1),
                Arguments.of(tracked, PrefixedBytesRecord.class, "", 1),
                Arguments.of(tracked, PrefixedBytesRecord.class, "00", 1),
                Arguments.of(tracked, PrefixedBytesRecord.class, "0002", 3),
                Arguments.of(tracked, PrefixedBytesRecord.class, "000241", 3),
                Arguments.of(tracked, BufferPojo.class, "", 1),
                Arguments.of(tracked, BufferPojo.class, "12", 1)));
    }

    @ParameterizedTest
    @MethodSource("truncatedFields")
    void rejectsMissingOrPartialField(boolean tracked, Class<?> type, String hex, int expectedReaderIndex) {
        final ByteBuf input = allocator.buffer().writeByte(0x55).writeBytes(ByteBufUtil.decodeHexDump(hex)).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            assertThatThrownBy(() -> entityCodec.decode(XtreamField.ALL_VERSION, type, input, tracker))
                    .isInstanceOf(IndexOutOfBoundsException.class);
            assertThat(input.readerIndex()).isEqualTo(expectedReaderIndex);
            assertThat(input.getUnsignedByte(0)).isEqualTo((short) 0x55);
            if (tracked) {
                assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
                    assertThat(diagnostic.exceptionType()).isEqualTo(IndexOutOfBoundsException.class.getName());
                    assertThat(diagnostic.byteOffset()).isEqualTo(expectedReaderIndex - 1);
                });
                if (type != PrefixedBytesRecord.class || hex.length() >= 4) {
                    assertThat(tracker.getTrace().getRoot().getChildren()).filteredOn("name", "value")
                            .singleElement().satisfies(field -> {
                                assertThat(field.getKind()).isEqualTo(CodecTraceNodeKind.FIELD);
                                assertThat(field.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                                assertThat(field.getByteStart()).isEqualTo(expectedReaderIndex - 1);
                                assertThat(field.getDiagnostics()).containsExactlyElementsOf(tracker.getTrace().getDiagnostics());
                                assertThat(field.getDiagnostics().getFirst().nodeId()).isEqualTo(field.getId());
                            });
                }
            }
        } finally {
            input.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, 01", "false, 0112", "true, 01", "true, 0112"})
    void collectionStopsAtIncompleteRecordWithoutReadingTail(boolean tracked, String content) {
        final ByteBuf input = allocator.buffer().writeByte(0x55).writeByte(content.length() / 2)
                .writeBytes(ByteBufUtil.decodeHexDump(content)).writeByte(0x7e).readerIndex(1);
        final int tailIndex = input.writerIndex() - 1;
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            assertThatThrownBy(() -> entityCodec.decode(XtreamField.ALL_VERSION,
                    NestedAndSequenceMetadataTest.RecordPacket.class, input, tracker))
                    .isInstanceOf(IndexOutOfBoundsException.class);
            assertThat(input.readerIndex()).isEqualTo(tailIndex);
            assertThat(input.readUnsignedByte()).isEqualTo((short) 126);
            if (tracked) {
                final var collection = tracker.getTrace().getRoot().getChildren().stream()
                        .filter(node -> node.getName().equals("entries")).findFirst().orElseThrow();
                assertThat(collection.getChildren()).hasSize(1);
                assertThat(tracker.getTrace().getDiagnostics()).isNotEmpty();
            }
        } finally {
            input.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, 01", "false, 0112", "true, 01", "true, 0112"})
    void nestedPojoStopsAtIncompleteFieldWithoutReadingTail(boolean tracked, String content) {
        final ByteBuf input = allocator.buffer().writeByte(content.length() / 2)
                .writeBytes(ByteBufUtil.decodeHexDump(content)).writeByte(0x7e);
        final int tailIndex = input.writerIndex() - 1;
        try {
            assertThatThrownBy(() -> entityCodec.decode(XtreamField.ALL_VERSION,
                    NestedAndSequenceMetadataTest.BoundedPojoContainer.class, input, tracked ? new CodecTracker() : null))
                    .isInstanceOf(IndexOutOfBoundsException.class);
            assertThat(input.readerIndex()).isEqualTo(tailIndex);
            assertThat(input.readUnsignedByte()).isEqualTo((short) 126);
        } finally {
            input.release();
        }
    }

    static Stream<Arguments> completeFields() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, new FixedRecord(0x1234), "1234"),
                Arguments.of(tracked, new TextRecord("AB"), "4142"),
                Arguments.of(tracked, new BytesRecord(new byte[]{0x41, 0x42}), "4142"),
                Arguments.of(tracked, new PrefixedBytesRecord(new byte[]{0x41, 0x42}), "00024142"),
                Arguments.of(tracked, new PrefixedBytesRecord(new byte[0]), "0000"),
                Arguments.of(tracked, new ZeroBytesRecord(new byte[0]), ""),
                Arguments.of(tracked, new ZeroTextRecord(null), ""),
                Arguments.of(tracked, new RemainingBytesRecord(new byte[0]), ""),
                Arguments.of(tracked, new RemainingTextRecord(null), "")));
    }

    @ParameterizedTest
    @MethodSource("completeFields")
    void completeAndEmptyContentKeepsCodecSemantics(boolean tracked, Object expected, String hex) {
        final ByteBuf input = allocator.buffer().writeByte(0x55).writeBytes(ByteBufUtil.decodeHexDump(hex)).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final Object result = entityCodec.decode(XtreamField.ALL_VERSION, expected.getClass(), input, tracker);
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
            assertThat(input.readerIndex()).isEqualTo(input.writerIndex());
            if (tracked) {
                assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
                assertThat(tracker.getTrace().getRoot().getChildren()).filteredOn("name", "value")
                        .singleElement().satisfies(field -> {
                            assertThat(field.getKind()).isEqualTo(CodecTraceNodeKind.FIELD);
                            assertThat(field.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                            assertThat(field.getChildren()).isEmpty();
                        });
            }
        } finally {
            input.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void falseConditionAllowsMissingFieldAtEndOfInput(boolean tracked) {
        final ByteBuf input = allocator.buffer().writeByte(0);
        try {
            final ConditionalRecord result = entityCodec.decode(XtreamField.ALL_VERSION, ConditionalRecord.class, input,
                    tracked ? new CodecTracker() : null);
            assertThat(result).isEqualTo(new ConditionalRecord((short) 0, null));
            assertThat(input.readerIndex()).isEqualTo(1);
        } finally {
            input.release();
        }
    }

    public record FixedRecord(@Preset.RustStyle.u16 Integer value) {
    }

    public record PrimitiveRecord(@Preset.RustStyle.u16 int value) {
    }

    @Getter
    @Setter
    public static class FixedPojo {
        @Preset.RustStyle.u16
        private Integer value;
    }

    @Getter
    @Setter
    public static class BufferPojo {
        @XtreamField(length = 2)
        private ByteBuf value;
    }

    public record TextRecord(@Preset.RustStyle.str(length = 2, charset = "UTF-8") String value) {
    }

    public record BytesRecord(@Preset.RustStyle.byte_array(length = 2) byte[] value) {
    }

    public record PrefixedBytesRecord(
            @Preset.RustStyle.byte_array(prependLengthFieldType = PrependLengthFieldType.u16) byte[] value) {
    }

    public record ZeroBytesRecord(@Preset.RustStyle.byte_array(lengthExpression = "0") byte[] value) {
    }

    public record ZeroTextRecord(@Preset.RustStyle.str(lengthExpression = "0") String value) {
    }

    public record RemainingBytesRecord(@Preset.RustStyle.byte_array(lengthExpression = "-1") byte[] value) {
    }

    public record RemainingTextRecord(@Preset.RustStyle.str String value) {
    }

    public record ConditionalRecord(
            @Preset.RustStyle.u8 Short status,
            @Preset.RustStyle.u16(condition = "status == 1") Integer value) {
    }
}
