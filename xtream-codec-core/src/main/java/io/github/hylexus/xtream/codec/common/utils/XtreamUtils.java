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

package io.github.hylexus.xtream.codec.common.utils;

import io.netty.buffer.ByteBufAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hylexus
 */
public class XtreamUtils {
    public static final ByteBufAllocator DEFAULT_BUFFER_FACTORY = ByteBufAllocator.DEFAULT;
    // private static final Logger log = LoggerFactory.getLogger(XtreamUtils.class);

    public static boolean hasElement(String str) {
        return str != null && !str.isEmpty();
    }

    public static String detectMainClassPackageName() {
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        final List<String> candidates = new ArrayList<>();
        for (StackTraceElement element : stackTraceElements) {
            if ("main".equals(element.getMethodName())) {
                final String className = element.getClassName();
                final int lastDotIndex = className.lastIndexOf('.');
                final String packageName = lastDotIndex > 0 ? className.substring(0, lastDotIndex) : "";
                candidates.add(packageName);
            }
        }
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Cannot determine Main class");
        }
        return candidates.getLast();
    }

    public static int setBitRange(int from, int length, int target, int offset) {
        return ((~(((1 << length) - 1) << offset)) & target)
                |
                (from << offset);
    }

}
