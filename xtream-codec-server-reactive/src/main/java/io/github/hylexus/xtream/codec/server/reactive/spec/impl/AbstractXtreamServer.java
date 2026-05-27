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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl;

import io.github.hylexus.xtream.codec.server.reactive.spec.NettyServerCustomizer;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamServer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.DisposableChannel;
import reactor.netty.transport.Transport;
import reactor.netty.transport.TransportConfig;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author hylexus
 */
@SuppressWarnings("checkstyle:ClassTypeParameterName")
public abstract class AbstractXtreamServer<
        T extends Transport<T, TC>,
        TC extends TransportConfig,
        CUS extends NettyServerCustomizer<T, TC>
        >
        implements XtreamServer {

    private static final Logger log = LoggerFactory.getLogger(AbstractXtreamServer.class);
    protected volatile @Nullable DisposableChannel disposableServer;
    protected final String name;

    protected AbstractXtreamServer(String name) {
        this.name = name;
    }

    @Override
    public void start() {
        try {
            this.disposableServer = this.initServer();
        } catch (Throwable e) {
            throw new IllegalStateException("Cannot start netty server", e);
        }

        this.doStart();

        log.info("{}({}) listening on {}({})", this.getClass().getSimpleName(), this.name, requireNonNull(this.disposableServer).address(), this.getServerType());
    }

    private void doStart() {
        final DisposableChannel disposable = requireNonNull(this.disposableServer);
        final String threadName = this.getClass().getSimpleName() + ":" + disposable.address();
        final Thread thread = new Thread(() -> {
            // ...
            disposable.onDispose().block();
        }, threadName);
        thread.setDaemon(false);
        thread.setContextClassLoader(this.getClass().getClassLoader());
        thread.start();
    }

    @Override
    public void stop() {
        final DisposableChannel server = this.disposableServer;
        if (server != null) {
            server.disposeNow();
        }
        this.disposableServer = null;
    }

    protected abstract DisposableChannel initServer();

    protected abstract List<CUS> getCustomizers();
}
