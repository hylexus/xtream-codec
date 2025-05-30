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

package io.github.hylexus.xtream.codec.ext.jt1078.utils;

import io.github.hylexus.xtream.codec.ext.jt1078.extensions.handler.Jt1078ServerUdpHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp.UdpXtreamHandlerAdapterBuilder;
import io.netty.buffer.ByteBufAllocator;

public class Jt1078ServerUdpHandlerAdapterBuilder extends UdpXtreamHandlerAdapterBuilder {

    public Jt1078ServerUdpHandlerAdapterBuilder(ByteBufAllocator allocator) {
        super(allocator);
    }

    @Override
    public Jt1078ServerUdpHandlerAdapter build() {
        final XtreamHandler exceptionHandlingHandler = createRequestHandler();
        return new Jt1078ServerUdpHandlerAdapter(super.byteBufAllocator, super.xtreamExchangeCreator, exceptionHandlingHandler);
    }

}
