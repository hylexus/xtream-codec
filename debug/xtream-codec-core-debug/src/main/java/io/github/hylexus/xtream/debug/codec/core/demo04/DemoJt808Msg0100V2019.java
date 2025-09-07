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

package io.github.hylexus.xtream.debug.codec.core.demo04;

import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.debug.codec.core.demo04.spec.BaseJt808Msg;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class DemoJt808Msg0100V2019 extends BaseJt808Msg {
    // 1. [0-2) WORD 省域ID
    @Preset.RustStyle.u16
    private int provinceId;

    // 2. [2-4) WORD 市县域ID
    @Preset.RustStyle.u16
    private int cityId;

    // 3. [4-15) BYTE[11] 制造商ID
    @Preset.RustStyle.str(length = 11, charset = XtreamConstants.CHARSET_NAME_GBK)
    private String manufacturerId;

    // 4. [15-45) BYTE[30] 终端型号
    @Preset.RustStyle.str(length = 30, charset = XtreamConstants.CHARSET_NAME_GBK)
    private String terminalType;

    // 5. [45-75) BYTE[30] 终端ID
    @Preset.RustStyle.str(length = 30, charset = XtreamConstants.CHARSET_NAME_GBK)
    private String terminalId;

    // 6. [75]   BYTE    车牌颜色
    @Preset.RustStyle.u8
    private short color;

    /**
     * 7. [76,n)   String    车辆标识
     * <p>
     * 使用 SpEL 计算消息长度(上下文中的消息体总长度减去前面消费掉的字节数)
     * <p>
     * 提示: 最后一个 String 类型的字段可以不指定长度: 意味着长度是后续所有的字节数
     *
     * @see FieldCodec#deserialize(io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata, FieldCodec.DeserializeContext, ByteBuf, int)
     */
    // 1. 使用 SpEL 计算消息长度(上下文中的消息体总长度减去前面消费掉的字节数)
    // @Preset.RustStyle.str(charset = XtreamConstants.CHARSET_NAME_GBK, lengthExpression = "msgBodyLength() - (1 + 30 + 30 +11 +2 +2)")
    // 2. 不指定长度(意味着长度是后续所有的字节数)
    @Preset.RustStyle.str(charset = XtreamConstants.CHARSET_NAME_GBK)
    private String carIdentifier;

}
