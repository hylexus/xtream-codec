---
icon: code-branch
article: false
---

# Latest

## 0.10.0 (2026-08-30)

### 🐞 Bug Fixes

- 修复 JT/T 808 2019 版 `0x0107` 查询终端属性应答中制造商 ID 字段长度错误的问题，由 11 字节修正为 5 字节 [#13](https://github.com/hylexus/xtream-codec/pull/13)

### 🚀 Improvements

- 补充 JT/T 808 2019 版及多版本合一 `0x0107` 消息的回归测试与 Dashboard 调试报文

### ❤️ Contributors

- [@yeluod](https://github.com/yeluod)
- [@hylexus](https://github.com/hylexus)

## 0.9.0 (2026-08-22)

### ⭐ New Features

- 新增结构化编解码跟踪能力，记录字段、集合、Map、长度字段等节点的编解码结果、字节范围和状态
- JT/T 808 调试面板新增编解码 Trace 查看、原始字节预览和结果详情展示

### 🚀 Improvements

- 重构 `CodecTracker`，统一编码和解码跟踪模型，并增强失败诊断与嵌套字段定位能力
- 调试面板支持更丰富的消息编辑、JSON 查看和 Trace 节点检查

### 🔨 Dependency Updates

- Gradle Wrapper 升级至 `9.7.1`

### ❤️ Contributors

- [OpenAI Codex](https://openai.com/codex/)
- [@hylexus](https://github.com/hylexus)

## 0.8.0 (2026-08-09)

### 📔 Documentation

- 更新自定义注解示例文档，改为直接引用后端源码，避免文档示例与实现分叉
- 补充 `@ReferencedByDocs` 关联，方便从源码快速定位对应文档页面

### ⭐ New Features

- 新增协议无关的通用 Server Builder，用于私有协议或未知协议场景
- 新增 JT/T 808 协议专用 Server Builder，覆盖指令/附件的 TCP 与 UDP 构建
- 新增 JT/T 1078 协议专用 Server Builder，覆盖音视频 TCP 与 UDP 构建

### 🐞 Bug Fixes

- 修复服务器启动监听器重复初始化时可能无法启动 Netty Server 的问题

### ❤️ Contributors

- [OpenAI Codex](https://openai.com/codex/)
- [@hylexus](https://github.com/hylexus)

## 0.7.0 (2026-08-08)

### 📔 Documentation

- 新增文档 [@EncodedLength](https://iotplanet.top/xtream-codec/guide/core/annotation-driven/encoded-length.html)

### ⭐ New Features

- 新增 [@EncodedLength](https://iotplanet.top/xtream-codec/guide/core/annotation-driven/encoded-length.html) 注解，支持编码时自动计算指定字段范围的实际编码字节数并回填到长度字段
- `@EncodedLength` 支持 `from` / `until` 声明左闭右开的字段范围；未指定边界时可从长度字段之后开始，或延伸到实体末尾
- `@EncodedLength` 支持 `u8`、`u16`、`u32` 无符号整数字段，并在元数据注册阶段校验非法引用、字段顺序、重复声明和不支持的长度字段格式

### 🚀 Improvements

- 编码器新增编码长度计划和运行时辅助逻辑；普通实体保持原有快速路径，带 `@EncodedLength` 的实体在编码阶段按实际写入字节数回填长度
- 更新 `DemoMessage005` 示例，演示 `@EncodedLength` 的典型协议体长度回填场景

### 🔨 Dependency Updates

- Gradle Wrapper 升级至 `9.7.0`
- 更新 Gradle Kotlin DSL 写法，替换已废弃或不推荐的 `settings` 委托、`tasks.registering` 和 `apply(plugin = "...")` 用法

### ❤️ Contributors

- [OpenAI Codex](https://openai.com/codex/)
- [@hylexus](https://github.com/hylexus)

## 0.6.0 (2026-06-02)

### 📔 Documentation

- 新增文档 [@DerivedField](https://iotplanet.top/xtream-codec/guide/core/annotation-driven/derived-field.html)
- 新增文档 [多版本支持](https://iotplanet.top/xtream-codec/guide/core/annotation-driven/multi-version.html)

### ⭐ New Features

- 新增 [@DerivedField](https://iotplanet.top/xtream-codec/guide/core/annotation-driven/derived-field.html) 注解

### ❤️ Contributors

- [opencode AI](https://github.com/anomalyco/opencode)
- [@hylexus](https://github.com/hylexus)

## 0.5.0 (2026-05-30)

### 📔 Documentation

- 新增 [服务端自定义注解示例](https://hylexus.github.io/xtream-codec/guide/server/samples/custom-demo-protocol/) 文档

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
- `XtreamMapField.ValueLength`
    - 删除 `io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField.ValueLength`
    - 使用 `io.github.hylexus.xtream.codec.core.annotation.ext.ValueLength` 替代
- `XtreamMapField.FallbackValueMatcher`
    - 删除 `io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField.FallbackValueMatcher`
    - 使用 `io.github.hylexus.xtream.codec.core.annotation.ext.FallbackValueMatcher` 代替
- `XtreamMapField.ValueMatcher`
    - 删除 `io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField.ValueMatcher`
    - 使用 `io.github.hylexus.xtream.codec.core.annotation.ext.ValueMatcher` 代替
- `XtreamMapField.DecoderParam`
    - 删除 `io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField.DecoderParam`
    - 使用 `io.github.hylexus.xtream.codec.core.annotation.ext.ValueDecoderCommonParam` 代替

### ⭐ New Features

- 新增内置编解码器 `StringFieldCodecAscII`

## 0.5.0-rc.3 (2026-05-27)

### ⭐ New Features

- 重构 `AbstractMapFieldCodec`

## 0.5.0-rc.2 (2026-05-22)

### 🎯 Highlights

`jt-808-server-dashboard-ui` 重构。感谢 [@dfEric](https://github.com/dfEric) 的贡献。

### ⭐ New Features

- [可观测性 #7](https://github.com/hylexus/xtream-codec/issues/7)
- [核心代码去掉 Lombok #12](https://github.com/hylexus/xtream-codec/issues/12)

### ❤️ Contributors

- [@dfEric](https://github.com/dfEric)
- [@hylexus](https://github.com/hylexus)

## 0.5.0-rc.1 (2026-01-11)

### ⭐ New Features

- 兼容低版本 `spring-boot` [#11](https://github.com/hylexus/xtream-codec/issues/11)

## 0.4.0 (2026-01-03)

### ⭐ New Features

- 多表达式引擎支持 [#5](https://github.com/hylexus/xtream-codec/issues/5)
- 数据类型扩展 [#6](https://github.com/hylexus/xtream-codec/issues/6)
    - `io.github.hylexus.xtream.codec.core.type.TLV`
    - `io.github.hylexus.xtream.codec.core.type.Pair`
    - `io.github.hylexus.xtream.codec.core.type.simple.DataField`
- 可观测性 - 后端 [#8](https://github.com/hylexus/xtream-codec/issues/8)

## 0.3.0 (2025-10-26)

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

## 0.2.0 (2025-10-18)

### ⭐ New Features

- 增强 `Record` 类型的表达式解析功能
- 增强 `Record` 类型的 `CodecTracker` 埋点
- 重构 `BeanPropertyMetadata.PropertyGetter` 和 `BeanPropertyMetadata.PropertySetter` 的实现类
