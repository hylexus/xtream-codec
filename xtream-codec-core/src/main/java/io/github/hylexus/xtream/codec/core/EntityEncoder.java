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

import io.github.hylexus.xtream.codec.common.bean.BeanMetadata;
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.bean.EncodedLengthPlan;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.DefaultSerializeContext;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.simple.DataField;
import io.github.hylexus.xtream.codec.core.utils.XtreamFieldUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * @author hylexus
 * @author opencode (AI)
 * @author Codex (AI)
 */
public class EntityEncoder {
    protected final ByteBufAllocator bufferFactory = ByteBufAllocator.DEFAULT;
    private final BeanMetadataRegistry beanMetadataRegistry;
    private final FieldCodecRegistry fieldCodecRegistry;
    protected final DataFieldEncoder dataFieldEncoder;
    protected final XtreamExpressionFactory expressionFactory;

    public EntityEncoder(BeanMetadataRegistry beanMetadataRegistry) {
        this.beanMetadataRegistry = beanMetadataRegistry;
        this.fieldCodecRegistry = beanMetadataRegistry.getFieldCodecRegistry();
        this.dataFieldEncoder = new DataFieldEncoder();
        this.expressionFactory = this.beanMetadataRegistry.expressionFactory();
    }

    public void encode(Object instance, ByteBuf target) {
        this.encode(XtreamField.ALL_VERSION, instance, target);
    }

    // todo 优化
    public void encode(int version, @Nullable Object instance, ByteBuf target) {
        switch (instance) {
            case null -> {
                // ignored
            }
            case DataField dataField -> {
                final FieldCodec.SerializeContext context = new DefaultSerializeContext(this.bufferFactory, this, instance, version, this.beanMetadataRegistry, null);
                this.dataFieldEncoder.encode(context, dataField, target);
            }
            case Iterable<?> iterable -> {
                final FieldCodec.SerializeContext context = new DefaultSerializeContext(this.bufferFactory, this, instance, version, this.beanMetadataRegistry, null);
                for (Object object : iterable) {
                    if (object instanceof DataField dataField) {
                        this.dataFieldEncoder.encode(context, dataField, target);
                    } else {
                        final BeanMetadata beanMetadata = beanMetadataRegistry.getBeanMetadata(object.getClass(), version);
                        this.encode(version, beanMetadata, object, target);
                    }
                }
            }
            default -> {
                final BeanMetadata beanMetadata = beanMetadataRegistry.getBeanMetadata(instance.getClass(), version);
                this.encode(version, beanMetadata, instance, target);
            }
        }
    }

    public void encode(BeanMetadata beanMetadata, Object instance, ByteBuf target) {
        this.encode(XtreamField.ALL_VERSION, beanMetadata, instance, target);
    }

    public void encode(int version, BeanMetadata beanMetadata, @Nullable Object instance, ByteBuf target) {
        if (instance == null) {
            return;
        }
        final FieldCodec.SerializeContext context = new DefaultSerializeContext(this.bufferFactory, this, instance, version, this.beanMetadataRegistry, null);
        final List<BeanPropertyMetadata> props = beanMetadata.getPropertyMetadataList();
        if (beanMetadata.hasEncodedLengthField()) {
            encodeFieldsWithEncodedLength(props, beanMetadata, instance, target, context, NORMAL, Objects.requireNonNull(beanMetadata.getEncodedLengthPlan()));
        } else {
            encodeFieldsNorm(props, beanMetadata, instance, target, context, NORMAL);
        }
    }

    // region withTracker
    @SuppressWarnings("unused")
    public void encodeWithTracker(Object instance, ByteBuf target, CodecTracker tracker) {
        this.encodeWithTracker(XtreamField.ALL_VERSION, instance, target, tracker);
    }

