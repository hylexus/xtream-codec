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

## 第九轮：长度回填边界与公开批量编码入口

新增 EncodedLengthBoundaryTest 的 22 个用例全部通过，覆盖普通与跟踪模式下 u8 的 0/255/256、u16 的 0/65535/65536，目标缓冲区已有头部数据及非零 readerIndex，超限时不回填截断后的长度且停止在尾字段前，长度字段与统计范围独立条件跳过，以及统计范围内全为 null 时回填零。成功用例同时验证精确字节、源对象不被修改、跟踪节点最终值与范围，并对可解码报文验证解码值和读取位置。

IterableEncodingTest 首轮有效夹具运行 16 个用例，9 个通过、7 个失败。普通模式的实体/DataField 混合顺序、版本传递、空列表及中途失败停止写入均通过；失败均来自跟踪边界：

- 两个实体编码为 12345678，根 Trace.payloadHex 却是最后一个实体的 5678。
- 两种协议版本的批量编码字节正确，根 Trace.payloadHex 仍只包含最后一个实体。
- 纯 DataField 列表的跟踪方向仍为 UNKNOWN。
- DataField 与实体混合时，各实体分别开始或结束跟踪，整批报文与节点坐标不一致。
- 第二个元素抛出异常时，缓冲区正确保留前一个元素的两字节内容，诊断 byteOffset 却是 0，而非相对整批起点的 2。

原因是 EntityEncoder 的 Iterable 跟踪分支只逐元素委托，没有建立整批操作边界；各实体入口分别以自身为根开始和结束跟踪。已提出将开始、完成和失败记录限定在整批调用边界的修复方案，保留上述回归用例。另补充一个外层跟踪已开启时的用例，验证批量委托不会提前关闭外层 scope。

补充外层 scope 用例后再次定向执行，IterableEncodingTest 共 17 个，10 个通过、7 个失败；外层 scope 用例通过，7 个失败仍全部来自上述批量跟踪问题。

用户确认一并修复并保留回归测试后，仅修改 EntityEncoder 的 Iterable 跟踪分支：使用 rootInvocation 判断是否由整批调用开始跟踪，整批成功后记录完整报文，异常时以整批起点记录失败；已有外层跟踪时继续使用外层边界。普通编码分支及写入字节不变。

修复后启用 Error Prone 定向运行 EncodedLengthBoundaryTest 与 IterableEncodingTest，22 + 17 = 39 个用例全部通过，失败、错误和跳过均为 0。修复前失败的 7 个用例全部通过，外层 scope 用例继续通过。

已完成独立验证：EncodedLengthBoundaryTest 的 22 个与既有 EncodedLengthCodecTest 的 17 个用例合计 39 个全部通过；启用 Error Prone 后生产与测试代码编译通过，Checkstyle 和测试源码许可证检查通过。OpenSpec 严格验证、工作区及暂存区差异空白检查、两个新增测试文件的独立空白检查通过。

定向复现命令：

```bash
./gradlew :xtream-codec-core:test \
  --tests '*EncodedLengthBoundaryTest' --tests '*IterableEncodingTest' --console=plain
```

本轮覆盖率基线保存在 `/private/tmp/xtream-core-round9.5g5C5f/before.xml`，保留废弃代码及生成代码的统计口径；正式构建配置未修改。

最终集成验证采用与 before-commit.sh 相同的严格构建参数，并在同一次构建中生成核心模块覆盖率报告：

```bash
./gradlew clean build :xtream-codec-core:jacocoTestReport \
  -I /private/tmp/xtream-core-coverage.init.gradle \
  -P xtream.backend.build.checkstyle.enabled=true \
  -P xtream.backend.build.errorprone.enabled=true \
  -P xtream.backend.build.license-checker.enabled=true \
  -P xtream.maven.repo.central-portal.enabled=false \
  -P xtream.maven.repo.private.enabled=false \
  -P xtream.maven.repo.github.enabled=false \
  -P xtream.maven.publications.signing=off --console=plain
```

结果为 BUILD SUCCESSFUL，用时 50 秒，226 个任务中 221 个实际执行、5 个保持最新。Error Prone/NullAway 编译检查及源码许可证检查通过；32 份 Checkstyle XML 报告的错误数合计为 0。构建仍输出已有的 Gradle 弃用提示、扩展模块过时 API 提示和测试 JVM 类共享提示。

JUnit XML 汇总如下，所有分组的失败、错误和跳过均为 0：

| 测试分组 | 用例数 |
| --- | ---: |
| xtream-codec-core:test | 1357 |
| xtream-codec-core:testWithoutJackson | 3 |
| xtream-codec-base:test | 129 |
| jt-808-server-spring-boot-starter-reactive:test | 355 |
| jt-1078-server-spring-boot-starter-reactive:test | 16 |
| xtream-codec-server-reactive:test | 16 |
| xtream-codec-studio-app:test | 42 |
| xtream-codec-core-debug:test | 23 |
| xtream-codec-server-reactive-debug-tcp:test | 1 |
| 合计 | 1942 |

覆盖率对比使用核心模块完整测试生成的报告，原始统计仍包含废弃代码和生成代码；本轮没有为废弃 API 新增测试。

| 范围 | 指标 | 修改前 | 修改后 |
| --- | --- | ---: | ---: |
| 核心模块 | 行 | 7655/9533（80.30%） | 7683/9545（80.49%） |
| 核心模块 | 分支 | 3307/4154（79.61%） | 3323/4160（79.88%） |
| EntityEncoder | 行 | 95/117（81.20%） | 119/129（92.25%） |
| EntityEncoder | 分支 | 36/50（72.00%） | 49/56（87.50%） |
| EncodedLengthPlan.Writer | 行 | 4/5（80.00%） | 5/5（100.00%） |
| EncodedLengthPlan.Writer | 分支 | 1/2（50.00%） | 2/2（100.00%） |

