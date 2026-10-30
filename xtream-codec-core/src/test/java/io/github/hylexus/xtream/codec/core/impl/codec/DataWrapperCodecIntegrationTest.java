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

package io.github.hylexus.xtream.codec.core.impl.codec;

import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.DefaultFieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.type.wrapper.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 包装类型通过默认注册器及公开入口编解码的集成测试。
 *
 * @author Codex (AI)
 */
class DataWrapperCodecIntegrationTest extends BaseEntityCodecTest {

    @Test
    void registersUtf8WrapperWithUtf8Codec() {
        assertThat(new DefaultFieldCodecRegistry().getFieldCodecForAtomicDataType(StringWrapperUtf8.class).orElseThrow())
                .isInstanceOf(DataWrapperFieldCodes.StringWrapperUtf8FieldCodec.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void preservesIntegerWrappersAtSignedAndUnsignedBoundaries(boolean tracked) {
        final NumericPacket source = new NumericPacket(new I8Wrapper(Byte.MIN_VALUE), new U8Wrapper((short) 255),
                new I16Wrapper(Short.MIN_VALUE), new U16Wrapper(65535),
                new I32Wrapper(Integer.MIN_VALUE), new U32Wrapper(4294967295L), (short) 126);
        roundTrip(source, NumericPacket.class, "80ff8000ffff80000000ffffffff7e", tracked);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void preservesGbkBcdAndRawBytesWithoutConsumingFollowingData(boolean tracked) {
        final BinaryPacket source = new BinaryPacket(new StringWrapperGbk("中文"), new StringWrapperBcd("001234"),
                new BytesDataWrapper(new byte[]{0, (byte) 128, (byte) 255}), (short) 126);
        roundTrip(source, BinaryPacket.class, "d6d0cec40012340080ff7e", tracked);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void preservesUtf8WrapperAndFollowingField(boolean tracked) {
        roundTrip(new Utf8Packet(new StringWrapperUtf8("中文"), (short) 126),
                Utf8Packet.class, "e4b8ade696877e", tracked);
    }

    private <T> void roundTrip(T source, Class<T> type, String expected, boolean tracked) {
        final ByteBuf buffer = allocator.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker encodeTracker = new CodecTracker();
        final CodecTracker decodeTracker = new CodecTracker();
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, source, buffer, tracked ? encodeTracker : null);
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo("55aa" + expected);
            assertThat(buffer.readerIndex()).isEqualTo(1);
            assertThat(buffer.writerIndex()).isEqualTo(2 + expected.length() / 2);
            final int end = buffer.writerIndex();
            buffer.writeByte(0x66).readerIndex(2);
            final T decoded = entityCodec.decode(XtreamField.ALL_VERSION, type, buffer, tracked ? decodeTracker : null);
            assertThat(decoded).usingRecursiveComparison().isEqualTo(source);
            assertThat(buffer.readerIndex()).isEqualTo(end);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            if (tracked) {
                for (final CodecTracker tracker : new CodecTracker[]{encodeTracker, decodeTracker}) {
                    assertThat(tracker.getTrace().getPayloadHex()).isEqualTo(expected);
                    assertThat(tracker.getTrace().getRoot().getByteStart()).isZero();
                    assertThat(tracker.getTrace().getRoot().getByteEnd()).isEqualTo(expected.length() / 2);
                    assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                    assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
                    assertThat(tracker.isNotTracing()).isTrue();
                }
            }
        } finally {
            buffer.release();
        }
    }

    public record NumericPacket(
            @Preset.RustStyle.struct(length = 1) I8Wrapper signed8,
            @Preset.RustStyle.struct(length = 1) U8Wrapper unsigned8,
            @Preset.RustStyle.struct(length = 2) I16Wrapper signed16,
            @Preset.RustStyle.struct(length = 2) U16Wrapper unsigned16,
            @Preset.RustStyle.struct(length = 4) I32Wrapper signed32,
            @Preset.RustStyle.struct(length = 4) U32Wrapper unsigned32,
            @Preset.RustStyle.u8 short tail) {
    }

    public record BinaryPacket(
            @Preset.RustStyle.struct(length = 4) StringWrapperGbk text,
            @Preset.RustStyle.struct(length = 3) StringWrapperBcd digits,
            @Preset.RustStyle.struct(length = 3) BytesDataWrapper bytes,
            @Preset.RustStyle.u8 short tail) {
    }

    public record Utf8Packet(
            @Preset.RustStyle.struct(length = 6) StringWrapperUtf8 text,
            @Preset.RustStyle.u8 short tail) {
    }
}
