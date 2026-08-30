---
icon: shapes
article: false
---

# 自定义请求处理器

JT/T 808 Starter 默认提供了基于 `@Jt808RequestHandler` 和 `@Jt808RequestHandlerMapping` 的注解驱动处理方式。 如果注解映射不能满足路由需求，也可以通过 `XtreamHandlerMapping`、`XtreamHandlerAdapter` 扩展自己的处理器模型。

::: tip

只是需要按消息 ID 和协议版本处理请求时，优先使用 [注解驱动开发](../annotation-driven/overview.md)。 只有路由条件无法用 `@Jt808RequestHandlerMapping` 表达，或者需要完全控制请求处理与响应写出过程时，才建议自定义处理器。

:::

## 设计目的与思想来源

::: tip 设计思想来源于 Spring

这个框架最初的设计目标，是把开发者熟悉的 **Spring MVC 请求处理模式复刻到 TCP/UDP 技术栈**：让二进制协议也能像 Web 应用一样，将请求路由、处理器调用、参数解析、返回值处理、过滤器和统一异常处理拆分成职责清晰的组件。

随着实现逐步深入，框架最终主要采用了更适合 Netty 和异步 I/O 的 **Spring WebFlux** 设计。当前请求处理主链路、核心接口的职责划分以及响应式返回类型，都直接来源于 Spring WebFlux 的思想；部分基础类型也是在 Spring 对应实现的基础上移植并针对 TCP/UDP 场景改造的。

:::

这套设计复刻的是 Spring 的 **编程模型、组件边界和扩展机制**，不是把 TCP/UDP 包装成 HTTP：

- HTTP 使用路径、请求方法和媒体类型进行路由，JT/T 808 使用消息 ID、协议版本、服务器类型以及 TCP/UDP 类型等信息进行路由；
- HTTP 响应具有状态码和 Header，JT/T 808 响应需要按照协议完成消息头组装、实体编码、分包、转义和校验；
- 两者底层协议不同，但“找到处理器 → 适配并执行 → 处理返回值 → 写出响应”的分层方式可以保持一致。

“来源于 Spring WebFlux”也不意味着请求会进入 Spring 的 HTTP WebFlux 运行时。TCP/UDP 连接和数据收发仍由 Reactor Netty 与 Xtream 自己的服务器组件负责；Spring 容器主要用于发现、装配和配置这些 Mapping、Adapter、ResultHandler、Filter 等扩展组件。

### 从 Spring MVC 模式到 WebFlux 模型

Spring MVC 最有价值的部分并不只是 `@Controller` 和 `@RequestMapping`，而是它将请求处理拆分成可替换组件的方式。Xtream 延续了这种使用体验，同时选择 WebFlux 作为主要实现蓝图，原因包括：

- Netty 的 TCP/UDP 服务器本身就是异步、事件驱动的，WebFlux 的非阻塞模型与其更匹配；
- `Mono`、`Flux` 可以组合解码、过滤、路由、业务处理和响应写出流程，避免层层回调；
- Handler、Mapping、Adapter 和 ResultHandler 彼此解耦，注解处理器只是其中一种实现，而不是框架唯一支持的处理器形态；
- 统一的响应式错误信号可以贯穿完整调用链，并由全局异常处理器集中处理；
- 调度器可以显式控制阻塞与非阻塞任务的执行位置，而不必把线程模型隐藏在处理器内部。

### 与 Spring WebFlux 的核心抽象对应关系

| Spring / Spring WebFlux           | Xtream                                                 | 在 TCP/UDP 场景中的职责                      |
|-----------------------------------|--------------------------------------------------------|----------------------------------------------|
| `WebHandler`                      | `XtreamHandler`                                        | 处理一次完整的网络请求                       |
| `ServerWebExchange`               | `XtreamExchange`                                       | 聚合请求、响应、会话、属性和缓冲区等上下文   |
| `WebFilter` / `WebFilterChain`    | `XtreamFilter` / `XtreamFilterChain`                   | 在请求分发前后组成过滤器链                   |
| `DispatcherHandler`               | `DispatcherXtreamHandler`                              | 编排 Mapping、Adapter 和 ResultHandler       |
| `HandlerMapping`                  | `XtreamHandlerMapping`                                 | 根据请求找到一个处理器对象                   |
| `HandlerAdapter`                  | `XtreamHandlerAdapter`                                 | 判断能否执行处理器，并适配其调用方式         |
| `HandlerResult`                   | `XtreamHandlerResult`                                  | 描述处理器及其返回值、返回类型和异常处理信息 |
| `HandlerResultHandler`            | `XtreamHandlerResultHandler`                           | 处理业务返回值，并按协议写出响应             |
| `HandlerMethod`                   | `XtreamHandlerMethod`                                  | 封装基于注解发现的处理器方法                 |
| `HandlerMethodArgumentResolver`   | `XtreamHandlerMethodArgumentResolver`                  | 为注解处理器解析方法参数                     |
| `WebExceptionHandler`             | `XtreamRequestExceptionHandler`                        | 统一处理请求链路中的异常                     |
| `WebSessionManager`               | `XtreamSessionManager`                                 | 创建、查找和管理 TCP/UDP 会话                |
| `@Controller` / `@RequestMapping` | `@Jt808RequestHandler` / `@Jt808RequestHandlerMapping` | 声明注解驱动的处理器及路由条件               |
| `@RequestBody` / `@ResponseBody`  | `@Jt808RequestBody` / `@Jt808ResponseBody`             | 完成请求体解码和响应体编码                   |

