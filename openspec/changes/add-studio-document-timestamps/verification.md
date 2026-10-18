# 验证记录

日期：2026-10-18。变更：`add-studio-document-timestamps`。

## 自动检查

| 检查 | 结果 |
| --- | --- |
| Studio `WorkspaceTest` | 27/27 通过，文件与 SQLite 均覆盖 |
| Studio `checkstyleMain` / `checkstyleTest` | 通过 |
| UI `pnpm test` | 50/50 通过 |
| UI `pnpm check` | 通过 |
| UI `pnpm build` | 通过 |
| 修改的 TS/TSX Prettier 检查 | 通过，单引号、100 列 |
| `git diff --check` | 通过 |
| `openspec validate add-studio-document-timestamps --strict` | 通过 |
| `openspec validate --specs --strict` | 11 项通过，0 失败；现有长文本信息提示保持原样 |

在仓库根目录执行：

```bash
JAVA_HOME="$(mise where java)" ./gradlew :xtream-codec-studio-app:test \
  --tests 'io.github.hylexus.xtream.codec.studio.workspace.WorkspaceTest' \
  :xtream-codec-studio-app:checkstyleMain :xtream-codec-studio-app:checkstyleTest \
  -Pxtream.frontend.build.xtream-codec-studio-ui.enabled=false \
  -Pxtream.backend.build.checkstyle.enabled=true
git diff --check
openspec validate add-studio-document-timestamps --strict
openspec validate --specs --strict
```

在 UI 目录执行 `pnpm test`、`pnpm check`、`pnpm build`。

## 场景核对

- **创建及更新报文**：HTTP 合同验证创建时间为正且与初次更新时间相同，更新保留创建时间；新目录及导入记录也使用同次时钟值。旧条目的重命名、移动保持未知创建时间，更新时间推进。
- **保存失败**：过期版本返回 409 后读取整个条目，与成功保存后的条目完全相同，包含两种时间。既有原子写入、存储异常与并发合同继续通过。
- **重启与历史记录**：两种介质的新记录重启后时间不变；测试实际写入不含 createdAt 的旧文件 JSON 或 SQLite JSON，验证读取为 null、更新不补造历史、再次重启保留时间。
- **备份导出和导入**：导出保留条目及时间；新版及缺失创建时间的旧备份均可导入，新身份使用此次导入时间，创建时间与更新时间相同，来源条目保持不变。
- **查看报文时间**：通过隔离 Chrome 和 Vite 页面验证未保存状态、首次保存后两种时间、目录名称提示、本地编辑不改变服务器时间、再次保存刷新更新时间、旧创建时间“未记录”和元数据不可用状态；查看信息不改变 dirty 标记，请求体不提交时间字段。

## 浏览器验收

使用 Playwright 临时浏览器上下文，加载 `http://127.0.0.1:5173` 的实际 App。API 全部拦截到测试数据，未操作真实用户工作区；两次写入仅发生于模拟响应，页面异常为空。

桌面为 1440×1000，窄屏为 390×844。“报文信息”通过文档工具栏的“…”菜单打开，对话框完整位于视口内，支持键盘打开、Escape 关闭并恢复菜单触发器焦点；侧栏折叠后入口仍可用。截图已检查动画结束后的布局。

复核步骤：新建报文后通过“…”菜单打开信息，应显示两处“尚未保存”；首次保存后查看时间并悬停目录名称；编辑 HEX 再次打开信息，原时间和未保存标记保持；使用 Ctrl + S 成功保存后更新时间刷新、创建时间保持。旧报文的创建时间显示“未记录”，服务器目录元数据未取得时显示“服务器时间不可用”。信息对话框打开时按保存快捷键不会叠加保存弹窗。

## 底栏移除及信息归位复验

- 移除底部状态栏及其专用样式，编辑区延伸到窗口底部；报文关联版本和未保存圆点继续正常展示。
- 左侧“我的报文”标题旁显示含目录的条目总数。模拟工作区请求返回 503 时显示“离线”，编解码服务仍可保持连接；刷新成功后恢复条目数。测试数据包含一个目录和一份报文，计数为 2。
- 保存按钮使用原生悬浮提示展示 Ctrl / ⌘ + S，工具栏中不增加常驻控件。1440、1100、1000、768 像素宽度均无工具栏横向溢出，目录标题与按钮不重叠；1440 像素下工具栏高 44 像素，较窄时沿用既有两行布局（82 像素）。390 像素下菜单与信息对话框可操作。
- 隔离 Chrome 验收 19 项检查通过，无页面脚本异常；模拟保存共 2 次，未访问真实工作区数据。复验等待保存通知消失后检查信息对话框截图。
- 本次界面调整复验直接运行项目内命令：`node --import tsx --test test/*.test.mjs`（50/50 通过）、`node node_modules/typescript/bin/tsc --noEmit`、`node --check src/completion.js`、`node node_modules/vite/bin/vite.js build`。App TSX 的 Prettier 检查通过，OpenSpec 变更及主规格严格校验通过。

## 部署与兼容

新增创建时间需要重启运行中的 Java 后端生效。旧数据无需批量迁移；创建时间未知的历史条目继续显示“未记录”。文件和 SQLite 的格式版本保持 1，原 Java Entry 构造方法保留；时间不进入文档内容、草稿格式或保存指纹。变更完成后保留待归档。
