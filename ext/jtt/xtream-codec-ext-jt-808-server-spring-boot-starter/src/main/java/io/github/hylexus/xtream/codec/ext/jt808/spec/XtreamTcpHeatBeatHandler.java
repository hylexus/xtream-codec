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

package io.github.hylexus.xtream.codec.ext.jt808.spec;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionEventListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;


/**
 * UDP 类型的客户端 由 {@link io.github.hylexus.xtream.codec.server.reactive.spec.impl.AbstractXtreamSessionManager AbstractXtreamSessionManager} 中的定时器处理
 * <p>
 * TCP 类型的客户端 由当前类处理
 *
 * @author hylexus
 * @see io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionManager#closeSessionById(String, XtreamSessionEventListener.SessionCloseReason)
 */
@Slf4j
@ChannelHandler.Sharable
public class XtreamTcpHeatBeatHandler extends ChannelInboundHandlerAdapter {

    @Nullable
    private final Jt808SessionManager sessionManager;
    @Nullable
    private final Jt808AttachmentSessionManager attachmentSessionManager;

    public XtreamTcpHeatBeatHandler(@Nullable Jt808SessionManager sessionManager, @Nullable Jt808AttachmentSessionManager attachmentSessionManager) {
        if (sessionManager == null && attachmentSessionManager == null) {
            throw new IllegalArgumentException("No session manager found");
        }
        this.sessionManager = sessionManager;
        this.attachmentSessionManager = attachmentSessionManager;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!(evt instanceof IdleStateEvent)) {
            super.userEventTriggered(ctx, evt);
            return;
        }
        boolean closed = false;
        try {
            if (this.sessionManager != null) {
                final String sessionId = this.sessionManager.sessionIdGenerator().generateTcpSessionId(ctx.channel());
                closed = this.sessionManager.closeSessionById(sessionId, XtreamSessionEventListener.DefaultSessionCloseReason.EXPIRED);
            } else if (this.attachmentSessionManager != null) {
                final String sessionId = this.attachmentSessionManager.sessionIdGenerator().generateTcpSessionId(ctx.channel());
                closed = this.attachmentSessionManager.closeSessionById(sessionId, XtreamSessionEventListener.DefaultSessionCloseReason.EXPIRED);
            } else {
                // 不应该执行到这里
                throw new IllegalStateException("No session manager found");
            }
        } catch (Exception exception) {
            log.error("Error occurred while closing session", exception);
        }

        // 之所以有下面这个再次关闭 channel 的代码, 是因为上面的 sessionManager.closeSessionById() 可能无法真正关闭对应的 channel
        // 比如: 即便是客户端建立了连接，但是客户端没有发送任何消息的情况下, sessionManager 里是不会有对应的session的；
        // @see https://github.com/hylexus/jt-framework/issues/66
        if (!closed) {
            try {
                log.info("Close channel because of idle state event: {}", ctx.channel());
                ctx.channel().close();
            } catch (Exception ignored) {
                // ignored
            }
        }
    }
}
