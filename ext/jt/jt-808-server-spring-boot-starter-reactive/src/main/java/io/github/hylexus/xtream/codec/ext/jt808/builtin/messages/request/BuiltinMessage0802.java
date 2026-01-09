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

import java.util.List;

/**
 * 存储多媒体数据检索应答
 *
 * @author hylexus
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Jt808ResponseBody(messageId = 0x0802, desc = "存储多媒体数据检索应答")
public class BuiltinMessage0802 {

    @Preset.JtStyle.Word(desc = "流水号; 对应平台摄像头立即拍摄命令的消息流水号")
    private int flowId;

    @Preset.JtStyle.Word(desc = "多媒体数据总项数")
    private int multimediaDataItemCount;

    @Preset.JtStyle.List(desc = "数据项列表")
    private List<Item> itemList;

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class Item {

        @Preset.JtStyle.Dword(desc = "多媒体ID")
        private long multimediaId;

        @Preset.JtStyle.Byte(desc = "多媒体类型。 0：图像；1：音频；2：视频")
        private short multimediaType;

        @Preset.JtStyle.Byte(desc = "通道ID")
        private short channelId;

        /**
         * 事件项编码
         * <p>
         * 0：平台下发指令；1：定时动作；2：抢劫报警触发；3：碰撞侧翻报警触发；其他保留
         */
        @Preset.JtStyle.Byte(desc = "事件项编码")
        private short eventItemCode;

        /**
         * 位置信息汇报(0x0200)消息体
         */
        @Preset.JtStyle.Object(length = 28, desc = "位置信息汇报")
        private BuiltinMessage0200 location;
    }
}
