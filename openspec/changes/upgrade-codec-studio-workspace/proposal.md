# Proposal

## Why

现有调试器每种模式只保留一份草稿，全页面共用一次执行结果，不能并行维护多个报文。将其升级为 Xtream Codec Studio，让协议样例成为可分类、保存和恢复的文档，同时保持编解码引擎及其既有 API 不变。

## What Changes

- **BREAKING** 将独立应用与前端移至顶层 xtream-codec-studio-app、xtream-codec-studio-ui，更新启动命令和构建配置；core.debugger 名称保持不变。
- 多文档标签支持新建、重命名、复制、保存、关闭；编辑内容、未完成 JSON、执行状态及最近结果独立。
- 左侧目录树管理报文与嵌套文件夹，支持移动、删除和打开文档。
- 底部运行面板支持展开、折叠、调整高度和最大化，跟随当前文档展示结果。
- 后端工作区支持默认文件存储、SQLite、部署配置与自定义存储 SPI；前端 IndexedDB 用于恢复草稿。
- 保存文档及最近结果快照，提供文档和工作区导入导出、保存冲突检查及旧浏览器草稿迁移。
- 动态 DSL 的前置长度作为位于对应字段之前的独立 Trace 同级节点返回，并提供不含前缀的内容范围；Studio 预览使用内容范围，配色统一采用 Dashboard 的中性底色和语义强调色，Trace 树按共享列宽对齐节点、类型、位范围和值。
- Trace 展示实际 DSL/Java 类型；TLV/map/pair 按条目序号保留输入和线上顺序，条目下分别展示 Tag/Key、Length（pair 除外）和 Value，支持重复键、未知值和各段独立定位。

## Capabilities

### New Capabilities

- `studio-editor`: Studio 产品、顶层应用、多文档编辑器及运行面板。
- `studio-workspace`: 目录与文档、持久化存储、恢复及数据交换合同。

### Modified Capabilities

- `codec-tracing`: 动态 DSL 的长度前缀节点、内容范围和声明类型，以及有序条目的完整头部与值跟踪；保持原始字节和业务值兼容，TLV 条目路径改用序号以区分重复 Tag。

## Impact

影响独立应用、React 前端、Gradle 模块与前端配置、文档；新增 SQLite JDBC 依赖和工作区 API。编解码、实体目录及 core 公共 API 保持兼容。首版是单用户工作区，不增加账号、协同编辑或 Dashboard 页面；存储选择在启动时完成。
