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


package io.github.hylexus.xtream.codec.core.dynamic.engine;

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicExpression;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLengthUnit;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 运行时位长度、结构消费不足和错误填充报文的边界测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicLengthFailureTest {
    static Stream<Arguments> bitLengths() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of("ref", "expression").flatMap(source ->
                Stream.of(0, 7, 8, 9, 16).map(bits -> Arguments.of(tracked, source, bits))));
    }

    @ParameterizedTest
    @MethodSource("bitLengths")
    void requiresReferencedAndComputedByteLengthsToBeWholeBytes(boolean tracked, String source, int bits) {
        final DynamicLength length = source.equals("ref") ? DynamicLength.ref("size", "bit")
                : DynamicLength.expression(DynamicExpression.ref("size"), DynamicLengthUnit.BIT);
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(), DynamicField.field("size", "u8").build(),
                DynamicField.field("body", "hex").length(length).build(), DynamicField.field("tail", "u8").build()));
        final String body = bits == 0 ? "" : bits <= 8 ? "aa" : "aabb";
        final String header = "7e" + String.format("%02x", bits);
        final String wire = "55" + header + body + "7f66";
        final ByteBuf input = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(wire)).readerIndex(1);
        final ByteBuf output = Unpooled.buffer().writeByte(0x55).readerIndex(1);
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        try {
            if (bits % 8 == 0) {
                final var result = plan.decode(input, decoder);
                assertThat(result.values().fields()).isEqualTo(Map.of("head", 126L, "size", (long) bits, "body", body, "tail", 127L));
                assertThat(result.consumedBytes()).isEqualTo(3 + bits / 8);
                assertThat(result.remainingBytes()).isEqualTo(1);
                assertThat(input.readerIndex()).isEqualTo(4 + bits / 8);
                plan.encode(DynamicValues.of(Map.of("head", 126, "size", bits, "body", body, "tail", 127)), output, encoder);
                assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55" + header + body + "7f");
                assertSuccess(decoder);
                assertSuccess(encoder);
            } else {
                final DynamicCodecException readError = catchThrowableOfType(DynamicCodecException.class, () -> plan.decode(input, decoder));
                assertFailure(readError, "$.body", 16, "完整字节", decoder);
                assertThat(input.readerIndex()).isEqualTo(1);
                final DynamicCodecException writeError = catchThrowableOfType(DynamicCodecException.class,
                        () -> plan.encode(DynamicValues.of(Map.of("head", 126, "size", bits, "body", body, "tail", 127)), output, encoder));
                assertFailure(writeError, "$.body", 16, "完整字节", encoder);
                assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55" + header);
            }
            assertThat(ByteBufUtil.hexDump(input, 0, input.writerIndex())).isEqualTo(wire);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(input.refCnt()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
        } finally {
            input.release();
            output.release();
        }
    }

    static Stream<Arguments> malformedPrefixes() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(7, 9).map(bits -> Arguments.of(tracked, bits)));
    }

    @ParameterizedTest
    @MethodSource("malformedPrefixes")
    void rejectsNonByteAlignedWirePrefixesAndCanDecodeAValidPacketLater(boolean tracked, int bits) {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(),
                DynamicField.field("body", "hex").length(DynamicLength.prefix(8, "bit")).build(),
                DynamicField.field("tail", "u8").build()));
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55).writeByte(126).writeByte(bits).writeShort(0xaabb)
                .writeByte(127).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class, () -> plan.decode(buffer, tracker));
            assertFailure(error, "$.body", 16, "完整字节", tracker);
            assertThat(buffer.readerIndex()).isEqualTo(1);
            buffer.clear().writeByte(0x55).readerIndex(1);
            plan.encode(DynamicValues.of(Map.of("head", 126, "body", "aabb", "tail", 127)), buffer);
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo("557e10aabb7f");
            final var result = plan.decode(buffer, tracked ? new CodecTracker() : null);
            assertThat(result.values().fields()).isEqualTo(Map.of("head", 126L, "body", "aabb", "tail", 127L));
            assertThat(buffer.readerIndex()).isEqualTo(6);
            assertThat(buffer.refCnt()).isEqualTo(1);
        } finally {
            buffer.release();
        }
    }

    static Stream<Arguments> structureLengths() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of("fixed", "ref", "expression", "prefix")
                .map(source -> Arguments.of(tracked, source)));
    }

    @ParameterizedTest
    @MethodSource("structureLengths")
    void rejectsUnconsumedStructureBytesBeforeReadingTheOuterTail(boolean tracked, String source) {
        final DynamicLength length = switch (source) {
            case "fixed" -> DynamicLength.fixed(2, "byte");
            case "ref" -> DynamicLength.ref("size", "byte");
            case "expression" -> DynamicLength.expression(DynamicExpression.ref("size"), DynamicLengthUnit.BYTE);
            case "prefix" -> DynamicLength.prefix(8, "byte");
            default -> throw new IllegalArgumentException(source);
        };
        final DynamicField body = DynamicField.field("body", "struct").length(length)
                .fields(DynamicField.field("value", "u8").build()).build();
        final DynamicPlan plan = source.equals("ref") || source.equals("expression")
                ? DynamicCompiler.compile(DynamicSchema.of(DynamicField.field("head", "u8").build(),
                        DynamicField.field("size", "u8").build(), body, DynamicField.field("tail", "u8").build()))
                : DynamicCompiler.compile(DynamicSchema.of(DynamicField.field("head", "u8").build(),
                        body, DynamicField.field("tail", "u8").build()));
        final String wire = source.equals("fixed") ? "557eab007f66" : "557e02ab007f66";
        final ByteBuf buffer = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(wire)).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class, () -> plan.decode(buffer, tracker));
            assertFailure(error, "$.body", source.equals("fixed") ? 16 : 24, "容器未恰好消费声明的长度", tracker);
            assertThat(buffer.readerIndex()).isEqualTo(1);
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo(wire);
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (tracker != null) {
                assertThat(tracker.getTrace().getRoot().getChildren()).filteredOn("name", "body")
                        .singleElement().satisfies(node -> assertThat(node.getChildren()).singleElement().satisfies(child -> {
                            assertThat(child.getName()).isEqualTo("value");
                            assertThat(child.getValue()).isEqualTo(171L);
                            assertThat(child.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                        }));
            }
        } finally {
            buffer.release();
        }
    }

    static Stream<Arguments> corruptedPadding() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(0, 255).flatMap(pad ->
                Stream.of(0, 1, 2).map(index -> Arguments.of(tracked, pad, index))));
    }

    @ParameterizedTest
    @MethodSource("corruptedPadding")
    void rejectsCorruptionAtEitherEndOrInsidePaddingWithoutCommittingReaderIndex(boolean tracked, int pad, int index) {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(),
                DynamicField.field("pad", "padding").length(DynamicLength.fixed(3, "byte")).options(Map.of("padByte", pad)).build(),
                DynamicField.field("tail", "u8").build()));
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55).readerIndex(1);
        try {
            plan.encode(DynamicValues.of(Map.of("head", 126, "tail", 127)), buffer, tracked ? new CodecTracker() : null);
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo(pad == 0 ? "557e0000007f" : "557effffff7f");
            buffer.writeByte(0x66);
            final var valid = plan.decode(buffer, tracked ? new CodecTracker() : null);
            assertThat(valid.values().fields()).containsEntry("pad", null).containsEntry("tail", 127L);
            assertThat(valid.consumedBytes()).isEqualTo(5);
            assertThat(valid.remainingBytes()).isEqualTo(1);
            buffer.readerIndex(1).setByte(2 + index, pad ^ 0xff);
            final String corrupted = ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex());
            final CodecTracker tracker = tracked ? new CodecTracker() : null;
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class, () -> plan.decode(buffer, tracker));
            assertFailure(error, "$.pad", 32, "填充字节不匹配", tracker);
            assertThat(error).hasCauseInstanceOf(IllegalArgumentException.class);
            assertThat(buffer.readerIndex()).isEqualTo(1);
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo(corrupted);
            assertThat(buffer.refCnt()).isEqualTo(1);
        } finally {
            buffer.release();
        }
    }

    private static void assertFailure(DynamicCodecException error, String path, long offset, String message, @Nullable CodecTracker tracker) {
        assertThat(error).hasMessageContaining(message);
        assertThat(error.fieldPath()).isEqualTo(path);
        assertThat(error.bitOffset()).isEqualTo(offset);
        if (tracker != null) {
            assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
            assertThat(tracker.getTrace().getRoot().getBitEnd()).isEqualTo(offset);
            assertThat(tracker.getTrace().getRoot().getChildren()).extracting("name").doesNotContain("tail");
            assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
                assertThat(diagnostic.level()).isEqualTo("ERROR");
                assertThat(diagnostic.bitOffset()).isEqualTo(offset);
            });
            assertThat(tracker.isNotTracing()).isTrue();
        }
    }

    private static void assertSuccess(@Nullable CodecTracker tracker) {
        if (tracker != null) {
            assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
            assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
        }
    }
}
