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

package io.github.hylexus.xtream.codec.common.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class XtreamConstants {

    private XtreamConstants() {
        throw new UnsupportedOperationException();
    }

    public static final String CHARSET_NAME_GBK = "GBK";
    public static final String CHARSET_NAME_GB_2312 = "GB2312";
    public static final String CHARSET_NAME_UTF8 = "UTF-8";
    public static final String CHARSET_NAME_BCD_8421 = "BCD_8421";
    public static final String CHARSET_NAME_HEX = "HEX";

    public static final Charset CHARSET_GBK = Charset.forName(CHARSET_NAME_GBK);
    public static final Charset CHARSET_GB_2312 = Charset.forName(CHARSET_NAME_GB_2312);
    public static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;

}