    public void encodeWithTracker(int version, @Nullable Object instance, ByteBuf target, CodecTracker tracker) {
        switch (instance) {
            case null -> {
                // ignored
            }
            case DataField dataField -> {
                final FieldCodec.SerializeContext context = new DefaultSerializeContext(this.bufferFactory, this, instance, version, this.beanMetadataRegistry, tracker);
                this.dataFieldEncoder.encodeWithTracker(context, dataField, target);
            }
            case Iterable<?> iterable -> {
                final FieldCodec.SerializeContext context = new DefaultSerializeContext(this.bufferFactory, this, instance, version, this.beanMetadataRegistry, tracker);
                final int indexBeforeWrite = target.writerIndex();
                final boolean rootInvocation = tracker.isNotTracing();
                if (rootInvocation) {
                    tracker.beginEncode(indexBeforeWrite, instance.getClass().getName());
                }
                try {
                    for (Object object : iterable) {
                        if (object instanceof DataField dataField) {
                            this.dataFieldEncoder.encodeWithTracker(context, dataField, target);
                        } else {
                            final BeanMetadata beanMetadata = beanMetadataRegistry.getBeanMetadata(object.getClass(), version);
                            this.encodeWithTracker(version, beanMetadata, object, target, tracker);
                        }
                    }
                    if (rootInvocation) {
                        final String hexString = FormatUtils.toHexString(target, indexBeforeWrite, target.writerIndex() - indexBeforeWrite);
                        tracker.finishTrace(hexString, target.writerIndex());
                    }
                } catch (RuntimeException | Error e) {
                    if (rootInvocation) {
                        tracker.recordFailure(e, target.writerIndex());
                    }
                    throw e;
                }
            }
            default -> {
                final BeanMetadata beanMetadata = beanMetadataRegistry.getBeanMetadata(instance.getClass(), version);
                this.encodeWithTracker(version, beanMetadata, instance, target, tracker);
            }
        }
    }

    public void encodeWithTracker(BeanMetadata beanMetadata, @Nullable Object instance, ByteBuf target, CodecTracker tracker) {
        this.encodeWithTracker(XtreamField.ALL_VERSION, beanMetadata, instance, target, tracker);
    }

    public void encodeWithTracker(int version, BeanMetadata beanMetadata, @Nullable Object instance, ByteBuf target, CodecTracker tracker) {
        if (instance == null) {
            return;
        }
        final FieldCodec.SerializeContext context = new DefaultSerializeContext(this.bufferFactory, this, instance, version, this.beanMetadataRegistry, tracker);
        final int indexBeforeWrite = target.writerIndex();
        final boolean rootInvocation = tracker.isNotTracing();
        if (rootInvocation) {
            tracker.beginEncode(indexBeforeWrite, beanMetadata.getRawType().getName());
        }
        try {
            final List<BeanPropertyMetadata> props = beanMetadata.getPropertyMetadataList();
            if (beanMetadata.hasEncodedLengthField()) {
                encodeFieldsWithEncodedLength(props, beanMetadata, instance, target, context, TRACKED, new TrackedEncodedLengthRuntime(Objects.requireNonNull(beanMetadata.getEncodedLengthPlan()), tracker));
            } else {
                encodeFieldsNorm(props, beanMetadata, instance, target, context, TRACKED);
            }
            if (rootInvocation) {
                final String hexString = FormatUtils.toHexString(target, indexBeforeWrite, target.writerIndex() - indexBeforeWrite);
                tracker.finishTrace(hexString, target.writerIndex());
            }
        } catch (RuntimeException | Error e) {
            if (rootInvocation) {
                tracker.recordFailure(e, target.writerIndex());
            }
            throw e;
        }
    }
    // endregion withTracker

    @SuppressWarnings("redundent")
    public BeanMetadataRegistry getBeanMetadataRegistry() {
        return beanMetadataRegistry;
    }

    @SuppressWarnings("redundent")
    public FieldCodecRegistry getFieldCodecRegistry() {
        return fieldCodecRegistry;
    }

    public XtreamExpressionFactory expressionFactory() {
        return this.expressionFactory;
    }

    // region field encoding

    @FunctionalInterface
    private interface FieldEncoder {
        void encode(BeanPropertyMetadata pm, FieldCodec.SerializeContext ctx, ByteBuf buf, Object value);
    }

