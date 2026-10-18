## Why

当前结构化 trace 虽然替换了旧的 Span 数据模型，但编解码埋点流程仍依赖 `CodecTracker` 的可变当前节点、临时提示状态和手动坐标恢复。普通字段、嵌套实体和 Map 都被迫使用同一套顺序敏感的底层操作，导致实现难以理解、扩展和排错。

现在已经有稳定的 `CodecTrace` 数据模型和 Web 调试消费场景，适合把“如何记录埋点”和“如何展示 trace”重新分层，消除旧版实现对埋点流程的持续影响。

## What Changes

- 保留 `FieldCodec`、`EntityCodec`、`EntityEncoder`、`EntityDecoder` 的 tracker 编解码入口以及 `CodecTracker#visit` 能力。
- 将内部埋点流程改为 scope/栈式记录，自动维护父子节点关系，删除 `current` 指针和一次性 hint 字段。
- 将 ByteBuf slice、临时 ByteBuf 和坐标平移封装到内部坐标映射能力中，不再要求普通 codec 手动处理坐标栈。
- 统一普通字段、嵌套实体、集合项和 Map entry 的进入、完成和异常记录流程。
- 保留 Map 动态 value、长度字段等需要特殊处理的能力，但把临时 segment 和回填逻辑限制在对应的内部 codec 实现中。
- 让 trace 节点优先保存范围和值等结构化信息，Web 视图在可行时根据根 payload 和范围生成字段 hex，减少调用方重复转换。
- 将节点处理组件统一表示为 `processorType`，准确覆盖基础 FieldCodec、容器 metadata、长度策略和 DataField 编码器。
- 将现有 `CodecTracker` 调整为兼容门面；内部实现不再以旧版 Span 操作协议作为主要编程模型。

## Capabilities

### Modified Capabilities

- `codec-tracing`: 将含义不完整的 codec 类型字段统一为实际处理组件类型，并保持结构化 trace 的诊断和展示合同明确。

## Impact

- 影响 `xtream-codec-core` 中的 `CodecTracker`、trace recorder、`FieldCodec` tracker 默认方法，以及嵌套实体、集合、Map、TLV、动态类型等 tracker 分支。
- 需要同步调整 core 埋点测试，并继续验证生产路径不创建 trace 节点。
- 不改变实体编解码语义或报文字节布局；Web trace view 将 `codecType` 迁移为语义明确的 `processorType`。
- 不涉及 JT808/JT1078 协议专用埋点和前端页面交互改造。
