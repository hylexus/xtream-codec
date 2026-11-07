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

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicExpression;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchemaJson;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 动态版本过滤后的集合元素、空结构和分支边界测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicVersionBoundaryTest {
    static Stream<Arguments> excludedItems() {
        return Stream.of(false, true).flatMap(json -> Stream.of(DynamicSchema.ALL_VERSION, 1).flatMap(version ->
                Stream.of(0, 1).map(count -> Arguments.of(json, version, count))));
    }

    @ParameterizedTest
    @MethodSource("excludedItems")
    void rejectsExcludedArrayItemEvenWhenTheDeclaredCountIsZero(boolean json, int version, int count) {
        final DynamicSchema schema = DynamicSchema.of(
                DynamicField.field("items", "array").length(DynamicLength.fixed(count, "item"))
                        .item(DynamicField.field("item", "u8").versions(2).build()).build());
        final DynamicSchema selected = json ? imported(schema) : schema;
        final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                () -> DynamicCompiler.compile(selected, version));
        assertThat(error).hasMessageContaining("当前版本的数组元素定义不存在");
        assertThat(error.fieldPath()).isEqualTo("$.items");
        assertThat(error.bitOffset()).isEqualTo(-1);
        assertThat(DynamicCompiler.compile(selected, 2).version()).isEqualTo(2);
    }

    static Stream<Arguments> excludedCollections() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(DynamicSchema.ALL_VERSION, 1)
                .map(version -> Arguments.of(tracked, version)));
    }

    @ParameterizedTest
    @MethodSource("excludedCollections")
    void excludingEntireCollectionSkipsItsUnavailableItemDefinition(boolean tracked, int version) {
        final DynamicSchema schema = imported(DynamicSchema.of(
                DynamicField.field("head", "u8").build(),
                DynamicField.field("items", "array").versions(2).length(DynamicLength.fixed(1, "item"))
                        .item(DynamicField.field("item", "u8").versions(3).build()).build(),
                DynamicField.field("tail", "u8").build()));
        assertRoundTrip(DynamicCompiler.compile(schema, version),
                Map.of("head", 126, "items", List.of(9), "tail", 127),
                Map.of("head", 126L, "tail", 127L), "7e7f", tracked);
        assertThat(schema.fields().get(1).versions()).containsExactly(2);
        assertThat(schema.fields().get(1).item()).isNotNull();
    }

    static Stream<Arguments> absentChoices() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(false, true)
                .map(excludedDefault -> Arguments.of(tracked, excludedDefault)));
    }

    @ParameterizedTest
    @MethodSource("absentChoices")
    void noEffectiveChoiceBranchFailsAtExecutionAndPreservesFailureState(boolean tracked, boolean excludedDefault) {
        final DynamicField.Builder choice = DynamicField.field("body", "choice").ref("kind").variants(Map.of(
                "1", DynamicField.field("known", "u8").versions(2).build()));
        if (excludedDefault) {
            choice.defaultVariant(DynamicField.field("fallback", "u16").versions(2).build());
        }
        final DynamicPlan plan = DynamicCompiler.compile(imported(DynamicSchema.of(
                DynamicField.field("kind", "u8").build(), choice.build(), DynamicField.field("tail", "u8").build())), 1);
        assertExecutionFailure(plan, Map.of("kind", 1, "body", 17, "tail", 127), "01117f", "01",
                "$.body", "没有匹配的判别分支", tracked);
    }

    static Stream<Arguments> effectiveChoices() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, 1, 1, 4660, "0112347f"),
                Arguments.of(tracked, 1, 99, 4660, "6312347f"),
                Arguments.of(tracked, 2, 1, 17, "01117f")));
    }

    @ParameterizedTest
    @MethodSource("effectiveChoices")
    void excludedKnownBranchUsesOnlyTheEffectiveDefault(boolean tracked, int version, int kind, int body, String hex) {
        final DynamicPlan plan = DynamicCompiler.compile(imported(DynamicSchema.of(
                DynamicField.field("kind", "u8").build(),
                DynamicField.field("body", "choice").ref("kind").variants(Map.of(
                                "1", DynamicField.field("known", "u8").versions(2).build()))
                        .defaultVariant(DynamicField.field("fallback", "u16").versions(1).build()).build(),
                DynamicField.field("tail", "u8").build())), version);
        assertRoundTrip(plan, Map.of("kind", kind, "body", body, "tail", 127),
                Map.of("kind", (long) kind, "body", (long) body, "tail", 127L), hex, tracked);
    }

    static Stream<Arguments> emptyStructures() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, 1, Map.of(), "7e7f"),
                Arguments.of(tracked, 2, Map.of("number", 9L), "7e097f")));
    }

    @ParameterizedTest
    @MethodSource("emptyStructures")
    void keepsAnEmptySelectedStructWhenOnlyItsChildrenAreExcluded(boolean tracked, int version, Map<String, Object> expectedBody, String hex) {
        final DynamicPlan plan = DynamicCompiler.compile(imported(DynamicSchema.of(
                DynamicField.field("head", "u8").build(),
                DynamicField.field("body", "struct").fields(DynamicField.field("number", "u8").versions(2).build()).build(),
                DynamicField.field("copy", "derived").expression(DynamicExpression.ref("body")).build(),
                DynamicField.field("tail", "u8").build())), version);
        assertRoundTrip(plan, Map.of("head", 126, "body", Map.of("number", 9), "tail", 127),
                Map.of("head", 126L, "body", expectedBody, "copy", expectedBody, "tail", 127L), hex, tracked);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void emptyStructArrayItemsAreRejectedForNotAdvancingTheCursor(boolean tracked) {
        final DynamicSchema schema = imported(DynamicSchema.of(
                DynamicField.field("head", "u8").build(),
                DynamicField.field("items", "array").length(DynamicLength.fixed(2, "item"))
                        .item(DynamicField.field("item", "struct")
                                .fields(DynamicField.field("number", "u8").versions(2).build()).build()).build(),
                DynamicField.field("tail", "u8").build()));
        final Map<String, Object> input = Map.of("head", 126, "items", List.of(Map.of("number", 9), Map.of("number", 10)), "tail", 127);
        assertExecutionFailure(DynamicCompiler.compile(schema, 1), input, "7e090a7f", "7e",
                "$.items", "集合元素未推进游标", tracked);
        assertRoundTrip(DynamicCompiler.compile(schema, 2), input,
                Map.of("head", 126L, "items", List.of(Map.of("number", 9L), Map.of("number", 10L)), "tail", 127L), "7e090a7f", tracked);
    }

    private static void assertRoundTrip(DynamicPlan plan, Map<String, Object> input, Map<String, Object> expected, String hex, boolean tracked) {
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            plan.encode(DynamicValues.of(input), buffer, encoder);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("55" + hex);
            buffer.writeByte(0x66).readerIndex(1);
            final var result = plan.decode(buffer, decoder);
            assertThat(result.values().fields()).isEqualTo(expected);
            assertThat(result.consumedBytes()).isEqualTo(hex.length() / 2);
            assertThat(result.remainingBytes()).isEqualTo(1);
            assertThat(buffer.readerIndex()).isEqualTo(1 + hex.length() / 2);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (encoder != null && decoder != null) {
                for (final CodecTracker tracker : List.of(encoder, decoder)) {
                    assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                    assertThat(tracker.getTrace().getRoot().getChildren()).extracting(CodecTraceNode::getName)
                            .containsExactlyInAnyOrderElementsOf(expected.keySet());
                    assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
                }
            }
        } finally {
            buffer.release();
        }
    }

    private static void assertExecutionFailure(DynamicPlan plan, Map<String, Object> input, String wireHex, String partialHex,
                                              String path, String message, boolean tracked) {

        final ByteBuf encoded = Unpooled.buffer().writeByte(0x55);
        final ByteBuf decoded = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump("55" + wireHex + "66")).readerIndex(1);
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            final DynamicCodecException writeError = catchThrowableOfType(DynamicCodecException.class,
                    () -> plan.encode(DynamicValues.of(input), encoded, encoder));
            final DynamicCodecException readError = catchThrowableOfType(DynamicCodecException.class, () -> plan.decode(decoded, decoder));
            for (final DynamicCodecException error : List.of(writeError, readError)) {
                assertThat(error).hasMessageContaining(message);
                assertThat(error.fieldPath()).isEqualTo(path);
                assertThat(error.bitOffset()).isEqualTo(8);
            }
            assertThat(ByteBufUtil.hexDump(encoded)).isEqualTo("55" + partialHex);
            assertThat(decoded.readerIndex()).isEqualTo(1);
            assertThat(ByteBufUtil.hexDump(decoded, 0, decoded.writerIndex())).isEqualTo("55" + wireHex + "66");
            assertThat(encoded.refCnt()).isEqualTo(1);
            assertThat(decoded.refCnt()).isEqualTo(1);
            assertFailureTrace(encoder);
            assertFailureTrace(decoder);
        } finally {
            encoded.release();
            decoded.release();
        }
    }

    private static void assertFailureTrace(@Nullable CodecTracker tracker) {
        if (tracker != null) {
            final var root = tracker.getTrace().getRoot();
            assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
            assertThat(root.getChildren()).hasSize(2);
            assertThat(root.getChildren().getFirst().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
            assertThat(root.getChildren().getLast().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
            assertThat(tracker.getTrace().getDiagnostics()).hasSize(1);
            assertThat(tracker.getTrace().getDiagnostics().getFirst().bitOffset()).isEqualTo(8L);
        }
    }

    private static DynamicSchema imported(DynamicSchema schema) {
        return DynamicSchemaJson.read(DynamicSchemaJson.write(schema));
    }
}
