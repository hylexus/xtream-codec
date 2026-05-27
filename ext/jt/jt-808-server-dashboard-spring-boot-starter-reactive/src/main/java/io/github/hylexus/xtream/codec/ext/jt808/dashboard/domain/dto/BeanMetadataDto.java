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
import org.jspecify.annotations.Nullable;

public class BeanMetadataDto extends PageableDto {
    private @Nullable String className;
    private @Nullable Integer version;
    private @Nullable String dataType;

    public @Nullable String getClassName() {
        return className;
    }

    public BeanMetadataDto setClassName(@Nullable String className) {
        this.className = className;
        return this;
    }

    public @Nullable Integer getVersion() {
        return version;
    }

    public BeanMetadataDto setVersion(@Nullable Integer version) {
        this.version = version;
        return this;
    }

    public @Nullable String getDataType() {
        return dataType;
    }

    public BeanMetadataDto setDataType(@Nullable String dataType) {
        this.dataType = dataType;
        return this;
    }
}
