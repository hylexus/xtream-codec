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

import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;

public enum CodecCharset {
    UTF_8(XtreamConstants.CHARSET_NAME_UTF8),
    GBK(XtreamConstants.CHARSET_NAME_GBK),
    GB_2312(XtreamConstants.CHARSET_NAME_GB_2312),
    BCD_8421(XtreamConstants.CHARSET_NAME_BCD_8421),
    HEX(XtreamConstants.CHARSET_NAME_HEX),
    UNSUPPORTED(null) {
        @Override
        public String charsetName() {
            throw new UnsupportedOperationException("CodecCharset.INVALID cannot be used to create a codec");
        }
    },
    DYNAMIC(null) {
        @Override
        public String charsetName() {
            throw new IllegalStateException("CodecCharset.DYNAMIC cannot be used to create a codec");
        }
    },
    ;
    private final String charsetName;

    CodecCharset(String charsetName) {
        this.charsetName = charsetName;
    }

    public String charsetName() {
        return this.charsetName;
    }

}
