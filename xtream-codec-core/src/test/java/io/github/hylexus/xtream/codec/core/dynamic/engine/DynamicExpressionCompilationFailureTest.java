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

import io.github.hylexus.xtream.codec.base.expression.CustomXtreamExpressionEngine;
import io.github.hylexus.xtream.codec.base.expression.SpelXtreamExpressionEngine;
import io.github.hylexus.xtream.codec.base.expression.XtreamExpressionEngine;
import io.github.hylexus.xtream.codec.base.expression.XtreamExpressionEngineRegistry;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicCondition;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicExpression;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLengthUnit;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtensionRegistry;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.expression.ParseException;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 三种表达式用途的语法错误、引擎创建异常和后续编译恢复测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicExpressionCompilationFailureTest {
    enum Use {
        DERIVED, CONDITION, LENGTH
    }

    static Stream<Arguments> uses() {
        return Stream.of(Use.values()).flatMap(use -> Stream.of(false, true).map(nested -> Arguments.of(use, nested)));
    }

    @ParameterizedTest
    @MethodSource("uses")
    void wrapsInvalidSyntaxAtTheOwningFieldAndAllowsSubsequentValidCompilation(Use use, boolean nested) {
        final SpelXtreamExpressionEngine engine = new SpelXtreamExpressionEngine();
        final DynamicCompileOptions options = options(engine);
        final DynamicSchema schema = schema(use, nested, engine.id().value(), "1 +");
        final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class, () -> DynamicCompiler.compile(schema, options));
        assertCompilationFailure(error, use, nested, engine.id().value());
        assertThat(error).rootCause().isInstanceOf(ParseException.class).hasMessageContaining("1 +");
        final DynamicPlan valid = DynamicCompiler.compile(schema(use, nested, engine.id().value(), validSource(use)), options);
        assertValidPlan(valid, use, nested);
    }

    @ParameterizedTest
    @MethodSource("uses")
    void preservesTheEngineCreationCauseAndDoesNotEvaluateDuringCompilation(Use use, boolean nested) {
        final CustomXtreamExpressionEngine engine = mock(CustomXtreamExpressionEngine.class);
        final SpelXtreamExpressionEngine delegate = new SpelXtreamExpressionEngine();
        final IllegalArgumentException failure = new IllegalArgumentException("测试表达式创建失败");
        when(engine.id()).thenReturn(() -> "compile-test");
        when(engine.createExpression("broken")).thenThrow(failure);
        final String validSource = validSource(use);
        when(engine.createExpression(validSource)).thenReturn(delegate.createExpression(validSource));
        when(engine.createEvaluationContext(null)).thenAnswer(ignored -> delegate.createEvaluationContext(null));
        final DynamicCompileOptions options = options(engine);
        final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                () -> DynamicCompiler.compile(schema(use, nested, "compile-test", "broken"), options));
        assertCompilationFailure(error, use, nested, "compile-test");
        assertThat(error).rootCause().isSameAs(failure);
        verify(engine).createExpression("broken");
        verify(engine, never()).createEvaluationContext(any());

        final DynamicPlan valid = DynamicCompiler.compile(schema(use, nested, "compile-test", validSource), options);
        verify(engine).createExpression(validSource);
        verify(engine, never()).createEvaluationContext(any());
        assertValidPlan(valid, use, nested);
        verify(engine, times(1)).createExpression(validSource);
    }

    private static void assertCompilationFailure(DynamicCodecException error, Use use, boolean nested, String engineId) {
        assertThat(error.fieldPath()).isEqualTo(nested ? "$.body.target" : "$.target");
        assertThat(error.bitOffset()).isEqualTo(-1);
        assertThat(error).hasMessageContaining("表达式编译失败 (" + engineId + ")");
        final String purpose = switch (use) {
            case DERIVED -> "derived.expression";
            case CONDITION -> "when.expression";
            case LENGTH -> "length.expression";
        };
        assertThat(error).hasMessageContaining(purpose);
    }

    private static DynamicCompileOptions options(XtreamExpressionEngine engine) {
        final XtreamExpressionEngineRegistry engines = new XtreamExpressionEngineRegistry(false);
        engines.register(engine);
        return new DynamicCompileOptions(DynamicSchema.ALL_VERSION, DynamicExtensionRegistry.EMPTY, engines);
    }

    private static String validSource(Use use) {
        return use == Use.CONDITION ? "#n > 0" : "#n";
    }

    private static DynamicSchema schema(Use use, boolean nested, String engineId, String source) {
        final DynamicExpression expression = DynamicExpression.engine(engineId, source, Map.of("n", "count"));
        final DynamicField target = switch (use) {
            case DERIVED -> DynamicField.field("target", "derived").expression(expression).build();
            case CONDITION -> DynamicField.field("target", "u8").when(DynamicCondition.expression(expression)).build();
            case LENGTH -> DynamicField.field("target", "hex").length(DynamicLength.expression(expression, DynamicLengthUnit.BYTE)).build();
        };
        final DynamicField count = DynamicField.field("count", "u8").build();
        final DynamicField tail = DynamicField.field("tail", "u8").build();
        return nested ? DynamicSchema.of(DynamicField.field("body", "struct").fields(count, target, tail).build())
                : DynamicSchema.of(count, target, tail);
    }

    private static void assertValidPlan(DynamicPlan plan, Use use, boolean nested) {
        final Map<String, Object> input = Map.of("count", 1, "target", use == Use.LENGTH ? "aa" : 5, "tail", 127);
        final Map<String, Object> expected = Map.of("count", 1L,
                "target", use == Use.LENGTH ? "aa" : use == Use.CONDITION ? 5L : 1L, "tail", 127L);
        final String hex = switch (use) {
            case DERIVED -> "017f";
            case CONDITION -> "01057f";
            case LENGTH -> "01aa7f";
        };
        for (final boolean tracked : new boolean[]{false, true}) {
            final ByteBuf buffer = Unpooled.buffer().writeByte(0x55).readerIndex(1);
            final CodecTracker encoder = tracked ? new CodecTracker() : null;
            final CodecTracker decoder = tracked ? new CodecTracker() : null;
            try {
                assertThat(plan.encode(DynamicValues.of(nested ? Map.of("body", input) : input), buffer, encoder)).isEqualTo(hex.length() * 4L);
                assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo("55" + hex);
                assertThat(buffer.readerIndex()).isEqualTo(1);
                buffer.writeByte(0x66);
                final var result = plan.decode(buffer, decoder);
                assertThat(result.values().fields()).isEqualTo(nested ? Map.of("body", expected) : expected);
                assertThat(result.consumedBytes()).isEqualTo(hex.length() / 2);
                assertThat(result.remainingBytes()).isEqualTo(1);
                assertThat(buffer.readerIndex()).isEqualTo(1 + hex.length() / 2);
                assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
                assertThat(buffer.refCnt()).isEqualTo(1);
                if (encoder != null && decoder != null) {
                    assertThat(encoder.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                    assertThat(decoder.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                    assertThat(encoder.getTrace().getDiagnostics()).isEmpty();
                    assertThat(decoder.getTrace().getDiagnostics()).isEmpty();
                }
            } finally {
                buffer.release();
            }
        }
    }
}
