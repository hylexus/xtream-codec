---
icon: server
article: false
---

# 自定义注解

本例将演示如何通过框架提供的元注解机制（`@XtreamRequestHandler`、`@XtreamRequestHandlerMapping`），**自行定义**一套协议的注解。

## 场景

假设你有一套私有的 IoT 协议（非 JT/T 808），但希望像 JT/T 808 那样通过注解来声明式地处理消息。

## 核心思路

- 定义自己的 `@DemoMessageHandler`（类级）和 `@DemoMessageMapping`（方法级），并让它们**元注解**框架提供的 `@XtreamRequestHandler` / `@XtreamRequestHandlerMapping`
- 编写继承 `AbstractSimpleXtreamRequestMappingHandlerMapping` 的 HandlerMapping，在 `getHandler()` 中根据报文中的 `msgType` 字段分发到正确的处理器方法
- 使用框架内置的 `@XtreamRequestBody` 自动将消息体解码为 POJO
- 使用框架内置的 `@XtreamResponseBody` 自动将 POJO 编码为响应报文，无需手动操作 ByteBuf

## 文档

- [协议格式说明](./protocol.md)
- [注解 + Handler 实现详解](./handler-demo.md)
