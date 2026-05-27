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

package io.github.hylexus.xtream.codec.core.impl.codec.pair;

import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.type.Pair;
import io.github.hylexus.xtream.codec.core.type.wrapper.DwordWrapper;
import io.github.hylexus.xtream.codec.core.type.wrapper.StringWrapperBcd;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.hylexus.xtream.codec.core.type.simple.DataFields.*;

class PairSequenceCodecsTest extends BaseEntityCodecTest {

    @Test
    void testDefaultVersion() {
        final PairDebugEntity01 entity = new PairDebugEntity01()
                .setId(1L)
                .setPairList(List.of(
                        new Pair(u16(1), gbkString("你是谁")),
                        new Pair(u16(2), utf8String("你是谁😂")),
                        new Pair(u16(3), new DwordWrapper(222L)),
                        new Pair(u16(4), new StringWrapperBcd("1122")),
                        new Pair(u16(5), new PairDebugEntity01.Item01(100L, "张三😄")),
                        new Pair(u16(6), hexString("332211"))
                ))
                .setDesc("......😁");

        doCodecTest(XtreamField.ALL_VERSION, entity, (source, hexString, decoded) -> {
            Assertions.assertEquals(source.getId(), decoded.getId());

        }, true);
    }

}
