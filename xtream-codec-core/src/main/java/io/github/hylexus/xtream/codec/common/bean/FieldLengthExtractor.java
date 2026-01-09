/*
 * Copyright 2024 the original author or authors.
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

import io.github.hylexus.xtream.codec.base.expression.XtreamEvaluationContext;
import io.github.hylexus.xtream.codec.base.expression.XtreamExpression;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.tracker.BaseSpan;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;

import java.util.Objects;
import java.util.StringJoiner;

public sealed interface FieldLengthExtractor
        permits FieldLengthExtractor.ConstantFieldLengthExtractor,
        FieldLengthExtractor.PrependFieldLengthExtractor,
        FieldLengthExtractor.ExpressionFieldLengthExtractor,
        FieldLengthExtractor.PlaceholderFieldLengthExtractor,
        FieldLengthExtractor.CustomFieldLengthExtractor {

    int extractFieldLength(FieldCodec.DeserializeContext context, XtreamEvaluationContext evaluationContext, ByteBuf input);

    default int extractFieldLengthWithTracker(FieldCodec.DeserializeContext context, XtreamEvaluationContext evaluationContext, ByteBuf input) {
        return this.extractFieldLength(context, evaluationContext, input);
    }

    record ConstantFieldLengthExtractor(int length) implements FieldLengthExtractor {

        @Override
        public int extractFieldLength(FieldCodec.DeserializeContext context, XtreamEvaluationContext evaluationContext, ByteBuf input) {
            return this.length;
        }
    }

    record PrependFieldLengthExtractor(PrependLengthFieldType prependLengthFieldType) implements FieldLengthExtractor {

        public PrependFieldLengthExtractor(int length) {
            this(PrependLengthFieldType.from(length));
        }

        @Override
        public int extractFieldLength(FieldCodec.DeserializeContext context, XtreamEvaluationContext evaluationContext, ByteBuf input) {
            return this.prependLengthFieldType.readFrom(input);
        }

        @Override
        public int extractFieldLengthWithTracker(FieldCodec.DeserializeContext context, XtreamEvaluationContext evaluationContext, ByteBuf input) {
            final int indexBeforeRead = input.readerIndex();
            final int value = this.prependLengthFieldType.readFrom(input);
            final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
            final BaseSpan currentSpan = codecTracker.getCurrentSpan();
            final String hexString = FormatUtils.toHexString(input, indexBeforeRead, input.readerIndex() - indexBeforeRead);
            codecTracker.addPrependLengthFieldSpan(currentSpan, "prependLengthField", value, hexString, this.prependLengthFieldType.name(), "前置长度字段");
            return value;
        }
    }

    record ExpressionFieldLengthExtractor(XtreamExpression expression, String expressionString) implements FieldLengthExtractor {

        public ExpressionFieldLengthExtractor(XtreamExpression expression) {
            this(expression, expression.expressionString());
        }

        @Override
        public int extractFieldLength(FieldCodec.DeserializeContext context, XtreamEvaluationContext evaluationContext, ByteBuf input) {
            final Number number = expression.getValue(evaluationContext, Number.class);
            if (number == null) {
                throw new IllegalArgumentException("Can not determine field length with Expression[" + expressionString + "]");
            }
            return number.intValue();
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ExpressionFieldLengthExtractor.class.getSimpleName() + "[", "]")
                    .add("expressionString='" + expressionString + "'")
                    .toString();
        }
    }

    record PlaceholderFieldLengthExtractor(String msg) implements FieldLengthExtractor {

        @Override
        public int extractFieldLength(FieldCodec.DeserializeContext context, XtreamEvaluationContext evaluationContext, ByteBuf input) {
            throw new IllegalArgumentException(msg);
        }
    }

    non-sealed interface CustomFieldLengthExtractor extends FieldLengthExtractor {

        @Override
        int extractFieldLength(FieldCodec.DeserializeContext context, XtreamEvaluationContext evaluationContext, ByteBuf input);

    }

}
