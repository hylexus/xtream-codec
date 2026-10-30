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

import io.github.hylexus.xtream.codec.common.utils.XtreamClassScanner.ScanMode;
import io.github.hylexus.xtream.codec.common.utils.scannerfixtures.ScannerFixtures;
import io.github.hylexus.xtream.codec.common.utils.scannerfixtures.ScannerFixtures.*;
import io.github.hylexus.xtream.codec.common.utils.scannerfixtures.child.ChildFixture;
import io.github.hylexus.xtream.codec.common.utils.scannerfixturesother.OutsideFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static io.github.hylexus.xtream.codec.common.utils.XtreamClassScanner.ScanMode.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 类扫描的注解模式、过滤、去重及包范围测试。
 *
 * @author Codex (AI)
 */
class DefaultXtreamClassScannerTest {
    private static final String BASE_PACKAGE = ScannerFixtures.class.getPackageName();
    private static final Set<ScanMode> ALL_MODES = Set.of(CLASS_ANNOTATION, FIELD_ANNOTATION, METHOD_ANNOTATION);
    private final XtreamClassScanner scanner = new DefaultXtreamClassScanner();

    static Stream<Arguments> scanModes() {
        return Stream.of(
                Arguments.of(Set.of(CLASS_ANNOTATION),
                        Set.of(ClassAnnotated.class, MultipleAnnotations.class, ChildFixture.class)),
                Arguments.of(Set.of(FIELD_ANNOTATION),
                        Set.of(FieldAnnotated.class, MultipleAnnotations.class)),
                Arguments.of(Set.of(METHOD_ANNOTATION),
                        Set.of(MethodAnnotated.class, MultipleAnnotations.class)),
                Arguments.of(Set.of(CLASS_ANNOTATION, FIELD_ANNOTATION),
                        Set.of(ClassAnnotated.class, FieldAnnotated.class, MultipleAnnotations.class, ChildFixture.class)),
                Arguments.of(Set.of(CLASS_ANNOTATION, METHOD_ANNOTATION),
                        Set.of(ClassAnnotated.class, MethodAnnotated.class, MultipleAnnotations.class, ChildFixture.class)),
                Arguments.of(Set.of(FIELD_ANNOTATION, METHOD_ANNOTATION),
                        Set.of(FieldAnnotated.class, MethodAnnotated.class, MultipleAnnotations.class)),
                Arguments.of(ALL_MODES,
                        Set.of(ClassAnnotated.class, FieldAnnotated.class, MethodAnnotated.class, MultipleAnnotations.class, ChildFixture.class)));
    }

    @ParameterizedTest
    @MethodSource("scanModes")
    void scansOnlySelectedAnnotationLocationsAndDeduplicatesClasses(Set<ScanMode> modes, Set<Class<?>> expected) {
        final Set<Class<?>> result = scanner.scan(new String[]{BASE_PACKAGE}, modes, Marker.class);
        assertThat(result).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void appliesPredicateToEverySelectedAnnotationLocation() {
        final Set<Class<?>> result = scanner.scan(new String[]{BASE_PACKAGE}, ALL_MODES, Marker.class,
                classInfo -> !classInfo.getName().equals(MultipleAnnotations.class.getName()));
        assertThat(result).containsExactlyInAnyOrder(
                ClassAnnotated.class, FieldAnnotated.class, MethodAnnotated.class, ChildFixture.class);
    }

    @Test
    void returnsEmptyWhenPredicateRejectsAllCandidates() {
        assertThat(scanner.scan(new String[]{BASE_PACKAGE}, ALL_MODES, Marker.class, classInfo -> false)).isEmpty();
    }

    @Test
    void restrictsScanningToRequestedSubpackage() {
        final Set<Class<?>> result = scanner.scan(
                new String[]{ChildFixture.class.getPackageName()}, ALL_MODES, Marker.class);
        assertThat(result).containsExactly(ChildFixture.class);
    }

    @Test
    void mergesMultipleAndOverlappingPackagesWithoutDuplicates() {
        final Set<Class<?>> result = scanner.scan(new String[]{
                BASE_PACKAGE, ChildFixture.class.getPackageName(), OutsideFixture.class.getPackageName()
        }, ALL_MODES, Marker.class);
        assertThat(result).containsExactlyInAnyOrder(ClassAnnotated.class, FieldAnnotated.class, MethodAnnotated.class,
                MultipleAnnotations.class, ChildFixture.class, OutsideFixture.class);
    }

    @Test
    void excludesSimilarlyPrefixedPackageUnlessExplicitlyRequested() {
        assertThat(OutsideFixture.class.getPackageName()).startsWith(BASE_PACKAGE);
        assertThat(scanner.scan(new String[]{BASE_PACKAGE}, Set.of(CLASS_ANNOTATION), Marker.class))
                .containsExactlyInAnyOrder(ClassAnnotated.class, MultipleAnnotations.class, ChildFixture.class);
        assertThat(scanner.scan(new String[]{OutsideFixture.class.getPackageName()}, ALL_MODES, Marker.class))
                .containsExactly(OutsideFixture.class);
    }

    @Test
    void returnsEmptyForMissingPackage() {
        assertThat(scanner.scan(new String[]{BASE_PACKAGE + ".missing"}, ALL_MODES, Marker.class)).isEmpty();
    }

    @Test
    void returnsEmptyWhenSelectedLocationHasNoMatchingAnnotation() {
        assertThat(scanner.scan(new String[]{OutsideFixture.class.getPackageName()},
                Set.of(FIELD_ANNOTATION, METHOD_ANNOTATION), Marker.class)).isEmpty();
    }

    @Test
    void returnsEmptyWhenNoScanModeIsSelected() {
        assertThat(scanner.scan(new String[]{BASE_PACKAGE}, Set.of(), Marker.class)).isEmpty();
    }

    @Test
    void defaultPredicateMatchesExplicitlyAcceptingAllCandidates() {
        final String[] packages = {BASE_PACKAGE};
        assertThat(scanner.scan(packages, ALL_MODES, Marker.class))
                .containsExactlyInAnyOrderElementsOf(scanner.scan(packages, ALL_MODES, Marker.class, classInfo -> true));
    }
}
