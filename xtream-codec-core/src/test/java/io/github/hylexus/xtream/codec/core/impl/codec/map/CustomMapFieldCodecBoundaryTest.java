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
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.codec.AbstractMapFieldCodec;
import io.github.hylexus.xtream.codec.core.impl.codec.EntityFieldCodec;
import io.github.hylexus.xtream.codec.core.impl.codec.U8FieldCodecs;
import io.github.hylexus.xtream.codec.core.impl.codec.U16FieldCodecs;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNodeKind;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 自定义 Map 的类注册、版本传递、输入边界和未知键测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class CustomMapFieldCodecBoundaryTest extends BaseEntityCodecTest {
    static Stream<Arguments> versions() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(false, true).flatMap(bounded ->
                Stream.of(1, 2).map(version -> Arguments.of(tracked, bounded, version))));
    }

    @ParameterizedTest
    @MethodSource("versions")
    void registersClassesAndPreservesVersionsInBothModes(boolean tracked, boolean bounded, int version) {
        final Map<Object, Object> entries = new LinkedHashMap<>();
        final Body body = new Body();
        body.number = version == 1 ? 0x34 : 0x1234;
        entries.put((short) 1, body);
        entries.put((short) 2, 0x5678);
        final Object packet = packet(bounded, entries);
        final String data = version == 1 ? "01013402025678" : "0102123402025678";
        final String wire = "7e" + (bounded ? version == 1 ? "07" : "08" : "") + data + (bounded ? "7f" : "");
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final ByteBuf input = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump("55aa" + wire + (bounded ? "66" : ""))).readerIndex(2);
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            entityCodec.encode(version, packet, output, encoder);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + wire);
            assertThat(output.readerIndex()).isEqualTo(1);
            final Object decoded = entityCodec.decode(version, packet.getClass(), input, decoder);
            final Map<Object, Object> values = decoded instanceof BoundedPacket p ? p.entries : ((UnboundedPacket) decoded).entries;
            assertThat(values.keySet()).containsExactly((short) 1, (short) 2);
            assertThat(values.get((short) 1)).isInstanceOfSatisfying(Body.class, value -> assertThat(value.number).isEqualTo(body.number));
            assertThat(values.get((short) 2)).isEqualTo(0x5678);
            if (decoded instanceof BoundedPacket p) {
                assertThat(p.tail).isEqualTo(127);
            }
            assertThat(input.readerIndex()).isEqualTo(2 + wire.length() / 2);
            assertThat(input.readableBytes()).isEqualTo(bounded ? 1 : 0);
            if (bounded) {
                assertThat(input.getUnsignedByte(input.readerIndex())).isEqualTo((short) 0x66);
            }
            assertThat(input.refCnt()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
            final var registry = entityCodec.entityEncoder().getBeanMetadataRegistry();
            final ValueMapCodec codec = new ValueMapCodec(registry);
            assertThat(codec.requireValueCodec((short) 1)).isInstanceOf(EntityFieldCodec.class);
            assertThat(codec.requireValueCodec((short) 2)).isInstanceOfSatisfying(CustomNumberCodec.class,
                    custom -> assertThat(custom.registry).isSameAs(registry));
            assertTrace(encoder, bounded, version, data, wire);
            assertTrace(decoder, bounded, version, data, wire);
        } finally {
            input.release();
            output.release();
        }
    }

    static Stream<Arguments> modes() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(false, true).map(bounded -> Arguments.of(tracked, bounded)));
    }

    @ParameterizedTest
    @MethodSource("modes")
    void acceptsAnEmptyMapWithoutConsumingTheFollowingField(boolean tracked, boolean bounded) {
        final Object packet = packet(bounded, new LinkedHashMap<>());
        final String wire = bounded ? "7e007f" : "7e";
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa);
        final ByteBuf input = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump("55aa" + wire + (bounded ? "66" : ""))).readerIndex(2);
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            entityCodec.encode(1, packet, output, encoder);
            final Object decoded = entityCodec.decode(1, packet.getClass(), input, decoder);
            assertThat(decoded instanceof BoundedPacket p ? p.entries : ((UnboundedPacket) decoded).entries).isEmpty();
            assertThat(ByteBufUtil.hexDump(output)).isEqualTo("55aa" + wire);
            assertThat(input.readerIndex()).isEqualTo(2 + wire.length() / 2);
            assertThat(input.readableBytes()).isEqualTo(bounded ? 1 : 0);
            assertThat(input.refCnt()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
            for (final CodecTracker tracker : new CodecTracker[]{encoder, decoder}) {
                if (tracker != null) {
                    final var map = mapNode(tracker);
                    assertThat(map.getChildren()).isEmpty();
                    assertThat(map.getByteStart()).isEqualTo(bounded ? 2 : 1);
                    assertThat(map.getByteEnd()).isEqualTo(map.getByteStart());
                    assertThat(map.getHex()).isEmpty();
                    assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                    assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
                }
            }
        } finally {
            input.release();
            output.release();
        }
    }

    @ParameterizedTest
    @MethodSource("modes")
    void unknownNumericKeysFailAfterTheHeaderAndPreserveInputOwnership(boolean tracked, boolean bounded) {
        final ByteBuf input = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(bounded ? "55aa7e030301ab7f66" : "55aa7e0301ab")).readerIndex(2);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        final Class<?> type = bounded ? BoundedPacket.class : UnboundedPacket.class;
        try {
            assertThatThrownBy(() -> entityCodec.decode(1, type, input, tracker))
                    .isInstanceOf(UnsupportedOperationException.class).hasMessageContaining("Unsupported key: 3(0x03)");
            assertThat(input.readerIndex()).isEqualTo(bounded ? 7 : 5);
            assertThat(input.getUnsignedByte(input.readerIndex())).isEqualTo((short) (bounded ? 0x7f : 0xab));
            assertThat(input.refCnt()).isEqualTo(1);
            if (tracker != null) {
                assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(tracker.getTrace().getDiagnostics()).isNotEmpty();
                assertThat(mapNode(tracker).getChildren()).singleElement().satisfies(entry ->
                        assertThat(entry.getChildren()).hasSize(2).allSatisfy(node -> assertThat(node.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS)));
                assertThat(tracker.isNotTracing()).isTrue();
            }
        } finally {
            input.release();
        }
    }

    @ParameterizedTest
    @ValueSource(classes = {int.class, Integer.class, String.class, U16FieldCodecs.U16FieldCodec.class})
    void rejectsBasicTypesAndCodecsWithoutTheCustomMarker(Class<?> type) {
        final ValueMapCodec codec = new ValueMapCodec(entityCodec.entityEncoder().getBeanMetadataRegistry());
        assertThatThrownBy(() -> codec.registerValueFieldCodec((short) 1, type)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(FieldCodec.class.isAssignableFrom(type) ? "subclass of CustomFieldCodec" : "entity class");
        assertThat(codec.requireValueCodec((short) 1)).isInstanceOf(EntityFieldCodec.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing", "3"})
    void unknownTextKeysKeepTheirOriginalRepresentation(String key) {
        final ValueMapCodec codec = new ValueMapCodec(entityCodec.entityEncoder().getBeanMetadataRegistry());
        assertThatThrownBy(() -> codec.requireValueCodec(key)).isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Unsupported key: " + key);
    }

    private static Object packet(boolean bounded, Map<Object, Object> entries) {
        if (bounded) {
            final BoundedPacket packet = new BoundedPacket();
            packet.entries = entries;
            return packet;
        }
        final UnboundedPacket packet = new UnboundedPacket();
        packet.entries = entries;
        return packet;
    }

    private static CodecTraceNode mapNode(CodecTracker tracker) {
        return tracker.getTrace().getRoot().getChildren().stream().filter(node -> node.getKind() == CodecTraceNodeKind.MAP).findFirst().orElseThrow();
    }

    private static void assertTrace(@Nullable CodecTracker tracker, boolean bounded, int version, String data, String wire) {
        if (tracker == null) {
            return;
        }
        assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
        assertThat(tracker.getTrace().getRoot().getByteEnd()).isEqualTo(wire.length() / 2);
        assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
        final CodecTraceNode map = mapNode(tracker);
        final int start = bounded ? 2 : 1;
        assertThat(map.getByteStart()).isEqualTo(start);
        assertThat(map.getByteEnd()).isEqualTo(start + data.length() / 2);
        assertThat(map.getHex()).isEqualTo(data);
        final List<CodecTraceNode> entries = map.getChildren();
        assertThat(entries).hasSize(2);
        final var value = entries.getFirst().getChildren().stream()
                .filter(node -> node.getAttributes().get("mapItemType") == CodecTracker.MapEntryItemType.VALUE).findFirst().orElseThrow();
        assertThat(value.getByteStart()).isEqualTo(start + 2);
        assertThat(value.getByteEnd()).isEqualTo(start + 2 + version);
        assertThat(value.getHex()).isEqualTo(version == 1 ? "34" : "1234");
        assertThat(value.getChildren()).singleElement().satisfies(child -> {
            assertThat(child.getByteStart()).isEqualTo(value.getByteStart());
            assertThat(child.getByteEnd()).isEqualTo(value.getByteEnd());
            assertThat(child.getHex()).isEqualTo(value.getHex());
        });
        assertThat(tracker.isNotTracing()).isTrue();
    }

    public static class ValueMapCodec extends AbstractMapFieldCodec<Object, U8FieldCodecs.U8FieldCodec> {
        @FieldCodecCreator
        public ValueMapCodec(BeanMetadataRegistry registry) {
            super(registry);
        }

        @Override
        protected void initValueCodec(BeanMetadataRegistry registry) {
            registerValueFieldCodec((short) 1, Body.class);
            registerValueFieldCodec((short) 2, CustomNumberCodec.class);
        }

        FieldCodec<?> requireValueCodec(Object key) {
            return getValueFieldCodec(key);
        }

        @Override
        protected FieldCodec<?> getKeyFieldCodec() {
            return U8FieldCodecs.SHORT_INSTANCE;
        }

        @Override
        protected U8FieldCodecs.U8FieldCodec getValueLengthFieldCodec() {
            return U8FieldCodecs.SHORT_INSTANCE;
        }
    }

    public static class CustomNumberCodec implements FieldCodec.CustomFieldCodec<Integer> {
        final BeanMetadataRegistry registry;

        public CustomNumberCodec(BeanMetadataRegistry registry) {
            this.registry = registry;
        }

        @Override
        public Integer deserialize(BeanPropertyMetadata metadata, DeserializeContext context, ByteBuf input, int length) {
            return (int) input.readUnsignedShort();
        }

        @Override
        public void serialize(BeanPropertyMetadata metadata, SerializeContext context, ByteBuf output, @Nullable Integer value) {
            output.writeShort(java.util.Objects.requireNonNull(value));
        }
    }

    public static class Body {
        @Preset.RustStyle.u8(version = 1)
        @Preset.RustStyle.u16(version = 2)
        public int number;
    }

    public static class BoundedPacket {
        @Preset.RustStyle.u8(order = 0)
        public int head = 126;
        @XtreamField(order = 1, prependLengthFieldType = PrependLengthFieldType.u8, fieldCodec = ValueMapCodec.class)
        public Map<Object, Object> entries = new LinkedHashMap<>();
        @Preset.RustStyle.u8(order = 2)
        public int tail = 127;
    }

    public static class UnboundedPacket {
        @Preset.RustStyle.u8(order = 0)
        public int head = 126;
        @XtreamField(order = 1, fieldCodec = ValueMapCodec.class)
        public Map<Object, Object> entries = new LinkedHashMap<>();
    }
}
