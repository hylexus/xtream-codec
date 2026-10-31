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
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.ext.Key;
import io.github.hylexus.xtream.codec.core.annotation.ext.KeyType;
import io.github.hylexus.xtream.codec.core.annotation.ext.LengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.ext.ValueLength;
import io.github.hylexus.xtream.codec.core.annotation.ext.ValueMatcher;
import io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField;
import io.github.hylexus.xtream.codec.core.impl.DefaultSerializeContext;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 实体 Map 输出不足时的部分字节、临时资源释放及跟踪恢复测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class MapOutputBoundaryTest extends BaseEntityCodecTest {
    private static final String WIRE = "7e000100021234000200025678000300029abc7f";

    static Stream<Arguments> failures() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(0, 1).flatMap(completed ->
                Stream.of("key", "length", "value").map(stage -> Arguments.of(tracked, completed, stage))));
    }

    @ParameterizedTest
    @MethodSource("failures")
    void preservesPartialOutputAndReleasesTemporaryValue(boolean tracked, int completed, String stage) {
        final int header = stage.equals("key") ? 0 : stage.equals("length") ? 2 : 4;
        final int written = 1 + completed * 6 + header;
        final int capacity = 2 + written + 1;
        final Packet packet = new Packet();
        packet.head = 126;
        packet.entries.put(1, 0x1234);
        packet.entries.put(2, 0x5678);
        packet.entries.put(3, 0x9abc);
        packet.tail = 127;
        final ByteBuf output = Unpooled.buffer(capacity, capacity).writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final IndexOutOfBoundsException error = catchThrowableOfType(IndexOutOfBoundsException.class,
                    () -> entityCodec.encode(0, packet, output, tracker));
            assertThat(error).hasMessageContaining("maxCapacity");
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + WIRE.substring(0, written * 2));
            assertThat(output.writerIndex()).isEqualTo(2 + written);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
            assertThat(packet.valueWrites).isEqualTo(completed + (stage.equals("key") ? 0 : 1));
            assertThat(packet.tailReads).isZero();
            if (tracker != null) {
                assertFailureTrace(tracker, completed, stage, written);
            }
            assertTemporaryReleased(packet, tracked, capacity, written);

            packet.valueWrites = 0;
            final ByteBuf healthy = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
            final CodecTracker encoder = tracked ? new CodecTracker() : null;
            final CodecTracker decoder = tracked ? new CodecTracker() : null;
            try {
                entityCodec.encode(0, packet, healthy, encoder);
                assertThat(ByteBufUtil.hexDump(healthy, 0, healthy.writerIndex())).isEqualTo("55aa" + WIRE);
                assertThat(packet.valueWrites).isEqualTo(3);
                assertThat(packet.tailReads).isEqualTo(1);
                healthy.writeByte(0x66).readerIndex(2);
                final Packet decoded = entityCodec.decode(0, Packet.class, healthy, decoder);
                assertThat(decoded.entries).containsExactlyEntriesOf(packet.entries);
                assertThat(decoded.head).isEqualTo(126);
                assertThat(decoded.tail).isEqualTo(127);
                assertThat(healthy.readerIndex()).isEqualTo(2 + WIRE.length() / 2);
                assertThat(healthy.readUnsignedByte()).isEqualTo((short) 0x66);
                assertThat(healthy.refCnt()).isEqualTo(1);
                assertSuccessTrace(encoder);
                assertSuccessTrace(decoder);
            } finally {
                healthy.release();
            }
        } finally {
            output.release();
        }
    }

    private void assertTemporaryReleased(Packet packet, boolean tracked, int capacity, int written) {
        final var registry = entityCodec.entityEncoder().getBeanMetadataRegistry();
        final BeanPropertyMetadata metadata = registry.getBeanMetadata(Packet.class, 0).getPropertyMetadataList().stream()
                .filter(property -> property.name().equals("entries")).findFirst().orElseThrow();
        final ByteBuf temp = Unpooled.buffer();
        final ByteBuf output = Unpooled.buffer(capacity, capacity).writeShort(0x55aa).writeByte(126).readerIndex(1);
        final ByteBufAllocator factory = mock(ByteBufAllocator.class);
        when(factory.buffer()).thenReturn(temp);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        if (tracker != null) {
            tracker.beginEncode(2, Packet.class.getName());
        }
        final var context = new DefaultSerializeContext(factory, entityCodec.entityEncoder(), packet, 0, registry, tracker);
        final FieldCodec<?> codec = metadata.fieldCodec();
        @SuppressWarnings("unchecked") final FieldCodec<Object> mapCodec = (FieldCodec<Object>) codec;
        try {
            assertThatThrownBy(() -> {
                if (tracked) {
                    mapCodec.serializeWithTracker(metadata, context, output, packet.entries);
                } else {
                    mapCodec.serialize(metadata, context, output, packet.entries);
                }
            }).isInstanceOf(IndexOutOfBoundsException.class);
            assertThat(temp.refCnt()).isZero();
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + WIRE.substring(0, written * 2));
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
            verify(factory).buffer();
        } finally {
            output.release();
            if (temp.refCnt() > 0) {
                temp.release();
            }
        }
    }

    private static void assertFailureTrace(CodecTracker tracker, int completed, String stage, int written) {
        final CodecTraceNode root = tracker.getTrace().getRoot();
        assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
        assertThat(root.getChildren()).extracting(CodecTraceNode::getName).containsExactly("head", "entries");
        final CodecTraceNode map = root.getChildren().getLast();
        assertThat(map.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
        assertThat(map.getByteStart()).isEqualTo(1);
        assertThat(map.getChildren()).hasSize(completed + 1);
        for (int i = 0; i < completed; i++) {
            final CodecTraceNode entry = map.getChildren().get(i);
            assertThat(entry.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
            assertThat(entry.getByteStart()).isEqualTo(1 + i * 6);
            assertThat(entry.getByteEnd()).isEqualTo(7 + i * 6);
            assertThat(entry.getHex()).isEqualTo(WIRE.substring((1 + i * 6) * 2, (7 + i * 6) * 2));
            assertThat(entry.getChildren()).allSatisfy(child -> assertThat(child.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS));
        }
        final CodecTraceNode failedEntry = map.getChildren().getLast();
        assertThat(failedEntry.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
        assertThat(failedEntry.getByteStart()).isEqualTo(1 + completed * 6);
        final List<CodecTraceNode> children = failedEntry.getChildren();
        assertThat(children).hasSize(stage.equals("key") ? 1 : 3);
        final CodecTraceNode failure;
        if (stage.equals("value")) {
            failure = failedEntry;
            assertThat(children).allSatisfy(child -> assertThat(child.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS));
            final var value = children.stream().filter(child -> child.getAttributes().get("mapItemType") == CodecTracker.MapEntryItemType.VALUE)
                    .findFirst().orElseThrow();
            assertThat(value.getByteStart()).isEqualTo(written);
            assertThat(value.getByteEnd()).isEqualTo(written + 2);
        } else {
            final var type = stage.equals("key") ? CodecTracker.MapEntryItemType.KEY : CodecTracker.MapEntryItemType.VALUE_LENGTH;
            failure = children.stream().filter(child -> child.getAttributes().get("mapItemType") == type).findFirst().orElseThrow();
            assertThat(failure.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
            assertThat(failure.getByteStart()).isEqualTo(written);
            assertThat(failure.getByteEnd()).isEqualTo(written);
        }
        assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
            assertThat(diagnostic.nodeId()).isEqualTo(failure.getId());
            assertThat(diagnostic.byteOffset()).isEqualTo(written);
            assertThat(diagnostic.exceptionType()).isEqualTo(IndexOutOfBoundsException.class.getName());
        });
        assertThat(tracker.isNotTracing()).isTrue();
    }

    private static void assertSuccessTrace(@Nullable CodecTracker tracker) {
        if (tracker != null) {
            assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
            assertThat(tracker.getTrace().getRoot().getByteEnd()).isEqualTo(WIRE.length() / 2);
            assertThat(tracker.getTrace().getRoot().getChildren()).extracting(CodecTraceNode::getName).containsExactly("head", "entries", "tail");
            for (int i = 0; i < 3; i++) {
                final var entry = tracker.getTrace().getRoot().getChildren().get(1).getChildren().get(i);
                for (final var child : entry.getChildren()) {
                    final int start = 1 + i * 6 + switch ((CodecTracker.MapEntryItemType) child.getAttributes().get("mapItemType")) {
                        case KEY -> 0;
                        case VALUE_LENGTH -> 2;
                        case VALUE -> 4;
                    };
                    assertThat(child.getByteStart()).isEqualTo(start);
                    assertThat(child.getByteEnd()).isEqualTo(start + 2);
                    assertThat(child.getHex()).isEqualTo(WIRE.substring(start * 2, (start + 2) * 2));
                }
            }
            assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
            assertThat(tracker.isNotTracing()).isTrue();
        }
    }

    public static class Packet {
        @Preset.RustStyle.u8(order = 0)
        public int head = -1;
        @XtreamMapField(order = 1, length = 18, key = @Key(type = KeyType.u16), valueLength = @ValueLength(type = LengthFieldType.u16),
                value = @XtreamMapField.Value(
                        encoder = @XtreamMapField.ValueEncoder(matchers = @ValueMatcher(matchU16 = {1, 2, 3}, valueCodec = ValueCodec.class)),
                        decoder = @XtreamMapField.ValueDecoder(matchers = @ValueMatcher(matchU16 = {1, 2, 3}, valueCodec = ValueCodec.class))))
        public Map<Integer, Integer> entries = new LinkedHashMap<>();
        @Preset.RustStyle.u8(order = 2)
        private int tail = -1;
        int valueWrites;
        int tailReads;

        public int getTail() {
            tailReads++;
            return tail;
        }
    }

    public static class ValueCodec implements FieldCodec<Integer> {
        @Override
        public Integer deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            return input.readUnsignedShort();
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable Integer value) {
            ((Packet) context.containerInstance()).valueWrites++;
            output.writeShort(java.util.Objects.requireNonNull(value));
        }
    }
}
