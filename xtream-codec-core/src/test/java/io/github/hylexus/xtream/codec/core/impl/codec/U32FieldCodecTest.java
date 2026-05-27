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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class U32FieldCodecTest extends BaseFieldCodecTest {

    @ParameterizedTest
    @CsvSource({
            "0",
            "1",
            "100",
            "2147483647",
            "2147483648",
            "4294967295"
    })
    void testBigEndianBoundaryValues(long boundary) {
        codec(XtreamField.ALL_VERSION, new U32BeFiledTestEntity()
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
            "0",
            "1",
            "100",
            "2147483647",
            "2147483648",
            "4294967295"
    })
    void testLittleEndianBoundaryValues(long boundary) {
        codec(XtreamField.ALL_VERSION, new U32LeFiledTestEntity()
                        .setL1(boundary)
                        .setL2(boundary),
                (s, t) -> {
                    Assertions.assertNotSame(s, t);
                    Assertions.assertEquals(s.l1, t.l1);
                    Assertions.assertEquals(s.l2, t.l2);
                }
        );
    }

    @Test
    void testBigEndianOutOfRangePositive() {
        final U32BeFiledTestEntity entity = new U32BeFiledTestEntity()
                .setL1(0L)
                .setL2(0L)
                .setOutOfRangeValue(4294967296L);
        codec(XtreamField.ALL_VERSION, entity,
                (s, t) -> Assertions.assertEquals(0L, t.outOfRangeValue)
        );
    }

    @Test
    void testBigEndianOutOfRangeNegative() {
        final U32BeFiledTestEntity entity = new U32BeFiledTestEntity()
                .setL1(0L)
                .setL2(0L)
                .setOutOfRangeValue(-1L);
        codec(XtreamField.ALL_VERSION, entity,
                (s, t) -> Assertions.assertEquals(4294967295L, t.outOfRangeValue)
        );
    }

    @Data
    @Accessors(chain = true)
    public static class U32BeFiledTestEntity {
        @Preset.RustStyle.u32
        private long l1;
        @Preset.RustStyle.u32
        private Long l2;
        @Preset.RustStyle.u32
        private long outOfRangeValue;
    }

    @Data
    @Accessors(chain = true)
    public static class U32LeFiledTestEntity {
        @Preset.RustStyle.u32_le
        private long l1;
        @Preset.RustStyle.u32_le
        private Long l2;
        @Preset.RustStyle.u32_le
        private long outOfRangeValue;
    }
}
