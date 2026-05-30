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

package io.github.hylexus.xtream.quickstart.custom.annotation.entity.request;

import io.github.hylexus.xtream.quickstart.custom.annotation.entity.AbstractEntity;
import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/// X-IoT 多传感器数据上报 (`msgType=0x13`) 消息体。
///
/// ## 报文格式
///
/// ```
/// +-------------+--------+------------------------------------------------+
/// | 偏移        | 长度   | 说明                                           |
/// +-------------+--------+------------------------------------------------+
/// | 0           | 4      | magic: u32, 固定 0x12345678                    |
/// | 4           | 1      | msgType: u8, 固定 0x13 (多传感器上报)           |
/// | 5           | 2      | bodyLength: u16, 消息体长度(大端)，固定为 15    |
/// | 7           | 2      | temperature: i16 big-endian, ×0.1°C            |
/// | 9           | 1      | humidity: u8, ×0.5%RH                          |
/// | 10          | 2      | pressure: u16 big-endian, ×0.1hPa              |
/// | 12          | 2      | windSpeed: u16 big-endian, ×0.1m/s             |
/// | 14          | 8      | timestamp: i64 big-endian, Unix 毫秒时间戳      |
/// +-------------+--------+------------------------------------------------+
/// ```
@Getter
@Setter
@ToString(callSuper = true)
public class MultiSensorData extends AbstractEntity {

    // region 消息体

    /// 温度值，int16 big-endian，分辨率 0.1°C。如 225 = 22.5°C
    @Preset.RustStyle.i16
    private int temperature;

    /// 湿度值，uint8，分辨率 0.5%RH。如 110 = 55.0%RH
    @Preset.RustStyle.u8
    private short humidity;

    /// 气压值，uint16 big-endian，分辨率 0.1hPa。如 10132 = 1013.2hPa
    @Preset.RustStyle.u16
    private int pressure;

    /// 风速值，uint16 big-endian，分辨率 0.1m/s。如 35 = 3.5m/s
    @Preset.RustStyle.u16
    private int windSpeed;

    /// Unix 毫秒时间戳，int64 big-endian
    @Preset.RustStyle.i64
    private long timestamp;
    // endregion

    public double temperatureInCelsius() {
        return temperature * 0.1;
    }

    public double humidityInPercent() {
        return humidity * 0.5;
    }

    public double pressureInHpa() {
        return pressure * 0.1;
    }

    public double windSpeedInMeterPerSecond() {
        return windSpeed * 0.1;
    }
}
