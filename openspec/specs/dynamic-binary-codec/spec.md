# dynamic-binary-codec Specification

## Purpose

提供不依赖特定协议或预编译 Java 实体的动态二进制编解码能力，使应用能用一份运行时结构定义对私有协议数据执行生产级编码、解码和可选跟踪。

## Requirements

### Requirement: 结构定义与值分离

系统 SHALL 提供独立于待编码值和待解码字节的可序列化结构定义。同一份定义 SHALL 用于两个方向；有序结构的字段 SHALL 按定义顺序处理，字段标识 SHALL 能唯一定位嵌套字段。

#### Scenario: 复用定义执行双向编解码

- **WHEN** 用户用一份定义描述整数、长度前缀字符串和嵌套结构，并分别提交字段值与对应字节
- **THEN** 编码与解码 SHALL 使用相同的字段顺序和长度语义
- **THEN** 解码结果 SHALL 保留按定义可识别的字段值

#### Scenario: Java 结构与 JSON 资源不依赖 Jackson 实现

- **WHEN** 宿主在没有 Jackson 2 实现的环境中通过 Java 构造动态结构
- **THEN** 结构 SHALL 能正常编译并执行双向编解码；JSON Schema 资源读取 SHALL 不触发 JSON 转换器初始化
- **THEN** 动态结构的 JSON 读写入口 SHALL 由宿主提供 Jackson 2 databind 后使用

### Requirement: 定义校验和可复用计划

系统 SHALL 在模型构造或 JSON 导入时拒绝字段自身可判定的无效定义，包括非法长度来源、字段属性组合及有限取值。Java API 对有限取值 SHALL 提供枚举，JSON SHALL 保持字符串交换格式。系统 SHALL 在执行前校验依赖字段上下文的规则并生成不可变计划。计划 SHALL 可被并发复用；执行状态、输入游标和跟踪数据 SHALL 限于单次调用。重复标识、无效引用、循环依赖或无法确定的解码边界 SHALL 在编译时报告字段路径和原因。

#### Scenario: 并发复用计划

- **WHEN** 多个线程用同一个已编译计划处理不同输入
- **THEN** 每次调用 SHALL 得到与单独执行一致的结果，且字段值、游标和诊断互不混用

#### Scenario: 拒绝无法确定边界的定义

- **WHEN** 变长字段未声明任何长度来源
- **THEN** 模型构造或 JSON 导入 SHALL 失败，并指出缺失的边界规则
- **WHEN** 使用容器剩余长度的字段后面仍有其他字段
- **THEN** 编译 SHALL 失败，并指出该字段路径

### Requirement: 基础字节字段

整数类型 SHALL 使用 `uN` 或 `iN` 声明符号与 1–64 bit 位宽，不得同时指定 `bits`；浮点数和原始位串 SHALL 继续使用显式 `bits`。类型名称 SHALL 在编译阶段解析，执行计划 SHALL 不重复解析名称。

系统 SHALL 双向支持 core 已有的有符号和无符号整数宽度、大小端浮点数、可配置字符集字符串、BCD 字符串、HEX 与原始字节、固定长度及填充规则。数值范围、无效字符编码和字节不足 SHALL 产生可定位的错误；超过 JSON 安全整数范围的值 SHALL 有无损表示。

#### Scenario: 混合基础类型往返

- **WHEN** 定义包含大端与小端整数、浮点数、UTF-8/GBK 字符串、BCD 和字节数组
- **THEN** 编码后按同一份定义解码 SHALL 还原各字段值及其原始字节布局

#### Scenario: 无损处理长整数

- **WHEN** 字段值超出 JavaScript 安全整数范围
- **THEN** 可序列化输入和输出 SHALL 使用无损表示，编码 SHALL 保持准确位值

### Requirement: 长度和边界规则

系统 SHALL 支持以 bit、byte 或元素个数为明确单位的固定长度、前置长度、引用已解析字段的长度、容器剩余长度，以及按编码后字段区间计算并回填的长度。编码和解码 SHALL 使用一致的边界解释；解码 SHALL 拒绝越过当前容器或输入末尾的长度。

#### Scenario: 引用前置长度字段

- **WHEN** 结构中的 `payloadLength` 声明后续 `payload` 的字节长度
- **THEN** 解码 SHALL 按该值限定 `payload`，编码 SHALL 按定义校验或计算该长度

#### Scenario: 回填跨字段区间长度

- **WHEN** 长度字段声明统计后续多个字段的编码字节数
- **THEN** 编码 SHALL 写入这些字段实际占用的字节数，包含满足条件才出现的字段

#### Scenario: 无边界嵌套结构消耗剩余空间

