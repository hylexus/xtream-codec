---
title: 常用报文配置示例
---

# 常用报文配置示例

先按协议需要选择例子，再调整字段。每份完整 DSL 都与文档示例测试共用 JSON 文件，可以独立导入 Studio；下方分别列出编码值、预期 HEX 和解码值。标为片段的配置需放入相应字段位置。

如果还不熟悉定义与值的关系，先读[入门教程](./quick-start.md)；属性的取值限制见[DSL 配置参考](./dsl-reference.md)。

## 文本、BCD 和填充

协议要求：流水号用小端 u16，名称为 6 字节 GBK 字符串、不足补零，终端号为 3 字节 BCD，最后携带带 8 bit 字节长度前缀的原始数据。

完整 DSL：

@[code json](@project/xtream-codec-core/src/test/resources/dynamic/docs/text.json)

编码值：

```json
{ "sequence": 4660, "name": "中", "terminal": "001234", "raw": "aabb" }
```

预期 HEX：

```text
3412 d6d000000000 001234 02aabb
```

解码值与编码值相同：

```json
{ "sequence": 4660, "name": "中", "terminal": "001234", "raw": "aabb" }
```

上面的空格只用于拆段展示，连续 HEX 为 `3412d6d00000000000123402aabb`，共 14 字节。

- `中` 的 GBK 编码为 d6d0，占 2 字节，右补四个 00；UTF-8 下它占 3 字节，修改 charset 会改变报文。
- 显式 padByte 允许短字符串补齐。stripPadding 在解码前去掉右侧补零，因此返回 `"中"`；未开启时，填充会留在字符串中。如果业务内容本身可能以相同字节结束，应按协议决定是否去填充。
- `"001234"` 是六位十进制数字，BCD 占 3 字节；使用 JSON 数字会丢失前导零。
- raw 前的 02 是长度头，表示两字节 aabb，不含长度头自身。无需再声明一个 rawLength 字段。

## 嵌套对象和重复记录

协议要求：u16 表示整个 payload 的字节数；payload 内有记录数量，每条记录又以 u16 表示本条内容长度。记录由一个 code 和剩余 UTF-8 文本组成。payload 之后还有一个 7e 尾标记。

完整 DSL：

@[code json](@project/xtream-codec-core/src/test/resources/dynamic/docs/records.json)

编码值只提供业务数据：

```json
{
  "payload": {
    "records": [
      { "code": 1, "text": "A" },
      { "code": 2, "text": "BC" }
    ]
  }
}
```

预期 HEX 为 `000a020002014100030242437e`，共 12 字节：

| 字节            | 含义                              |
|-----------------|-----------------------------------|
| `000a`          | payload 总长 10 字节              |
| `02`            | 两条记录，自动从 records 列表生成 |
| `0002 01 41`    | 第一条：内容长 2，code=1，文本 A  |
| `0003 02 42 43` | 第二条：内容长 3，code=2，文本 BC |
| `7e`            | payload 外的尾标记                |

解码值：

```json
{
  "payloadLength": 10,
  "payload": {
    "count": 2,
    "records": [
      { "code": 1, "text": "A" },
      { "code": 2, "text": "BC" }
    ]
  },
  "tail": 126
}
```

这里有三种不同长度：payloadLength 是 payload 的总字节数，count 是数组项数，item.length.prefixBits 是**每条记录**的长度头。每条记录的长度不含自己的 u16 前缀，但这些前缀计入 payload 总长。

text 的 remaining 只消费当前记录的剩余字节，因为 record 有独立的字节边界。去掉 record.length，第一条就没有结束依据；按 count 计数的数组不会自动为它划出边界。payload 自己也有独立边界，因此其后可以安全放置 tail。

## 条件与消息类型分支

协议要求：flags 的掩码 4 置位时，存在 extra 字段；kind=1 的 body 是温度对象，kind=2 是带长度前缀的字符串，其他 kind 的 body 固定为两字节原始内容。

完整 DSL：

@[code json](@project/xtream-codec-core/src/test/resources/dynamic/docs/choice.json)

温度报文的编码值：

```json
{ "flags": 4, "kind": 1, "extra": 4660, "body": { "temperature": 235 } }
```

预期 HEX 为 `0401123400eb`，解码值：

```json
{ "flags": 4, "kind": 1, "extra": 4660, "body": { "temperature": 235 } }
```

