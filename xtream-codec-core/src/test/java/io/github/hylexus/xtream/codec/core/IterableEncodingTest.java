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
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceDirection;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNodeKind;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static io.github.hylexus.xtream.codec.core.type.simple.DataFields.*;
import static org.assertj.core.api.Assertions.*;

/**
 * 公开入口的批量编码、版本传递及整批跟踪测试。
 *
 * @author Codex (AI)
 */
class IterableEncodingTest extends BaseEntityCodecTest {

    static Stream<Arguments> batches() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, List.of(new NumberRecord(0x1234), new NumberPojo(0x5678)), "12345678", 2),
                Arguments.of(tracked, List.of(u8("first", (short) 0x12), u16("second", 0x3456)), "123456", 2),
                Arguments.of(tracked, List.of(u8("first", (short) 0x12), new NumberRecord(0x3456),
                        u8("last", (short) 0x78)), "12345678", 3),
                Arguments.of(tracked, List.of(new NumberPojo(0x1234), u16("last", 0x5678)), "12345678", 2)));
    }

    @ParameterizedTest
    @MethodSource("batches")
    void encodesEveryElementInOrderAndKeepsBatchTrace(boolean tracked, List<?> items, String expected, int fieldCount) {
        final ByteBuf output = allocator.buffer().writeByte(0x55).readerIndex(1);
        final CodecTracker tracker = new CodecTracker();
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, items, output, tracked ? tracker : null);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55" + expected);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.writerIndex()).isEqualTo(1 + expected.length() / 2);
            if (tracked) {
                assertBatchTrace(tracker, expected, fieldCount);
            }
        } finally {
            output.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, 1, 1213", "true, 1, 1213", "false, 2, 34565678", "true, 2, 34565678"})
    void passesRequestedVersionToEveryEntity(boolean tracked, int version, String expected) {
        final List<VersionedRecord> items = List.of(new VersionedRecord(0x12, 0x3456), new VersionedRecord(0x13, 0x5678));
        final ByteBuf output = allocator.buffer().writeByte(0x55).readerIndex(1);
        final CodecTracker tracker = new CodecTracker();
        try {
            entityCodec.encode(version, items, output, tracked ? tracker : null);
            assertThat(ByteBufUtil.hexDump(output)).isEqualTo(expected);
            if (tracked) {
                assertBatchTrace(tracker, expected, 2);
            }
        } finally {
            output.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void emptyIterableDoesNotWriteBytes(boolean tracked) {
        final ByteBuf output = allocator.buffer().writeByte(0x55).readerIndex(1);
        final CodecTracker tracker = new CodecTracker();
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, List.of(), output, tracked ? tracker : null);
            assertThat(output.writerIndex()).isEqualTo(1);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.getUnsignedByte(0)).isEqualTo((short) 0x55);
            assertThat(tracker.getTrace().getRoot().getChildren()).isEmpty();
            assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
            assertThat(tracker.isNotTracing()).isTrue();
        } finally {
            output.release();
        }
    }

    @Test
    void appendsToActiveTraceWithoutClosingItsParentScope() {
        final ByteBuf output = allocator.buffer().writeByte(0x55).readerIndex(1);
        final CodecTracker tracker = new CodecTracker();
        tracker.beginEncode(0, "outer");
        try {
            try (final CodecTracker.TraceScope batch = tracker.enterScope(
                    CodecTraceNodeKind.NESTED_FIELD, "batch", null, null, null, 1)) {
                entityCodec.encode(XtreamField.ALL_VERSION,
                        List.of(new NumberRecord(0x1234), u8("last", (short) 0x7e)), output, tracker);
                assertThat(tracker.isNotTracing()).isFalse();
                batch.complete(null, output, output.writerIndex());
            }
            tracker.finishTrace("5512347e", output.writerIndex());
            assertThat(tracker.getTrace().getRoot().getChildren()).singleElement().satisfies(batch -> {
                assertThat(batch.getName()).isEqualTo("batch");
                assertThat(batch.getByteStart()).isEqualTo(1);
                assertThat(batch.getByteEnd()).isEqualTo(4);
                assertThat(batch.getChildren()).extracting("hex").containsExactly("1234", "7e");
                assertThat(batch.getChildren()).extracting("byteStart").containsExactly(1, 3);
                assertThat(batch.getChildren()).extracting("byteEnd").containsExactly(3, 4);
            });
            assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
            assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("5512347e");
        } finally {
            output.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void stopsAtFailedElementAndKeepsEarlierFields(boolean tracked) {
        final ByteBuf output = allocator.buffer().writeByte(0x55).readerIndex(1);
        final CodecTracker tracker = new CodecTracker();
        try {
            assertThatThrownBy(() -> entityCodec.encode(XtreamField.ALL_VERSION,
                    List.of(new NumberRecord(0x1234), new FailingPojo(), u8("tail", (short) 0x7e)), output,
                    tracked ? tracker : null)).isSameAs(FailingCodec.FAILURE);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("551234");
            assertThat(output.readerIndex()).isEqualTo(1);
            if (tracked) {
                assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(tracker.getTrace().getRoot().getChildren()).extracting("name").containsExactly("value", "failed");
                assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
                    assertThat(diagnostic.exceptionType()).isEqualTo(IllegalStateException.class.getName());
                    assertThat(diagnostic.byteOffset()).isEqualTo(2);
                    assertThat(diagnostic.nodeId()).isEqualTo(tracker.getTrace().getRoot().getChildren().get(1).getId());
                });
                assertThat(tracker.isNotTracing()).isTrue();
            }
        } finally {
            output.release();
        }
    }

    private static void assertBatchTrace(CodecTracker tracker, String expected, int fieldCount) {
        assertThat(tracker.getTrace().getDirection()).isEqualTo(CodecTraceDirection.ENCODE);
        assertThat(tracker.getTrace().getPayloadHex()).isEqualTo(expected);
        assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
        assertThat(tracker.getTrace().getRoot().getByteStart()).isZero();
        assertThat(tracker.getTrace().getRoot().getByteEnd()).isEqualTo(expected.length() / 2);
        assertThat(tracker.getTrace().getRoot().getChildren()).hasSize(fieldCount);
        int offset = 0;
        for (final var field : tracker.getTrace().getRoot().getChildren()) {
            assertThat(field.getByteStart()).isEqualTo(offset);
            assertThat(field.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
            offset = field.getByteEnd();
            assertThat(field.getHex()).isEqualTo(expected.substring(field.getByteStart() * 2, offset * 2));
        }
        assertThat(offset).isEqualTo(expected.length() / 2);
        assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
        assertThat(tracker.isNotTracing()).isTrue();
    }

    public record NumberRecord(@Preset.RustStyle.u16 int value) {
    }

    @Getter
    @Setter
    public static class NumberPojo {
        @Preset.RustStyle.u16
        private int value;

        public NumberPojo() {
        }

        public NumberPojo(int value) {
            this.value = value;
        }
    }

    public record VersionedRecord(
            @Preset.RustStyle.u8(version = 1) int first,
            @Preset.RustStyle.u16(version = 2) int second) {
    }

    @Getter
    @Setter
    public static class FailingPojo {
        @XtreamField(fieldCodec = FailingCodec.class, length = 1)
        private int failed = 1;
    }

    public static class FailingCodec implements FieldCodec<Integer> {
        private static final IllegalStateException FAILURE = new IllegalStateException("批量编码失败");

        @Override
        public Integer deserialize(BeanPropertyMetadata metadata, DeserializeContext context, ByteBuf input, int length) {
            return (int) input.readUnsignedByte();
        }

        @Override
        public void serialize(BeanPropertyMetadata metadata, SerializeContext context, ByteBuf output, Integer value) {
            throw FAILURE;
        }
    }
}