- **WHEN** 未声明独立长度的 struct 最后一个子字段使用 remaining，且该 struct 后面仍有同级字段
- **THEN** 编译 SHALL 拒绝定义，并指出会消耗外层剩余空间的 struct 路径

#### Scenario: 数组元素和选择分支消耗剩余空间

- **WHEN** 按 item 计数的数组有多个可能元素且元素使用 remaining，或 choice 的一个分支使用 remaining 后仍有同级字段
- **THEN** 编译 SHALL 拒绝定义；具有独立 bit/byte 边界的容器 SHALL 允许其末尾子节点使用 remaining

### Requirement: 容器与动态结构

系统 SHALL 双向支持嵌套结构、重复元素集合、键值 Map、TLV、Pair、由已解析值选择的分支及按版本选择的字段。Map/TLV SHALL 可按键或标签匹配值结构，并能保留未匹配项的原始字节；有重复键语义的结果 SHALL 保留条目顺序，不得静默覆盖。

#### Scenario: 解码嵌套重复结构

- **WHEN** 容器包含计数字段和若干个同构嵌套元素
- **THEN** 解码 SHALL 按计数读取元素，并返回有序结果及每个元素的字段值

#### Scenario: TLV 未知标签

- **WHEN** TLV 的标签无匹配结构，但其长度有效
- **THEN** 解码 SHALL 消费该条目的完整边界并保留标签和原始值字节

#### Scenario: 版本和判别值选择字段

- **WHEN** 输入版本及先前读取的判别字段确定一个分支
- **THEN** 两个方向 SHALL 只处理该版本和分支包含的字段

### Requirement: 编译时选择协议版本

系统 SHALL 接受独立于 schemaVersion 的目标协议版本，并在编译时按字段 versions 选择布局。精确版本 SHALL 优先于默认定义，均无匹配则跳过；嵌套结构 SHALL 沿用目标版本。重叠版本及筛选后的无效引用 SHALL 在编译期拒绝。计划 SHALL 固定目标版本，执行时无需再传入版本；计划生命周期 SHALL 由调用方管理。

#### Scenario: 同一字段按版本改变宽度

- **WHEN** 同一字段 id 提供默认 u8 定义和 versions 为 [2019] 的 u16 定义
- **THEN** 2013 计划 SHALL 使用 u8，2019 计划 SHALL 使用 u16，并可分别反复执行

#### Scenario: 活动字段引用被版本排除的字段

- **WHEN** 当前版本的活动字段引用了仅在另一版本中存在的长度字段
- **THEN** 编译 SHALL 报告该引用路径，不得把错误延迟到报文执行时

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

### Requirement: 连续位字段与整数位段

系统 SHALL 支持按定义的位序顺序读写连续位字段，包括跨字节字段、显式对齐和填充值，并 SHALL 支持对已读取整数的位段建立不额外消费字节的视图。位宽、符号解释、位序和对齐策略 SHALL 在定义中明确且双向一致。

#### Scenario: 跨字节位字段往返

- **WHEN** 顺序字段分别占 3 bit、5 bit 和 12 bit，随后声明按指定填充值对齐到字节
- **THEN** 编码 SHALL 产生确定的字节序列，解码 SHALL 还原全部字段，并验证对齐位

#### Scenario: 同一整数的多个位段视图

- **WHEN** 两个位段视图引用同一个已读取的整数
- **THEN** 解码 SHALL 返回各自的位值，且输入游标 SHALL 只因源整数移动一次

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

### Requirement: Pair 剩余边界传播
编译器 SHALL 对按项 Pair 中可能消费外层剩余空间的分支传播边界要求，并拒绝非末尾或多个条目共享外层剩余空间的定义。

#### Scenario: 末尾单条目
- **WHEN** Pair 固定一个条目且位于外层末尾，分支消费剩余空间
- **THEN** 定义允许编译并可往返编解码

#### Scenario: 不安全的共享边界
- **WHEN** 相同 Pair 位于非末尾或条目数可能大于一
- **THEN** 编译器拒绝定义，固定零条目和独立字节边界不受此限制

### Requirement: Choice 默认分支
系统 SHALL 支持 choice 的 defaultVariant，在判别键没有匹配显式分支时选择它，并沿用分支的版本、作用域、条件和边界规则。

#### Scenario: 未知判别键
- **WHEN** 判别键无显式匹配且存在适用版本的默认分支
- **THEN** 编解码执行默认分支

#### Scenario: 默认分支消费剩余空间
- **WHEN** 默认分支可能消费外层剩余空间
- **THEN** choice 参与容器末尾的边界校验
