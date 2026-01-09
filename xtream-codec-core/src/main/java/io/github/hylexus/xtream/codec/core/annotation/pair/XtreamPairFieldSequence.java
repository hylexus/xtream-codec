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

package io.github.hylexus.xtream.codec.core.annotation.pair;

import io.github.hylexus.xtream.codec.core.ContainerInstanceFactory;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.ext.Key;
import io.github.hylexus.xtream.codec.core.annotation.ext.ValueDecoderCommonParam;
import io.github.hylexus.xtream.codec.core.annotation.ext.ValueMatcher;
import io.github.hylexus.xtream.codec.core.impl.codec.pair.PairCodecs;
import org.jetbrains.annotations.ApiStatus;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @see io.github.hylexus.xtream.codec.core.impl.domain.XtreamPairFieldSequenceMeta
 * @since 0.4.0
 */
@Documented
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@XtreamField(fieldCodec = PairCodecs.PairSequenceCodec.class)
@ApiStatus.Experimental
@ApiStatus.AvailableSince("0.4.0")
public @interface XtreamPairFieldSequence {

    Decoder decoder() default @Decoder(key = {}, value = @Value(matchers = {}));

    @interface Decoder {
        Key[] key();

        Value value();
    }

    @interface Value {
        ValueDecoderCommonParam[] commonParams() default {};

        ValueMatcher[] matchers();
    }

    // 以下都是 XtreamField 的属性
    @AliasFor(annotation = XtreamField.class, attribute = "order")
    int order() default -1;

    @AliasFor(annotation = XtreamField.class, attribute = "length")
    int length() default -1;

    @AliasFor(annotation = XtreamField.class, attribute = "lengthExpression")
    String lengthExpression() default "";

    @AliasFor(annotation = XtreamField.class, attribute = "prependLengthFieldLength")
    int prependLengthFieldLength() default -1;

    @AliasFor(annotation = XtreamField.class, attribute = "prependLengthFieldType")
    PrependLengthFieldType prependLengthFieldType() default PrependLengthFieldType.none;

    @AliasFor(annotation = XtreamField.class, attribute = "condition")
    String condition() default "";

    @AliasFor(annotation = XtreamField.class, attribute = "containerInstanceFactory")
    Class<? extends ContainerInstanceFactory> containerInstanceFactory() default ContainerInstanceFactory.LinkedHashMapContainerInstanceFactory.class;

    @AliasFor(annotation = XtreamField.class, attribute = "desc")
    String desc() default "";
}
