# Design

## Context

现有 React App 按模式保存 Draft，result/pending 是全局状态；JSON 编辑原文未进入草稿。Java HttpServer 仅适配 core 调试服务。工作区实现放入顶层 studio-app 的独立 workspace 包，不让 core 依赖文件系统、数据库或 UI。

## Goals / Non-Goals

Goals：保持既有编解码与 HeroUI 交互；让切换、异步请求、恢复及保存以文档身份绑定；两种存储通过相同合同测试。
Non-Goals：账号与多人协同、完整执行历史、Dashboard 页面、运行时更换介质、任意远程 SQL 配置。

## Decisions

1. 顶层 app/ui 同时迁移及改名，Java 应用包改为 codec.studio.app，保留 core.debugger。统一 Gradle Java 白名单、构建开关及相对路径。复用 application 分发任务。
2. 文档保存 Draft、schemaText、schemaTab 与最近运行快照（执行方向、输入签名、时间、结果）。原文允许未完成，执行才解析；dirty 比较持久内容，不以 UI 切换作为修改。运行响应按捕获的 tabId 写回，标签切换保持 CodeMirror/Trace 组件的实例状态，仅挂载打开的文档。
3. 前端工作区协调器维护标签和服务目录；可复用编辑组件承担原 App 的字段编辑与执行。运行面板在各文档内保留实例，底部停靠、内部滚动，上边界用可键盘操作的 separator 调整高度。标签主题和目录展开等是客户端状态。
4. IndexedDB 异步保存工作区草稿；首次恢复读取旧 localStorage，迁移三种模式草稿。只在恢复副本写入成功后视为可恢复，错误显示提示。Ctrl/Cmd+S 正式保存到后端；失败保持 dirty；保存过程中再次修改不会被响应覆盖。
5. WorkspaceEntry 使用 UUID、parentId、kind、name、revision、更新时间，文档 payload 是版本化 JSON 原文对象。WorkspaceService 校验目录图、同目录唯一名、移动及递归删除，提供 GET /api/v1/workspace，POST /entries，PUT/DELETE /entries/{id}，GET /export，POST /import。每条更新传 expectedRevision；介质提交另有整体快照 revision，避免并发产生悬空目录。
6. WorkspaceStore 只提供读取快照及带版本的原子提交，service 负责领域规则，接口与 HTTP 无关。FileWorkspaceStore 为每条记录保存独立版本 JSON，原子替换 manifest 发布完整修改；启动持有数据目录锁，禁止多个进程同时操作。SQLite 用 JDBC 和事务保存元数据及 JSON，整体版本比较与更新在同一事务。限制数据规模并使用同一合同测试重启、冲突和导入原子性。
7. 配置以系统属性或环境变量选择 file/sqlite 与数据路径。默认目录 ~/.xtream-codec/studio；自定义 WorkspaceStoreProvider 通过 ServiceLoader 加载，嵌入应用可直接传入 WorkspaceService。内置 sqlite-jdbc 随应用提供，不传递到 core。
8. 文档文件格式 studio-document v1，工作区 studio-workspace v1；导入新增并重映射 ID，校验整个备份后一次提交，顶层使用独立导入目录避免与原有名称冲突。导出保留结果快照，草稿格式另有版本；不保存临时 pending 状态。

## Risks / Trade-offs

- 大型 Trace 增加草稿与存储体积 → 只保存最近一次、请求与工作区大小限制，IndexedDB 使用异步写入。
- 并发保存覆盖 → entry revision 与 store revision 两层比较，冲突要求重新打开或另存文档。
- 浏览器恢复副本和后端不同 → 后端是正式文档，草稿只恢复未保存状态，不自动覆盖服务器。
- 文件多记录原子性 → 先写独立记录，再发布 manifest；失败保持原 manifest，遗留临时记录在启动清理。
- SQLite 可备份性 → 工作区导出为跨介质备份入口，运行中不建议直接复制数据库文件。

## Migration Plan

先迁移模块并跑已有测试；实现工作区后端与合同测试，再接入编辑器；保留旧 localStorage 直至迁移恢复成功。升级命令写入 README，历史归档 OpenSpec 不重写。回滚可恢复旧模块代码，正式工作区保留在独立数据目录。
