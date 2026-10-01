# 重构验收记录

日期：2026-09-29。变更：`redesign-dynamic-codec-execution`。

2026-09-30 模块调整：调试服务类及测试已迁入 `xtream-codec-core` 的 `core.debugger` 包。下方独立服务模块的命令保留为历史验收记录；当前通过 `:xtream-codec-core:test --tests "io.github.hylexus.xtream.codec.core.debugger.CodecDebuggerServiceTest"` 验证同一调试入口。

## 工作区基线

基线 HEAD：`c8ce7c5035a64cfe6dc0487684b7bd61d161acc0`。动态 DSL、调试服务/应用和前序 OpenSpec 已存在于未提交工作区，不能以 HEAD 内容代替重构前实现。保留已有版本、模块、Trace 及其他用户修改；没有创建提交、归档或重置文件。

实际 `gradle.properties` 的 projectVersion 为 0.11.0；本次新增 JavaDoc 使用 @since 0.11.0。Gradle 9.8.0，mise 的 Temurin 21.0.12+101.0.LTS。性能参数及测量样本见 core 的 [REDESIGN-BENCHMARK.md](../../../xtream-codec-core/src/jmh/REDESIGN-BENCHMARK.md)。

基线执行 core、调试服务和独立应用测试：既有 core 测试通过，加入首批回归后 core 共 426 项、3 项预期失败；失败分别是 choice 条件绕过、padding 数组拒绝 null、缺少字符串抛无定位异常。调试服务和应用基线任务通过（UP-TO-DATE）。后续组合回归同时结合旧执行代码检查，不把未运行的旧用例标成实测失败。

## 场景与回归

| Spec 场景 | 测试 | 重构前差异或保留合同 |
| --- | --- | --- |
| 已选 choice 分支条件为假 | DynamicCompositionTest.choiceMustHonorSelectedBranchCondition | 旧实现实测读取分支字节；现在只消费 flag 并省略 choice |
| 必需字符串缺失 | missingStringMustReportFieldLocation | 旧实现实测无定位异常；现在 $.name、bit 0 |
| padding 集合 null | paddingArrayMustPreserveNullValue | 旧实现实测不可变列表拒绝 null；现在 [null]、1 byte |
| 未知 TLV 原始值超限 | unknownTlvMustCheckSizeBeforeHexConversion | 旧代码先转换 HEX 再检查；现在超限无效 HEX 首先报告边界 |
| 数组元素、TLV 分支跳过 | arrayAndTlvMustHonorItemConditions | 旧集合路径直接进入内容；现在检查零进度及非零声明长度 |
| 条件区间长度回填 | repeatedBackfillsMustHandleSkippedEndpoints | 旧代码依赖已执行端点位置；现在布局动作覆盖跳过端点，各元素独立回填 1/3 byte |
| 相邻元素缺失 | DynamicEngineExpressionTest.skippedBindingsMustNotReadPreviousElement | 新 bindings 路径在第二元素报缺失；既有简写 when 隔离测试继续通过 |
| 表达式读取待回填长度 | pendingLengthCannotBeUsedByExpressions | 现在编译期拒绝；既有 DynamicComputedLengthTest 的延迟 length.ref 继续通过 |
| 无用扩展解码及依赖复用 | extensionNormalizationMustBeDemandDriven | 旧普通编码无条件反向 decode；现在无依赖 0 次、有两个依赖和 Trace 共 1 次 |
| 条件跳过不读取输入 | skippedIntegerMustNotSynthesizeReverseViews | 被跳过源整数不再提前校验反向位段输入 |
| 下层输入异常定位 | invalidByteElementMustReportContainingField | bytes 非法元素转换统一定位 $.data，保留 cause |
| 表达式绑定隔离与复用 | engineIsBoundOnceAndContextsAreIsolated | 1 次 createExpression、6 次求值；注销注册项不改变已有计划 |
| 三种引擎双向与并发 | builtinEnginesMustSupportConditionsLengthsAndDerivedValues | SpEL/Aviator/MVEL 各验证条件、长度、计数、派生，32 次并发执行 |
| 严格结果及 cause | resultErrorsRetainLocationAndTrace / engineFailuresAndUnsupportedResultsPreserveCause | 非布尔、负数、小数、null、溢出、任意对象、变量修改及除零均不降级为默认值 |
| u64 表达式精度 | preserveUnsigned64BitExpressionResultsAndReferences | ffffffffffffffff → 18446744073709551615，后续 AST +1 精确 |
| 互斥形式及版本可见性 | rejectMutuallyExclusiveFormsAndVersionInvisibleBindings | 构造/JSON 拒绝混用，2013 不可见字段拒绝，2019 可编译 |
| 分配前计数限制 | expressionCountMustFailBeforeReadingElements | 3 项表达式、2 项上限，在 bit 0 拒绝且 readerIndex 不变 |

既有 DynamicBitCursorTest 遍历 1–64 位、偏移 0/1/3/7、两种位序及合法大小端组合，并用独立参考位算法验证读写。Scalar/Bits/Derived/Length/Containers/Entries/Trace/Version/Limits 测试继续覆盖 u64、有符号范围、重叠位段冲突、嵌套边界、零进度、回填源范围和失败缓冲区所有权。DynamicQuickStartTest 的独立 HEX 为 b10702011234025678。

## 验证命令与结果

