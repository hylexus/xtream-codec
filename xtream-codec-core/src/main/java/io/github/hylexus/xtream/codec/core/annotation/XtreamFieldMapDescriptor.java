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

package io.github.hylexus.xtream.codec.core.annotation;

import io.github.hylexus.xtream.codec.core.FieldCodec;
import lombok.Getter;

import java.lang.annotation.*;

/**
 * @author hylexus
 * @see FieldCodec
 * @see io.github.hylexus.xtream.codec.core.FieldCodecRegistry
 * @see io.github.hylexus.xtream.codec.core.EntityCodec
 * @see io.github.hylexus.xtream.codec.core.type.Preset.RustStyle
 * @see io.github.hylexus.xtream.codec.core.type.Preset.JtStyle
 * @see <a href="https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/expressions.html">SpEL 官方文档</a>
 * @see <a href="https://stackoverflow.com/questions/5001172/java-reflection-getting-fields-and-methods-in-declaration-order">java-reflection-getting-fields-and-methods-in-declaration-order</a>
 */
@Documented
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface XtreamFieldMapDescriptor {

    KeyDescriptor keyDescriptor();

    ValueLengthFieldDescriptor valueLengthFieldDescriptor();

    ValueDecoderDescriptors valueDecoderDescriptors() default @ValueDecoderDescriptors(
            defaultValueDecoderDescriptor = @ValueDecoderDescriptor(javaType = byte[].class, config = @XtreamField()),
            valueDecoderDescriptors = {}
    );

    ValueEncoderDescriptors valueEncoderDescriptors() default @ValueEncoderDescriptors(
            defaultValueEncoderDescriptor = @ValueEncoderDescriptor(config = @XtreamField()),
            valueEncoderDescriptors = {}
    );

    // ==========================
    @interface ValueDecoderDescriptors {
        ValueDecoderDescriptor defaultValueDecoderDescriptor();

        ValueCodecConfig[] valueDecoderDescriptors() default {};
    }

    @interface ValueEncoderDescriptors {
        ValueEncoderDescriptor defaultValueEncoderDescriptor();

        ValueCodecConfig[] valueEncoderDescriptors() default {};
    }

    @interface ValueDecoderDescriptor {
        Class<?> javaType();

        XtreamField config() default @XtreamField();
    }

    @interface ValueEncoderDescriptor {

        XtreamField config() default @XtreamField();
    }

    @interface ValueCodecConfig {
        int valueLengthFieldSize() default -1;

        boolean valueLengthFieldSizeIsLittleEndian() default false;

        byte whenKeyIsI8() default -1;

        short whenKeyIsU8() default -1;

        short whenKeyIsI16() default -1;

        int whenKeyIsU16() default -1;

        int whenKeyIsI32() default -1;

        long whenKeyIsU32() default -1;

        String whenKeyIsStr() default "";

        Class<?> javaType();

        XtreamField config() default @XtreamField();

        String desc() default "";
    }

    @Getter
    enum KeyType {
        i8(Byte.class, 1, NumberSignedness.SIGNED),
        u8(Short.class, 1, NumberSignedness.UNSIGNED),
        i16(Short.class, 2, NumberSignedness.SIGNED),
        u16(Integer.class, 2, NumberSignedness.UNSIGNED),
        i32(Integer.class, 4, NumberSignedness.SIGNED),
        u32(Long.class, 4, NumberSignedness.UNSIGNED),
        str(String.class, -1, NumberSignedness.NONE),
        ;

        private final Class<?> javaType;
        private final int sizeInBytes;
        private final NumberSignedness signedness;

        KeyType(Class<?> javaType, int sizeInBytes, NumberSignedness signedness) {
            this.javaType = javaType;
            this.sizeInBytes = sizeInBytes;
            this.signedness = signedness;
        }
    }

    @interface KeyDescriptor {
        KeyType type();

        int length() default -1;

        String charset() default "UTF-8";

        boolean littleEndian() default false;
    }

    @interface ValueLengthFieldDescriptor {
        int length();

        boolean littleEndian() default false;
    }

}
