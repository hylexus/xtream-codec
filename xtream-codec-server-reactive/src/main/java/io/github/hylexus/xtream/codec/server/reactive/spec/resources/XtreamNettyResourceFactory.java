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
import reactor.netty.transport.Transport;

public sealed interface XtreamNettyResourceFactory
        permits TcpXtreamNettyResourceFactory, UdpXtreamNettyResourceFactory, BaseXtreamNettyResourceFactory {

    LoopResources loopResources();

    /**
     * should prefer running on epoll, kqueue or similar instead of java NIO
     *
     * @see Transport#runOn(LoopResources, boolean)
     */
    boolean preferNative();

    /**
     * 在使用此工厂的服务停止后，释放由工厂管理的资源。
     * 默认实现为空操作，适用于由外部管理事件循环的工厂。
     *
     * @since 0.11.0
     */
    default void dispose() {
    }

    record LoopResourcesProperty(
            String prefix,
            int selectCount,
            int workerCount,
            boolean daemon,
            boolean colocate,
            boolean preferNative) {
    }
}
