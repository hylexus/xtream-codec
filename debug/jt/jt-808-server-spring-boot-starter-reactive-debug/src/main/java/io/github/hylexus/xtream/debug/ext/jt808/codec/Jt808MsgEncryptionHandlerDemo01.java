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

package io.github.hylexus.xtream.debug.ext.jt808.codec;


import io.github.hylexus.xtream.codec.common.exception.NotYetImplementedException;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageDescriber;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageEncryptionHandler;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestHeader;
import io.github.hylexus.xtream.codec.ext.jt808.utils.JtCryptoUtil;
import io.netty.buffer.ByteBuf;
import org.springframework.stereotype.Component;

/**
 * @author hylexus
 */
@Component
public class Jt808MsgEncryptionHandlerDemo01 implements Jt808MessageEncryptionHandler {

    public Jt808MsgEncryptionHandlerDemo01() {
    }

    @Override
    public ByteBuf decryptRequestBody(Jt808RequestHeader header, ByteBuf body) {
        final int encryptionType = header.messageBodyProps().encryptionType();
        if (encryptionType == 0) {
            return body;
        }
        // @see https://github.com/hylexus/jt-framework/issues/82
        // 消息属性中的 第10位，11位，12位 为 010 时，表示消息体经过SM4算法加密
        if (encryptionType == 0b010) {
            try {
                return JtCryptoUtil.SM4.ecbDecrypt(getSecretKey(), body);
            } finally {
                XtreamBytes.releaseBuf(body);
            }
        }
        throw new NotYetImplementedException("不支持的加密类型: 0b" + FormatUtils.toBinaryString(encryptionType, 3));
    }

    @Override
    public ByteBuf encryptResponseBody(Jt808MessageDescriber describer, ByteBuf plaintextBody) {
        final int encryptionType = describer.encryptionType();
        if (encryptionType == 0) {
            return plaintextBody;
        }

        // @see https://github.com/hylexus/jt-framework/issues/82
        // 消息属性中的 第10位，11位，12位 为 010 时，表示消息体经过SM4算法加密
        if (encryptionType == 0b010) {
            try {
                return JtCryptoUtil.SM4.ecbEncrypt(getSecretKey(), plaintextBody);
            } finally {
                XtreamBytes.releaseBuf(plaintextBody);
            }
        }
        throw new NotYetImplementedException("不支持的加密类型: 0b" + FormatUtils.toBinaryString(encryptionType, 3));
    }

    private byte[] getSecretKey() {
        // 从其他配置中获取密钥
        return XtreamBytes.decodeHex("8e47374be6b8d114cb47be6a9a128a37");
    }
}
