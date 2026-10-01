# Spec Delta

此 delta 基于尚未归档的 `add-dynamic-binary-codec-and-debugger` 中同名规格；归档前先同步该前序规格。以下 MODIFIED 块完整替换同名需求，其他前序需求保持有效。

## MODIFIED Requirements

### Requirement: 条件、派生值与扩展

系统 SHALL 支持基于已知字段值的声明式条件和派生规则，并为内置 DSL 无法描述的编解码或双向转换提供由应用预先注册的扩展标识。定义中的扩展引用 SHALL 只解析到注册项；来自序列化定义的任意 Java 类名 SHALL NOT 被直接加载执行。表达式默认 SHALL 限于受控运算；仅当宿主显式启用相应引擎时，系统 SHALL 按 dynamic-codec-expressions 合同接受 engine/source/bindings 形式，不隐式启用脚本能力。

#### Scenario: 条件字段与派生位视图

- **WHEN** 一个条件字段由先前的标志位决定，且派生字段从已解析整数的位段得到值
- **THEN** 解码 SHALL 只消费实际存在的字段并给出派生值，编码 SHALL 按定义生成一致的源整数与条件布局

#### Scenario: 未注册扩展

- **WHEN** 定义引用应用未注册的自定义处理器
- **THEN** 编译 SHALL 失败，并指出引用位置，不得按类名动态加载执行

#### Scenario: 已注册表达式引擎

- **WHEN** 定义包含宿主明确启用的引擎表达式
- **THEN** 系统 SHALL 按已声明字段绑定求值并校验结果，不改变其他字段和扩展的执行顺序

### Requirement: 执行结果与生产路径

系统 SHALL 提供直接面向 ByteBuf 的编码和解码入口。解码结果 SHALL 表明消费量并能区分剩余输入；未启用跟踪时 SHALL 不创建 Trace 节点或依赖 Web 展示类型。生产编码 SHALL 不为生成未请求的展示结果构造完整值树或无条件反向解码字段。后续语义确实依赖规范化值时 SHALL 按需获取且在单次执行中复用。已有 DataField、DataFieldEncoder、EntityCodec 以及既有动态入口的公开行为 SHALL 保持兼容，本变更明确纠正的错误行为除外。

#### Scenario: 按边界消费输入

- **WHEN** 一个缓冲区包含目标结构及其后的其他字节
- **THEN** 解码 SHALL 只消费目标结构的字节，并报告消费量及剩余字节

#### Scenario: 生产调用与跟踪调用结果一致

- **WHEN** 同一计划及输入分别在启用和不启用跟踪时执行
- **THEN** 编解码值和字节 SHALL 一致；未启用跟踪的调用 SHALL 不创建 Trace 节点

#### Scenario: 普通编码不触发无用的扩展解码

- **WHEN** 无 Trace 编码一个没有后续值依赖的扩展字段
- **THEN** 该次编码 SHALL 调用扩展 encode 一次且不调用扩展 decode

#### Scenario: 编码期间按需规范化

- **WHEN** 后续条件或表达式依赖一个已编码扩展字段的规范化值
- **THEN** 引擎 SHALL 对实际写出字节调用该扩展 decode 至多一次，并复用该值
- **THEN** 多个依赖及 Trace SHALL NOT 对同一字段重复调用扩展 decode

### Requirement: 失败定位与资源边界

编译和执行错误 SHALL 包含字段路径及可用的 bit 或 byte 偏移，转换底层异常时 SHALL 保留原因。执行 SHALL 校验长度、计数、递归深度与输出大小边界；失败时 SHALL 保持调用方缓冲区所有权约定，并在启用跟踪时保留已完成节点。引擎自身可预知大小的字节转换 SHALL 在分配前检查有效上限，包括未知 TLV 的 rawHex。缺少必需输入和类型错误 SHALL 形成可定位错误，不得向调用方泄漏无上下文的空指针异常。解码失败 SHALL 不提交 readerIndex；编码失败允许保留已写部分，调用方负责丢弃该结果。

