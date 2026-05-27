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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author hylexus
 */
public class TcpXtreamServerBuilder {
    protected final List<TcpNettyServerCustomizer> customizers;

    public TcpXtreamServerBuilder() {
        this.customizers = new ArrayList<>();
    }

    public TcpXtreamServerBuilder addServerCustomizer(TcpNettyServerCustomizer customizer) {
        this.customizers.add(customizer);
        return this;
    }

    public TcpXtreamServerBuilder addServerCustomizers(Collection<TcpNettyServerCustomizer> customizers) {
        this.customizers.addAll(customizers);
        return this;
    }

    public TcpXtreamServer build(String name) {
        return new TcpXtreamServer(name, this.customizers);
    }
}