修改后报告位于 `xtream-codec-core/build/reports/jacoco/test/jacocoTestReport.xml` 和同目录的 `html/index.html`。总行数和分支数的变化来自已授权的 Iterable 跟踪边界修复。

OpenSpec 严格验证、工作区及暂存区差异空白检查、两个新增测试文件的独立空白检查通过。第九轮新增的 39 个用例及已授权修复均完成验证。

## 第十轮：动态容器编码边界与 Trace 恢复

新增 DynamicContainerEncodingBoundaryTest，64 个用例全部通过，覆盖普通与跟踪模式：

- 20 个容器前缀用例：struct/array 的 8 位前缀按 byte 计长时验证 0、255、256 字节，按 bit 计长时验证 31、32 字节。成功时核对回填后的前缀、精确报文与解码结果；失败时确认占位前缀未截断回填，尾字段不写入，Trace 前缀节点为 ERROR 且没有最终值。
- 20 个容器契约用例：固定长度未写满、子字段超出父边界、集合项数过多或过少、非整字节内容、struct/array/条目集合的输入类型错误，以及数组元素未推进游标。
- 24 个非法条目用例：tlv/map/pair 的负键与超出 8 位范围的键、未知条目缺少字符串 rawHex、pair 未知键、非对象条目、条目数量不符和已知值未按完整字节结束。

所有动态用例均从已有 55aa 头部、非零 readerIndex 和 writerIndex 开始；失败时核对完整部分输出、精确字段路径与位偏移，并验证资源仍属于调用方。跟踪模式额外核对根状态、报文、诊断的 bitOffset/byteOffset、尾节点缺失及跟踪已结束。

新增 CodecTrackerBufferScopeTest，7 个用例全部通过：

- 两层临时缓冲区的子树先后迁移，检查每层全部后代的坐标，重复封存 checkpoint 不扩大迁移范围，后续长度节点与尾节点不被误移。
- 直接用记录器无显式范围的叶节点观察两层临时游标关闭后分别恢复到 4 和 2，后续叶节点从恢复位置继续。
- 两层临时缓冲区叠加坐标作用域抛出异常后，后续字段回到外层坐标与父节点；原异常保持身份，失败诊断只记录一次并定位到原失败节点。
- 根起点为 10，叠加两层 slice 坐标后检查子字段、外层兄弟节点与根范围。
- checkpoint 新增 0 或 2 个节点时拒绝 requireSingleNode，且不损坏后续字段记录。
- 未封存 checkpoint 时拒绝迁移；空范围封存后允许迁移且不移动随后新增节点。

定向验证命令：

```bash
./gradlew :xtream-codec-core:test \
  --tests '*DynamicContainerEncodingBoundaryTest' \
  --tests '*CodecTrackerBufferScopeTest' \
  -P xtream.backend.build.errorprone.enabled=true --console=plain
```

结果为 BUILD SUCCESSFUL，64 + 7 = 71 个用例的失败、错误和跳过均为 0。未发现需要修复的新生产缺陷；本轮仅增加测试及变更记录，未修改生产实现，没有为废弃 API 新增测试。

本轮修改前的覆盖率 XML 保存在 `/private/tmp/xtream-core-round10.ZvqR2i/before.xml`，继续使用临时 JaCoCo 配置并保留废弃代码及生成代码的原始统计口径。

集成验证先执行与 before-commit.sh 一致的 clean build 并附加 JaCoCo 报告任务；核心模块完整测试通过，Checkstyle 指出新增 Trace 测试中两处多资源 try 的续行缩进错误。仅修正这两处缩进后，使用相同严格参数继续完整构建：

```bash
./gradlew build :xtream-codec-core:jacocoTestReport \
  -I /private/tmp/xtream-core-coverage.init.gradle \
  -P xtream.backend.build.checkstyle.enabled=true \
  -P xtream.backend.build.errorprone.enabled=true \
  -P xtream.backend.build.license-checker.enabled=true \
  -P xtream.maven.repo.central-portal.enabled=false \
  -P xtream.maven.repo.private.enabled=false \
  -P xtream.maven.repo.github.enabled=false \
  -P xtream.maven.publications.signing=off --console=plain
```

最终结果为 BUILD SUCCESSFUL，用时 13 秒，202 个任务中 70 个实际执行、132 个保持最新。清理后的生产代码和测试代码均经过 Error Prone/NullAway 编译检查，源码许可证检查通过；修正后的 32 份 Checkstyle XML 报告错误数合计为 0。没有修改项目级检查规则或增加警告抑制。

JUnit XML 汇总：核心模块普通测试 1428 个，无 Jackson 环境测试 3 个；base 129 个、JT808 扩展 355 个、JT1078 扩展 16 个、响应式服务 16 个、Studio 42 个、core-debug 23 个、TCP debug 1 个。合计 2013 个，失败、错误和跳过均为 0。

