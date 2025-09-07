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

package io.github.hylexus.xtream.codec.core.tracker;

import io.github.hylexus.xtream.codec.core.type.Preset;
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
public class CodecDebugEntity03 {

    /**
     * 流水号
     * <p>
     * 对应平台摄像头立即拍摄命令的消息流水号
     */
    @Preset.JtStyle.Word
    private int flowId;

    /**
     * 多媒体数据总项数
     */
    @Preset.JtStyle.Word
    private int multimediaDataItemCount;

    @Preset.JtStyle.List(iterationTimesExpression = "getMultimediaDataItemCount()")
    private List<Item> itemList;

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class Item {
        /**
         * 多媒体ID
         */
        @Preset.JtStyle.Dword
        private long multimediaId;

        /**
         * 多媒体类型
         * <p>
         * 0：图像；1：音频；2：视频
         */
        @Preset.JtStyle.Byte
        private short multimediaType;

        /**
         * 通道ID
         */
        @Preset.JtStyle.Byte
        private short channelId;

        /**
         * 事件项编码
         * <p>
         * 0：平台下发指令；1：定时动作；2：抢劫报警触发；3：碰撞侧翻报警触发；其他保留
         */
        @Preset.JtStyle.Byte
        private short eventItemCode;

        /**
         * 位置信息汇报(0x0200)消息体
         */
        @Preset.JtStyle.Object(length = 28)
        private CodecDebugEntity02 location;
    }
}
