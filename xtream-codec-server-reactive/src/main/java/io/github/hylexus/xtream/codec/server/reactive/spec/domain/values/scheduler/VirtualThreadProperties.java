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

package io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.scheduler;

import java.util.StringJoiner;

public class VirtualThreadProperties {
    /**
     * @see Thread.Builder.OfVirtual#name(String, long)
     */
    private String prefix = "x-vt";

    /**
     * @see Thread.Builder.OfVirtual#name(String, long)
     */
    private long start = 0;

    /**
     * @see java.lang.Thread.Builder.OfVirtual#inheritInheritableThreadLocals(boolean)
     */
    private boolean inheritInheritableThreadLocals = true;

    public String getPrefix() {
        return prefix;
    }

    public VirtualThreadProperties setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public long getStart() {
        return start;
    }

    public VirtualThreadProperties setStart(long start) {
        this.start = start;
        return this;
    }

    public boolean isInheritInheritableThreadLocals() {
        return inheritInheritableThreadLocals;
    }

    public VirtualThreadProperties setInheritInheritableThreadLocals(boolean inheritInheritableThreadLocals) {
        this.inheritInheritableThreadLocals = inheritInheritableThreadLocals;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", VirtualThreadProperties.class.getSimpleName() + "[", "]")
                .add("prefix='" + prefix + "'")
                .add("start=" + start)
                .add("inheritInheritableThreadLocals=" + inheritInheritableThreadLocals)
                .toString();
    }

}
