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

class I64FieldCodecTest extends BaseFieldCodecTest {

    @ParameterizedTest
    @CsvSource({
            "-9223372036854775808",
            "-100",
            "-1",
            "0",
            "1",
            "100",
            "9223372036854775807"
    })
    void testBigEndianBoundaryValues(long boundary) {
        codec(XtreamField.ALL_VERSION, new I64BeFiledTestEntity()
                        .setL1(boundary)
                        .setL2(boundary),
                (s, t) -> {
                    Assertions.assertNotSame(s, t);
                    Assertions.assertEquals(s.l1, t.l1);
                    Assertions.assertEquals(s.l2, t.l2);
                }
        );
    }

    @ParameterizedTest
    @CsvSource({
            "-9223372036854775808",
            "-100",
            "-1",
            "0",
            "1",
            "100",
            "9223372036854775807"
    })
    void testLittleEndianBoundaryValues(long boundary) {
        codec(XtreamField.ALL_VERSION, new I64LeFiledTestEntity()
                        .setL1(boundary)
                        .setL2(boundary),
                (s, t) -> {
                    Assertions.assertNotSame(s, t);
                    Assertions.assertEquals(s.l1, t.l1);
                    Assertions.assertEquals(s.l2, t.l2);
                }
        );
    }

    @Data
    @Accessors(chain = true)
    public static class I64BeFiledTestEntity {
        @Preset.RustStyle.i64
        private long l1;
        @Preset.RustStyle.i64
        private Long l2;
    }

    @Data
    @Accessors(chain = true)
    public static class I64LeFiledTestEntity {
        @Preset.RustStyle.i64_le
        private long l1;
        @Preset.RustStyle.i64_le
        private Long l2;
    }
}
