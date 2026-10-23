# Proposal

## Why

JT808 内置消息包使用 @NullMarked，但条件字段、版本字段和已知可空字段的类型声明未完整表达实际空值，导致 IDEA 将合法的 assertNull 推导为必然失败。字段、getter 和 setter 参数的契约也存在不一致。

## What Changes

- 为 builtin.messages 中条件成立时才存在的引用类型字段补充 JSpecify @Nullable，并同步 getter、setter 参数及相关构造参数。
- 补充按版本省略的引用字段和已有测试、编解码器明确支持的可空字段，保留已有标注。
- 基本类型及必要字段保持原类型，不改变协议表达式和报文字节；0102 合一实体的软件版本格式化 getter 在字段缺省时返回 null。
- 复核标注完整性，运行 JT808 测试、样例编译、Checkstyle 和许可证检查。

## Capabilities

### New Capabilities

无。

### Modified Capabilities

无。本次仅修正既有空值契约，设置 skip_specs: true。

## Impact

涉及 JT808 内置实体及其访问器的类型注解和导入；不增加依赖，不改变字段类型及报文字节。
