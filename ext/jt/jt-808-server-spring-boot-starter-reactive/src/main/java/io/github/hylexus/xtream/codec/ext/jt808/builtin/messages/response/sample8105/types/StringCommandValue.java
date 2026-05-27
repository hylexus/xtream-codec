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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.sample8105.types;

import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.netty.buffer.ByteBuf;

public class StringCommandValue extends AbstractCommandValue<String> {
    public StringCommandValue(int offset) {
        super(offset);
    }

    public StringCommandValue(int offset, String value) {
        super(offset, value);
    }

    @Override
    public void writeTo(ByteBuf output) {
        output.writeCharSequence(value(), XtreamConstants.CHARSET_GBK);
        writeSeparator(output);
    }

    @Override
    public StringCommandValue doReadFrom(ByteBuf input) {
        final CharSequence charSequence = input.readCharSequence(input.readableBytes(), XtreamConstants.CHARSET_GBK);
        this.value = charSequence.toString();
        return this;
    }
}
