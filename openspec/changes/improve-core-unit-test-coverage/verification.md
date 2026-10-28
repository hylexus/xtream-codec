# 验证记录

## 变更范围

- 新增 9 个测试类、147 个测试用例（包含参数化展开），验证字节内容、长度、索引、跟踪范围、异常和缓冲区释放。
- 未给 `@Deprecated` 类型、方法或废弃包补测试；旧 MapBeanPropertyMetadata 已被注册器拒绝使用，也未新增测试。
- 按用户确认修复 `XtreamByteReader.readString` 的一行类型转换：使用 `CharSequence.toString()`，兼容 Netty 返回的 `AsciiString`。
- 修复前两个 ASCII 读取用例抛出 ClassCastException；修复后直接读取及链式消费者回调均通过。

## 覆盖率

环境：JDK 21、Gradle 9.8.1、JaCoCo 0.8.13。覆盖率只采集核心模块的 `test` 任务，不合并 debug 或无 Jackson 测试的执行数据。

| 指标 | 补充前 | 补充后 | 提升 |
| --- | --- | --- | --- |
| 行覆盖率 | 6606 / 9518，69.41% | 7185 / 9518，75.49% | 6.08 个百分点 |
| 分支覆盖率 | 2747 / 4142，66.32% | 2990 / 4142，72.19% | 5.87 个百分点 |

上述统计保留模块全部生产代码，包括废弃代码，前后分母一致；未通过排除废弃代码提升覆盖率数字。

| 本次重点类型 | 行覆盖率：补充前 → 补充后 | 分支覆盖率：补充前 → 补充后 |
| --- | --- | --- |
| ByteRingBuffer | 0% → 100% | 0% → 100% |
| XtreamBytes | 29.59% → 96.94% | 27.08% → 97.92% |
| DataFieldEncoder | 21.32% → 99.49% | 24.32% → 100% |
| MetadataUtils | 59.49% → 100% | 50.88% → 100% |
| BitStreamReader | 0% → 100% | 0% → 83.33% |
| DefaultBitStreamReader | 0% → 100% | 0% → 83.33% |
| XtreamByteReader | 0% → 100% | 无分支 |
| BytesFieldCodecs（含嵌套类型） | 27.27% → 93.94% | 12.50% → 100% |
| ByteBufContainer | 0% → 100% | 无分支 |
| XtreamDateTimeFieldCodec | 64.52% → 96.77% | 44.44% → 94.44% |

## 验证结果

- 核心模块 `test`：706 个用例通过，无失败、错误或跳过；原有 559 个用例加上新增 147 个用例。
- 核心模块 `testWithoutJackson`：3 个用例通过。
- debug 模块 `DebugEntity04Test`：2 个用例通过，覆盖直接使用 XtreamByteReader 的示例。
- 核心模块 `checkstyleMain`、`checkstyleTest`：通过，报告中错误数均为 0。
- `openspec validate improve-core-unit-test-coverage --strict`：通过。
- `git diff --check` 及新增文件的空白检查：通过。
- 未修改文档引用源码片段，无需运行 docs 构建。

## 复现命令与报告

正式 Gradle 配置未增加覆盖率插件。临时配置及基线 XML 已保存在本地构建产物中：

- `xtream-codec-core/build/reports/jacoco/test/coverage.init.gradle`
- `xtream-codec-core/build/reports/jacoco/test/baseline.xml`
- `xtream-codec-core/build/reports/jacoco/test/jacocoTestReport.xml`
- `xtream-codec-core/build/reports/jacoco/test/html/index.html`

从仓库根目录执行：

```bash
./gradlew :xtream-codec-core:test \
  :xtream-codec-core:testWithoutJackson \
  :xtream-codec-core:jacocoTestReport \
  :xtream-codec-core:checkstyleMain \
  :xtream-codec-core:checkstyleTest \
  -P xtream.backend.build.checkstyle.enabled=true \
  -I xtream-codec-core/build/reports/jacoco/test/coverage.init.gradle

./gradlew :debug:xtream-codec-core-debug:test \
  --tests 'io.github.hylexus.xtream.debug.codec.core.DebugEntity04Test'

openspec validate improve-core-unit-test-coverage --strict
git diff --check
```

