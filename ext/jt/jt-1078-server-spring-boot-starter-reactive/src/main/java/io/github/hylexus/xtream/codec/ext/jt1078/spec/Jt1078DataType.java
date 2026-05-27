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

/**
 * @author hylexus
 */
public enum Jt1078DataType {
    VIDEO_I((byte) 0b0000, "视频I帧"),
    VIDEO_P((byte) 0b0001, "视频P帧"),
    VIDEO_B((byte) 0b0010, "视频B帧"),
    AUDIO((byte) 0b0011, "音频帧"),
    TRANSPARENT_TRANSMISSION((byte) 0b0100, "透传数据"),
    ;
    private final byte value;
    private final String desc;

    Jt1078DataType(byte value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public byte value() {
        return value;
    }

    public String desc() {
        return desc;
    }

    public static Optional<Jt1078DataType> of(byte value) {
        return switch (value & 0X0F) {
            case 0b0000 -> Optional.of(VIDEO_I);
            case 0b0001 -> Optional.of(VIDEO_P);
            case 0b0010 -> Optional.of(VIDEO_B);
            case 0b0011 -> Optional.of(AUDIO);
            case 0b0100 -> Optional.of(TRANSPARENT_TRANSMISSION);
            default -> Optional.empty();
        };
    }

}
