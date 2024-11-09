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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.events;

/**
 * @author hylexus
 */
public class Jt808DashboardEventPayloads {

    public interface HasTerminalId {

        String terminalId();

        default boolean match(String input) {
            return terminalId().contains(input.toLowerCase());
        }
    }

    /**
     * 收到 JT808 请求
     */
    public record ReceiveRequest(
            String requestId,
            String traceId,
            String terminalId,
            String version,
            boolean isSubPackage,
            int messageId,
            String rawHexString,
            String escapedHexString) implements HasTerminalId {
    }

    /**
     * JT808 合并请求
     */
    public record MergeRequest(
            String requestId,
            String traceId,
            String terminalId,
            String version,
            boolean isSubPackage,
            int messageId,
            String mergedHexString) implements HasTerminalId {
    }

    /**
     * 回复 JT808 响应
     */
    public record SendResponse(
            String requestId,
            String traceId,
            String terminalId,
            int messageId,
            String hexString) implements HasTerminalId {
    }
}