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

package io.github.hylexus.xtream.loadtest.jt808;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request.*;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.BuiltinMessage8100;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.ServerCommonReplyMessage;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.*;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Jt808RequestHandler
public class LoadtestMessageHandler {
    private final LoadtestProperties properties;
    private final LoadtestMetrics metrics;

    public LoadtestMessageHandler(LoadtestProperties properties, LoadtestMetrics metrics) {
        this.properties = properties;
        this.metrics = metrics;
    }

    @Jt808RequestHandlerMapping(messageIds = 0x0100, versions = Jt808ProtocolVersion.AUTO_DETECTION)
    @Jt808ResponseBody(messageId = 0x8100)
    public Mono<BuiltinMessage8100> register(Jt808Request request, Jt808Session session, @Jt808RequestBody BuiltinMessage0100AllInOne body) {
        session.verified(false);
        metrics.accepted(request.messageId(), 0);
        return Mono.just(new BuiltinMessage8100()
                .setClientFlowId(request.header().flowId())
                .setResult((short) 0)
                .setAuthCode(properties.authCode()));
    }

    @Jt808RequestHandlerMapping(messageIds = 0x0102, versions = Jt808ProtocolVersion.AUTO_DETECTION)
    @Jt808ResponseBody(messageId = 0x8001)
    public Mono<ServerCommonReplyMessage> authenticate(Jt808Request request, Jt808Session session, @Jt808RequestBody BuiltinMessage0102AllInOne body) {
        final boolean accepted = properties.authCode().equals(body.getAuthenticationCode());
        session.verified(accepted);
        return reply(request, accepted ? (byte) 0 : (byte) 1, 0);
    }

    @Jt808RequestHandlerMapping(messageIds = 0x0002, versions = Jt808ProtocolVersion.AUTO_DETECTION)
    @Jt808ResponseBody(messageId = 0x8001)
    public Mono<ServerCommonReplyMessage> heartbeat(Jt808Request request, Jt808Session session, @Jt808RequestBody BuiltinMessage0002 body) {
        return reply(request, session.verified() ? (byte) 0 : (byte) 1, 0);
    }

    @Jt808RequestHandlerMapping(messageIds = 0x0200, versions = Jt808ProtocolVersion.AUTO_DETECTION)
    @Jt808ResponseBody(messageId = 0x8001)
    public Mono<ServerCommonReplyMessage> location(Jt808Request request, Jt808Session session, @Jt808RequestBody BuiltinMessage0200 body) {
        return reply(request, session.verified() ? (byte) 0 : (byte) 1, 1);
    }

    @Jt808RequestHandlerMapping(messageIds = 0x0704, versions = Jt808ProtocolVersion.AUTO_DETECTION)
    @Jt808ResponseBody(messageId = 0x8001)
    public Mono<ServerCommonReplyMessage> batchLocation(Jt808Request request, Jt808Session session, @Jt808RequestBody BuiltinMessage0704 body) {
        if (!session.verified()) {
            return reply(request, (byte) 1, 0);
        }
        if (body.getItemList() == null || body.getCount() != body.getItemList().size()
                || body.getType() < 0 || body.getType() > 1
                || body.getItemList().stream().anyMatch(item -> item.getLocationData() == null)) {
            return reply(request, (byte) 2, 0);
        }
        return reply(request, (byte) 0, body.getItemList().size());
    }

    private Mono<ServerCommonReplyMessage> reply(Jt808Request request, byte result, int locationRecords) {
        if (result == 0) {
            metrics.accepted(request.messageId(), locationRecords);
        } else {
            metrics.refused(request.messageId());
        }
        return Mono.just(ServerCommonReplyMessage.of(request, result));
    }

}