临时配置、覆盖率报告和测试报告位于忽略的 `build/` 目录，执行 clean 后会被删除。

## 第二轮：协议与嵌套路径

新增 3 个测试类、76 个用例（包含参数化展开）：

- `PairAndTlvSequenceCodecTest`：24 个，覆盖 Pair/TLV 的普通及跟踪编解码、已知值与未知值、每项实际长度、空列表与空元素、读取边界、迭代上限、跟踪范围和异常时临时缓冲区释放。
- `RuntimeTypeFieldCodecTest`：30 个，覆盖运行时整数、长整数、无符号小端整数、UTF-8 字符串、嵌套 Record、有界及剩余输入、空值、缺少 RuntimeTypeSupplier 和两个协议版本。
- `NestedAndSequenceMetadataTest`：22 个，覆盖集合中 Record 的专用解码分支、派生字段、条件字段、嵌套 POJO、长度前缀、空集合、空元素、自定义容器、固定及表达式迭代次数、声明长度超出输入和 DataField 元素编码。

第二轮新增测试继续只针对非废弃代码；两轮累计新增 12 个测试类、223 个用例。

### 版本传递回归

新增测试确认：`RuntimeTypeFieldCodec` 的普通嵌套解码、跟踪嵌套解码（有界及剩余输入）和跟踪嵌套编码调用了默认版本重载，丢失外部传入的版本号。在更深一层的版本专属字段上，版本 1 的 `12` 或版本 2 的 `3456` 会被跳过，解码字段变成 null。

用户确认一并修复。四处委托调用改为显式传入 `context.version()`；修复前 10 个版本用例失败，修复后全部通过。普通嵌套编码作为对照也保留测试。

### 第二轮覆盖率

统计口径与第一轮相同：只采集核心模块 `test`，保留废弃代码和生成代码，未修改正式 Gradle 配置。第二轮基线保存在 `xtream-codec-core/build/reports/jacoco/test/round-two-baseline.xml`。

| 指标 | 第一轮完成后 | 第二轮完成后 | 第二轮提升 | 相比原始基线累计提升 |
| --- | --- | --- | --- | --- |
| 行覆盖率 | 7185 / 9518，75.49% | 7385 / 9518，77.59% | 2.10 个百分点 | 8.18 个百分点 |
| 分支覆盖率 | 2990 / 4142，72.19% | 3092 / 4142，74.65% | 2.46 个百分点 | 8.33 个百分点 |

| 第二轮重点类型 | 行覆盖率：第一轮后 → 第二轮后 | 分支覆盖率：第一轮后 → 第二轮后 |
| --- | --- | --- |
| PairCodecs.PairSequenceCodec | 62.79% → 100% | 34.38% → 96.88% |
| TLVCodecs.TLVSequenceCodec | 61.97% → 100% | 34.62% → 100% |
| RuntimeTypeFieldCodec | 62.82% → 98.72% | 36.67% → 96.67% |
| NestedBeanPropertyMetadata | 71.84% → 97.09% | 64.00% → 94.00% |
| SequenceBeanPropertyMetadata | 54.88% → 96.34% | 39.29% → 100% |

### 第二轮验证结果

- 第二轮定向测试：76 个用例通过。
- 核心模块全量 `test`：782 个用例通过，失败、错误、跳过均为 0。
- `testWithoutJackson`：3 个用例通过。
- `checkstyleMain`、`checkstyleTest`：通过，报告中的错误数均为 0。
- OpenSpec 严格验证、已跟踪文件差异检查及新增文件空白检查：通过。
- 未修改文档引用的源码片段，无需运行 docs 构建。
- 完整验证命令与上文相同，最终 JaCoCo XML/HTML 及核心模块测试报告已更新。

### 额外观察

探索字段内部截断的用例时，观察到 `BasicBeanPropertyMetadata` 的普通模式会在可读字节不足时返回 null，而跟踪模式会直接调用字段编解码器并抛出 `IndexOutOfBoundsException`。例如上述 Record 集合夹具输入 `0201127e` 时，两种模式的异常处理不同。用户随后确认统一为必需字段数据不足即失败，已在后续变更 [reject-truncated-core-fields](../reject-truncated-core-fields/verification.md) 中修复，并以 58 个新增参数化用例验证截断输入、合法空内容及条件跳过。

