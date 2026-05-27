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

public class MapEntrySpan extends BaseSpan {
    private final String name;
    private final int offset;

    public MapEntrySpan(BaseSpan parent, String name, int offset) {
        super(parent);
        this.name = name;
        this.offset = offset;
    }

    public String getName() {
        return name;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MapEntrySpan.class.getSimpleName() + "[", "]")
                .add("name=" + name)
                .add("offset=" + offset)
                .add("hexString=" + hexString)
                .toString();
    }
}
