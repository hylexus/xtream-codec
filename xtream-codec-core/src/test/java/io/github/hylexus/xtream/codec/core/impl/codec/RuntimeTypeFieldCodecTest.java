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
import io.github.hylexus.xtream.codec.core.RuntimeTypeSupplier;
import io.github.hylexus.xtream.codec.core.annotation.NumberSignedness;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.DefaultDeserializeContext;
import io.github.hylexus.xtream.codec.core.impl.DefaultSerializeContext;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class RuntimeTypeFieldCodecTest extends BaseEntityCodecTest {
    private final RuntimeTypeFieldCodec codec = RuntimeTypeFieldCodec.INSTANCE;

    static Stream<Arguments> scalarValues() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, "signed", -42, "ffffffd6"),
                Arguments.of(tracked, "signed", 0x0102030405060708L, "0102030405060708"),
                Arguments.of(tracked, "littleEndian", 0xfedc, "dcfe"),
                Arguments.of(tracked, "text", "中文A", "e4b8ade6968741")));
    }

    @ParameterizedTest
    @MethodSource("scalarValues")
    void scalarCodecUsesRuntimeClassAndFieldConfiguration(boolean tracked, String field, Object value, String hex) {
        final DynamicFields holder = new DynamicFields();
        holder.setRuntimeType(value.getClass());
        final BeanPropertyMetadata metadata = metadata(field);
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        try {
            serialize(metadata, holder, value, buffer, XtreamField.ALL_VERSION, tracked);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("55" + hex);
            buffer.writeByte(0x7e).readerIndex(1);
            final Object result = deserialize(metadata, holder, buffer, hex.length() / 2, XtreamField.ALL_VERSION, tracked);
            assertThat(result).isEqualTo(value);
            assertThat(buffer.readerIndex()).isEqualTo(1 + hex.length() / 2);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 126);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, -1", "false, 2", "true, -1", "true, 2"})
    void nestedRecordSupportsBoundedAndRemainingInput(boolean tracked, int length) {
        final DynamicFields holder = new DynamicFields();
        holder.setRuntimeType(Body.class);
        final BeanPropertyMetadata metadata = metadata(length < 0 ? "automatic" : "bounded");
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        try {
            serialize(metadata, holder, new Body(0x1234), buffer, XtreamField.ALL_VERSION, tracked);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("551234");
            buffer.writeByte(0x7e).readerIndex(1);
            assertThat(deserialize(metadata, holder, buffer, length, XtreamField.ALL_VERSION, tracked)).isEqualTo(new Body(0x1234));
            assertThat(buffer.readerIndex()).isEqualTo(3);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 126);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, 1", "false, 2", "true, 1", "true, 2"})
    void nestedCodecKeepsRequestedVersionWhenEncoding(boolean tracked, int version) {
        final DynamicFields holder = new DynamicFields();
        holder.setRuntimeType(VersionedBody.class);
        final ByteBuf buffer = allocator.buffer().writeByte(0x55);
        try {
            serialize(metadata("automatic"), holder, new VersionedBody(new VersionedChild((short) 0x12, 0x3456)), buffer, version, tracked);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo(version == 1 ? "5512" : "553456");
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, 1, -1", "false, 2, -1", "true, 1, -1", "true, 2, -1",
            "false, 1, 1", "false, 2, 2", "true, 1, 1", "true, 2, 2"})
    void nestedCodecKeepsRequestedVersionWhenDecoding(boolean tracked, int version, int length) {
        final DynamicFields holder = new DynamicFields();
        holder.setRuntimeType(VersionedBody.class);
        final ByteBuf buffer = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(version == 1 ? "55127e" : "5534567e")).readerIndex(1);
        try {
            final VersionedBody result = (VersionedBody) deserialize(metadata("automatic"), holder, buffer, length, version, tracked);
            assertThat(result.child()).isEqualTo(version == 1
                    ? new VersionedChild((short) 0x12, null) : new VersionedChild(null, 0x3456));
            assertThat(buffer.readerIndex()).isEqualTo(version == 1 ? 2 : 3);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 126);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void missingRuntimeTypeSupplierFailsWithoutConsumingInput(boolean tracked) {
        final ByteBuf input = Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4});
        try {
            assertThatThrownBy(() -> deserialize(metadata("automatic"), new Object(), input, 4, XtreamField.ALL_VERSION, tracked))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThat(input.readerIndex()).isZero();
        } finally {
            input.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void nullValueDoesNotWriteBytes(boolean tracked) {
        final ByteBuf output = allocator.buffer().writeByte(0x55);
        try {
            serialize(metadata("automatic"), new DynamicFields(), null, output, XtreamField.ALL_VERSION, tracked);
            assertThat(ByteBufUtil.hexDump(output)).isEqualTo("55");
        } finally {
            output.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rejectsNestedLengthBeyondReadableInput(boolean tracked) {
        final DynamicFields holder = new DynamicFields();
        holder.setRuntimeType(Body.class);
        final ByteBuf input = Unpooled.wrappedBuffer(new byte[]{1});
        try {
            assertThatThrownBy(() -> deserialize(metadata("bounded"), holder, input, 2, XtreamField.ALL_VERSION, tracked))
                    .isInstanceOf(IndexOutOfBoundsException.class);
            assertThat(input.readerIndex()).isZero();
        } finally {
            input.release();
        }
    }

    private void serialize(BeanPropertyMetadata metadata, Object holder, Object value, ByteBuf output, int version, boolean tracked) {
        final CodecTracker tracker = tracker(tracked, false, output.writerIndex());
        final var context = new DefaultSerializeContext(allocator, entityCodec.entityEncoder(), holder, version,
                entityCodec.entityEncoder().getBeanMetadataRegistry(), tracker);
        if (tracked) {
            codec.serializeWithTracker(metadata, context, output, value);
        } else {
            codec.serialize(metadata, context, output, value);
        }
    }

    private Object deserialize(BeanPropertyMetadata metadata, Object holder, ByteBuf input, int length, int version, boolean tracked) {
        final CodecTracker tracker = tracker(tracked, true, input.readerIndex());
        final var context = new DefaultDeserializeContext(allocator, entityCodec.entityDecoder(), holder, version,
                entityCodec.entityDecoder().getBeanMetadataRegistry(), tracker);
        return tracked ? codec.deserializeWithTracker(metadata, context, input, length) : codec.deserialize(metadata, context, input, length);
    }

    private BeanPropertyMetadata metadata(String field) {
        return entityCodec.entityEncoder().getBeanMetadataRegistry().getBeanMetadata(DynamicFields.class)
                .getPropertyMetadataList().stream().filter(p -> p.name().equals(field)).findFirst().orElseThrow();
    }

    private CodecTracker tracker(boolean tracked, boolean decode, int start) {
        if (!tracked) {
            return null;
        }
        final CodecTracker tracker = new CodecTracker();
        if (decode) {
            tracker.beginDecode(start, null);
        } else {
            tracker.beginEncode(start, null);
        }
        return tracker;
    }

    @Getter
    @Setter
    public static class DynamicFields implements RuntimeTypeSupplier {
        @Preset.RustStyle.dyn
        private Object automatic;

        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.dynamic, signedness = NumberSignedness.SIGNED)
        private Object signed;

        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.dynamic, length = 2,
                signedness = NumberSignedness.UNSIGNED, littleEndian = true)
        private Object littleEndian;

        @Preset.RustStyle.dyn(length = 2)
        private Object bounded;

        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.dynamic, charset = "UTF-8")
        private Object text;

        private Class<?> runtimeType;

        @Override
        public Class<?> getRuntimeType(String name) {
            return runtimeType;
        }
    }

    public record Body(@Preset.RustStyle.u16 Integer id) {
    }

    public record VersionedBody(@Preset.JtStyle.Object VersionedChild child) {
    }

    public record VersionedChild(
            @Preset.RustStyle.u8(version = 1) Short first,
            @Preset.RustStyle.u16(version = 2) Integer second) {
    }
}
