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

package io.github.hylexus.xtream.codec.core.type;

import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.codec.BytesFieldCodecs;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 缓冲区容器的工厂身份、生命周期及当前读取起点测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class ByteBufContainerLifecycleTest {
    static Stream<Arguments> factories() {
        return Stream.of(
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofBytes(new byte[]{1, 2}), "0102"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofI8(-1), "ff"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofU8(255), "ff"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofI16(-2), "fffe"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofU16(65535), "ffff"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofI32(Integer.MIN_VALUE), "80000000"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofU32(-1), "ffffffff"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofBcd("12345"), "012345"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofString("中"), "e4b8ad"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofString("A", StandardCharsets.UTF_16BE), "0041"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofStringGbk("中"), "d6d0"));
    }

    @ParameterizedTest
    @MethodSource("factories")
    void factoryViewsAndWritesShareTheSameMutablePayload(Supplier<ByteBufContainer> factory, String hex) {
        final ByteBufContainer container = factory.get();
        final ByteBuf first = container.payload();
        final ByteBuf second = container.payload();
        final ByteBuf output = Unpooled.buffer().writeByte(0x55);
        try {
            assertThat(second).isSameAs(first);
            assertThat(ByteBufUtil.hexDump(first)).isEqualTo(hex);
            final byte[] expected = ByteBufUtil.decodeHexDump(hex);
            expected[0] ^= (byte) 0xff;
            first.setByte(first.readerIndex(), expected[0]);
            assertThat(container.asBytes()).containsExactly(expected);
            assertThat(container.length()).isEqualTo(expected.length);
            assertThat(first.readerIndex()).isZero();
            container.writeTo(output);
            assertThat(ByteBufUtil.hexDump(output)).isEqualTo("55" + ByteBufUtil.hexDump(expected));
            assertThat(container.length()).isZero();
            assertThat(container.asBytes()).isEmpty();
            assertThat(container.payload()).isSameAs(first);
            assertThat(first.refCnt()).isEqualTo(1);
        } finally {
            container.release();
            if (first.refCnt() > 0) {
                first.release();
            }
            if (second.refCnt() > 0) {
                second.release();
            }
            output.release();
        }
        assertThat(first.refCnt()).isZero();
    }

    @ParameterizedTest
    @MethodSource("factories")
    void containerReleaseReleasesPreviouslyReturnedPayload(Supplier<ByteBufContainer> factory, String hex) {
        final ByteBufContainer container = factory.get();
        final ByteBuf payload = container.payload();
        try {
            assertThat(ByteBufUtil.hexDump(payload)).isEqualTo(hex);
            container.release();
            assertThat(payload.refCnt()).isZero();
            container.release();
            assertThat(payload.refCnt()).isZero();
        } finally {
            if (payload.refCnt() > 0) {
                payload.release();
            }
        }
    }

    static Stream<Arguments> numericViews() {
        return Stream.of(1, 3).flatMap(prefix -> Stream.of(
                Arguments.of(prefix, 0x12345678, "12345678"),
                Arguments.of(prefix, 0xfffefdfc, "fffefdfc")));
    }

    @ParameterizedTest
    @MethodSource("numericViews")
    void numericViewsUseCurrentReaderIndexWithoutConsumingIt(int prefix, int value, String hex) {
        final ByteBuf payload = Unpooled.buffer().writeZero(prefix).writeInt(value).writeByte(0x66).readerIndex(prefix);
        final ByteBufContainer container = ByteBufContainer.ofBytes(payload);
        try {
            assertThat(container.length()).isEqualTo(5);
            assertThat(container.asBytes()).containsExactly(ByteBufUtil.decodeHexDump(hex + "66"));
            assertThat(container.asI8()).isEqualTo((byte) (value >>> 24));
            assertThat(container.asU8()).isEqualTo((short) ((value >>> 24) & 0xff));
            assertThat(container.asByte()).isEqualTo(container.asU8());
            assertThat(container.asI16()).isEqualTo((short) (value >>> 16));
            assertThat(container.asU16()).isEqualTo((value >>> 16) & 0xffff);
            assertThat(container.asWord()).isEqualTo(container.asU16());
            assertThat(container.asI32()).isEqualTo(value);
            assertThat(container.asU32()).isEqualTo(Integer.toUnsignedLong(value));
            assertThat(container.asDword()).isEqualTo(container.asU32());
            assertThat(payload.readerIndex()).isEqualTo(prefix);
            assertThat(payload.refCnt()).isEqualTo(1);
        } finally {
            container.release();
        }
        assertThat(payload.refCnt()).isZero();
    }

    @Test
    void wrappingRetainedSliceDoesNotAcquireAnAdditionalReference() {
        final ByteBuf owner = Unpooled.buffer().writeByte(0x55).writeShort(0x1234);
        final ByteBuf slice = owner.retainedSlice(1, 2);
        final ByteBufContainer container = ByteBufContainer.ofBytes(slice);
        try {
            assertThat(container.payload()).isSameAs(slice);
            assertThat(container.asU16()).isEqualTo(0x1234);
            assertThat(owner.refCnt()).isEqualTo(2);
            container.release();
            assertThat(owner.refCnt()).isEqualTo(1);
            assertThat(owner.getUnsignedShort(1)).isEqualTo(0x1234);
        } finally {
            if (owner.refCnt() > 1) {
                slice.release();
            }
            if (owner.refCnt() > 0) {
                owner.release();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void entityCodecConsumesSourceAndDecodesBorrowedContainerSlice(boolean tracked) {
        final ByteBuf payload = Unpooled.buffer().writeByte(0x55).writeShort(0x1234).readerIndex(1);
        final ByteBufContainer source = ByteBufContainer.ofBytes(payload);
        final ByteBuf wire = Unpooled.buffer().writeByte(0x77);
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            EntityCodec.DEFAULT.encode(XtreamField.ALL_VERSION, new ContainerMessage(source, (short) 127), wire, encoder);
            assertThat(ByteBufUtil.hexDump(wire)).isEqualTo("7712347f");
            assertThat(payload.readerIndex()).isEqualTo(3);
            assertThat(source.length()).isZero();
            assertThat(payload.refCnt()).isEqualTo(1);
            wire.writeByte(0x66).readerIndex(1);
            final ContainerMessage decoded = EntityCodec.DEFAULT.decode(XtreamField.ALL_VERSION, ContainerMessage.class, wire, decoder);
            assertThat(decoded.value().asU16()).isEqualTo(0x1234);
            assertThat(decoded.value().asBytes()).containsExactly((byte) 0x12, (byte) 0x34);
            assertThat(decoded.tail()).isEqualTo((short) 127);
            assertThat(wire.readerIndex()).isEqualTo(4);
            assertThat(wire.readUnsignedByte()).isEqualTo((short) 0x66);
            assertThat(decoded.value().payload().refCnt()).isEqualTo(1);
            source.release();
            assertThat(payload.refCnt()).isZero();
            if (encoder != null && decoder != null) {
                assertThat(encoder.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertThat(decoder.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
            }
        } finally {
            source.release();
            wire.release();
        }
    }

    public record ContainerMessage(
            @Preset.JtStyle.Bytes(order = 0, length = 2, fieldCodec = BytesFieldCodecs.BytesFieldCodecByteBufContainer.class) ByteBufContainer value,
            @Preset.RustStyle.u8(order = 1) short tail) {
    }
}
