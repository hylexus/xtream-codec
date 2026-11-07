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
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.bean.IterationTimesExtractor;
import io.github.hylexus.xtream.codec.core.EntityEncoder;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.ext.Key;
import io.github.hylexus.xtream.codec.core.annotation.ext.KeyType;
import io.github.hylexus.xtream.codec.core.annotation.ext.LengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.ext.ValueLength;
import io.github.hylexus.xtream.codec.core.annotation.ext.ValueMatcher;
import io.github.hylexus.xtream.codec.core.annotation.pair.XtreamPairFieldSequence;
import io.github.hylexus.xtream.codec.core.annotation.tlv.XtreamTLVFieldSequence;
import io.github.hylexus.xtream.codec.core.impl.DefaultDeserializeContext;
import io.github.hylexus.xtream.codec.core.impl.DefaultSerializeContext;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Pair;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.type.TLV;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;

import static io.github.hylexus.xtream.codec.core.type.simple.DataFields.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PairAndTlvSequenceCodecTest extends BaseEntityCodecTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void pairPacketPreservesBytesAndFollowingField(boolean tracked) {
        final PairPacket packet = new PairPacket(Arrays.asList(
                new Pair(u8((short) 1), u16(0x1234)), null,
                new Pair(u8((short) 2), utf8String("AB")), new Pair(u8((short) 3), new Item((short) 7))), (short) 126);
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        final CodecTracker encodeTracker = tracker(tracked);
        final CodecTracker decodeTracker = tracker(tracked);
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, packet, buffer, encodeTracker);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("550801123402414203077e");
            buffer.writeByte(0x66).readerIndex(1);
            final PairPacket result = entityCodec.decode(XtreamField.ALL_VERSION, PairPacket.class, buffer, decodeTracker);
            assertThat(result.getTail()).isEqualTo((short) 126);
            assertThat(result.getEntries()).hasSize(3);
            assertThat(result.getEntries()).extracting(p -> p.key().value()).containsExactly((short) 1, (short) 2, (short) 3);
            assertThat(result.getEntries()).extracting(Pair::value).containsExactly(0x1234, "AB", new Item((short) 7));
            assertThat(result.getEntries()).noneMatch(Pair::hasError);
            assertThat(buffer.readerIndex()).isEqualTo(11);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            if (tracked) {
                assertCollectionRange(encodeTracker, 9, 3);
                assertCollectionRange(decodeTracker, 9, 3);
            }
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void tlvPacketUsesEachValueLengthAndFallback(boolean tracked) {
        final TLV first = TLV.of(u8((short) 1), LengthFieldType.u8, u16(0x1234));
        final TLV second = TLV.of(u8((short) 2), LengthFieldType.u8, utf8String("A"));
        final TLV third = TLV.of(u8((short) 3), LengthFieldType.u8, new Item((short) 7));
        final TLV unknown = TLV.of(u8((short) 9), LengthFieldType.u8, byteSequence(new byte[]{(byte) 0xab, (byte) 0xcd}));
        final TlvPacket packet = new TlvPacket(Arrays.asList(first, null, second, third, unknown), (short) 126);
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        final CodecTracker encodeTracker = tracker(tracked);
        final CodecTracker decodeTracker = tracker(tracked);
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, packet, buffer, encodeTracker);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("550e010212340201410301070902abcd7e");
            assertThat(List.of(first, second, third, unknown)).extracting(t -> t.length().value()).containsExactly(2, 1, 1, 2);
            buffer.writeByte(0x66).readerIndex(1);
            final TlvPacket result = entityCodec.decode(XtreamField.ALL_VERSION, TlvPacket.class, buffer, decodeTracker);
            assertThat(result.getTail()).isEqualTo((short) 126);
            assertThat(result.getEntries()).hasSize(4);
            assertThat(result.getEntries()).extracting(t -> t.tag().value()).containsExactly((short) 1, (short) 2, (short) 3, (short) 9);
            assertThat(result.getEntries().subList(0, 3)).extracting(TLV::value).containsExactly(0x1234, "A", new Item((short) 7));
            assertThat((byte[]) result.getEntries().get(3).value()).containsExactly((byte) 0xab, (byte) 0xcd);
            assertThat(result.getEntries()).extracting(t -> t.length().value()).containsExactly(2, 1, 1, 2);
            assertThat(buffer.readerIndex()).isEqualTo(17);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            if (tracked) {
                assertCollectionRange(encodeTracker, 15, 4);
                assertCollectionRange(decodeTracker, 15, 4);
            }
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void unknownPairKeyRetainsRemainderInsideDeclaredLength(boolean tracked) {
        final ByteBuf input = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump("5501123409aabb7e")).readerIndex(1);
        try {
            @SuppressWarnings("unchecked") final List<Pair> result = (List<Pair>) decode(false, tracked, input, 6, false);
            assertThat(result).hasSize(2);
            assertThat(result.getFirst().value()).isEqualTo(0x1234);
            assertThat(result.getLast().key().value()).isEqualTo((short) 9);
            assertThat(result.getLast().value()).isNull();
            assertThat(result.getLast().hasError()).isTrue();
            assertThat(result.getLast().remainingHex()).isEqualTo("aabb");
            assertThat(input.readerIndex()).isEqualTo(7);
            assertThat(input.readUnsignedByte()).isEqualTo((short) 126);
        } finally {
            input.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, false", "false, true", "true, false", "true, true"})
    void iterationLimitLeavesRemainingInputReadable(boolean tlv, boolean tracked) {
        final ByteBuf input = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(tlv ? "5501021234020141" : "55011234024142")).readerIndex(1);
        try {
            final List<?> result = (List<?>) decode(tlv, tracked, input, -1, true);
            assertThat(result).hasSize(1);
            assertThat(input.readerIndex()).isEqualTo(tlv ? 5 : 4);
            assertThat(input.readUnsignedByte()).isEqualTo((short) 2);
        } finally {
            input.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, false", "false, true", "true, false", "true, true"})
    void emptyInputDecodesToEmptyList(boolean tlv, boolean tracked) {
        final ByteBuf input = allocator.buffer();
        try {
            assertThat((List<?>) decode(tlv, tracked, input, -1, false)).isEmpty();
            assertThat(input.readerIndex()).isZero();
        } finally {
            input.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, false", "false, true", "true, false", "true, true"})
    void rejectsTruncatedValue(boolean tlv, boolean tracked) {
        final ByteBuf input = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(tlv ? "010312" : "0112"));
        try {
            assertThatThrownBy(() -> decode(tlv, tracked, input, -1, false)).isInstanceOf(IndexOutOfBoundsException.class);
            assertThat(input.readerIndex()).isEqualTo(tlv ? 2 : 1);
        } finally {
            input.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, false", "false, true", "true, false", "true, true"})
    void nullAndEmptySequencesDoNotWriteBytes(boolean tlv, boolean tracked) {
        final BeanPropertyMetadata metadata = metadata(tlv);
        final FieldCodec<Object> codec = codec(metadata);
        final CodecTracker tracker = activeTracker(tracked, false);
        final var registry = entityCodec.entityEncoder().getBeanMetadataRegistry();
        final var context = new DefaultSerializeContext(allocator, entityCodec.entityEncoder(), new Object(), XtreamField.ALL_VERSION, registry, tracker);
        final ByteBuf output = allocator.buffer().writeByte(0x55);
        try {
            for (final Object value : Arrays.asList(null, List.of(), Arrays.asList(null, null))) {
                if (tracked) {
                    codec.serializeWithTracker(metadata, context, output, value);
                } else {
                    codec.serialize(metadata, context, output, value);
                }
                assertThat(ByteBufUtil.hexDump(output)).isEqualTo("55");
            }
        } finally {
            output.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void tlvReleasesTemporaryBufferWhenValueEncodingFails(boolean tracked) {
        final ByteBuf temp = allocator.buffer();
        final ByteBufAllocator bufferAllocator = mock(ByteBufAllocator.class);
        when(bufferAllocator.buffer()).thenReturn(temp);
        final BeanPropertyMetadata metadata = metadata(true);
        final FieldCodec<Object> codec = codec(metadata);
        final EntityEncoder encoder = spy(entityCodec.entityEncoder());
        final Item value = new Item((short) 1);
        final IllegalStateException failure = new IllegalStateException("测试编码失败");
        doThrow(failure).when(encoder).encode(same(value), same(temp));
        doThrow(failure).when(encoder).encodeWithTracker(same(value), same(temp), any(CodecTracker.class));
        final var context = new DefaultSerializeContext(bufferAllocator, encoder, new Object(), XtreamField.ALL_VERSION,
                entityCodec.entityEncoder().getBeanMetadataRegistry(), activeTracker(tracked, false));
        final ByteBuf output = allocator.buffer();
        final List<TLV> values = List.of(TLV.of(u8((short) 1), LengthFieldType.u8, value));
        try {
            assertThatThrownBy(() -> {
                if (tracked) {
                    codec.serializeWithTracker(metadata, context, output, values);
                } else {
                    codec.serialize(metadata, context, output, values);
                }
            }).isSameAs(failure);
            assertThat(temp.refCnt()).isZero();
        } finally {
            output.release();
            if (temp.refCnt() > 0) {
                temp.release();
            }
        }
    }

    private Object decode(boolean tlv, boolean tracked, ByteBuf input, int length, boolean limited) {
        final BeanPropertyMetadata metadata = limited ? spy(metadata(tlv)) : metadata(tlv);
        if (limited) {
            doReturn(new IterationTimesExtractor.ConstantIterationTimesExtractor(1)).when(metadata).iterationTimesExtractor();
        }
        final var context = new DefaultDeserializeContext(allocator, entityCodec.entityDecoder(), new Object(), XtreamField.ALL_VERSION,
                entityCodec.entityDecoder().getBeanMetadataRegistry(), activeTracker(tracked, true));
        return tracked
                ? codec(metadata).deserializeWithTracker(metadata, context, input, length)
                : codec(metadata).deserialize(metadata, context, input, length);
    }

    private BeanPropertyMetadata metadata(boolean tlv) {
        return entityCodec.entityEncoder().getBeanMetadataRegistry().getBeanMetadata(tlv ? TlvPacket.class : PairPacket.class)
                .getPropertyMetadataList().stream().filter(p -> p.name().equals("entries")).findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private FieldCodec<Object> codec(BeanPropertyMetadata metadata) {
        return (FieldCodec<Object>) metadata.fieldCodec();
    }

    private CodecTracker tracker(boolean tracked) {
        return tracked ? new CodecTracker() : null;
    }

    private void assertCollectionRange(CodecTracker tracker, int end, int size) {
        final var collection = tracker.getTrace().getRoot().getChildren().stream()
                .filter(node -> node.getName().equals("entries")).findFirst().orElseThrow();
        assertThat(collection.getByteStart()).isEqualTo(1);
        assertThat(collection.getByteEnd()).isEqualTo(end);
        assertThat(collection.getChildren()).hasSize(size);
    }

    private CodecTracker activeTracker(boolean tracked, boolean decode) {
        final CodecTracker tracker = tracker(tracked);
        if (tracker != null) {
            if (decode) {
                tracker.beginDecode(0, null);
            } else {
                tracker.beginEncode(0, null);
            }
        }
        return tracker;
    }

    public record Item(@Preset.RustStyle.u8 Short value) {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PairPacket {
        @XtreamPairFieldSequence(order = 0, prependLengthFieldType = PrependLengthFieldType.u8,
                decoder = @XtreamPairFieldSequence.Decoder(key = @Key(type = KeyType.u8),
                        value = @XtreamPairFieldSequence.Value(matchers = {
                                @ValueMatcher(matchU8 = 1, length = 2, valueType = XtreamDataType.u16),
                                @ValueMatcher(matchU8 = 2, length = 2, valueType = XtreamDataType.string_utf8),
                                @ValueMatcher(matchU8 = 3, length = 1, valueEntity = Item.class)
                        })))
        private List<Pair> entries;

        @Preset.RustStyle.u8(order = 1)
        private Short tail;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TlvPacket {
        @XtreamTLVFieldSequence(order = 0, prependLengthFieldType = PrependLengthFieldType.u8,
                decoder = @XtreamTLVFieldSequence.Decoder(tag = @Key(type = KeyType.u8),
                        length = @ValueLength(type = LengthFieldType.u8),
                        value = @XtreamTLVFieldSequence.Value(matchers = {
                                @ValueMatcher(matchU8 = 1, valueType = XtreamDataType.u16),
                                @ValueMatcher(matchU8 = 2, valueType = XtreamDataType.string_utf8),
                                @ValueMatcher(matchU8 = 3, valueEntity = Item.class)
                        })))
        private List<TLV> entries;

        @Preset.RustStyle.u8(order = 1)
        private Short tail;
    }
}
