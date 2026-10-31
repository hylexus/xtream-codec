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
import io.github.hylexus.xtream.codec.core.annotation.DerivedField;
import io.github.hylexus.xtream.codec.core.annotation.EncodedLength;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNodeKind;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 编码长度以派生、瞬态字段作为范围边界时的跳过及回填测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class EncodedLengthSkippedFieldTest extends BaseEntityCodecTest {

    static Stream<Arguments> boundaries() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, new FromDerived(126, 99, "initial", 0x1234, "sentinel", 66, 127),
                        new FromDerived(126, 2, "hex:1234", 0x1234, null, 66, 127), "7e021234427f", 2),
                Arguments.of(tracked, new FromTransient(126, 99, "sentinel", 0x1234, "initial", 66, 127),
                        new FromTransient(126, 2, null, 0x1234, "hex:1234", 66, 127), "7e021234427f", 2),
                Arguments.of(tracked, new EmptyRange(126, 99, "initial", "sentinel", 0x1234, 66, 127),
                        new EmptyRange(126, 0, "hex:1234", null, 0x1234, 66, 127), "7e001234427f", 0)));
    }

    @ParameterizedTest
    @MethodSource("boundaries")
    void usesSkippedFieldsAsRangeMarkersWithoutWritingTheirValues(boolean tracked, Object source, Object expected, String hex, int length) {
        final ByteBuf buffer = allocator.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, source, buffer, encoder);
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo("55aa" + hex);
            assertThat(buffer.readerIndex()).isEqualTo(1);
            assertThat(buffer.writerIndex()).isEqualTo(8);
            assertThat(source).extracting("length", "display", "ignored").containsExactly(99, "initial", "sentinel");
            buffer.writeByte(0x66).readerIndex(2);
            assertThat(entityCodec.decode(XtreamField.ALL_VERSION, source.getClass(), buffer, decoder)).isEqualTo(expected);
            assertThat(buffer.readerIndex()).isEqualTo(8);
            assertThat(buffer.getUnsignedByte(8)).isEqualTo((short) 0x66);
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (encoder != null && decoder != null) {
                for (final CodecTracker tracker : List.of(encoder, decoder)) {
                    final CodecTraceNode root = tracker.getTrace().getRoot();
                    assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                    assertThat(root.getByteStart()).isZero();
                    assertThat(root.getByteEnd()).isEqualTo(6);
                    assertThat(root.getChildren()).extracting(CodecTraceNode::getName)
                            .containsExactly("head", "length", "payload", "outside", "tail");
                    final CodecTraceNode field = root.getChildren().get(1);
                    assertThat(field.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                    assertThat(field.getValue()).isEqualTo(length);
                    assertThat(field.getHex()).isEqualTo("%02x".formatted(length));
                    assertThat(field.getByteStart()).isEqualTo(1);
                    assertThat(field.getByteEnd()).isEqualTo(2);
                    assertThat(root.getChildren().getLast().getByteStart()).isEqualTo(5);
                    assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
                }
                assertThat(encoder.getTrace().getRoot().getChildren().get(1).getKind()).isEqualTo(CodecTraceNodeKind.LENGTH_FIELD);
            }
        } finally {
            buffer.release();
        }
    }

    public record FromDerived(
            @Preset.RustStyle.u8 int head,
            @Preset.RustStyle.u8 @EncodedLength(from = "display", until = "ignored") int length,
            @DerivedField(source = "payload", using = HexTransformer.class) String display,
            @Preset.RustStyle.u16 int payload,
            @Preset.RustStyle.transient_rc(nulls = XtreamField.Nulls.AS_NULL) @Nullable String ignored,
            @Preset.RustStyle.u8 int outside,
            @Preset.RustStyle.u8 int tail) {
    }

    public record FromTransient(
            @Preset.RustStyle.u8 int head,
            @Preset.RustStyle.u8 @EncodedLength(from = "ignored", until = "display") int length,
            @Preset.RustStyle.transient_rc(nulls = XtreamField.Nulls.AS_NULL) @Nullable String ignored,
            @Preset.RustStyle.u16 int payload,
            @DerivedField(source = "payload", using = HexTransformer.class) String display,
            @Preset.RustStyle.u8 int outside,
            @Preset.RustStyle.u8 int tail) {
    }

    public record EmptyRange(
            @Preset.RustStyle.u8 int head,
            @Preset.RustStyle.u8 @EncodedLength(from = "display", until = "ignored") int length,
            @DerivedField(source = "payload", using = HexTransformer.class) String display,
            @Preset.RustStyle.transient_rc(nulls = XtreamField.Nulls.AS_NULL) @Nullable String ignored,
            @Preset.RustStyle.u16 int payload,
            @Preset.RustStyle.u8 int outside,
            @Preset.RustStyle.u8 int tail) {
    }

    public static class HexTransformer implements FieldTransformer<Integer, String> {
        @Override
        public @Nullable String read(@Nullable Integer source) {
            return source == null ? null : "hex:" + Integer.toHexString(source);
        }
    }
}
