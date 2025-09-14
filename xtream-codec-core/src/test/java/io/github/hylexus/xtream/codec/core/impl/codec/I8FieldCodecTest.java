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
import org.junit.jupiter.api.Test;

class I8FieldCodecTest extends BaseFieldCodecTest {

    @Test
    void test() {
        codec(
                XtreamField.DEFAULT_VERSION, new I8FiledTestEntity()
                        .setB1((byte) -127)
                        .setB2((byte) -128)
                        .setS1((short) -127)
                        .setS2((short) -128)
                        .setI1(-127)
                        .setI2(-128)
                        .setL1(-127)
                        .setL2(-128L),
                (s, t) -> {
                    Assertions.assertNotSame(s, t);
                    Assertions.assertEquals(s.b1, t.b1);
                    Assertions.assertEquals(s.b2, t.b2);
                    Assertions.assertEquals(s.s1, t.s1);
                    Assertions.assertEquals(s.s2, t.s2);
                    Assertions.assertEquals(s.i1, t.i1);
                    Assertions.assertEquals(s.i2, t.i2);
                    Assertions.assertEquals(s.l1, t.l1);
                    Assertions.assertEquals(s.l2, t.l2);
                }
        );
    }

    @Data
    @Accessors(chain = true)
    public static class I8FiledTestEntity {
        @Preset.RustStyle.i8
        private byte b1;

        @Preset.RustStyle.i8
        private Byte b2;

        @Preset.RustStyle.i8
        private short s1;

        @Preset.RustStyle.i8
        private Short s2;

        @Preset.RustStyle.i8
        private int i1;

        @Preset.RustStyle.i8
        private Integer i2;

        @Preset.RustStyle.i8
        private long l1;

        @Preset.RustStyle.i8
        private Long l2;
    }
}
