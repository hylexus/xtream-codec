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

package io.github.hylexus.xtream.quickstart.custom.annotation.handler;

import io.github.hylexus.xtream.codec.core.annotation.XtreamRequestBody;
import io.github.hylexus.xtream.codec.core.annotation.XtreamResponseBody;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.quickstart.custom.annotation.annotation.DemoMessageHandler;
import io.github.hylexus.xtream.quickstart.custom.annotation.annotation.DemoMessageMapping;
import io.github.hylexus.xtream.quickstart.custom.annotation.entity.request.AlarmReport;
import io.github.hylexus.xtream.quickstart.custom.annotation.entity.request.DeviceRegisterRequest;
import io.github.hylexus.xtream.quickstart.custom.annotation.entity.request.MultiSensorData;
import io.github.hylexus.xtream.quickstart.custom.annotation.entity.request.TemperatureReport;
import io.github.hylexus.xtream.quickstart.custom.annotation.entity.response.GenericAckResponse;
import io.github.hylexus.xtream.quickstart.custom.annotation.entity.response.RegisterAckResponse;
import io.github.hylexus.xtream.quickstart.custom.annotation.entity.response.ServerTimeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/// X-IoT Demo 协议的请求处理器。
///
/// 被 `@DemoMessageHandler` 标记后，`DemoMessageHandlerMapping` 在扫描时会自动发现此类。
@DemoMessageHandler
public class MyDemoHandler {

    private static final Logger log = LoggerFactory.getLogger(MyDemoHandler.class);

    /// 处理心跳请求 (`msgType=0x10`)。
    ///
    /// ## 测试报文 (hex)
    ///
    /// ```java
    /// // 心跳请求：magic(4) + msgType(1) + bodyLength(2) = 7 字节，无消息体
    /// 12 34 56 78 10 00 00
    /// ```
    ///
    /// 回复 `GenericAckResponse` 告知客户端心跳成功。
    @DemoMessageMapping(msgType = {0x10})
    @XtreamResponseBody
    public Mono<GenericAckResponse> handleHeartbeat() {
        log.info("Received heartbeat");
        return Mono.just(new GenericAckResponse(0x10, 0));
    }

    /// 处理服务器时间查询 (`msgType=0x11`)。
    ///
    /// ## 测试报文 (hex)
    ///
    /// 请求（无消息体）:
    ///
    /// ```java
    /// 12 34 56 78 11 00 00
    /// ```
    ///
    /// 响应（假设当前时间 2026-05-30 14:30:00）:
    ///
    /// ```java
    /// 12 34 56 78 81 00 06 26 05 30 14 30 00
    /// ```
    ///
    /// 演示 `@XtreamResponseBody`：返回 POJO，框架自动编码并写回响应。
    /// 响应体使用 BCD8421 编码的 `yyMMddHHmmss` 格式时间。
    @DemoMessageMapping(msgType = {0x11})
    @XtreamResponseBody
    public Mono<ServerTimeResponse> handleTimeQuery() {
        final LocalDateTime now = LocalDateTime.now();
        log.info("Received time query, responding with time: {}", now);
        return Mono.just(new ServerTimeResponse(now));
    }

    /// 处理温湿度上报 (`msgType=0x12`)。
    ///
    /// ## 测试报文 (hex)
    ///
    /// ```java
    /// // 温度 23.5°C (235=0x00EB), 湿度 60.0%RH (120=0x78)
    /// // 完整报文: magic(4) + msgType(1) + bodyLength(2) + temperature(2) + humidity(1)
    /// 12 34 56 78 12 00 03 00 EB 78
    /// ```
    ///
    /// 演示 `@XtreamRequestBody` 注解的参数注入：将消息体自动解码为实体类。
    /// 回复通用应答告知客户端上报成功。
    @DemoMessageMapping(msgType = {0x12})
    @XtreamResponseBody
    public Mono<GenericAckResponse> handleTemperatureReport(@XtreamRequestBody TemperatureReport report) {
        log.info("Received temperature report: {}°C, {}%RH",
                report.temperatureInCelsius(), report.humidityInPercent());
        return Mono.just(new GenericAckResponse(0x12, 0));
    }