::: important 为什么 Mapping 返回的是 `Object`？

这是从 Spring Handler 体系继承下来的关键设计： **Mapping 只负责找到处理器，Adapter 才负责定义如何执行处理器。**

如果 `XtreamHandlerMapping` 被限制为只能返回某个固定接口，框架就只能支持一种处理器模型。返回 `Object` 后，处理器既可以是注解方法、`SimpleXtreamRequestHandler`，也可以是任意领域对象；只需提供与之匹配的 `XtreamHandlerAdapter`。这也是 自定义处理器不一定非要实现 `SimpleXtreamRequestHandler` 的根本原因。

:::

### 实现原理

一次已经完成 JT/T 808 报文解码的请求，大致按以下顺序处理：

1. 框架创建 `Jt808Request` 和 `XtreamExchange`，将协议请求、响应通道、会话以及请求级属性聚合到同一个上下文中。
2. `XtreamFilterChain` 依次执行日志、分包合并、调度等过滤器，最后进入 `DispatcherXtreamHandler`。
3. `DispatcherXtreamHandler` 按顺序调用 `XtreamHandlerMapping`，使用第一个非空的处理器对象。
4. Dispatcher 查找第一个让 `supports(handler)` 返回 `true` 的 `XtreamHandlerAdapter`，由 Adapter 执行该处理器。
5. 如果 Adapter 返回 `XtreamHandlerResult`，Dispatcher 再查找匹配的 `XtreamHandlerResultHandler`，将业务结果编码成 JT/T 808 报文并写回终端。
6. 如果处理器或 Adapter 已经直接写出响应，可以返回空的结果流，跳过 ResultHandler 阶段。
7. 整条链路通过 Reactor 组合；过程中产生的错误信号交给 `XtreamRequestExceptionHandler` 统一处理。

因此，注解驱动、`SimpleXtreamRequestHandler` 和完全自定义的处理器对象虽然写法不同，最终都会进入同一套 WebFlux 风格的分发管线。

## 请求分发流程

自定义处理器涉及以下两个组件：

1. `XtreamHandlerMapping` 根据当前 `XtreamExchange` 查找处理器，并以 `Object` 类型返回。
2. `XtreamHandlerAdapter` 判断自己能否执行该处理器，并完成实际调用。

::: warning 自定义处理器不一定非要实现 `SimpleXtreamRequestHandler`

`XtreamHandlerMapping` 可以返回任意类型的处理器对象，框架没有要求自定义处理器必须实现某个统一接口。只要存在一个 `XtreamHandlerAdapter` 能够识别并执行该对象即可。

`SimpleXtreamRequestHandler` 只是框架提供的一种便捷约定：Starter 已经内置了与它配套的 `SimpleXtreamRequestHandlerHandlerAdapter`。选择它可以少写一个 Adapter，但它不是自定义处理器的必选模式。

:::

因此，自定义处理器有两种方式：

1. 实现 `SimpleXtreamRequestHandler`，复用内置 Adapter，只需额外提供 Mapping；
2. 使用任意 Java 对象作为处理器，同时提供能够执行该对象的自定义 `XtreamHandlerAdapter`。

第一种方式代码更少，适合直接处理请求并写出响应的场景。此时只需要提供：

- 一个实现 `SimpleXtreamRequestHandler` 的处理器；
- 一个能够返回该处理器的 `XtreamHandlerMapping`。

完整的分发机制参考 [DispatcherXtreamHandler](/guide/server/request-processing/dispatcher-handler.md)。

## 快捷方式：复用内置 Adapter

下面通过 `SimpleXtreamRequestHandler` 复用内置 Adapter，自定义终端心跳消息 `0x0002` 的路由和应答逻辑。

