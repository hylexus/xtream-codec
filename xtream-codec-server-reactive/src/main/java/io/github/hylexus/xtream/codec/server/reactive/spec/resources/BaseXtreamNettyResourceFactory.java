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

package io.github.hylexus.xtream.codec.server.reactive.spec.resources;

import reactor.netty.resources.LoopResources;

public abstract non-sealed class BaseXtreamNettyResourceFactory implements XtreamNettyResourceFactory {
    protected LoopResources loopResources;
    protected boolean preferNative;

    public BaseXtreamNettyResourceFactory(XtreamNettyResourceFactory.LoopResourcesProperty property) {
        this.loopResources = LoopResources.create(
                property.prefix(),
                property.selectCount(),
                property.workerCount(),
                property.daemon(),
                property.colocate()
        );
        this.preferNative = property.preferNative();
    }

    @Override
    public LoopResources loopResources() {
        return this.loopResources;
    }

    @Override
    public boolean preferNative() {
        return this.preferNative;
    }
}
