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

package io.github.hylexus.xtream.codec.core.debugger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicCompileOptions;
import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicLimits;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceDirection;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceView;
import io.github.hylexus.xtream.codec.core.type.Preset;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 调试服务输入输出资源边界、部分响应与后续请求隔离测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class CodecDebuggerResourceLimitTest {
    private static final DynamicSchema PACKET = DynamicSchema.of(
            DynamicField.field("head", "u8").build(), DynamicField.field("number", "u16").build());
    private static final DynamicSchema SMALL = DynamicSchema.of(DynamicField.field("head", "u8").build());
    private static final DynamicSchema EMPTY = DynamicSchema.of();
    private static final Map<String, Object> PACKET_VALUES = Map.of("head", 126, "number", 0x1234);

    static Stream<Arguments> inputLimits() {
        return Stream.of(DebugCodecMode.values()).flatMap(mode -> Stream.of(0, 2, 3).flatMap(limit ->
                Stream.of(false, true).map(whitespace -> Arguments.of(mode, limit, whitespace))));
    }

    static Stream<Arguments> outputLimits() {
        return Stream.of(DebugCodecMode.values()).flatMap(mode -> Stream.of(0, 2, 3).map(limit -> Arguments.of(mode, limit)));
    }

    @ParameterizedTest
    @MethodSource("inputLimits")
    void checksNormalizedHexSizeBeforeStartingDecode(DebugCodecMode mode, int limit, boolean whitespace) {
        final var service = service(limit, 3);
        final String hex = whitespace ? " \t7e 12\n34\r " : "7e1234";
        final var response = service.execute(request(mode, DebugCodecDirection.DECODE, "packet", PACKET, null, hex));
        if (limit == 3) {
            assertPacket(response, CodecTraceDirection.DECODE);
        } else {
            assertThat(response.success()).isFalse();
            assertThat(response.values()).isNull();
            assertThat(response.hex()).isEmpty();
            assertThat(response.consumedBits()).isZero();
            assertThat(response.consumedBytes()).isZero();
            assertThat(response.remainingHex()).isEmpty();
            assertThat(response.checksums()).isEmpty();
            assertThat(response.error()).isNotNull().satisfies(error -> {
                assertThat(error.message()).isEqualTo("输入超过资源上限");
                assertThat(error.fieldPath()).isNull();
                assertThat(error.bitOffset()).isNull();
            });
            assertThat(response.trace().direction()).isEqualTo(CodecTraceDirection.UNKNOWN);
            assertThat(response.trace().payloadHex()).isNull();
            assertThat(response.trace().root().status()).isEqualTo(CodecTraceStatus.STARTED);
            assertThat(response.trace().root().children()).isEmpty();
            assertThat(response.trace().root().byteStart()).isNull();
            assertThat(response.trace().root().byteEnd()).isNull();
            assertThat(response.trace().diagnostics()).isEmpty();
        }
        assertNextRequest(service, mode, limit == 0);
    }

    @ParameterizedTest
    @MethodSource("outputLimits")
    void preservesOnlyCompletedFieldsWhenOutputLimitIsExceeded(DebugCodecMode mode, int limit) {
        final var service = service(3, limit);
        final var response = service.execute(request(mode, DebugCodecDirection.ENCODE, "packet", PACKET, PACKET_VALUES, null));
        if (limit == 3) {
            assertPacket(response, CodecTraceDirection.ENCODE);
        } else {
            final int completed = limit == 0 ? 0 : 1;
            final String failedField = limit == 0 ? "head" : "number";
            assertThat(response.success()).isFalse();
            assertThat(response.values()).isNull();
            assertThat(response.hex()).isEqualTo(completed == 0 ? "" : "7e");
            assertThat(response.consumedBits()).isEqualTo(completed * 8L);
            assertThat(response.consumedBytes()).isEqualTo(completed);
            assertThat(response.remainingHex()).isEmpty();
            assertThat(response.checksums()).isEmpty();
            assertThat(response.trace().direction()).isEqualTo(CodecTraceDirection.ENCODE);
            assertThat(response.trace().root().status()).isEqualTo(CodecTraceStatus.ERROR);
            if (mode == DebugCodecMode.ENTITY && completed == 0) {
                assertThat(response.trace().root().byteEnd()).isNull();
            } else {
                assertThat(response.trace().root().byteEnd()).isEqualTo(completed);
            }
            final var children = response.trace().root().children();
            assertThat(children).extracting(CodecTraceView.Node::name)
                    .containsExactlyElementsOf(completed == 0 ? List.of("head") : List.of("head", "number"));
            if (completed != 0) {
                assertThat(children.getFirst().status()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertThat(children.getFirst().hex()).isEqualTo("7e");
                assertThat(((Number) children.getFirst().value()).longValue()).isEqualTo(126);
            }
            final var failed = children.getLast();
            assertThat(failed.status()).isEqualTo(CodecTraceStatus.ERROR);
            assertThat(failed.byteStart()).isEqualTo(completed);
            assertThat(failed.byteEnd()).isEqualTo(completed);
            assertThat(response.error()).isNotNull().satisfies(error -> {
                if (mode == DebugCodecMode.DYNAMIC) {
                    assertThat(error.message()).contains("输出超过边界或资源上限");
                    assertThat(error.fieldPath()).isEqualTo("$." + failedField);
                    assertThat(error.bitOffset()).isEqualTo(completed * 8L);
                } else {
                    if (limit == 0) {
                        assertThat(error.message()).isEqualTo("IndexOutOfBoundsException");
                    } else {
                        assertThat(error.message()).contains("maxCapacity");
                    }
                    assertThat(error.fieldPath()).isNull();
                    assertThat(error.bitOffset()).isNull();
                }
            });
            assertThat(response.trace().diagnostics()).isNotEmpty().allSatisfy(diagnostic -> {
                assertThat(diagnostic.level()).isEqualTo("ERROR");
                assertThat(diagnostic.byteOffset()).isEqualTo(completed);
            });
            assertThat(response.trace().diagnostics()).anySatisfy(diagnostic -> {
                assertThat(diagnostic.nodeId()).isEqualTo(failed.id());
                if (mode == DebugCodecMode.ENTITY) {
                    assertThat(diagnostic.exceptionType()).isEqualTo(IndexOutOfBoundsException.class.getName());
                }
            });
        }
        assertNextRequest(service, mode, limit == 0);
    }

    @ParameterizedTest
    @EnumSource(DebugCodecMode.class)
    void acceptsEmptyMessagesWithZeroInputAndOutputCapacity(DebugCodecMode mode) {
        assertNextRequest(service(0, 0), mode, true);
    }

    private static CodecDebuggerService service(int input, int output) {
        // 允许宿主映射无字段实体，以验证零字节消息的真实编解码。
        final var mapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return new CodecDebuggerService(new DebugEntityCatalog(Map.of("packet", Packet.class, "small", Small.class, "empty", Empty.class)),
                new Jackson2DebugEntityValueMapper(mapper), DynamicCompileOptions.DEFAULT, new DynamicLimits(input, output, 100, 16));
    }

    private static DebugCodecRequest request(DebugCodecMode mode, DebugCodecDirection direction, String entity,
                                             DynamicSchema schema, @Nullable Map<String, Object> values, @Nullable String hex) {
        return new DebugCodecRequest(mode, direction, mode == DebugCodecMode.ENTITY ? entity : null, null,
                mode == DebugCodecMode.DYNAMIC ? schema : null, values, hex);
    }

    private static void assertNextRequest(CodecDebuggerService service, DebugCodecMode mode, boolean empty) {
        final String entity = empty ? "empty" : "small";
        final DynamicSchema schema = empty ? EMPTY : SMALL;
        final Map<String, Object> values = empty ? Map.of() : Map.of("head", 127);
        final String hex = empty ? "" : "7f";
        final var encoded = service.execute(request(mode, DebugCodecDirection.ENCODE, entity, schema, values, null));
        assertSuccess(encoded, hex, CodecTraceDirection.ENCODE);
        final var decoded = service.execute(request(mode, DebugCodecDirection.DECODE, entity, schema, null, " \t" + hex + "\n"));
        assertSuccess(decoded, hex, CodecTraceDirection.DECODE);
        assertThat(decoded.values()).isEqualTo(encoded.values());
        if (empty) {
            assertThat(decoded.values()).isEmpty();
            assertThat(decoded.trace().root().children()).isEmpty();
            assertThat(encoded.trace().root().children()).isEmpty();
        } else {
            assertThat(((Number) decoded.values().get("head")).longValue()).isEqualTo(127);
            assertThat(decoded.trace().root().children()).extracting(CodecTraceView.Node::name).containsExactly("head");
            assertThat(encoded.trace().root().children()).extracting(CodecTraceView.Node::name).containsExactly("head");
        }
    }

    private static void assertPacket(DebugCodecResponse response, CodecTraceDirection direction) {
        assertSuccess(response, "7e1234", direction);
        assertThat(response.values()).hasSize(2);
        assertThat(((Number) response.values().get("head")).longValue()).isEqualTo(126);
        assertThat(((Number) response.values().get("number")).longValue()).isEqualTo(0x1234);
        assertThat(response.trace().root().children()).extracting(CodecTraceView.Node::name).containsExactly("head", "number");
    }

    private static void assertSuccess(DebugCodecResponse response, String hex, CodecTraceDirection direction) {
        assertThat(response.success()).as("%s", response.error()).isTrue();
        assertThat(response.error()).isNull();
        assertThat(response.hex()).isEqualTo(hex);
        assertThat(response.consumedBytes()).isEqualTo(hex.length() / 2);
        assertThat(response.consumedBits()).isEqualTo(hex.length() * 4L);
        assertThat(response.remainingHex()).isEmpty();
        assertThat(response.checksums()).isEmpty();
        assertThat(response.trace().direction()).isEqualTo(direction);
        assertThat(response.trace().root().status()).isEqualTo(CodecTraceStatus.SUCCESS);
        assertThat(response.trace().root().byteStart()).isZero();
        assertThat(response.trace().root().byteEnd()).isEqualTo(hex.length() / 2);
        assertThat(response.trace().root().children()).allSatisfy(node -> assertThat(node.status()).isEqualTo(CodecTraceStatus.SUCCESS));
        assertThat(response.trace().diagnostics()).isEmpty();
    }

    public static class Packet {
        @Preset.RustStyle.u8(order = 0)
        private int head;
        @Preset.RustStyle.u16(order = 1)
        private int number;

        public int getHead() {
            return head;
        }

        public void setHead(int head) {
            this.head = head;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }

    public static class Small {
        @Preset.RustStyle.u8(order = 0)
        private int head;

        public int getHead() {
            return head;
        }

        public void setHead(int head) {
            this.head = head;
        }
    }

    public static class Empty {
    }
}
