---
title: DSL 配置参考
---

# DSL 配置参考

第一次编写定义，先读[入门教程](./quick-start.md)。本页用于查属性、值的形状及配置之间的约束；可以直接运行的整份报文见[常用示例](./examples.md)。

## 顶层结构

一份完整定义只有两个顶层属性：

```json
{
  "schemaVersion": 1,
  "fields": [
    { "id": "sequence", "type": "u16" }
  ]
}
```

| 属性            | 必填 | 含义                           |
|-----------------|------|--------------------------------|
| `schemaVersion` | 是   | DSL 格式版本，当前只能为整数 1 |
| `fields`        | 是   | 按线上读写顺序排列的字段数组   |

定义与编码值分开提交。上例的编码值为 `{"sequence":4660}`，编码 HEX 为 `1234`。定义 JSON 不接受注释、尾逗号或未知属性；`$schema` 也不是定义属性。为外部编辑器关联 JSON Schema 时，在编辑器配置中设置关联。

下面的 JSON 除明确标为完整定义外，均为**字段片段**或**属性片段**，需放入对应位置。

## 字段属性总表

每个字段都是一个对象。不要把所有属性都填上，先按 `type` 选择适用属性。

| 属性               | 使用条件与默认值                                                                |
|--------------------|---------------------------------------------------------------------------------|
| `id`               | 必填，符合 `[A-Za-z_][A-Za-z0-9_]*`；同级在选定协议版本中唯一，用于值的键和引用 |
| `name`             | 可选显示名称，可重复，不参与寻址                                                |
| `type`             | 必填，取下表支持的类型                                                          |
| `bits`             | `float` 和 `bits` 类型必填；整数已在 `uN/iN` 名称中声明位宽，不能重复填写       |
| `endian`           | 数值字节序，`big`（默认）或 `little`；小端要求完整字节宽度                      |
| `bitOrder`         | 位序，`msb0`（默认）或 `lsb0`；共享一个字节的字段必须一致                       |
| `length`           | 字节标量、扩展、数组和条目集合必填；struct 可选；其他类型不能声明               |
| `charset`          | 仅 string，默认 `UTF-8`，也可用 `GBK` 等 JVM 支持的字符集                       |
| `alignTo`          | 仅 align 必填，单位 bit，取 1、2、4、8、16、32、64                              |
| `fillBit`          | align 填充位，0（默认）或 1                                                     |
| `ref`              | choice 必填，引用先前整数判别字段；derived 位段视图引用源整数                   |
| `computedLengthOf` | 无符号整数的编码长度回填目标，同级后续单字段或 `first..last` 区间               |
| `fields`           | 仅 struct 必填，子字段数组                                                      |
| `item`             | 仅 array 必填，一个完整的元素字段定义                                           |
| `variants`         | 仅 choice、tlv、map、pair 必填，十进制判别键到字段定义的对象映射                |
| `defaultVariant`   | 仅 choice 可选，未匹配时使用的完整字段定义                                      |
| `when`             | 可选存在条件；不成立就不读写该字段                                              |
| `expression`       | 仅 derived 的派生表达式，与 derived.ref 互斥                                    |
| `versions`         | 可选非空整数数组；省略表示默认布局，精确版本优先                                |
| `generated`        | 线上标量的编码生成规则，与 computedLengthOf 互斥                                |
| `options`          | 类型专属选项，详见[选项表](#类型专属选项)                                       |

## 类型与 JSON 值

| type                   | 配置重点                            | 编码值 / 解码值                                            |
|------------------------|-------------------------------------|------------------------------------------------------------|
| `uN`                   | 无符号，N 为 1–64，如 u3、u16       | 整数，范围为 0 到 2ᴺ−1                                     |
| `iN`                   | 有符号二进制补码，N 为 1–64         | 整数，范围为 −2ᴺ⁻¹ 到 2ᴺ⁻¹−1                               |
| `float`                | bits 为 32 或 64                    | 有限数值，如 `1.5`                                         |
| `string`               | length 必填，charset 可选           | 字符串，如 `"设备A"`                                       |
| `bcd`                  | length 必填；每字节两位十进制数字   | 偶数位数字字符串，如 `"001234"`，保留前导零                |
| `hex`                  | length 必填                         | 偶数位 HEX 字符串，如 `"aabb"`；解码为小写，不带 0x 或空格 |
| `bytes`                | length 必填                         | 0–255 的数组，如 `[170,187]`；Java 编码也接受 byte[]       |
| `bits`                 | bits 为位串长度，最多 1048576       | 只含 0/1 的字符串，如 `"101"`，长度必须匹配                |
| `padding`              | length 必填，options.padByte 默认 0 | 编码无需提供值；解码验证字节并返回 null                    |
| `align`                | alignTo 必填，fillBit 默认 0        | 编码无需提供值；解码验证补位并返回 null                    |
| `struct`               | fields 必填，length 可选            | 子对象，如 `{"code":1,"value":2}`                          |
| `array`                | length 和 item 必填                 | 有序数组，如 `[1,2]` 或 `[{"code":1}]`                     |
| `choice`               | ref 和 variants 必填                | **直接使用选中分支的值**，不增加分支 id 的包装             |
| `tlv` / `map` / `pair` | length 和 variants 必填             | 有序条目数组，如 `[{"key":1,"value":12}]`                  |
| `derived`              | ref + 位段 options，或 expression   | 不占用新的报文空间；解码返回计算结果                       |
| `extension`            | length 和 options.extensionId 必填  | 由宿主注册的扩展规定值格式                                 |

整数绝对值超过 JSON 安全整数上限 `9007199254740991` 时，用十进制字符串传值，解码也返回十进制字符串。例如完整 u64 最大值写作 `"18446744073709551615"`，不能交给浏览器 Number 保存。

string 的长度按**字符集编码后的字节数**计算。例如 UTF-8 的 `"中"` 是 3 字节，GBK 是 2 字节。BCD 的 `"001234"` 是 3 字节，HEX 的 `"aabb"` 是 2 字节。浮点数只描述线上数值格式，不自动处理协议规定的倍率；例如温度单位为 0.1°C 的 i16，23.5°C 应提供整数 235。

## 字节序、位序和对齐

`{"id":"n","type":"u16","endian":"little"}` 把 4660（0x1234）编码为 `3412`；默认大端为 `1234`。按字段声明字节序，不要依赖父容器给子字段隐式设置。

默认 msb0 下，顺序的 u3=5、u5=17 合成 `10110001`，即 `b1`。lsb0 从每字节低位开始，整数也先处理低有效位。**位序与字节序是两个不同设置**，详见[位序说明](./README.md#位序、字节序与对齐)。

当位字段没有结束在字节边界上，下一项是字节字段时，显式增加：

```json
{ "id": "align", "type": "align", "alignTo": 8, "fillBit": 0 }
```

alignTo 相对于本次调用起点；解码还会检查填充位。一次调用结束也必须处于字节边界。`padding` 是明确数量的**整字节**，`align` 是补到边界所需的**位**。

## 长度来源与单位

### length 五选一

length 必须有 `unit`，并且只选一个来源：

| 来源   | length 属性片段                                                                 | 何时使用                                            |
|--------|---------------------------------------------------------------------------------|-----------------------------------------------------|
| 固定   | `{"fixed":6,"unit":"byte"}`                                                     | 协议规定固定占用 6 字节                             |
| 引用   | `{"ref":"size","unit":"byte"}`                                                  | 前面已有整数长度字段；也可用 header.size 等限定路径 |
| 前缀   | `{"prefixBits":16,"unit":"byte"}`                                               | 内容前自带 16 bit 长度；允许 8、16、32              |
| 表达式 | `{"expression":{"op":"sub","args":[{"ref":"size"},{"value":2}]},"unit":"byte"}` | 长度需要从前序值计算，例如总长减去头部              |
| 剩余   | `{"remaining":true,"unit":"byte"}`                                              | 消费当前容器剩余内容，必须处于其末尾                |

fixed、ref 和 expression 指定的长度都需要与实际内容相符。固定空间不够时拒绝编码，不截断；内容短于空间时，需要显式 padByte 才能填充适用的字节字段。

`prefixBits` 会在报文中**实际增加一个长度头**，编码自动写入，解码自动读取，不必再声明一个相同用途的 u16。长度不包含前缀自身。例如 string 值 `"AB"`，8 bit 字节长度前缀的结果为 `02 41 42`。

### unit 选择

| unit   | 含义           | 使用范围                                         |
|--------|----------------|--------------------------------------------------|
| `byte` | 内容总字节数   | 字节标量、struct、array、tlv/map/pair、extension |
| `bit`  | 内容总 bit 数  | 上述类型；字节标量的边界必须是整字节             |
| `item` | 元素或条目个数 | 仅 array、tlv、map、pair                         |

两项 u16 的数组可以用 `fixed: 2, unit: "item"` 或 `fixed: 4, unit: "byte"`，含义分别是两项和总共四字节。按字节约束集合时，TLV/Map 条目的 key 和 length 头也在集合内容长度中。

### 容器边界与 remaining

无 length 的 struct 与外层共享边界。若最后一个子字段使用 remaining，这个 struct 也必须处于外层末尾；若后面还有校验字段，应先给 struct 声明独立长度。

choice 本身不能声明 length，边界写在具体分支字段上。分支里的 remaining 若没有独立容器边界，会消费外层剩余空间，choice 因而也必须位于外层末尾。

按 item 计数的数组不为每个元素提供独立字节边界，不能让重复元素各自吞掉外层 remaining。重复变长记录可给 item 的 struct 增加 `length.prefixBits`；TLV/Map 本来就有逐条目长度。更完整的容器末尾限制见[长度与容器](./README.md#长度与容器)。

## 容器和分支的值形状

| 定义意图                      | 编码值片段                                | 说明                                  |
|-------------------------------|-------------------------------------------|---------------------------------------|
| id=header 的 struct           | `{"header":{"size":2}}`                   | 子字段位于对象中                      |
| id=numbers 的 u16 数组        | `{"numbers":[1,2]}`                       | 不写 `[{"item":1}]`                   |
| id=items 的 struct 数组       | `{"items":[{"code":1}]}`                  | 不再包装 item.id                      |
| id=body 的 choice 选中 u16    | `{"kind":1,"body":4660}`                  | 不写 `{"body":{"branchId":4660}}`     |
| id=body 的 choice 选中 struct | `{"kind":2,"body":{"value":4660}}`        | 使用该 struct 自己的子字段            |
| id=extras 的已知 TLV 键       | `{"extras":[{"key":1,"value":12}]}`       | value 直接使用匹配分支的值            |
| id=extras 的未知 TLV/Map 键   | `{"extras":[{"key":99,"rawHex":"aabb"}]}` | 保留原始内容；不包含 key 和 length 头 |

variants 的键是**非负十进制字符串**，例如 `"512"`，不能用 `"0x0200"`。每个分支都是含 id/type 的完整字段定义，可以是 struct，也可以是标量。

TLV 与 Map 的布局都是 `key + valueLength + value`，valueLength 为 value 的字节数；Pair 是 `key + value`，未知键没有可确定的值边界，会报错。三种类型的值都是有序数组，保留重复键，不使用 JSON 对象合并键。

## 条件与协议版本

### when：这个字段是否存在

```json
{
  "id": "extra", "type": "u16",
  "when": { "ref": "flags", "op": "bitSet", "value": "4" }
}
```

flags 的掩码 4 对应 bit 满足条件时才读写 extra。`value` 在简写条件中写为字符串。可用运算符为 `eq/ne/gt/gte/lt/lte/bitSet/bitClear`，位测试的 value 是**掩码**，不是 bit 下标。条件不成立时，编码无需值，解码结果不含这个字段，也不占用线上空间。

when 只能引用先前可见字段；源字段被跳过时，简写条件的依赖字段也跳过，包括 ne。复杂组合条件使用 `when.expression`，其结果必须为 Boolean；它与 ref/op/value 简写互斥。

### choice：这个位置使用哪一种结构

先声明 kind 等整数判别字段，再声明 `type: "choice", ref: "kind", variants: {...}`。只处理选中的分支；没有匹配项时使用可选的 defaultVariant，没有适用默认分支则报错。完整例子见[条件与消息类型分支](./examples.md#条件与消息类型分支)。

### versions：编译时选择布局

| 版本概念         | 来源                                            | 作用                                 |
|------------------|-------------------------------------------------|--------------------------------------|
| DSL 格式版本     | 顶层 schemaVersion                              | 当前为 1，决定如何解释定义文件       |
| 目标协议版本     | Studio 顶部输入或 Java compile(schema, version) | 编译时选择字段布局                   |
| 报文里的版本字段 | 普通 u8 等字段的值                              | 按报文读写，可参与 when 等运行时判断 |

versions 省略表示默认定义；同一 id 可再提供 `versions: [2019]` 的精确版本，精确匹配优先于默认定义。无版本参数的编译只选默认定义。首次出现的位置决定该 id 的字段顺序，多个默认定义或重叠版本会被拒绝。

版本递归应用于子结构和分支。它不从报文中自动识别协议，也不会自动生成报文内的版本字段，见[多版本示例](./examples.md#多版本布局)。

## 引用作用域

普通 ref、when、表达式先查当前作用域已可用的字段，再查外层字段。可用 `header.count` 等限定路径消除歧义；数组和条目只读取当前元素及外层，不读取上一元素的数据。

普通引用不能指向后面的未知值。以下是有明确用途的例外：

- computedLengthOf 引用**同级后续字段**，编码结束目标区间后回填。
- generated.countOf 可以引用**同级后续集合**，从输入列表取得项数。
- generated.checksum.range 可以覆盖同级前方或后方字段，编译器检查回填顺序和依赖环。

尚未回填的长度或校验值不能提前用于条件和表达式。长度 ref 保留专门的延迟检查机制，可用于入门教程里的“先回填长度，再约束 payload”组合。

## 回填、生成和派生

| 配置                 | 编码值中可省略                       | 是否占用线上空间       | 解码行为                       |
|----------------------|--------------------------------------|------------------------|--------------------------------|
| computedLengthOf     | 回填字段                             | 是，按它的 uN 类型写入 | 读取实际长度；目标仍要声明边界 |
| generated.constant   | 固定值字段                           | 是                     | 读取实际值，不断言等于常量     |
| generated.countOf    | 数量字段                             | 是                     | 读取实际数量                   |
| generated.expression | 生成字段                             | 是                     | 读取实际值，不与表达式结果比较 |
| generated.checksum   | 校验字段                             | 是                     | 读取实际值并核对校验和         |
| derived.ref          | 位段视图；也可提供位段反向合成源整数 | 否                     | 从源整数提取位段               |
| derived.expression   | 派生字段                             | 否                     | 根据先前值计算结果             |

computedLengthOf 仅用于无符号整数，默认单位 byte，可设 `options.lengthUnit: "bit"`。目标 `"body"` 表示一个完整字段，`"first..last"` 包含两端和中间所有实际写出的字段。条件跳过的字段不计长度。

generated 四种来源必须且只能选一种，且与 computedLengthOf 互斥：

| generated 属性片段                                                 | 要求                                                               |
|--------------------------------------------------------------------|--------------------------------------------------------------------|
| `{"constant":126}`                                                 | 用于整数、float、string、bcd、hex、bytes、bits，仍需满足位宽与长度 |
| `{"countOf":"items"}`                                              | 字段为无符号整数，目标为同级 array/tlv/map/pair，编码输入提供列表  |
| `{"expression":{"op":"mul","args":[{"ref":"count"},{"value":2}]}}` | 用于线上标量，只引用先前可用值                                     |
| `{"checksum":{"algorithm":"xor8","range":"first..last"}}`          | 同级、字节对齐区间，不能包含校验字段自身                           |

校验算法 xor8、sum8 使用 u8，crc32、crc32c 使用 u32。区间按最终线上字节计算，包括范围内长度头、填充和已完成回填；默认解码返回核对结果，不匹配也继续解析。Java 严格模式和结果读取见[字节区间校验和](./README.md#字节区间校验和)。

derived 位段字段片段：

```json
{
  "id": "mode", "type": "derived", "ref": "flags",
  "options": { "bitOffset": 0, "bitWidth": 3 }
}
```

bitOffset 从**整数最低有效位**开始计数，与报文中从高位依次声明 u3/u5 的写法不同。源字段必须已定义，位段不能超出其位宽；源值和位段输入同时提供时必须一致。

## 表达式

默认支持结构化 AST，无需注册语言引擎。每个节点三选一：`{"ref":"count"}`、`{"value":2}`、`{"op":"mul","args":[...]}`。下面是派生字段片段：

```json
{
  "id": "doubleCount", "type": "derived",
  "expression": { "op": "mul", "args": [{ "ref": "count" }, { "value": 2 }] }
}
```

| 放置位置             | 结果要求                        |
|----------------------|---------------------------------|
| derived.expression   | JSON 可表达的派生值             |
| when.expression      | Boolean                         |
| length.expression    | 非负精确整数，仍需设置 unit     |
| generated.expression | 符合目标标量类型和位宽/长度的值 |

算术支持 add/sub/mul/div/mod；整数位运算 bitAnd/bitOr/bitXor/bitNot/shl/shr；比较 eq/ne/gt/gte/lt/lte；布尔 and/or/not。bitNot/not 一元，其他均二元。支持 `+`、`*`、`&` 等符号别名，完整表见[表达式运算符](./README.md#派生值与反向位段)。布尔 and/or 会短路，要求 Boolean，不把整数当真假值。

字符串形式 `{"engine":"SpEL","source":"#n + 2","bindings":{"n":"count"}}` 需要宿主显式注册对应引擎，并与 AST 属性互斥。默认 Studio 应用不启用字符串引擎。宿主接入方式和执行边界见[复用宿主表达式引擎](./README.md#复用宿主表达式引擎)。

## 类型专属选项

以下属性都放在字段的 `options` 对象里。

| 选项         | 适用场景                   | 取值和默认行为                                                  |
|--------------|----------------------------|-----------------------------------------------------------------|
| padByte      | 字节字段补齐；padding 填充 | 0–255；普通字节字段只有显式指定后才允许短值补齐，padding 默认零 |
| padSide      | 上述补齐方向               | left 或 right，默认 right                                       |
| stripPadding | 字节值解码前去填充         | 默认 false；true 时去掉所选侧连续 padByte，可能丢失业务数据     |
| keyBits      | tlv/map/pair 键宽          | 8（默认）、16、32                                               |
| lengthBits   | tlv/map 条目长度头宽       | 8（默认）、16、32；pair 不能填写                                |
| bitOffset    | derived 位段               | 非负整数，最低有效位起算，必须与 bitWidth 一起提供              |
| bitWidth     | derived 位段               | 正整数，位段必须在源整数范围内                                  |
| lengthUnit   | computedLengthOf 回填单位  | byte（默认）或 bit；与 length.unit 不是同一属性                 |
| extensionId  | extension                  | 宿主已注册的稳定标识；不是 Java 类名                            |

key 与条目长度头使用引擎规定的大端布局，不通过容器 endian 改写。需要小端的 value 可在它自己的数值字段上设置 endian。

options 可供扩展携带自定义配置，但不意味着任意拼写都能产生效果；上层字段属性则严格拒绝未知名称。扩展需要宿主注册和校验，参见[宿主注册扩展](./README.md#宿主注册扩展)。

## 校验与编辑补全

Studio 的 `/dynamic-codec-schema.json` 和 core 的 `DynamicSchemaJson.jsonSchema()` 提供相同版本的 JSON Schema，可辅助外部编辑器提示属性、枚举和结构约束。编辑器关联 Schema 后，仍应应用定义并用真实报文验证。

导入检查局部属性和互斥组合，编译检查引用/版本/依赖，执行检查实际位宽、值与边界。按阶段排查见[配置错误排查](./troubleshooting.md)。
