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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location;

import io.github.hylexus.xtream.codec.core.type.Preset;

import java.time.LocalDateTime;
import java.util.List;
import java.util.StringJoiner;

/**
 * 苏标-表-4-18 胎压监测系统报警信息数据格式
 *
 * @author hylexus
 */
public class LocationItem0x66 {

    // offset[0,4) DWORD 报警 ID: 按照报警先后，从0开始循环累加，不区分报警类型。
    @Preset.JtStyle.Dword
    private long alarmId;

    // offset[4,5) BYTE 标志状态
    // 0x00：不可用
    // 0x01：开始标志
    // 0x02：结束标志
    // 该字段仅适用于有开始和结束标志类型的报警或事件，报警类型或事件类型无开始和结束标志，则该位不可用，填入0x00即可
    @Preset.JtStyle.Byte
    private short status;

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
    @Preset.JtStyle.BcdDateTime
    // @XtreamDateTimeField(pattern = "yyMMddHHmmss", length = 6, charset = XtreamConstants.CHARSET_NAME_BCD_8421)
    private LocalDateTime datetime;

    // offset[29,31] WORD 车辆状态
    @Preset.JtStyle.Word
    private int vehicleStatus;
    // offset[31,31+16) BYTE[16] 报警标识号
    // 报警识别号定义见表4-16
    @Preset.JtStyle.Object
    private AlarmIdentifier alarmIdentifier;

    // offset[39, 40) BYTE 报警/事件列表总数
    @Preset.JtStyle.Byte
    private short eventItemCount;

    // 报警/事件信息列表
    @Preset.JtStyle.List(condition = "getEventItemCount() > 0")
    private List<EventItem> eventItemList;

    public long getAlarmId() {
        return alarmId;
    }

    public LocationItem0x66 setAlarmId(long alarmId) {
        this.alarmId = alarmId;
        return this;
    }

    public short getStatus() {
        return status;
    }

    public LocationItem0x66 setStatus(short status) {
        this.status = status;
        return this;
    }

    public short getSpeed() {
        return speed;
    }

    public LocationItem0x66 setSpeed(short speed) {
        this.speed = speed;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public LocationItem0x66 setHeight(int height) {
        this.height = height;
        return this;
    }

    public long getLatitude() {
        return latitude;
    }

    public LocationItem0x66 setLatitude(long latitude) {
        this.latitude = latitude;
        return this;
    }

    public long getLongitude() {
        return longitude;
    }

    public LocationItem0x66 setLongitude(long longitude) {
        this.longitude = longitude;
        return this;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public LocationItem0x66 setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
        return this;
    }

    public int getVehicleStatus() {
        return vehicleStatus;
    }

    public LocationItem0x66 setVehicleStatus(int vehicleStatus) {
        this.vehicleStatus = vehicleStatus;
        return this;
    }

    public AlarmIdentifier getAlarmIdentifier() {
        return alarmIdentifier;
    }

    public LocationItem0x66 setAlarmIdentifier(AlarmIdentifier alarmIdentifier) {
        this.alarmIdentifier = alarmIdentifier;
        return this;
    }

    public short getEventItemCount() {
        return eventItemCount;
    }

    public LocationItem0x66 setEventItemCount(short eventItemCount) {
        this.eventItemCount = eventItemCount;
        return this;
    }

    public List<EventItem> getEventItemList() {
        return eventItemList;
    }

    public LocationItem0x66 setEventItemList(List<EventItem> eventItemList) {
        this.eventItemList = eventItemList;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LocationItem0x66.class.getSimpleName() + "[", "]")
                .add("alarmId=" + alarmId)
                .add("status=" + status)
                .add("speed=" + speed)
                .add("height=" + height)
                .add("latitude=" + latitude)
                .add("longitude=" + longitude)
                .add("datetime=" + datetime)
                .add("vehicleStatus=" + vehicleStatus)
                .add("alarmIdentifier=" + alarmIdentifier)
                .add("eventItemCount=" + eventItemCount)
                .add("eventItemList=" + eventItemList)
                .toString();
    }

    public static class EventItem {
        // 胎压报警位置 BYTE 报警轮胎位置编号（从左前轮开始以Z字形从00依次编号，编号与是否安装TPMS无关）
        @Preset.JtStyle.Byte
        private short offset0;

        // 报警/事件类型 WORD 0表示无报警，1表示有报警
        // bit0：胎压（定时上报）
        // bit1：胎压过高报警
        // bit2：胎压过低报警
        // bit3：胎温过高报警
        // bit4：传感器异常报警
        // bit5：胎压不平衡报警
        // bit6：慢漏气报警
        // bit7：电池电量低报警
        // bit8~bit15：自定义
        @Preset.JtStyle.Word
        private int offset2;

        // 胎压 WORD 单位 Kpa
        @Preset.JtStyle.Word
        private int offset4;

        // 胎温 WORD 单位 ℃
        @Preset.JtStyle.Word
        private int offset6;

        // 电池电量 WORD 单位 %
        @Preset.JtStyle.Word
        private int offset8;

        public short getOffset0() {
            return offset0;
        }

        public EventItem setOffset0(short offset0) {
            this.offset0 = offset0;
            return this;
        }

        public int getOffset2() {
            return offset2;
        }

        public EventItem setOffset2(int offset2) {
            this.offset2 = offset2;
            return this;
        }

        public int getOffset4() {
            return offset4;
        }

        public EventItem setOffset4(int offset4) {
            this.offset4 = offset4;
            return this;
        }

        public int getOffset6() {
            return offset6;
        }

        public EventItem setOffset6(int offset6) {
            this.offset6 = offset6;
            return this;
        }

        public int getOffset8() {
            return offset8;
        }

        public EventItem setOffset8(int offset8) {
            this.offset8 = offset8;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", EventItem.class.getSimpleName() + "[", "]")
                    .add("offset0=" + offset0)
                    .add("offset2=" + offset2)
                    .add("offset4=" + offset4)
                    .add("offset6=" + offset6)
                    .add("offset8=" + offset8)
                    .toString();
        }
    }
}
