---
title: DSL 配置错误排查
---

# DSL 配置错误排查

先确认使用的是同一份定义、同一目标协议版本和对应的 HEX。再看失败发生在“导入定义”“编译定义”还是“编码/解码”：相同的表面现象可能来自不同阶段。

## 看阶段、路径和位置

| 阶段                | 常见问题                                              | 先检查什么                         |
|---------------------|-------------------------------------------------------|------------------------------------|
| JSON 导入与局部检查 | JSON 语法、未知属性、类型不支持、互斥属性             | 字段拼写、类型、必填项及异常原因   |
| 编译                | 重复 id、前向引用、版本冲突、remaining 位置、回填依赖 | 定义中的字段路径与 fields 顺序     |
| 编码                | 缺值、值形状错误、位宽溢出、长度不符                  | 编码值中的 id、单位和实际值        |
| 解码                | 输入不足、容器边界错误、填充不符、分支无匹配          | HEX 是否完整、长度头、版本和帧边界 |

Java 中，编译与执行的字段错误通过 `DynamicCodecException.fieldPath()` 定位定义；编译期 `bitOffset()` 为 -1，执行期偏移相对于本次调用起点。局部构造或 JSON 导入错误也可能是普通 IllegalArgumentException，应查看原因链。

执行偏移从 0 起算：bit=16 表示到了本次报文的第 3 个字节位置。偏移是检测出错误时的位置，不一定是错误数据最早出现的位置；前面错误的长度头可能在更后面的字段触发失败。

Studio 中先展开诊断，再用 Trace 检查已完成的字段和字节高亮，逐段对照协议表。数组的定义路径与 Trace 展开的具体元素路径可能不同，结合元素序号查找。

## 定义 JSON 无法导入

**现象：** 定义无效、未知属性、未知字段类型或不合法的属性组合。