### 1. 实现处理器

`SimpleXtreamRequestHandler` 只有一个方法：

```java
public interface SimpleXtreamRequestHandler {

    Mono<Void> handle(XtreamExchange exchange);
}
```

实现类可以从 `XtreamExchange` 中获取请求、响应、会话和缓冲区分配器。由于简单处理器不会进入注解驱动的返回值处理流程，需要自行编码并写出响应：

```java

@Component
public class HeartbeatRequestHandler implements SimpleXtreamRequestHandler {

    private final Jt808ResponseEncoder responseEncoder;
    private final Jt808RequestLifecycleListener lifecycleListener;

    public HeartbeatRequestHandler(
            Jt808ResponseEncoder responseEncoder,
            Jt808RequestLifecycleListener lifecycleListener) {
        this.responseEncoder = responseEncoder;
        this.lifecycleListener = lifecycleListener;
    }

    @Override
    public Mono<Void> handle(XtreamExchange exchange) {
        final Jt808Request request = (Jt808Request) exchange.request();
        final ServerCommonReplyMessage body = ServerCommonReplyMessage.success(request);
        final Jt808MessageDescriber describer = new Jt808MessageDescriber(
                0x8001,
                request.header().version(),
                request.header().terminalId()
        );
        final ByteBuf response = this.responseEncoder.encode(body, describer);
        this.lifecycleListener.beforeResponseSend(request, response);
        return exchange.response().writeWith(Mono.just(response));
    }
}
```

这里手动调用 `Jt808RequestLifecycleListener.beforeResponseSend(...)`，使响应仍然能被已注册的生命周期监听器观察到。把 `ByteBuf` 交给 `writeWith(...)` 后，不要再手动释放它。

如果不需要向终端回复数据，完成业务逻辑后返回 `Mono.empty()` 即可。

### 2. 实现路由

`XtreamHandlerMapping.getHandler(...)` 返回当前请求对应的处理器；当前 Mapping 不支持该请求时必须返回 `Mono.empty()`，以便框架继续尝试后面的 Mapping。

```java

@Component
public class HeartbeatHandlerMapping implements XtreamHandlerMapping {

    private final HeartbeatRequestHandler handler;

    public HeartbeatHandlerMapping(HeartbeatRequestHandler handler) {
        this.handler = handler;
    }

    @Override
    public Mono<Object> getHandler(XtreamExchange exchange) {
        if (exchange.request() instanceof Jt808Request request
            && request.serverType() == Jt808ServerType.INSTRUCTION_SERVER
            && request.messageId() == 0x0002) {
            return Mono.just(this.handler);
        }
        return Mono.empty();
    }

    @Override
    public int order() {
        return -100;
    }
}
```

示例同时检查了服务器类型和消息 ID。所有 `XtreamHandlerMapping` Bean 都会被指令服务器和附件服务器收集，因此实际项目中应根据需要检查：

- `request.serverType()`：区分指令服务器与附件服务器；
- `request.type()`：区分 TCP 与 UDP；
- `request.messageId()`：匹配消息 ID；
- `request.header().version()`：匹配 JT/T 808 协议版本；
- 请求头、会话或其他业务状态。

### 3. 自动注册

示例中的处理器和 Mapping 都使用了 `@Component`。Starter 会自动收集 `XtreamHandlerMapping` Bean，并使用内置的 `SimpleXtreamRequestHandlerHandlerAdapter` 执行 Mapping 返回的处理器，不需要额外声明 Adapter。

也可以在配置类中使用 `@Bean` 注册；两种方式选择一种即可。如果使用下面的 `@Bean` 方式，应移除两个实现类上的 `@Component`，避免重复注册：

```java

@Configuration
public class Jt808HandlerConfiguration {

    @Bean
    HeartbeatRequestHandler heartbeatRequestHandler(
            Jt808ResponseEncoder responseEncoder,
            Jt808RequestLifecycleListener lifecycleListener) {
        return new HeartbeatRequestHandler(responseEncoder, lifecycleListener);
    }

    @Bean
    HeartbeatHandlerMapping heartbeatHandlerMapping(
            HeartbeatRequestHandler handler) {
        return new HeartbeatHandlerMapping(handler);
    }
}
```

## 路由顺序

`DispatcherXtreamHandler` 按 `XtreamHandlerMapping.order()` 从小到大检查 Mapping，并使用第一个返回处理器的 Mapping。

内置的 `Jt808RequestMappingHandlerMapping` 顺序是 `0`。示例返回 `-100`，因此自定义心跳路由会先于内置注解路由执行。这样可以有意覆盖同一消息的注解处理器；如果不需要覆盖，应确保两个 Mapping 的匹配范围不重叠，或者为自定义 Mapping 设置更靠后的顺序。

