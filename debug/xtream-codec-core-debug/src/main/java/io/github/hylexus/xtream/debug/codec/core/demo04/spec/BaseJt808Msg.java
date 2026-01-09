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

package io.github.hylexus.xtream.debug.codec.core.demo04.spec;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseJt808Msg {

    /**
     * 消息头
     */
    private Jt808MsgHeader header;
    /**
     * <h3 style="color:red;">警告！！！</h3>
     * 不要依赖这个属性。
     * <p>
     * 不要依赖这个属性。
     * <p>
     * 不要依赖这个属性。
     * <p>
     * 这个属性是内部使用的：表示每个子包的消息体。
     * <p>
     * 合并之后的完整包的这个属性是 {@code null}。
     *
     * @see SimpleCache
     */
    private ByteBuf internalTemporarySubPackageBodyByteBuf;

    /**
     * 校验码
     */
    private byte checkSum;

    /**
     * 消息体长度
     */
    public int getMsgBodyLength() {
        return msgBodyLength();
    }

    /**
     * 消息体长度
     */
    public int msgBodyLength() {
        return header.msgBodyLength();
    }

    public BaseJt808Msg setHeader(Jt808MsgHeader header) {
        this.header = header;
        return this;
    }

    @Override
    public String toString() {
        return "BaseJt808Msg{"
               + "header=" + header
               + ", checkSum=" + checkSum
               + '}';
    }
}
