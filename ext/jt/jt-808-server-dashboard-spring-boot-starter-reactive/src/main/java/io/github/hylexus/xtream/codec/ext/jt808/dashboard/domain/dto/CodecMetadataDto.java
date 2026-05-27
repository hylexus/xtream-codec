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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto;

import io.github.hylexus.xtream.codec.base.web.domain.dto.PageableDto;
import io.github.hylexus.xtream.codec.core.annotation.NumberEndian;
import io.github.hylexus.xtream.codec.core.annotation.NumberSignedness;
import org.jspecify.annotations.Nullable;

public class CodecMetadataDto extends PageableDto {
    private @Nullable String key;
    private @Nullable String className;
    private @Nullable String charset;
    private @Nullable Boolean isBuiltin;
    private @Nullable NumberEndian endian;
    private @Nullable NumberSignedness signedness;

    public @Nullable String getKey() {
        return key;
    }

    public CodecMetadataDto setKey(@Nullable String key) {
        this.key = key;
        return this;
    }

    public @Nullable String getClassName() {
        return className;
    }

    public CodecMetadataDto setClassName(@Nullable String className) {
        this.className = className;
        return this;
    }

    public @Nullable String getCharset() {
        return charset;
    }

    public CodecMetadataDto setCharset(@Nullable String charset) {
        this.charset = charset;
        return this;
    }

    public @Nullable Boolean getBuiltin() {
        return isBuiltin;
    }

    public CodecMetadataDto setBuiltin(@Nullable Boolean builtin) {
        isBuiltin = builtin;
        return this;
    }

    public @Nullable NumberEndian getEndian() {
        return endian;
    }

    public CodecMetadataDto setEndian(@Nullable NumberEndian endian) {
        this.endian = endian;
        return this;
    }

    public @Nullable NumberSignedness getSignedness() {
        return signedness;
    }

    public CodecMetadataDto setSignedness(@Nullable NumberSignedness signedness) {
        this.signedness = signedness;
        return this;
    }
}
