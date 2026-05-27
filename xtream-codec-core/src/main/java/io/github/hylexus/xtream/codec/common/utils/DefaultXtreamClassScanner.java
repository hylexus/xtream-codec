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

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author hylexus
 */
public class DefaultXtreamClassScanner implements XtreamClassScanner {
    public DefaultXtreamClassScanner() {
    }

    @Override
    public Set<Class<?>> scan(String[] basePackages, Set<ScanMode> scanMode, Class<? extends Annotation> annotationClass, Predicate<ClassInfo> predicate) {
        final ClassGraph classGraph = new ClassGraph()
                .verbose(false)
                .enableAllInfo()
                .acceptPackages(basePackages);

        final Set<String> result = new HashSet<>();
        try (ScanResult scanResult = classGraph.scan()) {

            if (scanMode.contains(ScanMode.CLASS_ANNOTATION)) {
                for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(annotationClass)) {
                    if (predicate.test(classInfo)) {
                        result.add(classInfo.getName());
                    }
                }
            }

            if (scanMode.contains(ScanMode.FIELD_ANNOTATION)) {
                for (final ClassInfo classInfo : scanResult.getClassesWithFieldAnnotation(annotationClass)) {
                    if (predicate.test(classInfo)) {
                        result.add(classInfo.getName());
                    }
                }
            }

            if (scanMode.contains(ScanMode.METHOD_ANNOTATION)) {
                for (final ClassInfo classInfo : scanResult.getClassesWithMethodAnnotation(annotationClass)) {
                    if (predicate.test(classInfo)) {
                        result.add(classInfo.getName());
                    }
                }
            }

        }

        return result.stream().map(this::loadClass).collect(Collectors.toSet());
    }

    Class<?> loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
