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

package io.github.hylexus.xtream.codec.ext.jt808.spec;

import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import io.netty.buffer.ByteBuf;

/**
 * @author hylexus
 * @see <a href="https://github.com/hylexus/jt-framework/issues/82">https://github.com/hylexus/jt-framework/issues/82</a>
 */
public interface Jt808MessageEncryptionHandler {

    /**
     * @param header 请求头
     * @param body   请求体；可能是明文也可能是密文，根据 `header` 判断
     * @return 解密之后的明文 或者 原样返回 `body`
     * @see Jt808RequestHeader#messageBodyProps() ()
     * @see Jt808RequestHeader.Jt808MessageBodyProps#encryptionType()
     * @see Jt808RequestHeader.Jt808MessageBodyProps#dataEncryptionType()
     */
    ByteBuf decryptRequestBody(Jt808RequestHeader header, ByteBuf body);

    /**
     * @param describer     本次响应的其他信息
     * @param plaintextBody 明文数据；可能是完整包，也可能是一个子包
     * @return 返回密文 或者 原样返回 {@code plaintextBody}
     * @implNote 如果该方法返回的是一个 <strong>新的 ByteBuf</strong>，那么实现类应该负责将入参 {@code plaintextBody} 释放掉
     * @see Jt808ResponseBody#encryptionType()
     * @see Jt808MessageDescriber#encryptionType()
     */
    ByteBuf encryptResponseBody(Jt808MessageDescriber describer, ByteBuf plaintextBody);

    class NoOps implements Jt808MessageEncryptionHandler {

        @Override
        public ByteBuf decryptRequestBody(Jt808RequestHeader header, ByteBuf body) {
            return body;
        }

        @Override
        public ByteBuf encryptResponseBody(Jt808MessageDescriber describer, ByteBuf plaintextBody) {
            return plaintextBody;
        }
    }
}
