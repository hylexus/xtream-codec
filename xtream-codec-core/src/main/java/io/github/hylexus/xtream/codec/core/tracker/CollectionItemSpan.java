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

import org.jspecify.annotations.Nullable;

import java.util.StringJoiner;

public class CollectionItemSpan extends BaseSpan implements BaseSpan.HasFieldName {
    private String fieldName;
    private final int offset;
    private final @Nullable String fieldType;

    public CollectionItemSpan(BaseSpan parent, String fieldName, int offset, @Nullable String fieldType) {
        super(parent);
        this.fieldName = fieldName;
        this.offset = offset;
        this.fieldType = fieldType;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void setFieldName(String name) {
        this.fieldName = name;
    }

    public int getOffset() {
        return offset;
    }

    public @Nullable String getFieldType() {
        return fieldType;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CollectionItemSpan.class.getSimpleName() + "[", "]")
                .add("fieldName='" + fieldName + "'")
                .add("offset=" + offset)
                .add("fieldType=" + fieldType)
                .add("hexString=" + hexString)
                .toString();
    }
}
