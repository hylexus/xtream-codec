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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.values;

/**
 * @author hylexus
 * @see io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.mapper.adapter.Jt808VersionHandlerAdapter
 */
public enum Jt808Version {
    V2011(-1),
    V2013(0),
    V2019(1),
    UNKNOWN(-2),
    ;
    private final int value;

    Jt808Version(int value) {
        this.value = value;
    }

    public static Jt808Version fromValue(Integer result) {
        return switch (result) {
            case -1 -> V2011;
            case 0 -> V2013;
            case 1 -> V2019;
            case -2 -> UNKNOWN;
            default -> throw new IllegalArgumentException("Invalid Jt808Version value: " + result);
        };
    }

    public int getValue() {
        return value;
    }
}
