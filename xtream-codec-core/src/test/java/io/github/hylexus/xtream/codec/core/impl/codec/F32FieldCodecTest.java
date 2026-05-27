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

package io.github.hylexus.xtream.codec.core.impl.codec;

import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class F32FieldCodecTest extends BaseFieldCodecTest {

    @ParameterizedTest
    @CsvSource({
            "0",
            "1",
            "-1",
            "1.4E-45",
            "3.4028235E38",
            "-3.4028235E38",
            "Infinity",
            "-Infinity",
            "NaN"
    })
    void testBigEndianBoundaryValues(float boundary) {
        codec(XtreamField.ALL_VERSION, new F32BeFiledTestEntity()
                        .setF1(boundary)
                        .setF2(boundary),
                (s, t) -> {
                    Assertions.assertNotSame(s, t);
                    assertFloatEquals(s.f1, t.f1);
                    assertFloatEquals(s.f2, t.f2);
                }
        );
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "1",
            "-1",
            "1.4E-45",
            "3.4028235E38",
            "-3.4028235E38",
            "Infinity",
            "-Infinity",
            "NaN"
    })
    void testLittleEndianBoundaryValues(float boundary) {
        codec(XtreamField.ALL_VERSION, new F32LeFiledTestEntity()
                        .setF1(boundary)
                        .setF2(boundary),
                (s, t) -> {
                    Assertions.assertNotSame(s, t);
                    assertFloatEquals(s.f1, t.f1);
                    assertFloatEquals(s.f2, t.f2);
                }
        );
    }

    private static void assertFloatEquals(float expected, float actual) {
        Assertions.assertEquals(Float.floatToIntBits(expected), Float.floatToIntBits(actual));
    }

    @Data
    @Accessors(chain = true)
    public static class F32BeFiledTestEntity {
        @Preset.RustStyle.f32
        private float f1;
        @Preset.RustStyle.f32
        private Float f2;
    }

    @Data
    @Accessors(chain = true)
    public static class F32LeFiledTestEntity {
        @Preset.RustStyle.f32_le
        private float f1;
        @Preset.RustStyle.f32_le
        private Float f2;
    }
}
