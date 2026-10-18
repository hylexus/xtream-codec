# Design

## Context

动机见 `proposal.md`。当前实际情况如下：

- `docs/src/guide/core/dynamic/README.md` 已有约 330 行能力说明，但侧栏只有这一页，完整报文的配置路径分散在不同章节中。
- DSL 的实际属性和组合限制由 `DynamicSchema`、`DynamicField`、`DynamicFieldValidation`、JSON Schema 以及编译器共同决定。JSON Schema 的结构提示不能替代字段引用和报文边界的检查。
- `xtream-codec-studio-ui/src/presets.ts` 已有位字段与嵌套集合、文本与字节序、自动生成与校验和、808 位置汇报等预设，可以作为教程基础。
- `DynamicQuickStartTest`、`DynamicChoiceTest`、`DynamicEntriesTest`、`DynamicDerivedTest` 等已经提供已知 HEX 的验证。JT/T 808 模块另有完整 DSL 资源和 `Jt808DynamicDslTest`。
- `gradle.properties` 当前为 `projectVersion=0.11.0`。文档版本和 JavaDoc 应以该文件为准。

## Goals / Non-Goals

**Goals:**

- 用户无需阅读 Java 源码，即可把有字段顺序、位宽、长度和条件说明的协议表转换为合法 JSON 定义。
- 每种数据形状都能找到可直接填写的编码值；完整示例提供独立的预期字节及解码结果。
- 新手从教程进入，熟悉 DSL 的用户能按属性或错误现象查阅；Java 调用方仍能访问原有集成内容。
- 用户能按指南启动 Studio，并明确开发服务器与独立应用的端口、部署配置和持久化位置。

**Non-Goals:**

- 不修改引擎、Studio、JSON Schema、DSL 语义或默认配置。
- 不增加用户体系、协议自动识别、DSL 自动生成器或额外表达式引擎。
- 不把 JT/T 808 的转义、加解密、组包等协议处理描述为通用 DSL 的功能。

## Decisions

### 1. 分页补全，保留现有入口和章节锚点

在 `docs/src/guide/core/dynamic/` 新增五页，并在 `README.md` 开头和中文侧栏加入入口：

| 文件 | 用户要完成的任务 | 内容组织 |
| --- | --- | --- |
| `studio.md` | 启动编辑器并持久部署 | 环境要求、源码构建、单文件 JAR、前端开发、配置表、持久化及 Docker |
| `quick-start.md` | 写出并验证第一份报文 | 协议表、配置步骤、完整定义、编码值、字节拆解、解码结果、Studio 操作 |
| `dsl-reference.md` | 查某个属性如何填写 | 类型和值形状、字段属性、长度规则、条件/分支/版本、派生/生成、options |
| `examples.md` | 按类似协议修改现成定义 | 按协议需求分组的完整示例，附进阶 JT/T 808 入口 |
| `troubleshooting.md` | 定位应用定义或运行时错误 | 现象、原因、修正示例，以及字段路径和 bit 偏移的读取方式 |

现有 `README.md` 保留概览、Java 集成和已有章节。将所有内容继续堆在一页会增加定位成本；立即搬迁现有章节则会破坏外部链接，因此本次只增设阅读路径和相关页间链接。

### 2. 教程先解释配置决策，再给完整结果

教程以 Studio 中已有的“位字段与嵌套集合”预设为主线，解释 `version: u3`、`flags: u5`、负载长度、嵌套 `struct`、元素数量和 `array.item`。它的已知结果为 `b10702011234025678`。

按以下顺序讲解：

1. 从协议表确定字段顺序、数值位宽、字节序、长度单位和容器边界。
2. 用 `id` 建立稳定字段标识，说明 `name` 是显示名称、JSON 属性顺序不决定报文顺序。
3. 将报文中的长度字段配置为 `computedLengthOf`，同时为被约束的字段配置 `length.ref`；解释两者分别负责编码回填和字段边界。
4. 描述 `payload` 子对象和 `items` 数组，明确每个值对应的是字段 `id`，标量元素不额外包一层 `item`。
5. 给出可独立导入的完整 DSL、编码值、逐段 HEX 和完整解码结果，说明回填字段出现在解码值中。
6. 在 Studio 自由组合模式中使用“结构定义 JSON”“应用定义”“编码值 JSON”“编码为 HEX”“解码为字段值”等实际界面名称完成验证，并说明如何用 Trace 对照字段和字节。

