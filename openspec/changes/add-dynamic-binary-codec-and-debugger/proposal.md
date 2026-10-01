# Proposal

## Why

现有 `EntityCodec` 依赖预先编写的 Java 实体类，`DataField` 主要支持动态编码，无法用同一份运行时结构定义完成双向编解码。私有协议开发需要可用于生产的协议无关动态编解码能力，以及一个无需 JT/T 808 或 Spring Boot 服务即可使用的可视化调试入口。

## What Changes

- 在 `xtream-codec-core` 的独立 `dynamic` 包中新增与 `EntityCodec` 并列的动态二进制编解码器：结构定义与值分离，定义可校验并编译为可并发复用的不可变计划，支持 `ByteBuf` 编码、解码和可选跟踪。
- 用同一份声明式定义覆盖 core 常用的数字与字节序、字符串与 BCD、长度与填充、嵌套结构、重复集合、Map/TLV/Pair、条件与版本选择、动态分支、派生值及受控自定义扩展；支持连续位字段跨字节组合、显式对齐和整数位段视图。
- 扩展结构化跟踪结果，保留既有字节范围接口，并为位字段提供精确位范围与诊断位置。
- 新增独立的轻量调试应用，提供实体类和自由组合两种模式、双向编解码、结果与 Trace 查看；HTTP 层不依赖 JT/T 808，也不要求启动 Spring Boot。
- 将可复用的调试业务服务放入 core.debugger 包，与独立应用的 HTTP 适配器分离，不单独发布服务模块；Java 请求、响应和实体模板使用 Map 字段值，便于以后在 JT/T 808 Dashboard Starter 中接入同一能力。
- 保持现有 `DataField`、`DataFieldEncoder` 和 `EntityCodec` 对外行为与源码兼容。本次没有计划中的破坏性变更。

## Capabilities

### New Capabilities

- `dynamic-binary-codec`: 运行时结构定义、校验和编译、生产级双向编解码、位级组合、扩展点及故障定位。
- `generic-codec-debugger`: 独立轻量调试应用、实体类与自由组合模式、HTTP 合同、可视化结果和复用边界。

### Modified Capabilities

- `codec-tracing`: 为位级动态编解码新增可选的精确位范围及位偏移诊断，同时保持现有字节范围和页面消费合同。

## Impact

- 主要影响 `xtream-codec-core` 的新 `dynamic` 包及现有 `CodecTrace`/`CodecTraceView` 模型；现有实体编解码路径继续独立工作。
- 增加独立的调试应用后端与前端模块及其 Gradle、前端构建配置，不依赖 `ext/jt`。
- 增加动态结构、编解码、位级边界、错误诊断、独立 HTTP 接口与页面交互的测试和文档。
- `debug/jt/jt-808-server-spring-boot-starter-debug-ui` 的早期字段编辑器只作参考，不作为新运行时依赖。
