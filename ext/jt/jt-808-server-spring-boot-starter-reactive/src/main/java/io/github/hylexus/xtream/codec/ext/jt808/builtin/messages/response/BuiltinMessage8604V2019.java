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
 * 设置多边形区域
 *
 * @author hylexus
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Jt808ResponseBody(messageId = 0x8604)
public class BuiltinMessage8604V2019 {

    /**
     * 区域ID
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
     * 起始时间 BCD[6]
     * <p>
     * YY-MM-DD-hh-mm-ss，若区域属性0位为0则没有该字段
     */
    @Preset.JtStyle.BcdDateTime(condition = "hasTimeProperty()")
    private LocalDateTime startTime;

    /**
     * 结束时间 BCD[6]
     * <p>
     * YY-MM-DD-hh-mm-ss，若区域属性0位为0则没有该字段
     */
    @Preset.JtStyle.BcdDateTime(condition = "hasTimeProperty()")
    private LocalDateTime endTime;

    public boolean hasTimeProperty() {
        return Numbers.getBitAt(this.areaProps, 0) == 1;
    }

    /**
     * 最高速度
     * <p>
     * Km/h，若区域属性1位为 0 则没有该字段
     */
    @Preset.JtStyle.Word(condition = "hasSpeedProperty()")
    private int topSpeed;

    public boolean hasSpeedProperty() {
        return Numbers.getBitAt(this.areaProps, 1) == 1;
    }

    /**
     * 超速持续时间
     * <p>
     * 单位为秒（s） （类似表述，同前修改） ，若区域属性1位为0则没有该字段
     */
    @Preset.JtStyle.Byte(condition = "hasSpeedProperty()")
    private short durationOfOverSpeed;

    /**
     * 区域总顶点数
     */
    @Preset.JtStyle.Word
    private int pointCount;

    /**
     * 顶点项
     */
    @Preset.JtStyle.List
    private List<Point> pointList;

    /**
     * 夜间最高速度
     */
    @Preset.JtStyle.Word(condition = "hasSpeedProperty()")
    private int topSpeedAtNight;

    /**
     * 区域名称长度
     */
    @Preset.JtStyle.Word
    private int areaNameLength;

    /**
     * 区域名称
     */
    @Preset.JtStyle.Str
    private String areaName;

    @Getter
    @Setter
    @ToString
    public static class Point {

        @Preset.JtStyle.Dword
        private long latitude;

        @Preset.JtStyle.Dword
        private long longitude;

        public Point() {
        }

        public Point(long latitude, long longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
