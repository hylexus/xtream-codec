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

package io.github.hylexus.xtream.quickstart.ext.jt808.withdashboard.handler;

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
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @author hylexus
 */
@Component
@Jt808RequestHandler
public class Jt808QuickStartRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(Jt808QuickStartRequestHandler.class);
    private final Jt808SessionManager jt808SessionManager;
    private final Jt808CommandSender commandSender;

    public Jt808QuickStartRequestHandler(Jt808SessionManager jt808SessionManager, Jt808CommandSender commandSender) {
        this.jt808SessionManager = jt808SessionManager;
        this.commandSender = commandSender;
    }

    /**
     * 终端心跳
     */
    @Jt808RequestHandlerMapping(messageIds = 0x0002)
    @Jt808ResponseBody(messageId = 0x8001)
    public Mono<ServerCommonReplyMessage> processMessage0002(Jt808Request request, @Jt808RequestBody BuiltinMessage0002 requestBody) {
        log.info("receive message [0x0002]: {}", requestBody);
        final ServerCommonReplyMessage responseBody = ServerCommonReplyMessage.success(request);
        return Mono.just(responseBody);
    }

    /**
     * 终端通用应答
     */
    @Jt808RequestHandlerMapping(messageIds = 0x0001)
    public Mono<Void> processMessage0001(Jt808Request request, @Jt808RequestBody BuiltinMessage0001 requestBody) {
        log.info("receive message [0x0001]: {}", requestBody);
        final Jt808CommandSender.Jt808CommandKey commandKey = Jt808CommandSender.Jt808CommandKey.of(
                request.terminalId(),
                requestBody.getServerMessageId(),
                requestBody.getServerFlowId()
        );
        // 可能有下发的指令等待回复，这里写入回复信息
        // FIXME: 这里只是个示例 看你情况修改
        commandSender.setClientResponse(commandKey, requestBody);
        return Mono.empty();
    }

    /**
     * 终端注册(V2019)
     */
    @Jt808RequestHandlerMapping(messageIds = 0x0100, versions = Jt808ProtocolVersion.VERSION_2019)
    @Jt808ResponseBody(messageId = 0x8100)
    public Mono<BuiltinMessage8100> processMessage0x0100V2019(Jt808Request request, @Jt808RequestBody BuiltinMessage0100V2019 requestBody) {
        log.info("receive message [0x0100-v2019]: {}", requestBody);
        log.info("{}", Thread.currentThread());
        final BuiltinMessage8100 builtinMessage8100 = new BuiltinMessage8100()
                .setClientFlowId(request.header().flowId())
                .setResult((short) 0)
                .setAuthCode("auth-code-2019");

        return Mono.just(builtinMessage8100);
    }

    /**
     * 终端注册(V2013)
     */
    @Jt808RequestHandlerMapping(messageIds = 0x0100, versions = Jt808ProtocolVersion.VERSION_2013)
    @Jt808ResponseBody(messageId = 0x8100)
    public Mono<BuiltinMessage8100> processMessage0x0100V2013(Jt808Request request, @Jt808RequestBody BuiltinMessage0100V2013 requestBody) {
        log.info("receive message [0x0100-v2013]: {}", requestBody);
        final BuiltinMessage8100 builtinMessage8100 = new BuiltinMessage8100()
                .setClientFlowId(request.header().flowId())
                .setResult((short) 0)
                .setAuthCode("auth-code-2013");

        return Mono.just(builtinMessage8100);
    }

    /**
     * 终端注册(V2013)
     */
    @Jt808RequestHandlerMapping(messageIds = 0x0100, versions = Jt808ProtocolVersion.VERSION_2011)
    @Jt808ResponseBody(messageId = 0x8100)
    public Mono<BuiltinMessage8100> processMessage0x0100V2011(Jt808Request request, @Jt808RequestBody BuiltinMessage0100V2011 requestBody) {
        log.info("receive message [0x0100-v2011]: {}", requestBody);
        final BuiltinMessage8100 builtinMessage8100 = new BuiltinMessage8100()
                .setClientFlowId(request.header().flowId())
                .setResult((short) 0)
                .setAuthCode("auth-code-v2011");

        return Mono.just(builtinMessage8100);
    }

    /**
     * 终端鉴权(V2019)
     */
    @Jt808RequestHandlerMapping(messageIds = 0x0102, versions = Jt808ProtocolVersion.VERSION_2019)
    @Jt808ResponseBody(messageId = 0x8001)
    public Mono<ServerCommonReplyMessage> processMessage0102V2019(Jt808Request request, @Jt808RequestBody BuiltinMessage0102V2019 requestBody) {
        log.info("receive message [0x0100-v2019]: {}", requestBody);
        final ServerCommonReplyMessage responseBody = ServerCommonReplyMessage.success(request);
        return Mono.just(responseBody);
    }

    /**
     * 终端鉴权(V2011 or V2013)
     */
    @Jt808RequestHandlerMapping(messageIds = 0x0102, versions = {Jt808ProtocolVersion.VERSION_2011, Jt808ProtocolVersion.VERSION_2013})
    @Jt808ResponseBody(messageId = 0x8001)
    public Mono<ServerCommonReplyMessage> processMessage0102(Jt808Request request, @Jt808RequestBody BuiltinMessage0102V2013 requestBody) {
        log.info("receive message [0x0100-(v2011 or v2013)]: {}", requestBody);
        final ServerCommonReplyMessage responseBody = ServerCommonReplyMessage.success(request);
        return Mono.just(responseBody);
    }


    /**
     * 位置上报
     * <p>
     * 7e02004086010000000001893094655200E4000000000000000101D907F2073D336C000000000000211124114808010400000026030200003001153101002504000000001404000000011504000000FA160400000000170200001803000000EA10FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF02020000EF0400000000F31B017118000000000000000000000000000000000000000000000000567e
     */
    @Jt808RequestHandlerMapping(messageIds = 0x0200, versions = Jt808ProtocolVersion.AUTO_DETECTION)
    @Jt808ResponseBody(messageId = 0x8001, maxPackageSize = 1000)
    public Mono<ServerCommonReplyMessage> processMessage0200V2019(
            Jt808Session session,
            Jt808Request request,
            @Jt808RequestBody BuiltinMessage0200 body) {
        log.info("{}-0x0200: {}", request.header().version(), body);
        return this.processLocationMessage(session, body).map(result -> {
            // ...
            return ServerCommonReplyMessage.of(request, result);
        });
    }

    /**
     * 0:成功/确认; 1:失败; 2:消息有误; 3:不支持; 4:报警处理确认
     */
    private Mono<Byte> processLocationMessage(Jt808Session session, BuiltinMessage0200 body) {
        // 业务逻辑...
        return Mono.just((byte) 0);
    }

}
