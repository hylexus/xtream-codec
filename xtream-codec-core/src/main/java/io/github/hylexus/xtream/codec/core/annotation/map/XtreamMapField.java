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

package io.github.hylexus.xtream.codec.core.annotation.map;

import io.github.hylexus.xtream.codec.core.ContainerInstanceFactory;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.codec.BytesFieldCodecs;
import io.github.hylexus.xtream.codec.core.impl.codec.map.MapFieldCodec;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
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

    @interface Key {
        int[] version() default {ALL_VERSION};

        KeyType type() default KeyType.u8;

        int sizeInBytes() default -1;

        // todo string 类型支持 padding
        String charset() default DEFAULT_CHARSET;
    }

    @interface ValueLength {
        int[] version() default {ALL_VERSION};

        ValueLengthType type();
    }

    @interface Value {
        ValueEncoder encoder() default @ValueEncoder();

        ValueDecoder decoder() default @ValueDecoder();
    }

    @interface ValueDecoder {
        DecoderParam[] params() default {};

        ValueMatcher[] matchers() default {};

        FallbackValueMatcher[] fallbackMatchers() default {@FallbackValueMatcher(valueCodec = BytesFieldCodecs.BytesFieldCodecByteArray.class)};
    }

    @interface ValueEncoder {
        EncoderParam[] params() default {@EncoderParam(version = ALL_VERSION, charset = DEFAULT_CHARSET)};

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

    @interface ValueMatcher {
        int[] version() default {ALL_VERSION};

        byte matchI8() default -1;

        short matchU8() default -1;

        short matchI16() default -1;

        int matchU16() default -1;

        long matchU32() default -1;

        int matchI32() default -1;

        long matchI64() default -1;

        String matchString() default "";

        String charset() default "";

        XtreamDataType valueType() default XtreamDataType.placeholder;

        Class<? extends FieldCodec<?>> valueCodec() default FieldCodec.Placeholder.class;

        Class<?> valueEntity() default Object.class;

        String desc() default "";
    }

    @interface FallbackValueMatcher {
        int[] version() default {ALL_VERSION};

        XtreamDataType valueType() default XtreamDataType.placeholder;

        Class<? extends FieldCodec<?>> valueCodec() default FieldCodec.Placeholder.class;

        String charset() default "";
    }

    enum KeyType {
        i8(XtreamDataType.i8),
        u8(XtreamDataType.u8),
        i16(XtreamDataType.i16),
        u16(XtreamDataType.u16),
        i32(XtreamDataType.i32),
        u32(XtreamDataType.u32),
        i64(XtreamDataType.i64),
        str(XtreamDataType.string),
        str_gb_2312(XtreamDataType.string_gb_2312),
        str_gbk(XtreamDataType.string_gbk),
        str_utf8(XtreamDataType.string_utf8),
        ;

        private final XtreamDataType type;

        KeyType(XtreamDataType type) {
            this.type = type;
        }

        public XtreamDataType type() {
            return type;
        }
    }

    enum ValueLengthType {
        i8(XtreamDataType.i8),
        u8(XtreamDataType.u8),
        i16(XtreamDataType.i16),
        u16(XtreamDataType.u16),
        i32(XtreamDataType.i32),
        u32(XtreamDataType.u32),
        ;

        private final XtreamDataType type;

        ValueLengthType(XtreamDataType type) {
            this.type = type;
        }

        public XtreamDataType type() {
            return type;
        }
    }
}
