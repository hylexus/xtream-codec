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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response;

import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestEntity;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestHeader;

/**
 * Alias for {@link BuiltinMessage8001}
 * <p>
 * 平台通用应答 0x8001
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8001, desc = "BuiltinMessage8001的别名")
public class ServerCommonReplyMessage extends BuiltinMessage8001 {

    public static ServerCommonReplyMessage success(Jt808RequestEntity<?> requestEntity) {
        final Jt808RequestHeader header = requestEntity.getHeader();
        return of(header.messageId(), header.flowId(), (byte) 0);
    }

    public static ServerCommonReplyMessage success(Jt808Request request) {
        final Jt808RequestHeader header = request.header();
        return of(header.messageId(), header.flowId(), (byte) 0);
    }

    public static ServerCommonReplyMessage success(int clientFlowId, int clientMessageId) {
        return of(clientMessageId, clientFlowId, (byte) 0);
    }

    public static ServerCommonReplyMessage of(Jt808Request request, byte result) {
        return of(request.header().messageId(), request.header().flowId(), result);
    }

    public static ServerCommonReplyMessage of(Jt808RequestEntity<?> requestEntity, byte result) {
        return of(requestEntity.getHeader().messageId(), requestEntity.getHeader().flowId(), result);
    }

    public static ServerCommonReplyMessage of(int clientMessageId, int clientFlowId, byte result) {
        final ServerCommonReplyMessage message = new ServerCommonReplyMessage();
        message.setClientMessageId(clientMessageId);
        message.setClientFlowId(clientFlowId);
        message.setResult(result);
        return message;
    }

}
