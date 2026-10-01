## ADDED Requirements

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
