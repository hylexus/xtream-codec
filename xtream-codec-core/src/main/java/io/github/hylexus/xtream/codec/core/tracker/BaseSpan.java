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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("NullAway")
public abstract class BaseSpan {
    protected final String id = UUID.randomUUID().toString();
    @JsonIgnore
    protected final @Nullable BaseSpan parent;
    protected final List<BaseSpan> children = new ArrayList<>();
    protected @Nullable String hexString;

    public BaseSpan(@Nullable BaseSpan parent) {
        this.parent = parent;
    }

    public void addChild(BaseSpan child) {
        this.children.add(child);
    }

    public void addChild(int offset, BaseSpan child) {
        this.children.add(offset, child);
    }

    public List<BaseSpan> getChildren() {
        return children;
    }

    public @Nullable BaseSpan getParent() {
        return parent;
    }

    public @Nullable String getHexString() {
        return hexString;
    }

    public BaseSpan setHexString(String hexString) {
        this.hexString = hexString;
        return this;
    }

    public String getSpanType() {
        return this.getClass().getSimpleName();
    }

    public String getId() {
        return id;
    }
}
