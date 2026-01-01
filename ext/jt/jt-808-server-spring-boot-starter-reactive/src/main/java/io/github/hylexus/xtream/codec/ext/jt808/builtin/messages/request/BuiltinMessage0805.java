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

import io.github.hylexus.xtream.codec.core.annotation.Expression;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.type.wrapper.DwordWrapper;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 摄像头立即拍摄命令应答
 *
 * @author hylexus
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Jt808ResponseBody(messageId = 0x0805, desc = "摄像头立即拍摄命令应答")
public class BuiltinMessage0805 {

    @Preset.JtStyle.Word(desc = "流水号; 对应平台摄像头立即拍摄命令的消息流水号")
    private int flowId;

    /**
     * 0：成功；1：失败；2：通道不支持。
     * 以下字段在结果=0时才有效。
     */
    @Preset.JtStyle.Byte(desc = "0：成功；1：失败；2：通道不支持")
    private short result;

    /**
     * 拍摄成功的多媒体个数
     */
    // @Preset.JtStyle.Word(condition = "success()", desc = "多媒体 ID 个数")
    @Preset.JtStyle.Word(conditions = @Expression(spel = "success()", mvel = "self.success()", aviator = "self.success"), desc = "多媒体 ID 个数")
    private int multimediaIdCount;

    /**
     * BYTE[4*n]
     */
    // @Preset.JtStyle.List(condition = "success()", desc = "多媒体 ID 列表")
    @Preset.JtStyle.List(conditions = @Expression(spel = "success()", mvel = "self.success()", aviator = "self.success"), desc = "多媒体 ID 列表")
    private List<DwordWrapper> multimediaIdList;

    public boolean success() {
        return result == 0;
    }

    // for Aviator
    public boolean isSuccess() {
        return success();
    }

}