## 第三轮：Map 与自定义 FieldCodec

新增 3 个测试类、52 个用例（包含参数化展开）：

- `MapFieldCodecBoundaryTest`：28 个，覆盖普通及跟踪模式下的精确输出、逐项值长度、未知键回退、字符串键左右填充、空 Map 与 null、键/长度/值截断、有界输入外的尾字节保护，以及拒绝嵌套 Map/Collection 和值编码失败时临时缓冲区释放。
- `SimpleMapMetadataRegistryTest`：14 个，覆盖精确版本匹配优先于默认匹配（两种声明顺序）、按字段及版本缓存、重复键/版本配置、无效值类型、缺少或版本不匹配的回退配置、公共参数与回退的版本选择，以及跳过其他键类型的匹配配置。
- `FieldCodecConstructionTest`：10 个，覆盖单个私有构造器、注册器参数顺序、注解指定构造器、通过注册器创建时的 FieldCodecRegistryAware 回调、显式忽略未知参数、默认拒绝未知参数、歧义或缺少构造器，以及构造异常根因保留。

本轮仅新增测试和变更记录。继续跳过废弃 API，包括 BeanMetadataRegistryAware 回调。三轮覆盖率变更累计新增 15 个测试类、275 个用例；另一个截断字段变更的 58 个用例也包含在下述全量结果中。

### 第三轮覆盖率

基线为前两轮及截断字段修复完成后的报告。只采集核心模块 `test`，保留全部生产代码（包括废弃代码和生成代码），本轮未改变统计分母。

| 指标 | 本轮补充前 | 本轮补充后 | 本轮提升 |
| --- | --- | --- | --- |
| 行覆盖率 | 7394 / 9523，77.64% | 7436 / 9523，78.08% | 0.44 个百分点 |
| 分支覆盖率 | 3098 / 4144，74.76% | 3130 / 4144，75.53% | 0.77 个百分点 |

| 本轮重点类型 | 行覆盖率：补充前 → 补充后 | 分支覆盖率：补充前 → 补充后 |
| --- | --- | --- |
| MapFieldCodec | 92.36% → 96.18% | 67.50% → 90.00% |
| SimpleMapMetadataRegistry | 82.24% → 98.13% | 46.67% → 93.33% |
| BeanUtils | 75.71% → 86.43% | 62.79% → 72.09% |

BeanUtils 的统计仍包含未补测试的废弃方法；本轮仅测试非废弃的构造和注入路径。

### 第三轮验证结果

- 本轮定向测试：52 个用例通过。
- 核心模块全量 `test`：892 个用例通过，失败、错误、跳过均为 0。
- `testWithoutJackson`：3 个用例通过。
- `checkstyleMain`、`checkstyleTest`：通过，报告中的错误数均为 0。
- 两个相关 OpenSpec 变更的严格验证、`git diff --check` 及新增文件空白检查：通过。
- 未修改文档引用源码片段或 debug 示例，无需运行 docs 构建。

本轮使用仍存在于临时目录的 JaCoCo init script，正式 Gradle 配置保持原状：

```bash
./gradlew :xtream-codec-core:test \
  :xtream-codec-core:testWithoutJackson \
  :xtream-codec-core:jacocoTestReport \
  :xtream-codec-core:checkstyleMain \
  :xtream-codec-core:checkstyleTest \
  -P xtream.backend.build.checkstyle.enabled=true \
  -I /private/tmp/xtream-core-coverage.init.gradle --console=plain

openspec validate improve-core-unit-test-coverage --strict
openspec validate reject-truncated-core-fields --strict
git diff --check
```

最新覆盖率报告位于 `xtream-codec-core/build/reports/jacoco/test/`，测试报告位于 `xtream-codec-core/build/reports/tests/test/`。前两轮保存在 `build/` 下的临时脚本和基线 XML 可能已被 clean 清理，历史统计以上文记录为准；当前复现命令使用 `/private/tmp/` 下的脚本。

## 第四轮：DictKey 与条件位操作

