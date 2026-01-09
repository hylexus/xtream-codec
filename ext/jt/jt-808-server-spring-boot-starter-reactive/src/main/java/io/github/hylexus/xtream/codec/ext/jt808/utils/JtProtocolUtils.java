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

package io.github.hylexus.xtream.codec.ext.jt808.utils;

import io.netty.buffer.ByteBuf;

public final class JtProtocolUtils {
    private JtProtocolUtils() {
    }

    private static final byte[] ATTACHMENT_REQUEST_PREFIX = {0x30, 0x31, 0x63, 0x64};

    public static boolean isAttachmentRequest(ByteBuf originalPayload) {
        if (originalPayload.readableBytes() < ATTACHMENT_REQUEST_PREFIX.length) {
            return false;
        }
        for (int i = 0; i < ATTACHMENT_REQUEST_PREFIX.length; i++) {
            if (originalPayload.getByte(i) != ATTACHMENT_REQUEST_PREFIX[i]) {
                return false;
            }
        }
        return true;
    }
}
