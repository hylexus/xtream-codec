---
article: false
---

# 发版记录

## 0.0.1-beta.11(2025-01-19)

### ⭐ New Features

- `jt-808-server-dashboard-ui` 优化
- `jt-808-server-dashboard-spring-boot-starter-reactive` 兼容 **SpringMVC**
- 新增 `quck-start/jt/jt-808-server-quick-start-with-storage-blocking` 示例模块
- 内置编解码器支持十六进制字符串
- 调整虚拟线程默认配置

## 0.0.1-beta.10(2025-01-01)

### ⭐ New Features

- `jt-808-server-dashboard-ui` 优化
- `xtream-codec-server-reactive` 模块配置项默认值调整
- 新增 `quck-start/jt/jt-808-server-quick-start-with-storage` 示例模块

### ❤️ Contributors

- [@dfEric](https://github.com/dfEric)
- [@hylexus](https://github.com/hylexus)

## 0.0.1-beta.9(2024-12-22)

### ⭐ New Features

- `jt-808-server-dashboard-ui` 优化
- `jt-808-server-spring-boot-starter-reactive`
    - 新增 `Jt808MessageArgumentResolver`
    - 新增 `Jt808RequestEntityArgumentResolver`
    - 新增 `Jt808ResponseEntityHandlerResultHandler`
    - 使用 `@XtreamField.prependLengthFieldType` 属性简化部分内置消息的注解配置

### 📔 Documentation

- 完善 `xtream-codec-server-reactive` 和 `jt-808-server-spring-boot-starter-reactive` 部分文档

### ❤️ Contributors

- [@dfEric](https://github.com/dfEric)
- [@hylexus](https://github.com/hylexus)

## 0.0.1-beta.8(2024-12-15)

### ⭐ New Features

- `jt-808-server-dashboard-ui` 优化

### 📔 Documentation

- 新增 `xtream-codec-server-reactive` 和 `jt-808-server-spring-boot-starter-reactive` 部分文档

### ❤️ Contributors

- [@dfEric](https://github.com/dfEric)
- [@hylexus](https://github.com/hylexus)

## 0.0.1-beta.7(2024-12-08)

### ⭐ New Features

- `jt-808-server-dashboard-ui` 优化
- 集成 `reactor-core-micrometer`

### 🐞 Bug Fixes

- 修改部分 `Scheduler` 配置未生效的问题

### ❤️ Contributors

- [@dfEric](https://github.com/dfEric)
- [@hylexus](https://github.com/hylexus)

## 0.0.1-beta.6(2024-12-01)

### ⭐ New Features

- 新增 `jt-808-server-dashboard-spring-boot-starter-reactive` 模块
- 新增 `jt-808-server-dashboard-ui` 模块

### ⚠️ Breaking Changes

- `xtream-codec-ext-jt-808-server-spring-boot-starter` 重名为 `jt-808-server-spring-boot-starter-reactive`

### ❤️ Contributors

- [@dfEric](https://github.com/dfEric)
- [@hylexus](https://github.com/hylexus)

## 0.0.1-beta.5(2024-11-24)

### ⭐ New Features

- 优化 `Jt808SessionManager`
- 优化 `Jt808CommandSender`
- 优化 `XtreamEventPublisher`
- 内置 **actuator** 指标
- 请求解码逻辑移动到 `Filter` 之前
- 新增 `RequestDispatcherSchedulerFilter`

### ⚠️ Breaking Changes

- 配置项重新调整

## 0.0.1-beta.4(2024-11-02)

### ⭐ New Features

- 新增 **JT/T 808** 部分消息映射示例实体类
- `@XtreamField` 注解新增属性:
    - `prependLengthFieldType()`
    - `prependLengthFieldLength()`
    - `iterationTimes()`
    - `iterationTimesExpression()`

### 🐞 Bug Fixes

- 修复 `NestedBeanPropertyMetadata` 没有调用 `FieldConditionEvaluator` 的问题

## 0.0.1-beta.3(2024-10-20)

### ⭐ New Features

新增两个子模块:

- **xtream-codec-server-reactive**: 和具体协议格式无关的 **纯异步**、<font color="red">非阻塞</font> 的服务端
- **jt-808-server-spring-boot-starter-reactive**: 基于 **xtream-codec-server-reactive** 实现的 **JT/T 808** 服务端扩展

### ⚠️ Breaking Changes

- **LICENSE** 从 **MulanPSL2** 改为 <font color="red">Apache License 2.0</font>。改动原因如下：
    1. 项目里复制并修改了很多 **spring** 的源码
    2. **spring** 是使用 **Apache License 2.0** 开源的
    3. **MulanPSL2** 能兼容 **Apache License 2.0**，但反过来不行
- `FiledDataType.nested` 重命名为 `FiledDataType.struct`
- `@Preset.JtStyle.BCD` 重命名为 `@Preset.JtStyle.Bcd`

## 0.0.1-beta.2(2024-04-19)

### ⭐ New Features

- 新增 `@XtreamFieldMapDescriptor` 注解支持 `java.util.Map` 类型的编解码
- `@XtreamField` 新增 `containerInstanceFactory()` 属性

## 0.0.1-beta.1(2024-03-24)

### ⭐ New Features

- 新增 `ByteArrayContainer` 工具类
- 新增 `@Preset.RustStyle.byte_array` 注解

### 🐞 Bug Fixes

- 修复 `NestedBeanPropertyMetadata` 没有考虑 `FieldConditionEvaluator` 的问题

### 📔 Documentation

- 新增 **JT/T 808** 协议地理位置消息的解析示例

## 0.0.1-beta.0(2024-03-10)

### ⭐ New Features

- `EntityCodec`
- `@XtreamField` 注解支持
- 提供 `Rust` 命名风格 和 `JT/T 808` 命名风格的内置注解
