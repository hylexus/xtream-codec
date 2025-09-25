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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.values;

import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.mapper.adapter.Jt808VersionHandlerAdapter;

/**
 * @author hylexus
 * @see Jt808VersionHandlerAdapter
 */
public enum Jt808Version {
    V2011(2011),
    V2013(2013),
    V2019(2019),
    UNKNOWN(-2),
    ;
    private final int value;

    Jt808Version(int value) {
        this.value = value;
    }

    public static Jt808Version fromValue(Integer result) {
        return switch (result) {
            case 2011 -> V2011;
            case 2013 -> V2013;
            case 2019 -> V2019;
            case -2 -> UNKNOWN;
            default -> throw new IllegalArgumentException("Invalid Jt808Version value: " + result);
        };
    }

    public int getValue() {
        return value;
    }
}
