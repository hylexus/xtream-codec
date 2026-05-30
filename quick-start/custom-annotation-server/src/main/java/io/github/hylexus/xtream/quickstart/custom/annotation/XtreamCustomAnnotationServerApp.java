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

package io.github.hylexus.xtream.quickstart.custom.annotation;

import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamNettyHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin.LoggingXtreamRequestExceptionHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.LoggingXtreamFilter;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.XtreamServerBuilder;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.DefaultXtreamSchedulerRegistry;
import io.github.hylexus.xtream.quickstart.custom.annotation.handler.DemoMessageHandlerMapping;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.Schedulers;

/// X-IoT Demo 协议 TCP 服务端入口。
///
/// <h3>协议定义</h3>
///
/// 私有协议报文头格式（所有请求/响应共用）：
///
/// | 偏移 | 长度 | 说明                                         |
/// |------|------|----------------------------------------------|
/// | 0    | 4    | magic: u32，固定 0x12345678                  |
/// | 4    | 1    | msgType: u8，消息类型                        |
/// | 5    | 2    | bodyLength: u16，消息体长度（大端）           |
/// | 7    | N    | body: 消息体，长度由 bodyLength 决定          |
///
/// `@DemoMessageHandlerMapping` 读取 msgType（偏移 4）进行路由，消息体通过 `@XtreamRequestBody` 注入处理器。
///
/// <h3>架构说明</h3>
///
/// - 无 Spring 依赖，纯手动构建调度器注册表和 HandlerMapping
/// - `DemoMessageHandlerMapping` 扫描指定包下的 `@DemoMessageHandler` 类，收集 `@DemoMessageMapping` 方法
/// - `@XtreamRequestBody` / `@XtreamResponseBody` 由框架内置的 HandlerAdapter 处理，无需额外配置
/// - 编解码器传入 `EntityCodec.DEFAULT`，使用 xtream-codec-core 的注解驱动编解码
///
/// <h3>注意事项</h3>
///
/// 1. **端口冲突**：9527 被占用时启动会抛 BindException，修改 `port(9527)` 或先释放端口。
/// 2. **LengthFieldBasedFrameDecoder 参数**：lengthFieldOffset=5 (magic 4 + msgType 1)，lengthFieldLength=2 (bodyLength 大端)。调整时需与 `AbstractEntity` 的字段定义保持一致。
/// 3. **business 调度器**：用于设备注册等耗时操作，线程池大小(4)可根据实际场景调整。其他处理器使用默认的并行调度器。
/// 4. **包扫描路径**：`DemoMessageHandlerMapping` 构造参数 `"io.github.hylexus.xtream.quickstart.custom.annotation"` 必须包含 handler 所在的包。
/// 5. **ResourceLeakDetector**：Netty 资源泄漏检测仅用于调试，生产环境建议注释掉相关代码。
/// 6. **bodyLength 计算**：发送请求时需正确计算消息体长度写入报文头偏移 5-6，否则 LengthFieldBasedFrameDecoder 会解码错误。
/// 7. **测试建议**：先用 `nc` 或类似的工具发送 hex 报文验证基本流程，再集成到业务系统。
///
/// ## 测试方式（nc）
///
/// ```shell
/// # 心跳 (msgType=0x10):
/// echo -ne '\x12\x34\x56\x78\x10\x00\x00' | nc localhost 9527
///
/// # 服务器时间查询 (msgType=0x11):
/// echo -ne '\x12\x34\x56\x78\x11\x00\x00' | nc localhost 9527
///
/// # 温湿度上报 (msgType=0x12): temperature=23.5°C(0x00EB), humidity=60.0%RH(0x78)
/// echo -ne '\x12\x34\x56\x78\x12\x00\x03\x00\xeb\x78' | nc localhost 9527
///
/// # 多传感器数据上报 (msgType=0x13):
/// #   temperature=22.5°C(0x00E1), humidity=55.0%RH(0x6E)
/// #   pressure=1013.2hPa(0x2794), windSpeed=3.5m/s(0x0023)
/// #   timestamp=1700000000000ms(0x0000018B3F3B5A00)
/// echo -ne '\x12\x34\x56\x78\x13\x00\x0f\x00\xe1\x6e\x27\x94\x00\x23\x00\x00\x01\x8b\x3f\x3b\x5a\x00' | nc localhost 9527
///
/// # 设备注册 (msgType=0x14):
/// #   imeiLen=0x0F(15), imei="868105040876543"
/// #   productKeyLen=0x02, productKey="AB"
/// echo -ne '\x12\x34\x56\x78\x14\x00\x13\x0f\x38\x36\x38\x31\x30\x35\x30\x34\x30\x38\x37\x36\x35\x34\x33\x02\x41\x42' | nc localhost 9527
///
/// # 报警上报 (msgType=0x15): alarmType=1(通用报警), desc="overheat"
/// echo -ne '\x12\x34\x56\x78\x15\x00\x0b\x00\x01\x08\x6f\x76\x65\x72\x68\x65\x61\x74' | nc localhost 9527
/// ```
public class XtreamCustomAnnotationServerApp {

    private static final Logger log = LoggerFactory.getLogger(XtreamCustomAnnotationServerApp.class);

    public static void main(String[] args) {
        // 如果你不了解 ResourceLeakDetector 是做什么的，请务必注释掉下面这行代码
        // io.netty.util.ResourceLeakDetector.setLevel(io.netty.util.ResourceLeakDetector.Level.PARANOID);

        XtreamServerBuilder.newTcpServerBuilder()
                .addServerCustomizer(server -> server
                        .host("0.0.0.0")
                        .port(9527)
                        .doOnConnection(conn -> log.info("New connection: {}", conn))
                        .doOnChannelInit((observer, channel, remoteAddress) -> {
                            log.info("Channel init: {}", channel);
                            channel.pipeline().addFirst(
                                    // bodyLength 在报文头偏移 5 处，长度 2 字节
                                    new LengthFieldBasedFrameDecoder(
                                            1024,   // maxFrameLength
                                            5,      // lengthFieldOffset: magic(4) + msgType(1)
                                            2,      // lengthFieldLength: bodyLength 占 2 字节
                                            0,      // lengthAdjustment
                                            0       // initialBytesToStrip
                                    )
                            );
                        })
                )
                .addServerCustomizer(server -> {
                    // 创建自定义调度器注册表，注册一个名为 "business" 的自定义调度器
                    final DefaultXtreamSchedulerRegistry schedulerRegistry = new DefaultXtreamSchedulerRegistry(
                            Schedulers.parallel(),
                            Schedulers.boundedElastic(),
                            Schedulers.boundedElastic()
                    );
                    schedulerRegistry.registerScheduler("business",
                            Schedulers.newBoundedElastic(4, 100, "business"));

                    return server.handle(
                            XtreamNettyHandlerAdapter.newTcpBuilder()
                                    .addHandlerMappings(new DemoMessageHandlerMapping(
                                            new String[]{"io.github.hylexus.xtream.quickstart.custom.annotation"},
                                            cls -> io.github.hylexus.xtream.codec.core.utils.BeanUtils.createNewInstance(cls, new Object[0]),
                                            schedulerRegistry
                                    ))
                                    .enableBuiltinHandlerAdapters(EntityCodec.DEFAULT)
                                    .enableBuiltinHandlerResultHandlers(EntityCodec.DEFAULT)
                                    .addFilter(new LoggingXtreamFilter())
                                    .addExceptionHandler(new LoggingXtreamRequestExceptionHandler())
                                    .build()
                    );
                })
                .build("custom-annotation-server")
                .start();
    }
}
