---
icon: fa-brands fa-resolving
article: false
---

# 参数解析器

## 作用

`XtreamHandlerMethodArgumentResolver` 的作用就是给注解处理器提供参数。

下面代码中高亮的部分，都是 `XtreamHandlerMethodArgumentResolver` 解析的。

```java {8-21}
@Component
@Jt808RequestHandler
public class DemoJt808RequestHandler {

    @Jt808RequestHandlerMapping(messageIds = 0x0200, versions = Jt808ProtocolVersion.VERSION_2019)
    @Jt808ResponseBody(messageId = 0x8001)
    public Mono<ServerCommonReplyMessage> processMessage0200V2019(
            XtreamExchange exchange,
            XtreamSession xtreamSession,
            XtreamRequest xtreamRequest,
            Jt808Request jt808Request,
            DefaultXtreamRequest defaultXtreamRequest,
            XtreamResponse xtreamResponse,
            DefaultXtreamResponse defaultXtreamResponse,
            @Jt808RequestBody DemoLocationMsg01 msg01,
            @Jt808RequestBody DemoLocationMsg02 msg02,
            Jt808RequestEntity<BuiltinMessage0200> requestEntity,
            @Jt808RequestBody ByteBuf buf01,
            @Jt808RequestBody ByteBuf buf02,
            @Jt808RequestBody(bufferAsSlice = false) ByteBuf buf03,
            @Jt808RequestBody(bufferAsSlice = false) ByteBuf buf04) {

        log.info("v2019-0x0200: {}", msg01);
        assertNotSame(buf01, buf02);
        assertNotSame(buf01, buf03);
        assertSame(buf03, buf04);
        assertSame(exchange.request(), xtreamRequest);
        assertSame(exchange.request(), jt808Request);
        assertSame(exchange.request(), defaultXtreamRequest);
        assertSame(exchange.response(), xtreamResponse);
        assertSame(exchange.response(), defaultXtreamResponse);
        final ServerCommonReplyMessage responseBody = ServerCommonReplyMessage.success(jt808Request);
        return Mono.just(responseBody);
    }
}
```

下面介绍一些内置的参数解析器。

![内置参数解析器](/img/server/annotation-driven/handler-method-argument-resolver.png)

## XtreamRequestBodyArgumentResolver

这个参数解析器，用于支持 `@Jt808RequestBody`
注解。在上一小节的 [@Jt808RequestBody](./request-message-mapping.md#jt808requestbody) 中已经介绍过了，这里不再赘述。

## XtreamExchangeArgumentResolver

这个参数解析器用来给 处理器方法注入 `XtreamExchange` 类型的参数。

```java {4}
@Jt808RequestHandlerMapping(messageIds = 0x0200, versions = Jt808ProtocolVersion.VERSION_2019)
@Jt808ResponseBody(messageId = 0x8001)
public Mono<ServerCommonReplyMessage> processMessage0200V2019(
        XtreamExchange exchange
) {
    // ...
}
```

## XtreamRequestArgumentResolver

这个参数解析器用来给 处理器方法注入 `XtreamRequest` 类型的参数。

```java {4-5}
@Jt808RequestHandlerMapping(messageIds = 0x0200, versions = Jt808ProtocolVersion.VERSION_2019)
@Jt808ResponseBody(messageId = 0x8001)
public Mono<ServerCommonReplyMessage> processMessage0200V2019(
        XtreamRequest xtreamRequest,
        Jt808Request jt808Request,
) {
    // ...
}
```

## XtreamResponseArgumentResolver

这个参数解析器用来给 处理器方法注入 `XtreamResponse` 类型的参数。

```java {4}
@Jt808RequestHandlerMapping(messageIds = 0x0200, versions = Jt808ProtocolVersion.VERSION_2019)
@Jt808ResponseBody(messageId = 0x8001)
public Mono<ServerCommonReplyMessage> processMessage0200V2019(
        XtreamResponse xtreamResponse
) {
    // ...
}
```

## XtreamSessionArgumentResolver

这个参数解析器用来给 处理器方法注入 `XtreamSession` 类型的参数。

```java {4}
@Jt808RequestHandlerMapping(messageIds = 0x0200, versions = Jt808ProtocolVersion.VERSION_2019)
@Jt808ResponseBody(messageId = 0x8001)
public Mono<ServerCommonReplyMessage> processMessage0200V2019(
        XtreamSession xtreamSession
) {
    // ...
}
```

## Jt808RequestEntityArgumentResolver

这个参数解析器用来给 处理器方法注入 `Jt808RequestEntity` 类型的参数。

```java {4}
@Jt808RequestHandlerMapping(messageIds = 0x0200, versions = Jt808ProtocolVersion.VERSION_2019)
@Jt808ResponseBody(messageId = 0x8001)
public Mono<ServerCommonReplyMessage> processMessage0200V2019(
        Jt808RequestEntity<BuiltinMessage0200> requestEntity
) {
    // ...
}
```

## Jt808MessageArgumentResolver

这个参数解析器用来给 处理器方法注入 `AbstractJt808Message` **子类型** 的参数。

::: tip

要使用这种类型的参数，子类必须提供一个 `Jt808Request` 类型的 **单参** 构造函数。

:::

```java {4,12}
@Jt808RequestHandlerMapping(messageIds = 0x0200, versions = Jt808ProtocolVersion.VERSION_2019)
@Jt808ResponseBody(messageId = 0x8001)
public Mono<ServerCommonReplyMessage> processMessage0200V2019(
        DemoLocationMsg03 message
) {
    // ...
}

public static class DemoLocationMsg03 extends AbstractJt808Message {

    // 必须要有 Jt808Request 类型的单参构造函数
    public DemoLocationMsg03(Jt808Request request) {
        super(request);
    }

    // 报警标志  DWORD(4)
    @Preset.JtStyle.Dword
    private long alarmFlag;

    // 状态  DWORD(4)
    @Preset.JtStyle.Dword
    private long status;

    // 纬度  DWORD(4)
    @Preset.JtStyle.Dword
    private long latitude;

    // 经度  DWORD(4)
    @Preset.JtStyle.Dword
    private long longitude;
}
```

## 自定义参数解析器

参考 [定制化--参数解析器](../customization/argument-resolver.md)
