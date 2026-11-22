# 许可材料维护

根 `LICENSE` 为 Apache-2.0，第三方源码、模板及依赖保留各自适用的许可。本文集中说明维护范围、检查命令、固定来源及发布待办；材料检查通过不代表历史来源疑问已解决。

## 日常维护

普通第三方 JAR 依据上游 POM、官方许可、变更记录及发行材料管理，不要求逐文件复核内部源码或历史贡献授权。复制或改编到本仓库的源码，以及实际随应用重新分发的依赖，按各自范围保留材料。
Ordinary third-party JARs are maintained using upstream POMs, official licenses, release notes and distribution materials, without reviewing every internal source file or historical contribution. Copied or adapted source and dependencies actually redistributed with applications retain materials appropriate to their scope.

| 范围 | 材料与更新方式 |
| --- | --- |
| 普通、源码和 Javadoc JAR | 项目 LICENSE、NOTICE、源码归属声明及 `licenses/source/open_source_licenses.txt`；不复制独立依赖的许可清单或完整源码 |
| Boot/Shadow 应用 | Boot 保留原始嵌套 JAR，Shadow 保留、合并上游许可与声明；必要补充集中于 `licenses/jvm/open_source_licenses.txt`，各节仅适用于实际包含的组件，打包追加实际 EPL 版本的完整源码下载地址 |
| ADPCM 与 jump3r | ADPCM 随模块携带 MPL-2.0 和实际 Java 源码。捆绑 jump3r 的 Boot 和 Shadow 应用携带匹配源码、LGPL、上游声明和重建工具。该工具也支持替换符合条件的 Spring Boot JAR 内的嵌套库 |
| 八个前端 | `frontend-notices.mjs` 按实际输出模块及加载样式自动生成许可全文；少量缺失正文在 `licenses/frontend/open_source_licenses.txt` 按包名和 MIT 声明补充，不绑定精确版本，未知缺失许可仍使构建失败 |
| 手工分发资源 | Dashboard 保留 Next UI 模板 MIT，docs 字体保留官方 Font Awesome 原始许可及双语来源。图片的来源和预览方法见[静态资源记录](../../assets/STATIC-ASSETS.md) |

