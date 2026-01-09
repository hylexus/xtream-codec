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

import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808AttachmentSessionManager;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionManager;
import io.github.hylexus.xtream.codec.ext.jt808.spec.XtreamTcpHeatBeatHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.TcpSessionIdleStateCheckerProps;
import io.netty.handler.timeout.IdleStateHandler;
import org.jspecify.annotations.Nullable;
import reactor.netty.Connection;

import java.util.concurrent.TimeUnit;

import static io.github.hylexus.xtream.codec.ext.jt808.utils.JtProtocolConstant.BEAN_NAME_CHANNEL_INBOUND_IDLE_STATE_HANDLER;
import static io.github.hylexus.xtream.codec.ext.jt808.utils.JtProtocolConstant.BEAN_NAME_CHANNEL_INBOUND_IDLE_STATE_HANDLER_CALLBACK;

public final class BuiltinConfigurationUtils {
    private BuiltinConfigurationUtils() {
    }

    public static void addIdleStateHandler(TcpSessionIdleStateCheckerProps props, @Nullable Jt808SessionManager sessionManager, @Nullable Jt808AttachmentSessionManager attachmentSessionManager, Connection connection) {
        connection.addHandlerLast(
                BEAN_NAME_CHANNEL_INBOUND_IDLE_STATE_HANDLER,
                new IdleStateHandler(
                        props.getReaderIdleTime().toMillis(),
                        props.getWriterIdleTime().toMillis(),
                        props.getAllIdleTime().toMillis(),
                        TimeUnit.MILLISECONDS
                )
        );
        connection.addHandlerLast(
                BEAN_NAME_CHANNEL_INBOUND_IDLE_STATE_HANDLER_CALLBACK,
                new XtreamTcpHeatBeatHandler(sessionManager, attachmentSessionManager)
        );
    }

}
