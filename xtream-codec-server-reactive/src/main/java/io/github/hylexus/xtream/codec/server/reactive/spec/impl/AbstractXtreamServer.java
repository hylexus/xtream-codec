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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl;

import io.github.hylexus.xtream.codec.server.reactive.spec.NettyServerCustomizer;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.DisposableChannel;
import reactor.netty.transport.Transport;
import reactor.netty.transport.TransportConfig;

import java.util.List;

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
    protected volatile DisposableChannel disposableServer;
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

        log.info("XtreamServer({}) listening on {}({})", this.name, this.disposableServer.address(), this.getServerType());
    }

    private void doStart() {
        final Thread thread = new Thread(() -> {
            // ...
            this.disposableServer.onDispose().block();
        }, "XtreamServer");
        thread.setDaemon(false);
        thread.setContextClassLoader(this.getClass().getClassLoader());
        thread.start();
    }

    @Override
    public void stop() {
        if (this.disposableServer != null) {
            this.disposableServer.disposeNow();
        }
        this.disposableServer = null;
    }

    protected abstract DisposableChannel initServer();

    protected abstract List<CUS> getCustomizers();
}
