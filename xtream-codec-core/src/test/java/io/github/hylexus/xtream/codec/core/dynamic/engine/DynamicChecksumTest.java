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

import io.github.hylexus.xtream.codec.core.dynamic.schema.*;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 校验和已知向量、前后置回填、重复作用域与报文损坏测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicChecksumTest {
    private static DynamicField checksum(String id, String algorithm, String range) {
        final var value = DynamicChecksumAlgorithm.from(algorithm);
        return DynamicField.field(id, "u" + value.bits())
                .generated(DynamicGeneratedValue.checksum(new DynamicChecksum(value, range))).build();
    }

    @ParameterizedTest
    @CsvSource({"xor8,31", "sum8,dd", "crc32,cbf43926", "crc32c,e3069283"})
    void shouldMatchIndependentKnownVectorsBeforeAndAfterPayload(String algorithm, String expected) {
        final DynamicField body = DynamicField.field("body", "string").length(DynamicLength.fixed(9, "byte")).build();
        for (final boolean prefix : List.of(false, true)) {
            final DynamicField check = checksum("check", algorithm, "body");
            final DynamicSchema schema = prefix ? DynamicSchema.of(check, body) : DynamicSchema.of(body, check);
            assertEquals(schema, DynamicSchemaJson.read(DynamicSchemaJson.write(schema)));
            final DynamicPlan plan = DynamicCompiler.compile(schema);
            final ByteBuf buffer = Unpooled.buffer();
            try {
                buffer.writeByte(0x7e);
                final int start = buffer.writerIndex();
                plan.encode(DynamicValues.of(Map.of("body", "123456789")), buffer);
                final String payload = "313233343536373839";
                assertEquals(prefix ? expected + payload : payload + expected, ByteBufUtil.hexDump(buffer, start, buffer.writerIndex() - start));
                buffer.readerIndex(start);
                final var decoded = plan.decode(buffer);
                assertEquals("123456789", decoded.values().fields().get("body"));
                assertEquals(1, decoded.checksums().size());
                assertTrue(decoded.checksums().getFirst().matched());
                buffer.readerIndex(start);
                buffer.setByte(start + (prefix ? valueBits(algorithm) / 8 : 0), '0');
                final var reported = plan.decode(buffer);
                assertEquals("023456789", reported.values().fields().get("body"));
                assertEquals(1, reported.checksums().size());
                final var result = reported.checksums().getFirst();
                assertEquals("$.check", result.fieldPath());
                assertEquals(DynamicChecksumAlgorithm.from(algorithm), result.algorithm());
                assertEquals(Long.parseUnsignedLong(expected, 16), result.actual());
                assertFalse(result.matched());
                assertEquals(buffer.writerIndex(), buffer.readerIndex());
                assertThrows(UnsupportedOperationException.class, () -> reported.checksums().clear());
                buffer.readerIndex(start);
                final var error = assertThrows(DynamicCodecException.class, () -> plan.decode(buffer, DynamicDecodeOptions.STRICT));
                assertEquals("$.check", error.fieldPath());
                assertTrue(error.getMessage().contains("校验和不匹配"));
                assertEquals(start, buffer.readerIndex());
            } finally {
                buffer.release();
            }
        }
    }

    private static int valueBits(String algorithm) {
        return DynamicChecksumAlgorithm.from(algorithm).bits();
    }

    @Test
    void shouldComputeAfterLengthBackfillAndUpdateTrace() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                checksum("check", "xor8", "length..body"),
                DynamicField.field("length", "u8").computedLengthOf("body").build(),
                DynamicField.field("body", "hex").length(DynamicLength.ref("length", "byte")).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            final CodecTracker tracker = new CodecTracker();
            plan.encode(DynamicValues.of(Map.of("body", "aabb")), buffer, tracker);
            assertEquals("1302aabb", ByteBufUtil.hexDump(buffer));
            assertEquals(19L, tracker.getTrace().getRoot().getChildren().getFirst().getValue());
            assertEquals(Map.of("check", 19L, "length", 2L, "body", "aabb"), plan.decode(buffer).values().fields());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldIsolateRepeatedChecksumsAndUseLittleEndian() {
        final DynamicField item = DynamicField.field("item", "struct").fields(
                DynamicField.field("body", "string").length(DynamicLength.fixed(9, "byte")).build(),
                DynamicField.field("check", "u32").endian("little")
                        .generated(DynamicGeneratedValue.checksum(new DynamicChecksum(DynamicChecksumAlgorithm.CRC32, "body"))).build()).build();
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("items", "array").length(DynamicLength.fixed(2, "item")).item(item).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("items", List.of(Map.of("body", "123456789"), Map.of("body", "123456789")))), buffer);
            assertEquals("3132333435363738392639f4cb".repeat(2), ByteBufUtil.hexDump(buffer));
            final var decoded = plan.decode(buffer);
            assertEquals(2, ((List<?>) decoded.values().fields().get("items")).size());
            assertEquals(2, decoded.checksums().size());
            assertTrue(decoded.checksums().stream().allMatch(DynamicChecksumResult::matched));
            buffer.readerIndex(0);
            buffer.setByte(0, '0');
            final var corrupted = plan.decode(buffer);
            assertEquals(2, corrupted.checksums().size());
            assertFalse(corrupted.checksums().getFirst().matched());
            assertTrue(corrupted.checksums().getLast().matched());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldRejectInvalidRangesUnalignedIntervalsAndPendingDependencies() {
        final DynamicField scalar = DynamicField.field("body", "u8").build();
        assertThrows(DynamicCodecException.class, () -> DynamicCompiler.compile(DynamicSchema.of(
                checksum("check", "xor8", "check..body"), scalar)));
        assertThrows(DynamicCodecException.class, () -> DynamicCompiler.compile(DynamicSchema.of(
                checksum("check", "xor8", "missing"), scalar)));
        assertThrows(DynamicCodecException.class, () -> DynamicCompiler.compile(DynamicSchema.of(
                checksum("check", "xor8", "length"),
                DynamicField.field("length", "u8").computedLengthOf("body").build(), scalar)));
        assertThrows(DynamicCodecException.class, () -> DynamicCompiler.compile(DynamicSchema.of(
                checksum("a", "xor8", "b"), checksum("b", "xor8", "a"))));
        assertThrows(DynamicCodecException.class, () -> DynamicCompiler.compile(DynamicSchema.of(
                checksum("check", "xor8", "body"),
                DynamicField.field("n", "u8").generated(DynamicGeneratedValue.expression(DynamicExpression.ref("check"))).build(),
                scalar)));
        final DynamicPlan unaligned = DynamicCompiler.compile(DynamicSchema.of(
                checksum("check", "xor8", "part"), DynamicField.field("part", "u3").build(),
                DynamicField.field("rest", "u5").build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            assertThrows(DynamicCodecException.class, () -> unaligned.encode(DynamicValues.of(Map.of("part", 1, "rest", 0)), buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldIncludeNestedLengthPrefixesAndSupportCompositeBuffers() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("body", "struct").length(DynamicLength.prefix(8, "byte")).fields(
                        DynamicField.field("length", "u8").computedLengthOf("raw").build(),
                        DynamicField.field("raw", "hex").length(DynamicLength.ref("length", "byte")).build()).build(),
                checksum("check", "xor8", "body")));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("body", Map.of("raw", "aabb"))), buffer);
            assertEquals("0302aabb10", ByteBufUtil.hexDump(buffer));
            final ByteBuf composite = Unpooled.wrappedBuffer(
                    new byte[]{3, 2}, new byte[]{(byte) 0xaa}, new byte[]{(byte) 0xbb, 0x10});
            try {
                assertEquals(Map.of("body", Map.of("length", 2L, "raw", "aabb"), "check", 16L),
                        plan.decode(composite).values().fields());
            } finally {
                composite.release();
            }
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldHandleConditionalEndpointsAndSkippedChecksum() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("flag", "u8").build(),
                checksum("check", "xor8", "a..b"),
                DynamicField.field("a", "u8").when(new DynamicCondition("flag", "eq", "1")).build(),
                DynamicField.field("b", "u8").when(new DynamicCondition("flag", "eq", "1")).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("flag", 0)), buffer);
            assertEquals("0000", ByteBufUtil.hexDump(buffer));
            assertEquals(Map.of("flag", 0L, "check", 0L), plan.decode(buffer).values().fields());
        } finally {
            buffer.release();
        }
        final DynamicPlan skipped = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("flag", "u8").build(),
                DynamicField.field("check", "u8").generated(DynamicGeneratedValue.checksum(
                        new DynamicChecksum(DynamicChecksumAlgorithm.XOR8, "body")))
                        .when(new DynamicCondition("flag", "eq", "1")).build(),
                DynamicField.field("body", "u8").build()));
        final ByteBuf input = Unpooled.wrappedBuffer(new byte[]{0, 7});
        try {
            final var decoded = skipped.decode(input, DynamicDecodeOptions.STRICT);
            assertEquals(Map.of("flag", 0L, "body", 7L), decoded.values().fields());
            assertTrue(decoded.checksums().isEmpty());
        } finally {
            input.release();
        }
    }

    @Test
    void shouldReportEveryChecksumAndKeepWarningsSeparateFromFailures() {
        final DynamicField body = DynamicField.field("body", "u8").build();
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                checksum("first", "xor8", "body"), body, checksum("last", "sum8", "body")));
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{0x7e, 1, 3, 2, 0x55});
        try {
            buffer.readerIndex(1);
            final CodecTracker tracker = new CodecTracker();
            final var decoded = plan.decode(buffer, tracker);
            assertEquals(Map.of("first", 1L, "body", 3L, "last", 2L), decoded.values().fields());
            assertEquals(List.of(
                    new DynamicChecksumResult("$.first", DynamicChecksumAlgorithm.XOR8, 1, 3),
                    new DynamicChecksumResult("$.last", DynamicChecksumAlgorithm.SUM8, 2, 3)), decoded.checksums());
            assertEquals(4, buffer.readerIndex());
            assertEquals(1, decoded.remainingBytes());
            assertEquals(2, tracker.getTrace().getDiagnostics().size());
            assertTrue(tracker.getTrace().getDiagnostics().stream().allMatch(diagnostic -> "WARNING".equals(diagnostic.level())));
            assertEquals(io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus.SUCCESS, tracker.getTrace().getRoot().getStatus());
            buffer.readerIndex(1);
            final var error = assertThrows(DynamicCodecException.class,
                    () -> plan.decode(buffer, DynamicDecodeOptions.STRICT.withLimits(DynamicLimits.DEFAULT), tracker));
            assertEquals("$.first", error.fieldPath());
            assertEquals(1, buffer.readerIndex());
            assertEquals("ERROR", tracker.getTrace().getDiagnostics().getFirst().level());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldStillRejectStructuralErrorsAfterChecksumWarnings() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("body", "u8").build(), checksum("check", "xor8", "body"),
                DynamicField.field("tail", "u16").build()));
        final ByteBuf input = Unpooled.wrappedBuffer(new byte[]{3, 1});
        try {
            for (final var options : List.of(DynamicDecodeOptions.DEFAULT, DynamicDecodeOptions.STRICT)) {
                final CodecTracker tracker = new CodecTracker();
                final var error = assertThrows(DynamicCodecException.class, () -> plan.decode(input, options, tracker));
                assertEquals(options.strictChecksums() ? "$.check" : "$.tail", error.fieldPath());
                assertEquals(0, input.readerIndex());
                assertTrue(tracker.getTrace().getDiagnostics().stream().anyMatch(diagnostic -> "ERROR".equals(diagnostic.level())));
                if (!options.strictChecksums()) {
                    assertEquals("WARNING", tracker.getTrace().getDiagnostics().getFirst().level());
                }
            }
        } finally {
            input.release();
        }
    }

    @Test
    void shouldApplyResourceLimitsInBothModes() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("body", "u8").build(), checksum("check", "xor8", "body")));
        final ByteBuf input = Unpooled.wrappedBuffer(new byte[]{3, 3});
        try {
            final DynamicLimits limits = new DynamicLimits(1, 1024, 100, 10);
            for (final var options : List.of(DynamicDecodeOptions.DEFAULT, DynamicDecodeOptions.STRICT)) {
                assertThrows(DynamicCodecException.class, () -> plan.decode(input, options.withLimits(limits)));
                assertEquals(0, input.readerIndex());
            }
        } finally {
            input.release();
        }
    }
}
