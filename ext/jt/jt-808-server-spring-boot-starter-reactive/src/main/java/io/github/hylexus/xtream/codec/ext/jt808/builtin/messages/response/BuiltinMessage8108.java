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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.jackson.XtreamCodecDebugJsonSerializer;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 下发终端升级包
 *
 * @author hylexus
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Jt808ResponseBody(messageId = 0x8108, desc = "下发终端升级包")
public class BuiltinMessage8108 {
    /**
     * 升级类型
     * <p>
     * 0：终端，12：道路运输证 IC 卡读卡器，52：北斗卫星定位模块
     */
    @Preset.JtStyle.Byte(desc = "升级类型")
    private short type;

    @Preset.JtStyle.Bytes(length = 5, desc = "制造商 ID")
    private String manufacturerId;

    // prependLengthFieldType: 前置一个 u8类型的字段 表示 版本号长度
    @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "版本号")
    private String version;

    // prependLengthFieldType: 前置一个 u32类型的字段 表示 升级数据包长度
    @Preset.JtStyle.Bytes(prependLengthFieldType = PrependLengthFieldType.u32, desc = "升级数据包")
    @JsonSerialize(using = XtreamCodecDebugJsonSerializer.class)
    private byte[] data;

}
