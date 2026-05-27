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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.values;

import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.mapper.adapter.Jt808NetTypeHandlerAdapter;

/**
 * @author hylexus
 * @see Jt808NetTypeHandlerAdapter
 */
public enum Jt808NetType {
    TCP(0),
    UDP(1),
    UNKNOWN(-1);
    private final int value;

    Jt808NetType(int value) {
        this.value = value;
    }

    public static Jt808NetType fromValue(Integer result) {
        return switch (result) {
            case 0 -> TCP;
            case 1 -> UDP;
            case -1 -> UNKNOWN;
            default -> throw new IllegalArgumentException("Invalid value: " + result);
        };
    }

    public static Jt808NetType fromName(String name) {
        return switch (name.toUpperCase()) {
            case "TCP" -> TCP;
            case "UDP" -> UDP;
            case "UNKNOWN" -> UNKNOWN;
            default -> throw new IllegalArgumentException("Invalid value: " + name);
        };
    }

    public int getValue() {
        return value;
    }
}