    private static final FieldEncoder NORMAL = BeanPropertyMetadata::encodePropertyValue;
    private static final FieldEncoder TRACKED = BeanPropertyMetadata::encodePropertyValueWithTracker;

    private static void encodeFieldsNorm(List<BeanPropertyMetadata> props, BeanMetadata beanMetadata, Object instance, ByteBuf target, FieldCodec.SerializeContext context, FieldEncoder encoder) {
        for (final BeanPropertyMetadata pm : props) {
            if (pm.isDerived()) {
                continue;
            }
            if (pm.xtreamFieldAnnotation().codecStrategy() == XtreamField.CodecStrategy.TRANSIENT) {
                continue;
            }
            encodeField(pm, instance, beanMetadata, target, context, encoder);
        }
    }

    private static void encodeFieldsWithEncodedLength(List<BeanPropertyMetadata> props, BeanMetadata beanMetadata, Object instance, ByteBuf target, FieldCodec.SerializeContext context, FieldEncoder encoder, EncodedLengthPlan plan) {
        encodeFieldsWithEncodedLength(props, beanMetadata, instance, target, context, encoder, new NormalEncodedLengthRuntime(plan));
    }

    private static void encodeFieldsWithEncodedLength(List<BeanPropertyMetadata> props, BeanMetadata beanMetadata, Object instance, ByteBuf target, FieldCodec.SerializeContext context, FieldEncoder encoder, EncodedLengthRuntime runtime) {
        for (int i = 0; i < props.size(); i++) {
            final BeanPropertyMetadata pm = props.get(i);
            runtime.beforeField(i, target);
            if (pm.isDerived()) {
                continue;
            }
            if (pm.xtreamFieldAnnotation().codecStrategy() == XtreamField.CodecStrategy.TRANSIENT) {
                continue;
            }

            if (pm.isEncodedLength()) {
                if (!pm.conditionEvaluator().evaluate(context)) {
                    continue;
                }
                runtime.writePlaceholder(pm, target, context, encoder);
                continue;
            }

            encodeField(pm, instance, beanMetadata, target, context, encoder);
        }

        runtime.finish(target);
    }

    private static void encodeField(BeanPropertyMetadata pm, Object instance, BeanMetadata beanMetadata, ByteBuf target, FieldCodec.SerializeContext context, FieldEncoder encoder) {
        final Object value = resolveEncodingValue(pm, instance, beanMetadata);
        context.evaluationContext().setVariable(pm.name(), value);
        if (value == null) {
            return;
        }
        if (pm.conditionEvaluator().evaluate(context)) {
            encoder.encode(pm, context, target, value);
        }
    }

    // endregion field encoding

    private static @Nullable Object resolveEncodingValue(BeanPropertyMetadata sourceProperty, Object instance, BeanMetadata beanMetadata) {
        return XtreamFieldUtils.resolveEncodingValue(sourceProperty, instance, beanMetadata);
    }

    /**
     * {@link EncodedLengthPlan} 的单次编码运行时状态。
     * <p>
     * 编码循环在每个字段之前调用 {@link #beforeField(int, ByteBuf)}，在长度字段位置写入占位值，
     * 在范围结束位置或实体编码结束时完成回填。范围长度通过 {@code writerIndex} 的差值计算，
     * 因此会自然排除被条件表达式或 {@code null} 值跳过的字段。
     */
    private abstract static class EncodedLengthRuntime {
        final EncodedLengthPlan plan;
        // 长度字段占位值在 ByteBuf 中的起始位置
        int placeholderStart = -1;
        // 被统计范围的起始位置
        private int rangeStart = -1;
        // 长度字段是否已经实际写入占位值
        private boolean placeholderWritten;
        // 范围长度是否已经完成回填
        private boolean closed;

        private EncodedLengthRuntime(EncodedLengthPlan plan) {
            this.plan = plan;
        }

