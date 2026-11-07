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

package io.github.hylexus.xtream.codec.common.bean.impl;

import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.core.ContainerInstanceFactory;
import io.github.hylexus.xtream.codec.core.FieldTransformer;
import io.github.hylexus.xtream.codec.core.annotation.DerivedField;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static io.github.hylexus.xtream.codec.core.type.simple.DataFields.u8;
import static org.assertj.core.api.Assertions.*;

class NestedAndSequenceMetadataTest extends BaseEntityCodecTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void recordCollectionDecodesDerivedAndConditionalFields(boolean tracked) {
        final RecordPacket packet = new RecordPacket(Arrays.asList(
                new ConditionalRecord((short) 1, null, 0x1234), null,
                new ConditionalRecord((short) 0, null, 0xffff)), (short) 126);
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        final CodecTracker encodeTracker = tracker(tracked);
        final CodecTracker decodeTracker = tracker(tracked);
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, packet, buffer, encodeTracker);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("5504011234007e");
            buffer.writeByte(0x66).readerIndex(1);
            final RecordPacket result = entityCodec.decode(XtreamField.ALL_VERSION, RecordPacket.class, buffer, decodeTracker);
            assertThat(result.entries()).isInstanceOf(LinkedList.class).containsExactly(
                    new ConditionalRecord((short) 1, true, 0x1234),
                    new ConditionalRecord((short) 0, false, null));
            assertThat(result.tail()).isEqualTo((short) 126);
            assertThat(buffer.readerIndex()).isEqualTo(7);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            if (tracked) {
                assertRange(encodeTracker, "entries", 1, 5);
                assertRange(decodeTracker, "entries", 1, 5);
            }
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void emptyRecordCollectionKeepsFollowingField(boolean tracked) {
        final ByteBuf buffer = allocator.buffer();
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, new RecordPacket(List.of(), (short) 126), buffer, tracker(tracked));
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("007e");
            final RecordPacket result = entityCodec.decode(XtreamField.ALL_VERSION, RecordPacket.class, buffer, tracker(tracked));
            assertThat(result.entries()).isEmpty();
            assertThat(result.tail()).isEqualTo((short) 126);
            assertThat(buffer.readerIndex()).isEqualTo(2);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, 0", "false, 2", "true, 0", "true, 2"})
    void iterationExpressionStopsBeforeTail(boolean tracked, int count) {
        final CountedPacket packet = new CountedPacket((short) count,
                count == 0 ? List.of() : List.of(new Item((short) 3), new Item((short) 4)), (short) 126);
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, packet, buffer, tracker(tracked));
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo(count == 0 ? "55007e" : "550203047e");
            buffer.writeByte(0x66).readerIndex(1);
            assertThat(entityCodec.decode(XtreamField.ALL_VERSION, CountedPacket.class, buffer, tracker(tracked))).isEqualTo(packet);
            assertThat(buffer.readerIndex()).isEqualTo(count + 3);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void constantIterationLimitLeavesNextItemUnread(boolean tracked) {
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{0x55, 3, 4, 5}).readerIndex(1);
        try {
            final LimitedPacket result = entityCodec.decode(XtreamField.ALL_VERSION, LimitedPacket.class, buffer, tracker(tracked));
            assertThat(result.entries()).containsExactly(new Item((short) 3));
            assertThat(buffer.readerIndex()).isEqualTo(2);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 4);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, false, 0", "false, true, 0", "true, false, 0", "true, true, 0",
            "false, false, 1", "false, true, 1", "true, false, 1", "true, true, 1"})
    void nestedPojoSupportsBoundedAndRemainingInput(boolean tracked, boolean bounded, short status) {
        final PojoContainer source = new PojoContainer(new PojoData(status, null, 0x1234));
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        final var registry = entityCodec.entityEncoder().getBeanMetadataRegistry();
        final var metadata = registry.getBeanMetadata(bounded ? BoundedPojoContainer.class : PojoContainer.class);
        assertThat(metadata.getPropertyMetadataList().getFirst()).isInstanceOf(NestedBeanPropertyMetadata.class);
        try {
            if (bounded) {
                entityCodec.encode(XtreamField.ALL_VERSION, new BoundedPojoContainer(source.getData()), buffer, tracker(tracked));
            } else {
                entityCodec.encode(XtreamField.ALL_VERSION, source, buffer, tracker(tracked));
            }
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo(status == 1
                    ? (bounded ? "5503011234" : "55011234") : (bounded ? "550100" : "5500"));
            buffer.writeByte(0x7e).readerIndex(1);
            final PojoData result = bounded
                    ? entityCodec.decode(XtreamField.ALL_VERSION, BoundedPojoContainer.class, buffer, tracker(tracked)).getData()
                    : entityCodec.decode(XtreamField.ALL_VERSION, PojoContainer.class, buffer, tracker(tracked)).getData();
            assertThat(result.getStatus()).isEqualTo(status);
            assertThat(result.getEnabled()).isEqualTo(status == 1);
            assertThat(result.getDetail()).isEqualTo(status == 1 ? 0x1234 : null);
            assertThat(buffer.readerIndex()).isEqualTo(2 + (bounded ? 1 : 0) + (status == 1 ? 2 : 0));
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 126);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rejectsCollectionLengthBeyondReadableInput(boolean tracked) {
        final ByteBuf buffer = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump("0401127e"));
        try {
            assertThatThrownBy(() -> entityCodec.decode(XtreamField.ALL_VERSION, RecordPacket.class, buffer, tracker(tracked)))
                    .isInstanceOf(IndexOutOfBoundsException.class);
            assertThat(buffer.readerIndex()).isEqualTo(1);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 1);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void sequenceEncodesAtomicDataFieldsAndSkipsNulls(boolean tracked) {
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        try {
            entityCodec.encode(XtreamField.ALL_VERSION,
                    new AtomicPacket(Arrays.asList(u8((short) 3), null, u8((short) 4))), buffer, tracker(tracked));
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("550304");
        } finally {
            buffer.release();
        }
    }

    private CodecTracker tracker(boolean tracked) {
        return tracked ? new CodecTracker() : null;
    }

    private void assertRange(CodecTracker tracker, String name, int start, int end) {
        final CodecTraceNode node = tracker.getTrace().getRoot().getChildren().stream()
                .filter(child -> name.equals(child.getName())).findFirst().orElseThrow();
        assertThat(node.getByteStart()).isEqualTo(start);
        assertThat(node.getByteEnd()).isEqualTo(end);
    }

    public record ConditionalRecord(
            @Preset.RustStyle.u8 Short status,
            @DerivedField(source = "status", using = EnabledTransformer.class) Boolean enabled,
            @Preset.RustStyle.u16(condition = "status == 1") Integer detail) {
    }

    public record RecordPacket(
            @Preset.RustStyle.list(prependLengthFieldType = PrependLengthFieldType.u8,
                    containerInstanceFactory = ContainerInstanceFactory.LinkedListContainerInstanceFactory.class)
            List<ConditionalRecord> entries,
            @Preset.RustStyle.u8 Short tail) {
    }

    public record Item(@Preset.RustStyle.u8 Short value) {
    }

    public record CountedPacket(
            @Preset.RustStyle.u8 Short count,
            @Preset.RustStyle.list(iterationTimesExpression = "count") List<Item> entries,
            @Preset.RustStyle.u8 Short tail) {
    }

    public record LimitedPacket(@Preset.RustStyle.list(iterationTimes = 1) List<Item> entries) {
    }

    public record AtomicPacket(@Preset.RustStyle.list List<Object> entries) {
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PojoData {
        @Preset.RustStyle.u8
        private Short status;
        @DerivedField(source = "status", using = EnabledTransformer.class)
        private Boolean enabled;
        @Preset.RustStyle.u16(condition = "status == 1")
        private Integer detail;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PojoContainer {
        @Preset.RustStyle.struct
        private PojoData data;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BoundedPojoContainer {
        @Preset.RustStyle.struct(prependLengthFieldType = PrependLengthFieldType.u8)
        private PojoData data;
    }

    public static class EnabledTransformer implements FieldTransformer<Short, Boolean> {
        @Override
        public Boolean read(@Nullable Short source) {
            return source != null && source == 1;
        }
    }
}