新增 2 个测试类、174 个用例（包含参数化展开）：

- `DictKeyTest`：86 个，覆盖七种整数键的文本转换、有符号上下界及无符号范围、非法文本和超出范围、固定大端字节序、通用/GBK/GB2312/UTF-8 字符串键、BCD/HEX 及字符串填充、空字符串、数值输入截断、非零起始索引、尾字节保护及普通/跟踪模式下的范围和状态。
- `XtreamBitOperatorConditionalTest`：88 个，覆盖 set/reset、setRange/resetRange、两种 setWithStatus 的布尔值、Supplier 和 Predicate 条件重载；分别验证执行/跳过、回调仅一次及 Predicate 接收原值、可变对象身份与不可变对象原值、最高位、完整 64 位范围、无符号读取及非法位偏移和范围。

本轮继续跳过废弃 API。四轮覆盖率变更累计新增 17 个测试类、449 个用例；加上截断字段变更的 58 个用例，核心模块较最初 559 个用例累计增加 507 个。

### GB2312 返回类型回归

输入 `d6d0`（GB2312 的“中”），`DictKey.deserialize(str_gb_2312, ...)` 读取的文本正确，但返回 `GbkString`，其名称、类型标识和声明字符集均属于 GBK。普通和跟踪模式各有一个测试失败，说明仅验证文本往返不能发现这个错误。

用户确认一并修复。将该分支构造的类型改为 `Gb2312String`，保留 GB2312 解码字符集和其他逻辑；两个回归用例现均通过，断言具体键类型、字段值、精确字节、读取索引及跟踪范围。

### 第四轮覆盖率

沿用第三轮的临时 JaCoCo 配置，只采集核心模块 `test`，保留全部生产代码（包括废弃代码和生成代码），本轮分母不变。

| 指标 | 本轮补充前 | 本轮补充后 | 本轮提升 |
| --- | --- | --- | --- |
| 行覆盖率 | 7436 / 9523，78.08% | 7507 / 9523，78.83% | 0.75 个百分点 |
| 分支覆盖率 | 3130 / 4144，75.53% | 3197 / 4144，77.15% | 1.62 个百分点 |

| 本轮重点类型 | 行覆盖率：补充前 → 补充后 | 分支覆盖率：补充前 → 补充后 |
| --- | --- | --- |
| DataField.DictKey | 43.48% → 100% | 18.75% → 100% |
| XtreamBitOperator | 68.18% → 97.73% | 52.38% → 94.05% |

### 第四轮验证结果

- 本轮定向测试：174 个用例通过。
- 核心模块全量 `test`：1066 个用例通过，失败、错误、跳过均为 0。
- `testWithoutJackson`：3 个用例通过。
- `debug:xtream-codec-core-debug:test`：23 个用例通过。
- `checkstyleMain`、`checkstyleTest`：通过，报告中的错误数均为 0。
- 两个相关 OpenSpec 变更的严格验证、`git diff --check` 及新增文件空白检查：通过。
- 未修改文档引用源码片段，无需运行 docs 构建。

最终验证命令沿用第三轮命令，并加入 `:debug:xtream-codec-core-debug:test`。最终 JaCoCo XML/HTML 及测试报告均已更新。

## 第五轮：扩展元数据与字段编解码器注册表

新增 35 个用例（包含参数化展开）：

- 补充已有空测试类 `DefaultExtendMetaRegistryTest`：20 个，覆盖 Pair/TLV 按字段和版本缓存、精确版本匹配优先、回退匹配器选择、缺少注解、缺少目标版本的键及长度配置、Pair 未指定或指定零长度、重复匹配器及版本、无效值类型、忽略其他键类型/版本和公共/显式字符集优先级。
- 新增 `FieldCodecRegistryBehaviorTest`：15 个，使用隔离的注册表验证空注册表、数值/字符串/自定义编解码器描述信息、大小端及符号信息、覆盖注册、Aware 注入和复用、类型选择优先级、动态字符串字符集及缺失字符集错误、未注册和已注册原子类型、无配置、瞬态字段与 Record 委托。

本轮仅补充测试及变更记录，未发现新的生产代码问题；继续跳过废弃 API。五轮累计新增 18 个测试类、补充 1 个已有测试类，共增加 484 个用例；另一个截断字段变更增加 58 个用例。

