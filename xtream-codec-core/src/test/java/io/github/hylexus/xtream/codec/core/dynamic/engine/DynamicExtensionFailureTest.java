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

package io.github.hylexus.xtream.codec.core.dynamic.engine;

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtension;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtensionRegistry;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HexFormat;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * 动态扩展的运行时异常、输出边界与失败后再次调用测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicExtensionFailureTest {
    private static final IllegalArgumentException FAILURE = new IllegalArgumentException("测试扩展失败");
    private static final DynamicPlan PLAN = DynamicCompiler.compile(DynamicSchema.of(
            DynamicField.field("head", "u8").build(),
            DynamicField.field("data", "extension").length(DynamicLength.fixed(2, "byte"))
                    .options(Map.of("extensionId", "test")).build(),
            DynamicField.field("tail", "u8").build()), new DynamicExtensionRegistry(Map.of("test", new TestExtension())));

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void encodeFailureKeepsCauseAndPartialOutputWithoutPoisoningThePlan(boolean tracked) {
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                    () -> PLAN.encode(values("encode-error"), buffer, tracker));
            assertThat(error.getCause()).isSameAs(FAILURE);
            assertFailure(error, 8, tracker);
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo("5507");
            assertThat(buffer.readerIndex()).isEqualTo(1);
            assertThat(buffer.refCnt()).isEqualTo(1);
            buffer.writerIndex(1);
            PLAN.encode(values("aabb"), buffer, tracked ? new CodecTracker() : null);
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo("5507aabb7e");
            assertThat(PLAN.decode(buffer).values()).isEqualTo(values("aabb"));
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void decodeFailureDoesNotCommitInputAndLaterDecodeCanSucceed(boolean tracked) {
        final ByteBuf buffer = Unpooled.wrappedBuffer(HexFormat.of().parseHex("5507ff007e66")).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class, () -> PLAN.decode(buffer, tracker));
            assertThat(error.getCause()).isSameAs(FAILURE);
            assertFailure(error, 24, tracker);
            assertThat(buffer.readerIndex()).isEqualTo(1);
            assertThat(buffer.refCnt()).isEqualTo(1);
            buffer.setShort(2, 0xaabb);
            assertThat(PLAN.decode(buffer, tracked ? new CodecTracker() : null).values()).isEqualTo(values("aabb"));
            assertThat(buffer.readerIndex()).isEqualTo(5);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"false, false", "true, false", "false, true", "true, true"})
    void rejectsOversizedExtensionOutputBeforeWritingDataOrTail(boolean tracked, boolean limited) {
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        final DynamicLimits limits = limited ? new DynamicLimits(16, 2, 16, 8) : DynamicLimits.DEFAULT;
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                    () -> PLAN.encode(values(limited ? "aabb" : "aabbcc"), buffer, limits, tracker));
            assertFailure(error, 8, tracker);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("5507");
            assertThat(buffer.refCnt()).isEqualTo(1);
        } finally {
            buffer.release();
        }
    }

    private static void assertFailure(DynamicCodecException error, long offset, @Nullable CodecTracker tracker) {
        assertThat(error.fieldPath()).isEqualTo("$.data");
        assertThat(error.bitOffset()).isEqualTo(offset);
        if (tracker != null) {
            assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
            assertThat(tracker.getTrace().getDiagnostics()).isNotEmpty();
        }
    }

    private static DynamicValues values(String data) {
        return DynamicValues.of(Map.of("head", 7L, "data", data, "tail", 126L));
    }

    private static class TestExtension implements DynamicExtension {
        @Override
        public void validate(Map<String, Object> config) {
            // 此测试扩展仅通过固定注册标识使用，无额外配置。
        }

        @Override
        public byte[] encode(Object value, Map<String, Object> config) {
            if ("encode-error".equals(value)) {
                throw FAILURE;
            }
            return HexFormat.of().parseHex(value.toString());
        }

        @Override
        public Object decode(byte[] bytes, Map<String, Object> config) {
            if (bytes[0] == (byte) 0xff) {
                throw FAILURE;
            }
            return HexFormat.of().formatHex(bytes);
        }
    }
}