    /// 处理多传感器数据上报 (`msgType=0x13`)。
    ///
    /// ## 测试报文 (hex)
    ///
    /// ```java
    /// // 温度 22.5°C (225=0x00E1), 湿度 55.0%RH (110=0x6E)
    /// // 气压 1013.2hPa (10132=0x2794), 风速 3.5m/s (35=0x0023)
    /// // 时间戳 1700000000000ms (0x0000018B3F3B5A00)
    /// // 完整报文: magic(4) + msgType(1) + bodyLength(2) + body(15)
    /// 12 34 56 78 13 00 0F 00 E1 6E 27 94 00 23 00 00 01 8B 3F 3B 5A 00
    /// ```
    ///
    /// 演示 `@XtreamRequestBody` 注解的参数注入：将消息体自动解码为实体类。
    /// 回复通用应答告知客户端上报成功。
    @DemoMessageMapping(msgType = {0x13})
    @XtreamResponseBody
    public Mono<GenericAckResponse> handleMultiSensorReport(@XtreamRequestBody MultiSensorData report) {
        log.info("Received multi-sensor report: {}°C, {}%RH, {}hPa, {}m/s, ts={}",
                report.temperatureInCelsius(), report.humidityInPercent(),
                report.pressureInHpa(), report.windSpeedInMeterPerSecond(), report.getTimestamp());
        return Mono.just(new GenericAckResponse(0x13, 0));
    }

    /// 处理设备注册 (`msgType=0x14`)。
    ///
    /// ## 测试报文 (hex)
    ///
    /// ```java
    /// // imei="868105040876543", productKey="AB"
    /// // 完整报文: magic(4) + msgType(1) + bodyLength(2) + imeiLen(1) + imei(15) + productKeyLen(1) + productKey(2)
    /// 12 34 56 78 14 00 13 0F 38 36 38 31 30 35 30 34 30 38 37 36 35 34 33 02 41 42
    /// ```
    ///
    /// 演示同时注入 `XtreamExchange` 和 `@XtreamRequestBody` 实体。
    /// 回复注册结果告知客户端是否注册成功。
    ///
    /// 使用 `scheduler = "business"` 指定在自定义的业务调度器上运行。
    @DemoMessageMapping(msgType = {0x14}, scheduler = "business")
    @XtreamResponseBody
    public Mono<RegisterAckResponse> handleDeviceRegister(
            XtreamExchange exchange,
            @XtreamRequestBody DeviceRegisterRequest request) {

        log.info("Received device register: imei={}, productKey={}, remote={}",
                request.getImei(), request.getProductKey(), exchange.request().remoteAddress());
        return Mono.just(new RegisterAckResponse(0, "registered OK"));
    }

    /// 处理报警上报 (`msgType=0x15`)。
    ///
    /// ## 测试报文 (hex)
    ///
    /// ```java
    /// // alarmType=1 (通用报警), desc="overheat" (UTF-8)
    /// // 完整报文: magic(4) + msgType(1) + bodyLength(2) + alarmType(2) + descLen(1) + desc(8)
    /// // bodyLength = 0x0B (11 bytes)
    /// 12 34 56 78 15 00 0B 00 01 08 6F 76 65 72 68 65 61 74
    /// ```
    ///
    /// 演示 `@XtreamRequestBody` 注解的参数注入：将消息体自动解码为实体类。
    /// 回复通用应答告知客户端上报成功。
    @DemoMessageMapping(msgType = {0x15})
    @XtreamResponseBody
    public Mono<GenericAckResponse> handleAlarmReport(@XtreamRequestBody AlarmReport report) {
        log.info("Received alarm report: type={}, desc={}", report.getAlarmType(), report.getDesc());
        return Mono.just(new GenericAckResponse(0x15, 0));
    }
}
