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

package io.github.hylexus.xtream.codec.core.tracker;

import java.util.StringJoiner;

public class CollectionFieldSpan extends BaseSpan implements BaseSpan.HasFieldName {
    private String fieldName;
    private final String fieldType;
    private final String fieldDesc;

    public CollectionFieldSpan(BaseSpan parent, String fieldName, String fieldType, String fieldDesc) {
        super(parent);
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.fieldDesc = fieldDesc;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void setFieldName(String name) {
        this.fieldName = name;
    }

    public String getFieldDesc() {
        return fieldDesc;
    }

    public String getFieldType() {
        return fieldType;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CollectionFieldSpan.class.getSimpleName() + "[", "]")
                .add("fieldName='" + fieldName + "'")
                .add("fieldDesc='" + fieldDesc + "'")
                .add("fieldType='" + fieldType + "'")
                .add("hexString='" + hexString + "'")
                .toString();
    }

}
