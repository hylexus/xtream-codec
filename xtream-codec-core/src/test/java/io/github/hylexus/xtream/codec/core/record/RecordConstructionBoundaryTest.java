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

package io.github.hylexus.xtream.codec.core.record;

import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.core.FieldTransformer;
import io.github.hylexus.xtream.codec.core.annotation.DerivedField;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
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
 * Record 的构造参数顺序、版本顺序与静态字段隔离测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class RecordConstructionBoundaryTest extends BaseEntityCodecTest {

    static Stream<Arguments> records() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, 1, new Reordered(1, 2), new Reordered(1, 2), "0201"),
                Arguments.of(tracked, 1, new MixedTypes("A", (short) 7), new MixedTypes("A", (short) 7), "0741"),
                Arguments.of(tracked, 1, new Versioned(1, 2), new Versioned(1, 2), "0102"),
                Arguments.of(tracked, 2, new Versioned(1, 2), new Versioned(1, 2), "0201"),
                Arguments.of(tracked, 1, new WithConstants(WithConstants.TYPE_ID), new WithConstants(7), "07"),
                Arguments.of(tracked, 1, new EmptyWithConstant(), new EmptyWithConstant(), ""),
                Arguments.of(tracked, 1, new DerivedOrder(7, 99, (short) 126), new DerivedOrder(7, 14, (short) 126), "7e07"),
                Arguments.of(tracked, 1, new RecordList(List.of(new Reordered(1, 2)), (short) 126),
                        new RecordList(List.of(new Reordered(1, 2)), (short) 126), "0202017e")));
    }

    @ParameterizedTest
    @MethodSource("records")
    void preservesConstructorPositionsRegardlessOfWireOrderAndStaticFields(
            boolean tracked, int version, Object source, Object expected, String hex) {
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        try {
            entityCodec.encode(version, source, buffer, tracked ? new CodecTracker() : null);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("55" + hex);
            buffer.writeByte(0x66).readerIndex(1);
            final Object result = entityCodec.decode(version, source.getClass(), buffer, tracked ? new CodecTracker() : null);
            assertThat(result).isEqualTo(expected);
            assertThat(buffer.readerIndex()).isEqualTo(1 + hex.length() / 2);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            assertThat(buffer.refCnt()).isEqualTo(1);
        } finally {
            buffer.release();
        }
    }

    public record Reordered(
            @Preset.RustStyle.u8(order = 20) int first,
            @Preset.RustStyle.u8(order = 10) int second) {
    }

    public record MixedTypes(
            @Preset.RustStyle.str(order = 20, length = 1, charset = "UTF-8") String text,
            @Preset.RustStyle.u8(order = 10) short code) {
    }

    public record Versioned(
            @Preset.RustStyle.u8(order = 20) @Preset.RustStyle.u8(version = 1, order = 5) int first,
            @Preset.RustStyle.u8(order = 10) int second) {
    }

    public record WithConstants(@Preset.RustStyle.u8 int value) {
        public static final int TYPE_ID = 7;
        public static final String NAME = "message";
    }

    public record EmptyWithConstant() {
        public static final int TYPE_ID = 8;
    }

    public record DerivedOrder(
            @Preset.RustStyle.u8(order = 20) int source,
            @DerivedField(source = "source", using = DoubleTransformer.class) int doubled,
            @Preset.RustStyle.u8(order = 10) short tail) {
    }

    public record RecordList(
            @Preset.RustStyle.list(prependLengthFieldType = PrependLengthFieldType.u8) List<Reordered> entries,
            @Preset.RustStyle.u8 short tail) {
    }

    public static class DoubleTransformer implements FieldTransformer<Integer, Integer> {
        @Override
        public @Nullable Integer read(@Nullable Integer source) {
            return source == null ? null : source * 2;
        }
    }
}
