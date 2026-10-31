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

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtension;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtensionRegistry;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNodeKind;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 字节字段前缀容量不足和扩展缺值时的提前拒绝、调用隔离及恢复测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicByteEarlyFailureTest {
    static Stream<Arguments> prefixBoundaries() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(8, 16, 32).flatMap(width ->
                Stream.of(false, true).flatMap(parent -> Stream.of("hex", "extension")
                        .map(type -> Arguments.of(tracked, width, parent, type)))));
    }

    @ParameterizedTest
    @MethodSource("prefixBoundaries")
    void rejectsAnIncompletePrefixBeforeEncodingEvenEmptyContent(boolean tracked, int width, boolean parent, String type) {
        final CountingExtension extension = new CountingExtension();
        final DynamicField.Builder data = DynamicField.field("data", type).length(DynamicLength.prefix(width, "byte"));
        if (type.equals("extension")) {
            data.options(Map.of("extensionId", "test"));
        }
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(), DynamicField.field("bound", "u8").build(),
                DynamicField.field("body", "struct").length(parent ? DynamicLength.ref("bound", "byte") : null)
                        .fields(data.build()).build()), new DynamicExtensionRegistry(Map.of("test", extension)));
        final int prefixBytes = width / 8;
        final DynamicLimits limits = parent ? DynamicLimits.DEFAULT : new DynamicLimits(64, 2 + prefixBytes - 1, 8, 8);
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                    () -> plan.encode(prefixValues(prefixBytes - 1), output, limits, tracker));
            assertThat(error).hasMessageContaining("输出超过边界或资源上限");
            assertThat(error.fieldPath()).isEqualTo("$.body.data");
            assertThat(error.bitOffset()).isEqualTo(16L);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex()))
                    .isEqualTo("55aa7e" + "%02x".formatted(prefixBytes - 1));
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.writerIndex()).isEqualTo(4);
            assertThat(output.refCnt()).isEqualTo(1);
            assertThat(extension.encodes).isZero();
            assertThat(extension.decodes).isZero();
            if (tracker != null) {
                final CodecTraceNode root = tracker.getTrace().getRoot();
                assertThat(root.getChildren()).extracting(CodecTraceNode::getName).containsExactly("head", "bound", "body");
                assertThat(root.getChildren().get(0).getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertThat(root.getChildren().get(1).getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                final CodecTraceNode body = root.getChildren().get(2);
                assertThat(body.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(body.getChildren()).singleElement().satisfies(failed -> {
                    assertThat(failed.getName()).isEqualTo("data");
                    assertFailureTrace(tracker, failed, 16L);
                });
            }
            assertEmptyPrefixFits(plan, prefixBytes, tracked, extension, type.equals("extension"));
        } finally {
            output.release();
        }
    }

    static Stream<Arguments> missingValues() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(false, true).map(missing -> Arguments.of(tracked, missing)));
    }

    @ParameterizedTest
    @MethodSource("missingValues")
    void rejectsMissingAndNullExtensionValuesWithoutCallingTheExtension(boolean tracked, boolean missing) {
        final CountingExtension extension = new CountingExtension();
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(),
                DynamicField.field("data", "extension").length(DynamicLength.fixed(2, "byte"))
                        .options(Map.of("extensionId", "test")).build(), DynamicField.field("tail", "u8").build()),
                new DynamicExtensionRegistry(Map.of("test", extension)));
        final Map<String, Object> supplied = new LinkedHashMap<>();
        supplied.put("head", 126);
        if (!missing) {
            supplied.put("data", null);
        }
        supplied.put("tail", 127);
        final DynamicValues values = DynamicValues.of(supplied);
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class, () -> plan.encode(values, output, tracker));
            assertThat(error).hasMessageContaining("缺少必需字段值");
            assertThat(error.fieldPath()).isEqualTo("$.data");
            assertThat(error.bitOffset()).isEqualTo(8L);
            assertThat(values.fields()).isEqualTo(supplied);
            assertThat(values.fields().containsKey("data")).isEqualTo(!missing);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa7e");
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.writerIndex()).isEqualTo(3);
            assertThat(output.refCnt()).isEqualTo(1);
            assertThat(extension.encodes).isZero();
            assertThat(extension.decodes).isZero();
            if (tracker != null) {
                final CodecTraceNode root = tracker.getTrace().getRoot();
                assertThat(root.getChildren()).extracting(CodecTraceNode::getName).containsExactly("head", "data");
                assertThat(root.getChildren().getFirst().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertFailureTrace(tracker, root.getChildren().getLast(), 8L);
            }
            output.writerIndex(2);
            final CodecTracker encoder = tracked ? new CodecTracker() : null;
            final CodecTracker decoder = tracked ? new CodecTracker() : null;
            assertThat(plan.encode(DynamicValues.of(Map.of("head", 126, "data", "aabb", "tail", 127)), output, encoder)).isEqualTo(32L);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa7eaabb7f");
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(extension.encodes).isEqualTo(1);
            assertThat(extension.decodes).isEqualTo(tracked ? 1 : 0);
            final ByteBuf input = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump("55aa7eaabb7f66")).readerIndex(2);
            try {
                final DynamicDecodeResult result = plan.decode(input, decoder);
                assertThat(result.values().fields()).isEqualTo(Map.of("head", 126L, "data", "aabb", "tail", 127L));
                assertThat(result.consumedBits()).isEqualTo(32L);
                assertThat(result.remainingBytes()).isEqualTo(1);
                assertThat(input.readerIndex()).isEqualTo(6);
                assertThat(input.getUnsignedByte(6)).isEqualTo((short) 0x66);
                assertThat(input.refCnt()).isEqualTo(1);
                assertThat(output.refCnt()).isEqualTo(1);
                assertThat(extension.encodes).isEqualTo(1);
                assertThat(extension.decodes).isEqualTo(tracked ? 2 : 1);
                assertSuccess(encoder);
                assertSuccess(decoder);
            } finally {
                input.release();
            }
        } finally {
            output.release();
        }
    }

    private static void assertEmptyPrefixFits(DynamicPlan plan, int prefixBytes, boolean tracked, CountingExtension extension, boolean extended) {
        final String wire = "7e" + "%02x".formatted(prefixBytes) + "00".repeat(prefixBytes);
        final int length = 2 + prefixBytes;
        final DynamicLimits limits = new DynamicLimits(length + 1, length, 8, 8);
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final ByteBuf input = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump("55aa" + wire + "66")).readerIndex(2);
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            assertThat(plan.encode(prefixValues(prefixBytes), output, limits, encoder)).isEqualTo(length * 8L);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + wire);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.writerIndex()).isEqualTo(length + 2);
            assertThat(extension.encodes).isEqualTo(extended ? 1 : 0);
            assertThat(extension.decodes).isEqualTo(extended && tracked ? 1 : 0);
            final DynamicDecodeResult result = plan.decode(input, limits, decoder);
            assertThat(result.values().fields()).isEqualTo(Map.of("head", 126L, "bound", (long) prefixBytes, "body", Map.of("data", "")));
            assertThat(result.consumedBits()).isEqualTo(length * 8L);
            assertThat(result.consumedBytes()).isEqualTo(length);
            assertThat(result.remainingBytes()).isEqualTo(1);
            assertThat(input.readerIndex()).isEqualTo(length + 2);
            assertThat(input.getUnsignedByte(input.readerIndex())).isEqualTo((short) 0x66);
            assertThat(input.refCnt()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
            assertThat(extension.encodes).isEqualTo(extended ? 1 : 0);
            assertThat(extension.decodes).isEqualTo(!extended ? 0 : tracked ? 2 : 1);
            assertSuccess(encoder);
            assertSuccess(decoder);
            if (tracked) {
                for (final CodecTracker tracker : new CodecTracker[]{encoder, decoder}) {
                    final CodecTraceNode body = tracker.getTrace().getRoot().getChildren().get(2);
                    assertThat(body.getChildren()).extracting(CodecTraceNode::getName).containsExactly("prependLengthField(data)", "data");
                    final CodecTraceNode prefix = body.getChildren().getFirst();
                    assertThat(prefix.getKind()).isEqualTo(CodecTraceNodeKind.LENGTH_FIELD);
                    assertThat(prefix.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                    assertThat(prefix.getValue()).isEqualTo(0L);
                    assertThat(prefix.getBitStart()).isEqualTo(16L);
                    assertThat(prefix.getBitEnd()).isEqualTo(length * 8L);
                    final CodecTraceNode data = body.getChildren().getLast();
                    assertThat(data.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                    assertThat(data.getValue()).isEqualTo("");
                    assertThat(data.getAttributes()).containsEntry("contentBitStart", length * 8L).containsEntry("contentBitEnd", length * 8L);
                }
            }
        } finally {
            input.release();
            output.release();
        }
    }

    private static DynamicValues prefixValues(int bound) {
        return DynamicValues.of(Map.of("head", 126, "bound", bound, "body", Map.of("data", "")));
    }

    private static void assertFailureTrace(CodecTracker tracker, CodecTraceNode failed, long offset) {
        assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
        assertThat(tracker.getTrace().getRoot().getBitEnd()).isEqualTo(offset);
        assertThat(failed.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
        assertThat(failed.getValue()).isNull();
        assertThat(failed.getBitStart()).isEqualTo(offset);
        assertThat(failed.getBitEnd()).isEqualTo(offset);
        assertThat(failed.getChildren()).isEmpty();
        assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
            assertThat(diagnostic.level()).isEqualTo("ERROR");
            assertThat(diagnostic.nodeId()).isEqualTo(failed.getId());
            assertThat(diagnostic.bitOffset()).isEqualTo(offset);
            assertThat(diagnostic.byteOffset()).isEqualTo((int) (offset / 8));
        });
        assertThat(tracker.isNotTracing()).isTrue();
    }

    private static void assertSuccess(@Nullable CodecTracker tracker) {
        if (tracker != null) {
            assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
            assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
            assertThat(tracker.isNotTracing()).isTrue();
        }
    }

    private static class CountingExtension implements DynamicExtension {
        private int encodes;
        private int decodes;

        @Override
        public void validate(Map<String, Object> config) {
            // 测试扩展只接受固定注册标识，无额外配置。
        }

        @Override
        public byte[] encode(Object value, Map<String, Object> config) {
            encodes++;
            return HexFormat.of().parseHex(value.toString());
        }

        @Override
        public Object decode(byte[] bytes, Map<String, Object> config) {
            decodes++;
            return HexFormat.of().formatHex(bytes);
        }
    }
}
