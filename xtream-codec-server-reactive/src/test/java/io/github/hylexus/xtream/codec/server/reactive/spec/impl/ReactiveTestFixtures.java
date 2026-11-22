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

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamResponse;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.net.InetAddress;
import java.net.InetSocketAddress;

final class ReactiveTestFixtures {
    static final InetSocketAddress ADDRESS = new InetSocketAddress(InetAddress.getLoopbackAddress(), 9000);

    private ReactiveTestFixtures() {
    }

    static XtreamExchange exchange() {
        return exchange(new DefaultXtreamResponse(ByteBufAllocator.DEFAULT, null, XtreamInbound.Type.TCP, ADDRESS));
    }

    static XtreamExchange exchange(XtreamResponse response) {
        final var request = new DefaultXtreamRequest(7, "request", ByteBufAllocator.DEFAULT, null, response.type(),
                Unpooled.EMPTY_BUFFER, null, response.remoteAddress());
        final var session = new DefaultXtreamSession("session", response.type(), response.outbound(), response.remoteAddress(), null);
        return new DefaultXtreamExchange(session, request, response);
    }
}
