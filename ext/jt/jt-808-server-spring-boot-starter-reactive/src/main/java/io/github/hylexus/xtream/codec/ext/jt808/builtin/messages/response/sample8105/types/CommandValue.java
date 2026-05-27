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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.sample8105.types;

import io.netty.buffer.ByteBuf;

/**
 * @author hylexus
 */
public interface CommandValue<T> {

    static int checkOffset(int offset) {
        if (offset < 0 || offset >= 13) {
            throw new IllegalArgumentException("offset must be between 0 and 12. Actual : " + offset);
        }
        return offset;
    }

    /**
     * <li>offset-0: 连接控制</li>
     * <li>offset-1: 拨号点名称</li>
     * <li>offset-2: 拨号用户名</li>
     * <li>offset-3: 拨号密码</li>
     * <li>offset-4: 地址</li>
     * <li>offset-5: TCP端口</li>
     * <li>offset-6: UDP端口</li>
     * <li>offset-7: 制造商ID</li>
     * <li>offset-8: 监管平台鉴权码</li>
     * <li>offset-9: 硬件版本</li>
     * <li>offset-10: 固件版本</li>
     * <li>offset-11: URL地址</li>
     * <li>offset-12: 连接到指定服务器时限</li>
     *
     * @see #checkOffset(int)
     */
    int offset();

    /**
     * String_GBK / U16(WORD) / U8(BYTE)
     */
    T value();

    /**
     * 序列化到 {@code output} 中
     */
    void writeTo(ByteBuf output);

    /**
     * 从 {@code input} 中反序列化
     */
    CommandValue<T> readFrom(ByteBuf input);
}
