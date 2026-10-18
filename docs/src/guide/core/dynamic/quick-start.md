---
title: 从协议表写出第一份 DSL
---

# 从协议表写出第一份 DSL

动态 DSL 是一份 JSON：它告诉编解码器按什么顺序、用多少位读写字段。**定义描述报文布局，编码值描述这一次要发送的数据**，两份 JSON 分开填写。

本页用 Studio 的“位字段与嵌套集合”样例，走完协议分析、定义、编码和解码。无需先写 Java 实体，也无需配置表达式引擎。

还没有启动编辑器时，先按 [Studio 启动与部署](./studio.md)运行应用，再回到本页配置报文。

## 先列清楚协议字段

假设设备上报一组数据，整数默认大端，高位先写：

| 顺序 | 字段            | 线上长度       | 含义                                        |
|------|-----------------|----------------|---------------------------------------------|
| 1    | `version`       | 3 bit          | 协议版本，本例为 5                          |
| 2    | `flags`         | 5 bit          | 状态标志，本例为 17；和 version 合占 1 字节 |
| 3    | `length`        | 1 字节         | 后面整个 payload 的字节数，不含自身         |
| 4    | `payload.count` | 1 字节         | 数据项数量                                  |
| 5    | `payload.items` | count × 3 字节 | 每项由 code 和 value 组成                   |
| 5.1  | 每项的 `code`   | 1 字节         | 数据标识                                    |
| 5.2  | 每项的 `value`  | 2 字节         | 无符号整数                                  |

写自己的协议前，先确认字段顺序、有无符号、位宽、字节序、字符串编码、变长字段的结束位置。尤其要分清协议里的“长度”表示字节数、bit 数还是条目数量。

## 把字段表翻译为配置

### 类型、标识和顺序

`u3` 表示 3 bit 无符号整数，`u5` 表示 5 bit 无符号整数，`u16` 表示 16 bit 无符号整数。有符号数用 `iN`，例如 `i16`。

字段片段：

```json
{ "id": "version", "name": "协议版本", "type": "u3" }
```

`id` 用于编码值的键、其他字段引用和错误定位；`name` 只用来显示。使用 `version` 传值，不使用“协议版本”。`id` 符合 `[A-Za-z_][A-Za-z0-9_]*`。

**`fields` 数组顺序决定线上顺序。** 编码值对象里的键可以换顺序。前两个字段共 8 bit，无需补位；没有占满字节时，需要显式的 `align`，不能期待引擎自动跳过。

### 长度字段与负载边界

这两个配置要配套理解，下面是字段片段：

```json
{ "id": "length", "type": "u8", "computedLengthOf": "payload" }
```

```json
{
  "id": "payload",
  "type": "struct",
  "length": { "ref": "length", "unit": "byte" },
  "fields": []
}
```

`computedLengthOf` 让编码器在写完 payload 后回填它实际占用的字节数，因此编码值中可省略 `length`。`payload.length.ref` 则明确 payload 的边界：解码时从已经读到的 length 得知 payload 有多长。

只写 `length.ref` 不会自动生成那个长度字段；只写 `computedLengthOf` 也不会自动给后续容器加上解码边界。

### 嵌套对象与数组

`struct.fields` 定义一个子对象。`array.item` 定义每个元素，`length` 指定数量或总长度。本例按 **item 数量**读取：

```json
{
  "id": "items",
  "type": "array",
  "length": { "ref": "count", "unit": "item" },
  "item": {
    "id": "item",
    "type": "struct",
    "fields": [
      { "id": "code", "type": "u8" },
      { "id": "value", "type": "u16" }
    ]
  }
}
```

count 为 2 就读取两项。本例 count 由用户提供；想根据列表自动生成数量，可以为 count 加 `generated.countOf`，见[常用示例](./examples.md#自动生成长度、数量和校验和)。

## 完整 DSL

下面是可独立导入的完整定义，也是示例测试使用的文件：

@[code json](@project/xtream-codec-core/src/test/resources/dynamic/docs/quick-start.json)

顶层 `schemaVersion: 1` 是 DSL 格式版本，与报文里的 `version: 5` 没有关联。

## 编码值

```json
{
  "version": 5,
  "flags": 17,
  "payload": {
    "count": 2,
    "items": [
      { "code": 1, "value": 4660 },
      { "code": 2, "value": 22136 }
    ]
  }
}
```

`payload` 对应一个对象，`items` 对应一个数组。元素直接写 `{"code":1,"value":4660}`，不额外包成 `{"item":{...}}`；item.id 标识元素定义，不增加值对象的层级。

## 预期报文与解码结果

编码结果为 **`b10702011234025678`**，共 9 字节、72 bit：

| 字节       | 来自哪个字段    | 计算或解释                       |
|------------|-----------------|----------------------------------|
| `b1`       | version + flags | `101` + `10001` = `10110001`     |
| `07`       | length          | count 1 字节 + 两项各 3 字节 = 7 |
| `02`       | payload.count   | 两项                             |
| `01 12 34` | `items[0] `     | code=1，value=4660（0x1234）     |
| `02 56 78` | `items[1]`      | code=2，value=22136（0x5678）    |

用同一份 DSL 解码这段 HEX，字段值为：

```json
{
  "version": 5,
  "flags": 17,
  "length": 7,
  "payload": {
    "count": 2,
    "items": [
      { "code": 1, "value": 4660 },
      { "code": 2, "value": 22136 }
    ]
  }
}
```

解码会返回报文里实际读到的 length。预期消费 9 字节，剩余输入为 0。

## 在 Studio 中验证

1. 打开 Studio，新建一个“自由组合”报文；也可直接打开“位字段与嵌套集合”预设。
2. 在“结构定义 JSON”中填入完整 DSL，点击“应用定义”。目标协议版本留空，本例只使用默认布局。
3. 在“编码值 JSON”中填入上面的编码值，点击“编码为 HEX”。
4. 检查结果是否为 `b10702011234025678`。将它放入“解码 HEX”，点击“解码为字段值”。
5. 检查解码值、消费长度和剩余输入。点击 Trace 中的 `payload.items` 及其元素，对照高亮字节；点击 `version` 和 `flags`，查看它们在同一字节中的不同 bit 范围。

完整定义编辑器支持“补全”按钮和 Ctrl + Space，可提示属性、枚举与可见字段引用。补全帮助填写，应用和执行时仍会检查配置关系及实际边界。

## 改成自己的报文

先按自己的字段表替换标识、类型和顺序，再逐个添加变长字段、条件和校验。每次用一段已知报文核对字段边界，避免一次性引入多个不确定配置。

- 查属性和输入形状：[DSL 配置参考](./dsl-reference.md)。
- 找类似结构：[常用报文配置示例](./examples.md)。
- 应用或运行失败：[配置错误排查](./troubleshooting.md)。
- 在 Java 中编译和复用定义：[编译与执行](./README.md#编译与执行)。
