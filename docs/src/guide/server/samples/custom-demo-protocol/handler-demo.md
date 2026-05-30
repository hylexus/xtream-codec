---
date: 2026-05-30
icon: code
author: hylexus
contributors: true
category:
  - 示例
tag:
  - Quick-Start
  - 自定义注解
  - HandlerMapping
---

# 注解与 Handler 实现详解

:::tip 传送门

当前示例源码位于 [quick-start/custom-annotation-server](https://github.com/hylexus/xtream-codec/blob/develop/quick-start/custom-annotation-server/README.md)

:::

## 概述

本例通过自定义注解 + 继承框架 HandlerMapping 的方式，将 X-IoT Demo 协议的消息分发逻辑与业务处理解耦。核心流程：

1. 定义自己的 `@DemoMessageHandler` 和 `@DemoMessageMapping` 注解
2. 编写继承 `AbstractSimpleXtreamRequestMappingHandlerMapping` 的 `DemoMessageHandlerMapping`
3. 编写 `MyDemoHandler`，用自定义注解声明消息处理逻辑
4. 在服务端入口注册 HandlerMapping 和自定义调度器

## 1. 自定义注解

### @DemoMessageHandler

```java

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@XtreamRequestHandler
public @interface DemoMessageHandler {
}
```

- 元注解 `@XtreamRequestHandler` 让框架扫描器在类路径上自动发现被标记的类
- 如果你的应用只需要一个 Handler 类，这个注解并非必须（可以手动注册），但注解方式更符合声明式风格

### @DemoMessageMapping

```java

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@XtreamRequestHandlerMapping
public @interface DemoMessageMapping {

    /// @return 当前处理器方法能处理的消息类型（对应 X-IoT Demo 协议的 `msgType` 字段）
    int[] msgType();

    /// `scheduler()` 别名 —— 对应 `@XtreamRequestHandlerMapping.scheduler()`。
    ///
    /// 指定当前方法运行在哪个 `Scheduler` 上。
    ///
    /// - 默认空字符串：使用框架默认的调度器（非阻塞）
    /// - 指定名称：从 `XtreamSchedulerRegistry` 中查找
    @AliasFor(annotation = XtreamRequestHandlerMapping.class, attribute = "scheduler")
    String scheduler() default "";
}
```

- `msgType()` 指定该方法能处理的消息类型（对应协议头中的 msgType 字段）
- `scheduler()` 通过 `@AliasFor` 代理到框架的调度器机制，可指定方法运行在哪个调度器上
- 元注解 `@XtreamRequestHandlerMapping` 让框架将方法注册为可调用的 `XtreamHandlerMethod`

## 2. HandlerMapping

```java
public class DemoMessageHandlerMapping extends AbstractSimpleXtreamRequestMappingHandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(DemoMessageHandlerMapping.class);

    public DemoMessageHandlerMapping() {
        this(new String[]{XtreamUtils.detectMainClassPackageName()},
                cls -> BeanUtils.createNewInstance(cls, new Object[0]));
    }

    public DemoMessageHandlerMapping(String[] basePackages,
                                      Function<Class<?>, Object> instanceFactory) {
        super(
                new DefaultXtreamSchedulerRegistry(
                        Schedulers.parallel(), Schedulers.boundedElastic(), Schedulers.boundedElastic()),
                new DefaultXtreamBlockingHandlerMethodPredicate(),
                basePackages, instanceFactory
        );
    }

    public DemoMessageHandlerMapping(String[] basePackages,
                                      Function<Class<?>, Object> instanceFactory,
                                      XtreamSchedulerRegistry schedulerRegistry) {
        super(schedulerRegistry, new DefaultXtreamBlockingHandlerMethodPredicate(),
                basePackages, instanceFactory);
    }

    @Override
    public Mono<Object> getHandler(XtreamExchange exchange) {
        // msgType 在报文头的第 5 字节（偏移 4），无符号
        final int msgType = exchange.request().payload().getByte(4) & 0xFF;
        log.info("Dispatching request with msgType={}(0x{})",
                msgType, FormatUtils.toHexString(msgType, 2));

        // FIXME 这里可以缓存到 Map 里，不用每次都遍历（为演示方便，这里直接遍历）
        for (final var handlerMethod : handlerMethods) {
            final DemoMessageMapping mapping =
                    handlerMethod.getMethod().getAnnotation(DemoMessageMapping.class);
            if (mapping != null) {
                for (final int type : mapping.msgType()) {
                    if (type == msgType) {
                        return Mono.just(handlerMethod);
                    }
                }
            }
        }

        log.warn("No handler found for msgType={}(0x{})", msgType,
                FormatUtils.toHexString(msgType, 2));
        return Mono.empty();
    }
}
```

关键点：

- `handlerMethods` 是父类 `AbstractSimpleXtreamRequestMappingHandlerMapping` 的 `protected` 字段，在构造时自动扫描并填充
- `getHandler()` 的职责：从请求报文提取 `msgType`，遍历已注册的 `handlerMethods`，匹配到对应的方法后返回
- 返回 `Mono.empty()` 表示无匹配，框架会继续尝试其他 HandlerMapping 或返回 404

### 扫描机制

构造函数通过 `XtreamUtils.detectMainClassPackageName()` 自动推断扫描包路径：

```java
public DemoMessageHandlerMapping() {
    this(new String[]{XtreamUtils.detectMainClassPackageName()},
            cls -> BeanUtils.createNewInstance(cls, new Object[0]));
}
```

`detectMainClassPackageName()` 会寻找含有 `main()` 方法的类，并将其包名作为扫描根路径。如果自动推断不准确，可以手动指定包路径并将 `schedulerRegistry` 传入：

```java
new DemoMessageHandlerMapping(
    new String[]{"io.github.hylexus.xtream.quickstart.custom.annotation"},
    cls -> BeanUtils.createNewInstance(cls, new Object[0]),
    schedulerRegistry
)
```

## 3. Handler 方法

### 3.1 心跳处理 — 通用应答

```java

@DemoMessageHandler
public class MyDemoHandler {

    @DemoMessageMapping(msgType = {0x10})
    @XtreamResponseBody
    public Mono<GenericAckResponse> handleHeartbeat() {
        log.info("Received heartbeat");
        return Mono.just(new GenericAckResponse(0x10, 0));
    }
}
```

`@XtreamResponseBody` 是框架内置的回写注解。handler 方法只需返回 POJO，`XtreamResponseBodyHandlerResultHandler` 会自动使用 `EntityCodec` 将其编码为二进制并写回响应，无需手动操作 `ByteBuf`。

### 3.2 时间查询 — @XtreamResponseBody 自动编码响应

```java

@DemoMessageMapping(msgType = {0x11})
@XtreamResponseBody
public Mono<ServerTimeResponse> handleTimeQuery() {
    final LocalDateTime now = LocalDateTime.now();
    log.info("Received time query, responding with time: {}", now);
    return Mono.just(new ServerTimeResponse(now));
}
```

对应的响应实体：

```java

@Getter
@Setter
public class ServerTimeResponse extends AbstractEntity {

    @Preset.JtStyle.BcdDateTime
    private LocalDateTime serverTime;

    public ServerTimeResponse() {
        this.msgType = 0x81;
    }

    public ServerTimeResponse(LocalDateTime serverTime) {
        this();
        this.serverTime = serverTime;
        this.bodyLength = 6;
    }
}
```

响应体使用 `@Preset.JtStyle.BcdDateTime` 注解编码为 BCD[6] `yyMMddHHmmss` 格式，非 i64 时间戳。

::: tip 为什么推荐 @XtreamResponseBody？

- 避免手动 `ByteBuf` 操作（容易内存泄漏）
- 框架自动管理 buffer 生命周期（编码后自动 release）
- 统一编解码逻辑，请求和响应走同一套注解体系

:::

### 3.3 温湿度上报 — @XtreamRequestBody 注入实体

```java

@DemoMessageMapping(msgType = {0x12})
@XtreamResponseBody
public Mono<GenericAckResponse> handleTemperatureReport(
        @XtreamRequestBody TemperatureReport report) {
    log.info("Received temperature report: {}°C, {}%RH",
            report.temperatureInCelsius(), report.humidityInPercent());
    return Mono.just(new GenericAckResponse(0x12, 0));
}
```

`@XtreamRequestBody` 是框架内置注解，自动将消息体解码为指定实体类。方法返回 `Mono<GenericAckResponse>`，框架自动编码并写回响应。

### 3.4 多传感器上报 — @XtreamRequestBody 注入实体

```java

@DemoMessageMapping(msgType = {0x13})
@XtreamResponseBody
public Mono<GenericAckResponse> handleMultiSensorReport(
        @XtreamRequestBody MultiSensorData report) {
    log.info("Received multi-sensor report: {}°C, {}%RH, {}hPa, {}m/s, ts={}",
            report.temperatureInCelsius(), report.humidityInPercent(),
            report.pressureInHpa(), report.windSpeedInMeterPerSecond(),
            report.getTimestamp());
    return Mono.just(new GenericAckResponse(0x13, 0));
}
```

### 3.5 设备注册 — 混合注入 + 自定义调度器

```java

@DemoMessageMapping(msgType = {0x14}, scheduler = "business")
@XtreamResponseBody
public Mono<RegisterAckResponse> handleDeviceRegister(
        XtreamExchange exchange,
        @XtreamRequestBody DeviceRegisterRequest request) {

    log.info("Received device register: imei={}, productKey={}, remote={}",
            request.getImei(), request.getProductKey(),
            exchange.request().remoteAddress());
    return Mono.just(new RegisterAckResponse(0, "registered OK"));
}
```

同时注入 `XtreamExchange` 和 `@XtreamRequestBody` 实体，框架按参数类型自动选择解析器。

通过 `scheduler = "business"` 指定该方法运行在自定义的 `business` 调度器上（`Schedulers.newBoundedElastic(4, 100, "business")`），适用于设备注册等可能涉及 IO 或耗时操作的场景。

### 3.6 报警上报

```java

@DemoMessageMapping(msgType = {0x15})
@XtreamResponseBody
public Mono<GenericAckResponse> handleAlarmReport(
        @XtreamRequestBody AlarmReport report) {
    log.info("Received alarm report: type={}, desc={}",
            report.getAlarmType(), report.getDesc());
    return Mono.just(new GenericAckResponse(0x15, 0));
}
```

## 4. 服务端入口

```java
public class XtreamCustomAnnotationServerApp {

    public static void main(String[] args) {
        XtreamServerBuilder.newTcpServerBuilder()
                .addServerCustomizer(server -> server
                        .host("0.0.0.0")
                        .port(9527)
                        .doOnConnection(conn -> log.info("New connection: {}", conn))
                        .doOnChannelInit((observer, channel, remoteAddress) -> {
                            channel.pipeline().addFirst(
                                    new LengthFieldBasedFrameDecoder(
                                            1024,
                                            5,      // lengthFieldOffset: magic(4) + msgType(1)
                                            2,      // lengthFieldLength: bodyLength 占 2 字节
                                            0,      // lengthAdjustment
                                            0       // initialBytesToStrip
                                    )
                            );
                        })
                )
                .addServerCustomizer(server -> {
                    final DefaultXtreamSchedulerRegistry schedulerRegistry =
                            new DefaultXtreamSchedulerRegistry(
                                    Schedulers.parallel(),
                                    Schedulers.boundedElastic(),
                                    Schedulers.boundedElastic()
                            );
                    schedulerRegistry.registerScheduler("business",
                            Schedulers.newBoundedElastic(4, 100, "business"));

                    return server.handle(
                            XtreamNettyHandlerAdapter.newTcpBuilder()
                                    .addHandlerMappings(new DemoMessageHandlerMapping(
                                            new String[]{
                                                "io.github.hylexus.xtream.quickstart.custom.annotation"
                                            },
                                            cls -> BeanUtils.createNewInstance(cls, new Object[0]),
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
```

关键配置：

| 配置项                                              | 说明                                  |
|--------------------------------------------------|-------------------------------------|
| `LengthFieldBasedFrameDecoder(1024, 5, 2, 0, 0)` | 解决 TCP 粘包，从第 5 字节读取 2 字节 bodyLength |
| `new DemoMessageHandlerMapping(...)`              | 注册自定义 HandlerMapping（含自定义调度器）       |
| `schedulerRegistry.registerScheduler("business")` | 注册业务调度器，用于设备注册等耗时操作                |
| `enableBuiltinHandlerAdapters()`                  | 启用内置参数解析器（含 `@XtreamRequestBody`）   |
| `enableBuiltinHandlerResultHandlers()`            | 启用内置返回值处理器                          |

## 5. 测试

启动 `XtreamCustomAnnotationServerApp` 后，使用 `nc` 命令测试：

```shell
# 心跳 (msgType=0x10)
echo -ne '\x12\x34\x56\x78\x10\x00\x00' | nc localhost 9527

# 服务器时间查询 (msgType=0x11)
echo -ne '\x12\x34\x56\x78\x11\x00\x00' | nc localhost 9527

# 温湿度上报 (msgType=0x12) — 23.5°C, 60.0%RH
echo -ne '\x12\x34\x56\x78\x12\x00\x03\x00\xeb\x78' | nc localhost 9527

# 多传感器数据上报 (msgType=0x13)
echo -ne '\x12\x34\x56\x78\x13\x00\x0f\x00\xe1\x6e\x27\x94\x00\x23\x00\x00\x01\x8b\x3f\x3b\x5a\x00' | nc localhost 9527

# 设备注册 (msgType=0x14) — imei=868105040876543, productKey=AB
echo -ne '\x12\x34\x56\x78\x14\x00\x13\x0f\x38\x36\x38\x31\x30\x35\x30\x34\x30\x38\x37\x36\x35\x34\x33\x02\x41\x42' | nc localhost 9527

# 报警上报 (msgType=0x15) — alarmType=1, desc="overheat"
echo -ne '\x12\x34\x56\x78\x15\x00\x0b\x00\x01\x08\x6f\x76\x65\x72\x68\x65\x61\x74' | nc localhost 9527
```
