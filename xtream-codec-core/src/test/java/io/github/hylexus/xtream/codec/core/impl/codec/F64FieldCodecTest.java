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

package io.github.hylexus.xtream.codec.core.impl.codec;

import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class F64FieldCodecTest extends BaseFieldCodecTest {

    @ParameterizedTest
    @CsvSource({
            "0",
            "1",
            "-1",
            "4.9E-324",
            "1.7976931348623157E308",
            "-1.7976931348623157E308",
            "Infinity",
            "-Infinity",
            "NaN"
    })
    void testBigEndianBoundaryValues(double boundary) {
        codec(XtreamField.ALL_VERSION, new F64BeFiledTestEntity()
                        .setD1(boundary)
                        .setD2(boundary),
                (s, t) -> {
                    Assertions.assertNotSame(s, t);
                    assertDoubleEquals(s.d1, t.d1);
                    assertDoubleEquals(s.d2, t.d2);
                }
        );
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "1",
            "-1",
            "4.9E-324",
            "1.7976931348623157E308",
            "-1.7976931348623157E308",
            "Infinity",
            "-Infinity",
            "NaN"
    })
    void testLittleEndianBoundaryValues(double boundary) {
        codec(XtreamField.ALL_VERSION, new F64LeFiledTestEntity()
                        .setD1(boundary)
                        .setD2(boundary),
                (s, t) -> {
                    Assertions.assertNotSame(s, t);
                    assertDoubleEquals(s.d1, t.d1);
                    assertDoubleEquals(s.d2, t.d2);
                }
        );
    }

    private static void assertDoubleEquals(double expected, double actual) {
        Assertions.assertEquals(Double.doubleToLongBits(expected), Double.doubleToLongBits(actual));
    }

    @Data
    @Accessors(chain = true)
    public static class F64BeFiledTestEntity {
        @Preset.RustStyle.f64
        private double d1;
        @Preset.RustStyle.f64
        private Double d2;
    }

    @Data
    @Accessors(chain = true)
    public static class F64LeFiledTestEntity {
        @Preset.RustStyle.f64_le
        private double d1;
        @Preset.RustStyle.f64_le
        private Double d2;
    }
}