协议若以 0.1°C 为单位，235 对应 23.5°C。DSL 的 i16 直接读写整数，不自动应用显示倍率。

同一份定义还可处理：

| 编码值                                | 预期 HEX     | 解码值                                |
|---------------------------------------|--------------|---------------------------------------|
| `{"flags":0,"kind":2,"body":"OK"}`    | `0002024f4b` | `{"flags":0,"kind":2,"body":"OK"}`    |
| `{"flags":0,"kind":99,"body":"aabb"}` | `0063aabb`   | `{"flags":0,"kind":99,"body":"aabb"}` |

when 判断字段是否出现，choice 判断同一位置使用哪种布局。flags=0 时 extra 不读写，也不出现在解码对象中。bitSet 的 value `"4"` 是掩码，对应最低位起算的第 2 位，不能写成下标 `"2"`。

body 的值直接使用选中的结构：温度分支是 `{"temperature":235}`，字符串分支是 `"OK"`，不是再套一层分支 id。defaultVariant 也是完整字段定义；这里协议明确未知消息体为两字节，所以设置 fixed=2。若未知消息体长度不同，应按协议给它提供边界，不能照搬这个默认分支。

## 有序 TLV 与未知标签

协议要求：先用 u8 表示条目数量，再逐条写入 `标签(u8) + 内容长度(u8) + 内容`。标签 1 为 u16 数值，标签 2 为文本对象，未知标签保留原始字节。

完整 DSL：

@[code json](@project/xtream-codec-core/src/test/resources/dynamic/docs/tlv.json)

编码值：

```json
{
  "extras": [
    { "key": 1, "value": 4660 },
    { "key": 99, "rawHex": "aabb" },
    { "key": 1, "value": 7 },
    { "key": 2, "value": { "note": "A" } }
  ]
}
```

预期 HEX 为 `04010212346302aabb01020007020141`，共 15 字节：

```text
04  01 02 1234  63 02 aabb  01 02 0007  02 01 41
```

第一个 04 是**外层数量前缀**。每个条目自己的第二个字节则是**value 字节长度**。两者分别由 length.prefixBits 和 options.lengthBits 配置，单位与职责不同。

解码值保留原有条目顺序及两次标签 1：

```json
{
  "extras": [
    { "key": 1, "value": 4660 },
    { "key": 99, "rawHex": "aabb" },
    { "key": 1, "value": 7 },
    { "key": 2, "value": { "note": "A" } }
  ]
}
```

未知标签的 rawHex 只含内容 aabb，不包含 63 和长度 02。标签 2 的 note 使用 remaining 时，TLV 的逐条目长度已提供边界，因此只读取这个条目的文本。

### Map 和 Pair 如何选择

| 类型 | 线上条目布局         | 值形状       | 未知键                 |
|------|----------------------|--------------|------------------------|
| tlv  | Tag + Length + Value | 有序条目数组 | rawHex 保留            |
| map  | Key + Length + Value | 与 tlv 一样  | rawHex 保留            |
| pair | Key + Value          | 与 tlv 一样  | 无法确定内容边界，拒绝 |

本例把 type 改为 map 后，值和 HEX 保持一致；Map 也保留重复键，不变成 JSON 对象。键和值长度头可分别设为 8、16、32 bit，默认均为 8。

Pair 省略每条的长度头，options 里也必须去掉 lengthBits。下面是两个已知 u16 值的**完整字段片段**：

```json
{
  "id": "items", "type": "pair",
  "length": { "fixed": 2, "unit": "item" },
  "options": { "keyBits": 8 },
  "variants": { "1": { "id": "number", "type": "u16" } }
}
```

值片段 `{"items":[{"key":1,"value":4660},{"key":1,"value":7}]}` 对应 `011234010007`。不要为未知 Pair 键填写 rawHex，缺少逐条目长度时无法安全解码它的结束位置。

## 位字段、派生视图和对齐

协议的第一个字节已经是一个整体状态值：高 3 位表示 high，低 5 位表示 low。再跟随 3 bit 原始序列，最后以零补齐一个字节。

完整 DSL：

@[code json](@project/xtream-codec-core/src/test/resources/dynamic/docs/bits.json)

编码值可以省略 source，提供同级位段来反向合成它：

```json
{ "low": 17, "high": 5, "sequence": "101" }
```

预期 HEX 为 `b1a0`，共 2 字节：

```text
10110001 10100000
  source sequence + 补零
```

