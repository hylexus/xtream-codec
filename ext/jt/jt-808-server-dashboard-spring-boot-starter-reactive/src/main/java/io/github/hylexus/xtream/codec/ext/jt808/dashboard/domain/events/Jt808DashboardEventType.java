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

import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author hylexus
 * @see io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEvent.DefaultXtreamEventType
 */
public enum Jt808DashboardEventType implements XtreamEvent.XtreamEventType {
    AFTER_SUB_REQUEST_MERGED(-101, "请求合并"),
    ;

    private final int code;
    private final String description;

    Jt808DashboardEventType(int code, String description) {
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

    private static final Map<Integer, Jt808DashboardEventType> CACHES;

    static {
        CACHES = Arrays.stream(values()).collect(Collectors.toMap(Jt808DashboardEventType::code, it -> it));
    }

    public static Optional<XtreamEvent.XtreamEventType> of(Integer code) {
        return Optional.ofNullable(CACHES.get(code));
    }
}
