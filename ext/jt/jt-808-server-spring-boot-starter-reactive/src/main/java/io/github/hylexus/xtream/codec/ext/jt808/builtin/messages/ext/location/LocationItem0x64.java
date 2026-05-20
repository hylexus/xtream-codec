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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location;

import io.github.hylexus.xtream.codec.core.type.Preset;

import java.time.LocalDateTime;
import java.util.StringJoiner;

/**
 * 苏标-表-4-15 高级驾驶辅助报警信息数据格式
 *
 * @author hylexus
 */
public class LocationItem0x64 {

    // offset[0,4) DWORD 报警 ID: 按照报警先后，从0开始循环累加，不区分报警类型。
    @Preset.JtStyle.Dword(desc = "报警 ID: 按照报警先后，从0开始循环累加，不区分报警类型")
    private long alarmId;

    // offset[4,5) BYTE 标志状态
    // 0x00：不可用
    // 0x01：开始标志
    // 0x02：结束标志
    // 该字段仅适用于有开始和结束标志类型的报警或事件，报警类型或事件类型无开始和结束标志，则该位不可用，填入0x00即可。
    @Preset.JtStyle.Byte(desc = "标志状态")
    private short status;

    // offset[5,6) BYTE 报警/事件类型
    @Preset.JtStyle.Byte(desc = "报警/事件类型")
    private short alarmType;

    // offset[6,7) BYTE 报警级别
    @Preset.JtStyle.Byte(desc = "报警级别")
    private short alarmLevel;

    // offset[7,8) BYTE 前车车速
    // Front vehicle speed
    @Preset.JtStyle.Byte(desc = "前车车速")
    private short speedOfFrontObject;

    // offset[8,9) BYTE 前车/行人距离
    @Preset.JtStyle.Byte(desc = "前车/行人距离")
    private short distanceToFrontObject;

    // offset[9,10) BYTE 偏离类型
    @Preset.JtStyle.Byte(desc = "偏离类型")
    private short deviationType;

    // offset[10,11) BYTE 道路标志识别类型
    @Preset.JtStyle.Byte(desc = "道路标志识别类型")
    private short roadSignType;

    // offset[11,12) BYTE 道路标志识别数据
    @Preset.JtStyle.Byte(desc = "道路标志识别数据")
    private short roadSignData;

    // offset[12,13) BYTE 车速
    @Preset.JtStyle.Byte(desc = "车速")
    private short speed;

    // offset[13,15) WORD 高程
    @Preset.JtStyle.Word(desc = "高程")
    private int height;

    // offset[15,19) DWORD 纬度
    @Preset.JtStyle.Dword(desc = "纬度")
    private long latitude;

    // offset[19,23) DWORD 经度
    @Preset.JtStyle.Dword(desc = "经度")
    private long longitude;

    // offset[23,29) BCD[6] 日期时间
    @Preset.JtStyle.BcdDateTime(desc = "日期时间")
    private LocalDateTime datetime;

    // offset[29,31] WORD 车辆状态
    @Preset.JtStyle.Word(desc = "车辆状态")
    private int vehicleStatus;
    // offset[31,31+16) BYTE[16] 报警标识号
    // 报警识别号定义见表4-16
    @Preset.JtStyle.Object(length = 16, desc = "报警标识号")
    private AlarmIdentifier alarmIdentifier;

    public long getAlarmId() {
        return alarmId;
    }

    public LocationItem0x64 setAlarmId(long alarmId) {
        this.alarmId = alarmId;
        return this;
    }

    public short getStatus() {
        return status;
    }

    public LocationItem0x64 setStatus(short status) {
        this.status = status;
        return this;
    }

    public short getAlarmType() {
        return alarmType;
    }

    public LocationItem0x64 setAlarmType(short alarmType) {
        this.alarmType = alarmType;
        return this;
    }

    public short getAlarmLevel() {
        return alarmLevel;
    }

    public LocationItem0x64 setAlarmLevel(short alarmLevel) {
        this.alarmLevel = alarmLevel;
        return this;
    }

    public short getSpeedOfFrontObject() {
        return speedOfFrontObject;
    }

    public LocationItem0x64 setSpeedOfFrontObject(short speedOfFrontObject) {
        this.speedOfFrontObject = speedOfFrontObject;
        return this;
    }

    public short getDistanceToFrontObject() {
        return distanceToFrontObject;
    }

    public LocationItem0x64 setDistanceToFrontObject(short distanceToFrontObject) {
        this.distanceToFrontObject = distanceToFrontObject;
        return this;
    }

    public short getDeviationType() {
        return deviationType;
    }

    public LocationItem0x64 setDeviationType(short deviationType) {
        this.deviationType = deviationType;
        return this;
    }

    public short getRoadSignType() {
        return roadSignType;
    }

    public LocationItem0x64 setRoadSignType(short roadSignType) {
        this.roadSignType = roadSignType;
        return this;
    }

    public short getRoadSignData() {
        return roadSignData;
    }

    public LocationItem0x64 setRoadSignData(short roadSignData) {
        this.roadSignData = roadSignData;
        return this;
    }

    public short getSpeed() {
        return speed;
    }

    public LocationItem0x64 setSpeed(short speed) {
        this.speed = speed;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public LocationItem0x64 setHeight(int height) {
        this.height = height;
        return this;
    }

    public long getLatitude() {
        return latitude;
    }

    public LocationItem0x64 setLatitude(long latitude) {
        this.latitude = latitude;
        return this;
    }

    public long getLongitude() {
        return longitude;
    }

    public LocationItem0x64 setLongitude(long longitude) {
        this.longitude = longitude;
        return this;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public LocationItem0x64 setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
        return this;
    }

    public int getVehicleStatus() {
        return vehicleStatus;
    }

    public LocationItem0x64 setVehicleStatus(int vehicleStatus) {
        this.vehicleStatus = vehicleStatus;
        return this;
    }

    public AlarmIdentifier getAlarmIdentifier() {
        return alarmIdentifier;
    }

    public LocationItem0x64 setAlarmIdentifier(AlarmIdentifier alarmIdentifier) {
        this.alarmIdentifier = alarmIdentifier;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LocationItem0x64.class.getSimpleName() + "[", "]")
                .add("alarmId=" + alarmId)
                .add("status=" + status)
                .add("alarmType=" + alarmType)
                .add("alarmLevel=" + alarmLevel)
                .add("speedOfFrontObject=" + speedOfFrontObject)
                .add("distanceToFrontObject=" + distanceToFrontObject)
                .add("deviationType=" + deviationType)
                .add("roadSignType=" + roadSignType)
                .add("roadSignData=" + roadSignData)
                .add("speed=" + speed)
                .add("height=" + height)
                .add("latitude=" + latitude)
                .add("longitude=" + longitude)
                .add("datetime=" + datetime)
                .add("vehicleStatus=" + vehicleStatus)
                .add("alarmIdentifier=" + alarmIdentifier)
                .toString();
    }
}
