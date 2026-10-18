# server-quickstart-module Specification

## Purpose

提供一个不依赖 Spring Boot 的 X-IoT Demo TCP 服务端 quick-start，演示协议无关 Server Builder、自定义注解路由和实体编解码。
## Requirements
### Requirement: 新增 quick-start Gradle module

SHALL 在 `quick-start/custom-annotation-server/` 下新增 Gradle module，并注册到项目根 `settings.gradle.kts`。

Module 的 `build.gradle.kts` 依赖：
- `xtream-codec-server-reactive`（核心服务端库）
- `logback-classic`（日志实现）

不使用 Spring Boot，保持最简依赖。

#### Scenario: Module 构建成功
- **WHEN** 执行 `./gradlew :quick-start:custom-annotation-server:build`
- **THEN** 构建成功，生成可执行 jar

### Requirement: 可运行的 TCP 服务端入口

`XtreamCustomAnnotationServerApp.java` SHALL 包含 `main` 方法，启动 TCP 服务器：

1. 使用协议无关的 `XtreamServers.tcp()` 构建服务端
2. 注册 `DemoMessageHandlerMapping` 为 HandlerMapping
3. 通过 dispatcher 配置启用内置的 HandlerAdapter 和 HandlerResultHandler（`enableBuiltinHandlers(EntityCodec.DEFAULT)`）
4. 添加 `LengthFieldBasedFrameDecoder` 到 pipeline
5. 添加 `LoggingXtreamFilter` 用于调试日志
6. 监听端口 9527

#### Scenario: 服务端启动成功
- **WHEN** 运行 `main` 方法
- **THEN** 服务端在 9527 端口启动，日志输出 listening on 信息

#### Scenario: 示例不直接使用低层 Server Builder
- **WHEN** 阅读 `XtreamCustomAnnotationServerApp.java` 的服务端启动代码
- **THEN** 示例使用 Generic TCP Server Builder 作为主入口，而不是直接使用低层 TCP Server Builder

### Requirement: 包含完整的 Handler 方法

`MyDemoHandler` SHALL 包含以下 6 个 Handler 方法：

#### Scenario: Handler 方法签名

- **WHEN** 查看 `MyDemoHandler` 的消息处理方法
- **THEN** Handler 方法 SHALL 与下表的消息类型、方法名及参数一致

| msgType | 方法名 | 参数 | 说明 |
|---------|--------|------|------|
| 0x10 | handleHeartbeat | 无参 | 演示最简用法 |
| 0x11 | handleTimeQuery | 无参 | 演示时间响应 |
| 0x12 | handleTemperatureReport | `@XtreamRequestBody TemperatureReport report` | 演示 body 参数注入 |
| 0x13 | handleMultiSensorReport | `@XtreamRequestBody MultiSensorData report` | 演示多传感器数据注入 |
| 0x14 | handleDeviceRegister | `XtreamExchange exchange, @XtreamRequestBody DeviceRegisterRequest req` | 演示 exchange 和 body 联合注入 |
| 0x15 | handleAlarmReport | `@XtreamRequestBody AlarmReport report` | 演示字符串长度字段注入 |

#### Scenario: 心跳 Handler 可被调用
- **WHEN** 通过端口发送 heartbeat 报文（`12 34 56 78 10 00 00`）
- **THEN** handleHeartbeat 方法被执行，日志打印心跳记录

#### Scenario: 温湿度 Handler 可被调用
- **WHEN** 通过端口发送温湿度上报报文（msgType=0x12）
- **THEN** handleTemperatureReport 方法被执行，TemperatureReport 实体解析正确

### Requirement: 配套文档

文档 SHALL 位于 `docs/src/guide/server/samples/custom-demo-protocol/`，结构参考 `custom-protocol-sample-01`：

```
custom-demo-protocol/
├── README.md          # 示例索引
├── protocol.md         # 协议格式说明
└── handler-demo.md     # 自定义注解 + Handler 代码示例
```

#### Scenario: 文档包含协议结构表
- **WHEN** 阅读 `protocol.md`
- **THEN** 看到报文头字段表、消息类型列表、每种类型的消息体字段说明

#### Scenario: 文档包含完整代码示例
- **WHEN** 阅读 `handler-demo.md`
- **THEN** 看到自定义注解定义、HandlerMapping 实现、Handler 方法代码
