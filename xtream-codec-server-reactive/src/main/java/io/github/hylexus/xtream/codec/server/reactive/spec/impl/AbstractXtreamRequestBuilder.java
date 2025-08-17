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

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

public abstract class AbstractXtreamRequestBuilder<B extends XtreamRequest.XtreamRequestBuilder, R extends XtreamRequest>
        implements XtreamRequest.XtreamRequestBuilder {

    protected final R delegateRequest;

    protected ByteBuf payload;
    protected InetSocketAddress remoteAddress;
    protected Channel channel;

    public AbstractXtreamRequestBuilder(R delegateRequest) {
        this.delegateRequest = delegateRequest;
        this.payload = delegateRequest.payload();
        this.channel = delegateRequest.underlyingChannel();
        this.remoteAddress = delegateRequest.remoteAddress();
    }

    @SuppressWarnings("unchecked")
    B self() {
        return (B) this;
    }

    @Override
    public B payload(ByteBuf payload, boolean autoRelease) {
        final ByteBuf old = this.payload;
        try {
            this.payload = payload;
            return self();
        } finally {
            if (autoRelease) {
                XtreamBytes.releaseBuf(old);
            }
        }
    }

    @Override
    public B remoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
        return self();
    }

    public abstract R build();

    protected DatagramPacket createDatagramPacket(ByteBuf payload) {
        return new DatagramPacket(
                payload != null ? payload : this.delegateRequest.payload(),
                null,
                this.remoteAddress != null ? this.remoteAddress : this.delegateRequest.remoteAddress()
        );
    }
}
