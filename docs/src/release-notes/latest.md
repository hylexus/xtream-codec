---
icon: code-branch
article: false
---

# Latest

## 0.5.0-rc.4

### ⚠️ Breaking Changes

- `@XtreamMapField.PaddingType`
    - 删除 `io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField.PaddingType`
    - 使用 `io.github.hylexus.xtream.codec.core.annotation.PaddingType` 替代
- `XtreamMapField.KeyType`
    - 删除 `io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField.KeyType`
    - 使用 `io.github.hylexus.xtream.codec.core.annotation.ext.KeyType` 替代
- `XtreamMapField.ValueLengthType`
    - 删除 `io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField.ValueLengthType`
    - 使用 `io.github.hylexus.xtream.codec.core.annotation.ext.LengthFieldType` 替代
- `XtreamMapField.Key`
    - 删除 `io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField.Key`
    - 使用 `io.github.hylexus.xtream.codec.core.annotation.ext.Key` 替代

## 0.5.0-rc.3(2026-05-27)

### ⭐ New Features

- 重构 `AbstractMapFieldCodec`

## 0.5.0-rc.2(2026-05-22)

### 🎯 Highlights

`jt-808-server-dashboard-ui` 重构。感谢 [@dfEric](https://github.com/dfEric) 的贡献。

### ⭐ New Features

- [可观测性 #7](https://github.com/hylexus/xtream-codec/issues/7)
- [核心代码去掉 Lombok #12](https://github.com/hylexus/xtream-codec/issues/12)

### ❤️ Contributors

- [@dfEric](https://github.com/dfEric)
- [@hylexus](https://github.com/hylexus)

## 0.5.0-rc.1(2026-01-11)

### ⭐ New Features

- 兼容低版本 `spring-boot` [#11](https://github.com/hylexus/xtream-codec/issues/11)

## 0.4.0(2026-01-03)

### ⭐ New Features

- 多表达式引擎支持 [#5](https://github.com/hylexus/xtream-codec/issues/5)
- 数据类型扩展 [#6](https://github.com/hylexus/xtream-codec/issues/6)
    - `io.github.hylexus.xtream.codec.core.type.TLV`
    - `io.github.hylexus.xtream.codec.core.type.Pair`
    - `io.github.hylexus.xtream.codec.core.type.simple.DataField`
- 可观测性 - 后端 [#8](https://github.com/hylexus/xtream-codec/issues/8)

## 0.3.0(2025-10-26)

### 🎯 Highlights

- 默认属性访问策略由 `反射` 改为 `java.lang.invoke.LambdaMetafactory`

### ⭐ New Features

- 新增 `@XtreamEntity` 注解，支持配置类级别的属性访问策略
- 新增 `@XtreamField.propertyAccessStrategy()` 属性，支持配置字段级别的属性访问策略

### 🐞 Bug Fixes

- 修复 `AbstractJt808Message` 初始化异常

### 🔨 Dependency Updates

- 可空性标记全部使用 [jspecify](https://jspecify.dev/)
- 彻底移除 `jakarta.annotation-api`

## 0.2.0(2025-10-18)

### ⭐ New Features

- 增强 `Record` 类型的表达式解析功能
- 增强 `Record` 类型的 `CodecTracker` 埋点
- 重构 `BeanPropertyMetadata.PropertyGetter` 和 `BeanPropertyMetadata.PropertySetter` 的实现类
