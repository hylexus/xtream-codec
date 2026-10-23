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

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hylexus.xtream.codec.core.annotation.DerivedField;
import io.github.hylexus.xtream.codec.core.annotation.Expression;
import io.github.hylexus.xtream.codec.core.annotation.ext.*;
import io.github.hylexus.xtream.codec.core.impl.codec.StringFieldCodecs;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.BuiltinMessage0200Support.*;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.LocationItem0x64;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.LocationItem0x65;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.LocationItem0x66;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.LocationItem0x67;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import org.jspecify.annotations.Nullable;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import static io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField.*;
import static io.github.hylexus.xtream.codec.core.type.Preset.JtStyle;
import static io.github.hylexus.xtream.codec.core.type.XtreamDataType.*;

/**
 * 位置信息汇报 0x0200
 *
 * @author hylexus
 * @author opencode (AI)
 */
@Jt808ResponseBody(messageId = 0x0200, desc = "位置信息汇报")
public class BuiltinMessage0200 {

    /// # 表25 报警预警标志位定义
    ///
    /// | 位   | 定义                                     | 处理说明                 |
    /// | :--- | :--------------------------------------- | :----------------------- |
    /// | 0    | 1:紧急报警,触动报警开关后触发            | 收到应答后清零           |
    /// | 1    | 1:超速报警                               | 标志维持至报警条件解除   |
    /// | 2    | 1:疲劳驾驶报警                           | 标志维持至报警条件解除   |
    /// | 3    | 1:危险驾驶行为报警                       | 标志维持至报警条件解除   |
    /// | 4    | 1:GNSS 模块发生故障报警                  | 标志维持至报警条件解除   |
    /// | 5    | 1:GNSS 天线未接或被剪断报警              | 标志维持至报警条件解除   |
    /// | 6    | 1:GNSS 天线短路报警                      | 标志维持至报警条件解除   |
    /// | 7    | 1:终端主电源欠压报警                     | 标志维持至报警条件解除   |
    /// | 8    | 1:终端主电源掉电报警                     | 标志维持至报警条件解除   |
    /// | 9    | 1:终端 LCD 或显示器故障报警              | 标志维持至报警条件解除   |
    /// | 10   | 1:TTS 模块故障报警                       | 标志维持至报警条件解除   |
    /// | 11   | 1:摄像头故障报警                         | 标志维持至报警条件解除   |
    /// | 12   | 1:道路运输证 IC 卡模块故障报警           | 标志维持至报警条件解除   |
    /// | 13   | 1:超速预警                               | 标志维持至预警条件解除   |
    /// | 14   | 1:疲劳驾驶预警                           | 标志维持至预警条件解除   |
    /// | 15   | 1:违规行驶报警                           | 标志维持至报警条件解除   |
    /// | 16   | 1:胎压预警                               | 标志维持至预警条件解除   |
    /// | 17   | 1:右转盲区异常报警                       | 标志维持至报警条件解除   |
    /// | 18   | 1:当天累计驾驶超时报警                   | 标志维持至报警条件解除   |
    /// | 19   | 1:超时停车报警                           | 标志维持至报警条件解除   |
    /// | 20   | 1:进出区域报警                           | 收到应答后清零           |
    /// | 21   | 1:进出路线报警                           | 收到应答后清零           |
    /// | 22   | 1:路段行驶时间不足/过长报警              | 收到应答后清零           |
    /// | 23   | 1:路线偏离报警                           | 标志维持至报警条件解除   |
    /// | 24   | 1:车辆 VSS 故障                          | 标志维持至报警条件解除   |
    /// | 25   | 1:车辆油量异常报警                       | 标志维持至报警条件解除   |
    /// | 26   | 1:车辆被盗报警(通过车辆防盗器)           | 标志维持至报警条件解除   |
    /// | 27   | 1:车辆非法点火报警                       | 收到应答后清零           |
    /// | 28   | 1:车辆非法位移报警                       | 收到应答后清零           |
    /// | 29   | 1:碰撞侧翻报警                           | 标志维持至报警条件解除   |
    /// | 30   | 1:侧翻预警                               | 标志维持至预警条件解除   |
    /// | 31   | 保留                                     | —                        |
    @Preset.JtStyle.Dword(desc = "报警标志")
    private long alarmFlag;

