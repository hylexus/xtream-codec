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

package io.github.hylexus.xtream.codec.server.reactive.spec.event;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 标记接口
 *
 * @author hylexus
 */
public interface XtreamEvent {

    XtreamEventType type();

    Object payload();

    interface XtreamEventType {
        int code();

        String description();

        XtreamEventType ALL = DefaultXtreamEventType.ALL;
        XtreamEventType AFTER_SESSION_CREATED = DefaultXtreamEventType.AFTER_SESSION_CREATED;
        XtreamEventType BEFORE_SESSION_CLOSED = DefaultXtreamEventType.BEFORE_SESSION_CLOSED;
        XtreamEventType AFTER_REQUEST_RECEIVED = DefaultXtreamEventType.AFTER_REQUEST_RECEIVED;
        XtreamEventType BEFORE_RESPONSE_SEND = DefaultXtreamEventType.BEFORE_RESPONSE_SEND;
        XtreamEventType BEFORE_COMMAND_SEND = DefaultXtreamEventType.BEFORE_COMMAND_SEND;
    }

    enum DefaultXtreamEventType implements XtreamEventType {
        ALL(-1, "所有事件"),
        AFTER_SESSION_CREATED(-99, "Session创建"),
        BEFORE_SESSION_CLOSED(-98, "Session关闭"),
        AFTER_REQUEST_RECEIVED(-100, "收到请求"),
        BEFORE_RESPONSE_SEND(-102, "发送响应"),
        BEFORE_COMMAND_SEND(-103, "指令下发"),
        ;
        private final int code;
        private final String description;

        DefaultXtreamEventType(int code, String description) {
            this.code = code;
            this.description = description;
        }

        @Override
        public int code() {
            return this.code;
        }

        @Override
        public String description() {
            return this.description;
        }

        private static final Map<Integer, DefaultXtreamEventType> CACHES;

        static {
            CACHES = Arrays.stream(values()).collect(Collectors.toMap(DefaultXtreamEventType::code, it -> it));
        }

        public static Optional<XtreamEventType> of(Integer code) {
            return Optional.ofNullable(CACHES.get(code));
        }
    }
}
