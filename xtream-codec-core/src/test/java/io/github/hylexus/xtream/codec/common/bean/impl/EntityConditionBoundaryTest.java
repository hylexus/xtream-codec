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

import io.github.hylexus.xtream.codec.common.bean.BeanMetadata;
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.XtreamCacheableClassPredicate;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
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
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 实体条件表达式的空值、异常、部分进度和元数据复用测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class EntityConditionBoundaryTest {
    private static final String WIRE = "7e000241427f";
    private final SimpleBeanMetadataRegistry registry = new SimpleBeanMetadataRegistry(
            new DefaultFieldCodecRegistry(), new XtreamCacheableClassPredicate.Default());
    private final EntityCodec codec = new EntityCodec(registry);

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void nullConditionSkipsPrefixAndPreservesPojoValue(boolean tracked) {
        final ConditionalBean bean = new ConditionalBean();
        final BeanMetadata metadata = registry.getBeanMetadata(ConditionalBean.class, XtreamField.ALL_VERSION);
        final ByteBuf input = input("7e7f");
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            assertThat(codec.decode(XtreamField.ALL_VERSION, bean, input, decoder)).isSameAs(bean);
            assertThat(bean.head).isEqualTo(126);
            assertThat(bean.body).isEqualTo("initial");
            assertThat(bean.tail).isEqualTo(127);
            assertThat(bean.evaluations).isEqualTo(1);
            assertThat(input.readerIndex()).isEqualTo(4);
            assertThat(input.getUnsignedByte(4)).isEqualTo((short) 0x66);
            assertThat(input.refCnt()).isEqualTo(1);
            assertSuccess(decoder, 2, "head", "tail");
            assertEncoded(bean, "7e7f", tracked, "head", "tail");
            assertThat(bean.evaluations).isEqualTo(2);
            assertThat(bean.body).isEqualTo("initial");
            assertRecovery(bean, tracked, metadata);
        } finally {
            input.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void nullConditionUsesRecordDefaultsWithoutConsumingPrefixes(boolean tracked) {
        final BeanMetadata metadata = registry.getBeanMetadata(ConditionalRecord.class, XtreamField.ALL_VERSION);
        assertEncoded(new ConditionalRecord(1, 0x1234, "AB", "Z", 127), "017f", tracked, "head", "tail");
        final ByteBuf skipped = input("017f");
        final ByteBuf present = input("02123400024142015a7f");
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        final CodecTracker recovered = tracked ? new CodecTracker() : null;
        try {
            assertThat(codec.decode(XtreamField.ALL_VERSION, ConditionalRecord.class, skipped, decoder))
                    .isEqualTo(new ConditionalRecord(1, 0, null, "", 127));
            assertThat(skipped.readerIndex()).isEqualTo(4);
            assertThat(skipped.getUnsignedByte(4)).isEqualTo((short) 0x66);
            assertSuccess(decoder, 2, "head", "tail");

            final ConditionalRecord record = codec.decode(XtreamField.ALL_VERSION, ConditionalRecord.class, present, recovered);
            assertThat(record).isEqualTo(new ConditionalRecord(2, 0x1234, "AB", "Z", 127));
            assertThat(present.readerIndex()).isEqualTo(12);
            assertThat(present.getUnsignedByte(12)).isEqualTo((short) 0x66);
            assertSuccess(recovered, 10, "head", "number", "prependLengthField", "body", "prependLengthField", "empty", "tail");
            assertEncoded(record, "02123400024142015a7f", tracked,
                    "head", "number", "prependLengthField", "body", "prependLengthField", "empty", "tail");
            assertThat(registry.getBeanMetadata(ConditionalRecord.class, XtreamField.ALL_VERSION)).isSameAs(metadata);
            assertThat(skipped.refCnt()).isEqualTo(1);
            assertThat(present.refCnt()).isEqualTo(1);
        } finally {
            skipped.release();
            present.release();
        }
    }

    static Stream<Arguments> failures() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(false, true).map(encoding -> Arguments.of(tracked, encoding)));
    }

    @ParameterizedTest
    @MethodSource("failures")
    void conditionFailureStopsBeforeBodyAndPreservesEarlierProgress(boolean tracked, boolean encoding) {
        final ConditionalBean bean = new ConditionalBean();
        final BeanMetadata metadata = registry.getBeanMetadata(ConditionalBean.class, XtreamField.ALL_VERSION);
        bean.failCondition = true;
        bean.head = encoding ? 126 : -1;
        final ByteBuf buffer = encoding ? Unpooled.buffer().writeShort(0x55aa).readerIndex(1) : input(WIRE);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final RuntimeException error = catchThrowableOfType(RuntimeException.class, () -> {
                if (encoding) {
                    codec.encode(XtreamField.ALL_VERSION, bean, buffer, tracker);
                } else {
                    codec.decode(XtreamField.ALL_VERSION, bean, buffer, tracker);
                }
            });
            Throwable original = error;
            while (original.getCause() != null) {
                original = original.getCause();
            }
            assertThat(original).isSameAs(bean.failure);
            assertThat(bean.head).isEqualTo(126);
            assertThat(bean.body).isEqualTo("initial");
            assertThat(bean.tail).isEqualTo(-1);
            assertThat(bean.tailReads).isZero();
            assertThat(bean.evaluations).isEqualTo(1);
            assertThat(buffer.readerIndex()).isEqualTo(encoding ? 1 : 3);
            assertThat(buffer.writerIndex()).isEqualTo(encoding ? 3 : 9);
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo(encoding ? "55aa7e" : "55aa" + WIRE + "66");
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (tracker != null) {
                final CodecTraceNode root = tracker.getTrace().getRoot();
                assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(root.getByteEnd()).isEqualTo(1);
                assertThat(root.getChildren()).extracting(CodecTraceNode::getName).containsExactly("head");
                assertThat(root.getChildren().getFirst().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
                    assertThat(diagnostic.nodeId()).isEqualTo(root.getId());
                    assertThat(diagnostic.byteOffset()).isEqualTo(1);
                    assertThat(diagnostic.exceptionType()).isEqualTo(error.getClass().getName());
                    assertThat(diagnostic.message()).contains("测试条件表达式失败");
                });
                assertThat(tracker.isNotTracing()).isTrue();
            }
            assertRecovery(bean, tracked, metadata);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void nullEncodingValueSkipsConditionEvaluation(boolean tracked) {
        final ConditionalBean bean = new ConditionalBean();
        bean.head = 126;
        bean.body = null;
        bean.tail = 127;
        bean.failCondition = true;
        assertEncoded(bean, "7e7f", tracked, "head", "tail");
        assertThat(bean.evaluations).isZero();
        assertThat(bean.body).isNull();
    }

    private void assertRecovery(ConditionalBean bean, boolean tracked, BeanMetadata metadata) {
        bean.failCondition = false;
        bean.condition = true;
        bean.evaluations = 0;
        bean.tailReads = 0;
        final ByteBuf input = input(WIRE);
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            assertThat(codec.decode(XtreamField.ALL_VERSION, bean, input, decoder)).isSameAs(bean);
            assertThat(bean.body).isEqualTo("AB");
            assertThat(bean.head).isEqualTo(126);
            assertThat(bean.tail).isEqualTo(127);
            assertThat(input.readerIndex()).isEqualTo(8);
            assertThat(input.getUnsignedByte(8)).isEqualTo((short) 0x66);
            assertSuccess(decoder, 6, "head", "prependLengthField", "body", "tail");
            assertEncoded(bean, WIRE, tracked, "head", "prependLengthField", "body", "tail");
            assertThat(bean.evaluations).isEqualTo(2);
            assertThat(bean.tailReads).isEqualTo(1);
            assertThat(registry.getBeanMetadata(ConditionalBean.class, XtreamField.ALL_VERSION)).isSameAs(metadata);
        } finally {
            input.release();
        }
    }

    private void assertEncoded(Object bean, String hex, boolean tracked, String... fields) {
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            codec.encode(XtreamField.ALL_VERSION, bean, output, tracker);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + hex);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
            assertSuccess(tracker, hex.length() / 2, fields);
        } finally {
            output.release();
        }
    }

    private static ByteBuf input(String hex) {
        return Unpooled.buffer().writeShort(0x55aa).writeBytes(ByteBufUtil.decodeHexDump(hex)).writeByte(0x66).readerIndex(2);
    }

    private static void assertSuccess(@Nullable CodecTracker tracker, int length, String... fields) {
        if (tracker != null) {
            assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
            assertThat(tracker.getTrace().getRoot().getByteEnd()).isEqualTo(length);
            assertThat(tracker.getTrace().getRoot().getChildren()).extracting(CodecTraceNode::getName).containsExactly(fields);
            assertThat(tracker.getTrace().getRoot().getChildren()).allSatisfy(node -> assertThat(node.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS));
            assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
            assertThat(tracker.isNotTracing()).isTrue();
        }
    }

    public static class ConditionalBean {
        @Preset.RustStyle.u8(order = 0)
        public int head = -1;
        @Preset.RustStyle.str(order = 1, charset = "UTF-8", condition = "resolveCondition()", prependLengthFieldType = PrependLengthFieldType.u16)
        public @Nullable String body = "initial";
        @Preset.RustStyle.u8(order = 2)
        private int tail = -1;
        final IllegalStateException failure = new IllegalStateException("测试条件表达式失败");
        @Nullable Boolean condition;
        boolean failCondition;
        int evaluations;
        int tailReads;

        public @Nullable Boolean resolveCondition() {
            evaluations++;
            if (failCondition) {
                throw failure;
            }
            return condition;
        }

        public int getTail() {
            tailReads++;
            return tail;
        }
    }

    public record ConditionalRecord(
            @Preset.RustStyle.u8 int head,
            @Preset.RustStyle.u16(condition = "#head == 1 ? null : true") int number,
            @Preset.RustStyle.str(condition = "#head == 1 ? null : true", prependLengthFieldType = PrependLengthFieldType.u16) @Nullable String body,
            @EmptyTextWhenSkipped String empty,
            @Preset.RustStyle.u8 int tail) {
    }

    @Target(ElementType.RECORD_COMPONENT)
    @Retention(RetentionPolicy.RUNTIME)
    @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, charset = "UTF-8",
            condition = "#head == 1 ? null : true", nulls = XtreamField.Nulls.AS_EMPTY,
            prependLengthFieldType = PrependLengthFieldType.u8)
    public @interface EmptyTextWhenSkipped {
    }
}
