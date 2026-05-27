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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionEventListener;
import org.springframework.util.StringUtils;

import java.time.Instant;

/**
 * @author hylexus
 */
public class Jt808DashboardEventPayloads {

    public interface HasTerminalId {

        String terminalId();

        default boolean match(String input) {
            final String terminalId = terminalId();
            return StringUtils.hasText(terminalId)
                   && terminalId.contains(input.toLowerCase());
        }
    }

    public interface HasEventTime {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
        Instant eventTime();
    }

    /**
     * Session 创建
     */
    public record SessionCreationInfo(
            String id,
            String terminalId,
            String version,
            String type,
            String remoteAddress,
            Instant eventTime) implements HasTerminalId, HasEventTime {
    }

    /**
     * Session 关闭
     */
    public record SessionClosingInfo(
            String id,
            String terminalId,
            String version,
            String type,
            String remoteAddress,
            XtreamSessionEventListener.SessionCloseReason reason,
            Instant eventTime) implements HasTerminalId, HasEventTime {
    }

    /**
     * 收到 JT808 请求
     */
    public record ReceivedRequestInfo(
            String requestId,
            String traceId,
            String terminalId,
            String version,
            boolean isSubPackage,
            int messageId,
            String messageDesc,
            String hexString,
            Instant eventTime) implements HasTerminalId, HasEventTime {
    }

    /**
     * JT808 合并请求
     */
    public record MergedRequestInfo(
            String requestId,
            String traceId,
            String terminalId,
            String version,
            boolean isSubPackage,
            int messageId,
            String messageDesc,
            String mergedHexString,
            Instant eventTime) implements HasTerminalId, HasEventTime {
    }

    /**
     * 回复 JT808 响应
     */
    public record ResponseInfo(
            String requestId,
            String traceId,
            String terminalId,
            int messageId,
            String messageDesc,
            String hexString,
            Instant eventTime) implements HasTerminalId, HasEventTime {
    }

    /**
     * 主动下发指令
     */
    public record CommandInfo(
            String sessionId,
            String terminalId,
            String version,
            String command,
            Instant eventTime) implements HasTerminalId, HasEventTime {
    }

}