| 范围 | 指标 | 修改前 | 修改后 |
| --- | --- | ---: | ---: |
| 核心模块 | 行 | 7683/9545（80.49%） | 7702/9545（80.69%） |
| 核心模块 | 分支 | 3323/4160（79.88%） | 3345/4160（80.41%） |
| DynamicWriteExecution | 行 | 340/375（90.67%） | 353/375（94.13%） |
| DynamicWriteExecution | 分支 | 292/352（82.95%） | 307/352（87.22%） |
| CodecTraceRecorder | 行 | 123/134（91.79%） | 127/134（94.78%） |
| CodecTraceRecorder | 分支 | 41/64（64.06%） | 45/64（70.31%） |
| CodecTracker.TraceCheckpoint | 行 | 16/18（88.89%） | 18/18（100.00%） |
| CodecTracker.TraceCheckpoint | 分支 | 3/6（50.00%） | 6/6（100.00%） |

修改后 XML 与 HTML 报告位于 `xtream-codec-core/build/reports/jacoco/test/`。生产代码行数与分支总数未变化，本轮没有为废弃代码新增用例，也没有调整正式构建配置。

OpenSpec 严格验证、工作区及暂存区差异空白检查、两个新增测试文件的独立空白检查通过。新增测试没有文档源码引用，无需重建文档站点。

## 第十一轮：Trace 值展示与 JSON 表示

新增 CodecTraceValueRendererTest，57 个用例全部通过：

- 13 个标量用例验证字符串、非 String 字符序列、整数边界、大整数、小数、浮点数、布尔值与枚举保留实际类型，并分别核对摘要和 JSON 表示。枚举使用不同于名称的中文 toString，区分摘要与 JSON 枚举名称。
- 21 个非废弃包装类型用例覆盖 u8/u16/u32 的零值及无符号上界、i8/i16/i32 的上下界，以及 GBK/UTF8/BCD 字符串、空串和缺省空值。包装类型的 JSON 使用实际值而非调试 toString。
- 4 个字节用例对照 byte[] 和 BytesDataWrapper，验证 0、127、128、255 转为数字数组，空输入转为 []；输入不被修改，后续修改源数组也不会改变此前生成的展示值。
- 3 个其他对象用例验证普通对象、List 和 Map 按已有行为回退为文本。
- 8 个摘要用例验证 ASCII/中文的 119、120、121 字符边界，以及无符号字节列表格式化后的 120 字符边界。截断仅影响摘要，视图及序列化结果仍保留完整值。
- 6 个实际视图用例检查字节数组、包装值、布尔值、枚举与普通对象在树形和扁平 JSON 中的表示。
- 2 个节点用例验证既有字节视图与源数组隔离，以及节点值从长文本替换为短文本和 null 时同步更新、清空摘要。

StringBuilder 是有意保留的非 String 字符序列夹具，仅在标量数据工厂上局部抑制 UnnecessaryStringBuilder，并用中文说明其测试用途；没有调整项目级检查规则。JSON 字符序列测试使用默认 Jackson 支持的 StringBuilder，不为默认 Jackson 不支持的 CharBuffer 添加自定义序列化规则。

定向运行新增测试、启用 Error Prone 后编译通过，Checkstyle 和测试源码许可证检查通过。随后执行：

```bash
./gradlew :xtream-codec-core:check :xtream-codec-core:jacocoTestReport \
  :debug:xtream-codec-core-debug:test \
  -I /private/tmp/xtream-core-coverage.init.gradle \
  -P xtream.backend.build.errorprone.enabled=true \
  -P xtream.backend.build.checkstyle.enabled=true \
  -P xtream.backend.build.license-checker.enabled=true --console=plain
```

结果为 BUILD SUCCESSFUL，用时 11 秒，28 个任务中 6 个实际执行、22 个保持最新。核心模块完整测试 1485 个，无 Jackson 环境测试 3 个；core-debug 的 23 个用例继续通过，任务保持最新。上述测试失败、错误和跳过均为 0；核心模块 Checkstyle、许可证检查及 Error Prone/NullAway 检查通过。

| 范围 | 指标 | 修改前 | 修改后 |
| --- | --- | ---: | ---: |
| 核心模块 | 行 | 7702/9545（80.69%） | 7787/9545（81.58%） |
| 核心模块 | 分支 | 3345/4160（80.41%） | 3355/4160（80.65%） |
| CodecTraceValueRenderer | 行 | 25/45（55.56%） | 43/45（95.56%） |
| CodecTraceValueRenderer | 分支 | 16/29（55.17%） | 26/29（89.66%） |

修改前报告保存在 `/private/tmp/xtream-core-round11.4tNteC/before.xml`，修改后报告位于 `xtream-codec-core/build/reports/jacoco/test/`；继续包含废弃代码及生成代码的原始统计口径。没有为废弃 API 新增测试，本轮没有修改生产实现或正式构建配置。

OpenSpec 严格验证、工作区及暂存区差异空白检查、新增测试文件的独立空白检查通过。新增文件没有文档源码引用，无需重建文档站点。

## 第十二轮：调试 JSON 与类扫描

新增 XtreamCodecDebugJsonSerializerTest，42 个用例全部通过：

- 31 个包装值用例覆盖 u8/u16/u32 的零值、符号边界、无符号上界及负值掩码，i8/i16/i32 的零值和上下界，以及 GBK/UTF8/BCD 字符串的中文、表情、前导零、空串、缺省空值和 JSON 转义。每个用例通过真实 ObjectMapper 验证根值、列表元素及对象字段，不经过 Trace 值转换器。
- 2 个字节用例对照原始 byte[] 字段和 BytesDataWrapper，验证空数组、高位字节、对象及列表输出、后续字段完整性和源数组不变。保留该序列化器已有的有符号字节表示：0、127、128、255 对应 [0,127,-128,-1]，与 Trace 的无符号展示分别验证。
- 1 个空字节字段用例验证 Jackson 默认空值处理输出 null，后续字段保持完整。
- 8 个普通值用例验证注解字段中的 null、整数、布尔、字符串、List、Map 和 Record 通过 Jackson 的默认序列化生成正确 JSON 类型及内容。