### 第五轮覆盖率

统计口径不变：只采集核心模块 `test`，保留全部生产代码（包括废弃代码和生成代码），本轮分母不变。

| 指标 | 本轮补充前 | 本轮补充后 | 本轮提升 |
| --- | --- | --- | --- |
| 行覆盖率 | 7507 / 9523，78.83% | 7563 / 9523，79.42% | 0.59 个百分点 |
| 分支覆盖率 | 3197 / 4144，77.15% | 3219 / 4144，77.68% | 0.53 个百分点 |

| 本轮重点类型 | 行覆盖率：补充前 → 补充后 | 分支覆盖率：补充前 → 补充后 |
| --- | --- | --- |
| DefaultExtendMetaRegistry | 69.37% → 97.30% | 56.25% → 93.75% |
| DefaultFieldCodecRegistry | 92.52% → 100% | 85.71% → 100% |

### 第五轮验证结果

- 本轮定向测试：35 个用例通过。
- 核心模块全量 `test`：1101 个用例通过，失败、错误、跳过均为 0。
- `testWithoutJackson`：3 个用例通过。
- `checkstyleMain`、`checkstyleTest`：通过，报告中的错误数均为 0。
- 两个相关 OpenSpec 变更的严格验证、`git diff --check` 及新增文件空白检查：通过。
- 本轮未修改生产代码、文档引用片段或 debug 示例，无需重新运行 docs 构建或 debug 测试。

最终验证命令与第三轮相同，仍使用 `/private/tmp/xtream-core-coverage.init.gradle`；最新 JaCoCo XML/HTML 及测试报告均已更新。

## 第六轮：动态表达式与字段定义校验

新增 2 个测试类、109 个用例（包含参数化展开）：

- `DynamicExpressionBoundaryTest`：42 个，覆盖引擎结果中的 NaN、正负无穷、4096 位整数边界、64 层嵌套边界、非字符串对象键、不支持的返回类型、支持的标量类型与 null、递归只读快照和嵌套大整数的精确 JSON 转换。同时验证整数文本长度上限、运算结果超限，以及移位数量的合法端点和非法输入。主要场景均对照普通与跟踪模式，通过真实计划编码和解码验证精确输出、消费位数、错误字段路径、相对位偏移、失败读取索引和跟踪诊断。
- `DynamicFieldValidationBoundaryTest`：67 个，对照 Java 构造与 JSON 导入验证未知及非法字符集、选项值的错误类型和越界数值、派生字段属性缺失或为 null、expression 的非法组合、生成值与长度回填冲突、计数及校验和的目标类型和位宽约束；保留合法选项端点、派生定义和生成值配置的 JSON 往返测试。

本轮未修改生产代码，未为废弃 API 补测试。六轮覆盖率变更累计新增 20 个测试类、补充 1 个已有测试类，共增加 593 个用例；另一个截断字段变更增加 58 个用例。

### 第六轮覆盖率

统计口径不变：只采集核心模块 `test`，保留全部生产代码（包括废弃代码和生成代码），本轮分母不变。

| 指标 | 本轮补充前 | 本轮补充后 | 本轮提升 |
| --- | --- | --- | --- |
| 行覆盖率 | 7563 / 9523，79.42% | 7605 / 9523，79.86% | 0.44 个百分点 |
| 分支覆盖率 | 3219 / 4144，77.68% | 3265 / 4144，78.79% | 1.11 个百分点 |

| 本轮重点类型 | 行覆盖率：补充前 → 补充后 | 分支覆盖率：补充前 → 补充后 |
| --- | --- | --- |
| DynamicExpressionValues | 67.82% → 100% | 69.88% → 93.98% |
| DynamicFieldValidation | 83.94% → 93.43% | 80.87% → 91.74% |

### 第六轮验证结果

- 两组定向测试分别为 42 和 67 个用例，全部通过。
- 核心模块全量 `test`：1210 个用例通过，失败、错误、跳过均为 0。
- `testWithoutJackson`：3 个用例通过。
- `checkstyleMain`、`checkstyleTest`：通过，报告中的错误数均为 0。
- 两个相关 OpenSpec 变更的严格验证、`git diff --check` 及新增文件空白检查：通过。新增文件空白检查同时清理了前几轮 4 个测试文件末尾的多余空行。
- 本轮未修改文档引用片段或 debug 示例，无需重新运行 docs 构建或 debug 测试。

