---
icon: at
article: false
---

# 0x0200-位置信息汇报

## 示例1 - Map

使用 `@XtreamFieldMapDescriptor` 来描述附加项。

::: warning 提示

不太推荐使用 `@XtreamFieldMapDescriptor`，因为：

- 注解配置繁琐
- 极其不方便调试

推荐示例2 中的写法。

:::

- [GitHub](https://github.com/hylexus/xtream-codec/blob/main/ext/jt/jt-808-server-spring-boot-starter-reactive/src/main/java/io/github/hylexus/xtream/codec/ext/jt808/builtin/messages/request/BuiltinMessage0200.java)
- [Gitee](https://gitee.com/hylexus/xtream-codec/blob/main/ext/jt/jt-808-server-spring-boot-starter-reactive/src/main/java/io/github/hylexus/xtream/codec/ext/jt808/builtin/messages/request/BuiltinMessage0200.java)

## 示例2 - 自定义编解码器

本示例中使用自定义的 `BuiltinLocationMessageExtraItemFieldCodec` 来解析附加项，该类继承自
`io.github.hylexus.xtream.codec.core.impl.codec.AbstractMapFieldCodec`。

虽然代码比较繁琐，但是方便调试。

- [GitHub](https://github.com/hylexus/xtream-codec/blob/main/ext/jt/jt-808-server-spring-boot-starter-reactive/src/main/java/io/github/hylexus/xtream/codec/ext/jt808/builtin/messages/codec/BuiltinLocationMessageExtraItemFieldCodec.java)
- [Gitee](https://gitee.com/hylexus/xtream-codec/blob/main/ext/jt/jt-808-server-spring-boot-starter-reactive/src/main/java/io/github/hylexus/xtream/codec/ext/jt808/builtin/messages/request/BuiltinMessage0200Sample2.java)