新增 DefaultXtreamClassScannerTest 和三个独立包内的公开扫描夹具文件，16 个用例全部通过：

- 7 个模式组合分别验证类、字段、方法注解的位置选择及组合去重，精确断言结果集合，不依赖扫描顺序，并排除未注解类型。
- 2 个过滤用例验证每种注解位置都执行 predicate，以及全部候选被拒绝时返回空集合。
- 4 个包与重载用例验证仅扫描子包、多包及重叠包合并去重、相似包名前缀隔离，以及默认过滤器与显式接受全部候选的一致性。
- 3 个空结果用例验证不存在的包、所选注解位置无匹配、未选择扫描模式。

两个新增测试类分别在启用 Error Prone 时定向运行通过。首次严格检查发现 OutsideFixture 的许可证头缺少标准链接，补齐后执行：

```bash
./gradlew :xtream-codec-core:check :xtream-codec-core:jacocoTestReport \
  :debug:xtream-codec-core-debug:test \
  :debug:xtream-codec-server-reactive-debug-tcp:test \
  --tests io.github.hylexus.xtream.debug.codec.XtreamClassScannerTest \
  -I /private/tmp/xtream-core-coverage.init.gradle \
  -P xtream.backend.build.errorprone.enabled=true \
  -P xtream.backend.build.checkstyle.enabled=true \
  -P xtream.backend.build.license-checker.enabled=true --console=plain
```

结果为 BUILD SUCCESSFUL，用时 13 秒，35 个任务中 9 个实际执行、26 个保持最新。核心模块完整测试 1543 个，无 Jackson 环境测试 3 个；core-debug 的 23 个用例继续通过，任务保持最新；TCP debug 模块既有类扫描用例 1 个实际执行通过。上述测试失败、错误和跳过均为 0；核心模块 Checkstyle 的 main/test/jmh 报告均为 0 个错误，许可证检查及启用 Error Prone/NullAway 的构建通过。

| 范围 | 指标 | 修改前 | 修改后 |
| --- | --- | ---: | ---: |
| 核心模块 | 行 | 7787/9545（81.58%） | 7841/9545（82.15%） |
| 核心模块 | 分支 | 3355/4160（80.65%） | 3389/4160（81.47%） |
| XtreamCodecDebugJsonSerializer | 行 | 0/26（0.00%） | 24/26（92.31%） |
| XtreamCodecDebugJsonSerializer | 分支 | 0/18（0.00%） | 16/18（88.89%） |
| DefaultXtreamClassScanner | 行 | 0/27（0.00%） | 25/27（92.59%） |
| DefaultXtreamClassScanner | 分支 | 0/18（0.00%） | 18/18（100.00%） |

修改前后 XML 分别保存在 `/private/tmp/xtream-core-round12.3z0fVF/before.xml` 和 `after.xml`，最新 XML 与 HTML 报告位于 `xtream-codec-core/build/reports/jacoco/test/`。统计口径仍包含废弃及生成代码，生产代码行数和分支总数未变化。本轮只新增非废弃代码的测试，没有修改生产实现、正式构建配置或既有 debug 示例。

OpenSpec 严格验证、工作区及暂存区差异空白检查、五个新增 Java 文件的独立空白检查通过。新增文件没有文档源码引用，无需重建文档站点。

## 第十三轮：包装类型二进制行为与长度字段

新增三个测试类，共 161 个用例全部通过。修复前有 3 个用例复现同一处 UTF8 注册缺陷，用户确认后更正了单例声明及初始化的一行代码，并保留全部回归用例。没有为废弃实现增加用例。

DataWrapperBinaryTest 的 54 个用例全部通过：

- 30 个字节用例覆盖六种整数包装类型的零值、符号边界及上下界，GBK/UTF8 中文和表情、空串、BCD 前导零及奇数位补零，以及空字节和高位字节数组。分别用固定 HEX 验证 writeTo、asBytes、length，检查非零写入位置、前缀和读取索引。
- 6 个数值转换用例验证有符号窄化、无符号掩码、Byte/Word/Dword 别名和字符串转换；6 个缺省数值用例验证不写入、固定宽度、空字节／字符串／BCD，以及不允许对空值进行数值转换。
- 3 个缺省字符串用例保留现有行为：GBK/BCD 不写入，UTF8 写入抛 NullPointerException；三种包装器均返回零长度及空展示值，且不支持整数转换。UTF8 的异常前没有部分写入。本轮未统一这三种空值写入行为。
- 3 个独立用例验证六种数值 setter 的对象身份及实际输出、字符串 setter 的字节长度和 BCD 前导零，以及字节前缀的大端数值解释、BCD 和 ASCII 文本转换。
- 2 个字符集用例验证原始字节按 GBK/UTF8 转为中文；4 个短数组用例验证整数转换失败且输入不变。

LengthFieldTypeBoundaryTest 的 100 个用例全部通过：

- 56 个用例对照普通和跟踪入口，独立断言七种类型的零值、有符号最小／最大值和 -1、无符号符号边界和上界、实际 Number 类型及精确大端字节。验证非零起点、前后哨兵、读取／写入索引和长度节点的值、范围、HEX、方向及状态。
- 44 个用例遍历七种类型的每个不足字节数，对照两种入口验证 IndexOutOfBoundsException、读取索引和输入不变，且不建立虚假的成功长度节点。跟踪入口的读取异常发生在节点建立前，由测试中的调用方报告失败并关闭根跟踪。

