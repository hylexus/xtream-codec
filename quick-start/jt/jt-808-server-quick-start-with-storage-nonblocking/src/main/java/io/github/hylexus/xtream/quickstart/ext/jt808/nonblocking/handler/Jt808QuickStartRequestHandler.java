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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.handler;

import io.github.hylexus.xtream.codec.ext.jt808.boot.condition.ConditionalOnJt808Server;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request.*;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.BuiltinMessage8100;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.ServerCommonReplyMessage;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.Jt808CommandSender;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestBody;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestHandler;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestHandlerMapping;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @author hylexus
 */
@Component
@Jt808RequestHandler
@ConditionalOnJt808Server(serverType = ConditionalOnJt808Server.ServerType.INSTRUCTION_SERVER, protocolType = ConditionalOnJt808Server.ProtocolType.ANY)
public class Jt808QuickStartRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(Jt808QuickStartRequestHandler.class);
    private final Jt808CommandSender jt808CommandSender;

    public Jt808QuickStartRequestHandler(Jt808CommandSender jt808CommandSender) {
        this.jt808CommandSender = jt808CommandSender;
    }

    /**
     * 终端心跳
     */
    @Jt808RequestHandlerMapping(messageIds = 0x0002)
    @Jt808ResponseBody(messageId = 0x8001)
    public Mono<ServerCommonReplyMessage> processMessage0002(Jt808Request request, @Jt808RequestBody BuiltinMessage0002 requestBody) {
        log.info("receive message [0x0002-{}]: {}", request.header().version(), requestBody);
        final ServerCommonReplyMessage responseBody = ServerCommonReplyMessage.success(request);
        return Mono.just(responseBody);
    }

    /**
     * 终端通用应答
     */
    @Jt808RequestHandlerMapping(messageIds = 0x0001)
    public Mono<Void> processMessage0001(Jt808Request request, @Jt808RequestBody BuiltinMessage0001 requestBody) {
        log.info("receive message [0x0001-{}]: {}", request.header().version(), requestBody);
        final Jt808CommandSender.Jt808CommandKey commandKey = Jt808CommandSender.Jt808CommandKey.of(
                request.terminalId(),
                requestBody.getServerMessageId(),
                requestBody.getServerFlowId()
        );
        // 可能有下发的指令等待回复，这里写入回复信息
        // FIXME: 这里只是个示例 看你情况修改
        this.jt808CommandSender.setClientResponse(commandKey, requestBody);
        return Mono.empty();
    }

    @Jt808RequestHandlerMapping(messageIds = 0x0104)
    public Mono<ServerCommonReplyMessage> processMessage0104(Jt808Request request, @Jt808RequestBody BuiltinMessage0104Sample2 requestBody) {
        log.info("receive message [0x0104-{}]: {}", request.header().version(), requestBody);
        final Jt808CommandSender.Jt808CommandKey commandKey = Jt808CommandSender.Jt808CommandKey.of(
                request.terminalId(),
                request.messageId(),
                requestBody.getFlowId()
        );
        // FIXME: 这里只是个示例 看你情况修改
        this.jt808CommandSender.setClientResponse(commandKey, requestBody);
        return Mono.just(ServerCommonReplyMessage.success(request));
    }

    @Jt808RequestHandlerMapping(messageIds = 0x0100, versions = Jt808ProtocolVersion.AUTO_DETECTION)
    @Jt808ResponseBody(messageId = 0x8100)
    public Mono<BuiltinMessage8100> processMessage0x0100(Jt808Request request, @Jt808RequestBody BuiltinMessage0100AllInOne requestBody) {
        log.info("receive message [0x0100-{}]: {}", request.header().version(), requestBody);
        log.info("{}", Thread.currentThread());
        final BuiltinMessage8100 builtinMessage8100 = new BuiltinMessage8100()
                .setClientFlowId(request.header().flowId())
                .setResult((short) 0)
                .setAuthCode("auth-code-" + request.header().version());

        return Mono.just(builtinMessage8100);
    }

    /**
     * 终端鉴权(V2019)
     */
    @Jt808RequestHandlerMapping(messageIds = 0x0102, versions = Jt808ProtocolVersion.AUTO_DETECTION)
    @Jt808ResponseBody(messageId = 0x8001)
    public Mono<ServerCommonReplyMessage> processMessage0102(Jt808Request request, @Jt808RequestBody BuiltinMessage0102AllInOne requestBody) {
        log.info("receive message [0x0102-{}]: {}", request.header().version(), requestBody);
        final ServerCommonReplyMessage responseBody = ServerCommonReplyMessage.success(request);
        return Mono.just(responseBody);
    }

}
