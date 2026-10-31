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

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicChecksum;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicChecksumAlgorithm;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicGeneratedValue;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 不同缓冲区形态的长度与校验和回填、游标和所有权测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicBufferFormsTest {
    private static final String BODY = "313233343536373839";

    enum Form {
        HEAP, DIRECT, COMPOSITE, SLICE, READ_ONLY
    }

    static Stream<Arguments> inputs() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(Form.values()).flatMap(form ->
                Stream.of(false, true).flatMap(corrupt -> Stream.of(
                        Arguments.of(tracked, form, corrupt, DynamicChecksumAlgorithm.CRC32, "cbf43926"),
                        Arguments.of(tracked, form, corrupt, DynamicChecksumAlgorithm.CRC32C, "e3069283")))));
    }

    @ParameterizedTest
    @MethodSource("inputs")
    void decodesAndRejectsCorruptedChecksumsWithoutChangingOwnership(
            boolean tracked, Form form, boolean corrupt, DynamicChecksumAlgorithm algorithm, String crcHex) {

        final String wireHex = "55" + crcHex + "0009" + BODY + "7f66";
        final byte[] bytes = ByteBufUtil.decodeHexDump(wireHex);
        if (corrupt) {
            bytes[7] = '0';
        }
        final Buffer fixture = input(form, bytes);
        final ByteBuf buffer = fixture.view();
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            buffer.readerIndex(1);
            final DynamicPlan plan = plan(algorithm);
            if (corrupt) {
                final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                        () -> plan.decode(buffer, DynamicDecodeOptions.STRICT, tracker));
                assertThat(error).hasMessageContaining("校验和不匹配");
                assertThat(error.fieldPath()).isEqualTo("$.crc");
                assertThat(error.bitOffset()).isEqualTo(120);
                assertThat(buffer.readerIndex()).isEqualTo(1);
                if (tracker != null) {
                    assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                    assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
                        assertThat(diagnostic.level()).isEqualTo("ERROR");
                        assertThat(diagnostic.bitOffset()).isEqualTo(120);
                    });
                    assertThat(tracker.getTrace().getRoot().getChildren()).extracting("name").doesNotContain("tail");
                    assertThat(tracker.isNotTracing()).isTrue();
                }
            } else {
                final var result = plan.decode(buffer, DynamicDecodeOptions.STRICT, tracker);
                assertDecoded(result, algorithm, crcHex);
                assertThat(buffer.readerIndex()).isEqualTo(17);
                assertThat(buffer.getUnsignedByte(17)).isEqualTo((short) 0x66);
                if (tracker != null) {
                    assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                    assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
                }
            }
            assertThat(buffer.writerIndex()).isEqualTo(18);
            assertThat(ByteBufUtil.hexDump(buffer, 0, 18)).isEqualTo(ByteBufUtil.hexDump(bytes));
            assertThat(buffer.refCnt()).isEqualTo(1);
            assertThat(fixture.owner().refCnt()).isEqualTo(1);
            if (form == Form.SLICE || form == Form.READ_ONLY) {
                assertThat(fixture.owner().readerIndex()).isZero();
                assertThat(fixture.owner().writerIndex()).isEqualTo(form == Form.SLICE ? 22 : 18);
            }
        } finally {
            fixture.owner().release();
        }
        assertThat(buffer.refCnt()).isZero();
    }

    static Stream<Arguments> outputs() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(Form.values())
                .filter(form -> form != Form.READ_ONLY).flatMap(form -> Stream.of(
                        Arguments.of(tracked, form, DynamicChecksumAlgorithm.CRC32, "cbf43926"),
                        Arguments.of(tracked, form, DynamicChecksumAlgorithm.CRC32C, "e3069283"))));
    }

    @ParameterizedTest
    @MethodSource("outputs")
    void backfillsAcrossBufferBoundariesAndPreservesParentIndices(
            boolean tracked, Form form, DynamicChecksumAlgorithm algorithm, String crcHex) {

        final Buffer fixture = output(form);
        final ByteBuf buffer = fixture.view();
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            buffer.writeByte(0x55);
            final DynamicPlan plan = plan(algorithm);
            assertThat(plan.encode(DynamicValues.of(Map.of("body", BODY, "tail", 127)), buffer, encoder)).isEqualTo(128);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("55" + crcHex + "0009" + BODY + "7f");
            assertThat(buffer.readerIndex()).isZero();
            assertThat(buffer.writerIndex()).isEqualTo(17);
            buffer.writeByte(0x66).readerIndex(1);
            assertDecoded(plan.decode(buffer, DynamicDecodeOptions.STRICT, decoder), algorithm, crcHex);
            assertThat(buffer.readerIndex()).isEqualTo(17);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (form == Form.SLICE) {
                assertThat(fixture.owner().readerIndex()).isZero();
                assertThat(fixture.owner().writerIndex()).isEqualTo(36);
                assertThat(fixture.owner().getUnsignedByte(1)).isEqualTo((short) 0xa5);
                assertThat(fixture.owner().getUnsignedByte(34)).isEqualTo((short) 0x5a);
            }
            if (encoder != null && decoder != null) {
                assertThat(encoder.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertThat(encoder.getTrace().getRoot().getChildren()).filteredOn("name", "crc")
                        .singleElement().extracting("value").isEqualTo(Long.parseUnsignedLong(crcHex, 16));
                assertThat(encoder.getTrace().getRoot().getChildren()).filteredOn("name", "size")
                        .singleElement().extracting("value").isEqualTo(9L);
                assertThat(decoder.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
            }
        } finally {
            fixture.owner().release();
        }
        assertThat(buffer.refCnt()).isZero();
    }

    private static void assertDecoded(DynamicDecodeResult result, DynamicChecksumAlgorithm algorithm, String crcHex) {
        final long crc = Long.parseUnsignedLong(crcHex, 16);
        assertThat(result.values().fields()).isEqualTo(Map.of("crc", crc, "size", 9L, "body", BODY, "tail", 127L));
        assertThat(result.consumedBits()).isEqualTo(128);
        assertThat(result.consumedBytes()).isEqualTo(16);
        assertThat(result.remainingBytes()).isEqualTo(1);
        assertThat(result.checksums()).containsExactly(new DynamicChecksumResult("$.crc", algorithm, crc, crc));
        assertThat(result.checksums().getFirst().matched()).isTrue();
    }

    private static DynamicPlan plan(DynamicChecksumAlgorithm algorithm) {
        return DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("crc", "u32").generated(DynamicGeneratedValue.checksum(new DynamicChecksum(algorithm, "body"))).build(),
                DynamicField.field("size", "u16").computedLengthOf("body").build(),
                DynamicField.field("body", "hex").length(DynamicLength.ref("size", "byte")).build(),
                DynamicField.field("tail", "u8").build()));
    }

    private static Buffer input(Form form, byte[] bytes) {
        return switch (form) {
            case HEAP -> same(Unpooled.wrappedBuffer(bytes));
            case DIRECT -> same(Unpooled.directBuffer(bytes.length).writeBytes(bytes));
            case COMPOSITE -> same(Unpooled.wrappedBuffer(
                    Arrays.copyOfRange(bytes, 0, 3), Arrays.copyOfRange(bytes, 3, 8), Arrays.copyOfRange(bytes, 8, bytes.length)));
            case SLICE -> {
                final ByteBuf owner = Unpooled.buffer().writeShort(0xaabb).writeBytes(bytes).writeShort(0xccdd);
                yield new Buffer(owner, owner.slice(2, bytes.length));
            }
            case READ_ONLY -> {
                final ByteBuf owner = Unpooled.wrappedBuffer(bytes);
                yield new Buffer(owner, owner.asReadOnly());
            }
        };
    }

    private static Buffer output(Form form) {
        return switch (form) {
            case HEAP -> same(Unpooled.buffer(1));
            case DIRECT -> same(Unpooled.directBuffer(1));
            case COMPOSITE -> same(Unpooled.compositeBuffer().addComponents(false,
                    Unpooled.buffer(3).writeZero(3), Unpooled.buffer(5).writeZero(5), Unpooled.buffer(24).writeZero(24)));
            case SLICE -> {
                final ByteBuf owner = Unpooled.buffer(36).writeZero(36).setByte(1, 0xa5).setByte(34, 0x5a);
                yield new Buffer(owner, owner.slice(2, 32).clear());
            }
            case READ_ONLY -> throw new IllegalArgumentException("只读缓冲区不能作为输出");
        };
    }

    private static Buffer same(ByteBuf buffer) {
        return new Buffer(buffer, buffer);
    }

    private record Buffer(ByteBuf owner, ByteBuf view) {
    }
}
