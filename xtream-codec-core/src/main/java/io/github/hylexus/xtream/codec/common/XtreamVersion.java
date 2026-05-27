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

package io.github.hylexus.xtream.codec.common;


import jakarta.annotation.Nullable;

/**
 * 当前类是从 `org.springframework.core.SpringVersion` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `org.springframework.core.SpringVersion`.
 *
 * @author hylexus
 */
public final class XtreamVersion {

    private XtreamVersion() {
    }

    public static String getVersion(String def) {
        final String version = getVersion();
        return (version != null ? version : def);
    }

    @Nullable
    public static String getVersion() {
        final Package pkg = XtreamVersion.class.getPackage();
        return (pkg != null ? pkg.getImplementationVersion() : null);
    }

}
