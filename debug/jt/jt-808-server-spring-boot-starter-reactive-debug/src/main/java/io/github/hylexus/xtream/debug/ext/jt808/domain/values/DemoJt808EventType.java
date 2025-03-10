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

package io.github.hylexus.xtream.debug.ext.jt808.domain.values;

import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEvent;

/**
 * @author hylexus
 */
public enum DemoJt808EventType implements XtreamEvent.XtreamEventType {
    RECEIVE_PACKAGE(100, "请求"),
    MERGE_PACKAGE(101, "合并请求"),
    SEND_PACKAGE(102, "响应"),
    ;

    private final int code;
    private final String description;

    DemoJt808EventType(int code, String description) {
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
}
