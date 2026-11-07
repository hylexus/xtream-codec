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

package io.github.hylexus.xtream.codec.core.impl.codec.map;

import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.exception.NotYetImplementedException;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.PaddingType;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.ext.*;
import io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField;
import io.github.hylexus.xtream.codec.core.impl.DefaultDeserializeContext;
import io.github.hylexus.xtream.codec.core.impl.DefaultSerializeContext;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MapFieldCodecBoundaryTest extends BaseEntityCodecTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void preservesEntryLengthsFallbackAndFollowingField(boolean tracked) {
        final Packet packet = new Packet();
        final Map<Object, Object> entries = new LinkedHashMap<>();
        entries.put(1, 0x1234);
        entries.put(2, "中");
        entries.put(9, new byte[]{(byte) 0xab, (byte) 0xcd});
        packet.setEntries(entries);
        packet.setTail((short) 126);
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        try {
            entityCodec.encode(0, packet, buffer, tracked ? new CodecTracker() : null);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("551300010002341200020003e4b8ad00090002abcd7e");
            buffer.writeByte(0x66).readerIndex(1);
            final Packet decoded = entityCodec.decode(0, Packet.class, buffer, tracked ? new CodecTracker() : null);
            assertThat(decoded.getEntries().keySet()).containsExactly(1, 2, 9);
            assertThat(decoded.getEntries().get(1)).isEqualTo(0x1234);
            assertThat(decoded.getEntries().get(2)).isEqualTo("中");
            assertThat((byte[]) decoded.getEntries().get(9)).containsExactly((byte) 0xab, (byte) 0xcd);
            assertThat(decoded.getTail()).isEqualTo((short) 126);
            assertThat(buffer.readerIndex()).isEqualTo(22);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, 1, 30304142000112, 00AB", "true, 1, 30304142000112, 00AB",
            "false, 2, 41423030000112, AB00", "true, 2, 41423030000112, AB00"})
    void padsStringKeysOnConfiguredSide(boolean tracked, int version, String hex, String paddedKey) {
        final BeanPropertyMetadata metadata = metadata();
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        try {
            encode(metadata, tracked, version, allocator, buffer, Map.of("AB", new byte[]{0x12}));
            assertThat(ByteBufUtil.hexDump(buffer, 1, buffer.writerIndex() - 1)).isEqualTo(hex);
            buffer.writeByte(0x7e).readerIndex(1);
            final Map<?, ?> decoded = decode(metadata, tracked, version, buffer, 7);
            assertThat(decoded.keySet()).singleElement().isEqualTo(paddedKey);
            assertThat((byte[]) decoded.get(paddedKey)).containsExactly((byte) 0x12);
            assertThat(buffer.readerIndex()).isEqualTo(8);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 126);
        } finally {
            buffer.release();
        }
    }

    static Stream<Arguments> truncatedEntries() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(false, true).flatMap(bounded -> Stream.of(
                Arguments.of(tracked, bounded, "01", 0),
                Arguments.of(tracked, bounded, "000100", 2),
                Arguments.of(tracked, bounded, "0001000312", 4))));
    }

    @ParameterizedTest
    @MethodSource("truncatedEntries")
    void rejectsTruncatedKeyLengthOrPayload(boolean tracked, boolean bounded, String content, int consumed) {
        final ByteBuf input = allocator.buffer().writeByte(0x55).writeBytes(ByteBufUtil.decodeHexDump(content));
        final int end = input.writerIndex();
        if (bounded) {
            input.writeByte(0x7e);
        }
        input.readerIndex(1);
        try {
            assertThatThrownBy(() -> decode(metadata(), tracked, 0, input, bounded ? content.length() / 2 : -1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
            assertThat(input.readerIndex()).isEqualTo(bounded ? end : 1 + consumed);
            if (bounded) {
                assertThat(input.readUnsignedByte()).isEqualTo((short) 126);
            }
        } finally {
            input.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rejectsDeclaredMapLengthBeyondReadableBytes(boolean tracked) {
        final ByteBuf input = allocator.buffer().writeByte(0x55).writeByte(1).readerIndex(1);
        try {
            assertThatThrownBy(() -> decode(metadata(), tracked, 0, input, 2)).isInstanceOf(IndexOutOfBoundsException.class);
            assertThat(input.readerIndex()).isEqualTo(1);
        } finally {
            input.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void nullAndEmptyMapsDoNotWriteAndEmptyInputDecodesToEmptyMap(boolean tracked) {
        final BeanPropertyMetadata metadata = metadata();
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        try {
            for (final Object value : Arrays.asList(null, Map.of())) {
                encode(metadata, tracked, 0, allocator, buffer, value);
                assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("55");
            }
            buffer.readerIndex(1);
            assertThat(decode(metadata, tracked, 0, buffer, -1)).isEmpty();
            assertThat(buffer.readerIndex()).isEqualTo(1);
            buffer.writeByte(0x7e);
            assertThat(decode(metadata, tracked, 0, buffer, 0)).isEmpty();
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 126);
        } finally {
            buffer.release();
        }
    }

    static Stream<Arguments> invalidValues() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, Map.of(8, Map.of(1, 2)), NotYetImplementedException.class, "内嵌 Map"),
                Arguments.of(tracked, Map.of(8, List.of(1, 2)), NotYetImplementedException.class, "内嵌 Collection"),
                Arguments.of(tracked, Map.of(7, "失败"), IllegalStateException.class, "测试编码失败")));
    }

    @ParameterizedTest
    @MethodSource("invalidValues")
    void releasesTemporaryBufferWhenValueEncodingFails(boolean tracked, Map<?, ?> entries, Class<? extends Throwable> failure, String message) {
        final ByteBuf temp = allocator.buffer();
        final ByteBuf output = allocator.buffer().writeByte(0x55);
        final ByteBufAllocator bufferAllocator = mock(ByteBufAllocator.class);
        when(bufferAllocator.buffer()).thenReturn(temp);
        try {
            assertThatThrownBy(() -> encode(metadata(), tracked, 0, bufferAllocator, output, entries))
                    .isInstanceOf(failure).hasMessageContaining(message);
            assertThat(temp.refCnt()).isZero();
            assertThat(output.writerIndex()).isEqualTo(3);
            assertThat(output.getUnsignedByte(0)).isEqualTo((short) 0x55);
        } finally {
            output.release();
            if (temp.refCnt() > 0) {
                temp.release();
            }
        }
    }

    private BeanPropertyMetadata metadata() {
        return entityCodec.entityEncoder().getBeanMetadataRegistry().getBeanMetadata(Packet.class)
                .getPropertyMetadataList().stream().filter(p -> p.name().equals("entries")).findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private FieldCodec<Object> codec(BeanPropertyMetadata metadata) {
        return (FieldCodec<Object>) metadata.fieldCodec();
    }

    private void encode(BeanPropertyMetadata metadata, boolean tracked, int version, ByteBufAllocator bufferAllocator, ByteBuf output, Object value) {
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        if (tracker != null) {
            tracker.beginEncode(0, null);
        }
        final var context = new DefaultSerializeContext(bufferAllocator, entityCodec.entityEncoder(), new Object(), version,
                entityCodec.entityEncoder().getBeanMetadataRegistry(), tracker);
        if (tracked) {
            codec(metadata).serializeWithTracker(metadata, context, output, value);
        } else {
            codec(metadata).serialize(metadata, context, output, value);
        }
    }

    private Map<?, ?> decode(BeanPropertyMetadata metadata, boolean tracked, int version, ByteBuf input, int length) {
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        if (tracker != null) {
            tracker.beginDecode(0, null);
        }
        final var context = new DefaultDeserializeContext(allocator, entityCodec.entityDecoder(), new Object(), version,
                entityCodec.entityDecoder().getBeanMetadataRegistry(), tracker);
        return (Map<?, ?>) (tracked
                ? codec(metadata).deserializeWithTracker(metadata, context, input, length)
                : codec(metadata).deserialize(metadata, context, input, length));
    }

    @Getter
    @Setter
    public static class Packet {
        @XtreamMapField(
                prependLengthFieldType = PrependLengthFieldType.u8,
                key = {
                        @Key(type = KeyType.u16),
                        @Key(version = 1, type = KeyType.str_utf8, sizeInBytes = 4, paddingType = PaddingType.LEFT),
                        @Key(version = 2, type = KeyType.str_utf8, sizeInBytes = 4, paddingType = PaddingType.RIGHT)
                },
                valueLength = @ValueLength(type = LengthFieldType.u16),
                value = @XtreamMapField.Value(
                        encoder = @XtreamMapField.ValueEncoder(matchers = {
                                @ValueMatcher(version = 0, matchU16 = 1, valueType = XtreamDataType.u16_le),
                                @ValueMatcher(version = 0, matchU16 = 7, valueCodec = FailingValueCodec.class)
                        }),
                        decoder = @XtreamMapField.ValueDecoder(matchers = {
                                @ValueMatcher(version = 0, matchU16 = 1, valueType = XtreamDataType.u16_le),
                                @ValueMatcher(version = 0, matchU16 = 2, valueType = XtreamDataType.string_utf8)
                        })))
        private Map<Object, Object> entries;

        @Preset.RustStyle.u8
        private short tail;
    }

    public static class FailingValueCodec implements FieldCodec<Object> {
        @Override
        public Object deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Object value) {
            output.writeByte(1);
            throw new IllegalStateException("测试编码失败");
        }
    }
}
