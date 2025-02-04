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

public class NestedFieldSpan extends BaseSpan {
    private String fieldName;
    private final String fieldDesc;
    private String fieldType;
    private final String fieldCodec;

    public NestedFieldSpan(BaseSpan parent, String fieldName, String fieldDesc, String fieldType, String fieldCodec) {
        super(parent);
        this.fieldName = fieldName;
        this.fieldDesc = fieldDesc;
        this.fieldType = fieldType;
        this.fieldCodec = fieldCodec;
    }

    public NestedFieldSpan setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public NestedFieldSpan setFieldType(String fieldType) {
        this.fieldType = fieldType;
        return this;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public String getFieldDesc() {
        return fieldDesc;
    }

    public String getFieldCodec() {
        return fieldCodec;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NestedFieldSpan.class.getSimpleName() + "[", "]")
                .add("fieldName='" + fieldName + "'")
                .add("fieldDesc='" + fieldDesc + "'")
                .add("fieldCodec='" + fieldCodec + "'")
                .add("fieldType='" + fieldType + "'")
                .add("hexString='" + hexString + "'")
                .toString();
    }
}