解码值：

```json
{ "source": 177, "low": 17, "high": 5, "sequence": "101", "align": null }
```

source = (5 << 5) + 17 = 177（0xb1）。derived 不占用新的报文空间，所以 low、high 没有另外各占一份字节。align 没有业务值，解码返回 null；它消费的五个补零位仍属于报文，解码会检查这些位。

### 顺序位字段与整数位段的区别

也可把第一个字节拆成两个线上字段，字段列表片段如下：

```json
[
  { "id": "high", "type": "u3" },
  { "id": "low", "type": "u5" }
]
```

默认 msb0 下，前者先写 3 bit，后者再写 5 bit，同样得到 b1，但解码没有 source 字段。选择依据是是否要保留原始整数：需要原值及其视图时用 derived；协议直接按独立位字段工作时用 uN。

顺序 uN 从线上高位开始排列；derived.bitOffset 从**整数最低有效位**开始算，所以 low 的 offset=0，high 的 offset=5。不要把协议表中从左到右的位置直接当作 bitOffset。

## 自动生成长度、数量和校验和

协议要求：7e 开始，u8 表示条目数量，u16 表示 items 的字节数，随后是 u8 数组、一个 XOR8 校验字节和 7e 结束。校验覆盖 count 到 items 的连续字节。

完整 DSL：

@[code json](@project/xtream-codec-core/src/test/resources/dynamic/docs/generated.json)

编码值：

```json
{ "items": [16, 32] }
```

预期 HEX 为 `7e0200021020307e`：

| 字节    | 来源                                            |
|---------|-------------------------------------------------|
| `7e`    | generated.constant                              |
| `02`    | generated.countOf，items 有两项                 |
| `0002`  | computedLengthOf，items 占两字节                |
| `10 20` | 两项 u8 的值                                    |
| `30`    | generated.checksum，02 ^ 00 ^ 02 ^ 10 ^ 20 = 30 |
| `7e`    | generated.constant                              |

解码值：

```json
{ "start": 126, "count": 2, "length": 2, "items": [16, 32], "checksum": 48, "end": 126 }
```

长度回填完成后才计算校验，实际校验范围包括已回填的 0002。count..items 包含两端和中间的 length 字段，不含 start、checksum 和 end。

本例数组按 count 停止，length 作为独立的线上长度信息被回填；如果协议要求数组解码以总字节数为边界，应改为 `length: {"ref":"length","unit":"byte"}`。同时存在的数量与字节长度是否一致，还需按协议的业务规则检查。

### 固定值与校验的解码行为

generated 控制编码时的值来源。解码仍返回报文中的实际 start/count/length/checksum/end。constant=126 **不会自动断言**读到的标记必须为 7e；需要时由调用方检查实际值。

正常报文的校验实际值与计算值都是 48（0x30）。把校验字节改为 31，得到 `7e0200021020317e`，默认解码仍返回全部字段，其中 checksum=49；核对结果为 actual=49、expected=48、matched=false。Studio 会展示核对结果，启用跟踪时还有警告。

Java 调用方可以显式拒绝错误校验：

```java
// 默认返回完整结果，由调用方检查 result.checksums()。
DynamicDecodeResult result = plan.decode(input);

// 对独立输入使用严格模式；不匹配时抛出异常，不推进 readerIndex。
DynamicDecodeResult strictResult = plan.decode(otherInput, DynamicDecodeOptions.STRICT);
```

