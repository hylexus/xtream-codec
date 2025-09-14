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

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Assertions;

import java.util.function.BiConsumer;

class BaseFieldCodecTest {
    protected final EntityCodec entityCodec = EntityCodec.DEFAULT;
    protected final ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;

    <T> void codec(int version, T instance, BiConsumer<T, T> assertion) {
        final ByteBuf buffer = allocator.buffer();
        try {
            this.entityCodec.encode(version, instance, buffer);
            System.out.println(FormatUtils.toHexString(buffer));
            @SuppressWarnings("unchecked") final Class<T> cls = (Class<T>) instance.getClass();
            final T decode = this.entityCodec.decode(version, cls, buffer);
            assertion.accept(instance, decode);
        } finally {
            XtreamBytes.releaseBuf(buffer);
            Assertions.assertEquals(0, buffer.refCnt());
        }
    }
}
