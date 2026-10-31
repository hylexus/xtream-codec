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

import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.XtreamCacheableClassPredicate;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.DefaultFieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.impl.SimpleBeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 实体长度及迭代次数表达式失败时的消费进度、对象状态和元数据复用测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class EntityExpressionFailureTest {
    private final SimpleBeanMetadataRegistry registry = new SimpleBeanMetadataRegistry(
            new DefaultFieldCodecRegistry(), new XtreamCacheableClassPredicate.Default());
    private final EntityCodec codec = new EntityCodec(registry);

    static Stream<Arguments> failures() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of("null", "throw").flatMap(failure -> Stream.of(
                Arguments.of(tracked, failure, new BytesBean(), new byte[]{0x12, 0x34}, 1),
                Arguments.of(tracked, failure, new NestedBean(), new Word(0x1234), 1),
                Arguments.of(tracked, failure, new LengthListBean(), List.of(new Item(0x12), new Item(0x34)), 1),
                Arguments.of(tracked, failure, new BoundedIterationBean(), List.of(new Item(0x12), new Item(0x34)), 3),
                Arguments.of(tracked, failure, new UnboundedIterationBean(), List.of(new Item(0x12), new Item(0x34)), 1))));
    }

    @ParameterizedTest
    @MethodSource("failures")
    void preservesProgressAndStopsBeforeAssigningBody(boolean tracked, String failure, ExpressionBean bean, Object expected, int consumed) {
        final var metadata = registry.getBeanMetadata(bean.getClass(), XtreamField.ALL_VERSION);
        final Object initialBody = bean.body();
        bean.failureMode = failure;
        final ByteBuf input = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump("55aa7e12347f66")).readerIndex(2);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final RuntimeException error = catchThrowableOfType(RuntimeException.class,
                    () -> codec.decode(XtreamField.ALL_VERSION, bean, input, tracker));
            if (failure.equals("null")) {
                assertThat(error).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Can not determine field length")
                        .hasMessageContaining("resolveLimit()");
            } else {
                Throwable original = error;
                while (original.getCause() != null) {
                    original = original.getCause();
                }
                assertThat(original).isSameAs(bean.failure);
            }
            assertThat(bean.evaluations).isEqualTo(1);
            assertThat(bean.head).isEqualTo(126);
            assertThat(bean.tail).isEqualTo(-1);
            assertThat(bean.body()).isSameAs(initialBody);
            assertThat(input.readerIndex()).isEqualTo(2 + consumed);
            assertThat(input.writerIndex()).isEqualTo(7);
            assertThat(ByteBufUtil.hexDump(input, 0, input.writerIndex())).isEqualTo("55aa7e12347f66");
            assertThat(input.refCnt()).isEqualTo(1);
            if (tracker != null) {
                final CodecTraceNode root = tracker.getTrace().getRoot();
                assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(root.getByteEnd()).isEqualTo(1);
                assertThat(root.getChildren()).extracting(CodecTraceNode::getName).containsExactly("head");
                assertThat(root.getChildren().getFirst().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
                    assertThat(diagnostic.level()).isEqualTo("ERROR");
                    assertThat(diagnostic.nodeId()).isEqualTo(root.getId());
                    assertThat(diagnostic.byteOffset()).isEqualTo(consumed);
                    assertThat(diagnostic.exceptionType()).isEqualTo(error.getClass().getName());
                });
                assertThat(tracker.isNotTracing()).isTrue();
            }

            bean.failureMode = "";
            bean.evaluations = 0;
            input.readerIndex(2);
            final CodecTracker decoder = tracked ? new CodecTracker() : null;
            assertThat(codec.decode(XtreamField.ALL_VERSION, bean, input, decoder)).isSameAs(bean);
            assertThat(bean.body()).isEqualTo(expected);
            assertThat(bean.head).isEqualTo(126);
            assertThat(bean.tail).isEqualTo(127);
            assertThat(bean.evaluations).isEqualTo(1);
            assertThat(input.readerIndex()).isEqualTo(6);
            assertThat(input.getUnsignedByte(6)).isEqualTo((short) 0x66);
            assertThat(registry.getBeanMetadata(bean.getClass(), XtreamField.ALL_VERSION)).isSameAs(metadata);
            assertSuccessfulTrace(decoder);

            final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
            final CodecTracker encoder = tracked ? new CodecTracker() : null;
            try {
                codec.encode(XtreamField.ALL_VERSION, bean, output, encoder);
                assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa7e12347f");
                assertThat(output.readerIndex()).isEqualTo(1);
                assertThat(output.refCnt()).isEqualTo(1);
                assertSuccessfulTrace(encoder);
            } finally {
                output.release();
            }
        } finally {
            input.release();
        }
    }

    private static void assertSuccessfulTrace(@Nullable CodecTracker tracker) {
        if (tracker != null) {
            assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
            assertThat(tracker.getTrace().getRoot().getByteEnd()).isEqualTo(4);
            assertThat(tracker.getTrace().getRoot().getChildren()).extracting(CodecTraceNode::getName)
                    .containsExactly("head", "body", "tail");
            assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
            assertThat(tracker.isNotTracing()).isTrue();
        }
    }

    public abstract static class ExpressionBean {
        @Preset.RustStyle.u8(order = 0)
        protected int head = -1;
        @Preset.RustStyle.u8(order = 2)
        protected int tail = -1;
        final IllegalStateException failure = new IllegalStateException("测试实体表达式失败");
        String failureMode = "";
        int evaluations;

        public @Nullable Integer resolveLimit() {
            evaluations++;
            return switch (failureMode) {
                case "null" -> null;
                case "throw" -> throw failure;
                default -> 2;
            };
        }

        abstract Object body();
    }

    public static class BytesBean extends ExpressionBean {
        @Preset.RustStyle.byte_array(order = 1, lengthExpression = "resolveLimit()")
        private byte[] body = {(byte) 0xaa};

        @Override
        Object body() {
            return body;
        }
    }

    public static class NestedBean extends ExpressionBean {
        @Preset.RustStyle.struct(order = 1, lengthExpression = "resolveLimit()")
        private Word body = new Word(0x7777);

        @Override
        Object body() {
            return body;
        }
    }

    public static class LengthListBean extends ExpressionBean {
        @Preset.RustStyle.list(order = 1, lengthExpression = "resolveLimit()")
        private List<Item> body = List.of(new Item(0x77));

        @Override
        Object body() {
            return body;
        }
    }

    public static class BoundedIterationBean extends ExpressionBean {
        @Preset.RustStyle.list(order = 1, length = 2, iterationTimesExpression = "resolveLimit()")
        private List<Item> body = List.of(new Item(0x77));

        @Override
        Object body() {
            return body;
        }
    }

    public static class UnboundedIterationBean extends ExpressionBean {
        @Preset.RustStyle.list(order = 1, iterationTimesExpression = "resolveLimit()")
        private List<Item> body = List.of(new Item(0x77));

        @Override
        Object body() {
            return body;
        }
    }

    public record Word(@Preset.RustStyle.u16 int value) {
    }

    public record Item(@Preset.RustStyle.u8 int value) {
    }
}
