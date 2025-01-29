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

package io.github.hylexus.xtream.codec.core.tracker;

import java.util.StringJoiner;

public class CodecSpan extends BaseSpan {
    protected String fieldCodec;
    protected Object value;

    public CodecSpan(BaseSpan parent) {
        super(parent);
    }

    public String getFieldCodec() {
        return fieldCodec;
    }

    public CodecSpan setFieldCodec(String fieldCodec) {
        this.fieldCodec = fieldCodec;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public CodecSpan setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CodecSpan.class.getSimpleName() + "[", "]")
                .add("fieldCodec='" + fieldCodec + "'")
                .add("hexString='" + hexString + "'")
                .add("value=" + value)
                .toString();
    }
}
