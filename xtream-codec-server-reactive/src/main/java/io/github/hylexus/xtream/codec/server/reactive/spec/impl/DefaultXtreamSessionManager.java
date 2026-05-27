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
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamResponse;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSession;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionIdGenerator;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.UdpSessionIdleStateCheckerProps;
import org.jspecify.annotations.Nullable;

public class DefaultXtreamSessionManager extends AbstractXtreamSessionManager<XtreamSession> {

    public DefaultXtreamSessionManager(boolean udpSessionIdleStateCheckerEnabled, @Nullable UdpSessionIdleStateCheckerProps udpSessionIdleStateCheckerProps, XtreamSessionIdGenerator idGenerator) {
        super(udpSessionIdleStateCheckerEnabled, udpSessionIdleStateCheckerProps, idGenerator);
    }

    @Override
    protected XtreamSession doCreateSession(String sessionId, XtreamExchange exchange) {
        final XtreamResponse response = exchange.response();
        return new DefaultXtreamSession(
                sessionId,
                response.type(),
                response.outbound(),
                response.remoteAddress(),
                this
        );
    }

}