        void beforePlaceholderEncode(FieldCodec.SerializeContext context) {
            // 普通编码不需要记录长度字段的追踪节点。
        }

        void afterPlaceholderEncode(FieldCodec.SerializeContext context) {
            // 普通编码不需要记录长度字段的追踪节点。
        }

        void afterBackfill(ByteBuf target, int encodedLength) {
            // 普通编码不需要更新长度字段的追踪节点。
        }

        /**
         * 在当前字段编码前更新范围状态。
         * <p>
         * {@code until} 是左闭右开范围的结束字段，因此需要在该字段编码前回填。
         */
        void beforeField(int fieldIndex, ByteBuf target) {
            if (!this.placeholderWritten || this.closed) {
                return;
            }
            if (this.rangeStart < 0 && fieldIndex == this.plan.fromFieldIndex()) {
                this.rangeStart = target.writerIndex();
            }
            if (fieldIndex == this.plan.untilFieldIndex() && this.rangeStart >= 0) {
                this.backfill(target, target.writerIndex());
            }
        }

        /**
         * 写入长度字段的占位值，并记录后续回填所需的位置。
         */
        void writePlaceholder(BeanPropertyMetadata pm, ByteBuf target, FieldCodec.SerializeContext context, FieldEncoder encoder) {
            this.placeholderStart = target.writerIndex();
            this.beforePlaceholderEncode(context);
            encoder.encode(pm, context, target, 0);
            this.afterPlaceholderEncode(context);
            context.evaluationContext().setVariable(pm.name(), 0);
            this.placeholderWritten = true;
            if (this.plan.fromFieldIndex() < 0) {
                this.rangeStart = target.writerIndex();
            }
        }

        /**
         * 在没有显式 {@code until} 字段时，使用当前缓冲区尾部结束统计范围。
         */
        void finish(ByteBuf target) {
            if (this.placeholderWritten && !this.closed && this.rangeStart >= 0) {
                this.backfill(target, target.writerIndex());
            }
        }

        /**
         * 根据范围起止位置计算实际编码长度，并原地覆盖长度字段占位值。
         */
        private void backfill(ByteBuf target, int rangeEnd) {
            final int encodedLength = rangeEnd - this.rangeStart;
            this.plan.writer().write(target, this.placeholderStart, encodedLength);
            this.afterBackfill(target, encodedLength);
            this.closed = true;
        }

        static int lengthFieldByteCount(EncodedLengthPlan.Writer writer) {
            return switch (writer.maxValue()) {
                case 0xFF -> 1;
                case 0xFFFF -> 2;
                default -> 4;
            };
        }
    }

    private static final class NormalEncodedLengthRuntime extends EncodedLengthRuntime {
        private NormalEncodedLengthRuntime(EncodedLengthPlan plan) {
            super(plan);
        }
    }

    private static final class TrackedEncodedLengthRuntime extends EncodedLengthRuntime {
        private final CodecTracker tracker;
        private CodecTracker.@Nullable TraceCheckpoint checkpoint;
        private CodecTracker.@Nullable DeferredNode placeholderNode;

        private TrackedEncodedLengthRuntime(EncodedLengthPlan plan, CodecTracker tracker) {
            super(plan);
            this.tracker = tracker;
        }

        @Override
        void beforePlaceholderEncode(FieldCodec.SerializeContext context) {
            this.checkpoint = this.tracker.checkpoint();
        }

        @Override
        void afterPlaceholderEncode(FieldCodec.SerializeContext context) {
            this.placeholderNode = Objects.requireNonNull(this.checkpoint).requireSingleNode();
        }

        @Override
        void afterBackfill(ByteBuf target, int encodedLength) {
            if (this.placeholderNode == null) {
                return;
            }
            final int lengthFieldByteCount = lengthFieldByteCount(this.plan.writer());
            final String hexString = FormatUtils.toHexString(target, this.placeholderStart, lengthFieldByteCount);
            this.placeholderNode.update(encodedLength, hexString, this.placeholderStart, this.placeholderStart + lengthFieldByteCount);
        }

    }

}
