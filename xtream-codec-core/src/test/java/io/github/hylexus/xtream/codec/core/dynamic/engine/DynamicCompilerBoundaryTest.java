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
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicGeneratedValue;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchemaJson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 动态结构编译深度、引用类型及计数目标的边界与合法对照测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicCompilerBoundaryTest {
    enum ReferenceUse {
        LENGTH, CHOICE, DERIVED
    }

    static Stream<Arguments> depths() {
        return Stream.of(false, true).flatMap(json -> Stream.of(63, 64, 65).map(depth -> Arguments.of(json, depth)));
    }

    @ParameterizedTest
    @MethodSource("depths")
    void checksCompileDepthIndependentlyOfExecutionLimits(boolean json, int depth) {
        DynamicField nested = DynamicField.field("leaf", "u8").build();
        Map<String, Object> supplied = Map.of("leaf", 0x34);
        Map<String, Object> expected = Map.of("leaf", 0x34L);
        for (int index = depth - 1; index >= 0; index--) {
            final String id = "level" + index;
            nested = DynamicField.field(id, "struct").fields(nested).build();
            supplied = Map.of(id, supplied);
            expected = Map.of(id, expected);
        }
        final DynamicSchema schema = imported(DynamicSchema.of(nested, DynamicField.field("tail", "u8").build()), json);
        if (depth > 64) {
            final String path = "$" + IntStream.range(0, depth).mapToObj(index -> ".level" + index).collect(Collectors.joining());
            assertCompileFailure(schema, path, "结构嵌套超过 64 层");
            return;
        }
        assertRoundTrip(DynamicCompiler.compile(schema), Map.of("level0", supplied.get("level0"), "tail", 127),
                Map.of("level0", expected.get("level0"), "tail", 127L), "347f");
    }

    static Stream<Arguments> invalidReferences() {
        return Stream.of(false, true).flatMap(json -> Stream.of("string", "float").flatMap(type ->
                Arrays.stream(ReferenceUse.values()).map(use -> Arguments.of(json, type, use))));
    }

    @ParameterizedTest
    @MethodSource("invalidReferences")
    void rejectsNonIntegerLengthChoiceAndBitViewReferences(boolean json, String type, ReferenceUse use) {
        final DynamicField source = type.equals("string")
                ? DynamicField.field("source", type).length(DynamicLength.fixed(1, "byte")).build()
                : DynamicField.field("source", type).bits(32).build();
        assertCompileFailure(imported(DynamicSchema.of(source, reference(use)), json),
                "$.target", "引用目标必须为整数: source");
    }

    static Stream<Arguments> validReferences() {
        return Stream.of(false, true).flatMap(json -> Arrays.stream(ReferenceUse.values()).map(use -> Arguments.of(json, use)));
    }

    @ParameterizedTest
    @MethodSource("validReferences")
    void acceptsIntegerTargetsAndExecutesTheirDeclaredUse(boolean json, ReferenceUse use) {
        final DynamicPlan plan = DynamicCompiler.compile(imported(DynamicSchema.of(
                DynamicField.field("source", "u8").build(), reference(use), DynamicField.field("tail", "u8").build()), json));
        final Object supplied = switch (use) {
            case LENGTH -> "aa";
            case CHOICE -> 0x34;
            case DERIVED -> 1;
        };
        final Object expected = use == ReferenceUse.LENGTH ? "aa" : use == ReferenceUse.CHOICE ? 0x34L : 1L;
        final String wire = switch (use) {
            case LENGTH -> "01aa7f";
            case CHOICE -> "01347f";
            case DERIVED -> "017f";
        };
        assertRoundTrip(plan, Map.of("source", 1, "target", supplied, "tail", 127),
                Map.of("source", 1L, "target", expected, "tail", 127L), wire);
    }

    static Stream<Arguments> invalidCounts() {
        return Stream.of(false, true).flatMap(json -> Stream.of("missing", "scalar", "self", "outer").map(target -> Arguments.of(json, target)));
    }

    @ParameterizedTest
    @MethodSource("invalidCounts")
    void countOfRequiresAnExistingSiblingCollection(boolean json, String target) {
        final DynamicSchema schema = switch (target) {
            case "missing" -> DynamicSchema.of(count("missing"), items());
            case "scalar" -> DynamicSchema.of(count("number"), DynamicField.field("number", "u8").build());
            case "self" -> DynamicSchema.of(count("count"), items());
            case "outer" -> DynamicSchema.of(items(), DynamicField.field("body", "struct").fields(count("items")).build());
            default -> throw new IllegalArgumentException(target);
        };
        assertCompileFailure(imported(schema, json), target.equals("outer") ? "$.body.count" : "$.count", "countOf 必须指向同级集合字段");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void acceptsSiblingCollectionAndGeneratesItsActualCount(boolean json) {
        final DynamicPlan plan = DynamicCompiler.compile(imported(DynamicSchema.of(
                count("items"), items(), DynamicField.field("tail", "u8").build()), json));
        assertRoundTrip(plan, Map.of("items", List.of(3, 4), "tail", 127),
                Map.of("count", 2L, "items", List.of(3L, 4L), "tail", 127L), "0203047f");
    }

    private static DynamicField reference(ReferenceUse use) {
        return switch (use) {
            case LENGTH -> DynamicField.field("target", "hex").length(DynamicLength.ref("source", "byte")).build();
            case CHOICE -> DynamicField.field("target", "choice").ref("source")
                    .variants(Map.of("1", DynamicField.field("selected", "u8").build())).build();
            case DERIVED -> DynamicField.field("target", "derived").ref("source")
                    .options(Map.of("bitOffset", 0, "bitWidth", 1)).build();
        };
    }

    private static DynamicField count(String target) {
        return DynamicField.field("count", "u8").generated(DynamicGeneratedValue.countOf(target)).build();
    }

    private static DynamicField items() {
        return DynamicField.field("items", "array").length(DynamicLength.fixed(2, "item"))
                .item(DynamicField.field("item", "u8").build()).build();
    }

    private static DynamicSchema imported(DynamicSchema schema, boolean json) {
        return json ? DynamicSchemaJson.read(DynamicSchemaJson.write(schema)) : schema;
    }

    private static void assertCompileFailure(DynamicSchema schema, String path, String message) {
        final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class, () -> DynamicCompiler.compile(schema));
        assertThat(error.fieldPath()).isEqualTo(path);
        assertThat(error.bitOffset()).isEqualTo(-1);
        assertThat(error).hasMessageContaining(message);
    }

    private static void assertRoundTrip(DynamicPlan plan, Map<String, Object> supplied, Map<String, Object> expected, String wire) {
        final DynamicLimits limits = new DynamicLimits(32, 32, 32, 128);
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final ByteBuf input = Unpooled.buffer().writeShort(0x55aa).writeBytes(ByteBufUtil.decodeHexDump(wire)).writeByte(0x66).readerIndex(2);
        try {
            assertThat(plan.encode(DynamicValues.of(supplied), output, limits)).isEqualTo(wire.length() * 4L);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + wire);
            assertThat(output.readerIndex()).isEqualTo(1);
            final DynamicDecodeResult decoded = plan.decode(input, limits);
            assertThat(decoded.values().fields()).isEqualTo(expected);
            assertThat(decoded.consumedBits()).isEqualTo(wire.length() * 4L);
            assertThat(decoded.consumedBytes()).isEqualTo(wire.length() / 2);
            assertThat(decoded.remainingBytes()).isEqualTo(1);
            assertThat(input.readerIndex()).isEqualTo(2 + wire.length() / 2);
            assertThat(input.getUnsignedByte(input.readerIndex())).isEqualTo((short) 0x66);
            assertThat(input.refCnt()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
        } finally {
            input.release();
            output.release();
        }
    }
}
