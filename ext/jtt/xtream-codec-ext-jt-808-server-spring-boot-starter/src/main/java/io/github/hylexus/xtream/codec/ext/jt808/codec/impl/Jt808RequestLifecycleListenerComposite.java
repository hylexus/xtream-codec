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

package io.github.hylexus.xtream.codec.ext.jt808.codec.impl;

import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.function.Consumer;

public class Jt808RequestLifecycleListenerComposite implements Jt808RequestLifecycleListener {
    private final List<Jt808RequestLifecycleListener> listeners;

    public Jt808RequestLifecycleListenerComposite(List<Jt808RequestLifecycleListener> listeners) {
        this.listeners = listeners;
    }

    void invokeListeners(Consumer<Jt808RequestLifecycleListener> consumer) {
        for (final Jt808RequestLifecycleListener listener : this.listeners) {
            consumer.accept(listener);
        }
    }

    @Override
    public void afterRequestDecode(XtreamExchange exchange, Jt808Request jt808Request, XtreamRequest originalRequest) {
        this.invokeListeners(lifecycleListener -> lifecycleListener.afterRequestDecode(exchange, jt808Request, originalRequest));
    }

    @Override
    public void afterSubPackageMerged(XtreamExchange exchange, Jt808Request mergedRequest) {
        this.invokeListeners(lifecycleListener -> lifecycleListener.afterSubPackageMerged(exchange, mergedRequest));
    }

    @Override
    public void beforeResponseSend(XtreamRequest request, ByteBuf response) {
        this.invokeListeners(lifecycleListener -> lifecycleListener.beforeResponseSend(request, response));
    }

    @Override
    public void beforeCommandSend(ByteBuf command) {
        this.invokeListeners(lifecycleListener -> lifecycleListener.beforeCommandSend(command));
    }
}