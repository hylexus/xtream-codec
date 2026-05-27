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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.h264;

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.netty.buffer.ByteBuf;

public interface H264Nalu {

    // nalu header
    H264NaluHeader header();

    // RawByte Sequence Payload
    // 去掉 startCode、去掉防竞争字节序 之后的部分
    ByteBuf rbsp();

    default void close() {
        XtreamBytes.releaseBuf(this.rbsp());
    }

}
