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

package io.github.hylexus.xtream.codec.ext.jt808.spec.impl;

import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ServerType;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionManager;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamResponse;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionIdGenerator;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.UdpSessionIdleStateCheckerProps;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.AbstractXtreamSessionManager;
import reactor.netty.Connection;

/**
 * @author hylexus
 */
public class DefaultJt808SessionManager extends AbstractXtreamSessionManager<Jt808Session>
        implements Jt808SessionManager {

    public DefaultJt808SessionManager(boolean udpSessionIdleStateCheckerEnabled, UdpSessionIdleStateCheckerProps instructionServerSessionIdleStateChecker, XtreamSessionIdGenerator idGenerator) {
        super(udpSessionIdleStateCheckerEnabled, instructionServerSessionIdleStateChecker, idGenerator);
    }

    @Override
    protected Jt808Session doCreateSession(String sessionId, XtreamExchange exchange) {
        final Jt808Request request = (Jt808Request) exchange.request();
        final XtreamResponse response = exchange.response();
        return new DefaultJt808Session(
                sessionId,
                Jt808ServerType.INSTRUCTION_SERVER,
                response.type(),
                request.header().version(),
                request.terminalId(),
                response.outbound(),
                response.remoteAddress(),
                this
        );
    }

    @Override
    // @SuppressWarnings("deprecation")
    protected void beforeConnectionClose(Connection connection, Jt808Session session) {
        // switch (session.type()) {
        //     case TCP -> connection.channel().attr(JtProtocolConstant.tcpSessionKey()).remove();
        //     case UDP -> connection.channel().attr(JtProtocolConstant.udpSessionKey(session.remoteAddress())).remove();
        //     default -> throw new IllegalArgumentException("Unsupported session type: " + session.type());
        // }
        // connection.channel().attr(JtProtocolConstant.NETTY_ATTR_KEY_SESSION).set(null);
    }

}
