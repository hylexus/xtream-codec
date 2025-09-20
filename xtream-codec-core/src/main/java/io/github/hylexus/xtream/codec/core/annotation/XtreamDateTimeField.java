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

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.impl.codec.XtreamDateTimeFieldCodec;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author hylexus
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@XtreamField(dataType = BeanPropertyMetadata.FiledDataType.basic, fieldCodec = XtreamDateTimeFieldCodec.class)
public @interface XtreamDateTimeField {

    @AliasFor(annotation = XtreamField.class, attribute = "charset")
    String charset();

    String pattern();

    @AliasFor(annotation = XtreamField.class, attribute = "length")
    int length() default -1;

    @AliasFor(annotation = XtreamField.class, attribute = "order")
    int order() default -1;

    @AliasFor(annotation = XtreamField.class, attribute = "lengthExpression")
    String lengthExpression() default "";

    @AliasFor(annotation = XtreamField.class, attribute = "prependLengthFieldLength")
    int prependLengthFieldLength() default -1;

    @AliasFor(annotation = XtreamField.class, attribute = "prependLengthFieldType")
    PrependLengthFieldType prependLengthFieldType() default PrependLengthFieldType.none;

    @AliasFor(annotation = XtreamField.class, attribute = "condition")
    String condition() default "";

    @AliasFor(annotation = XtreamField.class, attribute = "desc")
    String desc() default "";

}
