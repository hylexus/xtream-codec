# 验证记录

日期：2026-10-18。变更：`isolate-studio-browser-workspaces`。所有实现任务完成后保留变更，等待后续归档。

## 验证结果

| 检查 | 结果 |
| --- | --- |
| Studio UI `pnpm test` | 50/50 通过 |
| Studio UI `pnpm check` | TypeScript 与补全脚本检查通过 |
| Studio UI `pnpm build` | 最终源码生产构建通过 |
| 草稿原生浏览器回归 | 10/10 通过 |
| 冲突界面浏览器回归 | 7/7 通过，包含焦点、Escape、快捷键保护、390px 窄屏按钮及并发冲突 |
| 临时 SQLite 后端与真实浏览器页面 | 1/1 通过 |
| 后端 `WorkspaceTest` | 25/25 通过，覆盖文件与 SQLite |
| Studio App `checkstyleTest` | 通过 |
| 变更 TypeScript、TSX、MJS 的 Prettier 检查 | 通过，单引号、100 列，与现有源码一致 |
| `git diff --check` | 通过 |
| `openspec validate isolate-studio-browser-workspaces --strict` | 通过 |
| `openspec validate --specs --strict` | 11 项通过，0 失败；已有规格的长文本提示不影响通过 |

草稿测试先在原共享 `current` 实现上运行，观察到 A 刷新后读取了 B 的备份，测试失败；独立记录实现后同一测试通过。此前的 47 项 UI 测试保持通过，新增 3 项 API/文档基线测试后共 50 项。

## 浏览器与数据隔离

- 使用系统 Chrome 的无头模式与 Playwright 临时浏览器上下文。同一测试中的 A/B 是同一上下文内的两个同源页面，实际使用原生 IndexedDB 和 sessionStorage。
- `draft-sessions.mjs` 导入 Vite 提供的实际存储模块；复制验证通过 `window.open` 实际继承 sessionStorage，再确认各页面获得不同可写记录。也覆盖并发初始化、刷新、旧共享草稿和旧模式迁移、事务回滚、配额、损坏记录及存储禁用。
- `save-conflicts.mjs` 加载实际 App，拦截 API 并模拟条件更新，记录提交版本，控制网络延迟以验证迟到运行和保存期间新增运行结果；捕获页面异常并断言为空。
- 真实接口验收使用现有 `0.11.0` 分发包启动后端，端口 `18765`，SQLite 数据目录位于新建的 `/private/tmp/studio-tab-integration.*/data3`。前端仍使用 `http://127.0.0.1:5173`，浏览器 API 请求全部转发到临时后端，没有写入开发后端 `8765` 或用户工作区。
- 真实接口记录的 UI PUT 状态依次为 `200, 409, 200, 409, 409, 200, 409, 404`。采用服务器版本没有额外 PUT；重复冲突没有自动重试；刷新后 A 为 `cc`、B 为 `dd`；删除后 A 的 `ee` 保留，服务器工作区仍为空；页面异常为空。
- 验收结束后关闭测试浏览器并停止本次启动的临时后端。用户已有服务保持运行。

## 规格场景核对

`studio-browser-sessions` 的 8 个场景均已核对：

| 场景 | 验证依据 |
| --- | --- |
| 两个页面分别编辑并备份 | 草稿回归 1：交替提交后分别刷新，完整 Session 相等；真实接口 A/B 编辑 |
| 切到后台和编辑器标签操作 | 页面隐藏备份沿用独立记录；草稿回归 1 对比多报文、固定、顺序、活动标签、侧栏及快照 |
| 刷新保留页面身份对应的内容 | 草稿回归 1 保留未完成 JSON、HEX 原文、基线和运行结果；真实接口分别恢复 `cc`/`dd` |
| 浏览器复制标签页 | 草稿回归 2 验证实际继承来源指针、首次写入前分配新记录，以及来源/副本刷新 |
| 新开页面及关闭后重新访问 | 草稿回归 2 验证无页面指针时复制最近成功快照，不改变既有页面归属 |
| 同时初始化多个新页面 | 草稿回归 2 并发加载三个页面，所有可写记录唯一 |
| 升级已有共享草稿 | 草稿回归 3、4 验证旧 `current` 与旧模式迁移，保留原始来源且不再写旧键 |
| 存储失败及已有恢复记录损坏 | 草稿回归 5–10 验证失败回滚、缺失/损坏指针、禁用存储、内存编辑及 UUID 回退 |