完整验证命令与第三轮相同。定向验证命令：

```bash
./gradlew :xtream-codec-core:test --tests '*DynamicExpressionBoundaryTest' --console=plain
./gradlew :xtream-codec-core:test --tests '*DynamicFieldValidationBoundaryTest' --console=plain
```

## 第七轮：默认值与派生字段回退

新增 2 个测试类、56 个用例（包含参数化展开）：

- `XtreamFieldDefaultsTest`：34 个，覆盖两种 nulls 策略下八种基本类型的零值及包装类型、引用类型的 AS_NULL、字符串和集合的 AS_EMPTY、隐式瞬态 Record 组件、条件跳过的数值组件、版本化显式瞬态组件、两次解码之间的可变集合隔离，以及 POJO 条件跳过时保留字段初始化值。修复前 26 个通过、8 个失败；修复后 34 个全部通过。
- `DerivedFieldFallbackTest`：22 个，全部通过。覆盖两种注解声明顺序下精确版本优先于默认版本、默认版本回退、无匹配版本时 POJO 字段保留初始化值、Record 的版本选择、派生值为 null 或只读 write() 时使用源值且不修改对象、其他 write() 异常继续抛出，以及 read() 异常后继续计算其他派生字段和读取依赖它们的后续字段。普通与跟踪模式均验证精确字节及读取索引。

### 瞬态字段元数据问题

修复前的 8 个失败用例分别为隐式基本类型默认值（2 个模式）、显式瞬态 Map 的版本化空值策略（2 个版本 × 2 个模式）和空容器隔离（2 个模式）。

复现命令：

```bash
./gradlew :xtream-codec-core:test --tests '*XtreamFieldDefaultsTest' --console=plain
```

`SimpleBeanMetadataRegistry.doGetMetadata` 在遇到 `FieldCodec.NullFieldCodec` 时直接按数据类型继续构建嵌套或集合元数据，没有先处理 `codecStrategy == TRANSIENT`。因此隐式瞬态 boolean 组件报 `No constructor found for boolean`；显式瞬态 Map 组件进入旧 Map 元数据构造并报缺少 `@XtreamFieldMapDescriptor`，虽然这些字段不应参与线上编解码。

用户确认一并修复。在该分支优先保留 TRANSIENT 字段的基本元数据，跳过嵌套及集合结构解析。生产代码仅调整该分支条件及对应注释，8 个回归用例修复后全部通过。

### 第七轮覆盖率

统计口径不变：只采集核心模块 `test`，保留全部生产代码（包括废弃代码和生成代码）。本轮生产修复增加 1 行可执行代码及 2 个分支，统计分母相应变化。

| 指标 | 本轮补充前 | 本轮补充后 | 本轮提升 |
| --- | --- | --- | --- |
| 行覆盖率 | 7605 / 9523，79.86% | 7636 / 9524，80.18% | 0.32 个百分点 |
| 分支覆盖率 | 3265 / 4144，78.79% | 3289 / 4146，79.33% | 0.54 个百分点 |

| 本轮重点类型 | 行覆盖率：补充前 → 补充后 | 分支覆盖率：补充前 → 补充后 |
| --- | --- | --- |
| XtreamFieldUtils | 70.00% → 89.29% | 58.95% → 84.21% |

七轮覆盖率变更累计新增 22 个测试类、补充 1 个已有测试类，共增加 649 个用例；另一个截断字段变更增加 58 个用例。

### 第七轮验证结果

- 两组定向测试合计 56 个通过，失败、错误、跳过均为 0。
- 核心模块全量 `test`：1266 个用例通过，失败、错误、跳过均为 0。
- `testWithoutJackson`：3 个用例通过。
- debug 核心模块全量 `test`：23 个用例通过。
- `checkstyleMain`、`checkstyleTest`：通过，报告中的错误数均为 0。
- 两个相关 OpenSpec 变更的严格验证、`git diff --check` 及新增文件空白检查：通过。
- 未新增对废弃 API 的测试；修复后瞬态 Map 不再进入旧 Map 元数据分支。
- 未修改文档引用片段，无需运行 docs 构建。

