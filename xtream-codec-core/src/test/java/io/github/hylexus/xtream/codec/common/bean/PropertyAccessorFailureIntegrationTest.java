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

package io.github.hylexus.xtream.codec.common.bean;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 真实实体属性访问异常的原始原因、部分进度、跟踪诊断和元数据复用测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class PropertyAccessorFailureIntegrationTest {
    private static final List<String> FIELDS = List.of("reflection", "methodHandle", "lambda");
    private static final String WIRE = "7e1122334455667f";
    private final SimpleBeanMetadataRegistry registry = new SimpleBeanMetadataRegistry(
            new DefaultFieldCodecRegistry(), new XtreamCacheableClassPredicate.Default());
    private final EntityCodec codec = new EntityCodec(registry);

    static Stream<Arguments> getters() {
        return Stream.of(false, true).flatMap(tracked -> FIELDS.stream().map(field -> Arguments.of(tracked, field)));
    }

    static Stream<Arguments> setters() {
        return Stream.of(false, true).flatMap(tracked -> FIELDS.stream()
                .flatMap(field -> Stream.of(false, true).map(chain -> Arguments.of(tracked, field, chain))));
    }

    @ParameterizedTest
    @MethodSource("getters")
    void getterFailurePreservesOutputAndStopsLaterAccessors(boolean tracked, String field) {
        final OrdinaryBean bean = new OrdinaryBean();
        final BeanMetadata metadata = registry.getBeanMetadata(bean.getClass(), XtreamField.ALL_VERSION);
        bean.failingGetter = field;
        final int index = FIELDS.indexOf(field);
        final int written = 1 + index * 2;
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final RuntimeException error = catchThrowableOfType(RuntimeException.class,
                    () -> codec.encode(XtreamField.ALL_VERSION, bean, output, tracker));
            assertOriginalFailure(error, bean.failure);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + WIRE.substring(0, written * 2));
            assertThat(output.writerIndex()).isEqualTo(2 + written);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
            assertState(bean, 0x7e, 0x1122, 0x3344, 0x5566, 0x7f);
            assertThat(bean.calls).containsExactlyElementsOf(FIELDS.subList(0, index + 1).stream().map(name -> "get:" + name).toList());
            if (tracker != null) {
                assertFailureTrace(tracker, error, written, index);
            }
            assertRecovery(bean, tracked, metadata);
        } finally {
            output.release();
        }
    }

    @ParameterizedTest
    @MethodSource("setters")
    void setterFailurePreservesConsumedInputAndPartialObject(boolean tracked, String field, boolean chain) {
        final AccessorBean bean = chain ? new ChainBean() : new OrdinaryBean();
        final BeanMetadata metadata = registry.getBeanMetadata(bean.getClass(), XtreamField.ALL_VERSION);
        bean.head = -1;
        bean.reflection = -1;
        bean.methodHandle = -1;
        bean.lambda = -1;
        bean.tail = -1;
        bean.failingSetter = field;
        final int index = FIELDS.indexOf(field);
        final int consumed = 3 + index * 2;
        final ByteBuf input = Unpooled.buffer().writeShort(0x55aa).writeBytes(ByteBufUtil.decodeHexDump(WIRE))
                .writeByte(0x66).readerIndex(2);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final RuntimeException error = catchThrowableOfType(RuntimeException.class,
                    () -> codec.decode(XtreamField.ALL_VERSION, bean, input, tracker));
            assertOriginalFailure(error, bean.failure);
            assertThat(input.readerIndex()).isEqualTo(2 + consumed);
            assertThat(input.writerIndex()).isEqualTo(11);
            assertThat(ByteBufUtil.hexDump(input, 0, input.writerIndex())).isEqualTo("55aa" + WIRE + "66");
            assertThat(input.refCnt()).isEqualTo(1);
            assertState(bean, 0x7e, index > 0 ? 0x1122 : -1, index > 1 ? 0x3344 : -1, -1, -1);
            assertThat(bean.calls).containsExactlyElementsOf(FIELDS.subList(0, index + 1).stream().map(name -> "set:" + name).toList());
            if (tracker != null) {
                // 字节解码已经成功，setter 的异常记录在根节点，失败字段不会回滚。
                assertFailureTrace(tracker, error, consumed, index + 1);
            }
            assertRecovery(bean, tracked, metadata);
        } finally {
            input.release();
        }
    }

    private static void assertOriginalFailure(Throwable error, Throwable expected) {
        Throwable original = error;
        while (original.getCause() != null) {
            original = original.getCause();
        }
        assertThat(original).isSameAs(expected);
    }

    private static void assertFailureTrace(CodecTracker tracker, RuntimeException error, int offset, int completedProperties) {
        final CodecTraceNode root = tracker.getTrace().getRoot();
        assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
        assertThat(root.getByteStart()).isZero();
        assertThat(root.getByteEnd()).isEqualTo(offset);
        final List<String> names = new ArrayList<>(List.of("head"));
        names.addAll(FIELDS.subList(0, completedProperties));
        assertThat(root.getChildren()).extracting(CodecTraceNode::getName).containsExactlyElementsOf(names);
        assertThat(root.getChildren()).allSatisfy(node -> assertThat(node.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS));
        assertThat(root.getChildren().getLast().getByteEnd()).isEqualTo(offset);
        assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
            assertThat(diagnostic.level()).isEqualTo("ERROR");
            assertThat(diagnostic.nodeId()).isEqualTo(root.getId());
            assertThat(diagnostic.byteOffset()).isEqualTo(offset);
            assertThat(diagnostic.exceptionType()).isEqualTo(error.getClass().getName());
            assertThat(diagnostic.message()).contains("测试属性访问失败");
        });
        assertThat(tracker.isNotTracing()).isTrue();
    }

    private void assertRecovery(AccessorBean bean, boolean tracked, BeanMetadata metadata) {
        bean.failingGetter = "";
        bean.failingSetter = "";
        bean.calls.clear();
        final ByteBuf input = Unpooled.buffer().writeShort(0x55aa).writeBytes(ByteBufUtil.decodeHexDump(WIRE))
                .writeByte(0x66).readerIndex(2);
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        try {
            assertThat(codec.decode(XtreamField.ALL_VERSION, bean, input, decoder)).isSameAs(bean);
            assertState(bean, 0x7e, 0x1122, 0x3344, 0x5566, 0x7f);
            assertThat(input.readerIndex()).isEqualTo(10);
            assertThat(input.getUnsignedByte(10)).isEqualTo((short) 0x66);
            codec.encode(XtreamField.ALL_VERSION, bean, output, encoder);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + WIRE);
            assertThat(output.writerIndex()).isEqualTo(10);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(bean.calls).containsExactly("set:reflection", "set:methodHandle", "set:lambda",
                    "get:reflection", "get:methodHandle", "get:lambda");
            assertThat(registry.getBeanMetadata(bean.getClass(), XtreamField.ALL_VERSION)).isSameAs(metadata);
            assertThat(input.refCnt()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
            if (encoder != null && decoder != null) {
                for (final CodecTracker tracker : List.of(encoder, decoder)) {
                    assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                    assertThat(tracker.getTrace().getRoot().getByteEnd()).isEqualTo(8);
                    assertThat(tracker.getTrace().getRoot().getChildren()).extracting(CodecTraceNode::getName)
                            .containsExactly("head", "reflection", "methodHandle", "lambda", "tail");
                    assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
                    assertThat(tracker.isNotTracing()).isTrue();
                }
            }
        } finally {
            input.release();
            output.release();
        }
    }

    private static void assertState(AccessorBean bean, int head, int reflection, int methodHandle, int lambda, int tail) {
        assertThat(bean.head).isEqualTo(head);
        assertThat(bean.reflection).isEqualTo(reflection);
        assertThat(bean.methodHandle).isEqualTo(methodHandle);
        assertThat(bean.lambda).isEqualTo(lambda);
        assertThat(bean.tail).isEqualTo(tail);
    }

    public static class AccessorBean {
        @Preset.RustStyle.u8(order = 0)
        protected int head = 0x7e;
        @Preset.RustStyle.u16(order = 1, propertyAccessStrategy = PropertyAccessStrategy.REFLECTION)
        protected int reflection = 0x1122;
        @Preset.RustStyle.u16(order = 2, propertyAccessStrategy = PropertyAccessStrategy.METHOD_HANDLE)
        protected int methodHandle = 0x3344;
        @Preset.RustStyle.u16(order = 3, propertyAccessStrategy = PropertyAccessStrategy.LAMBDA_META_FACTORY)
        protected int lambda = 0x5566;
        @Preset.RustStyle.u8(order = 4)
        protected int tail = 0x7f;
        final List<String> calls = new ArrayList<>();
        final IllegalStateException failure = new IllegalStateException("测试属性访问失败");
        String failingGetter = "";
        String failingSetter = "";

        public int getReflection() {
            return read("reflection", reflection);
        }

        public int getMethodHandle() {
            return read("methodHandle", methodHandle);
        }

        public int getLambda() {
            return read("lambda", lambda);
        }

        private int read(String field, int value) {
            calls.add("get:" + field);
            if (failingGetter.equals(field)) {
                throw failure;
            }
            return value;
        }

        protected void beforeSet(String field) {
            calls.add("set:" + field);
            if (failingSetter.equals(field)) {
                throw failure;
            }
        }
    }

    public static class OrdinaryBean extends AccessorBean {
        public void setReflection(int value) {
            beforeSet("reflection");
            reflection = value;
        }

        public void setMethodHandle(int value) {
            beforeSet("methodHandle");
            methodHandle = value;
        }

        public void setLambda(int value) {
            beforeSet("lambda");
            lambda = value;
        }
    }

    public static class ChainBean extends AccessorBean {
        public ChainBean setReflection(int value) {
            beforeSet("reflection");
            reflection = value;
            return this;
        }

        public ChainBean setMethodHandle(int value) {
            beforeSet("methodHandle");
            methodHandle = value;
            return this;
        }

        public ChainBean setLambda(int value) {
            beforeSet("lambda");
            lambda = value;
            return this;
        }
    }
}
