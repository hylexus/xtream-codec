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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设置圆形区域
 * <p>
 * 注：本条消息协议支持周期时间范围，如要限制每天的8:30-18:00，起始/结束时间设为：00-00-00-08-30-00/00-00-00-18-00-00，其他以此类推
 *
 * @author hylexus
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Jt808ResponseBody(messageId = 0x8600)
public class BuiltinMessage8600V2019 {

    /**
     * 设置属性
     * <li>0：更新区域</li>
     * <li>1：追加区域</li>
     * <li>2：修改区域</li>
     */
    @Preset.JtStyle.Byte
    private short type;

    /**
     * 区域总数
     */
    @Preset.JtStyle.Byte
    private short areaCount;

    // @Preset.JtStyle.List(iterationTimesExpression = "getAreaCount()")
    @Preset.JtStyle.List(iterationTimesExpressions = @Expression(spel = "getAreaCount()", mvel = "self.getAreaCount()", aviator = "self.areaCount"))
    private List<CircularArea> areaList;

    @Getter
    @Setter
    @Accessors(chain = true)
    @ToString
    public static class CircularArea {
        /**
         * 区域 ID
         */
        @Preset.JtStyle.Dword
        private long areaId;

        /**
         * 区域属性
         * <li>bit[0] - 1：根据时间</li>
         * <li>bit[1] - 1：限速</li>
         * <li>bit[2] - 1：进区域报警给驾驶员</li>
         * <li>bit[3] - 1：进区域报警给平台</li>
         * <li>bit[4] - 1：出区域报警给驾驶员</li>
         * <li>bit[5] - 1：出区域报警给平台</li>
         * <li>bit[6] - 0：北纬；1：南纬</li>
         * <li>bit[7] - 0：东经；1：西经</li>
         * <li>bit[8] - 0：允许开门；1：禁止开门</li>
         * <li>bit[9~13] - 保留</li>
         * <li>bit[14] - 0：进区域开启通信模块；1：进区域关闭通信模块</li>
         * <li>bit[15] - 0：进区域不采集GNSS详细定位数据；1：进区域采集GNSS 详细定位数据</li>
         */
        @Preset.JtStyle.Word
        private int areaProps;

        /**
         * 中心点纬度
         * <p>
         * 以度为单位的纬度值乘以10 的6次方，精确到百万分之一度
         */
        @Preset.JtStyle.Dword
        private long latitude;

        /**
         * 中心点经度
         * <p>
         * 以度为单位的纬度值乘以10 的6次方，精确到百万分之一度
         */
        @Preset.JtStyle.Dword
        private long longitude;

        /**
         * 半径
         * <p>
         * 单位为米(m)，路段为该拐点到下一拐点
         */
        @Preset.JtStyle.Dword
        private long radius;

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
            return Numbers.getBitAt(this.areaProps, 0) == 1;
        }

        // for Aviator
        @SuppressWarnings("unused")
        public boolean isHasTimeProperty() {
            return Numbers.getBitAt(this.areaProps, 0) == 1;
        }

        /**
         * 最高速度
         * <p>
         * Km/h，若区域属性1位为 0 则没有该字段
         */
        // @Preset.JtStyle.Word(condition = "hasSpeedProperty()")
        @Preset.JtStyle.Word(conditions = @Expression(spel = "hasSpeedProperty()", mvel = "self.hasSpeedProperty()", aviator = "self.hasSpeedProperty"))
        private int topSpeed;

        @SuppressWarnings("unused")
        public boolean hasSpeedProperty() {
            return Numbers.getBitAt(this.areaProps, 1) == 1;
        }

        // for Aviator
        @SuppressWarnings("unused")
        public boolean isHasSpeedProperty() {
            return Numbers.getBitAt(this.areaProps, 1) == 1;
        }

        /**
         * 超速持续时间
         * <p>
         * 单位为秒（s） （类似表述，同前修改） ，若区域属性1位为0则没有该字段
         */
        // @Preset.JtStyle.Byte(condition = "hasSpeedProperty()")
        @Preset.JtStyle.Byte(conditions = @Expression(spel = "hasSpeedProperty()", mvel = "self.hasSpeedProperty()", aviator = "self.hasSpeedProperty"))
        private short durationOfOverSpeed;

        /**
         * 夜间最高速度
         */
        // @Preset.JtStyle.Word(condition = "hasSpeedProperty()")
        @Preset.JtStyle.Word(conditions = @Expression(spel = "hasSpeedProperty()", mvel = "self.hasSpeedProperty()", aviator = "self.hasSpeedProperty"))
        private int topSpeedAtNight;

        /**
         * 区域名称长度
         */
        @Preset.JtStyle.Word
        private int areaNameLength;

        /**
         * 区域名称
         */
        // @Preset.JtStyle.Str(lengthExpression = "getAreaNameLength()")
        @Preset.JtStyle.Str(lengthExpressions = @Expression(spel = "getAreaNameLength()", mvel = "self.getAreaNameLength()", aviator = "self.areaNameLength"))
        private String areaName;

        @SuppressWarnings("unused")
        public int getAreaNameLength() {
            return areaNameLength;
        }
    }
}
