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

package io.github.hylexus.xtream.codec.common.utils.scannerfixtures;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 独立包内的扫描夹具，区分类、字段、方法和多处同时标注的候选。
 *
 * @author Codex (AI)
 */
public final class ScannerFixtures {
    private ScannerFixtures() {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
    public @interface Marker {
    }

    @Marker
    public static class ClassAnnotated {
    }

    public static class FieldAnnotated {
        @Marker
        public int value;
    }

    public static class MethodAnnotated {
        @Marker
        public void handle() {
        }
    }

    @Marker
    public static class MultipleAnnotations {
        @Marker
        public int value;

        @Marker
        public void handle() {
        }
    }

    public static class Unannotated {
    }
}
