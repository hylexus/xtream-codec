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

package io.github.hylexus.xtream.codec;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Assertions;

public class BaseEntityCodecTest {
    protected final EntityCodec entityCodec = EntityCodec.DEFAULT;
    protected final ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;

    @FunctionalInterface
    protected interface CodecAssertion<T> {
        void accept(T sourceEntity, String encodedHexString, T decodedEntity);
    }

    protected <T> void doCodecTest(int version, T instance, CodecAssertion<T> assertion, boolean withTracker) {
        final ByteBuf buffer = allocator.buffer();
        try {
            if (withTracker) {
                final CodecTracker encodeTracker = new CodecTracker();
                this.entityCodec.encode(version, instance, buffer, encodeTracker);
                encodeTracker.visit();
            } else {
                this.entityCodec.encode(version, instance, buffer, null);
            }
            final String encodedHexString = FormatUtils.toHexString(buffer);

            @SuppressWarnings("unchecked") final Class<T> cls = (Class<T>) instance.getClass();
            final T decode;
            if (withTracker) {
                final CodecTracker decodeTracker = new CodecTracker();
                decode = this.entityCodec.decode(version, cls, buffer, decodeTracker);
                decodeTracker.visit();
            } else {
                decode = this.entityCodec.decode(version, cls, buffer, null);
            }
            assertion.accept(instance, encodedHexString, decode);
        } finally {
            XtreamBytes.releaseBuf(buffer);
            Assertions.assertEquals(0, buffer.refCnt());
        }
    }

    protected static String hexStringForGbk(String string) {
        return FormatUtils.toHexString(string.getBytes(XtreamConstants.CHARSET_GBK));
    }

    protected static String hexStringForUtf8(String string) {
        return FormatUtils.toHexString(string.getBytes(XtreamConstants.CHARSET_UTF8));
    }

}
