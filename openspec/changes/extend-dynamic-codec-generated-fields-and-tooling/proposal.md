# Proposal

## Why

现有 DSL 存在按项 Pair 的剩余空间传播遗漏，可能生成无法按同一定义解码的报文。协议常量、集合计数和校验和仍需调用方重复生成，定义编辑也缺少机器可读的结构描述和补全支持。

## What Changes

- 修复 Pair 分支 remaining 的边界传播和重复条目校验。
- 增加互斥的字段生成配置：固定值、同级集合计数、前序字段表达式、字节区间校验和。
- 解码默认返回校验和核对结果，由调用方通过单次解码选项显式启用严格模式。
- 增加 choice 默认分支，沿用普通分支的作用域、条件和边界规则。
- 发布 schemaVersion 1 的 JSON Schema，并在调试页面提供属性、枚举和字段引用补全。
- 保持已有 Java 构造签名和 JSON 定义兼容；不增加业务断言、类型复用、终止符或变长整数。

## Capabilities

### New Capabilities

- `dynamic-codec-generation`: 协议固定值、计数、线上表达式值和字节区间校验和生成。
- `dynamic-codec-authoring`: JSON Schema 交换与调试编辑器补全。

### Modified Capabilities

- `dynamic-binary-codec`: 完善 Pair 边界校验并增加 choice 默认分支。

dynamic-binary-codec 尚未同步主规格，归档时先同步 add-dynamic-binary-codec-and-debugger 和 redesign-dynamic-codec-execution，再应用本变更。

## Impact

影响 core 的 schema、编译布局、执行器、测试和资源文件，以及独立调试页面和文档。新增属性为可选属性，不改变现有缓冲区所有权、跟踪及默认表达式引擎合同。
