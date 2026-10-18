# Tasks

## 1. 入门教程和配置参考

- [x] 1.1 新增 `quick-start.md`，以已有位字段与嵌套集合预设串联协议表、配置步骤、完整 DSL、编码值、逐段 HEX、解码结果和 Studio 操作；复用或补充文档示例测试，验证 JSON 导入编译、编码结果 `b10702011234025678`、已知 HEX 解码结果和消费长度，并维护被引用类型的 `@ReferencedByDocs`。
- [x] 1.2 新增 `dsl-reference.md`，覆盖设计中列出的所有公开字段属性、类型和值形状、长度来源/单位、作用域、条件/版本/分支、派生/生成和 options；逐项对照 `DynamicField`、`DynamicFieldValidation` 和随 core 发布的 JSON Schema 核查必填条件、默认值及互斥关系，确认完整 JSON 与片段均有清晰标记。

## 2. 按协议需求提供可运行示例

- [x] 2.1 在 `examples.md` 编写文本/BCD/填充及嵌套重复记录示例，给出完整定义、编码值、预期 HEX 和解码结果，解释字符与字节长度、数组项数与总长度及逐项长度前缀；运行对应现有测试，对未覆盖的新组合补充窄范围示例测试并通过。
- [x] 2.2 编写条件/choice/defaultVariant 和 TLV 示例，给出完整输入输出，说明 choice 直接承载选中分支的值、TLV 重复键和未知键 `rawHex`，补充 map/pair 的布局差异；运行或补充对应测试，核对命中和默认分支、重复键顺序、未知 TLV 键保留及未知 pair 键限制。
- [x] 2.3 编写位字段/对齐/派生视图及自动生成/校验和示例，给出完整输入输出，区分顺序位布局与最低有效位偏移、线上生成值与零占用派生值；运行或补充对应测试，验证 HEX、回填长度、自动计数、解码实际值以及默认和严格校验行为。
- [x] 2.4 编写多版本完整示例及 JT/T 808 进阶索引，给出两个目标版本的编码值、HEX 和解码结果；用对应版本测试核对布局，检查 0200/0704/8003 资源链接可达且帧标识/转义说明与各自测试资源及 Studio 预设一致。

## 3. 排错和阅读路径

- [x] 3.1 新增 `troubleshooting.md`，覆盖未知属性/旧整数写法、长度单位与边界、remaining 位置、前向引用与缺失字段、数组/choice/TLV 值形状、未对齐、固定长度不足以及校验警告；每类给出原因和修正方式，依据当前校验和现有失败测试核对描述，说明如何读取字段路径、bit 偏移及 Trace。
- [x] 3.2 在现有 `README.md` 和 `sidebar/zh.ts` 增加四个新页面的阅读入口及必要页间链接；确认旧概览的章节与锚点保留，教程、参考、示例、排错均能从侧栏定位。

## 4. 集成验收

- [x] 4.1 执行 `cd docs && pnpm docs:build`，确认构建成功，新增页面、文档代码导入和站内链接无新增错误。
- [x] 4.2 执行 `openspec validate document-dynamic-codec-dsl --strict`、`openspec validate --specs --strict` 和 `git diff --check`；核对本次 diff 仅包含文档、导航、必要的示例验证及变更计划，确认未修改生产 DSL 行为。

## 5. Studio 启动与部署

- [x] 5.1 新增 `studio.md`，覆盖构建与运行环境、带 UI 的单文件 JAR、Gradle 运行、Vite 开发、配置优先级和默认值、文件/SQLite 持久化与备份迁移，以及 Docker 构建、数据卷、日志和停止重启；逐项对照当前构建脚本、应用入口、工作区实现和 Dockerfile 核对命令及说明。
- [x] 5.2 将 Studio 页面接入动态编解码侧栏、概览阅读路径和入门教程，使未启动编辑器的用户能先完成启动再继续 DSL 操作。
- [x] 5.3 运行文档构建，检查新增页面及动态编解码页面间的链接和锚点，完成变更与主规范的严格校验以及 `git diff --check`。

## 6. Studio 文档与项目 README 配图

- [x] 6.1 核对用户提供的八张 Studio 截图，在 Studio 指南中按基础报文和 JT/T 808 0200 分组配图说明，并在根目录 README 增加 Studio 介绍、文档入口及可打开原图的双列预览；两处均使用全部八张图，复用原始图片资源。
- [x] 6.2 运行文档构建，核对两处的八张图片引用、文档产物图片路径及新增链接和锚点，完成 OpenSpec 严格校验与 `git diff --check`。
- [x] 6.3 在根目录 README 的 Studio 和 JT/T 808 QuickStart 入口补充 Studio / Dashboard 用途及运行方式备注，说明 Studio 的 808 截图是内置 DSL 示例，核对 Dashboard 的现有功能，并通过 OpenSpec 严格校验和 `git diff --check`。
- [x] 6.4 在根目录 README 的 Studio 入口增加 Docker 快速启动命令、访问地址和数据卷说明，按发布脚本和 Dockerfile 核对镜像名、端口及挂载路径，检查命令语法并通过 OpenSpec 严格校验和 `git diff --check`。
- [x] 6.5 将根目录 README 的 Modules / 项目模块整节移动到 Intro / 介绍之后，确认章节内容与标题保持一致、其余章节顺序不变，并通过 OpenSpec 严格校验和 `git diff --check`。
