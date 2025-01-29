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

public class RootSpan extends BaseSpan {
    private String entityClass;

    public RootSpan() {
        super(null);
    }

    public String getEntityClass() {
        return entityClass;
    }

    public RootSpan setEntityClass(String entityClass) {
        this.entityClass = entityClass;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RootSpan.class.getSimpleName() + "[", "]")
                .add("entityClass='" + entityClass + "'")
                .add("hexString='" + hexString + "'")
                .toString();
    }
}
