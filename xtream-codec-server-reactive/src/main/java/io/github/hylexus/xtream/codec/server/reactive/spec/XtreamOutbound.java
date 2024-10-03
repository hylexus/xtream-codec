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

package io.github.hylexus.xtream.codec.server.reactive.spec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;

import java.net.InetSocketAddress;

/**
 * @author hylexus
 */
public interface XtreamOutbound {

    ByteBufAllocator bufferFactory();

    XtreamRequest.Type type();

    NettyOutbound outbound();

    InetSocketAddress remoteAddress();

    Mono<Void> writeWith(Publisher<? extends ByteBuf> body);

    Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends ByteBuf>> publisher);

}