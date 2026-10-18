# Spec Delta

## ADDED Requirements

### Requirement: 位级跟踪位置

跟踪模型 SHALL 为实际只占部分字节的字段提供可选的起止 bit 偏移，并在位级失败时提供 bit 位置。现有节点字节范围、字节索引和 Web 视图字段 SHALL 继续可用；跨字节位字段的字节范围 SHALL 覆盖其触及的全部字节。

#### Scenario: 两个位字段共享一个字节

- **WHEN** 跟踪一次将 3 bit 与 5 bit 字段写入同一字节的编码
- **THEN** 两个节点 SHALL 有不同且准确的 bit 范围，其字节范围 SHALL 都覆盖该字节

#### Scenario: 既有字节消费者继续工作

- **WHEN** 仅识别现有字节范围字段的页面读取新增位级跟踪结果
- **THEN** 它 SHALL 仍能显示字段与原始字节的关联，无须依赖新 bit 字段

