---
title: 动态二进制编解码
---

# 动态二进制编解码

从 **0.11.0** 开始，`io.github.hylexus.xtream.codec.core.dynamic` 提供与 Java 实体无关的编解码能力。Java API 按职责分布在 `schema`（结构定义和 JSON 导入导出）、`engine`（编译、计划和执行结果）以及 `spi`（自定义扩展）子包。

## 阅读路径

第一次为报文配置 DSL，建议先完成教程，再按协议需要查配置和示例。本页保留能力概览及 Java 集成说明。

| 你要做什么                                                | 文档                                             |
|-----------------------------------------------------------|--------------------------------------------------|
| 启动 Studio，部署 JAR / Docker 并保存报文                 | [Studio 启动与部署](./studio.md)                 |
| 从协议字段表写出可运行定义                                | [从协议表写出第一份 DSL](./quick-start.md)       |
| 查字段属性、长度规则和编码值形状                          | [DSL 配置参考](./dsl-reference.md)               |
| 复制并修改文本、数组、分支、TLV、位段、校验和、多版本报文 | [常用报文配置示例](./examples.md)                |
| 定位导入、编译或运行错误                                  | [DSL 配置错误排查](./troubleshooting.md)         |
| 在宿主应用中加载定义并复用计划                            | [编译与执行](#编译与执行)及[生产调用](#生产调用) |

## 结构和值

`DynamicSchema` 描述线上字段顺序，`DynamicValues` 携带一次调用的值。字段 `id` 在选定版本的同级结构内唯一，`name` 只是可重复的显示名称；嵌套对象按 `id` 组成值树，数组保留元素顺序。

结构的 `schemaVersion` 当前为 `1`。JSON 中的未知属性会被拒绝；未知版本在编译期报错。

```json
{
  "schemaVersion": 1,
  "fields": [
    { "id": "version", "type": "u3" },
    { "id": "flags", "type": "u5" },
    { "id": "sequence", "type": "u16", "endian": "little" }
  ]
}
```

对应值为 `{"version":5,"flags":17,"sequence":4660}`，编码结果为 `b13412`。

## 编译与执行

使用 `DynamicCompiler.compile(schema)` 生成可并发复用的计划。计划不保存请求值、游标或缓冲区；重复使用同一计划可避免重复校验结构。

core 保留 `jackson-annotations` 依赖，但不传递 Jackson 2 databind 或 Spring Boot JSON starter。Java 构造结构并执行计划不需要 Jackson 2 实现；使用下面示例中的 `DynamicSchemaJson.read/write` 时，宿主需提供 Jackson 2 databind。如果宿主已有该依赖，无需重复添加；版本由项目 BOM 或宿主的依赖管理指定。

```kotlin
implementation("com.fasterxml.jackson.core:jackson-databind")
```

`DynamicSchemaJson.jsonSchema()` 仅读取随 core 发布的资源，不初始化 JSON 转换器，因此不需要 Jackson 2 实现。

下面的测试完整展示 Java 构造、JSON 导出和导入、编译以及双向执行：

@[code java](@core-test/io/github/hylexus/xtream/codec/core/dynamic/engine/DynamicQuickStartTest.java)

## 缓冲区与资源上限

- 编码从 `writerIndex` 开始写，解码从 `readerIndex` 开始读；报告的 bit 偏移相对于本次起点。
- 编码返回消费的 bit 数；解码返回字段值、消费的 bit/byte 数以及剩余输入字节数。
- 调用方持有和释放 `ByteBuf`，引擎不改变其引用计数。失败时输出可能已写入部分数据，解码失败不提交读取索引。
- `DynamicLimits` 可设置最大输入字节数、最大输出字节数、集合项数和嵌套深度。不要在并发调用之间共享可变值树或同一个缓冲区。
- 每次调用结束必须处于字节边界；连续 bit 字段末尾不足一个字节时，用显式 `align` 节点声明补位。

## 定义错误

编译错误使用 `DynamicCodecException`，可通过 `fieldPath()` 获取定义路径；编译期 `bitOffset()` 为 `-1`，执行期表示失败位置。重复标识、向前引用、循环依赖、未知类型和缺失变长边界均被拒绝。

普通引用首先查找同级已经可用的字段，再查找外层已经可用的字段。长度回填的目标必须是后续字段，其逆向长度关系不会被误判成解码依赖环。

## 标量与字节字段

| type        | 定义与值                                                                                                              |
|-------------|-----------------------------------------------------------------------------------------------------------------------|
| `uN` / `iN` | N 为 1–64，例如 u16、i32、u3；整数不能再声明 bits。有符号数使用二进制补码，大于 JSON 安全整数范围的值使用十进制字符串 |
| `float`     | `bits` 为 32 或 64；输入输出使用有限数值                                                                              |
| `string`    | 默认 UTF-8，可指定 `charset`（例如 GBK）；非法字符和不可映射字符报错                                                  |
| `bcd`       | 偶数位十进制字符串，每字节承载两位，保留前导零                                                                        |
| `hex`       | 偶数位 HEX 字符串，输出为小写                                                                                         |
| `bytes`     | JSON 使用 0–255 的有序数组，Java 输入也接受 `byte[]`                                                                  |
| `padding`   | 按边界写入 `options.padByte`，默认为零；解码验证每个填充字节                                                          |
| `bits`      | 指定长度的 0/1 字符串，支持超过 64 bit；字符顺序就是线上位的处理顺序                                                  |

固定长度小于值的实际字节数时拒绝编码，不会截断。需要补齐时显式设置 `options.padByte`（0–255）及 `padSide`（`left` 或 `right`，默认右侧）。字符串去填充必须显式设置 `stripPadding: true`；这样可能去掉业务值中相同的尾部或头部字节，应按协议选择。

Java 中的 `options.padSide` 可以传入 `DynamicPaddingSide.LEFT`/`RIGHT`，`options.lengthUnit` 可以传入 `DynamicLengthUnit.BIT`/`BYTE`。构造快照将其规范化为 JSON 字符串；扩展专属的 `options` 仍由扩展自行校验。

## 位序、字节序与对齐

`bitOrder` 默认为 `msb0`：顺序位从字节最高位开始，整数先处理高有效位。`lsb0` 从字节最低位开始，整数先处理低有效位；共享一个字节的字段必须使用同一种位序。

`endian` 默认为 `big`。`little` 只适用于完整字节宽度，表示低有效字节先出现；每个字节内部仍按 `bitOrder` 放置数值位。字符串、浮点数、完整字节整数等字节字段要求起点对齐。

Java 构造入口为这些有限取值提供枚举：`DynamicEndian`（`BIG`/`LITTLE`）、`DynamicBitOrder`（`MSB0`/`LSB0`）、`DynamicLengthUnit`（`BIT`/`BYTE`/`ITEM`）、`DynamicConditionOperator` 和 `DynamicExpressionOperator`。固定名称的字段类型可使用 `DynamicFieldType`；`uN`/`iN` 因位宽可变仍使用字符串。JSON 中继续写 `big`、`lsb0`、`byte`、`bitSet` 等原有字符串，便于交换已有定义。字段类型、长度来源和属性组合在构造或 JSON 导入时检查；字段引用、版本覆盖和剩余长度的位置在编译时检查。

例如字段 `a=5`、`b=17`、`c=2748` 分别占 3、5、12 bit，随后用 `{"id":"align","type":"align","alignTo":8,"fillBit":0}` 对齐：

| 所有位字段及对齐节点的 bitOrder | HEX      | 位范围                                       |
|---------------------------------|----------|----------------------------------------------|
| `msb0`                          | `b1abc0` | a: [0,3)，b: [3,8)，c: [8,20)，填充: [20,24) |
| `lsb0`                          | `8dbc0a` | a: [0,3)，b: [3,8)，c: [8,20)，填充: [20,24) |

`alignTo` 为 1、2、4、8、16、32 或 64，相对于本次调用的起点计算。`fillBit` 为 0 或 1，编码补位，解码验证；它不会无声跳过填充。

## 可选 Trace

向 `plan.encode(values, buffer, tracker)` 或 `plan.decode(buffer, tracker)` 传入 `CodecTracker` 可记录字段树。`bitStart` / `bitEnd` 为左闭右开的精确范围，`byteStart` / `byteEnd` 向外取整覆盖触及的字节。选择一个字节可能关联多个位字段。

失败时已完成的节点仍保留，诊断含 `bitOffset`。未传入 tracker 的调用不创建 Trace 节点。

## 长度与容器

`length` 的来源必须且只能选择一个，单位必须明确：

| 规则     | JSON 示例                                     | 说明                                                     |
|----------|-----------------------------------------------|----------------------------------------------------------|
| 固定     | `{"fixed":3,"unit":"byte"}`                   | 编码校验实际长度，解码严格按指定边界读取                 |
| 先前字段 | `{"ref":"size","unit":"byte"}`                | 引用已解析的整数，可使用 `header.size` 等显式嵌套路径    |
| 前置长度 | `{"prefixBits":8,"unit":"byte"}`              | 字段前写入 8/16/32 bit 的长度                            |
| 表达式   | `{"expression":{"ref":"size"},"unit":"byte"}` | 支持 AST 或宿主启用的字符串表达式；返回非负精确整数      |
| 容器剩余 | `{"remaining":true,"unit":"byte"}`            | 仅允许作为该容器最后一个字段；在顶层表示本次输入剩余部分 |

未声明独立 `length` 的 `struct` 与外层共享边界。如果它的最后一个子字段（包括更深层的无长度 `struct`）使用 `remaining`，这个 `struct` 自身也必须位于外层末尾；需要在它后面放字段时，应先给它声明固定、引用或前置长度。

同样的规则适用于 `choice`：任一分支使用 `remaining` 时，`choice` 必须位于当前容器末尾。有界外层容器结束后仍可继续定义字段。按 `item` 计数的数组没有逐项字节边界；若元素会消费 `remaining`，编译器只允许固定项数为 0 或 1，且非空数组须位于容器末尾。按 `bit` 或 `byte` 长度限定的数组可以让唯一元素消费数组剩余空间。

按 `item` 计数的 `pair` 同样没有逐条目的字节边界，任一分支会消费外层 `remaining` 时也遵守上述限制。需要重复这种分支时，给分支声明独立的字节长度。`tlv` 和 `map` 自带条目长度头，分支中的 `remaining` 只消费当前条目的内容。

单位 `bit` 和 `byte` 用于有明确位/字节边界的字段；`item` 用于数组与有序条目的元素个数。数组用 `item` 时按计数停止，用 `bit` 或 `byte` 时按容器边界停止。元素不推进游标会被拒绝，以防止无穷循环。

`struct` 的 `fields` 组成嵌套对象；`array` 的 `item` 描述每个元素。字段引用必须已可用，层次外的字段用限定路径，例如 `header.count`。

长度字段可声明 `computedLengthOf: "first..last"`，区间包含两端字段，长度根据编码后实际写入位数回填。默认单位为 `byte`，可以用 `options.lengthUnit: "bit"` 指定 bit。被条件跳过的字段不计入长度。解码把长度字段当普通整数读取；后续字段仍需要自己的 `length.ref` 明确使用它。

## 有序键值条目

`tlv`、`map` 和 `pair` 的值都是按顺序排列的条目数组。每个条目包含 `key` 和已匹配的 `value`；未知 TLV/Map 键使用 `rawHex` 保存原始字节。重复键不会合并。`variants` 按十进制键匹配值定义；`options.keyBits` 默认 8，TLV/Map 的 `options.lengthBits` 默认 8，允许 8/16/32。

TLV/Map 的线上布局为 `key + valueLength + value`，长度单位是字节。Pair 的布局为 `key + value`，因此未知键没有可确定的值边界，会被拒绝。整个条目集合再按 `length` 的 item/bit/byte 规则限制。

例如 `variants` 中的键 `"1"` 可映射到 `string` 字段定义，未知键 `99` 的值 `aabb` 使用条目 `{"key":99,"rawHex":"aabb"}`。两条键 `1` 会保留为两条有序结果。

## 条件、版本与分支

协议版本由调用方在编译时传入，与 DSL 格式版本 `schemaVersion` 独立。字段的 `versions` 为整数数组；省略表示默认定义，也可显式填写 `DynamicSchema.ALL_VERSION`（值为 -2147483648，与 `XtreamField.ALL_VERSION` 一致）。匹配目标版本时精确定义优先，没有精确定义则使用默认定义，两者都没有则跳过该字段。无版本参数的 `compile(schema)` 只选择默认定义。

同一 `id` 可以提供多个版本定义，首次出现的位置决定该字段的顺序。重叠版本、多个默认定义以及空 `versions` 会被拒绝。目标版本递归应用于嵌套结构、数组和分支；筛选后仍须满足引用、长度与布局校验。数组元素若不适用目标版本，应对整个数组声明版本；TLV 的不适用标签按未知标签保留原始字节，choice 的不适用分支不参与匹配。

```json
{
  "schemaVersion": 1,
  "fields": [
    { "id": "packageCount", "type": "u8" },
    { "id": "packageCount", "type": "u16", "versions": [2019] }
  ]
}
```

```java
// 配置加载时编译一次，随后反复使用各自的计划。
final DynamicPlan plan2013 = DynamicCompiler.compile(schema, 2013);
final DynamicPlan plan2019 = DynamicCompiler.compile(schema, 2019);
final DynamicDecodeResult result = plan2019.decode(input);
plan2019.encode(values, output);
```

计划的 `version()` 返回编译时选定的协议版本。core 不建立全局缓存，计划由调用方持有；执行器无需逐字段匹配版本。

`when` 使用 `ref`、`op`、`value` 描述是否存在字段。运算符为 `eq`、`ne`、`gt`、`gte`、`lt`、`lte`、`bitSet`、`bitClear`。条件只读取先前字段，不消费输入。条件源字段被跳过时，依赖它的条件字段也跳过，包括 `ne` 条件。数组元素及 TLV、map、pair 条目只读取当前元素和外层已经解析的值，不读取上一元素的值。报文内的版本值仍可作为普通整数参与运行时比较，例如：

```json
{"id":"modern","type":"u16","when":{"ref":"version","op":"gte","value":"2"}}
```

`choice` 使用 `ref` 指向先前的整数判别字段，按 `variants` 的十进制键选择一个值结构。只会编解码被选中的分支；没有匹配项时执行可选的 `defaultVariant`，没有适用默认分支则给出字段路径错误。默认分支是完整字段定义，使用自己的作用域、条件和版本选择，也参与 `remaining` 的容器末尾检查。例如：

```json
{"id":"body","type":"choice","ref":"kind","variants":{"1":{"id":"known","type":"u8"}},
 "defaultVariant":{"id":"raw","type":"hex","length":{"fixed":1,"unit":"byte"}}}
```

默认入口不启用字符串表达式；宿主可以通过下述配置显式提供表达式引擎。

## 自动生成线上字段

`generated` 与 `computedLengthOf` 互斥，四种生成来源也互斥。编码以生成值为准，无需在输入对象中重复提供该字段；解码仍返回报文中的实际值。固定值、计数和表达式不执行业务值断言，调用方可自行检查报文首尾标志或其他业务规则。

| 配置 | 编码行为 | 字段类型 |
| --- | --- | --- |
| `{"constant":126}` | 自动写入固定值，例如 `0x7e` | 线上标量 |
| `{"countOf":"items"}` | 写入输入中同级集合的元素数，可引用后续集合 | 无符号整数 |
| `{"expression":{"op":"*","args":[{"ref":"count"},{"value":2}]}}` | 求值后按字段格式写入报文 | 线上标量 |
| `{"checksum":{"algorithm":"xor8","range":"first..last"}}` | 根据最终线上字节生成校验和 | 算法对应的无符号整数 |

固定值和表达式支持 `uN`、`iN`、`float`、`string`、`bcd`、`hex`、`bytes`、`bits`，结果仍须符合相应字段的格式和位宽。计数目标支持 `array`、`tlv`、`map` 和 `pair`，编码输入必须提供对应列表。表达式只引用先前可用字段，与 `derived.expression` 复用受控 AST 和宿主显式启用的表达式引擎，但会占用报文空间。

下面的定义只需输入 `{"items":[10,11]}`，编码结果为 `7e02040a0b7e`：

```json
{
  "schemaVersion": 1,
  "fields": [
    {"id":"start","type":"u8","generated":{"constant":126}},
    {"id":"count","type":"u8","generated":{"countOf":"items"}},
    {"id":"twice","type":"u8","generated":{"expression":{"op":"*","args":[{"ref":"count"},{"value":2}]}}},
    {"id":"items","type":"array","length":{"ref":"count","unit":"item"},"item":{"id":"item","type":"u8"}},
    {"id":"end","type":"hex","length":{"fixed":1,"unit":"byte"},"generated":{"constant":"7e"}}
  ]
}
```

Java 入口使用 `DynamicField.Builder.generated(DynamicGeneratedValue.constant(...))`，另外三种来源分别使用 `countOf(...)`、`expression(...)` 和 `checksum(new DynamicChecksum(...))`。

### 字节区间校验和

`range` 引用同级单字段或包含两端的连续区间，不能包含校验字段自身。校验字段可位于区间之前或之后；前置字段预留并回填，后置字段直接写入结果。覆盖内容包括区间内长度头、填充及已经完成的回填值。若区间内某个占位字段要等区间结束之后才能回填，或校验和存在相互依赖，编译器拒绝定义。

| algorithm | 字段类型 | 算法             |
|-----------|----------|------------------|
| `xor8`    | `u8`     | 对每个字节异或   |
| `sum8`    | `u8`     | 字节之和模 256   |
| `crc32`   | `u32`    | JDK 标准 CRC-32  |
| `crc32c`  | `u32`    | JDK 标准 CRC-32C |

数值按校验字段声明的 `endian` 和 `bitOrder` 写入；校验算法始终处理实际线上字节。区间起点和终点必须字节对齐。

解码默认返回完整字段值和 `result.checksums()`，校验不匹配不会中断解析，`readerIndex` 正常推进。每个核对结果提供 `fieldPath()`、`algorithm()`、`actual()`、`expected()` 和 `matched()`；结果列表不可修改，重复元素分别记录，条件跳过的校验字段不产生记录。启用跟踪时，不匹配另记为 `WARNING`。调试页面展示实际值和计算值，损坏报文仍可查看完整字段。

严格模式由调用方在每次解码时显式启用，不写入 Schema，也不需要重新编译计划：

```java
// 默认：读取实际值并报告核对结果，由调用方决定如何处理。
DynamicDecodeResult result = plan.decode(input);
for (DynamicChecksumResult check : result.checksums()) {
    if (!check.matched()) {
        // 调用方可记录日志、丢弃报文或继续分析。
    }
}

// 严格模式：校验不匹配抛出 DynamicCodecException，不推进 readerIndex。
DynamicDecodeResult strictResult = plan.decode(input, DynamicDecodeOptions.STRICT);

// 严格模式也可使用自定义资源限制和可选的跟踪器。
DynamicDecodeOptions options = DynamicDecodeOptions.STRICT.withLimits(limits);
DynamicDecodeResult tracedResult = plan.decode(input, options, tracker);
```

以上是独立调用方式；重复解码同一缓冲区前，应由调用方恢复起始 `readerIndex` 或使用独立视图。两种模式下，数据不足、字段边界错误、未对齐区间及资源超限仍会失败，且不提交输入游标。

```json
{"schemaVersion":1,"fields":[
  {"id":"check","type":"u8","generated":{"checksum":{"algorithm":"xor8","range":"length..body"}}},
  {"id":"length","type":"u8","computedLengthOf":"body"},
  {"id":"body","type":"hex","length":{"ref":"length","unit":"byte"}}
]}
```

输入 `{"body":"aabb"}` 编码为 `1302aabb`：先回填长度 `02`，再计算 `02 ^ aa ^ bb = 13`。

### JSON Schema 与编辑补全

核心模块的 `DynamicSchemaJson.jsonSchema()` 返回 Draft 2020-12 JSON Schema。资源路径为 `io/github/hylexus/xtream/codec/core/dynamic/schema/dynamic-codec-schema.json`。它描述属性、互斥来源和字段结构；引用可见性、版本覆盖、回填依赖和实际字节边界由编译器与执行器校验。

调试页面的“定义 JSON”编辑区点击“补全”或按 **Ctrl + Space** 显示候选，方向键选择，Enter 或 Tab 插入，Esc 关闭。系统输入法占用快捷键时可使用按钮。候选包含属性、枚举和当前作用域的字段引用；`countOf` 支持同级后续集合，普通表达式仅补全前序可见字段。页面提供 JSON Schema 下载，地址为 `/dynamic-codec-schema.json`，可关联到外部 JSON 编辑器。补全提示用于辅助编写，应用定义时仍以核心编译检查为准。

## 派生值与反向位段

`derived` 不消费输入。整数位段视图用 `ref` 指向源整数，`options.bitOffset` 从整数最低有效位起算，`options.bitWidth` 表示宽度。位段结果与整数标量使用相同的 JSON 表示规则：超过安全整数范围时返回十进制字符串，64 位无符号值不会转换为负数。源整数编码值缺省时，可由同级位段输入组合，未指定的位为零；源值与位段值同时提供时必须一致。

通用派生字段可用 `expression` 代替 `ref`：

```json
{"id":"total","type":"derived","expression":{"op":"add","args":[{"ref":"count"},{"value":2}]}}
```

默认的受控表达式节点只能为 `ref`、`value` 或 `op + args` 中的一种。支持整数 `add/sub/mul/div/mod`、位运算 `bitAnd/bitOr/bitXor/bitNot/shl/shr`、比较 `eq/ne/gt/gte/lt/lte`、布尔 `and/or/not`。`bitNot/not` 各有一个参数，其他运算有两个参数；树深度最多 32、节点数最多 4096，移位数量最多 64，计算整数最多 4096 bit。`and/or` 短路求值，操作数及结果都要求 Boolean，不执行 Python 风格的真假值转换或返回操作数。条件也可直接写 `when.expression`，长度或集合计数可写 `length.expression`。

`op` 同时接受英文名称和符号别名，Java 的 `DynamicExpression.op(String, ...)` 与 JSON 导入使用同一解析规则：

| 运算   | 英文名称                              | 对应符号                         |
|--------|---------------------------------------|----------------------------------|
| 算术   | `add`、`sub`、`mul`、`div`、`mod`     | `+`、`-`、`*`、`/`、`%`          |
| 位运算 | `bitAnd`、`bitOr`、`bitXor`、`bitNot` | `&`、`\|`、`^`、`~`              |
| 移位   | `shl`、`shr`                          | `<<`、`>>`                       |
| 比较   | `eq`、`ne`、`gt`、`gte`、`lt`、`lte`  | `==`、`!=`、`>`、`>=`、`<`、`<=` |
| 布尔   | `and`、`or`、`not`                    | `&&`、`\|\|`、`!`                |

例如 `{"op":"+","args":[{"ref":"count"},{"value":2}]}` 与上述 `add` 表达式等价，可以在同一棵树中混用英文名称和符号。符号不改变参数数量或求值语义：`-` 是二元减法，`&&/||` 保持短路，`&/|` 是整数位运算。`bitNot`/`~` 对无固定位宽的精确整数逐位取反，等价于 `-x-1`；例如 `~6` 得到 `-7`，需要限定字段位宽时可再使用 `bitAnd` 掩码。导出 JSON 时统一使用英文名称；符号别名用于受控表达式的 `op`，`when` 简写的 `ref/op/value` 继续使用原有条件运算符。

`all/any` 作为 `and/or` 的兼容输入别名，`xor` 作为 `bitXor` 的兼容输入别名；导出使用规范名称。早期定义中使用 `and/or` 进行位运算的地方需改为 `bitAnd/bitOr` 或 `&/|`。Java 位运算枚举使用 `BIT_AND/BIT_OR/BIT_XOR/BIT_NOT`，逻辑运算使用 `AND/OR/NOT`；原有 `ALL/ANY` 枚举常量调用需改为 `AND/OR`。

## 复用宿主表达式引擎

`DynamicCompileOptions` 把协议版本、扩展注册表和可选的 `XtreamExpressionEngineRegistry` 一起传给编译器。旧 `compile(schema)`、版本与扩展重载保持兼容，默认不启用任何字符串表达式引擎；即使 classpath 存在 SpEL，也需要宿主显式注册。

下面是经过测试的完整示例：

@[code java](@core-test/io/github/hylexus/xtream/codec/core/dynamic/engine/DynamicExpressionQuickStartTest.java)

字符串表达式的形状为：

```json
{"engine":"SpEL","source":"#n + 2","bindings":{"n":"header.count"}}
```

`engine/source/bindings` 与 AST 的 `ref/value/op/args` 互斥。`engine` 是已注册引擎的精确标识，`bindings` 显式把局部变量映射到先前可见的字段；不把整份值树或 ByteBuf 暴露为 root。绑定名须为标识符，保留名称 `self` 不可使用。

| 引擎标识  | 条件 source | 长度/计数 source | 派生 source |
|-----------|-------------|------------------|-------------|
| `SpEL`    | `#n > 0`    | `#n`             | `#n + 2`    |
| `Aviator` | `n > 0`     | `n`              | `n + 2`     |
| `MVEL`    | `n > 0`     | `n`              | `n + 2`     |

分别注册 `SpelXtreamExpressionEngine`、`AviatorXtreamExpressionEngine`、`MvelXtreamExpressionEngine`。Aviator/MVEL 的实现依赖需由宿主加入运行时 classpath。各语言保留自身运算与数值语义；core 不改写 source，也不承诺语言间的隐式转换一致。涉及完整 u64 的运算可使用精确整数 AST；字符串引擎需按其语言规则处理十进制字符串及大整数。

`when.expression` 必须返回 Boolean，不能以数字代替；`length.expression` 必须返回非负、无小数且不溢出的精确整数，单位仍由 `unit` 指定。派生结果仅接受 JSON 可表示的标量、列表和字符串键对象，拒绝任意 Java 对象及非有限浮点数。嵌套整数沿用 JSON 安全整数表示规则。

绑定遵守原有版本与作用域规则，不能前向引用或读取相邻集合元素。字段被跳过时，直接使用它的表达式绑定报缺失错误；旧 `when.ref` 缺失时跳过的行为保持不变。尚未完成回填的字段不能用于条件或表达式。长度的既有延迟 `ref` 用法仍可使用。

计划编译时调用 `createExpression` 并绑定具体引擎实例，注册表之后的更新不改变已有计划。每次求值使用独立上下文，列表和对象绑定是只读快照，变量赋值不影响后续求值或调用方数据。自定义引擎与编译表达式仍须支持并发调用。异常保留 cause，外层 `DynamicCodecException` 标出字段路径及 bit 偏移，失败不会变为默认值。

这种接入用于宿主信任的配置，不提供脚本沙箱或超时中断。宿主负责决定允许的引擎、定义来源及执行资源策略；HTTP 调试请求不能注册引擎。

## 宿主注册扩展

实现 `DynamicExtension` 的 `validate`、`encode` 和 `decode`，用 `DynamicExtensionRegistry` 注册一个稳定标识。编译入口为 `DynamicCompiler.compile(schema, registry)`。

结构使用 `type: "extension"`、明确的 `length` 和 `options.extensionId`。例如日期扩展可以接受 ISO 日期字符串，输出 10 字节 ASCII；注册项的其余 `options` 由 `validate` 校验。任何未注册标识（包括 Java 类名）都在编译期拒绝。

扩展得到已经按字段边界截取的字节数组，无权释放调用方缓冲区。扩展必须可并发调用并遵守宿主执行时间和内存约束；同步 Java 处理器的计算时间由宿主控制，core 只限制字段输入、实际输出及集合/深度，不能强制中断任意扩展代码。

## 生产调用

应用应在配置加载时解析 JSON 并编译计划，再按请求调用 `encode` / `decode`。对不可信定义，宿主只注册允许使用的扩展，使用适合协议的 `DynamicLimits`。计划线程安全；tracker、值树和 ByteBuf 按请求隔离。

现有 `EntityCodec` 与 `DataField` 的使用方式无需迁移。它们与动态引擎并列存在。

早期动态样例中的 `type: "uint", bits: 16` 应改为 `type: "u16"`，`int + bits` 应改为 `iN`；整数名称不包含字节序，小端仍通过 `endian: "little"` 声明。`float + bits` 和原始位串 `bits + bits` 保持原有写法。调试页中已保存的旧 JSON 草稿也需要同步修改。
