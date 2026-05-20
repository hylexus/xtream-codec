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

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * 苏标-表-4-17 驾驶状态监测系统报警信息数据格式
 *
 * @author hylexus
 */
public class LocationItem0x65 {

    // offset[0,4) 报警 ID: 按照报警先后，从0开始循环累加，不区分报警类型。
    @Preset.JtStyle.Dword
    private long alarmId;

    // 0x00：不可用
    // 0x01：开始标志
    // 0x02：结束标志
    // 该字段仅适用于有开始和结束标志类型的报警或事件，报警类型或事件类型无开始和结束标志，则该位不可用，填入0x00即可
    @Preset.JtStyle.Byte
    private short status;

    // offset[5,6) 报警/事件类型
    @Preset.JtStyle.Byte
    private short alarmType;

    // offset[6,7) 报警级别
    @Preset.JtStyle.Byte
    private short alarmLevel;

    // offset[7,8) 疲劳程度 范围1~10。数值越大表示疲劳程度越严重，仅在报警类型为0x01时有效
    @Preset.JtStyle.Byte
    private short fatigueLevel;

    // offset[8,12) BYTE[4] 预留
    @Preset.JtStyle.Bytes(length = 4)
    private byte[] reservedOffset8;

    // offset[12,13) BYTE 车速
    @Preset.JtStyle.Byte
    private short speed;

    // offset[13,15) WORD 高程
    @Preset.JtStyle.Word
    private int height;

    // offset[15,19) DWORD 纬度
    @Preset.JtStyle.Dword
    private long latitude;

    // offset[19,23) DWORD 经度
    @Preset.JtStyle.Dword
    private long longitude;

    // offset[23,29) BCD[6] 日期时间
    @Preset.JtStyle.Bcd(length = 6)
    private String datetime;

    // offset[29,31] WORD 车辆状态
    @Preset.JtStyle.Word
    private int vehicleStatus;
    // offset[31,31+16) BYTE[16] 报警标识号
    // 报警识别号定义见表4-16
    @Preset.JtStyle.Object
    private AlarmIdentifier alarmIdentifier;

    public long getAlarmId() {
        return alarmId;
    }

    public LocationItem0x65 setAlarmId(long alarmId) {
        this.alarmId = alarmId;
        return this;
    }

    public short getStatus() {
        return status;
    }

    public LocationItem0x65 setStatus(short status) {
        this.status = status;
        return this;
    }

    public short getAlarmType() {
        return alarmType;
    }

    public LocationItem0x65 setAlarmType(short alarmType) {
        this.alarmType = alarmType;
        return this;
    }

    public short getAlarmLevel() {
        return alarmLevel;
    }

    public LocationItem0x65 setAlarmLevel(short alarmLevel) {
        this.alarmLevel = alarmLevel;
        return this;
    }

    public short getFatigueLevel() {
        return fatigueLevel;
    }

    public LocationItem0x65 setFatigueLevel(short fatigueLevel) {
        this.fatigueLevel = fatigueLevel;
        return this;
    }

    public byte[] getReservedOffset8() {
        return reservedOffset8;
    }

    public LocationItem0x65 setReservedOffset8(byte[] reservedOffset8) {
        this.reservedOffset8 = reservedOffset8;
        return this;
    }

    public short getSpeed() {
        return speed;
    }

    public LocationItem0x65 setSpeed(short speed) {
        this.speed = speed;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public LocationItem0x65 setHeight(int height) {
        this.height = height;
        return this;
    }

    public long getLatitude() {
        return latitude;
    }

    public LocationItem0x65 setLatitude(long latitude) {
        this.latitude = latitude;
        return this;
    }

    public long getLongitude() {
        return longitude;
    }

    public LocationItem0x65 setLongitude(long longitude) {
        this.longitude = longitude;
        return this;
    }

    public String getDatetime() {
        return datetime;
    }

    public LocationItem0x65 setDatetime(String datetime) {
        this.datetime = datetime;
        return this;
    }

    public int getVehicleStatus() {
        return vehicleStatus;
    }

    public LocationItem0x65 setVehicleStatus(int vehicleStatus) {
        this.vehicleStatus = vehicleStatus;
        return this;
    }

    public AlarmIdentifier getAlarmIdentifier() {
        return alarmIdentifier;
    }

    public LocationItem0x65 setAlarmIdentifier(AlarmIdentifier alarmIdentifier) {
        this.alarmIdentifier = alarmIdentifier;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LocationItem0x65.class.getSimpleName() + "[", "]")
                .add("alarmId=" + alarmId)
                .add("status=" + status)
                .add("alarmType=" + alarmType)
                .add("alarmLevel=" + alarmLevel)
                .add("fatigueLevel=" + fatigueLevel)
                .add("reservedOffset8=" + Arrays.toString(reservedOffset8))
                .add("speed=" + speed)
                .add("height=" + height)
                .add("latitude=" + latitude)
                .add("longitude=" + longitude)
                .add("datetime='" + datetime + "'")
                .add("vehicleStatus=" + vehicleStatus)
                .add("alarmIdentifier=" + alarmIdentifier)
                .toString();
    }
}
