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

package io.github.hylexus.xtream.codec.ext.jt1078.spec;


import java.util.Optional;

public enum Jt1078SubPackageIdentifier {
    ATOMIC((byte) 0b0000, "原子包,不可被拆分"),
    FIRST((byte) 0b0001, "分包处理时的第一个包"),
    LAST((byte) 0b0010, "分包处理时的最后一个包"),
    MIDDLE((byte) 0b0011, "分包处理时的中间包"),
    ;
    private final byte value;
    private final String desc;

    Jt1078SubPackageIdentifier(byte value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public byte value() {
        return value;
    }

    public String desc() {
        return desc;
    }

    public static Optional<Jt1078SubPackageIdentifier> of(byte value) {
        return switch (value & 0x0F) {
            case 0b0000 -> Optional.of(ATOMIC);
            case 0b0001 -> Optional.of(FIRST);
            case 0b0010 -> Optional.of(LAST);
            case 0b0011 -> Optional.of(MIDDLE);
            default -> Optional.empty();
        };
    }
}