DataWrapperCodecIntegrationTest 的 7 个用例全部通过：

- 六种整数包装类型的边界值，以及 GBK/BCD/字节包装类型在普通和跟踪模式中均通过公开 EntityCodec 编解码；精确字节、返回类型、前后哨兵、索引和根跟踪范围均符合预期。
- 修复前，默认注册器的 UTF8 类型检查和两种模式下的 UTF8 Record 解码失败。输入字段为 StringWrapperUtf8("中文")，编码为 e4b8ade69687，后跟尾字段 7e；解码错误返回 DwordWrapper，在 Record 构造时抛出 ClassCastException。修复后，三个用例均通过，正确恢复 UTF8 包装类型、文本和尾字段。
- 根因是 DataWrapperFieldCodes.INSTANCE_STRING_UTF_8 声明并初始化为 DwordWrapperFieldCodec；DefaultFieldCodecRegistry 将该对象注册给 StringWrapperUtf8。
- 用户明确确认一并修复，将该公开常量的声明类型与初始化对象同时更正为 StringWrapperUtf8FieldCodec。三个回归用例保留预期正确行为，未跳过或改为接受错误结果。

修复前三个测试类分别启用 Error Prone 定向运行；前两个全部通过，集成类稳定复现上述 3 个失败。修复前还执行：

```bash
./gradlew :xtream-codec-core:check :debug:xtream-codec-core-debug:test --continue \
  -P xtream.backend.build.errorprone.enabled=true \
  -P xtream.backend.build.checkstyle.enabled=true \
  -P xtream.backend.build.license-checker.enabled=true --console=plain
```

修复前核心模块完整执行 1704 个用例，1701 个通过，3 个失败均为上述 UTF8 回归用例；错误和跳过为 0。无 Jackson 环境的 3 个用例全部通过，core-debug 的 23 个用例继续通过、任务保持最新。启用 Error Prone/NullAway 的编译、核心模块 Checkstyle 和许可证检查通过。该次构建因上述 3 个失败返回 BUILD FAILED，用时 14 秒，27 个任务中 6 个实际执行、21 个保持最新。

用户确认的一行修复落地后，54 个包装类型直接测试与 7 个集成测试定向运行全部通过。随后执行最终验证：

```bash
./gradlew :xtream-codec-core:check :xtream-codec-core:jacocoTestReport \
  :debug:xtream-codec-core-debug:test \
  -I /private/tmp/xtream-core-coverage.init.gradle \
  -P xtream.backend.build.errorprone.enabled=true \
  -P xtream.backend.build.checkstyle.enabled=true \
  -P xtream.backend.build.license-checker.enabled=true --console=plain
```

结果为 BUILD SUCCESSFUL，用时 13 秒，28 个任务中 11 个实际执行、17 个保持最新。核心模块完整测试 1704 个、无 Jackson 环境测试 3 个、core-debug 测试 23 个均通过，失败、错误和跳过均为 0；core-debug 本次实际重新执行。启用 Error Prone/NullAway 的构建、核心模块 Checkstyle 和许可证检查通过，main/test/jmh 三份 Checkstyle 报告均为 0 个错误。

| 范围 | 指标 | 修改前 | 修改后 |
| --- | --- | ---: | ---: |
| 核心模块 | 行 | 7841/9545（82.15%） | 8036/9545（84.19%） |
| 核心模块 | 分支 | 3389/4160（81.47%） | 3447/4160（82.86%） |
| 本轮包装类型 | 行 | 90/257（35.02%） | 255/257（99.22%） |
| 本轮包装类型 | 分支 | 2/52（3.85%） | 50/52（96.15%） |
| LengthFieldType | 行 | 28/37（75.68%） | 36/37（97.30%） |
| LengthFieldType | 分支 | 3/12（25.00%） | 11/12（91.67%） |

包装类型统计包括 DataWrapper 接口、六种整数类型、GBK/UTF8/BCD 字符串和 BytesDataWrapper，不包括未作为本轮目标的 WordWrapper、DwordWrapper 别名。模块原始统计仍包含废弃及生成代码，生产代码行数和分支总数未变化。

修改前后覆盖率 XML 分别保存在 `/private/tmp/xtream-core-round13.qJuk5q/before.xml` 和 `after.xml`，复现缺陷的集成测试 XML 保存在同目录的 `utf8-registration-before-fix.xml`。最新 XML 与 HTML 报告位于 `xtream-codec-core/build/reports/jacoco/test/`。生产变更仅为用户确认的 UTF8 单例声明及初始化修复，没有修改正式构建配置或既有 debug 示例。

OpenSpec 严格验证、工作区及暂存区差异空白检查、三个新增 Java 文件的独立空白检查通过。新增文件没有文档源码引用，无需重建文档站点。

## 第十四轮：动态配置快照与表达式定义边界

新增两个测试类，共 64 个用例全部通过。本轮没有发现需要修复的生产代码缺陷，仅新增非废弃 API 的测试并更新变更记录。

DynamicConfigurationSnapshotTest 的 26 个用例全部通过：

- 2 个用例通过真实 extension 字段对照普通和跟踪模式。在字段构建后修改原始嵌套 Map、List 和 extensionId，再编译计划；编译后再次清空原始配置。计划仍使用 offset=1，输入 40 编码为独立预期字节 29，解码恢复 40，非零缓冲区起点及读取位置符合预期。
- 1 个用例验证 Map → List → Map 和 List → List 的逐层复制与只读性，包括 Map 的 entrySet、keySet、values 以及 List 的添加、替换和迭代器删除入口；核对字段顺序、嵌套 null、源集合清空后的快照以及 JSON 往返。
- 11 个标量用例覆盖 null、字符串、布尔值及八种数值类型，在顶层、嵌套对象和列表中核对值及实际类型，包括超出 long 范围的大整数和高精度 BigDecimal。数值保真针对 Java 配置快照，不额外声明 JSON 导入的数值类型或精度契约。
- 12 个非法配置用例将普通对象、byte[]、Object[] 和 Set 分别放在顶层选项、嵌套对象和列表中，验证在字段构建阶段拒绝，并报告实际不受支持的类型。

