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

import io.github.hylexus.xtream.codec.common.utils.Numbers;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.impl.DefaultFlvTagHeader;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.tag.FlvTagType;
import io.netty.buffer.ByteBuf;

/**
 * 11 bytes
 */
public interface FlvTagHeader {
    /**
     * 1 byte
     *
     * @return Tag类型
     */
    FlvTagType tagTye();

    /**
     * 3 bytes
     * <p>
     * 从 {@link #streamId()} 之后开始算起
     *
     * @return data部分大小
     */
    int dataSize();

    /**
     * 3 bytes
     *
     * @return Tag时间戳
     */
    int timestamp();

    /**
     * 1 byte
     *
     * @return Tag时间戳扩展
     */
    byte timestampExtended();


    /**
     * 3 bytes
     * <p>
     * 总是为零
     *
     * @return streamId
     */
    default int streamId() {
        return 0;
    }

    default int writeTo(ByteBuf byteBuf) {
        // 1 byte
        byteBuf.writeByte(tagTye().getValue());
        // 3 bytes dataSize
        byteBuf.writeBytes(Numbers.intTo3Bytes(dataSize()));
        // byteBuf.writeInt(timestampExtended() << 24 | timestamp());
        // 3 bytes timestamp
        byteBuf.writeBytes(Numbers.intTo3Bytes(timestamp()));
        // 1 byte timestampExtended
        byteBuf.writeByte(timestamp() >>> 24);
        // 3 bytes streamId
        byteBuf.writeBytes(Numbers.intTo3Bytes(streamId()));
        return 11;
    }

    static FlvTagHeader newVideoTagHeader(int dataSize, int timestamp) {
        return new DefaultFlvTagHeader(FlvTagType.VIDEO, dataSize, timestamp);
    }

    static FlvTagHeader newAudioTagHeader(int dataSize, int timestamp) {
        return new DefaultFlvTagHeader(FlvTagType.AUDIO, dataSize, timestamp);
    }

    static FlvTagHeader newScriptTagHeader(int dataSize, int timestamp) {
        return new DefaultFlvTagHeader(FlvTagType.SCRIPT_DATA, dataSize, timestamp);
    }

}
