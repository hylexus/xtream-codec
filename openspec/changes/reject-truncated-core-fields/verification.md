# 验证记录

## 修复前复现

新增 TruncatedFieldDecodingTest，参数化展开共 58 个用例。修复前执行 58 个，失败 22 个：

- 普通模式未对缺失或截断的必需字段抛出预期异常；基本类型 Record 组件还可能在构造器阶段才失败。
- 集合及嵌套 POJO 中缺失、截断的 detail 字段在普通模式被接受。
- 合法空字节数组在普通模式可能返回 null，与跟踪模式结果不同。
- 字符串及 ByteBuf 字段在容量大于已写入数据时，即使启用跟踪也可能不拒绝截断内容。

输入缓冲区使用非零起始偏移，并保留额外容量以区分容量边界与可读数据边界。回归用例还覆盖已读取前缀后的内容不足，以及条件不成立、零长度和读取剩余输入的合法空内容。

复现命令：

```bash
./gradlew :xtream-codec-core:test --tests '*TruncatedFieldDecodingTest'
```

## 定向验证

修复后 TruncatedFieldDecodingTest 的 58 个用例，以及既有 NestedAndSequenceMetadataTest 的 22 个用例全部通过。

- 两种模式统一在字段长度提取后检查当前可读字节数，缺失或截断内容抛出 IndexOutOfBoundsException。
- 集合及嵌套对象的半个 detail 不再被忽略，集合外 tail 保持未读。
- 条件为 false 的字段正常跳过；零长度字符串沿用 null，零长度字节数组沿用空数组。
- 跟踪模式保留失败节点和诊断信息，已消费的长度前缀不回滚。

## 集成验证

- 核心模块全量 test：840 个用例通过，失败、错误和跳过均为 0。
- 核心模块 testWithoutJackson：3 个用例通过。
- debug 核心模块全量 test：23 个用例通过，包含既有嵌套对象与编码长度示例。
- checkstyleMain、checkstyleTest：通过，错误数为 0。
- OpenSpec 严格验证、git diff --check 及新增文件空白检查：通过。
- 未修改文档引用源码片段，不需要运行 docs 构建。

集成验证命令：

```bash
./gradlew :xtream-codec-core:test \
  :xtream-codec-core:testWithoutJackson \
  :xtream-codec-core:checkstyleMain \
  :xtream-codec-core:checkstyleTest \
  :debug:xtream-codec-core-debug:test \
  -P xtream.backend.build.checkstyle.enabled=true

openspec validate reject-truncated-core-fields --strict
git diff --check
```

## 兼容性说明

普通解码不再对缺失或截断的必需字段返回部分对象。调用方应提供完整报文；协议允许字段不存在时，应通过字段条件明确表达。合法空内容仍使用字段编解码器自身的结果，例如空字符串为 null、空字节数组为长度为零的数组。

失败不承诺回滚已读取的前缀或容器切片；本次保证不继续把半个字段当成下一条集合元素，也不借用容器外尾字段的数据。

## 提交前脚本的全仓库回归

按 `build-script/before-commit.sh` 的 clean build 及全部参数复现，失败任务为 `:ext:jt:jt-808-server-spring-boot-starter-reactive:test`。该模块 355 个测试中有 5 个失败，均为期望 null、实际得到空内容：

- `BuiltinMessage0608Test.testEmptyDataList` 的四种查询类型：count 为零，自定义 Message0608FieldCodec 返回空列表。
- `BuiltinMessage8800Test.testNoRetransmissionRequired`：重传包数量为零，字段长度表达式为零，字节数组编解码器返回空数组。

原因是普通解码入口不再在 readableBytes 为零时提前返回 null，而是按已确认的契约调用字段编解码器。测试原先断言了提前返回的 null，需要同步为合法空内容的结果。生产行为符合本变更规范；本次仅修改两行断言，保留原有精确报文字节及其他字段断言，提交前脚本及构建检查开关均未修改。

先定向执行两个测试类，13 个用例全部通过；随后重新执行与脚本相同的完整构建：

```bash
./gradlew clean build \
  -P xtream.backend.build.checkstyle.enabled=true \
  -P xtream.backend.build.errorprone.enabled=true \
  -P xtream.backend.build.license-checker.enabled=true \
  -P xtream.maven.repo.central-portal.enabled=false \
  -P xtream.maven.repo.private.enabled=false \
  -P xtream.maven.repo.github.enabled=false \
  -P xtream.maven.publications.signing=off --console=plain
```

结果为 BUILD SUCCESSFUL，耗时 39 秒，225 项任务中 220 项执行、5 项为最新状态。汇总所有模块的 JUnit XML 报告，共 1903 个测试，失败、错误、跳过均为 0：

| 测试任务 | 通过数量 |
| --- | ---: |
| 核心模块 test | 1318 |
| 核心模块 testWithoutJackson | 3 |
| 基础模块 test | 129 |
| JT/T 808 test | 355 |
| JT/T 1078 test | 16 |
| reactive server test | 16 |
| studio app test | 42 |
| debug 核心模块 test | 23 |
| debug reactive TCP test | 1 |

Checkstyle、Error Prone（含生产代码 NullAway）和源码许可证检查均按脚本启用并通过。两个相关 OpenSpec 变更的严格验证、工作区及暂存区差异空白检查均通过。未修改文档引用片段。

## 长度校验与跟踪记录分离

将 BasicBeanPropertyMetadata.checkReadableBytes 改为纯校验方法，仅检查当前可读长度并在不足时抛出 IndexOutOfBoundsException。普通解码直接调用；跟踪入口只捕获该检查的异常，创建失败字段节点、显式调用 scope.fail，再原样抛出异常。长度提取仍先于检查执行，成功路径仍由字段编解码器创建节点。

加强现有 TruncatedFieldDecodingTest 的断言，验证失败诊断仅有一条、异常类型与相对偏移正确、内容不足时诊断归属具体字段节点；成功及合法空内容的字段仅创建一个成功节点且没有诊断。长度前缀自身截断的用例保留其原有诊断归属。

执行下列定向命令，重构前后均为 80 个用例通过，其中截断回归 58 个、嵌套及集合元数据回归 22 个，失败、错误和跳过均为 0：

```bash
./gradlew :xtream-codec-core:test \
  --tests '*TruncatedFieldDecodingTest' \
  --tests '*NestedAndSequenceMetadataTest' --console=plain
```

随后再次执行上一节与 before-commit.sh 相同的完整构建命令，结果为 BUILD SUCCESSFUL，耗时 46 秒，225 项任务中 220 项执行、5 项为最新状态。所有模块 JUnit XML 汇总仍为 1903 个用例，其中核心模块 1318 个、无 Jackson 测试 3 个、JT/T 808 模块 355 个、debug 核心模块 23 个；失败、错误和跳过均为 0。

Checkstyle、Error Prone（含生产代码 NullAway）、源码许可证检查、OpenSpec 严格验证、工作区及暂存区差异空白检查均通过。未修改文档引用片段，未新增废弃 API 的测试。
