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

package io.github.hylexus.xtream.codec.core.annotation.ext;

import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.annotation.PaddingType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.domain.KeyMeta;

/**
 * @author hylexus
 * @see KeyMeta
 */
public @interface Key {
    int[] version() default {XtreamField.ALL_VERSION};

    /**
     * {@link java.util.Map} 中 {@code Key} 的类型
     */
    KeyType type() default KeyType.u8;

    /**
     * 仅仅 {@link String} 类型的 {@code Key} 用到
     */
    int sizeInBytes() default -1;

    /**
     * 仅仅 {@link String} 类型的 {@code Key} 用到
     */
    String charset() default XtreamConstants.CHARSET_NAME_UTF8;

    /**
     * 仅仅 {@link String} 类型的 {@code Key} 用到
     */
    PaddingType paddingType() default PaddingType.NONE;

    /**
     * 仅仅 {@link String} 类型的 {@code Key} 用到
     * <p>
     * {@code 0x30(48) == "0".getBytes()[0];}
     */
    byte paddingElement() default 0x30;

}
