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

package io.github.hylexus.xtream.codec.core;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public interface BeanMetadataRegistryAware {

    void setBeanMetadataRegistry(BeanMetadataRegistry registry);

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated(since = "0.5.0-rc.3", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "1.0.0")
    default void setBeanMetadataRegistry(@Nullable Integer version, BeanMetadataRegistry registry) {
        this.setBeanMetadataRegistry(registry);
    }

}
