---
icon: at
article: false
---

# 0x0200-位置信息汇报

## 示例1 - @XtreamMapField

使用 `@XtreamMapField` 来解析附加项。为统一风格，内置了两个别名注解：

- `@Preset.RustStyle.simple_map`
- `@Preset.JtStyle.SimpleMap`

::: warning 提示

- `@XtreamMapField` 注解的使用有点繁琐，不太方便调试
- 如果需要调试，可以使用示例2 中的自定义 `FieldCodec` 的写法

:::

- [GitHub](https://github.com/hylexus/xtream-codec/blob/main/ext/jt/jt-808-server-spring-boot-starter-reactive/src/main/java/io/github/hylexus/xtream/codec/ext/jt808/builtin/messages/request/BuiltinMessage0200.java)
- [Gitee](https://gitee.com/hylexus/xtream-codec/blob/main/ext/jt/jt-808-server-spring-boot-starter-reactive/src/main/java/io/github/hylexus/xtream/codec/ext/jt808/builtin/messages/request/BuiltinMessage0200.java)

## 示例2 - 自定义编解码器

本示例中使用自定义的 `BuiltinLocationMessageExtraItemFieldCodec` 来解析附加项，该类继承自
`io.github.hylexus.xtream.codec.core.impl.codec.AbstractMapFieldCodec`。

虽然代码比较繁琐，但是方便调试。

- [GitHub](https://github.com/hylexus/xtream-codec/blob/main/ext/jt/jt-808-server-spring-boot-starter-reactive/src/main/java/io/github/hylexus/xtream/codec/ext/jt808/builtin/messages/codec/BuiltinLocationMessageExtraItemFieldCodec.java)
- [Gitee](https://gitee.com/hylexus/xtream-codec/blob/main/ext/jt/jt-808-server-spring-boot-starter-reactive/src/main/java/io/github/hylexus/xtream/codec/ext/jt808/builtin/messages/request/BuiltinMessage0200Sample2.java)

