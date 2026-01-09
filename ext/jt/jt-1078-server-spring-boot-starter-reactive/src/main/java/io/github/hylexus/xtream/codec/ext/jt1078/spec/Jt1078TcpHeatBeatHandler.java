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

package io.github.hylexus.xtream.codec.ext.jt1078.spec;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionEventListener;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;


/**
 * UDP 类型的客户端 由 {@link io.github.hylexus.xtream.codec.server.reactive.spec.impl.AbstractXtreamSessionManager AbstractXtreamSessionManager} 中的定时器处理
 * <p>
 * TCP 类型的客户端 由当前类处理
 *
 * @author hylexus
 * @see XtreamSessionManager#closeSessionById(String, XtreamSessionEventListener.SessionCloseReason)
 */
@Slf4j
@ChannelHandler.Sharable
public class Jt1078TcpHeatBeatHandler extends ChannelInboundHandlerAdapter {

    private final Jt1078SessionManager sessionManager;

    public Jt1078TcpHeatBeatHandler(Jt1078SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!(evt instanceof IdleStateEvent)) {
            super.userEventTriggered(ctx, evt);
            return;
        }

        this.doWithSessionManager(manager -> {
            boolean closed = false;
            try {
                final String sessionId = manager.sessionIdGenerator().generateTcpSessionId(ctx.channel());
                closed = manager.closeSessionById(sessionId, XtreamSessionEventListener.DefaultSessionCloseReason.EXPIRED);
            } finally {
                if (!closed) {
                    closeAndIgnoreException(ctx, "Close channel because of [idle state event]: {}");
                }
            }
        });
    }

    @Override
    public void channelInactive(@Nonnull ChannelHandlerContext ctx) {
        if (log.isDebugEnabled()) {
            log.warn("channelInactive, address={} ", ctx.channel().remoteAddress());
        }

        this.doWithSessionManager(manager -> {
            boolean closed = false;
            try {
                final String sessionId = manager.sessionIdGenerator().generateTcpSessionId(ctx.channel());
                closed = manager.closeSessionById(sessionId, XtreamSessionEventListener.DefaultSessionCloseReason.CLOSED_BY_CLIENT);
            } finally {
                if (!closed) {
                    closeAndIgnoreException(ctx, "Close channel because of [channelInactive]: {}");
                }
            }
        });
    }

    protected void doWithSessionManager(Consumer<XtreamSessionManager<Jt1078Session>> consumer) {
        if (this.sessionManager != null) {
            consumer.accept(this.sessionManager);
        }
    }

    // 之所以有下面这个再次关闭 channel 的代码, 是因为上面的 sessionManager.closeSessionById() 可能无法真正关闭对应的 channel
    // 比如: 即便是客户端建立了连接，但是客户端没有发送任何消息的情况下, sessionManager 里是不会有对应的session的；
    // @see https://github.com/hylexus/jt-framework/issues/66
    private static void closeAndIgnoreException(ChannelHandlerContext ctx, String message) {
        try {
            log.info(message, ctx.channel());
            var ignored = ctx.channel().close();
        } catch (Exception ignored) {
            // ignored
        }
    }
}
