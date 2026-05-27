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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.hylexus.xtream.codec.core.jackson.XtreamCodecDebugJsonSerializer;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * CAN 总线数据上传
 *
 * @author hylexus
 */
@SuppressWarnings("NotNullFieldNotInitialized")
@Jt808ResponseBody(messageId = 0x0705, desc = "CAN 总线数据上传")
public class BuiltinMessage0705 {

    /**
     * 数据项个数
     * <p>
     * 包含的CAN总线数据项个数，>0
     */
    @Preset.JtStyle.Word
    private int count;
    /**
     * CAN总线数据接收时间 BCD[5]
     * <p>
     * 第1条CAN总线数据的接收时间，hh-mm-ss-msms
     */
    @Preset.JtStyle.BcdDateTime(length = 5, pattern = "HHmmssSSSS")
    private LocalTime receiveTime;

    /**
     * CAN总线数据项
     */
    @Preset.JtStyle.List
    private List<Item> itemList;

    public int getCount() {
        return count;
    }

    public BuiltinMessage0705 setCount(int count) {
        this.count = count;
        return this;
    }

    public LocalTime getReceiveTime() {
        return receiveTime;
    }

    public BuiltinMessage0705 setReceiveTime(LocalTime receiveTime) {
        this.receiveTime = receiveTime;
        return this;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public BuiltinMessage0705 setItemList(List<Item> itemList) {
        this.itemList = itemList;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage0705.class.getSimpleName() + "[", "]")
                .add("count=" + count)
                .add("receiveTime=" + receiveTime)
                .add("itemList=" + itemList)
                .toString();
    }

    public static class Item {
        /**
         * CAN ID
         * <li>bit[31] -- 表示CAN 通道号，0：CAN1，1：CAN2</li>
         * <li>bit[30] -- 表示帧类型，0：标准帧，1：扩展帧</li>
         * <li>bit[29] -- 表示数据采集方式，0：原始数据，1：采集区间的平均值</li>
         * <li>bit[28~0] -- 表示CAN总线ID</li>
         */
        @Preset.JtStyle.Dword
        private long canId;

        /**
         * CAN DATA
         * <p>
         * CAN数据
         */
        @Preset.JtStyle.Bytes(length = 8)
        @JsonSerialize(using = XtreamCodecDebugJsonSerializer.class)
        private byte[] canData;

        public long getCanId() {
            return canId;
        }

        public Item setCanId(long canId) {
            this.canId = canId;
            return this;
        }

        public byte[] getCanData() {
            return canData;
        }

        public Item setCanData(byte[] canData) {
            this.canData = canData;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Item.class.getSimpleName() + "[", "]")
                    .add("canId=" + canId)
                    .add("canData=" + Arrays.toString(canData))
                    .toString();
        }
    }

}