#### Scenario: 输入在嵌套位字段处截断

- **WHEN** 解码进行到嵌套结构的位字段时输入不足
- **THEN** 系统 SHALL 报告该字段路径与失败位偏移，已成功解码字段的 Trace SHALL 可访问

#### Scenario: 必需字符串值缺失

- **WHEN** 编码值中缺少一个必需字符串字段
- **THEN** 系统 SHALL 报告该字段路径及当前位置，不得抛出无上下文的 NullPointerException

#### Scenario: 未知 TLV 原始值超限

- **WHEN** 未知条目的 rawHex 解码后大小超过条目前缀容量、父容器剩余量或全局输出上限
- **THEN** 系统 SHALL 在分配其完整字节数组前拒绝并指出所属条目位置

## ADDED Requirements

### Requirement: 节点位置不改变条件语义

系统 SHALL 对普通字段、数组元素、choice 分支及已匹配的条目值执行声明的条件。条件为 false 的节点 SHALL 不执行内容读写或扩展求值。具名字段被跳过时 SHALL 从结果中省略；choice 的已选分支被跳过时其具名 choice SHALL 被省略。数组及条目仍 SHALL 遵守已有进度和长度边界，不能因跳过节点而静默违反计数或消费后续字段。

#### Scenario: 已选 choice 分支条件为假

- **WHEN** flag 为 0，choice 选择键 0 的分支，而分支声明 flag 等于 1 的条件
- **THEN** 编解码 SHALL 跳过该分支和对应的 choice 值，不消费或生成分支字节

#### Scenario: 数组元素被跳过

- **WHEN** 数组尝试处理一个条件为 false 的元素
- **THEN** 系统 SHALL 报告该元素未推进游标，不能读取本应属于后续字段的字节

#### Scenario: 条目值被跳过但声明长度非零

- **WHEN** 一个 TLV 已匹配分支被条件跳过，而条目声明非零内容长度
- **THEN** 解码 SHALL 报告条目长度不匹配并保留失败位置

### Requirement: 无业务值节点与缺失字段分离

系统 SHALL 区分因条件跳过而缺失的字段和已执行但不产生业务值的 padding/align 节点。后者在已有具名结果中 SHALL 保留 null；位于集合元素或条目值时 SHALL 允许 null 结果。集合空进度保护仍 SHALL 有效。

#### Scenario: 解码 padding 集合

- **WHEN** 固定 1 项的数组元素是长度为 1 byte 的 padding，输入为正确填充字节
- **THEN** 解码 SHALL 消费 1 byte 并返回包含一个 null 的列表，不得因不可变集合拒绝 null 而失败

### Requirement: 集合局部值与回填隔离

数组元素及条目局部值 SHALL 不泄漏至其他元素或并发调用。长度回填 SHALL 基于本次执行的实际区间，正确处理条件跳过的起止字段、嵌套集合和多个关联长度；未完成的回填占位值 SHALL NOT 被普通表达式或条件作为已解析整数读取。

#### Scenario: 相邻元素字段有条件缺失

- **WHEN** 第一元素包含某个局部字段而第二元素跳过它
- **THEN** 第二元素的引用 SHALL 观察到字段缺失，不能使用第一元素的值

#### Scenario: 条件区间长度回填

- **WHEN** 长度区间的起点或终点字段被跳过，且该区间位于重复执行的结构中
- **THEN** 每个结构实例 SHALL 只回填自身实际写出的长度，不影响其他实例

#### Scenario: 表达式读取待回填长度

- **WHEN** 表达式或条件在编码长度区间结束前引用待回填的长度字段
- **THEN** 计划编译 SHALL 拒绝该依赖并报告字段路径
- **THEN** 原有 length.ref 引用对应回填长度以限定其目标内容的用法 SHALL 继续可用
