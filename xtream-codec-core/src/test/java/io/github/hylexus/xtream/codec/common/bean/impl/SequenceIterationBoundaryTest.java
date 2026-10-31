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
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 集合的最大迭代次数、字节边界与零消费元素测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class SequenceIterationBoundaryTest extends BaseEntityCodecTest {

    @ParameterizedTest
    @CsvSource({"false, 1023", "true, 1023", "false, 1024", "true, 1024", "false, 1025", "true, 1025"})
    void defaultLimitCapsResultsAndConsumesEntireBoundedRegion(boolean tracked, int count) {
        final List<Item> items = Collections.nCopies(count, new Item((short) 3));
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        final CodecTracker trace = tracker(tracked);
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, new BoundedPacket(items, (short) 126), buffer, null);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("55" + "%04x".formatted(count) + "03".repeat(count) + "7e");
            buffer.writeByte(0x66).readerIndex(1);
            final BoundedPacket result = entityCodec.decode(XtreamField.ALL_VERSION, BoundedPacket.class, buffer, trace);
            assertThat(result.items()).containsExactlyElementsOf(items.subList(0, Math.min(count, 1024)));
            assertThat(result.tail()).isEqualTo((short) 126);
            assertThat(buffer.readerIndex()).isEqualTo(count + 4);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (tracked) {
                final var collection = trace.getTrace().getRoot().getChildren().getFirst();
                assertThat(collection.getName()).isEqualTo("items");
                assertThat(collection.getByteStart()).isEqualTo(2);
                assertThat(collection.getByteEnd()).isEqualTo(count + 2);
                assertThat(collection.getChildren()).hasSize(Math.min(count, 1024));
                assertThat(trace.getTrace().getDiagnostics()).isEmpty();
            }
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void explicitLimitCanDecodeMoreThanDefaultMaximum(boolean tracked) {
        final LargePacket packet = new LargePacket(Collections.nCopies(1025, new Item((short) 3)), (short) 126);
        final ByteBuf buffer = allocator.buffer();
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, packet, buffer, null);
            assertThat(buffer.getUnsignedShort(0)).isEqualTo(1025);
            assertThat(entityCodec.decode(XtreamField.ALL_VERSION, LargePacket.class, buffer, tracker(tracked))).isEqualTo(packet);
            assertThat(buffer.readableBytes()).isZero();
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void unboundedInputLeavesBytesAfterDefaultMaximumUnread(boolean tracked) {
        final ByteBuf buffer = allocator.buffer().writeByte(0x55).writeBytes(new byte[1025]).writeByte(0x66).readerIndex(1);
        try {
            final UnboundedPacket result = entityCodec.decode(XtreamField.ALL_VERSION, UnboundedPacket.class, buffer, tracker(tracked));
            assertThat(result.items()).containsExactlyElementsOf(Collections.nCopies(1024, new Item((short) 0)));
            assertThat(buffer.readerIndex()).isEqualTo(1025);
            assertThat(buffer.readUnsignedByte()).isZero();
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, 0", "true, 0", "false, 1", "true, 1", "false, 3", "true, 3"})
    void byteLengthAndIterationCountAreIndependentUpperBounds(boolean tracked, short count) {
        final ByteBuf buffer = allocator.buffer().writeByte(0x55).writeByte(count).writeBytes(ByteBufUtil.decodeHexDump("03047e66")).readerIndex(1);
        try {
            final CountedPacket result = entityCodec.decode(XtreamField.ALL_VERSION, CountedPacket.class, buffer, tracker(tracked));
            assertThat(result.count()).isEqualTo(count);
            assertThat(result.items()).containsExactlyElementsOf(List.of(new Item((short) 3), new Item((short) 4)).subList(0, Math.min(count, 2)));
            assertThat(result.tail()).isEqualTo((short) 126);
            assertThat(buffer.readerIndex()).isEqualTo(5);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void zeroByteRegionWinsOverPositiveIterationCount(boolean tracked) {
        final ByteBuf buffer = allocator.buffer().writeByte(0x7e);
        try {
            assertThat(entityCodec.decode(XtreamField.ALL_VERSION, EmptyRegionPacket.class, buffer, tracker(tracked)))
                    .isEqualTo(new EmptyRegionPacket(List.of(), (short) 126));
            assertThat(buffer.readerIndex()).isEqualTo(1);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void zeroConsumptionElementsStillStopAtIterationLimit(boolean tracked) {
        final ByteBuf buffer = allocator.buffer().writeByte(0x55).writeByte(0x7e).writeByte(0x66).readerIndex(1);
        final CodecTracker trace = tracker(tracked);
        try {
            final ZeroConsumptionPacket result = entityCodec.decode(XtreamField.ALL_VERSION, ZeroConsumptionPacket.class, buffer, trace);
            assertThat(result.items()).containsExactly(new SkippedItem(0), new SkippedItem(0));
            assertThat(result.items().getFirst()).isNotSameAs(result.items().getLast());
            assertThat(result.tail()).isEqualTo((short) 126);
            assertThat(buffer.readerIndex()).isEqualTo(2);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            if (tracked) {
                final var collection = trace.getTrace().getRoot().getChildren().getFirst();
                assertThat(collection.getByteStart()).isZero();
                assertThat(collection.getByteEnd()).isZero();
                assertThat(collection.getChildren()).hasSize(2).allSatisfy(item -> {
                    assertThat(item.getByteStart()).isZero();
                    assertThat(item.getByteEnd()).isZero();
                });
            }
        } finally {
            buffer.release();
        }
    }

    private CodecTracker tracker(boolean tracked) {
        return tracked ? new CodecTracker() : null;
    }

    public record Item(@Preset.RustStyle.u8 short value) {
    }

    public record BoundedPacket(@Preset.RustStyle.list(prependLengthFieldType = PrependLengthFieldType.u16) List<Item> items,
                                 @Preset.RustStyle.u8 short tail) {
    }

    public record LargePacket(@Preset.RustStyle.list(prependLengthFieldType = PrependLengthFieldType.u16, iterationTimes = 1025) List<Item> items,
                               @Preset.RustStyle.u8 short tail) {
    }

    public record UnboundedPacket(@Preset.RustStyle.list List<Item> items) {
    }

    public record CountedPacket(@Preset.RustStyle.u8 short count,
                                 @Preset.RustStyle.list(length = 2, iterationTimesExpression = "count") List<Item> items,
                                 @Preset.RustStyle.u8 short tail) {
    }

    public record EmptyRegionPacket(@Preset.RustStyle.list(lengthExpression = "0", iterationTimes = 1) List<Item> items,
                                     @Preset.RustStyle.u8 short tail) {
    }

    public record SkippedItem(@Preset.RustStyle.u8(condition = "false") int value) {
    }

    public record ZeroConsumptionPacket(@Preset.RustStyle.list(iterationTimes = 2) List<SkippedItem> items,
                                         @Preset.RustStyle.u8 short tail) {
    }
}