    /// # 表24 状态位定义
    ///
    /// | 位       | 状态                                                                 |
    /// |----------|----------------------------------------------------------------------|
    /// | 0        | 0:ACC 关;1: ACC 开                                                   |
    /// | 1        | 0:未定位;1:定位                                                      |
    /// | 2        | 0:北纬;1:南纬                                                        |
    /// | 3        | 0:东经;1:西经                                                        |
    /// | 4        | 0:运营状态;1:停运状态                                                |
    /// | 5        | 0:经纬度未经保密插件加密;1:经纬度已经保密插件加密                     |
    /// | 6        | 1:紧急刹车系统采集的前撞预警                                         |
    /// | 7        | 1:车道偏移预警                                                       |
    /// | 8 ~ 9    | 00:空车;01:半载;10:保留;11:满载。<br>可表示客车的空载状态,重车及货车的空载、满载状态,该状态可由人工输入或传感器获取 |
    /// | 10       | 0:车辆油路正常;1:车辆油路断开                                        |
    /// | 11       | 0:车辆电路正常;1:车辆电路断开                                        |
    /// | 12       | 0:车门解锁;1:车门加锁                                                |
    /// | 13       | 0:门 1 关;1:门 1 开(前门)                                            |
    /// | 14       | 0:门 2 关;1:门 2 开(中门)                                            |
    /// | 15       | 0:门 3 关;1:门 3 开(后门)                                            |
    /// | 16       | 0:门 4 关;1:门 4 开(驾驶席门)                                        |
    /// | 17       | 0:门 5 关;1:门 5 开(自定义)                                          |
    /// | 18       | 0:未使用 GPS 卫星进行定位;1:使用 GPS 卫星进行定位                    |
    /// | 19       | 0:未使用北斗卫星进行定位;1:使用北斗卫星进行定位                      |
    /// | 20       | 0:未使用 GLONASS 卫星进行定位;1:使用 GLONASS 卫星进行定位            |
    /// | 21       | 0:未使用 Galileo 卫星进行定位;1:使用 Galileo 卫星进行定位            |
    /// | 22       | 0:车辆处于停止状态;1:车辆处于行驶状态                                |
    /// | 23 ~ 31  | 保留                                                                 |
    @Preset.JtStyle.Dword(desc = "状态")
    private long status;

    // ========== @DerivedField 使用示例 ==========

    /// 「可选」示例1: EnumSetBitTransformer + reverseSource=true（回环编解码）
    ///
    /// alarmFlag 的 bit 位 → EnumSet<AlarmFlag>，编码时通过 write() 回写 alarmFlag
    @DerivedField(source = "alarmFlag", using = AlarmFlagTransformer.class, reverseSource = true)
    private transient @Nullable Set<AlarmFlag> alarmFlags;

    /// 「可选」示例2: 自定义 Transformer + reverseSource=false（只读衍生，不参与编码回写）
    ///
    /// alarmFlag → 人类可读的报警描述字符串，仅解码时计算，不影响编码
    @DerivedField(source = "alarmFlag", using = AlarmDescriptionTransformer.class, reverseSource = false)
    private transient @Nullable String alarmDescription;

    /// 「可选」示例3: EnumSetBitTransformer + reverseSource=true（回环编解码）
    ///
    /// status 的全部 bit（含独立标志 + `bit 8~9` 载货状态）→ EnumSet<StatusBit>，
    ///
    /// 编码时通过 write() 按位 OR 回写 status，不同 bitOffset 互不干扰
    @DerivedField(source = "status", using = StatusBitTransformer.class, reverseSource = true)
    private transient @Nullable Set<StatusBit> statusFlags;

    @Preset.JtStyle.Dword(desc = "纬度")
    private long latitude;

    @Preset.JtStyle.Dword(desc = "经度")
    private long longitude;

    @Preset.JtStyle.Word(desc = "高程")
    private int altitude;

    @Preset.JtStyle.Word(desc = "速度")
    private int speed;

    @Preset.JtStyle.Word(desc = "方向")
    private int direction;