```bash
JAVA_HOME="$(mise where java)" ./gradlew \
  :xtream-codec-base:test --tests "io.github.hylexus.xtream.codec.base.expression.*" \
  :xtream-codec-core:test \
  :xtream-codec-debugger-service:test \
  :debug:xtream-codec-debugger-app:test \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:test --tests "*Jt808DynamicDslTest" \
  :xtream-codec-core:checkstyleMain :xtream-codec-core:checkstyleTest \
  :xtream-codec-debugger-service:checkstyleMain :xtream-codec-debugger-service:checkstyleTest \
  :debug:xtream-codec-debugger-app:checkstyleMain :debug:xtream-codec-debugger-app:checkstyleTest \
  :xtream-codec-core:checkLicenses :xtream-codec-debugger-service:checkLicenses \
  :debug:xtream-codec-debugger-app:checkLicenses \
  -Pxtream.backend.build.checkstyle.enabled=true \
  -Pxtream.backend.build.license-checker.enabled=true --console=plain
```

以上检查通过。最后的 core 改动再跑了 core 全量、服务、应用和 808 DSL 测试，以及 core Checkstyle、license 和 jmhJar。最终结果：base 表达式 13、core 443、调试服务 6、独立应用 3、808 DSL 10，合计 475 项，0 失败、0 跳过。808 包含 2013/2019 既有 JSON、HEX 和 Trace 验证。

文档构建：`cd docs && mise exec node@22 -- pnpm docs:build`，通过。新增 DynamicExpressionQuickStartTest 已标注 ReferencedByDocs，网页源码引用同步；测试验证 02aabb 与 sum=4。

前端：在 debug/xtream-codec-debugger-ui 执行 `mise exec node@22 -- pnpm run check` 和 `pnpm run build`，均通过。

本地页面：启动 127.0.0.1:18765，经 Chrome 实际导入 AST 条件/长度/派生定义，编码 n=2、payload=aabb → 02aabb；解码回相同字段并得到 sum=4，消费 24 bit。导出 codec-schema.json 后用 JSON 比较验证与导入定义相等。将派生改为 SpEL 后，默认应用展示 $.result、bit -1、“未启用表达式引擎”。页面验证完成后关闭本次标签页并停止服务。HTTP 测试另验证不能注册引擎。

`git diff --check` 通过；另扫描本次涉及的未跟踪源码和 artifacts，无行尾空白。运行期源码已去掉 DynamicExecution、DynamicEncoder/Decoder、ScopedMap、DynamicByteValues 与 DynamicValueEvaluator；新执行器不访问 definition/options。

## OpenSpec 与边界

`mise exec node@26.10.0 -- openspec validate redesign-dynamic-codec-execution --strict` 通过。CLI 提示归档依赖：主 dynamic-binary-codec spec 尚不存在，需先同步/归档前序 add-dynamic-binary-codec-and-debugger 的同名规格，再归档本变更。本次不执行归档。

字符串表达式沿用宿主引擎语义，未提供脚本沙箱或时间强制中断；自定义引擎及编译表达式必须可并发使用。性能对比是同机短测，分配变化较稳定，耗时不作为跨机器保证。具体局限与 TLV 成本见基准报告。

## 提交前完整检查补验

此前的定向验收命令未启用 Error Prone，不能替代仓库提交前脚本。用户反馈后实际运行原始脚本，首次在 core compileJava 复现 36 个 NullAway 错误和 4 个 NullablePrimitiveArray 警告；生产编译修复后，继续在 compileJmhJava 发现 12 个 JMH 生命周期初始化错误，以及测试中的 3 个 MissingOverride 警告。

修复限定在本次动态实现中：

- DynamicFrame 将数组本身的可空注解写为 long @Nullable []，Trace 数组同时标明数组及元素可空。
- DynamicLayoutCompiler 在由已校验定义生成布局时显式要求必填属性与已分配地址非空；保留原有定义校验和执行语义。
- DynamicRuntime 明确诊断消息非空；DynamicWriteExecution 在扩展分支中检查缺失值，使静态分析能够证明调用合同。
- 两个新增 JMH 基准仅对由 @Param/@Setup 初始化的字段豁免 NullAway.Init，保留其他空值检查。
- DynamicCompositionTest 的接口实现补齐 @Override。

最终执行：

```bash
JAVA_HOME="$(mise where java)" bash build-script/before-commit.sh
```

结果为 **BUILD SUCCESSFUL in 17s**，231 actionable tasks：226 executed、5 up-to-date。脚本中的 clean build、Error Prone/NullAway、Checkstyle、license 与全库测试按原配置执行；没有修改脚本或关闭检查。最终编译日志没有上述源码错误和警告，仍有 Gradle 10 兼容性弃用提示及 JVM 类共享提示。日志保存在本机 /tmp/xtream-before-commit-redesign-final.log；git diff --check 和涉及的未跟踪 Java 文件空白检查通过。

## 逻辑与位运算命名调整

按用户反馈将 and/or 与 &&/|| 对齐为严格 Boolean 的短路逻辑运算，Java 常量使用 AND/OR；位运算使用 BIT_AND/BIT_OR/BIT_XOR，对应 bitAnd/bitOr/bitXor 及 &/|/^。all/any/xor 保留为 JSON/字符串输入别名，导出使用规范名称。旧位运算 and/or 定义需迁移到 bitAnd/bitOr，旧 ALL/ANY Java 常量需改为 AND/OR；仓库中的旧位运算用法已迁移，规格和文档已注明兼容例外。

DynamicExpressionTest 验证全部 19 个规范运算与符号的 Java/JSON 解析、规范导出和实际求值；all/any/xor 的兼容导入；and/or、&&/||、all/any 三组短路；编码和解码拒绝数字逻辑操作数，不执行 Python 风格的真假值转换或返回操作数。定向测试与 Error Prone/Checkstyle、docs 生产构建、OpenSpec 严格校验和 git diff --check 通过。

最后实际执行原始 before-commit.sh，结果为 **BUILD SUCCESSFUL in 17s**，231 actionable tasks：226 executed、5 up-to-date。本机日志为 /tmp/xtream-logical-operators-before-commit.log，无源码编译错误或警告。
