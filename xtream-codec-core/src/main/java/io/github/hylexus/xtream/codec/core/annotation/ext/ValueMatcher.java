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

package io.github.hylexus.xtream.codec.core.annotation.ext;

import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.domain.ValueMatcherMeta;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;

/**
 * @author hylexus
 * @see ValueMatcherMeta
 */
public @interface ValueMatcher {

    /**
     * 反序列化 {@link java.util.List}&lt;{@link io.github.hylexus.xtream.codec.core.type.Pair}&gt; 类型时必须指定值长度
     */
    int length() default -1;

    int[] version() default {XtreamField.ALL_VERSION};

    byte[] matchI8() default {};

    short[] matchU8() default {};

    short[] matchI16() default {};

    int[] matchU16() default {};

    long[] matchU32() default {};

    int[] matchI32() default {};

    long[] matchI64() default {};

    String[] matchString() default {};

    String charset() default "";

    XtreamDataType valueType() default XtreamDataType.placeholder;

    Class<? extends FieldCodec<?>> valueCodec() default FieldCodec.Placeholder.class;

    Class<?> valueEntity() default Object.class;

    String desc() default "";

}