::: warning

不要对不支持的请求返回一个“兜底处理器”，否则后续 Mapping 将永远没有机会处理请求。只有真正匹配时才返回 `Mono.just(handler)`，其余情况返回 `Mono.empty()`。

:::

## 与注解驱动处理器的差异

下表对比的是复用内置 Adapter 的 `SimpleXtreamRequestHandler` 方式；使用自定义 `XtreamHandlerAdapter` 时，可以自行定义参数、返回值和调用模型。

| 能力         | 注解驱动处理器                                   | `SimpleXtreamRequestHandler`               |
|--------------|--------------------------------------------------|--------------------------------------------|
| 请求路由     | `@Jt808RequestHandlerMapping`                    | 自定义 `XtreamHandlerMapping`              |
| 方法参数注入 | 支持内置及自定义参数解析器                       | 不适用，直接使用 `XtreamExchange`          |
| 请求体解码   | 支持 `@Jt808RequestBody`                         | 需要自行解码                               |
| 响应编码     | 支持 `@Jt808ResponseBody`、`Jt808ResponseEntity` | 需要调用 `Jt808ResponseEncoder` 并写入响应 |
| 调度器选择   | 支持在注解中配置                                 | 需要在响应式调用链中自行安排调度           |
| 适合场景     | 常规的按消息 ID、版本路由                        | 特殊路由规则或完全自定义处理流程           |

过滤器、请求解码、分包合并和全局异常处理位于请求分发流程之外，使用自定义 Mapping 和简单处理器时仍然有效。注解驱动专属的参数解析与返回值处理则不会生效。

## 异常与资源管理

- 在 `handle(...)` 或返回的响应式调用链中抛出的异常，会进入已注册的 `XtreamRequestExceptionHandler`。
- 不要在 `handle(...)` 中执行阻塞操作。确实存在阻塞调用时，应显式切换到合适的 Reactor `Scheduler`。
- 请求中的 `ByteBuf` 由框架管理，不要随意释放，也不要在请求处理结束后继续持有。
- 自行创建但没有交给 `exchange.response().writeWith(...)` 的 `ByteBuf`，需要由创建方负责释放。
- `SimpleXtreamRequestHandler` 直接负责写出响应，不要期待 `XtreamHandlerResultHandler` 再次处理返回结果。

## 使用任意对象作为处理器

不实现 `SimpleXtreamRequestHandler` 时，处理器可以是任意普通 Java 对象。例如：

```java
public class HeartbeatHandler {

    public Mono<Void> handle(XtreamExchange exchange) {
        // 处理请求并按需写出响应
        return Mono.empty();
    }
}
```

这里的方法名和方法签名也不是框架契约；示例使用 `handle(XtreamExchange)` 只是为了直观。处理器可以暴露任意方法，具体调用方式完全由对应的 `XtreamHandlerAdapter` 决定。

此时需要提供与该对象配套的 `XtreamHandlerAdapter`：

```java

@Component
public class HeartbeatHandlerAdapter implements XtreamHandlerAdapter {

    @Override
    public boolean supports(Object handler) {
        return handler instanceof HeartbeatHandler;
    }

    @Override
    public Mono<XtreamHandlerResult> handle(
            XtreamExchange exchange,
            Object handler) {

        final HeartbeatHandler heartbeatHandler = (HeartbeatHandler) handler;
        return heartbeatHandler.handle(exchange).then(Mono.empty());
    }

    @Override
    public int order() {
        return -100;
    }
}
```

对应的 `XtreamHandlerMapping` 只需返回这个 `HeartbeatHandler` 对象，Starter 会自动收集自定义 Adapter，并调用第一个让 `supports(handler)` 返回 `true` 的实现。

如果处理器希望返回业务对象，而不是直接写出响应，还可以进一步提供自己的 `XtreamHandlerResultHandler`。一套完全自定义的处理器模型通常包括：

1. `XtreamHandlerMapping`：返回自定义类型的处理器对象；
2. `XtreamHandlerAdapter`：识别并执行该处理器，返回 `XtreamHandlerResult`；
3. `XtreamHandlerResultHandler`：在需要自定义返回值编码时处理执行结果。

这三个组件都可以注册为 Spring Bean，Starter 会自动收集并按各自的 `order()` 排序。`XtreamHandlerResultHandler` 只有在 Adapter 会产生 `XtreamHandlerResult` 时才是必需的；像上面的示例一样直接写出响应并返回空的结果流，则不需要额外实现它。
