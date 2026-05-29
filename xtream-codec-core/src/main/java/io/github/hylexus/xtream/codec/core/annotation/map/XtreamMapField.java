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

package io.github.hylexus.xtream.codec.core.annotation.map;

import io.github.hylexus.xtream.codec.core.ContainerInstanceFactory;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.ext.FallbackValueMatcher;
import io.github.hylexus.xtream.codec.core.annotation.ext.Key;
import io.github.hylexus.xtream.codec.core.annotation.ext.ValueLength;
import io.github.hylexus.xtream.codec.core.annotation.ext.ValueMatcher;
import io.github.hylexus.xtream.codec.core.impl.codec.BytesFieldCodecs;
import io.github.hylexus.xtream.codec.core.impl.codec.map.MapFieldCodec;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author hylexus
 */
@Documented
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@XtreamField(fieldCodec = MapFieldCodec.class)
public @interface XtreamMapField {

    int ALL_VERSION = XtreamField.ALL_VERSION;

    String DEFAULT_CHARSET = "utf-8";

    Key[] key();

    ValueLength[] valueLength();

    Value value();

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

    @interface Value {
        ValueEncoder encoder() default @ValueEncoder();

        ValueDecoder decoder() default @ValueDecoder();
    }

    @interface ValueDecoder {
        DecoderParam[] params() default {@DecoderParam};

        ValueMatcher[] matchers() default {};

        FallbackValueMatcher[] fallbackMatchers() default {@FallbackValueMatcher(valueCodec = BytesFieldCodecs.BytesFieldCodecByteArray.class)};
    }

    @interface ValueEncoder {
        EncoderParam[] params() default {@EncoderParam};

        ValueMatcher[] matchers() default {};
    }

    @interface EncoderParam {
        int[] version() default {ALL_VERSION};

        boolean writeNumberAsLittleEndian() default false;

        String charset() default DEFAULT_CHARSET;
    }

    @interface DecoderParam {
        int[] version() default {ALL_VERSION};

        String charset() default DEFAULT_CHARSET;
    }

}