DynamicExpressionDefinitionTest 的 38 个用例全部通过：

- 22 个用例对照 Java 构造和 JSON 导入，验证来源缺失及互斥、空白引用、非标量常量、无 op 却携带 args、不完整引擎定义以及引擎形式混入其他来源时的明确拒绝原因。
- 7 个非法绑定用例验证空名字、数字开头、非法字符、中文、保留名 self 和空白目标；3 个合法名字用例确认下划线及字母数字组合可正常构建和 JSON 往返。
- 3 个运算符用例分别对 NOT、BIT_NOT、ADD 检查缺失 args 以及 0 至 3 个参数的组合，在 Java 和 JSON 入口核对一元／二元参数数量规则，并保留合法数量的往返验证。
- 1 个用例验证 args 和 bindings 都复制调用方集合，源集合修改或清空后定义不变，返回集合不可修改；算术表达式仍得到独立预期结果 3。
- 1 个深度边界用例确认最大深度 32、共 33 个节点的表达式可以 JSON 往返和实际执行，深度 33 则在 Java 与 JSON 入口明确拒绝；节点数远低于上限。深度按根节点为 0 计数。
- 1 个节点数边界用例用深度 11 的平衡树独立计数 4095 个节点，添加一元根节点后恰好 4096 个节点、深度 12，可以往返并执行得到 -2049；将两个合法子树组合为 4097 个节点时，两种入口均因节点数超限而拒绝，未触及深度限制。

两个测试类分别启用 Error Prone 定向运行通过，随后执行：

```bash
./gradlew :xtream-codec-core:check :xtream-codec-core:jacocoTestReport \
  -I /private/tmp/xtream-core-coverage.init.gradle \
  -P xtream.backend.build.errorprone.enabled=true \
  -P xtream.backend.build.checkstyle.enabled=true \
  -P xtream.backend.build.license-checker.enabled=true --console=plain
```

结果为 BUILD SUCCESSFUL，用时 14 秒，22 个任务中 8 个实际执行、14 个保持最新。核心模块完整测试 1768 个、无 Jackson 环境测试 3 个全部通过，失败、错误和跳过均为 0。启用 Error Prone/NullAway 的测试源码编译、核心模块 Checkstyle 和许可证检查通过；main/test/jmh 三份 Checkstyle 报告均为 0 个错误。此次没有改动生产实现、debug 示例或文档引用源码，未重复执行其他模块测试或文档构建。

| 范围 | 指标 | 修改前 | 修改后 |
| --- | --- | ---: | ---: |
| 核心模块 | 行 | 8036/9545（84.19%） | 8059/9545（84.43%） |
| 核心模块 | 分支 | 3447/4160（82.86%） | 3474/4160（83.51%） |
| DynamicData | 行 | 23/39（58.97%） | 39/39（100.00%） |
| DynamicData | 分支 | 12/22（54.55%） | 22/22（100.00%） |
| DynamicExpression | 行 | 37/44（84.09%） | 44/44（100.00%） |
| DynamicExpression | 分支 | 61/78（78.21%） | 78/78（100.00%） |

模块原始统计仍包含废弃及生成代码；本轮没有为废弃代码补测试。修改前后覆盖率 XML 保存在 `/private/tmp/xtream-core-round14.MryZ20/before.xml` 和 `after.xml`，最新 HTML 报告位于 `xtream-codec-core/build/reports/jacoco/test/html/index.html`，未修改正式构建配置。

OpenSpec 严格验证、工作区及暂存区差异空白检查、两个新增 Java 文件的独立空白检查通过。按 openspec-apply-change 工作流更新第十四轮范围、设计与任务状态，保留既有工作区改动。

## 第十五轮：瞬态注解代理与方法参数契约

新增两个测试类，共 31 个用例全部通过。瞬态注解代理的 15 个用例在修复前有 9 个失败，用户确认后进行了最小修复并保留全部回归用例；没有为废弃代码新增测试。

XtreamTransientFieldProxyTest 的 15 个用例全部通过：

- 3 个用例分别以 int、String、List 字段创建代理，逐个属性与真实 `@XtreamField(codecStrategy = TRANSIENT)` 比较，并验证 annotationType、目标类型及基本类型／引用类型的默认值。
- 1 个用例验证同一字段及不同目标类型创建的等价注解满足自反、对称、传递性及哈希一致性；1 个用例与真实瞬态注解双向比较，并确认 version 数组实例不同但内容相同。
- 3 个用例确认 DEFAULT 策略、不同 version 内容、不同 desc 的注解均不相等；1 个用例拒绝 null 和其他类型的对象。
- 1 个用例以 JDK 真实注解的哈希为独立期望，验证包括 version 数组在内的属性哈希一致性。
- 2 个 Map 和 2 个 Set 用例对照真实注解先插入、代理先插入两种顺序，验证等价键的查询和替换、去重以及不同目标类型的代理查询。
- 1 个用例验证字符串展示中的 codecStrategy 与代理生效的 TRANSIENT 属性一致，不依赖属性排列顺序或完整字符串格式。

