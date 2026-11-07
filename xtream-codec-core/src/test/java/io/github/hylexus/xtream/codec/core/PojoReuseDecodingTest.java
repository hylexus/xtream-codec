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

package io.github.hylexus.xtream.codec.core;

import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.core.annotation.DerivedField;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * POJO 连续解码时的条件跳过、实例状态和表达式变量测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class PojoReuseDecodingTest extends BaseEntityCodecTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void skippedFieldsKeepPreviousValuesAndLaterPresentFieldsUpdateThem(boolean tracked) {
        final ReusablePojo target = new ReusablePojo();
        decodeInto(target, "01123405067e", tracked, new String[]{"flag", "detail", "variableGuard", "objectGuard", "tail"});
        assertState(target, 1, 0x1234, "hex:1234", 5, 6, 126);

        // 条件跳过不清空实例属性，但本次求值上下文中的 #detail 为空。
        decodeInto(target, "00077f", tracked, new String[]{"flag", "objectGuard", "tail"});
        assertState(target, 0, 0x1234, "hex:1234", 5, 7, 127);
        // 编码从实例属性创建变量，保留的 detail 会使 variableGuard 再次参与编码。
        assertEncoded(target, "0005077f", tracked);

        decodeInto(target, "01567808097e", tracked, new String[]{"flag", "detail", "variableGuard", "objectGuard", "tail"});
        assertState(target, 1, 0x5678, "hex:5678", 8, 9, 126);
        assertEncoded(target, "01567808097e", tracked);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void firstSkippedDecodePreservesCallerInitializationAndDerivedValue(boolean tracked) {
        final ReusablePojo target = new ReusablePojo();
        decodeInto(target, "000a7e", tracked, new String[]{"flag", "objectGuard", "tail"});
        assertState(target, 0, 0x7777, "initial", 11, 10, 126);
        assertEncoded(target, "000b0a7e", tracked);
    }

    private void decodeInto(ReusablePojo target, String hex, boolean tracked, String[] tracedFields) {
        final ByteBuf input = allocator.buffer().writeByte(0x55).writeBytes(ByteBufUtil.decodeHexDump(hex)).writeByte(0x66).readerIndex(1);
        final CodecTracker trace = tracked ? new CodecTracker() : null;
        try {
            assertThat(entityCodec.decode(XtreamField.ALL_VERSION, target, input, trace)).isSameAs(target);
            assertThat(input.readerIndex()).isEqualTo(1 + hex.length() / 2);
            assertThat(input.readUnsignedByte()).isEqualTo((short) 0x66);
            assertThat(input.refCnt()).isEqualTo(1);
            if (tracked) {
                assertThat(trace.getTrace().getRoot().getHex()).isEqualTo(hex);
                assertThat(trace.getTrace().getRoot().getChildren()).extracting(CodecTraceNode::getName).containsExactly(tracedFields);
                assertThat(trace.getTrace().getDiagnostics()).isEmpty();
            }
        } finally {
            input.release();
        }
    }

    private void assertEncoded(ReusablePojo target, String hex, boolean tracked) {
        final ByteBuf output = allocator.buffer().writeByte(0x55);
        final int detail = target.getDetail();
        final String view = target.getView();
        try {
            entityCodec.encode(XtreamField.ALL_VERSION, target, output, tracked ? new CodecTracker() : null);
            assertThat(ByteBufUtil.hexDump(output)).isEqualTo("55" + hex);
            assertThat(target.getDetail()).isEqualTo(detail);
            assertThat(target.getView()).isEqualTo(view);
        } finally {
            output.release();
        }
    }

    private static void assertState(ReusablePojo target, int flag, int detail, String view, int variableGuard, int objectGuard, int tail) {
        assertThat(target.getFlag()).isEqualTo(flag);
        assertThat(target.getDetail()).isEqualTo(detail);
        assertThat(target.getView()).isEqualTo(view);
        assertThat(target.getVariableGuard()).isEqualTo(variableGuard);
        assertThat(target.getObjectGuard()).isEqualTo(objectGuard);
        assertThat(target.getTail()).isEqualTo(tail);
    }

    @Getter
    @Setter
    public static class ReusablePojo {
        @Preset.RustStyle.u8(order = 0)
        private int flag;
        @Preset.RustStyle.u16(order = 1, condition = "flag == 1")
        private int detail = 0x7777;
        @DerivedField(source = "detail", using = HexTransformer.class)
        private String view = "initial";
        @Preset.RustStyle.u8(order = 2, condition = "#detail != null")
        private int variableGuard = 11;
        @Preset.RustStyle.u8(order = 3, condition = "detail != null")
        private int objectGuard = 12;
        @Preset.RustStyle.u8(order = 4)
        private int tail;
    }

    public static class HexTransformer implements FieldTransformer<Integer, String> {
        @Override
        public String read(@Nullable Integer source) {
            return source == null ? "empty" : "hex:" + Integer.toHexString(source);
        }
    }
}
