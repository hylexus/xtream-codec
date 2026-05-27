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

package io.github.hylexus.xtream.codec.ext.jt808.extensions.handler;


import io.github.hylexus.xtream.codec.core.annotation.XtreamResponseBody;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageEncryptionType;
import io.github.hylexus.xtream.codec.ext.jt808.utils.JtProtocolConstant;

import java.lang.annotation.*;


/**
 * 默认解析顺序:
 * <ol>
 *     <li>处理器方法上的注解</li>
 *     <li>处理器方法返回类型上的注解</li>
 * </ol>
 *
 * @author hylexus
 * @see Jt808ResponseBodyHandlerResultHandler
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@XtreamResponseBody
public @interface Jt808ResponseBody {
    /**
     * @return 服务端响应消息的消息类型
     */
    int messageId();

    /**
     * @return 消息体加密类型
     */
    byte encryptionType() default Jt808MessageEncryptionType.DEFAULT_ENCRYPTION_TYPE;

    /**
     * @return 单个消息包的最大字节数, 超过该值会自动分包发送
     */
    int maxPackageSize() default JtProtocolConstant.DEFAULT_MAX_INSTRUCTION_FRAME_LENGTH;

    /**
     * @return 消息头的消息体属性字段中保留的第15个二进制位
     */
    byte reversedBit15InHeader() default 0;

    String desc() default "";
}