应用材料的集中组织参考 Spring Boot CLI 的 [open_source_licenses.txt](https://github.com/spring-projects/spring-boot/blob/9e8d20d7417af0220bd2780a4bcb51ce9e84722f/cli/spring-boot-cli/src/main/content/legal/open_source_licenses.txt)。升级依据上游许可和发行材料更新适用段落，不维护逐版本放行表或普通依赖的逐文件哈希；具体许可矛盾、修改、重定位或重新打包另行核对。
Application materials follow Spring Boot CLI's consolidated organization. Update applicable sections from upstream licenses and distribution materials on upgrades, without per-version approval tables or per-file hashes for ordinary dependencies; investigate specific conflicts, modifications, relocations or repackaging separately.

默认源码许可头独立维护于 `license-header`，ADPCM 和 sky 专用头集中于 `headers.txt` 的两个分节，MIT 等专用头从源码许可汇总提取，不能用项目统一头覆盖上游声明。`.gitattributes` 保留上游原文的换行和空白。许可内容索引见 [`THIRD-PARTY-MATERIALS.md`](../../THIRD-PARTY-MATERIALS.md)。

前端生成器同时纳入工具注入的辅助代码和生成样式。完整相同的正文只输出一次，每个包仍保留名称、版本、文件及正文引用；版权或条款不同的正文分别保留。空入口和完全移除的纯转导出不纳入。双语说明在生成器及来源材料维护，不手工修改 dist；后端复制静态资源时保留 `THIRD-PARTY-FRONTEND-NOTICES.txt`。

## 本地与 CI 核对

统一入口为 `check-materials.py`，使用 Python 3.9+ 标准库。完整前端流程还需 Node.js 22 和 pnpm 10；脚本根据自身位置定位仓库，不依赖调用目录。环境配置好后可省略 `mise exec --`。

```sh
# 默认：收集器测试、按锁文件安装、重建并核对全部八个前端
mise exec -- python3 -B build-script/license/check-materials.py

# 复用已安装依赖，仍重建全部八个前端
mise exec -- python3 -B build-script/license/check-materials.py --skip-install

# 仅核对八前端现有产物，不证明产物已包含最新源码
python3 -B build-script/license/check-materials.py --check-only

# 仅核对已构建的文档站点目录
python3 -B build-script/license/check-materials.py --docs-dist docs/src/.vuepress/dist

# 按需生成图片路径、引用页和哈希清单及可编辑数据预览
python3 -B build-script/license/check-materials.py --preview-assets
```

完整流程使用 `CI=true` 和 `pnpm install --frozen-lockfile`，不更新锁文件；安装可能刷新 node_modules，构建刷新 dist、VuePress 缓存及 Dashboard 的 `stats.html`。任何失败返回非零状态，日志位于 `build/reports/frontend-license-checks/`，失败时输出日志路径及最后 30 行。图片预览是独立模式，不安装依赖或重建前端，也不能与其他选项混用；输出及证据限制见[静态资源说明](../../assets/STATIC-ASSETS.md#待核实资源)。

前端核对覆盖项目 Apache LICENSE、项目归属、双语说明、补充正文及 Dashboard 模板。docs 还按 CSS 的 WOFF2 引用核对字体、CSS、原始 LICENSE 和双语 README 的仓库及站点副本，两份材料须完整出现在汇总中；不设置固定字体哈希门禁，也不重新下载字体包。字体版本、来源和归档成员见 [webfonts/README.txt](../../docs/src/.vuepress/public/webfonts/README.txt)。

文档部署按锁文件安装并构建，随后用 `--docs-dist` 核对实际上传的目录。校验也会拒绝包含可编辑 `xtream-codec.drawio` 的站点产物。 [文档部署配置](../../.github/workflows/deploy-docs.yml)

### 指定归档或前端汇总

先重新构建拟分发的归档，再用 `--jar` 或 `--frontend` 核对实际文件。`--jar-frontend JAR ENTRY DIST_NOTICE` 逐字节比对归档条目与对应构建的汇总文件，可以重复指定。这些选项只读取产物，可组合使用。示例：

```sh
mise exec -- ./gradlew \
  :xtream-codec-base:jar :xtream-codec-base:sourcesJar :xtream-codec-base:javaDocJar \
  :ext:jt:jt-1078-server-spring-boot-starter-reactive:jar \
  :quick-start:jt:jt-1078-server-quick-start-nonblocking:jar \
  :quick-start:jt:jt-1078-server-quick-start-nonblocking:bootJar \
  :xtream-codec-studio-app:shadowJar \
  -Pxtream.backend.build.debug-module-fatjar.enabled=true \
  -Pxtream.frontend.build.jt-1078-server-quick-start-ui.enabled=true \
  -Pxtream.frontend.build.xtream-codec-studio-ui.enabled=true --no-parallel

python3 -B build-script/license/check-materials.py --jar \
  xtream-codec-base/build/libs/*.jar \
  ext/jt/jt-1078-server-spring-boot-starter-reactive/build/libs/*.jar \
  quick-start/jt/jt-1078-server-quick-start-nonblocking/build/libs/*.jar \
  xtream-codec-studio-app/build/libs/*.jar \
  --jar-frontend quick-start/jt/jt-1078-server-quick-start-nonblocking/build/libs/jt-1078-server-quick-start-nonblocking.jar \
  BOOT-INF/classes/static/quickstart-ui/THIRD-PARTY-FRONTEND-NOTICES.txt \
  quick-start/jt/jt-1078-server-quick-start-ui/dist/THIRD-PARTY-FRONTEND-NOTICES.txt
```

`--jar` 和 `--frontend` 均接受多个路径。Windows PowerShell 应传入展开后的实际路径；目录有多个版本时，明确指定拟分发版本。核对覆盖项目和源码许可全文、ZIP 路径唯一性、应用补充及 EPL 地址、Jakarta 原声明、ADPCM 对应源码，以及 jump3r 匹配源码校验、声明和工具；普通库不要求携带独立依赖材料。

JAR 中的前端入口 `index.html` 须与 `THIRD-PARTY-FRONTEND-NOTICES.txt` 同目录，该检查也覆盖 Boot 嵌套的 JT808 Dashboard JAR。CI 会确认两份 JT808 Quick Start Boot JAR 的 Quick Start 和嵌套 Dashboard 入口存在，再核对其许可材料，并将 JT1078 Quick Start Boot 和 Studio Shadow JAR 中的汇总与对应 `dist` 文件逐字节比较。

### 工具测试与 CI

```sh
python3 -B build-script/license/test_check_materials.py
mise exec -- node --test build-script/license/frontend-notices.test.mjs
```

CI 显式指定该 Python 测试文件，不自动发现同目录的 `test_*.py`。测试覆盖原有材料缺失、损坏、分发边界、版本变化、字体与模板，以及统一入口的模式选择和错误退出。

`.github/workflows/license-materials.yml` 的 `materials` 任务运行测试、八前端完整流程及代表性 Java 归档核对，并尝试上传 `frontend-license-check-logs`。`dependency-policy` 仅在手动触发时运行全模块元数据政策并上传报告；日常 PR/push 不运行这个重复政策门禁。验证结果及日志仅对应当次源码、依赖和产物，不消除下文的来源待办。

## 依赖政策检查

| 命令 | 检查范围 |
| --- | --- |
| `:模块:checkLicenses`（复数） | 源码许可头，由 Forge Licenser 检查 |
| `:模块:generateLicenseReport` | 按实际运行时依赖的上游元数据生成报告 |
| `:模块:checkLicense`（单数）或 `:模块:enforceLicenses` | 按 `allowed-licenses.json` 检查元数据中的许可名称 |
| `:enforceLicenses` | 汇总所有 Java 子模块的元数据政策检查 |

```sh
mise exec -- ./gradlew :xtream-codec-base:generateLicenseReport --no-parallel
mise exec -- ./gradlew :enforceLicenses --continue --no-parallel
```

报告位于各模块 `build/reports/dependency-license/`。类库和 Boot/Shadow 应用打包均不隐式运行报告或依赖政策检查，也不附带依赖报告；这些工具通过上述命令显式执行。两种检查不混入源码头检查的结论。
Reports are written to each module's `build/reports/dependency-license/`. Neither library nor Boot/Shadow application packaging implicitly runs reports or dependency policy checks or includes dependency reports; use the explicit commands above. These checks are separate from source-header validation.

## 政策与结果

政策按 Apache-2.0、MIT、BSD、Bouncy Castle、EPL-2.0 等上游许可名称及别名匹配，不为普通依赖建立逐版本人工许可白名单。GPL/LGPL 不在通用允许类别中；具体例外仍须结合适用条款处理。
The policy matches upstream license names and aliases for Apache-2.0, MIT, BSD, Bouncy Castle and EPL-2.0, without a manually reviewed per-version allowlist for ordinary dependencies. GPL/LGPL are not generally allowed categories; individual cases still require consideration of their terms.

报告插件按元数据中任一匹配名称判定通过，可能包含父 POM 许可；它不完整解析 SPDX AND/OR，也不证明上游的全部历史授权。通过表示匹配本项目政策，失败表示需要核对元数据或具体许可选项，均不直接构成法律结论。
The report plugin accepts any matching metadata license name and may include parent-POM licenses. It does not fully interpret SPDX AND/OR expressions or establish every historical upstream authorization. Passing means matching this project's policy; failing calls for checking metadata or specific license choices. Neither is a legal determination.

未匹配项见 `dependencies-without-allowed-license.json`，完整输入见 `project-licenses-for-check-license-task.json`。按坐标归并阅读多个别名，不把报告行数当作依赖数量。升级时通常只需查看上游许可、变更记录和分发材料；发现具体矛盾时再进一步调查。
Unmatched entries are in `dependencies-without-allowed-license.json`; the complete input is in `project-licenses-for-check-license-task.json`. Group aliases by coordinate rather than counting report rows as dependencies. Upgrades normally require checking upstream licenses, release notes and distribution materials; specific conflicts may warrant further investigation.

## 源码来源与证据限制

### 已核实的 Apache 来源

核对使用已获取的固定版本 Maven 源码归档，版本仅作为比较基准，**不代表最初复制版本**。文件头版权范围按该比较版本保留。来源判断以具体代码对应关系和适用许可为依据；无法定位首次复制提交的情况与具体授权矛盾分别记录。

上游作者归属及比较版本见根 `THIRD-PARTY-SOURCE-NOTICES.txt`。其中的名单说明上游类的作者信息，不表示这些作者参与了当前项目开发，也不能用于确定具体方法的个人作者。派生文件头保留上游版权和已有作者标签；`Modifications Copyright 2024-present the xtream-codec authors.` 仅声明项目修改部分的版权。

根 `NOTICE` 和 `THIRD-PARTY-SOURCE-NOTICES.txt` 中由本项目编写的说明采用中英对照，覆盖适配范围、作者归属、比较版本限制、材料位置和待核实事项。逐文件对应关系使用中英字段标签，路径、版本和作者姓名保留原值。上游版权行、Spring Framework/Boot 转载声明和 G711 源码头引用完整保留原文；项目说明不替代原始许可条款。

免费开源、免责声明及自愿注明来源的边界见[完整说明](../../docs/src/disclaimer.md)；必需版权、许可和归属仍按适用条款保留。

- Spring Framework `6.1.8`：`XtreamInternalStringUtils` ← `StringUtils`；`XtreamVersion` ← `SpringVersion`；`XtreamMethodParameter` ← `MethodParameter`。
- 同一版本：reactive 模块的 `XtreamExchange`、`XtreamExchangeDecorator` ← `ServerWebExchange`；`XtreamFilter`、`XtreamFilterChain`、`XtreamSessionManager`、`XtreamHandler` ← 对应 Web 类型；`XtreamHandlerMethod` ← `HandlerMethod`。
- 同一版本：`XtreamHandlerAdapter`、`XtreamHandlerMapping`、`XtreamHandlerResult`、`XtreamHandlerResultHandler`、`XtreamDispatchExceptionHandler`、`XtreamRequestExceptionHandler` ← 对应 reactive/server 类型；`DefaultXtreamFilterChain`、`FilteringXtreamHandler`、`ExceptionHandlingXtreamHandler`、`DispatcherXtreamHandler` ← 对应 Web 实现。
- Spring Boot `3.3.0`：808 Dashboard 的 `XtreamMappingDescriptionProvider`、`DispatcherHandlerXtreamMappingDescriptionProvider` ← actuator 对应类型。
- Reactor `3.6.6`：`XtreamReactorThreadFactory` ← `ReactorThreadFactory`。
- 上游仓库：<https://github.com/spring-projects/spring-framework>、<https://github.com/spring-projects/spring-boot>、<https://github.com/reactor/reactor-core>。

Spring Boot Admin：比较依据为源码注释中的 `1433ca5b8343247075ff775d558c9a82341e2ac6`、`83db63e82e916357d36b1e6b4d552e1b6506ecc9` 两个固定提交。对应上游类型包括 `HttpHeaderFilter`、`InstanceWebProxy`、`InstanceWebClient`、`InstanceExchangeFilterFunction`、`IntervalCheck` 及 reactive/servlet 的 `InstancesProxyController`；`base/web/proxy/` 中五个类和 `XtreamIntervalChecker` 保留 `Copyright 2014-2023 the original author or authors.`。所查快照包含 Apache LICENSE，未发现独立 NOTICE。上游类作者、逐文件对应关系及限制在根来源声明中记录，固定来源见下表，不维护逐文件哈希清单。

### 已核实的 MIT 源码来源

以下列出对应的上游固定源码、许可证及版权信息。

| 当前文件 | 固定比较来源 | 许可证版权原文 |
| --- | --- | --- |
| `Message8105CodecSample` | SmallChi/JT808 `e79d18c3b465f5c450a1d6ba0edc6d9d51c7b13b` 的 `JT808_0x8105.cs`，核对命令参数分隔处理 | `Copyright (c) 2018 SmallChi(Koike)` |
| `DefaultSpsDecoder` | SmallChi/JT1078 `c4931a8ec37678fb5ecedcc749ffe3c9f4c890fe` 的 `EXPGolombReader.cs`，核对 SPS、裁剪和宽高比处理 | `Copyright (c) 2019 SmallChi(Koike)` |
| `DefaultSpsDecoder` | EyerH264Decoder `59e0a396f0b9c0a4a2c034ba6d44fae22297e979` 的 `NaluSPS.cpp`，对应 SPS 参数解析 | `Copyright (c) 2021 Redknot` |
| G711 三个实现类 | mazcpnt/maz-g711 `8b6736f55f2cf832fbaaf3dfc9796d400d63880c` 的 `maz_cpnt_g711.c`，核对 A-law/μ-law 解码步骤 | `Copyright (c) 2021 mz8023yt` |

G711 原源码另标注作者 `wangbing`，与 LICENSE 的版权名分别原样记录；这些信息用于上游归属，不据此认定为同一人或列为当前项目作者。
四份 MIT 全文与其他源码材料集中于 `licenses/source/open_source_licenses.txt`；固定来源见下表。
分发副本仅统一 LF 换行（G711 原文为 CRLF），不改写条款。Java 头保留 MIT 全文后，明确项目修改部分沿用 Apache-2.0。
固定比较快照用于核对代码和许可，不能单独证明最初引入版本；具体未解决事项见“发布待办”。

### 固定比较来源

下表的链接指向固定提交或标签；文件位置相对于所链接的仓库根目录。许可及声明原文集中在源码汇总中，类与适配范围见根来源声明；比较快照不能确定历史首次引入版本。
The links below identify fixed commits or tags; file locations are relative to each linked repository root. Original licenses and notices are consolidated in the source summary, with classes and adaptation scope in the root source notices; comparison snapshots do not establish the first historical import.

| 来源 / Source | 固定快照 / Fixed snapshot | 比较文件 / Compared files |
| --- | --- | --- |
| Spring Boot Admin | https://github.com/codecentric/spring-boot-admin/blob/1433ca5b8343247075ff775d558c9a82341e2ac6 | `LICENSE.txt`<br>`spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/HttpHeaderFilter.java`<br>`spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/InstanceWebProxy.java`<br>`spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/client/InstanceWebClient.java`<br>`spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/client/InstanceExchangeFilterFunction.java`<br>`spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/services/IntervalCheck.java` |
| Spring Boot Admin 控制器 / controllers | https://github.com/codecentric/spring-boot-admin/blob/83db63e82e916357d36b1e6b4d552e1b6506ecc9 | `spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/{reactive,servlet}/InstancesProxyController.java` |
| SmallChi/JT808 | https://github.com/SmallChi/JT808/blob/e79d18c3b465f5c450a1d6ba0edc6d9d51c7b13b | `LICENSE`<br>`src/JT808.Protocol/MessageBody/JT808_0x8105.cs` |
| SmallChi/JT1078 | https://github.com/SmallChi/JT1078/blob/c4931a8ec37678fb5ecedcc749ffe3c9f4c890fe | `LICENSE`<br>`src/JT1078.Protocol/MessagePack/EXPGolombReader.cs` |
| redknotmiaoyuqiao/EyerH264Decoder | https://github.com/redknotmiaoyuqiao/EyerH264Decoder/blob/59e0a396f0b9c0a4a2c034ba6d44fae22297e979 | `LICENSE`<br>`EyerH264Decoder/NaluPPS.cpp`<br>`EyerH264Decoder/NaluSPS.cpp` |
| mazcpnt/maz-g711 | https://gitee.com/mazcpnt/maz-g711/blob/8b6736f55f2cf832fbaaf3dfc9796d400d63880c | `LICENSE`<br>`maz_cpnt_g711.c` |
| matrixy/jtt1078-video-server | https://gitee.com/matrixy/jtt1078-video-server/blob/7cf571bbd89d988ff1081e790775a538aa5abc0c | `LICENSE`<br>`src/main/java/cn/org/hentai/jtt1078/codec/MP3Encoder.java`<br>`src/main/java/cn/org/hentai/jtt1078/codec/ADPCMCodec.java`<br>`src/main/java/cn/org/hentai/jtt1078/flv/FlvEncoder.java`<br>`src/main/java/cn/org/hentai/jtt1078/flv/FlvAudioTagEncoder.java` |
| ldming/JT1078 (SmallChi mirror) | https://gitee.com/ldming/JT1078/blob/3604422593af7d8a343333f52fc93d2092f18261 | `LICENSE`<br>`src/JT1078.Flv/FlvEncoder.cs`<br>`src/JT1078.Flv/MessagePack/FlvMessagePackWriter_Flv.cs`<br>`src/JT1078.Flv/Metadata/AVCDecoderConfigurationRecord.cs` |
| sky/jt1078 (hui_hui_zhou/open-source-repository) | https://gitee.com/hui_hui_zhou/open-source-repository/blob/37ebeb29bfff29a13848a6e4d753a6ae85f10e3a | `LICENSE`<br>`src/main/java/com/huai/jt1078/handler/MessageDecoderHandler.java`<br>`README.md` |
| sky 后续 README / later README | https://gitee.com/hui_hui_zhou/open-source-repository/blob/75012fb72ac5774017781e59382b2d1cae451ac2/README.md | `README.md`；2026 年说明的比较 / comparison of the 2026 wording |
| Micrometer | https://github.com/micrometer-metrics/micrometer/blob/v1.13.0 | `micrometer-core/src/main/java/io/micrometer/core/instrument/binder/jvm/JvmThreadMetrics.java`<br>`NOTICE` |

### jt-framework 工具类

以下工具类复用自 hylexus 在 jt-framework 项目中编写的代码：
`BcdOps`、`FormatUtils`、`XtreamBitOperator`、`ByteArrayContainer`、`BytesContainer`。
相关源码保留来源注释，按本项目 Apache-2.0 许可提供。

### Symphonia 核实依据

已核对源码注释指向的固定提交中 `codec_ima.rs` 的 MPL-2.0 头、两个表、状态结构、
前导读取及 `expand_nibble`。具体代码对应关系和历史 Microchip 引用见
[音频来源记录](#音频来源记录)。

- 比较归档：<https://codeload.github.com/pdeljanov/Symphonia/zip/b157bb58826a2035405702f481b6fdcbd922cbf3>。
- 归档 SHA-256：`e5ac60cbfd25ba7cbd0ec736e896c77aefb880dbc24f35e7e81f578706d51aaa`。
- `codec_ima.rs` SHA-256：`33eec84903c837982020139f5c976360faddb69b7d6d21f115db69efeb4bc562`。
- 原根 LICENSE SHA-256：`c76f740d1521b9bed9ca7a04ad526c310493c62621b1341d623b431736533b30`，保存为 `licenses/MPL-2.0.txt`。
- Java 的 predictor 转换和半字节顺序与上游有差异；修改及来源说明随 `THIRD-PARTY-SOURCE-NOTICES.txt` 分发。

### 其他已核实来源

- matrixy：MP3、FLV、ADPCM 的比较依据为固定提交 `7cf571bbd89d988ff1081e790775a538aa5abc0c` 及其根 LICENSE。对应文件保留原版权 `Copyright [2019] [matrixy]`，包括原文中的方括号；所查完整树未发现独立 NOTICE。ADPCM 同时适用其 Symphonia 来源部分的 MPL 义务。
- ldming：`3604422593af7d8a343333f52fc93d2092f18261` 明确为 SmallChi 镜像，MIT 与已保存的 SmallChi JT1078 原文字节一致。已匹配的 `DefaultFlvEncoder` 实现保留该 MIT。`H264ToFlvSubscriber` 和 `H264ToFlvJt1078ChannelCollector` 的注释包含相关项目的技术参考链接；现有比较记录未建立这两个类与所链接项目之间具体的源码改编对应关系。
- sky：`37ebeb29bfff29a13848a6e4d753a6ae85f10e3a`（2024）的 LICENSE、README 和 MessageDecoderHandler 已核对，解码字段及条件处理可对应；该快照 README 无非商用条件，保留其 Mulan 双语全文。`75012fb72ac5774017781e59382b2d1cae451ac2`（2026）的 README 增加“商业使用联系作者”，所比较的解码器未变。解码器许可依据为 2024 固定快照，适用范围限于已匹配部分；固定文件位置见上表，后续 README 比较不改变上述依据。AAC 的处理思路参考见下文。
- Micrometer：Dashboard 中标注复制来源的方法对应 1.13.0 的 `JvmThreadMetrics#getThreadStateCount`，保留 `Copyright 2017 VMware, Inc.`、项目修改版权及完整上游 NOTICE。Jon Schneider、Johnny Lim 记录为上游比较类作者。
- jump3r：使用未修改的 Maven 依赖，依据上游 POM 的 LGPL-2.1-or-later 声明及发行材料维护。分发说明和保留的 LAME 上游原文见 [`NOTICE.txt`](../../licenses/jump3r/NOTICE.txt)，完整源码保留原始声明；日常升级无需逐文件复核源码或许可头。

`BitStreamReader` 等文件保留网页、视频和标准的技术参考链接。技术事实与具体代码或文字分别核对；未建立源码对应关系的链接仅记录为技术参考。

字体来源见上文。标志、首页背景、拖拽图标、截图及流程图的来源见 [`assets/STATIC-ASSETS.md`](../../assets/STATIC-ASSETS.md) 和[标志维护说明](../../assets/branding/README.md)。当前分发的 PNG 清单可按需生成。

## 音频来源记录

源码比较日期：2026-10-24；AAC 参考范围说明更新于 2026-11-14。本文记录 JT1078 音频源码的比较依据、对应关系、技术方案参考及待补充证据，范围限于所列文件和固定版本。源码或来源材料变更后，应更新对应记录。

### ADPCM 与 Microchip

核对文件为 `AdpcmImaJt1078AudioCodec.java`。历史版本中的未调用 Microchip 移植方法、逐字 C 示例和两行重复的 `0xff` 表注释已移除；当前源码保留 Microchip 的技术参考链接。

比较来源：

- Microchip AN643，1997，DS00643B，<https://ww1.microchip.com/downloads/en/AppNotes/00643b.pdf>。
- PDF SHA-256：`06ac98fdbb54203ce5451e8461af51931e6697c23ffccf27953e3f18625b9d4d`。
- Appendix B 为其 C 解码函数，用于与当前实现比较；PDF 和 C 示例不在当前分发材料中。
- Symphonia 固定提交 `b157bb58826a2035405702f481b6fdcbd922cbf3` 的 [codec_ima.rs](https://github.com/pdeljanov/Symphonia/blob/b157bb58826a2035405702f481b6fdcbd922cbf3/symphonia-codec-adpcm/src/codec_ima.rs)，MPL-2.0。
- Symphonia 源文件 SHA-256：`33eec84903c837982020139f5c976360faddb69b7d6d21f115db69efeb4bc562`；提交归档哈希见 [Symphonia 核实依据](#symphonia-核实依据)。

| 实现部分 | 已记录的对应关系及差异 |
| --- | --- |
| 16 项 index 表、89 项 step 表 | 与上述 Symphonia 文件逐项数值比较一致；Microchip 文献也列有同一算法表，表值相同本身不能识别具体复制来源 |
| `AdpcmImaBlockStatus`、`readPreamble` | 对应 Symphonia 状态和前导读取，Java 使用 Netty 缓冲区 |
| `expandNibble` 的符号、delta、diff、predictor 和 index 更新 | 对应 Symphonia 的表达式和步骤；Microchip 示例采用逐位累加 diff 的不同写法 |
| `doDecodeAsPcm` | 适配为 Java/Netty；半字节输出顺序、首样本处理和 predictor 转换与 Symphonia 有差异 |
| index 范围处理、状态打印、资源释放 | Java/Netty 适配与通用边界处理；未找到可独立归因于 Microchip C 示例的剩余完整方法 |

适用的 MPL 材料、对应 Java 源码和 matrixy 声明随当前 JAR 分发。对照未在当前实现中发现可独立归因于 Microchip C 示例的代码块。旧 C 示例及其 Java 移植的使用依据仍待核实，此记录适用于含有这些内容的历史版本。

### AAC 的 ADTS 头处理思路参考

`AacJt1078AudioCodec` 中识别并跳过 7 字节 ADTS 头的处理思路参考 sky；注释中的链接用于介绍 sky 的 JT1078 项目。该来源说明限于处理思路，未建立该类具体源码与上游文件的复制或改编对应关系。消息解码器的 Mulan 许可证据不据此扩展到该类。

历史源码查询记录：所查 master 分支的 2024（`37ebeb29bfff29a13848a6e4d753a6ae85f10e3a`）和 2026（`75012fb72ac5774017781e59382b2d1cae451ac2`）固定快照均未找到对应 AAC 实现；解码器和 README 的固定来源见 [固定比较来源](#固定比较来源)。另两个公开分支的 Gitee API 查询结果如下；这些记录仅描述查询范围和当时返回的内容，不据此判断其他历史版本是否存在对应实现，也不证明发生了源码复制。

| 分支 | 固定提交 | 查询结果 |
| --- | --- | --- |
| `first` | `952ae2d8411af7c500bc0c08a667f9ae057f8a3c` | 递归 Git tree 查询返回空树，未取得 AAC 对应源码 |
| `rust-1078` | `82d43697c9f5b297156f2dd6f374374734d75570` | 递归 Git tree 查询返回空树，未取得 AAC 对应源码 |

API 查询地址：

- <https://gitee.com/api/v5/repos/hui_hui_zhou/open-source-repository/branches>
- <https://gitee.com/api/v5/repos/hui_hui_zhou/open-source-repository/git/trees/952ae2d8411af7c500bc0c08a667f9ae057f8a3c?recursive=1>
- <https://gitee.com/api/v5/repos/hui_hui_zhou/open-source-repository/git/trees/82d43697c9f5b297156f2dd6f374374734d75570?recursive=1>

该项按技术方案参考记录，不因处理思路或项目介绍链接单独要求取得覆盖整个类的源码授权。后续若引入或识别出具体源码复用，应按对应源码及适用许可补充材料。

## 发布待办

以下列出尚需补充的来源证据及持续维护要求。NOTICE 和依赖报告用于记录与分发许可材料，不构成对缺失授权的补充；待核实事项不代表已确认侵权。依赖和资源数量以所列核对记录为准，发布前应按实际发行内容更新。

| 范围 | 已知事实 | 发布前需要处理 |
| --- | --- | --- |
| jump3r FFT 的历史权利说明 | jump3r 1.0.5 的 `FFT.java` 保留 LAME `fft.c` 的历史 GPL、机构权利及专利说明；Debian 对 C 文件单独记录 `GPL-1+` | 发布捆绑 jump3r 的发行物前，向上游核实 Java 移植中该文件适用的许可与再分发依据，或改用许可已明确的实现 |
| 前端复制内容和发布流程 | 八个构建已生成 npm 许可全文；模板 MIT 已保留 | 发布时保留汇总文件；补充非打包器资源的来源证据，核对锁文件、升级后缺失的许可及具体发行物；`private: true` 不免除这些要求 |
| 依赖检查策略 | 普通依赖根据上游元数据和发行材料按许可类别检查，无需逐文件审查；jump3r 的具体疑问保留 | 入口和规则见上文依赖政策检查；常规升级检查上游许可变化，具体矛盾另行调查 |

`jump3r` 的 Maven POM 声明 LGPL-2.1-or-later。1.0.5 源码 JAR 中 `de/sciss/jump3r/mp3/FFT.java` 的文件头保留 Ron Mayer 和 Takehiro Tominaga 的版权行，并称相关例程源于一个曾按 GPL 发布的实现。LAME 3.98.4 和后续固定提交都保留相同的历史说明。Debian 将 `libmp3lame/fft.c` 单列为 `GPL-1+`，并注明原文未明确 GPL 版本。该条目是下游对 C 文件的记录，Java 移植文件的适用许可仍待上游确认。 [jump3r 1.0.5 构件](https://repo.maven.apache.org/maven2/de/sciss/jump3r/1.0.5/) · [LAME 固定源码](https://github.com/lameproject/lame/blob/7c71c623b6f3c9de6aa1cbc55ca5e4520fe504d4/libmp3lame/fft.c#L1-L35) · [Debian 3.100-6 版权记录](https://metadata.ftp-master.debian.org/changelogs/main/l/lame/lame_3.100-6_copyright)

实际捆绑 jump3r 的 Boot 和 Shadow 应用在 `META-INF/third-party-sources/jump3r/` 自动附带同版本源码、声明、构件清单和重建工具，并保留 LGPL 全文。项目归档校验使用清单中实际分发的坐标及构建生成的校验值核对源码版本与文件完整性。升级时依据上游许可和发行材料更新。重建及在未签名、无前置启动脚本的 Spring Boot JAR 中替换嵌套库的命令见 [jump3r NOTICE](../../licenses/jump3r/NOTICE.txt)。
Boot and Shadow applications bundling jump3r automatically include matching sources, notices, an artifact manifest, and the rebuild tool under `META-INF/third-party-sources/jump3r/`, together with the full LGPL text. This project's archive checks use the distributed coordinates and build-generated checksums in the manifest to verify source versions and file integrity. Maintain upgrades using upstream licenses and distribution materials. See the jump3r NOTICE for rebuilding and replacing the nested library in an unsigned Spring Boot JAR without a prepended launch script.