数据不足、错误边界和资源超限在两种模式下都会失败。算法还支持 sum8（u8）、crc32 和 crc32c（u32）；完整规则见[校验和说明](./README.md#字节区间校验和)。

## 多版本布局

同一协议升级后，数组数量由 u8 扩展为 u16，数组元素仍为 u16。使用相同 id 声明默认布局和 2019 精确布局：

@[code json](@project/xtream-codec-core/src/test/resources/dynamic/docs/versions.json)

两个版本都使用以下编码值：

```json
{ "items": [2, 3, 4] }
```

| 目标协议版本 | 被选中的 count 类型 | 预期 HEX           | 消费字节 |
|--------------|---------------------|--------------------|----------|
| 2013         | 默认 u8             | `03000200030004`   | 7        |
| 2019         | 精确版本 u16        | `0003000200030004` | 8        |

两种报文使用各自目标版本解码，得到相同的业务值：

```json
{ "count": 3, "items": [2, 3, 4] }
```

在 Studio 顶部填写 2013 或 2019，再用相应 HEX 解码。Java 加载同一份 schema，分别调用 `DynamicCompiler.compile(schema, 2013)` 和 `DynamicCompiler.compile(schema, 2019)`；每个计划固定使用编译时选定的布局。

本例默认定义也会适用于其他没有精确定义的版本；如果只允许特定版本，应显式配置它们的 versions。目标版本留空只选择默认定义。引擎不会通过 HEX 长度或报文内容自动识别版本，schemaVersion 在两种情况下都为 1。

## 进阶：JT/T 808 完整报文

仓库已有完整的消息头、消息体和末尾校验字节样例：

| 消息                    | 可借鉴的配置                                                    | 定义文件                                                                                                                                                                                                   |
|-------------------------|-----------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 0x8003 补传分包请求     | 2013/2019 布局、独立位字段、消息体长度回填、自动数组计数与 XOR8 | [8003-multi-version.json](https://github.com/hylexus/xtream-codec/blob/develop/ext/jt/jt-808-server-spring-boot-starter-reactive/src/test/resources/dynamic/jt808/8003-multi-version.json)                 |
| 0x8003 派生位段方案     | 保留原始 bodyProps，位段反向合成                                | [8003-multi-version-derived.json](https://github.com/hylexus/xtream-codec/blob/develop/ext/jt/jt-808-server-spring-boot-starter-reactive/src/test/resources/dynamic/jt808/8003-multi-version-derived.json) |
| 0x0200 位置汇报         | 固定定位字段、BCD 时间、有序 TLV 附加项                         | [0200-2019.json](https://github.com/hylexus/xtream-codec/blob/develop/ext/jt/jt-808-server-spring-boot-starter-reactive/src/test/resources/dynamic/jt808/0200-2019.json)                                   |
| 0x0704 定位数据批量上传 | 数量引用、逐条 u16 长度前缀和嵌套定位记录                       | [0704-2019.json](https://github.com/hylexus/xtream-codec/blob/develop/ext/jt/jt-808-server-spring-boot-starter-reactive/src/test/resources/dynamic/jt808/0704-2019.json)                                   |

对应源码目录为 `ext/jt/jt-808-server-spring-boot-starter-reactive/src/test/resources/dynamic/jt808/`。这些样例的版本、编码值、HEX 和生成规则见该目录的 [README](https://github.com/hylexus/xtream-codec/blob/develop/ext/jt/jt-808-server-spring-boot-starter-reactive/src/test/resources/dynamic/jt808/README.md)。

以下直接展开多版本 0x8003 定义，便于从本页复制：

<details>
<summary>完整 0x8003 多版本 DSL</summary>

@[code json](@project/ext/jt/jt-808-server-spring-boot-starter-reactive/src/test/resources/dynamic/jt808/8003-multi-version.json)

</details>

选择目标版本 2013 后，编码值如下：

```json
{
  "messageId": 32771,
  "hasSubpackage": 0,
  "encryption": 0,
  "terminalId": "013912344323",
  "serialNo": 0,
  "body": {
    "originalMessageFlowId": 111,
    "packageIdList": [2, 3, 4]
  }
}
```

预期 HEX 为 `800300090139123443230000006f030002000300049d`。消息体长 9 字节，packageCount=3，checksum=157（0x9d）。这些自动生成字段也会出现在解码结果中。

**帧标识和转义要与定义一致：** 上述仓库测试样例去掉了两侧 7e，不执行转义/反转义，编码也不自动增加帧标识。Studio 的“808 · 0200 位置汇报”预设则显式声明了 start/end 字段，因此它使用带两侧 7e 的 HEX。两者都不会自动处理转义、分包重组或加解密；真实链路报文应先按协议完成相应处理。

单版本 0200/0704 样例的普通 checksum 字段需由编码输入提供，不会自动核对；两份多版本 8003 样例才声明了 generated.checksum。判断行为应看具体定义。

## 示例验证

本页和教程的八份 JSON 定义通过 `DynamicDocumentationTest` 导入并编译，核对独立给出的 HEX、完整解码值、消费长度及校验行为：

@[code java](@core-test/io/github/hylexus/xtream/codec/core/dynamic/engine/DynamicDocumentationTest.java)

配置失败时，按[排错指南](./troubleshooting.md)检查发生阶段、字段路径和实际字节边界。
