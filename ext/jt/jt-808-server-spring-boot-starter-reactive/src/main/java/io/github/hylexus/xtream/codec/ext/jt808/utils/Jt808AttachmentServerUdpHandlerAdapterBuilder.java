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

package io.github.hylexus.xtream.codec.ext.jt808.utils;

import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestDecoder;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808UdpDatagramPackageSplitter;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808AttachmentServerUdpHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamHandler;
import io.netty.buffer.ByteBufAllocator;

/**
 * @author hylexus
 */
public class Jt808AttachmentServerUdpHandlerAdapterBuilder extends Jt808InstructionServerUdpHandlerAdapterBuilder {
    protected XtreamHandler attachementdispatcherXtreamHandler;

    public Jt808AttachmentServerUdpHandlerAdapterBuilder(ByteBufAllocator allocator, Jt808UdpDatagramPackageSplitter udpDatagramPackageSplitter) {
        super(allocator, udpDatagramPackageSplitter);
    }

    public Jt808AttachmentServerUdpHandlerAdapterBuilder setAttachmentDispatcherHandler(XtreamHandler handler) {
        this.attachementdispatcherXtreamHandler = handler;
        return this;
    }

    public Jt808AttachmentServerUdpHandlerAdapterBuilder requestDecoder(Jt808RequestDecoder requestDecoder) {
        this.requestDecoder = requestDecoder;
        return this;
    }

    public Jt808AttachmentServerUdpHandlerAdapterBuilder requestLifecycleListener(Jt808RequestLifecycleListener requestLifecycleListener) {
        this.requestLifecycleListener = requestLifecycleListener;
        return this;
    }

    @Override
    public Jt808AttachmentServerUdpHandlerAdapter build() {
        final XtreamHandler exceptionHandlingHandler = createRequestHandler();
        return new Jt808AttachmentServerUdpHandlerAdapter(super.byteBufAllocator, super.sessionManager, exceptionHandlingHandler, this.udpDatagramPackageSplitter, this.attachementdispatcherXtreamHandler, this.requestDecoder, this.requestLifecycleListener);
    }

}
