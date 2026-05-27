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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request;

import io.github.hylexus.xtream.codec.core.annotation.ext.*;
import io.github.hylexus.xtream.codec.core.annotation.tlv.XtreamTLVFieldSequence;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.type.TLV;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.StringJoiner;

/**
 * 查询终端参数应答 0x0104
 *
 * @author hylexus
 * @since 0.4.0
 *
 */
@ApiStatus.AvailableSince("0.4.0")
@Jt808ResponseBody(messageId = 0x0104, desc = "查询终端参数应答(TLV示例)")
public class BuiltinMessage0104SampleTLV {
    /**
     * 对应的终端参数查询消息的流水号
     */
    @Preset.JtStyle.Word
    private int flowId;

    /**
     * 应答参数个数
     */
    @Preset.JtStyle.Byte
    private short parameterCount;

    @XtreamTLVFieldSequence(
            decoder = @XtreamTLVFieldSequence.Decoder(
                    tag = @Key(type = KeyType.u32),
                    length = @ValueLength(type = LengthFieldType.u8),
                    value = @XtreamTLVFieldSequence.Value(
                            matchers = {
                                    @ValueMatcher(
                                            matchU32 = {
                                                    0x0001L, 0x0002L, 0x0003L, 0x0004L, 0x0005L, 0x0006L, 0x0007L,
                                                    0x001BL, 0x001CL,
                                                    0x0020L, 0x0021L, 0x0022L,
                                                    0x0027L, 0x0028L, 0x0029L,
                                                    0x002CL, 0x002DL, 0x002EL, 0x002FL, 0x0030L,
                                            },
                                            valueType = XtreamDataType.u32
                                    ),
                                    @ValueMatcher(
                                            matchU32 = {
                                                    0x0045L, 0x0046L, 0x0047L,
                                                    0x0050L, 0x0051L, 0x0052L, 0x0053L, 0x0054L, 0x0055L,
                                                    0x0056L, 0x0057L, 0x0058L, 0x0059L, 0x005AL,
                                                    0x0064L, 0x0065L,
                                                    0x0070L, 0x0071L, 0x0072L, 0x0073L, 0x0074L,
                                                    0x0080L, 0x0093L, 0x0095L, 0x0100L, 0x0102L,
                                            },
                                            valueType = XtreamDataType.u32
                                    ),
                                    @ValueMatcher(
                                            matchU32 = {
                                                    0x0011L, 0x0012L, 0x0013L, 0x0014L, 0x0015L, 0x0016L, 0x0017L,
                                                    0x001AL, 0x001DL,
                                                    0x0023L, 0x0024L, 0x0025L, 0x0026L,
                                                    0x0032L,
                                                    0x0040L, 0x0041L, 0x0042L, 0x0043L, 0x0044L,
                                            },
                                            valueType = XtreamDataType.string_gbk
                                    ),
                                    @ValueMatcher(matchU32 = {0x0048L, 0x0049L, 0x0083L,}, valueType = XtreamDataType.string_gbk),
                                    @ValueMatcher(matchU32 = 0x0010L, valueType = XtreamDataType.string_gbk),
                                    @ValueMatcher(matchU32 = 0x0031L, valueType = XtreamDataType.u16),
                                    @ValueMatcher(
                                            matchU32 = {
                                                    0x005BL, 0x005CL, 0x005DL, 0x005EL,
                                                    0x0081L, 0x0082L, 0x0101L, 0x0103L,
                                            },
                                            valueType = XtreamDataType.u16
                                    ),
                                    @ValueMatcher(matchU32 = {0x0084L, 0x0090L, 0x0091L, 0x0092L, 0x0094L,}, valueType = XtreamDataType.u8)
                            },
                            // 其他没匹配到的参数 都解析为 hexString
                            fallbackMatchers = @FallbackValueMatcher(valueType = XtreamDataType.string_hex)
                    )
            )
    )
    private List<TLV> parameterItems;

    public int getFlowId() {
        return flowId;
    }

    public BuiltinMessage0104SampleTLV setFlowId(int flowId) {
        this.flowId = flowId;
        return this;
    }

    public short getParameterCount() {
        return parameterCount;
    }

    public BuiltinMessage0104SampleTLV setParameterCount(short parameterCount) {
        this.parameterCount = parameterCount;
        return this;
    }

    public List<TLV> getParameterItems() {
        return parameterItems;
    }

    public BuiltinMessage0104SampleTLV setParameterItems(List<TLV> parameterItems) {
        this.parameterItems = parameterItems;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage0104SampleTLV.class.getSimpleName() + "[", "]")
                .add("flowId=" + flowId)
                .add("parameterCount=" + parameterCount)
                .add("parameterItems=" + parameterItems)
                .toString();
    }
}
