# Studio 持久工作区

每个标签是一份独立文档，包含调试模式、协议版本、应用的 Schema、未完成 DSL JSON、值输入、HEX 和最近运行快照。Ctrl/⌘+S 或“保存”写入后端；新文档首次保存时选择名称和目录。“我的报文”支持嵌套目录、重命名、移动和递归删除。关闭标签不删除服务器文档；删除服务器目录时，打开的文档作为未保存副本保留。

浏览器 IndexedDB 保存打开的标签和未保存草稿，首次使用迁移原 `xtream-codec-workbench-v1` 的三个模式草稿。清理站点数据会删除这些恢复副本，后端正式文档仍保留。浏览器草稿恢复失败时提示错误，允许继续内存编辑，不会自动覆盖无法识别的原草稿；正式保存失败或冲突时保留编辑内容和未保存标记。

## 选择存储

默认是文件存储，目录为 `~/.xtream-codec/studio`。系统属性优先于环境变量；更改介质需要重启应用，不会自动迁移数据。

| 环境变量 | 系统属性 | 默认值 |
| --- | --- | --- |
| `XTREAM_STUDIO_STORAGE` | `xtream.studio.storage` | `file`，可选 `sqlite` 或自定义提供者名称 |
| `XTREAM_STUDIO_DATA_DIR` | `xtream.studio.data-dir` | `~/.xtream-codec/studio` |
| `XTREAM_STUDIO_SQLITE_PATH` | `xtream.studio.sqlite-path` | 数据目录下的 `workspace.db` |

```bash
# 使用指定磁盘目录
XTREAM_STUDIO_DATA_DIR=/var/lib/xtream-studio \
  ./gradlew :xtream-codec-studio-app:run --args="8765" \
  -Pxtream.frontend.build.xtream-codec-studio-ui.enabled=true

# 使用 SQLite，JDBC 驱动随应用分发
XTREAM_STUDIO_STORAGE=sqlite XTREAM_STUDIO_DATA_DIR=/var/lib/xtream-studio \
  ./gradlew :xtream-codec-studio-app:run --args="8765" \
  -Pxtream.frontend.build.xtream-codec-studio-ui.enabled=true
```

发布时运行 `:xtream-codec-studio-app:installDist`，启动 `build/install/xtream-codec-studio-app/bin/xtream-codec-studio-app`。分发包运行只需要 JDK 21，包含 UI、Jackson 与 SQLite JDBC。容器部署将数据目录挂载到持久卷即可。

文件介质以 `records/{UUID}.{revision}.json` 保存独立条目，通过原子替换 `manifest.json` 发布完整快照；更新失败时旧 manifest 仍有效。启动后确认有效记录再清理孤立记录。进程持有数据目录锁，一个目录只允许一个应用实例。SQLite 在事务中同时提交整体版本和条目。初始化失败明确终止启动，不回退成空工作区。

工作区限制 10000 个条目，目录深度最多 64 层，名称为 1–120 个字符。同一目录内文档和目录名称不能重复。文档内容允许未完成的编辑原文，但格式版本、已应用 Schema 和运行快照必须符合合同。工作区 HTTP 请求最多 20 MiB；编解码请求仍使用原来的 2 MiB 上限。

## HTTP 合同

| 方法与路径 | 行为 |
| --- | --- |
| `GET /api/v1/workspace` | `{revision, entries}` 目录快照，仅含元数据，`document` 为 null |
| `POST /api/v1/workspace/entries` | 创建目录或文档，返回完整条目，201 |
| `GET /api/v1/workspace/entries/{id}` | 获取完整条目及文档内容，200 |
| `PUT /api/v1/workspace/entries/{id}` | 保存内容、重命名或移动，返回新版本条目，200 |
| `DELETE /api/v1/workspace/entries/{id}` | 删除条目及所有后代，返回新目录快照，200 |
| `GET /api/v1/workspace/export` | 导出 `studio-workspace` v1 备份 |
| `POST /api/v1/workspace/import` | 一次提交导入备份，返回目录快照，201 |

创建与更新请求包含 `parentId`（根目录为 null）、`kind`（folder/document）、`name`、`expectedRevision`、`document`（文件夹为 null，文档为内容）。创建时 `expectedRevision` 为 0；更新时传条目的当前版本，成功后加一。删除请求为 `{expectedRevision, workspaceRevision}`，除条目版本外还核对用户确认时的整个目录版本，避免误删确认后新增的后代。

错误响应为 `{error: "说明"}`。无效格式、循环移动、重复名称返回 400；不存在返回 404；过期版本返回 409；超限返回 413；存储读写失败返回 500。冲突时可保留当前标签，复制为新标签后另存，或关闭后重新打开服务器版本。

## 导出与迁移

文档菜单的“导出完整文档”生成 `{format: "studio-document", formatVersion: 1, name, document}`，包含当前未保存内容和最近运行快照。“导出服务器工作区”仅导出正式保存的所有目录及文档：`{format: "studio-workspace", formatVersion: 1, entries}`。字段编辑区继续提供仅 Schema 的导入导出。

完整文档导入为新标签，之后可以选择保存目录。工作区导入先验证全部记录和层级，再将所有身份重映射到新 UUID，放入独立的“导入-…”目录，一次性提交，不清空已有工作区。无效备份不会留下部分导入。切换文件 / SQLite 前先导出工作区，在新介质启动后导入；不要在应用运行时直接复制数据库作为迁移方式。

## 扩展存储

`WorkspaceStore` 提供 `read()` 和 `commit(expectedRevision, entries)`，属于 Studio 的工作区包，与 core 和 HTTP 无关。提交必须原子地比较整体版本、替换完整条目集合并递增版本，冲突抛出状态为 409 的 `WorkspaceException`；失败保留旧快照。条目领域规则由 `WorkspaceService` 校验。

嵌入应用时直接构造 `new WorkspaceService(customStore)`，传给 `CodecStudioServer` 的四参数构造器。宿主管理服务的关闭生命周期，HTTP server 不关闭注入的工作区。

独立部署可实现 `WorkspaceStoreProvider` 的 `type()` 和 `open(Path dataDirectory)`，在 JAR 的 `META-INF/services/io.github.hylexus.xtream.codec.studio.workspace.WorkspaceStoreProvider` 写入实现类的全限定名，将 JAR 加入运行 classpath，并将 `XTREAM_STUDIO_STORAGE` 设置为其 type。提供者自行读取所需部署配置。一个 type 必须匹配且只匹配一个提供者，初始化异常不会自动降级。
