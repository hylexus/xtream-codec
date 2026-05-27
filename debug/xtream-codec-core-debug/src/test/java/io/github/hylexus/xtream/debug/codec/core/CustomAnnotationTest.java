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

package io.github.hylexus.xtream.debug.codec.core;

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.debug.codec.core.democustomannotation.CustomAnnotationDebugEntity01;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class CustomAnnotationTest {

    final EntityCodec entityCodec = EntityCodec.DEFAULT;

    @Test
    void testEncode() {

        final CustomAnnotationDebugEntity01 originalInstance = new CustomAnnotationDebugEntity01();
        originalInstance.setMajorVersion((short) 2);
        originalInstance.setBirthday(LocalDate.of(2024, 2, 10));

        // 编码/序列化
        final String hexString = encode(originalInstance);

        // 解码/反序列化
        final CustomAnnotationDebugEntity01 decodedInstance = decode(hexString);

        Assertions.assertEquals(originalInstance.getMajorVersion(), decodedInstance.getMajorVersion());
        Assertions.assertEquals(originalInstance.getBirthday(), decodedInstance.getBirthday());
    }

    private CustomAnnotationDebugEntity01 decode(String hexString) {
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer().writeBytes(XtreamBytes.decodeHex(hexString));
        try {
            return entityCodec.decode(CustomAnnotationDebugEntity01.class, buffer);
        } finally {
            buffer.release();
        }
    }

    private String encode(CustomAnnotationDebugEntity01 originalInstance) {
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        try {
            entityCodec.encode(originalInstance, buffer);
            return ByteBufUtil.hexDump(buffer);
        } finally {
            buffer.release();
        }
    }
}
