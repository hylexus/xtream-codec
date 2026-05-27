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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.event;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestHeader;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.netty.buffer.ByteBuf;

import java.time.LocalDateTime;

/**
 * @author hylexus
 */
public class Jt808EventPayloads {

    /**
     * 收到 JT808 请求
     */
    public record Jt808ReceiveEvent(
            // 同一个网络包确保唯一
            String requestId,
            // 同一个请求确保唯一
            String traceId,
            // 收到请求的时间
            LocalDateTime receiveTime,
            // 协议类型
            XtreamRequest.Type netType,
            // 终端手机号
            String terminalId,
            // 消息流水号
            int flowId,
            // 消息 ID
            int messageId,
            // 消息体属性 WORD(16)
            int messageBodyProperty,
            // 未转义前的报文
            String rawHexString,
            // 下面都是冗余属性
            // 协议版本号
            Jt808ProtocolVersion version,
            // 收否是子包
            boolean isSubPackage,
            // 消息体长度
            int messageBodyLength,
            // 总包数
            int totalPackage,
            // 当前包序号
            int currentPackage) {

        public static Jt808ReceiveEvent fromRequest(ByteBuf rawPayload, Jt808Request request, LocalDateTime receiveTime) {
            final Jt808RequestHeader header = request.header();
            int totalPackage = 1;
            int currentPackage = 1;
            if (header.messageBodyProps().hasSubPackage()) {
                totalPackage = header.subPackage().totalSubPackageCount();
                currentPackage = header.subPackage().currentPackageNo();
            }
            return new Jt808ReceiveEvent(
                    request.requestId(),
                    request.traceId(),
                    receiveTime,
                    request.type(),
                    header.terminalId(),
                    header.flowId(),
                    header.messageId(),
                    header.messageBodyProps().intValue(),
                    "7e" + FormatUtils.toHexString(rawPayload) + "7e",
                    header.version(),
                    header.messageBodyProps().hasSubPackage(),
                    header.messageBodyLength(),
                    totalPackage,
                    currentPackage
            );
        }
    }

    /**
     * 回复 JT808 响应
     */
    public record Jt808SendEvent(
            String requestId,
            String traceId,
            XtreamRequest.Type netType,
            // 终端手机号
            String terminalId,
            String hexString,
            LocalDateTime sentAt) {

        public static Jt808SendEvent of(Jt808Request request, ByteBuf response, LocalDateTime sentAt) {
            return new Jt808SendEvent(
                    request.requestId(),
                    request.traceId(),
                    request.type(),
                    request.header().terminalId(),
                    FormatUtils.toHexString(response),
                    sentAt
            );
        }
    }

}