    // 时间  BCD[6] yyMMddHHmmss
    @Preset.JtStyle.BcdDateTime(desc = "时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    /**
     * @param locationType 位置类型
     * @param locationId   区域或路段ID; 若位置类型为0，无该字段
     */
    public record Item0x11(
            @JtStyle.Byte short locationType,
            // @JtStyle.Dword(condition = "#locationType != 0") @Nullable Long locationId,
            @JtStyle.Dword(conditions = @Expression(spel = "locationType != 0", mvel = "self.locationType != 0", aviator = "self.locationType != 0")) @Nullable Long locationId) {

        // for Aviator
        @SuppressWarnings("unused")
        public short getLocationType() {
            return locationType;
        }
    }

    /**
     * @param locationType 位置类型
     * @param locationId   区域或路段 ID
     * @param direction    方向；0：进；1：出
     */
    public record Item0x12(
            @Preset.JtStyle.Byte short locationType,
            @Preset.JtStyle.Dword long locationId,
            @Preset.JtStyle.Byte short direction) {
    }

    /**
     * @param lineId     路线 ID
     * @param locationId 路段行驶时间（秒）
     * @param result     结果。0：不足；1：过长
     */
    public record Item0x13(
            @Preset.JtStyle.Dword long lineId,
            @Preset.JtStyle.Word int locationId,
            @Preset.JtStyle.Byte short result) {
    }

    // 长度：消息体长度减去前面的 28 字节(未指定长度时读取后续所有字节)
    @Preset.JtStyle.SimpleMap(
            desc = "附加项列表",
            key = @Key(type = KeyType.u8),
            valueLength = @ValueLength(type = LengthFieldType.u8),
            value = @Value(
                    // 位置汇报消息一般不会在服务端编码
                    // 所以这个 encoder 可以删掉(编码时才需要, 解码时用 decoder 属性)
                    encoder = @ValueEncoder(
                            params = {@EncoderParam(charset = "GBK")},
                            matchers = {
                                    @ValueMatcher(matchU8 = 0x01, valueType = u32, desc = "里程，DWORD，1/10km，对应车上里程表读数"),
                                    @ValueMatcher(matchU8 = 0x02, valueType = u16, desc = "油量，WORD，1/10L，对应车上油量表读数"),
                                    @ValueMatcher(matchU8 = 0x03, valueType = u16, desc = "行驶记录功能获取的速度，WORD，1/10km/h"),
                                    @ValueMatcher(matchU8 = 0x04, valueType = u16, desc = "需要人工确认报警事件的 ID，WORD，从 1 始计数"),
                                    @ValueMatcher(matchU8 = 0x05, valueType = byte_array, desc = "胎压"),
                                    @ValueMatcher(matchU8 = 0x06, valueType = i16_as_int, desc = "车厢温度"),
                                    @ValueMatcher(matchU8 = 0x11, valueEntity = Item0x11.class, desc = "长度1或5；超速报警附加信息见 表 28"),
                                    @ValueMatcher(matchU8 = 0x25, valueType = u32, desc = "扩展车辆信号状态位，定义见 表 31"),
                                    @ValueMatcher(matchU8 = 0x2A, valueType = u16, desc = "IO 状态位，表 32"),
                                    @ValueMatcher(matchU8 = 0x2B, valueType = i32, desc = "模拟量，bit0-15,AD0,bit16-31,AD1"),
                                    @ValueMatcher(matchU8 = 0x30, valueType = u8, desc = "数据类型为 BYTE，无线通信网络信号强度"),
                                    @ValueMatcher(matchU8 = 0x31, valueType = u8, desc = "数据类型为 BYTE，GNSS定位卫星数"),
                            }
                    ),
                    decoder = @ValueDecoder(
                            params = {@ValueDecoderCommonParam(charset = "GBK")},
                            matchers = {
                                    @ValueMatcher(matchU8 = 0x01, valueType = u32, desc = "里程，DWORD，1/10km，对应车上里程表读数"),
                                    @ValueMatcher(matchU8 = 0x02, valueType = u16, desc = "油量，WORD，1/10L，对应车上油量表读数"),
                                    @ValueMatcher(matchU8 = 0x03, valueType = u16, desc = "行驶记录功能获取的速度，WORD，1/10km/h"),
                                    @ValueMatcher(matchU8 = 0x04, valueType = u16, desc = "需要人工确认报警事件的 ID，WORD，从 1 开始计数"),
                                    @ValueMatcher(matchU8 = 0x05, valueType = byte_array, desc = "胎压"),
                                    @ValueMatcher(matchU8 = 0x06, valueType = i16_as_int, desc = "车厢温度"),
                                    @ValueMatcher(matchU8 = 0x11, valueEntity = Item0x11.class, desc = "长度1或5；超速报警附加信息见 表 28"),
                                    @ValueMatcher(matchU8 = 0x12, valueEntity = Item0x12.class, desc = "进出区域/路线报警附加信息消息"),
                                    @ValueMatcher(matchU8 = 0x13, valueEntity = Item0x13.class, desc = "路线行驶时间不足/过长报警附加信息"),
                                    @ValueMatcher(matchU8 = 0x25, valueType = u32, desc = "扩展车辆信号状态位，定义见 表 31"),
                                    @ValueMatcher(matchU8 = 0x2A, valueType = u16, desc = "IO 状态位，表 32"),
                                    @ValueMatcher(matchU8 = 0x2B, valueType = i32, desc = "模拟量，bit0-15,AD0,bit16-31,AD1"),
                                    @ValueMatcher(matchU8 = 0x30, valueType = u8, desc = "数据类型为 BYTE，无线通信网络信号强度"),
                                    @ValueMatcher(matchU8 = 0x31, valueType = u8, desc = "数据类型为 BYTE，GNSS定位卫星数"),
                                    @ValueMatcher(matchU8 = 0x64, valueEntity = LocationItem0x64.class, desc = "苏标: 高级驾驶辅助报警信息，定义见表 4-15"),
                                    @ValueMatcher(matchU8 = 0x65, valueEntity = LocationItem0x65.class, desc = "苏标: 驾驶员状态监测系统报警信息，定义见表 4-17"),
                                    @ValueMatcher(matchU8 = 0x66, valueEntity = LocationItem0x66.class, desc = "苏标: 胎压监测系统报警信息，定义见表 4-18"),
                                    @ValueMatcher(matchU8 = 0x67, valueEntity = LocationItem0x67.class, desc = "苏标: 盲区监测系统报警信息，定义见表 4-20"),
                            },
                            // 其他未知的附加项 都解码为十六进制字符串
                            fallbackMatchers = {
                                    @FallbackValueMatcher(valueCodec = StringFieldCodecs.StringFieldCodecHex.class)
                            }
                    )
            )
    )
    private @Nullable Map<Short, Object> extraItems;

    public long getAlarmFlag() {
        return alarmFlag;
    }

    public BuiltinMessage0200 setAlarmFlag(long alarmFlag) {
        this.alarmFlag = alarmFlag;
        return this;
    }

    public long getStatus() {
        return status;
    }

    public BuiltinMessage0200 setStatus(long status) {
        this.status = status;
        return this;
    }

    public @Nullable Set<AlarmFlag> getAlarmFlags() {
        return alarmFlags;
    }

    public BuiltinMessage0200 setAlarmFlags(@Nullable Set<AlarmFlag> alarmFlags) {
        this.alarmFlags = alarmFlags;
        return this;
    }

    public @Nullable String getAlarmDescription() {
        return alarmDescription;
    }

    public BuiltinMessage0200 setAlarmDescription(@Nullable String alarmDescription) {
        this.alarmDescription = alarmDescription;
        return this;
    }

    public @Nullable Set<StatusBit> getStatusFlags() {
        return statusFlags;
    }

    public BuiltinMessage0200 setStatusFlags(@Nullable Set<StatusBit> statusFlags) {
        this.statusFlags = statusFlags;
        return this;
    }

    public long getLatitude() {
        return latitude;
    }

    public BuiltinMessage0200 setLatitude(long latitude) {
        this.latitude = latitude;
        return this;
    }

    public long getLongitude() {
        return longitude;
    }

    public BuiltinMessage0200 setLongitude(long longitude) {
        this.longitude = longitude;
        return this;
    }

    public int getAltitude() {
        return altitude;
    }

    public BuiltinMessage0200 setAltitude(int altitude) {
        this.altitude = altitude;
        return this;
    }

    public int getSpeed() {
        return speed;
    }

    public BuiltinMessage0200 setSpeed(int speed) {
        this.speed = speed;
        return this;
    }

    public int getDirection() {
        return direction;
    }

    public BuiltinMessage0200 setDirection(int direction) {
        this.direction = direction;
        return this;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public BuiltinMessage0200 setTime(LocalDateTime time) {
        this.time = time;
        return this;
    }

    public @Nullable Map<Short, Object> getExtraItems() {
        return extraItems;
    }

    public BuiltinMessage0200 setExtraItems(@Nullable Map<Short, Object> extraItems) {
        this.extraItems = extraItems;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage0200.class.getSimpleName() + "[", "]")
                .add("alarmFlag=" + alarmFlag)
                .add("alarmFlags=" + alarmFlags)
                .add("alarmDescription=" + alarmDescription)
                .add("status=" + status)
                .add("statusFlags=" + statusFlags)
                .add("latitude=" + latitude)
                .add("longitude=" + longitude)
                .add("altitude=" + altitude)
                .add("speed=" + speed)
                .add("direction=" + direction)
                .add("time=" + time)
                .add("extraItems=" + extraItems)
                .toString();
    }
}
