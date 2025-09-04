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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request;

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 终端注册(多版本合一)
 *
 * @author hylexus
 * @see io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.BuiltinMessage8100
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Jt808ResponseBody(messageId = 0x0100, desc = "终端注册(多版本合一)")
public class BuiltinMessage0100AllInOne {

    @Preset.JtStyle.Word(desc = "省域ID")
    private int provinceId;

    @Preset.JtStyle.Word(desc = "市域ID")
    private int cityId;

    @Preset.JtStyle.Bytes(version = {-1, 0}, length = 5, desc = "制造商ID(2011 || 2013)")
    @Preset.JtStyle.Bytes(version = 1, length = 11, desc = "制造商ID(2019)")
    private String manufacturerId;

    @Preset.JtStyle.Bytes(version = -1, length = 8, desc = "终端型号(2011)")
    @Preset.JtStyle.Bytes(version = 0, length = 20, desc = "终端型号(2013)")
    @Preset.JtStyle.Bytes(version = 1, length = 30, desc = "终端型号(2019)")
    private String terminalType;

    @Preset.JtStyle.Bytes(version = {-1, 0}, length = 7, desc = "终端ID(2011 || 2013)")
    @Preset.JtStyle.Bytes(version = 1, length = 30, desc = "终端ID(2019)")
    private String terminalId;

    @Preset.JtStyle.Byte(desc = "车牌颜色")
    private short color;

    // 这里不写 length 表示读取后续所有字节
    @Preset.JtStyle.Str(desc = "车辆标识")
    private String carIdentifier;

}
