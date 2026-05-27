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

package io.github.hylexus.xtream.codec.ext.jt808.extensions;

import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.codec.server.reactive.spec.InternalXtreamCommandSender;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSession;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * @author hylexus
 */
public interface Jt808CommandSender extends InternalXtreamCommandSender<Jt808Session> {

    Mono<Void> sendObject(String sessionId, Mono<Object> body);

    /**
     * @param sessionId {@link XtreamSession#id()}
     * @param body      要下发的指令
     * @param messageId 客户端对该条指令的回复消息的 ID
     * @see #setClientResponse(Jt808CommandKey, Object)
     */
    <R> Mono<R> sendObjectAndWaitingResponse(String sessionId, Mono<Object> body, int messageId);

    default <R> Mono<R> sendObjectAndWaitingResponseWithSessionId(String sessionId, Mono<Object> body, int messageId) {
        return this.sendObjectAndWaitingResponse(sessionId, body, messageId);
    }

    <R> Mono<R> sendObjectAndWaitingResponseWithTerminalId(String terminalId, Mono<Object> body, int messageId);

    /**
     * 设置客户端的回复结果
     *
     * @see #sendObjectAndWaitingResponse(String, Mono, int)
     */
    void setClientResponse(Jt808CommandKey commandKey, Object clientResponse);

    interface Jt808CommandKey {

        String terminalId();

        int responseMessageId();

        Optional<Integer> serverFlowId();

        @Override
        int hashCode();

        @Override
        boolean equals(Object obj);

        static Jt808CommandKey of(String terminalId, int responseMessageId) {
            return new DefaultJt808CommandKey(terminalId, responseMessageId);
        }

        static Jt808CommandKey of(String terminalId, int responseMessageId, Integer serverFlowId) {
            return new DefaultJt808CommandKey(terminalId, responseMessageId, serverFlowId);
        }

    }

    class DefaultJt808CommandKey implements Jt808CommandKey {
        private final String terminalId;
        private final int responseMessageId;
        private final @Nullable Integer serverFlowId;

        public DefaultJt808CommandKey(String terminalId, int responseMessageId) {
            this(terminalId, responseMessageId, null);
        }

        public DefaultJt808CommandKey(String terminalId, int responseMessageId, @Nullable Integer serverFlowId) {
            this.terminalId = terminalId;
            this.responseMessageId = responseMessageId;
            this.serverFlowId = serverFlowId;
        }

        @Override
        public String terminalId() {
            return this.terminalId;
        }

        @Override
        public int responseMessageId() {
            return this.responseMessageId;
        }

        @Override
        public Optional<Integer> serverFlowId() {
            return Optional.ofNullable(this.serverFlowId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Jt808CommandKey that)) {
                return false;
            }

            return this.responseMessageId == that.responseMessageId()
                   && this.terminalId.equals(that.terminalId())
                   && Optional.ofNullable(this.serverFlowId).equals(that.serverFlowId());
        }

        @Override
        public int hashCode() {
            int result = terminalId.hashCode();
            result = 31 * result + responseMessageId;
            result = 31 * result + Objects.hashCode(serverFlowId);
            return result;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", DefaultJt808CommandKey.class.getSimpleName() + "[", "]")
                    .add("terminalId='" + terminalId + "'")
                    .add("responseMessageId=" + responseMessageId)
                    .add("serverFlowId=" + serverFlowId)
                    .toString();
        }
    }

}
