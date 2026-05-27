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

package io.github.hylexus.xtream.codec.common.utils;

import io.github.classgraph.ClassInfo;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author hylexus
 */
public interface XtreamClassScanner {

    enum ScanMode {
        CLASS_ANNOTATION,
        FIELD_ANNOTATION,
        METHOD_ANNOTATION,
    }

    default Set<Class<?>> scan(String[] basePackages, Set<ScanMode> scanMode, Class<? extends Annotation> annotationClass) {
        return this.scan(basePackages, scanMode, annotationClass, classInfo -> true);
    }

    Set<Class<?>> scan(String[] basePackages, Set<ScanMode> scanMode, Class<? extends Annotation> annotationClass, Predicate<ClassInfo> predicate);
}
