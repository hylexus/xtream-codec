# Proposal

## Why

动态 DSL 的功能目标已经确定，但当前执行器仍依赖原始定义、路径 Map 和隐含的调用顺序，导致节点条件遗漏、错误定位不一致，以及编码时重建值树和反向解码等额外成本。需要重新设计内部执行合同，并复用 base 层的 `XtreamExpressionEngine` 接入宿主选择的表达式引擎，形成可测试、可维护的长期实现。

## What Changes

- 将 DSL 定义编译成按语义分组的不可变节点、明确的值引用、作用域布局、边界及回填动作；执行不再通过原始 DSL 解释配置。
- 分开编码与解码主流程，共享编译结果和语义原语；统一普通字段、集合元素和选择分支的条件、边界及错误规则。
- 保留公开 JSON 值表示及既有 Java 入口；生产编码不为展示构造完整结果树，普通整数和位操作采用 primitive 快速路径。
- 保留受控表达式树，新增 `engine + source + bindings` 表达式形式，复用 `XtreamExpressionEngine`、`XtreamExpression` 和宿主提供的注册表，在计划编译时绑定引擎并创建表达式。
- 条件、派生值及表达式长度/计数统一使用编译后的求值合同；明确变量可见性、缺失值、结果类型、并发隔离和异常位置。
- 修复已发现的条件绕过、padding 集合 NPE、缺少必需值 NPE 和未知 TLV 先分配后校验问题，增加组合场景回归与双向性能基准。
- 调试服务可由宿主注入表达式引擎；独立应用默认不开启字符串引擎表达式。既有实体编解码和 HTTP 响应合同保持兼容。

## Capabilities

### New Capabilities

- `dynamic-codec-expressions`: 受控表达式与宿主表达式引擎的编译、绑定、求值、结果检查及隔离合同。

### Modified Capabilities

- `dynamic-binary-codec`: 完善生产执行、节点组合、错误和资源边界，允许显式启用的引擎表达式。

该能力尚位于前序变更 `add-dynamic-binary-codec-and-debugger/specs/dynamic-binary-codec/spec.md`，未同步至主规格。本变更复用该能力名称，不建立平行规格；实现以该文件为基线，归档时必须先同步前序变更，再应用本变更 delta。本轮不自动归档或改写前序变更。

## Impact

- core：`dynamic/schema`、`dynamic/engine` 及新增内部表达式适配；公开入口使用兼容重载，内部类型不承诺兼容。
- base：复用现有表达式 API 和注册表；不重写注解表达式工厂，不改变默认注册表行为，不引入新的表达式依赖。
- debugger-service：增加宿主配置接入，保留默认禁用引擎表达式及现有请求/响应；页面通过 JSON 导入、编辑、导出支持新形式。
- verification：core、base 表达式、调试服务、独立应用、JT/T 808 动态 DSL 示例、JMH 和文档。

## Non-Goals

- 不替换既有 DSL 为字符串语言，不将 AST 翻译成 SpEL，不生成运行时字节码，不建立全局计划缓存。
- 不重构 `EntityCodec`，不增加自动依赖推断、表达式跨引擎翻译、通用脚本沙箱或安全强制中断机制。
- 不新增表达式编辑器 UI，不处理本次无关的页面状态问题，不改变既有 JT/T 808 Dashboard。
