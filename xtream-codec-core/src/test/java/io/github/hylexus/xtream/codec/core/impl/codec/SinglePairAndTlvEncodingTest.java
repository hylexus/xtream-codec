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
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.ext.LengthFieldType;
import io.github.hylexus.xtream.codec.core.impl.DefaultSerializeContext;
import io.github.hylexus.xtream.codec.core.impl.codec.pair.PairCodecs;
import io.github.hylexus.xtream.codec.core.impl.codec.tlv.TLVCodecs;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Pair;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.type.TLV;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.github.hylexus.xtream.codec.core.type.simple.DataFields.u8;
import static io.github.hylexus.xtream.codec.core.type.simple.DataFields.u16;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 默认注册的单值 Pair／TLV 编码、跟踪坐标和临时资源生命周期测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class SinglePairAndTlvEncodingTest extends BaseEntityCodecTest {
    static Stream<Arguments> values() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(false, true).flatMap(tlv ->
                Stream.of("dataField", "entity", "null").map(value -> Arguments.of(tracked, tlv, value))));
    }

    @ParameterizedTest
    @MethodSource("values")
    void encodesSingleFieldsUsingTheRegisteredCodecAndPreservesFollowingFields(boolean tracked, boolean tlv, String kind) {
        final Object value = kind.equals("entity") ? new Body() : u16(0x1234);
        final PairPacket pairs = new PairPacket();
        final TlvPacket tlvs = new TlvPacket();
        if (!kind.equals("null")) {
            pairs.body = Pair.of(u8((short) 1), value);
            tlvs.body = TLV.of(u8((short) 1), LengthFieldType.u8, value);
        }
        final Object packet = tlv ? tlvs : pairs;
        final String wire = kind.equals("null") ? "7e7f" : tlv ? "7e010212347f" : "7e0112347f";
        final BeanPropertyMetadata metadata = metadata(packet.getClass());
        assertThat(metadata.fieldCodec()).isSameAs(tlv ? TLVCodecs.INSTANCE : PairCodecs.INSTANCE);
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, packet, output, tracker);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + wire);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.writerIndex()).isEqualTo(2 + wire.length() / 2);
            assertThat(output.refCnt()).isEqualTo(1);
            if (tlv && tlvs.body != null) {
                assertThat(tlvs.body.length().value()).isEqualTo(2);
            }
            if (tracker != null) {
                assertSuccess(tracker);
                final var children = tracker.getTrace().getRoot().getChildren();
                assertThat(children).extracting(CodecTraceNode::getName).containsExactlyElementsOf(kind.equals("null")
                        ? java.util.List.of("head", "tail") : tlv
                        ? java.util.List.of("head", "tag", "value", "length", "tail") : java.util.List.of("head", "key", "value", "tail"));
                assertRange(children.getFirst(), 0, 1, "7e");
                assertRange(children.getLast(), wire.length() / 2 - 1, wire.length() / 2, "7f");
                if (!kind.equals("null")) {
                    assertRange(children.get(1), 1, 2, "01");
                    assertRange(children.get(2), tlv ? 3 : 2, tlv ? 5 : 4, "1234");
                    if (kind.equals("entity")) {
                        assertThat(children.get(2).getChildren()).singleElement().satisfies(node ->
                                assertRange(node, tlv ? 3 : 2, tlv ? 5 : 4, "1234"));
                    }
                    if (tlv) {
                        assertRange(children.get(3), 2, 3, "02");
                    }
                }
            }
        } finally {
            output.release();
        }
    }

    static Stream<Arguments> nullFields() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(false, true).map(tlv -> Arguments.of(tracked, tlv)));
    }

    @ParameterizedTest
    @MethodSource("nullFields")
    void directNullEncodingDoesNotAllocateOrWrite(boolean tracked, boolean tlv) {
        final BeanPropertyMetadata metadata = metadata(tlv ? TlvPacket.class : PairPacket.class);
        final ByteBufAllocator factory = mock(ByteBufAllocator.class);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        final var registry = entityCodec.entityEncoder().getBeanMetadataRegistry();
        final var context = new DefaultSerializeContext(factory, entityCodec.entityEncoder(), new Object(), XtreamField.ALL_VERSION, registry, tracker);
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        try {
            serialize(metadata, context, output, null, tracked);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa");
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.writerIndex()).isEqualTo(2);
            assertThat(output.refCnt()).isEqualTo(1);
            verifyNoInteractions(factory);
        } finally {
            output.release();
        }
    }

    static Stream<Arguments> lifecycle() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of("success", "tag", "length", "copy", "encode")
                .map(stage -> Arguments.of(tracked, stage)));
    }

    @ParameterizedTest
    @MethodSource("lifecycle")
    void tlvReleasesItsTemporaryBufferOnSuccessAndEveryEncodingFailure(boolean tracked, String stage) {
        final int capacity = switch (stage) {
            case "tag" -> 2;
            case "length" -> 3;
            case "copy" -> 5;
            default -> 6;
        };
        final Body body = new Body();
        body.failing = stage.equals("encode");
        final TLV value = TLV.of(u8((short) 1), LengthFieldType.u8, body);
        final BeanPropertyMetadata metadata = metadata(TlvPacket.class);
        final ByteBuf temp = Unpooled.buffer();
        final ByteBufAllocator factory = mock(ByteBufAllocator.class);
        when(factory.buffer()).thenReturn(temp);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        if (tracker != null) {
            tracker.beginEncode(2, TlvPacket.class.getName());
        }
        final var registry = entityCodec.entityEncoder().getBeanMetadataRegistry();
        final var context = new DefaultSerializeContext(factory, entityCodec.entityEncoder(), new TlvPacket(), XtreamField.ALL_VERSION, registry, tracker);
        final ByteBuf output = Unpooled.buffer(capacity, capacity).writeShort(0x55aa).readerIndex(1);
        try {
            if (stage.equals("success")) {
                serialize(metadata, context, output, value, tracked);
                if (tracker != null) {
                    tracker.finishTrace("01021234", output.writerIndex());
                    assertSuccess(tracker);
                }
            } else {
                final RuntimeException error = catchThrowableOfType(RuntimeException.class, () -> serialize(metadata, context, output, value, tracked));
                if (stage.equals("encode")) {
                    assertThat(error).hasStackTraceContaining(body.failure.getMessage());
                } else {
                    assertThat(error).isInstanceOf(IndexOutOfBoundsException.class).hasMessageContaining("maxCapacity");
                }
                if (tracker != null) {
                    tracker.recordFailure(error, output.writerIndex());
                    assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                    assertThat(tracker.getTrace().getDiagnostics()).isNotEmpty();
                    assertThat(tracker.isNotTracing()).isTrue();
                }
            }
            final String written = switch (stage) {
                case "tag" -> "";
                case "length", "encode" -> "01";
                case "copy" -> "0102";
                default -> "01021234";
            };
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + written);
            assertThat(output.writerIndex()).isEqualTo(2 + written.length() / 2);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
            assertThat(temp.refCnt()).isZero();
            assertThat(body.reads).isEqualTo(stage.equals("tag") ? 0 : 1);
            assertThat(value.length().value()).isEqualTo(stage.equals("tag") || stage.equals("encode") ? 0 : 2);
            verify(factory).buffer();
        } finally {
            output.release();
            if (temp.refCnt() > 0) {
                temp.release();
            }
        }
    }

    private BeanPropertyMetadata metadata(Class<?> type) {
        return entityCodec.entityEncoder().getBeanMetadataRegistry().getBeanMetadata(type).getPropertyMetadataList().stream()
                .filter(property -> property.name().equals("body")).findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private static void serialize(BeanPropertyMetadata metadata, DefaultSerializeContext context, ByteBuf output, @Nullable Object value, boolean tracked) {
        final FieldCodec<Object> codec = (FieldCodec<Object>) metadata.fieldCodec();
        if (tracked) {
            codec.serializeWithTracker(metadata, context, output, value);
        } else {
            codec.serialize(metadata, context, output, value);
        }
    }

    private static void assertRange(CodecTraceNode node, int start, int end, String hex) {
        assertThat(node.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
        assertThat(node.getByteStart()).isEqualTo(start);
        assertThat(node.getByteEnd()).isEqualTo(end);
        assertThat(node.getHex()).isEqualTo(hex);
    }

    private static void assertSuccess(CodecTracker tracker) {
        assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
        assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
        assertThat(tracker.isNotTracing()).isTrue();
    }

    public static class Body {
        @Preset.RustStyle.u16
        private int number = 0x1234;
        private boolean failing;
        private int reads;
        private final IllegalStateException failure = new IllegalStateException("测试单值 TLV 编码失败");

        public int getNumber() {
            reads++;
            if (failing) {
                throw failure;
            }
            return number;
        }
    }

    public static class PairPacket {
        @Preset.RustStyle.u8(order = 0)
        public int head = 126;
        @XtreamField(order = 1)
        public @Nullable Pair body;
        @Preset.RustStyle.u8(order = 2)
        public int tail = 127;
    }

    public static class TlvPacket {
        @Preset.RustStyle.u8(order = 0)
        public int head = 126;
        @XtreamField(order = 1)
        public @Nullable TLV body;
        @Preset.RustStyle.u8(order = 2)
        public int tail = 127;
    }
}
