# Spec Delta

## ADDED Requirements

### Requirement: 有序条目的完整跟踪

系统 SHALL 按输入数组或报文顺序返回 TLV/map/pair 的 MAP_ENTRY 节点，用从零开始的条目序号构造路径，并通过属性记录键值。每个条目 SHALL 包含 Tag（TLV）或 Key（map/pair）、Length（pair 除外）和 Value 子节点，分别返回实际类型、值和独立位及字节范围；条目范围 SHALL 覆盖整个头部和内容。已知 Value SHALL 保留分支类型、说明及嵌套结构，未知 Value SHALL 以 hex 类型保留原始内容。跟踪 SHALL 不改变业务值、重复键、条目顺序或编码字节。

#### Scenario: 非递增键及重复键

- **WHEN** 编码或解码包含非递增键、重复键和未知键的条目集合
- **THEN** 条目 SHALL 保持原顺序且路径可区分，未知条目 SHALL 能独立定位 Tag、Length 和原始 Value；编码回填 Length SHALL 显示最终长度

#### Scenario: 条目头部或内容失败

- **WHEN** 条目头部不足、内容截断或编码内容失败
- **THEN** Trace SHALL 保留已经完成的子节点及失败范围，解码已读取的 Length SHALL 保留其值，编码未完成的 Length 回填 SHALL 不显示成功值

### Requirement: 动态字段声明类型跟踪

系统 SHALL 在启用跟踪的动态编解码节点中通过 `attributes.dataType` 返回字段声明类型，保留 `uN`、`iN` 及非整数类型的声明名称；嵌套字段、数组元素和实际执行分支 SHALL 返回各自的类型，长度前缀 SHALL 返回对应位宽的 `uN`。附加类型元数据 SHALL 不改变节点类别、位范围、业务值或编解码结果，关闭跟踪时 SHALL 不创建类型元数据节点。

#### Scenario: 编码和解码字段类型

- **WHEN** 启用跟踪并执行动态编码或解码
- **THEN** 相同字段 SHALL 返回相同的声明类型，已开始但失败的字段 SHALL 保留类型，合成的长度前缀 SHALL 使用对应的无符号整数类型

### Requirement: 动态长度前缀跟踪

系统 SHALL 在启用跟踪的动态编解码中，将已消费的 prefixBits 长度前缀作为 LENGTH_FIELD 同级节点返回，插入对应字段之前，包含独立位范围、字节范围、最终长度值与长度单位。前缀 SHALL 与对应字段具有相同 parentId，通过 fieldPath 属性关联对应字段，并以包含目标字段名的节点名称区分同一容器内的多个前缀。对应字段 SHALL 保留包含前缀的完整范围，并通过 contentBitStart/contentBitEnd 属性提供内容范围；关闭跟踪时 SHALL 不创建这些节点。

#### Scenario: 编码及解码前缀字段

- **WHEN** 动态字段以 prefixBits 声明字符串、字节、结构或集合长度
- **THEN** 编码和解码 SHALL 返回相同的前缀范围、值及内容范围，编码回填的前缀 SHALL 显示最终值；启用跟踪 SHALL 不改变字节或业务值

#### Scenario: 多个字段和数组元素前缀

- **WHEN** 同一容器内存在多个前缀字段，或数组元素自身声明前缀
- **THEN** 每个前缀 SHALL 位于对应字段之前且与其同级，前缀路径 SHALL 可区分，标量内容字段 SHALL 不因长度前缀而成为可展开的容器

#### Scenario: 前缀后的内容失败

- **WHEN** 解码已读取完整前缀后内容不足，或编码容器内容失败而无法回填前缀
- **THEN** Trace SHALL 保留已读取的前缀值，未完成的回填前缀 SHALL 不被标为成功，内容范围 SHALL 仅覆盖已处理部分
