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
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设置矩形区域
 *
 * @author hylexus
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Jt808ResponseBody(messageId = 0x8602, desc = "设置矩形区域")
public class BuiltinMessage8602 {

    /**
     * 设置属性
     * <li>0：更新区域</li>
     * <li>1：追加区域</li>
     * <li>2：修改区域</li>
     */
    @Preset.JtStyle.Byte(desc = "设置属性")
    private short type;

    @Preset.JtStyle.Byte(desc = "区域总数")
    private short areaCount;

    @Preset.JtStyle.List(desc = "区域列表")
    private List<RectangularArea> areaList;

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class RectangularArea {

        @Preset.JtStyle.Dword(desc = "区域ID")
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
        @Preset.JtStyle.Word(desc = "区域属性")
        private int areaProps;

        /**
         * 左上点纬度: 以度为单位的纬度值乘以10 的6次方，精确到百万分之一度
         */
        @Preset.JtStyle.Dword(desc = "左上点纬度")
        private long leftTopLatitude;
        /**
         * 左上点经度: 以度为单位的纬度值乘以10 的6次方，精确到百万分之一度
         */
        @Preset.JtStyle.Dword(desc = "左上点经度")
        private long leftTopLongitude;

        /**
         * 右下点纬度: 以度为单位的纬度值乘以10 的6次方，精确到百万分之一度
         */
        @Preset.JtStyle.Dword(desc = "右下点纬度")
        private long rightBottomLatitude;
        /**
         * 右下点经度: 以度为单位的纬度值乘以10 的6次方，精确到百万分之一度
         */
        @Preset.JtStyle.Dword(desc = "右下点经度")
        private long rightBottomLongitude;

        /**
         * 起始时间 BCD[6]
         * <p>
         * YY-MM-DD-hh-mm-ss，若区域属性0位为0则没有该字段
         */
        @Preset.JtStyle.BcdDateTime(condition = "hasTimeProperty()", desc = "起始时间 BCD[6]")
        private LocalDateTime startTime;

        /**
         * 结束时间 BCD[6]
         * <p>
         * YY-MM-DD-hh-mm-ss，若区域属性0位为0则没有该字段
         */
        @Preset.JtStyle.BcdDateTime(condition = "hasTimeProperty()", desc = "结束时间 BCD[6]")
        private LocalDateTime endTime;

        public boolean hasTimeProperty() {
            return Numbers.getBitAt(this.areaProps, 0) == 1;
        }

        /**
         * 最高速度
         * <p>
         * Km/h，若区域属性1位为 0 则没有该字段
         */
        @Preset.JtStyle.Word(condition = "hasSpeedProperty()", desc = "最高速度")
        private int topSpeed;

        public boolean hasSpeedProperty() {
            return Numbers.getBitAt(this.areaProps, 1) == 1;
        }

        /**
         * 超速持续时间
         * <p>
         * 单位为秒（s） （类似表述，同前修改） ，若区域属性1位为0则没有该字段
         */
        @Preset.JtStyle.Byte(condition = "hasSpeedProperty()", desc = "超速持续时间")
        private short durationOfOverSpeed;
    }
}
