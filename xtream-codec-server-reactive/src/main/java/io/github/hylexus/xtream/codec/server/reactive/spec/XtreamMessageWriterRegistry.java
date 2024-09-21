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

package io.github.hylexus.xtream.codec.server.reactive.spec;

import io.github.hylexus.xtream.codec.core.annotation.OrderedComponent;

import java.util.List;

/**
 * @author hylexus
 */
public class XtreamMessageWriterRegistry {
    private final List<XtreamMessageWriter> writers;

    public XtreamMessageWriterRegistry(List<XtreamMessageWriter> writers) {
        this.writers = writers;
    }

    public XtreamMessageWriterRegistry removeWriter(Class<? extends XtreamMessageWriter> cls) {
        this.writers.removeIf(writer -> writer.getClass().equals(cls));
        return this;
    }

    public XtreamMessageWriterRegistry addWriter(XtreamMessageWriter writer) {
        this.writers.add(writer);
        return this;
    }

    public List<XtreamMessageWriter> getWriters() {
        return OrderedComponent.sort(this.writers);
    }
}
