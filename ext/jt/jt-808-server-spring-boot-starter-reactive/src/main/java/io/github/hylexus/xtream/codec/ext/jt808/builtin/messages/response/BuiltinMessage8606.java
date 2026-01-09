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

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hylexus.xtream.codec.common.utils.Numbers;
import io.github.hylexus.xtream.codec.core.annotation.Expression;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设置路线
 *
 * @author hylexus
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Jt808ResponseBody(messageId = 0x8606, desc = "设置路线")
public class BuiltinMessage8606 {

    @Preset.JtStyle.Dword(desc = "路线 ID")
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
    @Preset.JtStyle.Word(desc = "路线属性")
    private int routeProps;

    /**
     * 起始时间 BCD[6]
     * <p>
     * YY-MM-DD-hh-mm-ss，若区域属性0位为0则没有该字段
     */
    // @Preset.JtStyle.BcdDateTime(condition = "hasTimeProperty()", desc = "起始时间 BCD[6]")
    @Preset.JtStyle.BcdDateTime(conditions = @Expression(spel = "hasTimeProperty()", mvel = "self.hasTimeProperty()", aviator = "self.hasTimeProperty"), desc = "起始时间 BCD[6]")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间 BCD[6]
     * <p>
     * YY-MM-DD-hh-mm-ss，若区域属性0位为0则没有该字段
     */
    // @Preset.JtStyle.BcdDateTime(condition = "hasTimeProperty()", desc = "结束时间 BCD[6]")
    @Preset.JtStyle.BcdDateTime(conditions = @Expression(spel = "hasTimeProperty()", mvel = "self.hasTimeProperty()", aviator = "self.hasTimeProperty"), desc = "结束时间 BCD[6]")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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

    @Preset.JtStyle.Word(desc = "路线总拐点数")
    private int count;

    @Preset.JtStyle.List(desc = "拐点项")
    private List<Item> itemList;

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class Item {

        @Preset.JtStyle.Dword(desc = "拐点 ID")
        private long id;

        @Preset.JtStyle.Dword(desc = "路段 ID")
        private long routeId;

        /**
         * 拐点纬度: 以度为单位的纬度值乘以 10 的 6 次方，精确到百万分之一度
         */
        @Preset.JtStyle.Dword(desc = "拐点纬度")
        private long latitude;

        /**
         * 拐点经度: 以度为单位的纬度值乘以 10 的 6 次方，精确到百万分之一度
         */
        @Preset.JtStyle.Dword(desc = "拐点经度")
        private long longitude;

        /**
         * 路段宽度
         */
        @Preset.JtStyle.Byte(desc = "路段宽度")
        private short routeWidth;

        /**
         * 路段属性
         * <li>bit[0] --  1：行驶时间</li>
         * <li>bit[1] --  1：限速</li>
         * <li>bit[2] --  0：北纬；1：南纬</li>
         * <li>bit[3] --  0：东经；1：西经</li>
         * <li>bit[4~7] -- 保留</li>
         */
        @Preset.JtStyle.Byte(desc = "路段属性")
        private short routeProps;

        /**
         * 路段行驶过长阈值
         * <p>
         * 单位为秒（s），若路段属性0位为0则没有该字段
         */
        // @Preset.JtStyle.Word(condition = "hasThresholdProperty()", desc = "路段行驶过长阈值")
        @Preset.JtStyle.Word(conditions = @Expression(spel = "hasThresholdProperty()", mvel = "self.hasThresholdProperty()", aviator = "self.hasThresholdProperty"), desc = "路段行驶过长阈值")
        private Integer longDriveThreshold;

        /**
         * 路段行驶不足阈值
         * 单位为秒（s），若路段属性0位为0则没有该字段
         */
        // @Preset.JtStyle.Word(condition = "hasThresholdProperty()", desc = "路段行驶不足阈值")
        @Preset.JtStyle.Word(conditions = @Expression(spel = "hasThresholdProperty()", mvel = "self.hasThresholdProperty()", aviator = "self.hasThresholdProperty"), desc = "路段行驶不足阈值")
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
        // @Preset.JtStyle.Word(condition = "hasSpeedLimitProperty()", desc = "路段最高速度")
        @Preset.JtStyle.Word(conditions = @Expression(spel = "hasSpeedLimitProperty()", mvel = "self.hasSpeedLimitProperty()", aviator = "self.hasSpeedLimitProperty"), desc = "路段最高速度")
        private Integer maxSpeedLimit;

        /**
         * 路段超速持续时间
         * <p>
         * 单位为秒（s），若路段属性1位为0则没有该字段
         */
        // @Preset.JtStyle.Byte(condition = "hasSpeedLimitProperty()", desc = "路段超速持续时间")
        @Preset.JtStyle.Byte(conditions = @Expression(spel = "hasSpeedLimitProperty()", mvel = "self.hasSpeedLimitProperty()", aviator = "self.hasSpeedLimitProperty"), desc = "路段超速持续时间")
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
    }
}
