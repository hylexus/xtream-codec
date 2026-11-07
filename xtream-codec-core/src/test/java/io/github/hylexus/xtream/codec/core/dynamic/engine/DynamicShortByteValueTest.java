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
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 定长字节字段缺少填充规则时拒绝短值及后续恢复测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicShortByteValueTest {
    static Stream<Arguments> shortValues() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(false, true).flatMap(empty -> Stream.of(
                Arguments.of(tracked, "string", empty ? "" : "A", "AB", "4142", "AB"),
                Arguments.of(tracked, "hex", empty ? "" : "ab", "abcd", "abcd", "abcd"),
                Arguments.of(tracked, "bcd", empty ? "" : "12", "1234", "1234", "1234"),
                Arguments.of(tracked, "bytes", empty ? List.of() : List.of(128), List.of(128, 255), "80ff", List.of(128, 255)),
                Arguments.of(tracked, "bytes", empty ? new byte[0] : new byte[]{(byte) 128},
                        new byte[]{(byte) 128, (byte) 255}, "80ff", List.of(128, 255)))));
    }

    @ParameterizedTest
    @MethodSource("shortValues")
    void rejectsShortContentWithoutPaddingAndReusesPlan(boolean tracked, String type, Object shortValue,
                                                       Object fullValue, String hex, Object expected) {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(),
                DynamicField.field("body", type).length(DynamicLength.fixed(2, "byte")).build(),
                DynamicField.field("tail", "u8").build()));
        final ByteBuf buffer = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                    () -> plan.encode(values(shortValue), buffer, tracker));
            assertThat(error).hasMessageContaining("值的字节长度不足且未声明 padByte").hasCauseInstanceOf(IllegalArgumentException.class);
            assertThat(error.fieldPath()).isEqualTo("$.body");
            assertThat(error.bitOffset()).isEqualTo(8);
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo("55aa7e");
            assertThat(buffer.readerIndex()).isEqualTo(1);
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (tracker != null) {
                final var root = tracker.getTrace().getRoot();
                assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(root.getBitEnd()).isEqualTo(8);
                assertThat(root.getChildren()).extracting(CodecTraceNode::getName).containsExactly("head", "body");
                assertThat(root.getChildren().getFirst().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                final var failed = root.getChildren().getLast();
                assertThat(failed.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(failed.getByteStart()).isEqualTo(1);
                assertThat(failed.getByteEnd()).isEqualTo(1);
                assertThat(failed.getValue()).isNull();
                assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
                    assertThat(diagnostic.level()).isEqualTo("ERROR");
                    assertThat(diagnostic.nodeId()).isEqualTo(failed.getId());
                    assertThat(diagnostic.byteOffset()).isEqualTo(1);
                    assertThat(diagnostic.bitOffset()).isEqualTo(8);
                });
                assertThat(tracker.isNotTracing()).isTrue();
            }

            buffer.writerIndex(2);
            final CodecTracker encoder = tracked ? new CodecTracker() : null;
            final CodecTracker decoder = tracked ? new CodecTracker() : null;
            assertThat(plan.encode(values(fullValue), buffer, encoder)).isEqualTo(32);
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo("55aa7e" + hex + "7f");
            assertThat(buffer.readerIndex()).isEqualTo(1);
            assertSuccess(encoder, hex);
            buffer.writeByte(0x66).readerIndex(2);
            final DynamicDecodeResult result = plan.decode(buffer, decoder);
            assertThat(result.values().fields()).isEqualTo(Map.of("head", 126L, "body", expected, "tail", 127L));
            assertThat(result.consumedBits()).isEqualTo(32);
            assertThat(result.remainingBytes()).isEqualTo(1);
            assertThat(buffer.readerIndex()).isEqualTo(6);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            assertThat(buffer.refCnt()).isEqualTo(1);
            assertSuccess(decoder, hex);
            if (shortValue instanceof byte[] bytes) {
                assertThat(bytes).containsExactly(bytes.length == 0 ? new byte[0] : new byte[]{(byte) 128});
            }
            if (fullValue instanceof byte[] bytes) {
                assertThat(bytes).containsExactly((byte) 128, (byte) 255);
            }
        } finally {
            buffer.release();
        }
    }

    private static DynamicValues values(Object body) {
        return DynamicValues.of(Map.of("head", 126, "body", body, "tail", 127));
    }

    private static void assertSuccess(@Nullable CodecTracker tracker, String hex) {
        if (tracker != null) {
            final var root = tracker.getTrace().getRoot();
            assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
            assertThat(root.getBitEnd()).isEqualTo(32);
            assertThat(root.getChildren()).extracting(CodecTraceNode::getName).containsExactly("head", "body", "tail");
            final var body = root.getChildren().get(1);
            assertThat(body.getByteStart()).isEqualTo(1);
            assertThat(body.getByteEnd()).isEqualTo(3);
            assertThat(tracker.toTraceView().root().children().get(1).hex()).isEqualTo(hex);
            assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
            assertThat(tracker.isNotTracing()).isTrue();
        }
    }
}