修复前稳定复现 9 个失败：两个等价代理不相等；代理与真实瞬态注解比较不对称；代理错误接受 DEFAULT 注解；hashCode 与真实注解不一致；两种插入顺序下 HashMap 均无法使用等价键查询、HashSet 均保存了 3 个等价注解；展示值仍为 DEFAULT。根因是 invoke 对 codecStrategy 单独返回 TRANSIENT，而 equals/hashCode/toString 使用的默认属性仍为 DEFAULT；version 数组还按对象身份计算哈希。

用户确认一并修复后，仅调整 XtreamFieldUtils 内的三个位置：构造默认属性时写入有效的 TRANSIENT 策略，移除重复的 codecStrategy 特殊返回分支，哈希计算针对 int[] 使用 Arrays.hashCode。公开 API 及线上编解码行为不变。未通过跳过、禁用或接受错误结果来处理失败用例。修复前的测试 XML 保存在 `/private/tmp/xtream-core-round15.gDdlNF/proxy-before-fix.xml`。

XtreamMethodParameterBoundaryTest 的 16 个用例全部通过：

- 3 个用例验证返回槽 -1 及两个参数槽的 Method、声明类、原始类型和泛型参数；4 个用例拒绝小于 -1、等于参数数量及整数极值等非法索引。
- 1 个用例验证无参 void 方法仅允许返回槽，并区分方法注解、返回类型注解及参数注解读取入口。
- 3 个用例分别以返回槽和两个参数槽验证独立反射构建的等价对象可命中和替换同一 Map 条目、在 Set 中去重；1 个用例验证不同参数槽、重载和不同声明类在缓存中保持 5 个独立条目。
- 1 个用例确认子类获取的继承方法仍采用原声明类并复用等价缓存键；2 个用例在泛型确实存在时检查查询恰好越界、继续越界和极大位置均返回 null，合法泛型类型上的注解仍可读取。
- 1 个用例验证自定义组合请求注解通过 AliasFor 保留 bufferAsSlice=false，并区分方法、参数和返回类型的注解作用域。没有扩展不受支持的复杂泛型语义，也未修改 XtreamMethodParameter 的生产实现。

两组新增测试分别启用 Error Prone 定向运行通过，随后执行最终验证：

```bash
./gradlew :xtream-codec-core:check :xtream-codec-core:jacocoTestReport \
  :debug:xtream-codec-core-debug:test \
  -I /private/tmp/xtream-core-round15.gDdlNF/coverage.init.gradle \
  -P xtream.backend.build.errorprone.enabled=true \
  -P xtream.backend.build.checkstyle.enabled=true \
  -P xtream.backend.build.license-checker.enabled=true --console=plain
```

结果为 BUILD SUCCESSFUL，用时 14 秒，28 个任务中 11 个实际执行、17 个保持最新。核心模块完整测试 1799 个、无 Jackson 环境测试 3 个、core-debug 测试 23 个全部通过，失败、错误和跳过均为 0；core-debug 本次实际重新执行。启用 Error Prone/NullAway 的生产及测试源码编译、核心模块 Checkstyle 和许可证检查通过，main/test/jmh 三份 Checkstyle 报告均为 0 个错误。此前的临时覆盖率脚本已不存在，在本轮临时目录恢复同样的 JaCoCo 0.8.13 配置后完成验证，没有修改正式构建配置。

| 范围 | 指标 | 修改前 | 修改后 |
| --- | --- | ---: | ---: |
| 核心模块 | 行 | 8059/9545（84.43%） | 8108/9546（84.94%） |
| 核心模块 | 分支 | 3474/4160（83.51%） | 3507/4161（84.28%） |
| 瞬态注解代理处理器 | 行 | 17/55（30.91%） | 54/56（96.43%） |
| 瞬态注解代理处理器 | 分支 | 8/28（28.57%） | 28/29（96.55%） |
| XtreamMethodParameter | 行 | 34/48（70.83%） | 46/48（95.83%） |
| XtreamMethodParameter | 分支 | 16/30（53.33%） | 29/30（96.67%） |

模块原始统计仍包含废弃及生成代码；本轮最小修复使生产代码统计增加 1 行和 1 个分支。修改前后覆盖率 XML 保存在 `/private/tmp/xtream-core-round15.gDdlNF/before.xml` 和 `after.xml`，最新 HTML 报告位于 `xtream-codec-core/build/reports/jacoco/test/html/index.html`。没有改动文档引用源码，无需重建文档站点。

按 openspec-apply-change 工作流记录用户确认的修复范围并更新任务。OpenSpec 严格验证、工作区及暂存区差异空白检查、两个新增 Java 文件的独立空白检查通过，保留既有工作区改动。

## 第十六轮：DataField JSON 转换契约

新增两个测试类，共 95 个用例全部通过。首轮有效验证运行 80 个用例，52 个通过、28 个失败；用户确认后，仅修复 DictJsonDeserializer 的元数据保留和 value 结构校验，保留全部 28 个回归用例，并增加 15 个合法边界用例。没有为废弃代码新增测试。

DataFieldJsonRoundTripTest 的 47 个用例全部通过：

