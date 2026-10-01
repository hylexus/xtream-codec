# Spec Delta

## Purpose

为动态二进制 DSL 提供统一的表达式合同，在保留可分析的受控表达式树的同时允许宿主显式接入已有表达式引擎，并确保字段依赖、数值精度、作用域、错误位置和并发执行具有确定行为。

## ADDED Requirements

### Requirement: 兼容的表达式定义

系统 SHALL 保留现有 ref、value、op/args 表达式格式，并支持互斥的 engine/source/bindings 格式。引擎标识及源码 SHALL 为非空字符串，bindings SHALL 明确将合法变量名映射到 DSL 字段引用；混用两种表达式格式 SHALL 在定义校验时拒绝。schemaVersion 为 1 的既有定义及 Java 构造入口 SHALL 保持可用，以下明确调整的运算符命名除外：and/or SHALL 表示布尔逻辑运算，原先以 and/or 表示位运算的定义 SHALL 迁移到 bitAnd/bitOr 或 &/|；位运算 Java 枚举 SHALL 使用 BIT_AND/BIT_OR/BIT_XOR/BIT_NOT，逻辑运算 SHALL 使用 AND/OR/NOT。

#### Scenario: 往返既有表达式树

- **WHEN** 导入包含嵌套算术及布尔表达式的既有 JSON，再导出并执行
- **THEN** 字段引用、运算顺序、短路行为及公开结果表示 SHALL 保持一致

#### Scenario: 导入指定引擎的表达式

- **WHEN** 导入包含 engine、source 和 bindings 的定义
- **THEN** 系统 SHALL 保留这三个属性并可通过 Java API 构造等价定义
- **THEN** 若同时出现 op、args、ref 或 value，定义校验 SHALL 拒绝并指出定义位置

#### Scenario: 逻辑名称与符号一致

- **WHEN** 定义使用 and/or/not 或 &&/||/!，或导入兼容别名 all/any
- **THEN** 系统 SHALL 按布尔逻辑求值，and/or SHALL 短路；操作数 SHALL 为 Boolean，不采用真假值转换或返回操作数
- **THEN** 导出 SHALL 使用 and/or/not 规范名称

#### Scenario: 位运算名称与符号一致

- **WHEN** 定义使用 bitAnd/bitOr/bitXor/bitNot 或 &/|/^/~，或导入兼容别名 xor
- **THEN** 系统 SHALL 按精确整数执行位运算；bitNot/~ SHALL 使用无固定位宽的逐位取反语义，即 ~x = -x - 1
- **THEN** 导出 SHALL 使用 bitAnd/bitOr/bitXor/bitNot 规范名称

### Requirement: 宿主显式启用表达式引擎

系统 SHALL 仅使用宿主显式提供的表达式引擎解析引擎表达式。未配置引擎的现有编译入口、调试服务默认构造入口及独立调试应用默认启动方式 SHALL 仅允许受控表达式。DSL SHALL NOT 通过类名加载引擎。宿主启用通用引擎 SHALL NOT 被描述为对表达式提供了沙箱或安全强制中断保证。

#### Scenario: 默认拒绝字符串引擎表达式

- **WHEN** 未配置表达式引擎的编译入口收到 engine/source/bindings 定义
- **THEN** 编译 SHALL 失败并报告所属字段及未启用的引擎标识

#### Scenario: 绑定宿主提供的引擎

- **WHEN** 宿主提供的注册表包含表达式指定的引擎
- **THEN** 编译 SHALL 使用该实例创建表达式，不自动创建其他引擎
- **THEN** 计划生成后替换或移除注册项 SHALL NOT 改变已有计划绑定的引擎

### Requirement: 显式依赖与字段可用性

系统 SHALL 在计划编译时解析受控引用和 bindings，拒绝不存在、前向、循环、版本筛选后不可见及跨集合元素的引用。表达式的运行时变量 SHALL 仅绑定已声明的字段，不自动注入整个编码输入、后续字段、ByteBuf 或宿主服务。任意引擎源码内部的变量使用及动态访问不属于 DSL 编译器的静态依赖推断范围。

#### Scenario: 编译时拒绝不可见的绑定

- **WHEN** bindings 指向当前版本排除的字段、后续字段或其他数组元素的局部字段
- **THEN** 计划编译 SHALL 失败并报告表达式所属字段及绑定目标

#### Scenario: 条件跳过的绑定在运行时缺失

- **WHEN** 引擎表达式的已声明绑定字段在本次执行中被条件跳过
- **THEN** 求值 SHALL 在调用引擎前失败并指出缺失的绑定，不能复用上一元素的值
- **THEN** 既有简写 when 在条件源缺失时仍 SHALL 按 false 处理

### Requirement: 条件派生与长度的统一求值规则

系统 SHALL 支持在派生字段 expression、when.expression 和 length.expression 使用同一种表达式定义。旧 when 的 ref/op/value 与 when.expression SHALL 互斥；length.expression SHALL 与 fixed/ref/prefixBits/remaining 互斥，并继续要求 bit/byte/item 单位。条件结果 SHALL 为非空布尔值；长度及计数 SHALL 为精确的非负整数且满足字段、容器及全局上限，不得截断、溢出或将小数隐式取整。引擎表达式的数值算术遵循指定引擎，系统 SHALL NOT 承诺跨引擎运算结果一致。

#### Scenario: 使用绑定计算条件和派生值

- **WHEN** when.expression 返回 true，且派生表达式引用已经执行的前序字段
- **THEN** 两个方向 SHALL 执行相同的条件布局并返回符合公开值表示的派生结果
- **THEN** 条件返回字符串或数字时 SHALL 报告类型错误，不进行真假值转换

#### Scenario: 长度表达式与实际内容一致

- **WHEN** length.expression 计算出内容长度或元素个数
- **THEN** 解码 SHALL 在访问内容前建立边界，编码 SHALL 校验实际内容与结果一致
- **THEN** 负数、小数、超出表示范围或资源上限的结果 SHALL 在使用该长度前失败

#### Scenario: 保留大整数精度

- **WHEN** 表达式得到超过 JavaScript 安全整数范围的精确整数
- **THEN** 公开派生结果 SHALL 使用无损十进制字符串，后续整数引用 SHALL 保持原始精度

### Requirement: 预创建表达式与执行隔离

系统 SHALL 在计划编译阶段创建引擎表达式，执行阶段不得重复解析源码。同一计划的并发调用、嵌套作用域及集合元素 SHALL 隔离可变求值上下文。可接入引擎及其表达式实例 SHALL 满足并发求值合同，宿主注册不符合该合同的自定义引擎不属于计划的并发保证范围。

#### Scenario: 重复并发执行

- **WHEN** 一个计划用于多个线程和含不同元素值的集合
- **THEN** 表达式创建次数 SHALL 与计划编译次数相关而与报文及集合元素数量无关
- **THEN** 每次求值 SHALL 仅观察自身绑定，不受其他执行及上一元素的变量修改影响

### Requirement: 表达式失败诊断

表达式语法、绑定及运行时求值失败 SHALL 报告所属 DSL 字段路径、表达式用途和可用的 bit 偏移，保留原始异常因果链。启用 Trace 时 SHALL 保留此前完成的节点及失败诊断；不得将求值失败默认为 false、零长度或空值。

#### Scenario: 引擎求值失败

- **WHEN** 已注册引擎在求值时抛出异常
- **THEN** 调用方 SHALL 获得带字段位置和原始 cause 的动态编解码异常
- **THEN** 调试响应 SHALL 使用现有错误合同展示路径、偏移和部分 Trace
