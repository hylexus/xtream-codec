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

package io.github.hylexus.xtream.codec.server.reactive.spec;

/**
 * @author hylexus
 * @see XtreamSessionManager#addListener(XtreamSessionEventListener)
 * @see XtreamSessionManager#closeSessionById(String, SessionCloseReason)
 */
public interface XtreamSessionEventListener {

    /**
     * {@code Session} 新建之后回调
     *
     * @param session 新创建的 {@code Session}
     * @apiNote 不应该做阻塞或者耗时的操作
     */
    default void afterSessionCreate(XtreamSession session) {
    }

    /**
     * {@code Session} 关闭之前回调
     *
     * @param session 准备关闭的 {@code Session}
     * @param reason  关闭原因
     * @apiNote 不应该做阻塞或者耗时的操作
     */
    default void beforeSessionClose(XtreamSession session, SessionCloseReason reason) {
    }

    interface SessionCloseReason {
        String getReason();
    }

    enum DefaultSessionCloseReason implements SessionCloseReason {
        EXPIRED("EXPIRED"),
        MANUAL_CLOSED("MANUAL_CLOSED"),
        ;
        private final String reason;

        DefaultSessionCloseReason(String reason) {
            this.reason = reason;
        }

        @Override
        public String getReason() {
            return this.reason;
        }
    }
}
