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

package io.github.hylexus.xtream.codec.base.expression;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class XtreamExpressionEngineRegistry {

    private final Map<String, XtreamExpressionEngine> engines = new ConcurrentHashMap<>();

    public XtreamExpressionEngineRegistry() {
        this(true);
    }

    public XtreamExpressionEngineRegistry(boolean registerDefaults) {
        if (registerDefaults) {
            this.register(new SpelXtreamExpressionEngine());
            this.register(new AviatorXtreamExpressionEngine());
            this.register(new MvelXtreamExpressionEngine());
        }
    }

    public void register(XtreamExpressionEngine engine) {
        this.engines.put(engine.id().value(), Objects.requireNonNull(engine));
    }

    public XtreamExpressionEngine getEngine(XtreamExpressionEngine.XtreamExpressionEngineId id) {
        final XtreamExpressionEngine engine = this.engines.get(id.value());
        if (engine == null) {
            throw new IllegalArgumentException("No XtreamEngine registered for id: " + id);
        }
        return engine;
    }

    public void unregister(XtreamExpressionEngine.XtreamExpressionEngineId id) {
        this.engines.remove(id.value());
    }
}
