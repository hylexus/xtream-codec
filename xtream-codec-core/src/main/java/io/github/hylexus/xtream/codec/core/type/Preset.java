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

package io.github.hylexus.xtream.codec.core.type;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.ContainerInstanceFactory;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.*;
import io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author hylexus
 */
public @interface Preset {
    @interface RustStyle {

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.RustStyleContainerI8.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, length = 1, signedness = NumberSignedness.SIGNED)
        @interface i8 {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.RustStyleContainerU8.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, length = 1, signedness = NumberSignedness.UNSIGNED)
        @interface u8 {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.RustStyleContainerI16.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, length = 2, signedness = NumberSignedness.SIGNED)
        @interface i16 {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.RustStyleContainerI16Le.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, length = 2, littleEndian = true, signedness = NumberSignedness.SIGNED)
        @interface i16_le {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.RustStyleContainerU16.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, length = 2, signedness = NumberSignedness.UNSIGNED)
        @interface u16 {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.RustStyleContainerU16Le.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, length = 2, littleEndian = true, signedness = NumberSignedness.UNSIGNED)
        @interface u16_le {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.RustStyleContainerI32.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, length = 4, signedness = NumberSignedness.SIGNED)
        @interface i32 {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.RustStyleContainerI32Le.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, length = 4, littleEndian = true, signedness = NumberSignedness.SIGNED)
        @interface i32_le {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.RustStyleContainerU32.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, length = 4, signedness = NumberSignedness.UNSIGNED)
        @interface u32 {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.RustStyleContainerU32Le.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, length = 4, littleEndian = true, signedness = NumberSignedness.UNSIGNED)
        @interface u32_le {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.RustStyleContainerStr.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, charset = "utf-8")
        @interface str {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamField.class, attribute = "length")
            int length() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "lengthExpression")
            String lengthExpression() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "charset")
            String charset() default "utf-8";

            @AliasFor(annotation = XtreamField.class, attribute = "paddingLeft")
            Padding paddingLeft() default @Padding(minEncodedLength = 0);

            @AliasFor(annotation = XtreamField.class, attribute = "paddingRight")
            Padding paddingRight() default @Padding(minEncodedLength = 0);

            @AliasFor(annotation = XtreamField.class, attribute = "prependLengthFieldLength")
            int prependLengthFieldLength() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "prependLengthFieldType")
            PrependLengthFieldType prependLengthFieldType() default PrependLengthFieldType.none;

            @AliasFor(annotation = XtreamField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "fieldCodec")
            Class<? extends FieldCodec<?>> fieldCodec() default FieldCodec.Placeholder.class;

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.RustStyleContainerStruct.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.struct)
        @interface struct {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

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

            @AliasFor(annotation = XtreamField.class, attribute = "fieldCodec")
            Class<? extends FieldCodec<?>> fieldCodec() default FieldCodec.Placeholder.class;

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.RustStyleContainerDyn.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.dynamic)
        @interface dyn {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

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

            @AliasFor(annotation = XtreamField.class, attribute = "fieldCodec")
            Class<? extends FieldCodec<?>> fieldCodec() default FieldCodec.Placeholder.class;

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.RustStyleContainerList.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.sequence, containerInstanceFactory = ContainerInstanceFactory.ArrayListContainerInstanceFactory.class)
        @interface list {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

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

            @AliasFor(annotation = XtreamField.class, attribute = "fieldCodec")
            Class<? extends FieldCodec<?>> fieldCodec() default FieldCodec.Placeholder.class;

            @AliasFor(annotation = XtreamField.class, attribute = "containerInstanceFactory")
            Class<? extends ContainerInstanceFactory> containerInstanceFactory() default ContainerInstanceFactory.ArrayListContainerInstanceFactory.class;

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.RustStyleContainerMap.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.map, containerInstanceFactory = ContainerInstanceFactory.LinkedHashMapContainerInstanceFactory.class)
        @interface map {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

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

            @AliasFor(annotation = XtreamField.class, attribute = "fieldCodec")
            Class<? extends FieldCodec<?>> fieldCodec() default FieldCodec.Placeholder.class;

            @AliasFor(annotation = XtreamField.class, attribute = "containerInstanceFactory")
            Class<? extends ContainerInstanceFactory> containerInstanceFactory() default ContainerInstanceFactory.LinkedHashMapContainerInstanceFactory.class;

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @XtreamMapField(key = {}, valueLength = {}, value = @XtreamMapField.Value())
        @interface simple_map {

            @AliasFor(annotation = XtreamMapField.class, attribute = "key")
            XtreamMapField.Key[] key();

            @AliasFor(annotation = XtreamMapField.class, attribute = "valueLength")
            XtreamMapField.ValueLength[] valueLength();

            @AliasFor(annotation = XtreamMapField.class, attribute = "value")
            XtreamMapField.Value value();

            @AliasFor(annotation = XtreamMapField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamMapField.class, attribute = "length")
            int length() default -1;

            @AliasFor(annotation = XtreamMapField.class, attribute = "lengthExpression")
            String lengthExpression() default "";

            @AliasFor(annotation = XtreamMapField.class, attribute = "prependLengthFieldLength")
            int prependLengthFieldLength() default -1;

            @AliasFor(annotation = XtreamMapField.class, attribute = "prependLengthFieldType")
            PrependLengthFieldType prependLengthFieldType() default PrependLengthFieldType.none;

            @AliasFor(annotation = XtreamMapField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamMapField.class, attribute = "containerInstanceFactory")
            Class<? extends ContainerInstanceFactory> containerInstanceFactory() default ContainerInstanceFactory.LinkedHashMapContainerInstanceFactory.class;

            @AliasFor(annotation = XtreamMapField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.RustStyleContainerByteArray.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic)
        @interface byte_array {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

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

            @AliasFor(annotation = XtreamField.class, attribute = "fieldCodec")
            Class<? extends FieldCodec<?>> fieldCodec() default FieldCodec.Placeholder.class;

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target(ElementType.RECORD_COMPONENT)
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.RustStyleContainerTransientRecordComponent.class)
        @XtreamField(
                dataType = BeanPropertyMetadata.FiledDataType.basic,
                codecStrategy = XtreamField.CodecStrategy.TRANSIENT
        )
        @interface transient_record_component {
            @AliasFor(annotation = XtreamField.class, attribute = "nulls")
            XtreamField.Nulls nulls() default XtreamField.Nulls.AS_NULL;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};
        }
    }

    @interface JtStyle {
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.JtStyleContainerWord.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, length = 2, signedness = NumberSignedness.UNSIGNED)
        @interface Word {

            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamField.class, attribute = "littleEndian")
            boolean littleEndian() default false;

            @AliasFor(annotation = XtreamField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.JtStyleContainerDword.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, length = 4, signedness = NumberSignedness.UNSIGNED)
        @interface Dword {

            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamField.class, attribute = "littleEndian")
            boolean littleEndian() default false;

            @AliasFor(annotation = XtreamField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.JtStyleContainerBcd.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, charset = XtreamConstants.CHARSET_NAME_BCD_8421)
        @interface Bcd {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

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

            @AliasFor(annotation = XtreamField.class, attribute = "fieldCodec")
            Class<? extends FieldCodec<?>> fieldCodec() default FieldCodec.Placeholder.class;

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.JtStyleContainerBcdDateTime.class)
        @XtreamDateTimeField(pattern = "yyMMddHHmmss", charset = XtreamConstants.CHARSET_NAME_BCD_8421)
        @interface BcdDateTime {

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamDateTimeField.class, attribute = "pattern")
            String pattern() default "yyMMddHHmmss";

            @AliasFor(annotation = XtreamDateTimeField.class, attribute = "length")
            int length() default 6;

            @AliasFor(annotation = XtreamDateTimeField.class, attribute = "prependLengthFieldLength")
            int prependLengthFieldLength() default -1;

            @AliasFor(annotation = XtreamDateTimeField.class, attribute = "prependLengthFieldType")
            PrependLengthFieldType prependLengthFieldType() default PrependLengthFieldType.none;

            @AliasFor(annotation = XtreamDateTimeField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamDateTimeField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamDateTimeField.class, attribute = "desc")
            String desc() default "";
        }

        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.JtStyleContainerByte.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, length = 1, signedness = NumberSignedness.UNSIGNED)
        @interface Byte {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.JtStyleContainerBytes.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic)
        @interface Bytes {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

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

            @AliasFor(annotation = XtreamField.class, attribute = "fieldCodec")
            Class<? extends FieldCodec<?>> fieldCodec() default FieldCodec.Placeholder.class;

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @SuppressWarnings("checkstyle:TypeName")
        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.JtStyleContainerStr.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, charset = XtreamConstants.CHARSET_NAME_GBK)
        @interface Str {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamField.class, attribute = "length")
            int length() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "lengthExpression")
            String lengthExpression() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "charset")
            String charset() default XtreamConstants.CHARSET_NAME_GBK;

            @AliasFor(annotation = XtreamField.class, attribute = "paddingLeft")
            Padding paddingLeft() default @Padding(minEncodedLength = 0);

            @AliasFor(annotation = XtreamField.class, attribute = "paddingRight")
            Padding paddingRight() default @Padding(minEncodedLength = 0);

            @AliasFor(annotation = XtreamField.class, attribute = "prependLengthFieldLength")
            int prependLengthFieldLength() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "prependLengthFieldType")
            PrependLengthFieldType prependLengthFieldType() default PrependLengthFieldType.none;

            @AliasFor(annotation = XtreamField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "fieldCodec")
            Class<? extends FieldCodec<?>> fieldCodec() default FieldCodec.Placeholder.class;

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.JtStyleContainerObject.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.struct)
        @interface Object {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamField.class, attribute = "length")
            int length() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "lengthExpression")
            String lengthExpression() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "prependLengthFieldLength")
            int prependLengthFieldLength() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "prependLengthFieldType")
            PrependLengthFieldType prependLengthFieldType() default PrependLengthFieldType.none;

            @AliasFor(annotation = XtreamField.class, attribute = "fieldCodec")
            Class<? extends FieldCodec<?>> fieldCodec() default FieldCodec.Placeholder.class;

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.JtStyleContainerRuntimeType.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.dynamic)
        @interface RuntimeType {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

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

            @AliasFor(annotation = XtreamField.class, attribute = "fieldCodec")
            Class<? extends FieldCodec<?>> fieldCodec() default FieldCodec.Placeholder.class;

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.JtStyleContainerList.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.sequence, containerInstanceFactory = ContainerInstanceFactory.ArrayListContainerInstanceFactory.class)
        @interface List {
            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

            @AliasFor(annotation = XtreamField.class, attribute = "length")
            int length() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "lengthExpression")
            String lengthExpression() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "iterationTimes")
            int iterationTimes() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "iterationTimesExpression")
            String iterationTimesExpression() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "prependLengthFieldLength")
            int prependLengthFieldLength() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "prependLengthFieldType")
            PrependLengthFieldType prependLengthFieldType() default PrependLengthFieldType.none;

            @AliasFor(annotation = XtreamField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamField.class, attribute = "fieldCodec")
            Class<? extends FieldCodec<?>> fieldCodec() default FieldCodec.Placeholder.class;

            @AliasFor(annotation = XtreamField.class, attribute = "containerInstanceFactory")
            Class<? extends ContainerInstanceFactory> containerInstanceFactory() default ContainerInstanceFactory.ArrayListContainerInstanceFactory.class;

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.JtStyleContainerMap.class)
        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.map, containerInstanceFactory = ContainerInstanceFactory.LinkedHashMapContainerInstanceFactory.class)
        @interface Map {

            @AliasFor(annotation = XtreamField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

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

            @AliasFor(annotation = XtreamField.class, attribute = "fieldCodec")
            Class<? extends FieldCodec<?>> fieldCodec() default FieldCodec.Placeholder.class;

            @AliasFor(annotation = XtreamField.class, attribute = "containerInstanceFactory")
            Class<? extends ContainerInstanceFactory> containerInstanceFactory() default ContainerInstanceFactory.LinkedHashMapContainerInstanceFactory.class;

            @AliasFor(annotation = XtreamField.class, attribute = "desc")
            String desc() default "";
        }

        @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @XtreamMapField(key = {}, valueLength = {}, value = @XtreamMapField.Value())
        @interface SimpleMap {

            @AliasFor(annotation = XtreamMapField.class, attribute = "key")
            XtreamMapField.Key[] key();

            @AliasFor(annotation = XtreamMapField.class, attribute = "valueLength")
            XtreamMapField.ValueLength[] valueLength();

            @AliasFor(annotation = XtreamMapField.class, attribute = "value")
            XtreamMapField.Value value();

            @AliasFor(annotation = XtreamMapField.class, attribute = "order")
            int order() default -1;

            @AliasFor(annotation = XtreamMapField.class, attribute = "length")
            int length() default -1;

            @AliasFor(annotation = XtreamMapField.class, attribute = "lengthExpression")
            String lengthExpression() default "";

            @AliasFor(annotation = XtreamMapField.class, attribute = "prependLengthFieldLength")
            int prependLengthFieldLength() default -1;

            @AliasFor(annotation = XtreamMapField.class, attribute = "prependLengthFieldType")
            PrependLengthFieldType prependLengthFieldType() default PrependLengthFieldType.none;

            @AliasFor(annotation = XtreamMapField.class, attribute = "condition")
            String condition() default "";

            @AliasFor(annotation = XtreamMapField.class, attribute = "containerInstanceFactory")
            Class<? extends ContainerInstanceFactory> containerInstanceFactory() default ContainerInstanceFactory.LinkedHashMapContainerInstanceFactory.class;

            @AliasFor(annotation = XtreamMapField.class, attribute = "desc")
            String desc() default "";
        }

        @Target(ElementType.RECORD_COMPONENT)
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Repeatable(PresetAliasContainers.JtStyleContainerTransientRecordComponent.class)
        @XtreamField(
                dataType = BeanPropertyMetadata.FiledDataType.basic,
                codecStrategy = XtreamField.CodecStrategy.TRANSIENT
        )
        @interface TransientRecordComponent {

            @AliasFor(annotation = XtreamField.class, attribute = "nulls")
            XtreamField.Nulls nulls() default XtreamField.Nulls.AS_NULL;

            @AliasFor(annotation = XtreamField.class, attribute = "version")
            int[] version() default {XtreamField.ALL_VERSION};

        }
    }

}
