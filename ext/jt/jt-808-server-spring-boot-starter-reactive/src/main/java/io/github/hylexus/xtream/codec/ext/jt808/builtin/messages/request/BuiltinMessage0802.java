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

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.List;
import java.util.StringJoiner;

/**
 * 存储多媒体数据检索应答
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x0802, desc = "存储多媒体数据检索应答")
public class BuiltinMessage0802 {

    @Preset.JtStyle.Word(desc = "流水号; 对应平台摄像头立即拍摄命令的消息流水号")
    private int flowId;

    @Preset.JtStyle.Word(desc = "多媒体数据总项数")
    private int multimediaDataItemCount;

    @Preset.JtStyle.List(desc = "数据项列表")
    private List<Item> itemList;

    public int getFlowId() {
        return flowId;
    }

    public BuiltinMessage0802 setFlowId(int flowId) {
        this.flowId = flowId;
        return this;
    }

    public int getMultimediaDataItemCount() {
        return multimediaDataItemCount;
    }

    public BuiltinMessage0802 setMultimediaDataItemCount(int multimediaDataItemCount) {
        this.multimediaDataItemCount = multimediaDataItemCount;
        return this;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public BuiltinMessage0802 setItemList(List<Item> itemList) {
        this.itemList = itemList;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage0802.class.getSimpleName() + "[", "]")
                .add("flowId=" + flowId)
                .add("multimediaDataItemCount=" + multimediaDataItemCount)
                .add("itemList=" + itemList)
                .toString();
    }

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

        public long getMultimediaId() {
            return multimediaId;
        }

        public Item setMultimediaId(long multimediaId) {
            this.multimediaId = multimediaId;
            return this;
        }

        public short getMultimediaType() {
            return multimediaType;
        }

        public Item setMultimediaType(short multimediaType) {
            this.multimediaType = multimediaType;
            return this;
        }

        public short getChannelId() {
            return channelId;
        }

        public Item setChannelId(short channelId) {
            this.channelId = channelId;
            return this;
        }

        public short getEventItemCode() {
            return eventItemCode;
        }

        public Item setEventItemCode(short eventItemCode) {
            this.eventItemCode = eventItemCode;
            return this;
        }

        public BuiltinMessage0200 getLocation() {
            return location;
        }

        public Item setLocation(BuiltinMessage0200 location) {
            this.location = location;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Item.class.getSimpleName() + "[", "]")
                    .add("multimediaId=" + multimediaId)
                    .add("multimediaType=" + multimediaType)
                    .add("channelId=" + channelId)
                    .add("eventItemCode=" + eventItemCode)
                    .add("location=" + location)
                    .toString();
        }
    }
}