教程只使用默认可运行的配置。宿主字符串表达式、扩展注册、缓冲区生命周期等留在现有集成章节，避免阻塞首次配置。

### 3. 参考页同时回答语法、适用范围和输入形状

参考页以实际 `DynamicField` 和 JSON Schema 为索引，覆盖所有公开字段属性，不依靠用户打开源码补齐信息：

- 顶层：`schemaVersion`、`fields`；说明未知属性被拒绝、不能把编辑器配置 `$schema` 直接塞进定义。
- 通用属性：`id`、`name`、`type`、`endian`、`bitOrder`、`when`、`versions`。
- 标量：`uN/iN`、`float + bits`、`string + charset`、`bcd`、`hex`、`bytes`、`bits` 的输入和解码形状，注明 JSON 大整数、字节长度与字符数的区别。
- 边界：`length` 的 `fixed/ref/prefixBits/expression/remaining` 五选一，`bit/byte/item` 单位和适用类型；明确数组的字节总长与元素数量不同，`prefixBits` 会实际占用报文字节。
- 结构：`fields`、`item`、`variants`、`defaultVariant`、`ref`；展示 `struct` 对象、标量/对象数组、直接承载所选分支值的 `choice`、有序条目数组的实际值形状。
- 自动值：`computedLengthOf`、`generated` 四种来源、`derived.ref` 和 `derived.expression`；解释哪些值编码时可以省略、哪些值占用线上空间。
- 对齐和类型选项：`alignTo/fillBit`，`padByte/padSide/stripPadding`，`keyBits/lengthBits`，`bitOffset/bitWidth`，`lengthUnit`，`extensionId` 的使用条件。
- 引用和版本：同级与外层已可见字段、限定路径、普通前向引用限制、生成计数/长度回填的特例，区分 DSL 版本、编译目标版本和报文内版本字段。
- 表达式：可复制的 AST 示例、条件/长度/生成值/派生值的入口及返回类型；字符串引擎和扩展的宿主注册要求链接到现有章节。

属性表注明必填条件、默认值和互斥关系，短片段必须标为字段片段；完整示例都带 `schemaVersion` 和顶层 `fields`。

### 4. 每个示例提供完整的使用闭环

`examples.md` 按以下主题组织。每个主题提供独立定义、编码值、预期 HEX、解码结果及协议选择说明；可以复用已有测试中相同的定义与预期结果。

| 主题 | 必须说明的配置决策 |
| --- | --- |
| 文本、BCD 和填充 | 长度按编码后的字节计算；BCD 前导零；显式启用补齐和去填充 |
| 嵌套对象和重复记录 | `struct`、`array.item`、记录数量、总字节长度和逐项长度前缀的区别 |
| 条件和消息类型分支 | `when` 决定是否存在，`choice` 选择结构，未命中时的默认分支和值形状 |
| TLV、Map、Pair | 外层集合长度与条目头的区别；用一个完整 TLV 示例证明重复键和未知键的 `rawHex`，另给 map/pair 的配置差异及未知 pair 键限制 |
| 位字段和派生视图 | 顺序位宽与整数最低位偏移的区别；`align` 补位；`derived` 不重复占用字节 |
| 自动生成与校验和 | 固定值、自动计数、长度回填、校验范围；默认报告不匹配与严格模式的差别 |
| 多版本布局 | 相同 `id` 的默认定义与精确版本、目标版本选择、两种版本各自的值与 HEX |

进阶部分引用现有 JT/T 808 完整样例，说明 0200、0704、8003 分别能帮助解决哪些配置问题。必须明确仓库测试样例去掉两侧 `7e` 且不处理转义，Studio 0200 预设显式声明了边界字段，避免混用两类 HEX。

### 5. 验证面向文档承诺的字节和结果