定义只接受顶层 schemaVersion 和 fields。去掉 JSON 注释、尾逗号、编辑器配置 `$schema` 等内容。查看[字段属性表](./dsl-reference.md#字段属性总表)核对拼写；不要把 lengthUnit 放成顶层字段属性，它属于 options。

早期整数片段：

```json
{ "id": "sequence", "type": "uint", "bits": 16 }
```

应改为：

```json
{ "id": "sequence", "type": "u16" }
```

整数类型自带位宽；float 和原始 bits 位串仍需 bits 属性。struct 必须有 fields，array 必须有 item，choice/tlv/map/pair 必须有 variants；不要混用这些子结构属性。

## 缺少长度、长度不符或输入不足

**现象：** 变长字段缺少边界、读取超出容器、编码实际长度不符，或后续字段总是少几个字节。

先核对五件事：

1. string、hex、bcd、bytes、extension、array 和 tlv/map/pair 是否声明 length。
2. 协议里的长度单位究竟是 byte、bit 还是 item。
3. 长度是否包含头部、长度字段自身或尾部校验。DSL 不会自动替你减掉这些字节。
4. 字符串是否按指定字符集的字节数计算；`"中"` 在 UTF-8 下为 3 字节，HEX `"aabb"` 为 2 字节。
5. prefixBits 是否重复声明了一份长度头。它会自己读写前缀，长度值不包含前缀自身。

例如数组中有两项 u16，字段片段应明确选择两项：

```json
{
  "id": "items", "type": "array",
  "length": { "fixed": 2, "unit": "item" },
  "item": { "id": "item", "type": "u16" }
}
```

或者使用 fixed=4、unit=byte 表示总共四字节；fixed=2、unit=byte 只允许两字节。对应完整例子见[重复记录](./examples.md#嵌套对象和重复记录)。

若解码后 remainingBytes 不为零，表示当前定义没有消费全部输入，不一定是执行失败。检查是否多传帧标识/校验，或定义遗漏了协议字段；不要直接把尾字段改成 remaining 来掩盖布局问题。

## remaining 把后面的字段吃掉了

**原因：** remaining 表示当前容器的剩余空间，不会自动为后面的校验码留一个字节。它必须处于所属容器末尾；无 length 的 struct 会继承外层边界。

假设 raw 只占两字节，其后有 tail，修正后的字段列表片段为：

```json
[
  {
    "id": "body", "type": "struct",
    "length": { "fixed": 2, "unit": "byte" },
    "fields": [
      { "id": "raw", "type": "hex", "length": { "remaining": true, "unit": "byte" } }
    ]
  },
  { "id": "tail", "type": "u8" }
]
```

变长 body 则用前面的长度引用或协议已有的长度前缀来建立独立边界。choice 分支也遵守同样规则。重复记录中的 remaining 必须受逐项长度约束，不能让每项都共享整个数组的剩余空间。

## 引用不存在、前向引用或循环依赖

**原因：** 普通引用的字段尚不可用、id 拼错、作用域不对，或选定版本中没有这个字段。

将正常被引用的长度、判别或条件字段放在使用它之前。用 id 引用而不是 name；外层嵌套字段可写 `header.count`。数组元素不能引用上一项数据。

`computedLengthOf: "body"`、`generated.countOf: "items"` 允许引用同级后方目标，它们有专门的编码生成机制。不能因为这种配置合法，就把普通表达式也改成读取后方字段；尚未回填的值也不能用于前面的 when 或表达式。

length 只能选择 fixed、ref、prefixBits、expression、remaining 中的一种。不要同时配置 ref 和 fixed 来表达“兜底长度”，应根据协议选定实际来源。

## 缺少必需字段值，或字段值类型不正确

**原因：** 把值填成显示名称、把结构层次多包或少包了一层，或误以为普通字段会自动生成。

| 字段定义                 | 正确值片段                                | 常见误写                            |
|--------------------------|-------------------------------------------|-------------------------------------|
| id=sequence，name=流水号 | `{"sequence":1}`                          | `{"流水号":1}`                      |
| id=items 的 u16 数组     | `{"items":[1,2]}`                         | `{"items":[{"item":1},{"item":2}]}` |
| choice 选中字符串分支    | `{"body":"OK"}`                           | `{"body":{"text":"OK"}}`            |
| TLV 已知键               | `{"extras":[{"key":1,"value":12}]}`       | `{"extras":{"1":12}}`               |
| TLV 未知键               | `{"extras":[{"key":99,"rawHex":"aabb"}]}` | 把 key/length 头也放进 rawHex       |

普通 size 字段仍需输入 `{"size":2}`。想省略它，必须按协议配置 computedLengthOf、generated.countOf 等规则。无 generated 的 checksum 也只是普通整数，不会自动计算。

如果字段被 when 跳过，结果中不会出现它。确认条件源字段存在，简写条件的 value 为字符串，bitSet/bitClear 使用掩码。直接依赖缺失值的表达式会报错；简写 when.ref 依赖被跳过字段时则跟随跳过。

## 固定长度不够或没有补齐

**现象：** 值超过边界，或内容较短却提示未声明 padByte。

例如一个 string 占四字节但输入只有 `"AB"`：若协议规定右补空格，字段片段可写为：

```json
{
  "id": "text", "type": "string",
  "length": { "fixed": 4, "unit": "byte" },
  "options": { "padByte": 32, "padSide": "right", "stripPadding": true }
}
```

预期字节为 41422020，解码为 `"AB"`。长度超过四字节仍拒绝编码，不会截断。stripPadding 会移除同侧相同字节，可能影响业务值，需要协议允许才能开启。

## 位宽溢出、未对齐或填充错误

u3 只能保存 0–7，不能填 8；有符号数也不能超出声明范围。JSON 大整数使用十进制字符串，避免浏览器精度损失。

位字段不足整字节时，在后面的字节字段之前增加 align 节点，例如 u3 后补到 8 bit：

```json
{ "id": "align", "type": "align", "alignTo": 8, "fillBit": 0 }
```

共享同一字节的字段保持相同 bitOrder，little 只用于整字节位宽。align 和 padding 的解码都要验证填充值；实际协议补 1、定义补 0 就会失败，不能把它们当作“任意跳过”。

## choice 找不到分支或版本选错

variants 的键写十进制字符串，例如 `"512"`，不是 `"0x0200"`。检查 ref 指向的实际 kind 值，再确认分支是否适用于当前目标版本。需要处理未知 kind 时增加 defaultVariant，并按协议配置它的内容边界。

schemaVersion=1 不代表协议版本是 1。目标协议版本由 Studio 顶部或 Java 编译入口提供，报文内的 version 只是普通值；它不会自动切换已编译布局。留空仅使用默认定义，见[多版本示例](./examples.md#多版本布局)。

## 校验不匹配，解码为什么仍然成功

generated.checksum 的默认行为是返回完整字段及核对结果，方便分析损坏报文。检查 `result.checksums()` 中 actual、expected 和 matched，不能仅看是否抛异常。

严格拒收时用 `plan.decode(input, DynamicDecodeOptions.STRICT)`；错误校验会抛异常且不提交 readerIndex。默认和严格模式的差异见[完整校验示例](./examples.md#自动生成长度、数量和校验和)。

如果期望值不对，核对 range 是否包括协议规定的头部、长度和填充，是否排除了校验字段自身，以及转义是否已在正确阶段处理。generated.constant 也不代表解码时必须等于常量，标志位等业务约束由调用方检查。

## 表达式引擎或扩展未注册

结构化 AST 可直接使用。SpEL/Aviator/MVEL 字符串表达式和自定义 extension 需要宿主显式注册，修改 DSL 或 HTTP 请求不会注册实现。

默认 Studio 未启用字符串引擎时，可改用等价 AST，例如把“count 加 2”写成表达式片段：

```json
{ "op": "add", "args": [{ "ref": "count" }, { "value": 2 }] }
```

AST 的 when.expression 必须返回 Boolean，length.expression 必须返回非负精确整数。宿主配置见[表达式接入](./README.md#复用宿主表达式引擎)和[扩展接入](./README.md#宿主注册扩展)。
