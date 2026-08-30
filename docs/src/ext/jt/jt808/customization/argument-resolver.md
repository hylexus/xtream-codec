---
icon: fa-brands fa-resolving
article: false
---

# 参数解析器

`XtreamHandlerMethodArgumentResolver` 用来为基于注解的请求处理器解析方法参数。 当内置参数解析器不能直接提供业务所需的数据时，可以实现该接口，把请求上下文中的数据转换成更适合处理器使用的参数。

内置解析器及其支持的参数类型参考 [注解驱动开发：参数解析器](../annotation-driven/argument-resolver.md)。

::: tip

如果某个值只在少量处理器中使用，直接注入 `Jt808Request`、`Jt808Session` 等内置参数并在方法中取值通常更简单。 当同一套取值、校验或转换逻辑会被多个处理器重复使用时，再考虑自定义参数解析器。

:::

## 工作方式

接口包含两个方法：

```java
public interface XtreamHandlerMethodArgumentResolver {

    boolean supportsParameter(XtreamMethodParameter parameter);

    Mono<Object> resolveArgument(
            XtreamMethodParameter parameter,
            XtreamExchange exchange);
}
```

- `supportsParameter(...)` 判断当前解析器是否支持某个处理器方法参数。
- `resolveArgument(...)` 从当前请求上下文中解析参数值，并通过 `Mono` 返回。

请求到达后，框架会依次检查已注册的参数解析器。第一个让 `supportsParameter(...)` 返回 `true` 的解析器负责解析该参数；所有参数解析完成后，框架才会调用处理器方法。

## 示例：注入终端标识

下面通过自定义 `@Jt808TerminalId` 注解，将请求头中的终端手机号或设备 ID 直接注入处理器方法。

### 1. 定义参数注解

```java

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Jt808TerminalId {
}
```

必须保留到运行时，并且作用于方法参数，解析器才能通过反射读取该注解。

### 2. 实现参数解析器

```java

@Component
public class Jt808TerminalIdArgumentResolver
        implements XtreamHandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(XtreamMethodParameter parameter) {
        return parameter.getParameterType() == String.class
               && parameter.getParameterAnnotation(Jt808TerminalId.class).isPresent();
    }

    @Override
    public Mono<Object> resolveArgument(
            XtreamMethodParameter parameter,
            XtreamExchange exchange) {

        if (!(exchange.request() instanceof Jt808Request request)) {
            return Mono.error(new IllegalStateException("当前请求不是 JT/T 808 请求"));
        }
        return Mono.just(request.terminalId());
    }
}
```

这里同时检查了参数类型和参数注解。不要仅根据 `String` 类型判断，否则处理器中的其他 `String` 参数也会被这个解析器接管。

### 3. 在处理器中使用

解析器上的 `@Component` 会将其注册为 Spring Bean，JT/T 808 Starter 会自动把所有 `XtreamHandlerMethodArgumentResolver` 类型的 Bean 加入参数解析器链，不需要额外修改框架配置。

```java

@Component
@Jt808RequestHandler
public class LocationMessageHandler {

    @Jt808RequestHandlerMapping(messageIds = 0x0200)
    public Mono<Void> processLocation(
            @Jt808TerminalId String terminalId,
            @Jt808RequestBody BuiltinMessage0200 body) {

        return saveLocation(terminalId, body);
    }

    private Mono<Void> saveLocation(String terminalId, BuiltinMessage0200 body) {
        // 保存位置数据
        return Mono.empty();
    }
}
```

`@Jt808TerminalId String terminalId` 由自定义解析器提供，`@Jt808RequestBody BuiltinMessage0200 body` 仍由内置解析器提供，两者可以同时使用。

## 注册方式与优先级

推荐直接将实现类声明为 Spring Bean，可以使用 `@Component`，也可以在配置类中使用 `@Bean`。两种方式选择一种即可；如果使用下面的 `@Bean` 方式，应移除实现类上的 `@Component`：

```java

@Configuration
public class Jt808HandlerConfiguration {

    @Bean
    Jt808TerminalIdArgumentResolver jt808TerminalIdArgumentResolver() {
        return new Jt808TerminalIdArgumentResolver();
    }
}
```

::: warning 解析器匹配遵循“第一个命中”规则

同一个参数可能被多个解析器支持时，排在前面的解析器生效。自定义解析器的匹配范围应尽量精确，通常同时检查参数类型、参数注解或泛型信息，避免与内置解析器冲突。

如果多个自定义解析器确实存在重叠，可以使用 Spring 的 `@Order` 调整这些 Bean 的顺序。解析器一旦匹配某个方法参数，框架会缓存匹配结果，因此不要让 `supportsParameter(...)` 依赖当前请求或其他会动态变化的状态。

:::

## 返回值与异常

- 成功解析时返回 `Mono.just(value)`，异步取值时返回对应的 `Mono`。
- 无法解析或校验失败时返回 `Mono.error(...)`；框架不会调用处理器方法，异常会进入服务器的异常处理流程。
- `Mono.empty()` 会被框架转换成 `null` 参数。只有处理器明确允许空值时才应这样做，并避免用于 Java 基本类型参数。
- 参数解析发生在处理器方法被调度之前，不要在 `resolveArgument(...)` 中执行阻塞操作。需要访问数据库或远程服务时，应使用非阻塞 API，并在返回的 `Mono` 中完成处理。

## 常见问题

### 启动时没有报错，但请求到达后提示无法解析参数

确认以下事项：

1. 解析器已经注册为 Spring Bean，并且位于应用的组件扫描范围内。
2. `supportsParameter(...)` 能匹配处理器参数的实际类型、注解和泛型声明。
3. 参数注解使用了 `@Retention(RetentionPolicy.RUNTIME)`。

### 自定义解析器没有生效

通常是更靠前的解析器已经支持该参数。先缩小各解析器的匹配范围；确实需要覆盖时，再使用 `@Order` 明确自定义解析器之间的顺序。