`studio-save-conflicts` 的 11 个场景均已核对：

| 场景 | 验证依据 |
| --- | --- |
| 两个客户端从同一版本保存 | 冲突回归 1 与真实接口验收，确认选择前保留 B 内容与基线 |
| 普通失败不触发覆盖选择 | API 单测与冲突回归 3：400/404/500、网络错误及 422 编解码合同 |
| 成功采用服务器版本 | 文档单测验证完整替换、模式/身份/固定状态/元数据/基线；浏览器验证不发 PUT，A 不受影响 |
| 本地内容成功覆盖 | 冲突回归 2、真实接口验收和后端合同，验证新版本及保存基线 |
| 服务器已重命名或移动 | 冲突回归 2 验证保留服务器名称与目录，真实接口验证第三方重命名 |
| 确认后第三方再次保存 | 冲突回归 2 的预期版本为 `[1, 2, 3]`，重新选择前不自动重试；真实接口与两种后端合同通过 |
| 取消或关闭弹窗 | 冲突回归 1、3、6：Escape 和取消保留编辑/基线，不额外写入或关闭文档 |
| 获取或写入失败及服务器删除 | 冲突回归 3、6、真实接口和后端合同：保留文档，不发送 POST 重建 |
| 覆盖请求期间继续编辑 | 冲突回归 5 控制 PUT 与编解码回包顺序，只确认提交快照，新增 HEX/运行结果保持 dirty |
| 采用服务器内容后旧运行回包到达 | 冲突回归 4：旧回包不改变已采用的 HEX、快照和保存基线 |
| 保存后关闭遇到版本冲突 | 冲突回归 6：原确认框暂停、冲突弹窗单独呈现，取消/失败停止队列，成功后才关闭并询问下一报文 |

冲突回归 7 额外验证不同报文的保存同时返回冲突时按队列逐个处理，保持每个保存流程和目标归属。

## 复现命令

在 `xtream-codec-studio-ui` 目录执行：

```bash
pnpm test
pnpm check
pnpm build
STUDIO_PLAYWRIGHT_MODULE=/path/to/playwright/index.mjs node --test test/browser/draft-sessions.mjs test/browser/save-conflicts.mjs
```

浏览器脚本依赖本地 Vite 和单独准备的 Playwright。真实后端脚本默认跳过；先在仓库根目录用空临时目录启动独立后端，例如：

```bash
studio_verify_dir="$(mktemp -d /private/tmp/studio-verify.XXXXXX)"
JAVA_HOME="$(mise where java)" XTREAM_STUDIO_DATA_DIR="$studio_verify_dir" XTREAM_STUDIO_STORAGE=sqlite \
  ./gradlew :xtream-codec-studio-app:run --args="18765" \
  -Pxtream.frontend.build.xtream-codec-studio-ui.enabled=false
```

另一个终端在 UI 目录运行：

```bash
STUDIO_PLAYWRIGHT_MODULE=/path/to/playwright/index.mjs \
STUDIO_TEMP_API_URL=http://127.0.0.1:18765 \
  node --test test/browser/live-workspace.mjs
```

在仓库根目录执行：

```bash
JAVA_HOME="$(mise where java)" ./gradlew :xtream-codec-studio-app:test \
  --tests "io.github.hylexus.xtream.codec.studio.workspace.WorkspaceTest"
JAVA_HOME="$(mise where java)" ./gradlew :xtream-codec-studio-app:checkstyleTest \
  -Pxtream.backend.build.checkstyle.enabled=true \
  -Pxtream.frontend.build.xtream-codec-studio-ui.enabled=false
git diff --check
openspec validate isolate-studio-browser-workspaces --strict
openspec validate --specs --strict
```

## 行为边界

- 本次隔离浏览器标签页的报文草稿；主题、主题色与布局偏好仍按站点保存。
- 页面加载保留恢复来源记录，暂不自动清理历史来源；容量不足会提示备份失败并保留现有内容。
- 恢复以成功提交的备份为准，无法保证浏览器被强制终止前尚未完成的异步写入。
- 没有自动合并或绕过版本检查的强制覆盖接口；目录元数据操作不增加此次内容冲突弹窗。
