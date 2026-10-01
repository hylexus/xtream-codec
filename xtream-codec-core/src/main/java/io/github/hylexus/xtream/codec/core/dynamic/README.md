# 动态编解码源码阅读路径

公开 DSL 用法见 `docs/src/guide/core/dynamic/README.md`。执行模型与 DSL 模型分开：后者用于构造、JSON 和诊断，前者只保存执行需要的数据。

## 编译到执行

1. `schema` 中的构造器冻结输入，检查字段及长度、条件、表达式的互斥属性。
2. `DynamicCompiler` 筛选版本，验证引用可见性、边界与回填布局。内部 `Field` 只是编译期中间树。
3. `DynamicLayoutCompiler` 把中间树降为不可变 `DynamicLayout`：引用变为 `Address(scope, slot)`，类型变为互斥的 `Operation`，长度变为 `LengthSource`；字符集、常量、扩展实例和回填动作在这里解析。中间树不进入执行器。
4. `DynamicPlan` 保留原始 schema 供公开访问，执行时只传布局。每次调用创建 `DynamicWriteExecution` 或 `DynamicReadExecution`，没有方向标志或公共执行模板。

## 状态所有权

| 对象                      | 生命周期及职责                                                          |
|---------------------------|-------------------------------------------------------------------------|
| `DynamicLayout` / `Node`  | 计划持有的不可变节点、地址、依赖及边界动作，可跨调用复用                |
| `DynamicFrame`            | 单次调用的槽位；MISSING、null、PENDING 分别表示跳过、无业务值、尚未回填 |
| `DynamicRuntime`          | 单次调用的游标、当前边界、集合计数、当前帧和可选 Trace；通过组合复用    |
| `DynamicWriteExecution`   | 编码主流程、按需规范化、回填；只返回消费 bit 数                         |
| `DynamicReadExecution`    | 解码主流程和结果树；成功后提交 readerIndex                              |
| `DynamicBitReader/Writer` | long 位模式及 ByteBuf 整字节快速路径，跨字节位布局和定点回填            |
| `DynamicByteFormat`       | 使用预编译格式转换字节，不再解释 DSL options                            |
| `DynamicExpressionValues` | AST 精确整数运算、只读绑定快照和结果校验                                |

普通 struct 的子字段在当前词法作用域内分配独立槽位。每个 array item、choice/TLV variant 在进入时创建自己的帧，退出即丢弃；外层引用按 scope 找到父帧，因此不会读到上一元素的数据。路径字符串只用于错误定位和 Trace 展示，不作为运行期值索引。

回填由编译器在区间两端布置动作，执行帧保存预留位置与区间起点。动作围绕条件入口执行，端点跳过时仍能结算实际消费量；重复元素各自保存状态。条件或表达式提前读取 PENDING 在编译时拒绝，既有 length.ref 延迟校验语义保留。

编码只在后续引用、容器收集或 Trace 需要时规范化值。无依赖 bytes 不重建整数列表，无依赖扩展只调用 encode；多个引用复用同一槽位中的规范化值。无 Trace 时不创建 Trace 节点和源范围数组。

## 表达式接入

AST 被编译为直接访问帧的求值函数。字符串表达式使用宿主显式提供的 `DynamicCompileOptions.expressionEngines`，绑定具体引擎与 `XtreamExpression`；注册表后续变动不影响已有计划。每次求值创建独立上下文，只注入已编译 bindings 的只读快照。条件要求 Boolean，长度要求非负精确整数，失败保留 cause、字段路径和位置。

上下文隔离不等于脚本沙箱；宿主决定可信来源、可用引擎及其资源策略。自定义引擎、编译表达式和扩展需满足并发复用合同。

## 修改节点时的落点

单节点属性检查放在 schema；跨字段可见性、版本、布局检查放在 DynamicCompiler；预解析放在 DynamicLayoutCompiler。新增操作要在 DynamicLayout 的 sealed Operation 中定义，并分别实现读写分支。执行错误在各自节点入口统一定位，普通字段、item 与 variant 都经过该入口。

测试阅读顺序：`DynamicQuickStartTest` 的 u3/u5、回填及嵌套数组样例 → `DynamicCompositionTest` 的条件端点与 null 组合 → `DynamicEngineExpressionTest` 的绑定、隔离与失败 → `DynamicExpressionQuickStartTest` 的公开宿主配置。测试中的 HEX 均独立写出，避免仅依赖编解码互相验证。
