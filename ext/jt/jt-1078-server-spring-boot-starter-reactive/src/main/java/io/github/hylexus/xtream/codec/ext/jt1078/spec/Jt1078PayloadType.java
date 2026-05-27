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


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.hylexus.xtream.codec.common.utils.Numbers;
import io.github.hylexus.xtream.codec.ext.jt1078.exception.Jt1078MessageDecodeException;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum Jt1078PayloadType {

    G_721((byte) 1, "音频"),
    G_722((byte) 2, "音频"),
    G_723((byte) 3, "音频"),
    G_728((byte) 4, "音频"),
    G_729((byte) 5, "音频"),
    G_711A((byte) 6, "音频"),
    G_711U((byte) 7, "音频"),
    G_726((byte) 8, "音频"),
    G_729A((byte) 9, "音频"),
    DVI4_3((byte) 10, "音频"),
    DVI4_4((byte) 11, "音频"),
    DVI4_8K((byte) 12, "音频"),
    DVI4_16K((byte) 13, "音频"),
    LPC((byte) 14, "音频"),
    S16BE_STEREO((byte) 15, "音频"),
    S16BE_MONO((byte) 16, "音频"),
    MPEGAUDIO((byte) 17, "音频"),
    LPCM((byte) 18, "音频"),
    AAC((byte) 19, "音频"),
    WMA9STD((byte) 20, "音频"),
    HEAAC((byte) 21, "音频"),
    PCM_VOICE((byte) 22, "音频"),
    PCM_AUDIO((byte) 23, "音频"),
    AACLC((byte) 24, "音频"),
    MP3((byte) 25, "音频"),
    ADPCMA((byte) 26, "音频"),
    MP4AUDIO((byte) 27, "音频"),
    AMR((byte) 28, "音频"),
    // [29,90] 保留
    TRANSPARENT_TRANSMISSION((byte) 91, "透传"),
    // [92,97] 保留
    H264((byte) 98, "视频"),
    H265((byte) 99, "视频"),
    AVS((byte) 100, "视频"),
    SVAC((byte) 101, "视频"),
    // [102,110] 保留
    // [111,127] 自定义
    ;

    private final byte value;
    private final String desc;

    Jt1078PayloadType(byte value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private static final Map<Byte, Jt1078PayloadType> VALUE_TO_ENUM = new HashMap<>();
    private static final Map<String, Jt1078PayloadType> NAME_TO_ENUM = new HashMap<>();

    static {
        for (Jt1078PayloadType value : values()) {
            VALUE_TO_ENUM.put(value.value, value);
            NAME_TO_ENUM.put(value.name(), value);
        }
    }

    public static Optional<Jt1078PayloadType> fromByte(byte pt) {
        return Optional.ofNullable(VALUE_TO_ENUM.get(pt));
    }

    public static Jt1078PayloadType createOrError(byte pt) {
        return Jt1078PayloadType.fromByte(pt)
                .orElseThrow(() -> new Jt1078MessageDecodeException("Unknown payLoadType " + pt));
    }

    @JsonValue
    public Map<String, Object> jsonValue() {
        return Map.of("name", this.name(), "value", this.value);
    }

    @JsonCreator
    public static @Nullable Jt1078PayloadType jsonCreator(@Nullable Object o) {
        return switch (o) {
            case Number n -> Jt1078PayloadType.fromByte(n.byteValue()).orElse(null);
            case String s -> {
                final String trimmed = s.trim();
                if (trimmed.isEmpty()) {
                    yield null;
                }
                yield Numbers.parseInteger(trimmed)
                        .flatMap(n -> Jt1078PayloadType.fromByte(n.byteValue()))
                        .orElseGet(() -> NAME_TO_ENUM.get(trimmed.toUpperCase()));
            }
            case null, default -> null;
        };
    }

    public byte value() {
        return this.value;
    }

    public String desc() {
        return this.desc;
    }

    public boolean isAudio() {
        return isAudio(this.value());
    }

    public static boolean isAudio(byte payloadType) {
        return payloadType <= 28 && payloadType >= 1;
    }

    public boolean isVideo() {
        return isVideo(this.value());
    }

    public static boolean isVideo(byte payloadType) {
        return payloadType <= 101 && payloadType >= 92;
    }

}
