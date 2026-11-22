# 第三方许可材料

项目代码按根 [`LICENSE`](LICENSE) 中的 Apache-2.0 提供，文件或目录另有声明的除外。
[`licenses/`](licenses/) 保存适用的第三方许可和归属材料。来源证据及发布待办见
[`build-script/license/README.md`](build-script/license/README.md)。

项目状态、免责、商用与来源展示说明见[免责声明与第三方商业化](docs/src/disclaimer.md)。

- [`MPL-2.0.txt`](licenses/MPL-2.0.txt)：从 Symphonia 固定提交
  `b157bb58826a2035405702f481b6fdcbd922cbf3` 的根 `LICENSE` 原样提取。
  对应 ADPCM Java 派生文件，随模块归档附带实际源码。
- [`LGPL-2.1.txt`](licenses/LGPL-2.1.txt)：从 GNU 原站保存的 LGPL 2.1 全文，<https://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt>，
  SHA-256 为 `20e50fe7aae3e56378ebf0417d9de904f55a0e61e4df315333e632a4d3555d95`。
  jump3r 的上游 Maven POM 声明 LGPL-2.1-or-later。实际捆绑该库的 Boot 和 Shadow 应用附带此许可全文、同版本源码及重建工具。
  该工具也可替换未签名且无前置启动脚本的 Spring Boot JAR 中的嵌套库。
  源码说明、工具用法及四份 LAME 上游原文见 [`jump3r/NOTICE.txt`](licenses/jump3r/NOTICE.txt)。
- [`frontend/NOTICE.txt`](licenses/frontend/NOTICE.txt)：项目归属、双语许可摘要及完整声明链接。收集器测试核对其与根 NOTICE 的开头一致。
- [`frontend/open_source_licenses.txt`](licenses/frontend/open_source_licenses.txt)：少量 npm 包未附带许可全文时的共用补充材料，
  保留适用包名、比较版本及固定上游链接；构建按其中“适用包 / Packages”列出的名称和 MIT 声明采用，
  不绑定具体版本，不维护额外审查哈希。包自带的原始许可与声明继续自动收集，
  升级时依据上游许可和发行材料更新适用段落。
  mitt 的旧 README 提供 MIT 链接及作者归属，未标注版权年份。该段原文与其引用的
  完整 MIT 权限/免责条款在汇总中分开列明；后者是项目补充材料，并非上游独立的 LICENSE 文件。
- [`source/open_source_licenses.txt`](licenses/source/open_source_licenses.txt)：七份复制源码的许可与声明，
  包含四份 MIT、matrixy Apache 声明、sky Mulan 双语全文及 Micrometer NOTICE。
  原文按分节保留，许可头检查从中提取适用内容；ldming 镜像沿用 SmallChi MIT。
  该文件随 Java 归档分发，独立前端不附带。固定来源与证据限制见
  [`build-script/license/README.md`](build-script/license/README.md)，不维护额外审查 JSON 或逐文件哈希清单。
- 文档手工分发的 Font Awesome 6.7.1 字体，其原始完整许可位于
  `docs/src/.vuepress/public/webfonts/LICENSE.txt`，字体适用其中 SIL OFL 1.1 部分。
  三个字体与官方固定 npm 包的对应成员逐字节一致，沿用项目本地文件名。
  原许可与同目录双语 `README.txt` 均随字体目录复制，也纳入 docs 的前端清单；
  版本、来源及归档成员见字体目录的 [`README.txt`](docs/src/.vuepress/public/webfonts/README.txt)；
  构建检查 CSS 引用的字体和材料完整复制，不维护额外的固定审查哈希清单。
- 项目标志已独立重绘，SVG、PNG、站点图标及生成方式见
  [`assets/branding/README.md`](assets/branding/README.md)，按项目根许可提供。
  此记录不覆盖未替换的旧截图、背景或其他第三方图形。
- 文档首页背景和 debug 拖拽图标也已直接重绘，来源见
  [`assets/STATIC-ASSETS.md`](assets/STATIC-ASSETS.md)。hylexus 自行绘制的 draw.io 源图及对应六张
  PNG 按项目根许可提供。开发和项目网页截图的来源及待核对图片的查看方式见[静态资源记录](assets/STATIC-ASSETS.md)。
- [`jvm/open_source_licenses.txt`](licenses/jvm/open_source_licenses.txt)：Boot/Shadow 应用共用的补充许可与归属原文，
  文件内记录适用组件和上游来源；各节仅适用于实际包含相应组件的应用，不是全部依赖清单。
  维护时依据上游官方许可和发行材料更新相应段落，不维护逐文件源码索引、审查哈希或逐版本放行表。
  应用打包追加实际 EPL 依赖的源码下载地址；普通类库不附带本汇总或独立依赖源码。

前端生产构建用 `build-script/license/frontend-notices.mjs`，按实际输出模块和
已加载样式定位包版本，并纳入 Vite/Vue/React 插件注入的辅助代码及 Tailwind/UnoCSS
样式生成器的许可，保留完整 LICENSE、COPYING、NOTICE、COPYRIGHT 文本。
仅有空入口、且未在产物保留内容的包不会列入；例如 client-only 的客户端空文件
和 lodash-unified 的纯转导出入口。缺少全文会使构建失败；许可短名仅用于标识，不作为正文的替代。
生产产物根目录的 `THIRD-PARTY-FRONTEND-NOTICES.txt` 保留项目 Apache LICENSE、项目归属、
适用模板和显式静态资源材料，不附带与该前端无关的后端源码声明。完全相同的 npm 许可正文
只输出一次，每个包仍保留名称、版本、许可文件路径及正文编号；同为 MIT 等许可但版权或
条款不同的正文分别保留。后端复制整个 dist 目录时须保留此文件。

该汇总由共享生成器维护，标题、自动生成提示、收集范围、许可边界和分发要求为中文在前、
英文紧随的对照说明；许可、补充来源及模板许可使用中英标签，项目补充说明也提供对照。
包名、版本、路径、URL 和上游许可、版权及免责原文保持原样；项目说明的译文与上游原文分开。
更新时修改共享生成器或对应来源材料，再执行生产构建，不手工编辑 dist 中的汇总。

验证收集规则：`node --test build-script/license/frontend-notices.test.mjs`。
仍需独立核对手工复制的代码、图标、字体、图片和未通过打包器加载的 public 资源。

实际归档和前端材料的核对命令见
[`build-script/license/README.md`](build-script/license/README.md#本地与-ci-核对)。
八个前端及 docs 字体的本地/CI 共用入口为
`python3 -B build-script/license/check-materials.py`；默认按锁文件安装并重建后核对。
