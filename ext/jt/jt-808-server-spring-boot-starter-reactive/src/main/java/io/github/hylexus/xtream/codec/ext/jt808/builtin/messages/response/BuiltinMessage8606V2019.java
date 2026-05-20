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

import io.github.hylexus.xtream.codec.common.utils.Numbers;
import io.github.hylexus.xtream.codec.core.annotation.Expression;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.StringJoiner;

/**
 * 设置路线
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8606)
public class BuiltinMessage8606V2019 {

    /**
     * 路线 ID
     */
    @Preset.JtStyle.Dword
    private long routeId;

    /**
     * 路线属性
     * <li> bit[0] -- 1：根据时间</li>
     * <li> bit[1] -- 保留</li>
     * <li> bit[2] -- 1：进路线报警给驾驶员</li>
     * <li> bit[3] -- 1：进路线报警给平台</li>
     * <li> bit[4] -- 1：出路线报警给驾驶员</li>
     * <li> bit[5] -- 1：出路线报警给平台</li>
     * <li> bit[6~15] --- 保留</li>
     */
    @Preset.JtStyle.Word
    private int routeProps;

    /**
     * 起始时间 BCD[6]
     * <p>
     * YY-MM-DD-hh-mm-ss，若区域属性0位为0则没有该字段
     */
    // @Preset.JtStyle.BcdDateTime(condition = "hasTimeProperty()")
    @Preset.JtStyle.BcdDateTime(conditions = @Expression(spel = "hasTimeProperty()", mvel = "self.hasTimeProperty()", aviator = "self.hasTimeProperty"))
    private LocalDateTime startTime;

    /**
     * 结束时间 BCD[6]
     * <p>
     * YY-MM-DD-hh-mm-ss，若区域属性0位为0则没有该字段
     */
    // @Preset.JtStyle.BcdDateTime(condition = "hasTimeProperty()")
    @Preset.JtStyle.BcdDateTime(conditions = @Expression(spel = "hasTimeProperty()", mvel = "self.hasTimeProperty()", aviator = "self.hasTimeProperty"))
    private LocalDateTime endTime;

    @SuppressWarnings("unused")
    public boolean hasTimeProperty() {
        return Numbers.getBitAt(this.routeProps, 0) == 1;
    }

    // for Aviator
    @SuppressWarnings("unused")
    public boolean isHasTimeProperty() {
        return Numbers.getBitAt(this.routeProps, 0) == 1;
    }

    /**
     * 路线总拐点数
     */
    @Preset.JtStyle.Word
    private int count;

    @Preset.JtStyle.List
    private List<Item> itemList;

    public long getRouteId() {
        return routeId;
    }

    public BuiltinMessage8606V2019 setRouteId(long routeId) {
        this.routeId = routeId;
        return this;
    }

    public int getRouteProps() {
        return routeProps;
    }

    public BuiltinMessage8606V2019 setRouteProps(int routeProps) {
        this.routeProps = routeProps;
        return this;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public BuiltinMessage8606V2019 setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public BuiltinMessage8606V2019 setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public int getCount() {
        return count;
    }

    public BuiltinMessage8606V2019 setCount(int count) {
        this.count = count;
        return this;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public BuiltinMessage8606V2019 setItemList(List<Item> itemList) {
        this.itemList = itemList;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8606V2019.class.getSimpleName() + "[", "]")
                .add("routeId=" + routeId)
                .add("routeProps=" + routeProps)
                .add("startTime=" + startTime)
                .add("endTime=" + endTime)
                .add("count=" + count)
                .add("itemList=" + itemList)
                .toString();
    }

    public static class Item {
        /**
         * 拐点 ID
         */
        @Preset.JtStyle.Dword
        private long id;
        /**
         * 路段 ID
         */
        @Preset.JtStyle.Dword
        private long routeId;

        /**
         * 拐点纬度: 以度为单位的纬度值乘以 10 的 6 次方，精确到百万分之一度
         */
        @Preset.JtStyle.Dword
        private long latitude;

        /**
         * 拐点经度: 以度为单位的纬度值乘以 10 的 6 次方，精确到百万分之一度
         */
        @Preset.JtStyle.Dword
        private long longitude;

        /**
         * 路段宽度
         */
        @Preset.JtStyle.Byte
        private short routeWidth;

        /**
         * 路段属性
         * <li>bit[0] --  1：行驶时间</li>
         * <li>bit[1] --  1：限速</li>
         * <li>bit[2] --  0：北纬；1：南纬</li>
         * <li>bit[3] --  0：东经；1：西经</li>
         * <li>bit[4~7] -- 保留</li>
         */
        @Preset.JtStyle.Byte
        private short routeProps;

        /**
         * 路段行驶过长阈值
         * <p>
         * 单位为秒（s），若路段属性0位为0则没有该字段
         */
        // @Preset.JtStyle.Word(condition = "hasThresholdProperty()")
        @Preset.JtStyle.Word(conditions = @Expression(spel = "hasThresholdProperty()", mvel = "self.hasThresholdProperty()", aviator = "self.hasThresholdProperty"))
        private Integer longDriveThreshold;

        /**
         * 路段行驶不足阈值
         * 单位为秒（s），若路段属性0位为0则没有该字段
         */
        // @Preset.JtStyle.Word(condition = "hasThresholdProperty()")
        @Preset.JtStyle.Word(conditions = @Expression(spel = "hasThresholdProperty()", mvel = "self.hasThresholdProperty()", aviator = "self.hasThresholdProperty"))
        private Integer shortDriveThreshold;

        @SuppressWarnings("unused")
        public boolean hasThresholdProperty() {
            return Numbers.getBitAt(this.routeProps, 0) == 1;
        }

        // for Aviator
        @SuppressWarnings("unused")
        public boolean isHasThresholdProperty() {
            return Numbers.getBitAt(this.routeProps, 0) == 1;
        }

        /**
         * 路段最高速度
         * <p>
         * 单位为公里每小时（km/h），若路段属性 1 位为 0 则没有该字段
         */
        // @Preset.JtStyle.Word(condition = "hasSpeedLimitProperty()")
        @Preset.JtStyle.Word(conditions = @Expression(spel = "hasSpeedLimitProperty()", mvel = "self.hasSpeedLimitProperty()", aviator = "self.hasSpeedLimitProperty"))
        private Integer maxSpeedLimit;

        /**
         * 路段超速持续时间
         * <p>
         * 单位为秒（s），若路段属性1位为0则没有该字段
         */
        // @Preset.JtStyle.Byte(condition = "hasSpeedLimitProperty()")
        @Preset.JtStyle.Byte(conditions = @Expression(spel = "hasSpeedLimitProperty()", mvel = "self.hasSpeedLimitProperty()", aviator = "self.hasSpeedLimitProperty"))
        private Short speedingDuration;

        @SuppressWarnings("unused")
        public boolean hasSpeedLimitProperty() {
            return Numbers.getBitAt(this.routeProps, 1) == 1;
        }

        // for Aviator
        @SuppressWarnings("unused")
        public boolean isHasSpeedLimitProperty() {
            return Numbers.getBitAt(this.routeProps, 1) == 1;
        }

        /**
         * 名称长度
         */
        @Preset.JtStyle.Word
        private int areaNameLength;

        /**
         * 路线名称
         */
        @Preset.JtStyle.Str
        private String areaName;

        public long getId() {
            return id;
        }

        public Item setId(long id) {
            this.id = id;
            return this;
        }

        public long getRouteId() {
            return routeId;
        }

        public Item setRouteId(long routeId) {
            this.routeId = routeId;
            return this;
        }

        public long getLatitude() {
            return latitude;
        }

        public Item setLatitude(long latitude) {
            this.latitude = latitude;
            return this;
        }

        public long getLongitude() {
            return longitude;
        }

        public Item setLongitude(long longitude) {
            this.longitude = longitude;
            return this;
        }

        public short getRouteWidth() {
            return routeWidth;
        }

        public Item setRouteWidth(short routeWidth) {
            this.routeWidth = routeWidth;
            return this;
        }

        public short getRouteProps() {
            return routeProps;
        }

        public Item setRouteProps(short routeProps) {
            this.routeProps = routeProps;
            return this;
        }

        public Integer getLongDriveThreshold() {
            return longDriveThreshold;
        }

        public Item setLongDriveThreshold(Integer longDriveThreshold) {
            this.longDriveThreshold = longDriveThreshold;
            return this;
        }

        public Integer getShortDriveThreshold() {
            return shortDriveThreshold;
        }

        public Item setShortDriveThreshold(Integer shortDriveThreshold) {
            this.shortDriveThreshold = shortDriveThreshold;
            return this;
        }

        public Integer getMaxSpeedLimit() {
            return maxSpeedLimit;
        }

        public Item setMaxSpeedLimit(Integer maxSpeedLimit) {
            this.maxSpeedLimit = maxSpeedLimit;
            return this;
        }

        public Short getSpeedingDuration() {
            return speedingDuration;
        }

        public Item setSpeedingDuration(Short speedingDuration) {
            this.speedingDuration = speedingDuration;
            return this;
        }

        public int getAreaNameLength() {
            return areaNameLength;
        }

        public Item setAreaNameLength(int areaNameLength) {
            this.areaNameLength = areaNameLength;
            return this;
        }

        public String getAreaName() {
            return areaName;
        }

        public Item setAreaName(String areaName) {
            this.areaName = areaName;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Item.class.getSimpleName() + "[", "]")
                    .add("id=" + id)
                    .add("routeId=" + routeId)
                    .add("latitude=" + latitude)
                    .add("longitude=" + longitude)
                    .add("routeWidth=" + routeWidth)
                    .add("routeProps=" + routeProps)
                    .add("longDriveThreshold=" + longDriveThreshold)
                    .add("shortDriveThreshold=" + shortDriveThreshold)
                    .add("maxSpeedLimit=" + maxSpeedLimit)
                    .add("speedingDuration=" + speedingDuration)
                    .add("areaNameLength=" + areaNameLength)
                    .add("areaName='" + areaName + "'")
                    .toString();
        }
    }
}
