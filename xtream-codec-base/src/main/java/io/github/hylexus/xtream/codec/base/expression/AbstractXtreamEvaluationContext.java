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

package io.github.hylexus.xtream.codec.base.expression;

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AbstractXtreamEvaluationContext implements XtreamEvaluationContext {

    protected final Map<String, Object> variables = new ConcurrentHashMap<>();

    protected final @Nullable Object rootObject;

    public AbstractXtreamEvaluationContext(@Nullable Object rootObject) {
        this.rootObject = rootObject;
        this.setVariable(ROOT_OBJECT_KEY, rootObject);
    }

    @Override
    public @Nullable Object rootObject() {
        return this.rootObject;
    }

    @Override
    public Map<String, Object> getVariables() {
        return this.variables;
    }

    @Override
    public void setVariable(String name, @Nullable Object value) {
        if (value == null) {
            this.variables.remove(name);
        } else {
            this.variables.put(name, value);
        }
    }
}
