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

class U16FieldCodecTest extends BaseFieldCodecTest {

    @ParameterizedTest
    @CsvSource({
        "0",
        "1",
        "32767",
        "32768",
        "65534",
        "65535"
    })
    void testBigEndianBoundaryValues(int boundary) {
        codec(XtreamField.ALL_VERSION, new U16BeFiledTestEntity()
                        .setI1(boundary)
                        .setI2(boundary)
                        .setL1(boundary)
                        .setL2((long) boundary),
                (s, t) -> {
                    Assertions.assertNotSame(s, t);
                    Assertions.assertEquals(s.i1, t.i1);
                    Assertions.assertEquals(s.i2, t.i2);
                    Assertions.assertEquals(s.l1, t.l1);
                    Assertions.assertEquals(s.l2, t.l2);
                }
        );
    }

    @ParameterizedTest
    @CsvSource({
        "0",
        "1",
        "32767",
        "32768",
        "65534",
        "65535"
    })
    void testLittleEndianBoundaryValues(int boundary) {
        codec(XtreamField.ALL_VERSION, new U16LeFiledTestEntity()
                        .setI1(boundary)
                        .setI2(boundary)
                        .setL1(boundary)
                        .setL2((long) boundary),
                (s, t) -> {
                    Assertions.assertNotSame(s, t);
                    Assertions.assertEquals(s.i1, t.i1);
                    Assertions.assertEquals(s.i2, t.i2);
                    Assertions.assertEquals(s.l1, t.l1);
                    Assertions.assertEquals(s.l2, t.l2);
                }
        );
    }

    @Test
    void testBigEndianOutOfRangePositive() {
        final U16BeFiledTestEntity entity = new U16BeFiledTestEntity()
                .setI1(0)
                .setI2(0)
                .setL1(0)
                .setL2(0L)
                .setOutOfRangeValue(65536);
        codec(XtreamField.ALL_VERSION, entity,
                (s, t) -> Assertions.assertEquals(0, t.outOfRangeValue)
        );
    }

    @Test
    void testBigEndianOutOfRangeNegative() {
        final U16BeFiledTestEntity entity = new U16BeFiledTestEntity()
                .setI1(0)
                .setI2(0)
                .setL1(0)
                .setL2(0L)
                .setOutOfRangeValue(-1);
        codec(XtreamField.ALL_VERSION, entity,
                (s, t) -> Assertions.assertEquals(65535, t.outOfRangeValue)
        );
    }

    @Data
    @Accessors(chain = true)
    public static class U16BeFiledTestEntity {
        @Preset.RustStyle.u16
        private int i1;
        @Preset.RustStyle.u16
        private Integer i2;
        @Preset.RustStyle.u16
        private long l1;
        @Preset.RustStyle.u16
        private Long l2;
        @Preset.RustStyle.u16
        private int outOfRangeValue;
    }

    @Data
    @Accessors(chain = true)
    public static class U16LeFiledTestEntity {
        @Preset.RustStyle.u16_le
        private int i1;
        @Preset.RustStyle.u16_le
        private Integer i2;
        @Preset.RustStyle.u16_le
        private long l1;
        @Preset.RustStyle.u16_le
        private Long l2;
        @Preset.RustStyle.u16_le
        private int outOfRangeValue;
    }
}
