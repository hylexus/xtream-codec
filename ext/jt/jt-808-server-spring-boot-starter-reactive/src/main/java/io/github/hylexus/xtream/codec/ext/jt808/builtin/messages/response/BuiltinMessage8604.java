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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hylexus.xtream.codec.common.utils.Numbers;
import io.github.hylexus.xtream.codec.core.annotation.Expression;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.StringJoiner;

/**
 * 设置多边形区域
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8604, desc = "设置多边形区域")
public class BuiltinMessage8604 {

    @Preset.JtStyle.Dword(desc = "区域 ID")
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
     * 起始时间 BCD[6]
     * <p>
     * YY-MM-DD-hh-mm-ss，若区域属性0位为0则没有该字段
     */
    // @Preset.JtStyle.BcdDateTime(condition = "hasTimeProperty()", desc = "起始时间 BCD[6]")
    @Preset.JtStyle.BcdDateTime(
            conditions = @Expression(
                    spel = "hasTimeProperty()",
                    mvel = "self.hasTimeProperty()",
                    aviator = "self.hasTimeProperty"
            ),
            desc = "起始时间 BCD[6]"
    )
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间 BCD[6]
     * <p>
     * YY-MM-DD-hh-mm-ss，若区域属性0位为0则没有该字段
     */
    // @Preset.JtStyle.BcdDateTime(condition = "hasTimeProperty()", desc = "结束时间 BCD[6]")
    @Preset.JtStyle.BcdDateTime(
            conditions = @Expression(
                    spel = "hasTimeProperty()",
                    mvel = "self.hasTimeProperty()",
                    aviator = "self.hasTimeProperty"
            ),
            desc = "结束时间 BCD[6]"
    )
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    public boolean hasTimeProperty() {
        return Numbers.getBitAt(this.areaProps, 0) == 1;
    }

    // for Aviator
    public boolean isHasTimeProperty() {
        return Numbers.getBitAt(this.areaProps, 0) == 1;
    }

    /**
     * 最高速度
     * <p>
     * Km/h，若区域属性1位为 0 则没有该字段
     */
    // @Preset.JtStyle.Word(condition = "hasSpeedProperty()", desc = "最高速度")
    @Preset.JtStyle.Word(
            conditions = @Expression(
                    spel = "hasSpeedProperty()",
                    mvel = "self.hasSpeedProperty()",
                    aviator = "self.hasSpeedProperty"
            ),
            desc = "最高速度"
    )
    private int topSpeed;

    public boolean hasSpeedProperty() {
        return Numbers.getBitAt(this.areaProps, 1) == 1;
    }

    // for Aviator
    public boolean isHasSpeedProperty() {
        return Numbers.getBitAt(this.areaProps, 1) == 1;
    }

    /**
     * 超速持续时间
     * <p>
     * 单位为秒（s） （类似表述，同前修改） ，若区域属性1位为0则没有该字段
     */
    // @Preset.JtStyle.Byte(condition = "hasSpeedProperty()", desc = "超速持续时间")
    @Preset.JtStyle.Byte(
            conditions = @Expression(
                    spel = "hasSpeedProperty()",
                    mvel = "self.hasSpeedProperty()",
                    aviator = "self.hasSpeedProperty"
            ),
            desc = "超速持续时间"
    )
    private short durationOfOverSpeed;

    @Preset.JtStyle.Word(desc = "区域总顶点数")
    private int pointCount;

    @Preset.JtStyle.List(desc = "顶点项")
    private List<Point> pointList;

    public long getAreaId() {
        return areaId;
    }

    public BuiltinMessage8604 setAreaId(long areaId) {
        this.areaId = areaId;
        return this;
    }

    public int getAreaProps() {
        return areaProps;
    }

    public BuiltinMessage8604 setAreaProps(int areaProps) {
        this.areaProps = areaProps;
        return this;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public BuiltinMessage8604 setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public BuiltinMessage8604 setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public int getTopSpeed() {
        return topSpeed;
    }

    public BuiltinMessage8604 setTopSpeed(int topSpeed) {
        this.topSpeed = topSpeed;
        return this;
    }

    public short getDurationOfOverSpeed() {
        return durationOfOverSpeed;
    }

    public BuiltinMessage8604 setDurationOfOverSpeed(short durationOfOverSpeed) {
        this.durationOfOverSpeed = durationOfOverSpeed;
        return this;
    }

    public int getPointCount() {
        return pointCount;
    }

    public BuiltinMessage8604 setPointCount(int pointCount) {
        this.pointCount = pointCount;
        return this;
    }

    public List<Point> getPointList() {
        return pointList;
    }

    public BuiltinMessage8604 setPointList(List<Point> pointList) {
        this.pointList = pointList;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8604.class.getSimpleName() + "[", "]")
                .add("areaId=" + areaId)
                .add("areaProps=" + areaProps)
                .add("startTime=" + startTime)
                .add("endTime=" + endTime)
                .add("topSpeed=" + topSpeed)
                .add("durationOfOverSpeed=" + durationOfOverSpeed)
                .add("pointCount=" + pointCount)
                .add("pointList=" + pointList)
                .toString();
    }

    public static class Point {

        @Preset.JtStyle.Dword(desc = "顶点纬度")
        private long latitude;

        @Preset.JtStyle.Dword(desc = "顶点经度")
        private long longitude;

        public Point() {
        }

        public Point(long latitude, long longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public long getLatitude() {
            return latitude;
        }

        public Point setLatitude(long latitude) {
            this.latitude = latitude;
            return this;
        }

        public long getLongitude() {
            return longitude;
        }

        public Point setLongitude(long longitude) {
            this.longitude = longitude;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Point.class.getSimpleName() + "[", "]")
                    .add("latitude=" + latitude)
                    .add("longitude=" + longitude)
                    .toString();
        }
    }
}
