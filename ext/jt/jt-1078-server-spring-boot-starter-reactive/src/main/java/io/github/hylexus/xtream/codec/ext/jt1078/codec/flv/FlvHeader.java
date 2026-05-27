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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.flv;

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.impl.DefaultFlvHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * 9 bytes. FLV file header.
 */
public interface FlvHeader {

    int FLV_HEADER_SIGNATURE = ('F' << 16) | ('L' << 8) | ('V');

    /**
     * signature: 3byte 固定为 "FLV" 即 0x46,0x4c,0x56
     */
    default int signature() {
        return FLV_HEADER_SIGNATURE;
    }

    /**
     * version: 1byte
     */
    default byte version() {
        return 0x01;
    }


    /**
     * total: 1byte (00000101)
     * <p>
     * - bit[3,7]: 保留(必须为0) TypeFlagsReserved UB[5]
     * <p>
     * - bit[2]: 是否存在音频 TypeFlagsAudio UB[1]
     * <p>
     * - bit[1]: 保留(必须为0) TypeFlagsReserved UB[1]
     * <p>
     * - bit[0]: 是否存在视频 TypeFlagsVideo  UB[1]
     */
    byte flag();

    /**
     * DataOffset UI32
     * <p>
     * version1 中始终为 9
     * <p>
     * 从起始位置到 body 部分的字节数(即 header 的大小)
     */
    default int headerSize() {
        return 9;
    }

    default byte[] toBytes(boolean withPreviousTagSize) {
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        try {
            this.writeTo(buffer, withPreviousTagSize);
            return XtreamBytes.getBytes(buffer);
        } finally {
            buffer.release();
        }
    }

    default void writeTo(ByteBuf byteBuf, boolean withPreviousTagSize) {
        // signature
        byteBuf.writeByte('F');
        byteBuf.writeByte('L');
        byteBuf.writeByte('V');
        // version
        byteBuf.writeByte(this.version());
        // flags
        byteBuf.writeByte(this.flag());
        // size
        byteBuf.writeInt(this.headerSize());
        //  第一个 previousTagSize === 0
        if (withPreviousTagSize) {
            byteBuf.writeInt(0);
        }
    }

    static FlvHeader of(boolean hasVideo, boolean hasAudio) {
        return new DefaultFlvHeader(hasVideo, hasAudio);
    }

}
