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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class DecodeMessageDto {

    @NotNull(message = "hexString is null")
    @Size(min = 1, message = "hexString is empty")
    private List<String> hexString;

    @NotNull(message = "bodyClass is null")
    @NotEmpty(message = "bodyClass is empty")
    private String bodyClass;

    public List<@Nullable String> getHexString() {
        return hexString;
    }

    public DecodeMessageDto setHexString(List<String> hexString) {
        this.hexString = hexString;
        return this;
    }

    public String getBodyClass() {
        return bodyClass;
    }

    public DecodeMessageDto setBodyClass(String bodyClass) {
        this.bodyClass = bodyClass;
        return this;
    }
}
