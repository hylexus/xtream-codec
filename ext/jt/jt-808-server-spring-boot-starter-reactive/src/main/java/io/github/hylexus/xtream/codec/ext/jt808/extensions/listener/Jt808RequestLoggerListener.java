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

package io.github.hylexus.xtream.codec.ext.jt808.extensions.listener;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageDescriptionRegistry;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.NettyInbound;

public class Jt808RequestLoggerListener implements Jt808RequestLifecycleListener {

    private static final Logger log = LoggerFactory.getLogger(Jt808RequestLoggerListener.class);
    private final Jt808MessageDescriptionRegistry descriptionRegistry;

    public Jt808RequestLoggerListener(Jt808MessageDescriptionRegistry descriptionRegistry) {
        this.descriptionRegistry = descriptionRegistry;
    }

    @Override
    public void afterRequestDecoded(NettyInbound nettyInbound, ByteBuf rawPayload, Jt808Request request) {
        final int messageId = request.messageId();
        log.info("===> Receive [{}/0x{}({})] message: requestId = {}, traceId = {}, remoteAddr = {}, payload = 7e{}7e",
                request.type(),
                FormatUtils.toHexString(messageId, 4),
                this.descriptionRegistry.getDescription(messageId, "Unknown"),
                request.requestId(),
                request.traceId(),
                request.remoteAddress(),
                FormatUtils.toHexString(rawPayload)
        );
    }

    @Override
    public void beforeResponseSend(Jt808Request request, ByteBuf response) {
        log.info("<=== Send [{}] message: requestId = {}, traceId = {}, remoteAddr = {}, payload = {}",
                request.type(),
                request.requestId(),
                request.traceId(),
                request.remoteAddress(),
                FormatUtils.toHexString(response)
        );
    }

    @Override
    public void beforeCommandSend(Jt808Session session, ByteBuf command) {
        log.info("===> Send [{}] command: terminalId = {}, protocolVersion = {}, command = {}",
                session.type(),
                session.terminalId(),
                session.protocolVersion(),
                FormatUtils.toHexString(command)
        );
    }

}
