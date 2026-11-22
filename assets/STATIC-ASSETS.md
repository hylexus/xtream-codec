# 静态资源来源

下表列明已有来源记录的静态资源。

| 文件 | 来源及用途 | 许可 |
| --- | --- | --- |
| `assets/branding/` 及其导出副本 | 由 Codex（AI）绘制，主 SVG 由 Python 生成。来源与导出方式见[项目标志说明](branding/README.md) | 项目根 Apache-2.0 |
| `docs/src/.vuepress/public/codec-background.svg` | 直接编写的渐变、网格和曲线路径，用作文档首页背景；未引用外部图片、字体或路径 | 项目根 Apache-2.0 |
| `debug/jt/jt-808-server-spring-boot-starter-debug-ui/src/assets/icons/svg/drag.svg` | 以直线坐标绘制的四向箭头，用作拖拽提示；未沿用旧 SVG 路径 | 项目根 Apache-2.0 |
| [`xtream-codec.drawio`](xtream-codec.drawio) 及[六张导出 PNG](#六张请求处理流程图) | hylexus 自行绘制的请求处理流程图 | 项目根 Apache-2.0 |
| `docs/src/.vuepress/public/img/ext/jt/jt808/extension/su-biao-attachment-flow.svg` | Codex（AI）根据项目的消息定义及附件服务示例编写，由 Mermaid CLI 11.12.0 导出。可编辑源码见[苏标文档](../docs/src/ext/jt/jt808/extension/jiangsu.md) | 项目根 Apache-2.0 |
| [十一张开发截图](#intellij-idea-开发截图) | hylexus 在 IntelliJ IDEA 中截取本项目示例 | 项目内容及画面中的第三方界面、标志各按适用许可和权利条款处理 |
| [Dashboard 与 Studio 网页截图](#dashboard-与-studio-网页截图) | hylexus 截取本项目运行中的界面 | 项目内容按项目许可提供，第三方素材保留各自适用的权利 |
| [工具截图和打赏码](#工具截图和打赏码) | NetworkCat 运行界面和项目打赏图片 | 见各自的来源记录 |

新背景和拖拽图标直接保存为可编辑 SVG，无需渲染脚本或额外字体。
原 `th1.jpg` 已停止随 docs 分发；历史版本仍可从 Git 取得，不对其历史授权作新声明。
原苏标附件流程图 `subiao.png` 带有 `CSDN @WXL2010` 水印，原作者、原文链接和转载许可均未确认。该图已从文档资源移除，由上表中的苏标附件上传时序图替代。
原 Reactor Netty 文档截图 `reactor-netty-consuming-data.png` 已从站点资源移除。[适配器文档](../docs/src/guide/server/core-component/netty-handler-adapter.md)现用项目说明介绍 TCP 集成，并保留上游文档链接。
未引用的 JT1078 Dashboard `public/vite.svg` 已移除。
Font Awesome 字体的版本、来源与归档成员见 [`webfonts/README.txt`](../docs/src/.vuepress/public/webfonts/README.txt)，原始许可见同目录 `LICENSE.txt`。

## 待核实资源

预览列出 `docs/src/.vuepress/public/img/` 中仍待核对的图片。`public` 中的 PNG 随站点发布，可编辑源图保存在 `assets/` 供仓库维护和本地预览。

无需手工维护图片路径、引用页和哈希表。运行：

```sh
python3 -B build-script/license/check-materials.py --preview-assets
```

工具扫描当前 PNG，在 `build/reports/static-assets/index.html` 生成本地预览，
`inventory.json` 列出完整路径、来源记录状态、SHA-256、元数据及可能的引用页，提取的 draw.io 数据也在同目录。
引用页按根 README 和 docs Markdown 中出现的完整文件名检索，只作为定位线索。
工具不修改原图，不构建前端，这些报告不进入站点或版本库。

补充来源记录时，在「已有来源记录的文档图片」的表格中填写相对于 `docs/src/.vuepress/public/img/` 的图片路径，并注明制作人或上游来源、适用许可或授权凭证、使用范围及核对日期。预览按表中的路径更新状态。

## 已有来源记录的文档图片

### 六张请求处理流程图

2026-11-21，hylexus 确认 `xtream-codec.drawio` 中的流程图均由本人绘制。源图及下列六张对应的
PNG 按仓库根 [Apache-2.0 许可证](../LICENSE) 提供。

`img/server/request-processing/` 中下列 PNG 和可编辑源图由 Git 提交
`6950f4f7`（2024-12-13，提交作者 hylexus）一并引入。核对各 PNG 的内嵌 draw.io 数据、
图元属性以及画面中的标题和高亮框后，可用当前源文件的相应页面维护这些图片。

| PNG | `xtream-codec.drawio` 页面 | 画面特征 |
| --- | --- | --- |
| `server/request-processing/request-flow-0.png` | `request-processing1` | 高亮 `XtreamNettyHandlerAdapter` |
| `server/request-processing/request-flow-1.png` | `request-processing2` | 高亮 `XtreamHandler` |
| `server/request-processing/request-processing.png` | `request-processing3` | 完整请求处理链路 |
| `server/request-processing/dispatcher-handler.png` | `handler1` | 从 `DispatcherXtreamHandler` 开始 |
| `server/request-processing/filtering-xtream-handler.png` | `handler2` | 从 `FilteringXtreamHandler` 开始 |
| `server/request-processing/exception-handling-xtream-handler.png` | `handler3` | 包含异常处理分支 |

匹配页面的图元结构和属性一致。当前源文件的这六页未发现 `image=` 或 HTML `img` 嵌入图元。

### IntelliJ IDEA 开发截图

2026-11-21，hylexus 确认下列截图由本人使用 IntelliJ IDEA 演示、构建或运行 xtream-codec 时制作，用于文档讲解项目类结构、构建和启动步骤。示例代码按所在文件的许可证提供，画面中的第三方界面、图标和商标保留各自适用的权利。

| `docs/src/.vuepress/public/img/` 下的路径 | 文档用途 |
| --- | --- |
| `ext/jt/jt808/customization/netty-customizer.png` | Netty 自定义接口类图 |
| `ext/jt/jt808/quick-start/quick-start-app-0200-log.png`、`ext/jt/jt808/quick-start/quick-start-app-startup.png` | 快速启动和位置汇报示例日志 |
| `faq/building-from-source/command-line-gradle-build.png`、`faq/building-from-source/idea-0.png`、`faq/building-from-source/idea-1.png`、`faq/building-from-source/idea-2.png`、`faq/building-from-source/idea-3.png` | Gradle 构建及 IDEA 项目配置 |
| `faq/code-analysis/field-codec.png` | 字段编解码器类层次 |
| `server/annotation-driven/handler-method-argument-resolver.png` | 参数解析器类图 |
| `server/core-component/ordered-components.png` | 可排序组件类图 |

### Dashboard 与 Studio 网页截图

2026-11-21，hylexus 确认下列图片来自本项目运行中的 Dashboard 和 Studio 网页，用于介绍项目功能和使用方法。Dashboard 模板保留 [Next UI 的 MIT 许可](../ext/jt/jt-808-server-dashboard-ui/LICENSE)，两套前端的第三方许可随构建汇总。部分 Dashboard 截图显示旧版标志。

| `docs/src/.vuepress/public/img/` 下的路径 | 文档用途 |
| --- | --- |
| `ext/jt/jt808/dashboard/overview-dashboard.png`、`ext/jt/jt808/dashboard/overview-data-link.png`、`ext/jt/jt808/dashboard/overview-session.png` | Dashboard 总览、数据链路和会话 |
| `readme/jt-808-dashboard-debug.png`、`readme/jt-808-docker-quickstart.png` | Dashboard 联调和 Docker 快速启动 |
| `readme/xtream-codec-studio-808-0200-01.png`、`readme/xtream-codec-studio-808-0200-02.png`、`readme/xtream-codec-studio-808-0200-03.png`、`readme/xtream-codec-studio-808-0200-04.png` | Studio 的 JT/T 808 位置汇报示例 |
| `readme/xtream-codec-studio-demo-01-01.png`、`readme/xtream-codec-studio-demo-01-02.png`、`readme/xtream-codec-studio-demo-01-03.png`、`readme/xtream-codec-studio-demo-01-04.png` | Studio 的自定义协议示例 |

### 工具截图和打赏码

下列来源由 hylexus 于 2026-11-21 确认。

| `docs/src/.vuepress/public/img/` 下的路径 | 来源及文档用途 |
| --- | --- |
| `ext/jt/jt808/quick-start/quick-start-app-network-cat-0200.png` | 从 App Store 安装的 NetworkCat 运行界面，用于演示以十六进制发送 JT/T 808 测试报文。画面中的第三方界面、图标和商标保留各自权利。 [NetworkCat 官方商店页面](https://apps.apple.com/cn/app/networkcat/id6503148471?mt=12) |
| `pay.png` | 两张用于项目打赏的收款二维码，显示在 README 和文档关于页。画面中的支付平台标志保留各自权利。 |

## JT/T 808 标准截图

hylexus 确认原有的五张 PNG 截自 JT/T 808 官方标准 PDF。位操作、`0x0200` 示例和内置注解文档已改用按项目示例编写的说明，并标注 JT/T 808—2019。五张截图已从站点资源移除。

本节仅说明该手工资源范围，不代替源码、内联图标、外链内容或所有第三方资源的审查。
