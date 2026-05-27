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

package io.github.hylexus.xtream.codec.ext.jt808.codec;

import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.netty.buffer.ByteBuf;
import reactor.netty.NettyInbound;

/**
 * JT808 请求生命周期监听器。
 * <p>
 * 这个组件的目的仅仅是将 请求处理的关键节点 通知给用户。不是用来修改请求或响应数据的。
 * <p>
 * 如果有修改请求或响应的需求，请使用 {@link io.github.hylexus.xtream.codec.server.reactive.spec.XtreamFilter} 实现。
 *
 * <h3 style="color:red">注意:</h3>
 * <p>
 * 不要在这里做任何耗时/阻塞的操作。
 * <p>
 * 推荐的处理方式：发送消息给其他组件之后立即返回。
 *
 * @author hylexus
 * @implNote 不要在这里做任何耗时/阻塞的操作
 */
public interface Jt808RequestLifecycleListener {
    /**
     * 原始请求解码为 JTT/808 请求之后回调
     *
     * @param nettyInbound 请求上下文
     * @param rawPayload   原始请求
     * @param request      解码后的 JTT/808 请求
     * @implNote 不要有阻塞操作
     */
    default void afterRequestDecoded(NettyInbound nettyInbound, ByteBuf rawPayload, Jt808Request request) {
    }

    /**
     * 分包合并之后回调
     *
     * @param exchange      请求上下文
     * @param mergedRequest 由子包合并而来的请求
     * @implNote 不要有阻塞操作
     */
    default void afterSubPackageMerged(XtreamExchange exchange, Jt808Request mergedRequest) {
    }

    /**
     * 写数据给客户端之前回调
     *
     * @param request  请求
     * @param response 给客户端回复的数据
     * @implNote 不要有阻塞操作
     */
    default void beforeResponseSend(Jt808Request request, ByteBuf response) {
    }

    /**
     * (主动)下发指令之前回调
     *
     * @param session 会话信息
     * @param command 下发的指令
     * @see io.github.hylexus.xtream.codec.ext.jt808.extensions.Jt808CommandSender
     */
    default void beforeCommandSend(Jt808Session session, ByteBuf command) {
    }

    class NoopJt808RequestLifecycleListener implements Jt808RequestLifecycleListener {
    }
}
