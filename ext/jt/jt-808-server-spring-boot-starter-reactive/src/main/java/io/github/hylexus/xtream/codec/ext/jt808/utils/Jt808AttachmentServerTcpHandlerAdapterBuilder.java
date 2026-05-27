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

package io.github.hylexus.xtream.codec.ext.jt808.utils;

import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestDecoder;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808AttachmentServerTcpHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DispatcherXtreamHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.AbstractXtreamHandlerAdapterBuilderTcp;
import io.netty.buffer.ByteBufAllocator;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * @author hylexus
 */
public class Jt808AttachmentServerTcpHandlerAdapterBuilder extends AbstractXtreamHandlerAdapterBuilderTcp<Jt808AttachmentServerTcpHandlerAdapterBuilder> {
    protected @Nullable XtreamHandler attachementdispatcherXtreamHandler;
    protected @Nullable Jt808RequestDecoder requestDecoder;
    protected @Nullable Jt808RequestLifecycleListener requestLifecycleListener;

    public Jt808AttachmentServerTcpHandlerAdapterBuilder(ByteBufAllocator allocator) {
        super(allocator);
    }

    public Jt808AttachmentServerTcpHandlerAdapterBuilder requestDecoder(Jt808RequestDecoder requestDecoder) {
        this.requestDecoder = requestDecoder;
        return this;
    }

    public Jt808AttachmentServerTcpHandlerAdapterBuilder requestLifecycleListener(Jt808RequestLifecycleListener requestLifecycleListener) {
        this.requestLifecycleListener = requestLifecycleListener;
        return this;
    }

    public Jt808AttachmentServerTcpHandlerAdapterBuilder setAttachmentDispatcherXtreamHandler(DispatcherXtreamHandler dispatcherXtreamHandler) {
        this.attachementdispatcherXtreamHandler = dispatcherXtreamHandler;
        return this;
    }

    @Override
    public Jt808AttachmentServerTcpHandlerAdapter build() {
        final XtreamHandler exceptionHandlingHandler = createRequestHandler();
        return new Jt808AttachmentServerTcpHandlerAdapter(
                super.byteBufAllocator,
                Objects.requireNonNull(super.sessionManager),
                exceptionHandlingHandler,
                Objects.requireNonNull(this.requestDecoder),
                Objects.requireNonNull(this.requestLifecycleListener),
                Objects.requireNonNull(this.attachementdispatcherXtreamHandler)
        );
    }
}