- 16 个标量用例遍历 33 个类型别名，并分别通过 JSON 字符串／对象、单字段／列表四种公开入口解析；验证类型、数值、byte[] 内容、名称、展开的扩展属性、长度配置及普通／跟踪编码的独立 HEX，包括有符号最小值、无符号最大值、浮点数、中文、补充字符、BCD 和字节序列。
- 3 个 Sequence 别名和 3 个 Dict 别名用例验证元素与键的顺序、默认前缀及大写值长度配置；2 个嵌套用例对照两种模式，验证 Struct → Sequence → TLV 和 Dict 的组合配置、具体类型及往返结果，独立期望字节为 `0011010702030703e4b8ad020200ff0102d6d0`。
- 2 个字符串用例验证 GBK 字符集、左右填充配置和长度前缀的 JSON 保留；编码结果按现有 DataFieldEncoder 的直接字符串写入行为检查为 `04d6d0cec4`，本轮未扩展字符串填充编码功能。
- 4 个字典元数据回归用例分别通过四种入口确认自定义名称 `business-dictionary`、中文属性及嵌套配置保留，普通与跟踪模式均得到独立期望 `06000203e4b8ad`。
- 7 个数字键用例分别覆盖 i8、u8、i16、u16、i32、u32、i64 的边界值，检查实际键类型、数值、再次往返和独立编码字节；4 个空字典用例检查缺省与显式 null 名称回退为 Dict、无扩展属性、空输出和合法 `{}` 的接受行为。
- 4 个扩展属性用例验证 null、列表、布尔值、嵌套对象以及名为 attributes 的普通扩展属性按既有 JsonAnyGetter 格式保留，不将模型字段混入扩展属性。
- 1 个自定义 DataField 用例通过类全名及四种入口恢复实现，编码为 `02415a`；1 个混合列表用例保留 null 元素及顺序，并检查空列表的序列化与两种解析入口。

DataFieldJsonValidationTest 的 48 个用例全部通过：

- 5 个非法 JSON 用例在单字段和列表字符串入口确认保留 Jackson 原因；15 个非法字段用例通过四种入口验证缺失／未知／非子类型的类型标识、非法数字及超限值、字典必需配置缺失或 null、非法键类型和长度枚举、非数字／超限键及嵌套未知类型。
- 24 个字典结构回归用例覆盖 value 缺失、null、数组、数字、布尔值、字符串与四种入口的组合，均明确报告 `value must be a JSON object`，并保留 JsonProcessingException 原因。
- 4 个非数组输入用例确认列表字符串／对象入口拒绝对象及标量；对象转换失败保留 IllegalArgumentException 和 Jackson 原因。错误断言仅匹配稳定的原因片段，不依赖完整 Jackson 文本。

修复前四种入口均将字典的名称改回 Dict、丢弃扩展属性；value 缺失时报空指针，其他五种非法值则被静默转为空字典。用户确认一并修复后，在 DictJsonDeserializer 构造 Dict 时传入名称和展开的扩展属性，并排除六个模型属性；value 非对象时抛出 JsonParseException。合法空对象、默认名称、类型别名、键顺序和线上编码字节均由回归测试验证。修复前的 XML 保存在 `/private/tmp/xtream-core-round16.d2Hjqs/roundtrip-before-fix.xml` 和 `validation-before-fix.xml`。

新增测试启用 Error Prone 定向运行通过，随后执行最终验证：

```bash
./gradlew :xtream-codec-core:check :xtream-codec-core:jacocoTestReport \
  :debug:xtream-codec-core-debug:test \
  -I /private/tmp/xtream-core-round16.d2Hjqs/coverage.init.gradle \
  -P xtream.backend.build.errorprone.enabled=true \
  -P xtream.backend.build.checkstyle.enabled=true \
  -P xtream.backend.build.license-checker.enabled=true --console=plain
```

结果为 BUILD SUCCESSFUL，用时 13 秒，28 个任务中 8 个实际执行、20 个保持最新。核心模块完整测试 1894 个、无 Jackson 环境测试 3 个、core-debug 测试 23 个全部通过，失败、错误和跳过均为 0；core-debug 在本轮前一次完整检查中已实际执行并通过，最终运行保持最新。启用 Error Prone/NullAway 的生产及测试源码编译、核心模块 Checkstyle 和许可证检查通过，main/test/jmh 三份 Checkstyle 报告均为 0 个错误。首次完整检查只在新测试的枚举 switch 语句上触发 MissingSwitchDefault，改为穷尽的 switch 表达式后重新验证通过，未调整检查配置。

| 范围 | 指标 | 修改前 | 修改后 |
| --- | --- | ---: | ---: |
| 核心模块 | 行 | 8108/9546（84.94%） | 8146/9555（85.25%） |
| 核心模块 | 分支 | 3507/4161（84.28%） | 3521/4169（84.46%） |
| DictJsonDeserializer | 行 | 29/32（90.62%） | 41/41（100.00%） |
| DictJsonDeserializer | 分支 | 8/14（57.14%） | 22/22（100.00%） |
| DataFields | 行 | 45/110（40.91%） | 64/110（58.18%） |
| DataFieldTypeIdResolver | 行 | 54/61（88.52%） | 56/61（91.80%） |

DataFields 的四个转换入口及 mixedListToJsonString 方法各自行覆盖率均为 100%；整类统计还包含本轮未作为目标的工厂方法。模块原始统计仍包含废弃及生成代码；本轮最小修复使生产代码统计增加 9 行、8 个分支。修改前后覆盖率 XML 保存在 `/private/tmp/xtream-core-round16.d2Hjqs/before.xml` 和 `after.xml`，最新 HTML 报告位于 `xtream-codec-core/build/reports/jacoco/test/html/index.html`。继续仅使用临时 JaCoCo 配置，未改动正式构建配置或文档引用源码。

按 openspec-apply-change 工作流记录用户确认的两处修复并更新第十六轮范围、设计与任务。OpenSpec 严格验证、工作区及暂存区差异空白检查、两个新增 Java 文件的独立空白检查通过，保留既有工作区改动。
