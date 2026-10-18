## Purpose

提供结构化的编解码调试埋点能力，使实体编解码过程可以被直接访问、序列化展示，并支持网页版报文调试页面进行字段树与报文字节范围的双向定位。

## Requirements

### Requirement: Public tracking API compatibility

The system SHALL preserve existing public tracking entry points for debug encode and decode operations.

- Existing public encode/decode APIs that accept a tracker SHALL remain source compatible.
- Existing public encoder/decoder methods with tracker variants SHALL remain source compatible.
- Existing field codec tracker variants SHALL remain source compatible.
- A null tracker passed through nullable public convenience APIs SHALL continue to use the production encode/decode path.

#### Scenario: Decode with existing tracker argument

- **WHEN** an application calls an existing entity decode overload with a tracker argument
- **THEN** the call SHALL compile and decode the entity through the debug path
- **THEN** the tracker SHALL contain trace data for the completed decode operation

#### Scenario: Encode with null tracker convenience overload

- **WHEN** an application calls an existing entity encode overload whose tracker argument is null
- **THEN** the system SHALL encode through the production path
- **THEN** the system SHALL NOT require trace objects to be created

### Requirement: Structured codec trace

The system SHALL produce a structured trace for debug encode and decode operations.

- The trace SHALL identify the operation direction as encode or decode.
- The trace SHALL expose the root entity type when known.
- The trace SHALL contain ordered trace nodes representing the encoded or decoded structure.
- Byte ranges SHALL use offsets relative to the traced input or output buffer segment.

#### Scenario: Decode simple entity

- **WHEN** a simple entity is decoded with tracking enabled
- **THEN** the resulting trace SHALL contain one root node and ordered child nodes for decoded fields
- **THEN** each decoded field node SHALL include the bytes consumed by that field
- **THEN** each decoded field node SHALL identify its FieldCodec as the processor type when available

#### Scenario: Encode simple entity

- **WHEN** a simple entity is encoded with tracking enabled
- **THEN** the resulting trace SHALL contain one root node and ordered child nodes for encoded fields
- **THEN** each encoded field node SHALL include the bytes written for that field

#### Scenario: Trace node metadata

- **WHEN** an application inspects a node from a debug encode or decode trace
- **THEN** each trace node SHALL expose a stable node id, parent relation, node kind, display name, field path when available, Java type when available, processor type when available, value summary when available, byte range, and local hex representation
- **THEN** the processor type SHALL identify the actual component responsible for the node's codec operation or structural orchestration

#### Scenario: Encode structured property

- **WHEN** a nested entity, collection, or map property is encoded with tracking enabled
- **THEN** the resulting trace SHALL contain a hierarchical node for that property
- **THEN** the property node SHALL identify the component that organized its child codec operations as the processor type when available

### Requirement: Nested and collection trace hierarchy

The system SHALL trace nested entities, collections, maps, and generated length fields hierarchically.

- Nested entity fields SHALL contain child nodes for their nested fields.
- Collection fields SHALL contain child nodes for encoded or decoded items.
- Map fields SHALL contain child nodes for encoded or decoded entries.
- Generated or backfilled length fields SHALL be identifiable as length-related trace nodes.
- Item index information SHALL be represented separately from byte offsets.

#### Scenario: Decode nested collection

- **WHEN** an entity containing a nested collection field is decoded with tracking enabled
- **THEN** the trace SHALL contain a collection node under the owning field path
- **THEN** each item node SHALL expose its item index and consumed byte range

#### Scenario: Encode generated length field

- **WHEN** an entity containing an automatically backfilled length field is encoded with tracking enabled
- **THEN** the trace SHALL identify the length field node
- **THEN** the trace SHALL expose the final bytes written for the length field

### Requirement: Visit-based direct access

The system SHALL continue to support direct tree traversal of tracked codec data.

- Applications SHALL be able to visit trace nodes in deterministic depth-first order.
- Visitor callbacks SHALL receive the node depth and node data.
- Visiting a trace SHALL NOT require JSON serialization or Web UI components.

#### Scenario: Visit tracked decode

- **WHEN** an application decodes an entity with tracking enabled and calls the visitor API
- **THEN** the visitor SHALL be invoked for the root node and each descendant node
- **THEN** callback order SHALL match the trace tree order

### Requirement: Web debug trace view

The system SHALL expose trace data in a form suitable for a Web-based packet encode/decode debugging page.

- The view SHALL include the complete traced hex payload.
- The view SHALL include trace tree nodes with byte ranges.
- The view SHALL allow a frontend to highlight bytes for a selected node.
- The view SHALL allow a frontend to identify owning nodes for a selected byte offset.
- The view SHALL provide JSON-friendly value representations for byte arrays and binary wrapper values.

#### Scenario: Highlight selected field bytes

- **WHEN** a frontend receives a trace view and a user selects a field node
- **THEN** the frontend SHALL be able to determine the exact byte range to highlight without searching for the node's hex text

#### Scenario: Locate node from selected byte

- **WHEN** a frontend receives a trace view and a user selects a byte offset
- **THEN** the frontend SHALL be able to determine the trace nodes whose byte ranges contain that offset

### Requirement: Error diagnostics

The system SHALL preserve useful trace data when a debug encode or decode operation fails after partially processing a buffer.

- The trace SHALL record diagnostic entries for failures detected during tracked operations.
- A diagnostic SHALL include message information and the best available node or byte location.
- Successfully recorded nodes before the failure SHALL remain visitable and serializable.

#### Scenario: Decode fails after partial field processing

- **WHEN** a tracked decode operation fails after one or more fields have been decoded
- **THEN** the tracker SHALL retain nodes recorded before the failure
- **THEN** the trace SHALL contain a diagnostic describing the failure location when available

### Requirement: Production path isolation

The system SHALL keep production encode/decode operations independent from debug trace collection.

- Production encode/decode calls without a tracker SHALL NOT allocate trace nodes.
- Production encode/decode calls without a tracker SHALL NOT depend on Web debug DTO types.
- Debug trace collection SHALL remain opt-in through existing tracker-enabled entry points or explicitly introduced trace APIs.

#### Scenario: Production decode without tracker

- **WHEN** an application decodes an entity through a public API without a tracker
- **THEN** the entity SHALL decode without creating trace node data
- **THEN** the decode result SHALL remain identical to the equivalent tracked decode result, excluding trace side effects

### Requirement: 位级跟踪位置

跟踪模型 SHALL 为实际只占部分字节的字段提供可选的起止 bit 偏移，并在位级失败时提供 bit 位置。现有节点字节范围、字节索引和 Web 视图字段 SHALL 继续可用；跨字节位字段的字节范围 SHALL 覆盖其触及的全部字节。

#### Scenario: 两个位字段共享一个字节

- **WHEN** 跟踪一次将 3 bit 与 5 bit 字段写入同一字节的编码
- **THEN** 两个节点 SHALL 有不同且准确的 bit 范围，其字节范围 SHALL 都覆盖该字节

#### Scenario: 既有字节消费者继续工作

- **WHEN** 仅识别现有字节范围字段的页面读取新增位级跟踪结果
- **THEN** 它 SHALL 仍能显示字段与原始字节的关联，无须依赖新 bit 字段

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