修复后的验证命令：

```bash
./gradlew :xtream-codec-core:test \
  --tests '*XtreamFieldDefaultsTest' --tests '*DerivedFieldFallbackTest' --console=plain

./gradlew :xtream-codec-core:test \
  :xtream-codec-core:testWithoutJackson \
  :xtream-codec-core:jacocoTestReport \
  :xtream-codec-core:checkstyleMain \
  :xtream-codec-core:checkstyleTest \
  :debug:xtream-codec-core-debug:test \
  -P xtream.backend.build.checkstyle.enabled=true \
  -I /private/tmp/xtream-core-coverage.init.gradle --console=plain
```

## 第八轮：Record 派生组件、动态字节与访问策略组合

新增 2 个测试类，扩展已有 `DerivedFieldFallbackTest`，合计新增 52 个用例（按 JUnit 参数化展开计数，部分用例内部对照普通与跟踪两种模式）：

- `DerivedFieldFallbackTest`：由 22 个增加到 36 个，新增 14 个。覆盖仅有版本 1 注解的 Record 派生组件在版本 1、2、3 和 ALL_VERSION 下的构造参数位置、引用及基本类型默认值；八种基本类型派生转换抛异常或返回 null 后仍能构造 Record；源字段被条件跳过而为空时派生组件保留零值。检查精确报文字节、读取索引及尾部保护字节，普通与跟踪模式均验证。
- `DynamicByteBoundaryTest`：35 个。覆盖 byte[] 的有符号 Java 字节到无符号报文字节、列表中的精确整数表示、负数、超过 255、非整数、错误类型、空元素和超长输入；左右填充及剥离、全填充内容、只剥离指定边缘、保留内部或另一侧的同值字节、不剥离填充和非法 UTF-8 填充字节的先剥离后解码。严格字符集用例覆盖合法代理对、非法 UTF-8 连续字节、截断、过长编码、代理区编码、超出 Unicode 范围及孤立 Java 代理字符；错误用例核对字段路径、位偏移、跟踪诊断和失败时未提交的读取位置。
- `PropertyAccessStrategyIntegrationTest`：3 个，每个均执行普通与跟踪模式。使用真实 `@XtreamEntity` 和字段注解，验证字段的 REFLECTION、METHOD_HANDLE、LAMBDA_META_FACTORY 策略覆盖类级 VAR_HANDLE，字段 AUTO 继承类级策略，字段 VAR_HANDLE 覆盖类级 REFLECTION，以及双方 AUTO 时按是否存在方法选择访问方式。Getter 加 16、Setter 加 32，使精确报文和解码后的原始字段值能够区分方法访问与直接字段访问，并验证编码不修改源对象。

### Record 派生组件缺陷与修复

定向复现命令：

```bash
./gradlew :xtream-codec-core:test --tests '*DerivedFieldFallbackTest' --console=plain
```

修复前 34 个用例中 24 个通过、10 个失败：

- 无默认派生版本的 Record 在版本 2、3 和 ALL_VERSION 下，三个派生组件被移出元数据，仅剩两个普通字段；调用五参数规范构造器时仅传入两个参数，六个普通／跟踪用例抛出 WrongMethodTypeException。
- 八种基本类型派生转换抛异常或返回 null 后，构造参数仍保留 null；四个普通／跟踪用例调用 Record 构造器时拆箱触发 NullPointerException。

用户确认一并修复。`SimpleBeanMetadataRegistry` 为未匹配版本的 Record 派生组件保留瞬态元数据，维持构造参数数量与顺序；`EntityDecoder` 在调用 Record 构造器前，仅为仍为空的基本类型派生参数补入对应零值。引用类型及 POJO 保留原有空值或初始化值行为。修复后上述 10 个回归用例全部通过，额外增加的两个空源值用例也通过。

### 第八轮覆盖率

统计口径不变：只采集核心模块 `test`，保留全部生产代码（包括废弃代码和生成代码）。本轮修复增加 7 行可执行代码和 12 个分支，统计分母相应变化。