新增完整示例都使用当前引擎核对。优先复用已有测试；对新增组合补充集中、简短的文档示例测试，不引入 Markdown 解析框架或新的生产依赖。预期 HEX 按协议独立给出，不能只用“编码后再解码”作为正确性证明。

测试要覆盖 JSON 导入和编译、编码 HEX、已知 HEX 的解码结果、消费长度以及示例涉及的生成/校验行为。文档引用的 Java 类型按仓库规范维护 `@ReferencedByDocs`；新增 JavaDoc 使用简体中文、保留原作者并注明 Codex 贡献。

最终运行所涉及的 core 测试、`pnpm docs:build`、OpenSpec 严格校验和 `git diff --check`；检查新页面均能从侧栏访问、代码导入和站内链接有效。

### 6. Studio 部署说明以现有运行入口为准

新增 `studio.md`，从仓库根目录的 `shadowJar` 构建和 `java -jar` 启动开始，说明 Java 21 运行要求、Node.js/pnpm 构建要求及默认关闭的前端构建开关。分别提供 Gradle 直接运行与 Vite 热更新路径，区分 `8765` 独立应用端口和 `5173` 前端开发端口。

对照 `CodecStudioConfiguration`、`WorkspaceStores`、Logback 和 Dockerfile 列出系统属性、环境变量、优先级与默认值，给出文件/SQLite 数据目录、导出迁移、Docker 本地构建、挂载卷、日志及停止重启命令。容器默认监听地址和数据目录单独说明；镜像发布脚本用于核对镜像名称与标签。根目录 README 的 Studio 入口提供与 Dashboard 类似的 `docker run` 快速启动，使用发布脚本定义的 `latest` 镜像，映射 8765 端口并挂载命名卷到 `/data`，附访问地址和持久化说明。检查命令语法与配置一致性；纯 README 扩展不需要重新构建文档站，也不新增运行时代码或测试。

### 7. 复用用户截图连接功能介绍与实际界面

在 Studio 指南增加界面预览，按基础报文、JT/T 808 0200 两组展示八张现有 PNG，并按可视化编辑、DSL JSON、运行结果排列，逐张提供与截图一致的说明。文档中按正文宽度展示，沿用其他页面的 `![说明](/img/...)` 绝对图片路径；根目录 README 增加 Studio 介绍、部署文档入口和双列截图预览，使用仓库相对路径并链接原图。复用同一组资源，不复制、裁剪或修改原截图。构建后核对八张图片在两处均完整引用且文档产物中的图片可达。

根目录 README 在 Studio 和 JT/T 808 QuickStart 入口补充定位说明，突出 Studio 与具体协议无关、Dashboard 为 JT/T 808 协议专用：Studio 是独立的通用报文编辑与编解码调试工具，其中 808 截图展示内置 DSL 样例；Dashboard 随 JT/T 808 服务端运行，用于 808 服务状态、在线会话和协议联调。保留现有标题与锚点，以简短备注帮助用户选择入口。

模块结构章节紧接项目介绍，随后展示 Studio 和 JT/T 808 QuickStart，使阅读顺序从项目用途、模块组成过渡到工具与启动方式；移动时保留各节原文和标题。

## Risks / Trade-offs

- 文档示例与 Java 测试中的 JSON 可能重复 → 限定为清晰的教学样例，维护文档引用标记，逐份核对预期 HEX 和解码值；不为消除少量重复引入示例生成系统。
- 参考页与现有概览可能出现说明重叠 → 新页负责查配置和输入形状，现有页保留 API 与执行行为，使用页间链接连接详细说明。
- JSON Schema 能通过而编译或执行仍失败 → 明确结构校验、引用/版本校验、实际字节边界校验的不同阶段，在排错页给出对应例子。
- 将生成常量误认为解码断言、将校验警告误认为默认拒收 → 在生成与校验示例中明确实际解码行为和严格模式入口。

## Migration Plan

只增加文档入口和示例验证，无数据或 API 迁移。保留旧路径与锚点；需要回退时移除新增文档及对应导航、恢复新增的概览入口即可。
