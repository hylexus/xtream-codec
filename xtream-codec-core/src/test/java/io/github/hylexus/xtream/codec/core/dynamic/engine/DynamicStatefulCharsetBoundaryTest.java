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
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 有状态字符集收尾字节的扩容、字段及输出边界和异常后复用测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicStatefulCharsetBoundaryTest {

    static Stream<Arguments> boundaries() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(1, 14).flatMap(count ->
                Stream.of(false, true).flatMap(outputLimit -> Stream.of(false, true)
                        .map(fits -> Arguments.of(tracked, count, outputLimit, fits)))));
    }

    @ParameterizedTest
    @MethodSource("boundaries")
    void includesFlushBytesInFieldAndOutputLimits(boolean tracked, int count, boolean outputLimit, boolean fits) {
        // あ 对应 JIS X 0208 的 2422；末尾需要 ESC ( B 切回 ASCII。
        // 14 个字符在 encode 阶段占 31 字节，flush 才使 32 字节初始缓冲区溢出。
        final String text = "あ".repeat(count);
        final String hex = "1b2442" + "2422".repeat(count) + "1b2842";
        final int textBytes = count * 2 + 6;
        final int capacity = fits ? textBytes : textBytes - 1;
        final DynamicLength length = outputLimit ? DynamicLength.prefix(8, "byte") : DynamicLength.fixed(capacity, "byte");
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(),
                DynamicField.field("text", "string").charset("ISO-2022-JP").length(length).build(),
                DynamicField.field("tail", "u8").build()));
        // 失败上限容纳 head、长度前缀和少一字节的文本；成功上限还容纳 tail。
        final DynamicLimits limits = outputLimit
                ? new DynamicLimits(1024, fits ? textBytes + 3 : capacity + 2, 8, 8) : DynamicLimits.DEFAULT;
        if (fits) {
            assertRoundTrip(plan, text, hex, outputLimit, limits, tracked);
            return;
        }

        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                    () -> plan.encode(values(text), output, limits, tracker));
            assertThat(error).hasMessageContaining("值的字节长度超过字段或输出边界")
                    .hasCauseInstanceOf(IllegalArgumentException.class);
            assertThat(error.fieldPath()).isEqualTo("$.text");
            assertThat(error.bitOffset()).isEqualTo(8L);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa7e");
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.writerIndex()).isEqualTo(3);
            assertThat(output.refCnt()).isEqualTo(1);
            if (tracker != null) {
                final CodecTraceNode root = tracker.getTrace().getRoot();
                assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(root.getBitEnd()).isEqualTo(8L);
                assertThat(root.getChildren()).extracting(CodecTraceNode::getName).containsExactly("head", "text");
                assertThat(root.getChildren().getFirst().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                final CodecTraceNode failed = root.getChildren().getLast();
                assertThat(failed.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(failed.getBitStart()).isEqualTo(8L);
                assertThat(failed.getBitEnd()).isEqualTo(8L);
                assertThat(failed.getChildren()).isEmpty();
                assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
                    assertThat(diagnostic.level()).isEqualTo("ERROR");
                    assertThat(diagnostic.nodeId()).isEqualTo(failed.getId());
                    assertThat(diagnostic.byteOffset()).isEqualTo(1);
                    assertThat(diagnostic.bitOffset()).isEqualTo(8L);
                });
                assertThat(tracker.isNotTracing()).isTrue();
            }
            final int asciiLength = outputLimit ? 1 : capacity;
            assertRoundTrip(plan, "A".repeat(asciiLength), "41".repeat(asciiLength), outputLimit, limits, tracked);
        } finally {
            output.release();
        }
    }

    private static DynamicValues values(String text) {
        return DynamicValues.of(Map.of("head", 126L, "text", text, "tail", 127L));
    }

    private static void assertRoundTrip(DynamicPlan plan, String text, String hex, boolean prefix, DynamicLimits limits, boolean tracked) {
        final String expected = "7e" + (prefix ? "%02x".formatted(hex.length() / 2) : "") + hex + "7f";
        final long bits = expected.length() * 4L;
        final ByteBuf buffer = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            assertThat(plan.encode(values(text), buffer, limits, encoder)).isEqualTo(bits);
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo("55aa" + expected);
            assertThat(buffer.readerIndex()).isEqualTo(1);
            buffer.writeByte(0x66).readerIndex(2);
            final var result = plan.decode(buffer, limits, decoder);
            assertThat(result.values().fields()).isEqualTo(values(text).fields());
            assertThat(result.consumedBits()).isEqualTo(bits);
            assertThat(result.remainingBytes()).isEqualTo(1);
            assertThat(buffer.readerIndex()).isEqualTo(2 + expected.length() / 2);
            assertThat(buffer.getUnsignedByte(buffer.readerIndex())).isEqualTo((short) 0x66);
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (encoder != null && decoder != null) {
                for (final CodecTracker tracker : List.of(encoder, decoder)) {
                    final CodecTraceNode root = tracker.getTrace().getRoot();
                    assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                    assertThat(root.getBitEnd()).isEqualTo(bits);
                    assertThat(root.getChildren()).extracting(CodecTraceNode::getName).containsExactlyElementsOf(prefix
                            ? List.of("head", "prependLengthField(text)", "text", "tail") : List.of("head", "text", "tail"));
                    assertThat(root.getChildren()).filteredOn("name", "text").singleElement().satisfies(node -> {
                        assertThat(node.getValue()).isEqualTo(text);
                        assertThat(node.getBitStart()).isEqualTo(8L);
                        assertThat(node.getBitEnd()).isEqualTo(bits - 8);
                        if (prefix) {
                            assertThat(node.getAttributes()).containsEntry("contentBitStart", 16L)
                                    .containsEntry("contentBitEnd", bits - 8);
                        }
                    });
                    if (prefix) {
                        assertThat(root.getChildren().get(1).getValue()).isEqualTo(hex.length() / 2L);
                    }
                    assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
                    assertThat(tracker.isNotTracing()).isTrue();
                }
            }
        } finally {
            buffer.release();
        }
    }
}