| 指标 | 本轮补充前 | 本轮补充后 | 本轮提升 |
| --- | --- | --- | --- |
| 行覆盖率 | 7636 / 9524，80.18% | 7653 / 9531，80.30% | 0.12 个百分点 |
| 分支覆盖率 | 3289 / 4146，79.33% | 3311 / 4158，79.63% | 0.30 个百分点 |

| 本轮重点类型 | 行覆盖率：补充前 → 补充后 | 分支覆盖率：补充前 → 补充后 |
| --- | --- | --- |
| DynamicByteFormat | 86.79% → 90.57% | 80.00% → 88.57% |
| PropertyAccessorFactory | 66.07% → 67.86% | 76.67% → 80.00% |

八轮覆盖率变更累计新增 24 个测试类、补充 1 个原有测试类，共增加 701 个用例；另一个截断字段变更增加 58 个用例。

### 第八轮验证结果

- 三组定向测试合计 74 个通过，其中本轮新增 52 个；失败、错误、跳过均为 0。
- 核心模块全量 `test`：1318 个用例通过，失败、错误、跳过均为 0。
- `testWithoutJackson`：3 个用例通过。
- debug 核心模块全量 `test`：23 个用例通过。
- `checkstyleMain`、`checkstyleTest`：通过，报告中的错误数均为 0。
- 两个相关 OpenSpec 变更的严格验证、`git diff --check` 及 36 个未跟踪文件的空白检查：通过。
- 未新增对废弃 API 的测试，未修改正式构建配置或文档引用片段。

验证命令：

```bash
./gradlew :xtream-codec-core:test \
  --tests '*DerivedFieldFallbackTest' --tests '*DynamicByteBoundaryTest' \
  --tests '*PropertyAccessStrategyIntegrationTest' --console=plain

./gradlew :xtream-codec-core:test \
  :xtream-codec-core:testWithoutJackson \
  :xtream-codec-core:jacocoTestReport \
  :xtream-codec-core:checkstyleMain \
  :xtream-codec-core:checkstyleTest \
  :debug:xtream-codec-core-debug:test \
  -P xtream.backend.build.checkstyle.enabled=true \
  -I /private/tmp/xtream-core-coverage.init.gradle --console=plain
```

## 反射测试夹具的 Error Prone 警告

提交前编译中的 12 条警告来自反射测试输入：`FieldCodecConstructionTest` 的七个构造器触发 UnusedMethod，四个故意保留但不读取的参数触发 UnusedVariable；`PropertyAccessStrategyIntegrationTest` 的 fieldOnly 字段仅由字段访问器和反射断言读写，触发另一条 UnusedVariable。

在七个构造器及一个字段上添加对应的成员级 `@SuppressWarnings`，用中文注释说明反射用途，保留构造器签名、注解配置和测试逻辑；不修改项目级静态检查规则。

清理核心模块产物并禁用构建缓存后，启用 Error Prone、Checkstyle 和许可证检查重新编译与验证：

```bash
./gradlew :xtream-codec-core:clean :xtream-codec-core:test \
  --tests '*FieldCodecConstructionTest' \
  --tests '*PropertyAccessStrategyIntegrationTest' \
  :xtream-codec-core:checkstyleTest :xtream-codec-core:checkLicenseTest \
  -P xtream.backend.build.errorprone.enabled=true \
  -P xtream.backend.build.checkstyle.enabled=true \
  -P xtream.backend.build.license-checker.enabled=true \
  -P xtream.maven.repo.central-portal.enabled=false \
  -P xtream.maven.repo.private.enabled=false \
  -P xtream.maven.repo.github.enabled=false \
  -P xtream.maven.publications.signing=off --no-build-cache --console=plain
```

结果为 BUILD SUCCESSFUL。compileJava 与 compileTestJava 均实际执行，原有 12 条 UnusedMethod／UnusedVariable 警告消失。构造器测试 10 个、属性访问策略测试 3 个用例全部通过，失败、错误和跳过均为 0；Checkstyle 报告错误数为 0，源码许可证检查通过。OpenSpec 严格验证及工作区、暂存区差异空白检查通过。
