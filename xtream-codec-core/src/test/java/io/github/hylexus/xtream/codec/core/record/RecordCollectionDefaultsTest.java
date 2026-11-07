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
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 集合 Record 元素的条件、瞬态和派生字段默认值测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class RecordCollectionDefaultsTest extends BaseEntityCodecTest {

    @ParameterizedTest
    @CsvSource({"false, 1", "true, 1", "false, 2", "true, 2"})
    void defaultsMatchTopLevelRecordsAndKeepFollowingFields(boolean tracked, int version) {
        final Entry skipped = new Entry(0, 0, 0, false, null, List.of(), 0, 0, 0, (short) 126);
        final Entry present = new Entry(1, 0x1234, 0, false, null, List.of(), 0, 0, version == 1 ? 1 : 0, (short) 126);
        final Packet packet = new Packet(List.of(skipped, present), (short) 127);
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        try {
            entityCodec.encode(version, packet, buffer, tracked ? new CodecTracker() : null);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("5506007e0112347e7f");
            buffer.writeByte(0x66).readerIndex(1);
            final Packet result = entityCodec.decode(version, Packet.class, buffer, tracked ? new CodecTracker() : null);
            assertThat(result).isEqualTo(packet);
            assertThat(buffer.readerIndex()).isEqualTo(9);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            assertThat(result.entries().getFirst().empty()).isNotSameAs(result.entries().getLast().empty());
        } finally {
            buffer.release();
        }
    }

    public record Entry(
            @Preset.RustStyle.u8 int enabled,
            @Preset.RustStyle.u16(condition = "enabled == 1") int detail,
            @Preset.RustStyle.u16(condition = "false") int unused,
            boolean transientFlag,
            @Preset.RustStyle.transient_rc(nulls = XtreamField.Nulls.AS_NULL) String absent,
            @Preset.RustStyle.transient_rc(nulls = XtreamField.Nulls.AS_EMPTY) List<String> empty,
            @DerivedField(source = "enabled", using = NullTransformer.class) int nullDerived,
            @DerivedField(source = "enabled", using = FailingTransformer.class) long failedDerived,
            @DerivedField(source = "enabled", using = IdentityTransformer.class, version = 1) int versionedDerived,
            @Preset.RustStyle.u8 short tail) {
    }

    public record Packet(
            @Preset.RustStyle.list(prependLengthFieldType = PrependLengthFieldType.u8) List<Entry> entries,
            @Preset.RustStyle.u8 short tail) {
    }

    public static class NullTransformer implements FieldTransformer<Integer, Integer> {
        @Override
        public @Nullable Integer read(@Nullable Integer source) {
            return null;
        }
    }

    public static class FailingTransformer implements FieldTransformer<Integer, Long> {
        @Override
        public Long read(@Nullable Integer source) {
            throw new IllegalArgumentException("测试派生失败");
        }
    }

    public static class IdentityTransformer implements FieldTransformer<Integer, Integer> {
        @Override
        public @Nullable Integer read(@Nullable Integer source) {
            return source;
        }
    }
}
