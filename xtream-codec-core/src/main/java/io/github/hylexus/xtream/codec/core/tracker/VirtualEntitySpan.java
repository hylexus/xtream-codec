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

@SuppressWarnings("NullAway")
public class VirtualEntitySpan extends BaseSpan implements BaseSpan.HasFieldName {
    private String entityClass;
    private String fieldName;
    private final String fieldDesc;

    public VirtualEntitySpan(String fieldName, String fieldDesc) {
        super(null);
        this.fieldName = fieldName;
        this.fieldDesc = fieldDesc;
    }

    public VirtualEntitySpan(RootSpan rootSpan, String fieldName, String fieldDesc) {
        super(null);
        this.fieldName = fieldName;
        this.fieldDesc = fieldDesc;
        this.setEntityClass(rootSpan.getEntityClass());
        this.setHexString(rootSpan.getHexString());
        this.getChildren().addAll(rootSpan.getChildren());
    }

    public String getEntityClass() {
        return entityClass;
    }

    public VirtualEntitySpan setEntityClass(String entityClass) {
        this.entityClass = entityClass;
        return this;
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

    @Override
    public String toString() {
        return new StringJoiner(", ", VirtualEntitySpan.class.getSimpleName() + "[", "]")
                .add("entityClass='" + entityClass + "'")
                .add("hexString='" + hexString + "'")
                .toString();
    }
}
