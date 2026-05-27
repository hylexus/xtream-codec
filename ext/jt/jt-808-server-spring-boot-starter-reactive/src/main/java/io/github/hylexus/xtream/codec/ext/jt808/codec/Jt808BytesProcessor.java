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

package io.github.hylexus.xtream.codec.ext.jt808.codec;

import io.github.hylexus.xtream.codec.ext.jt808.exception.Jt808MessageEscapeException;
import io.netty.buffer.ByteBuf;

/**
 * @author hylexus
 */
public interface Jt808BytesProcessor {

    /**
     * 转义请求消息
     *
     * @param byteBuf 请求消息, 不包含分隔符 {@code 0x7e}
     * @return 转义之后的消息
     */
    ByteBuf doEscapeForReceive(ByteBuf byteBuf) throws Jt808MessageEscapeException;

    /**
     * 转义响应消息
     *
     * @param byteBuf 响应给客户端的消息, 不包含分隔符 {@code 0x7e}
     * @return 转义之后的消息
     */
    ByteBuf doEscapeForSend(ByteBuf byteBuf) throws Jt808MessageEscapeException;

    /**
     * 校验码指从消息头开始，同后一字节异或，直到校验码前一个字节，占用一个字节。
     *
     * @param byteBuf 请求消息/响应消息
     * @return 检验码
     */
    byte calculateCheckSum(ByteBuf byteBuf);
}
