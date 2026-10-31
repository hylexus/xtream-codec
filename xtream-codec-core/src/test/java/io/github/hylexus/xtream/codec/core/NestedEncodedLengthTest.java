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
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNodeKind;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 嵌套 POJO 的编码长度回填、统计范围与外层长度前缀测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class NestedEncodedLengthTest extends BaseEntityCodecTest {

    static Stream<Arguments> packets() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, new WholeBody(), "021234"),
                Arguments.of(tracked, new RangeBody(), "020312347e"),
                Arguments.of(tracked, new WholePacket(new WholeBody(), (short) 127), "0212347f"),
                Arguments.of(tracked, new RangePacket(new RangeBody(), (short) 127), "020312347e7f"),
                Arguments.of(tracked, new BoundedPacket(new RangeBody(), (short) 127), "05020312347e7f")));
    }

    @ParameterizedTest
    @MethodSource("packets")
    void backfillsNestedLengthWithoutMutatingSourceAndKeepsTraceValues(boolean tracked, Object source, String hex) {
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        final String lengthPath = source instanceof WholeBody || source instanceof RangeBody ? "length" : "body.length";
        try {
            entityCodec.encode(1, source, buffer, tracker);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("55" + hex);
            assertThat(source).extracting(lengthPath).isEqualTo(99);
            if (tracker != null) {
                final List<CodecTraceNode> nodes = new ArrayList<>();
                tracker.getTrace().visit((level, node) -> nodes.add(node));
                assertThat(nodes).filteredOn("name", "length").singleElement().satisfies(node -> {
                    assertThat(node.getKind()).isEqualTo(CodecTraceNodeKind.LENGTH_FIELD);
                    assertThat(node.getValue()).isEqualTo(2);
                    assertThat(node.getHex()).isEqualTo("02");
                });
                assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
            }
            buffer.writeByte(0x66).readerIndex(1);
            final Object result = entityCodec.decode(1, source.getClass(), buffer, tracked ? new CodecTracker() : null);
            assertThat(result).usingRecursiveComparison().ignoringFields("length", "body.length").isEqualTo(source);
            assertThat(result).extracting(lengthPath).isEqualTo(2);
            assertThat(buffer.readerIndex()).isEqualTo(1 + hex.length() / 2);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            assertThat(buffer.refCnt()).isEqualTo(1);
        } finally {
            buffer.release();
        }
    }

    @Getter
    @Setter
    public static class WholeBody {
        @EncodedLength
        @Preset.RustStyle.u8
        private int length = 99;
        @Preset.RustStyle.byte_array(lengthExpression = "length")
        private byte[] payload = {0x12, 0x34};
    }

    @Getter
    @Setter
    public static class RangeBody {
        @EncodedLength(from = "payload", until = "tail")
        @Preset.RustStyle.u8
        private int length = 99;
        @Preset.RustStyle.u8
        private short tag = 3;
        @Preset.RustStyle.byte_array(lengthExpression = "length")
        private byte[] payload = {0x12, 0x34};
        @Preset.RustStyle.u8
        private short tail = 126;
    }

    public record WholePacket(@Preset.RustStyle.struct WholeBody body, @Preset.RustStyle.u8 short tail) {
    }

    public record RangePacket(@Preset.RustStyle.struct RangeBody body, @Preset.RustStyle.u8 short tail) {
    }

    public record BoundedPacket(
            @Preset.RustStyle.struct(prependLengthFieldType = PrependLengthFieldType.u8) RangeBody body,
            @Preset.RustStyle.u8 short tail) {
    }
}
