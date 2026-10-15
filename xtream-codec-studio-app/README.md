# Xtream Codec Studio

Studio 是独立的报文编辑器与编解码工作区，应用和前端分别位于顶层的 `xtream-codec-studio-app` 与 `xtream-codec-studio-ui`。编解码依赖 core 的通用调试服务，工作区存储在 Studio 应用中，不依赖 JT/T 808 或 Spring Boot。后端使用 JDK 21 的 `HttpServer`，默认只监听 `127.0.0.1`。

## 构建与启动

发布应用使用包含 UI、Jackson、SQLite JDBC 与 Logback 的单文件 JAR，运行需要 Java 21，无需 Node.js 或 Gradle。在仓库根目录构建：

```bash
JAVA_HOME="$(mise where java)" ./gradlew :xtream-codec-studio-app:shadowJar \
  -Pxtream.frontend.build.xtream-codec-studio-ui.enabled=true

java -jar xtream-codec-studio-app/build/libs/xtream-codec-studio-app-0.11.0-all.jar
```

打开 [http://127.0.0.1:8765](http://127.0.0.1:8765)。产物名称中的版本来自根目录 `gradle.properties` 的 `projectVersion`。打包任务会检查必要的 UI 资源，缺少页面或脚本时明确失败。

日常 `build`、`assemble`、`check` 以及 `build-script/before-commit.sh` 不会自动构建 fat JAR 或执行其集成测试，普通 JAR、application 分发、常规测试和检查保持原有流程。需要单文件应用时手动运行 `shadowJar`；需要真实 `java -jar` 集成验证时手动运行 `fatJarTest`，该任务会先构建 fat JAR。完整发布检查：

```bash
JAVA_HOME="$(mise where java)" ./gradlew :xtream-codec-studio-app:check :xtream-codec-studio-app:fatJarTest \
  -Pxtream.frontend.build.xtream-codec-studio-ui.enabled=true \
  -Pxtream.backend.build.checkstyle.enabled=true
```

常规 Java 构建与测试不要求已生成 UI 资源，也不需要 Node.js 或 pnpm。首页、脚本、样式和 JSON Schema 资源的完整性由手动运行的 `fatJarTest` 检查。

开发时也可直接使用 Gradle application：

在仓库根目录运行：

```bash
JAVA_HOME="$(mise where java)" ./gradlew :xtream-codec-studio-app:run --args="8765" \
  -Pxtream.frontend.build.xtream-codec-studio-ui.enabled=true
```

打开 [http://127.0.0.1:8765](http://127.0.0.1:8765)。不传端口时也使用 `8765`。前端使用 pnpm、Vite、React、HeroUI 3；与其他前端模块统一使用 `xtream-codec-frontend-build-plugin` 和 `xtreamCodecFrontendBuild` 扩展。前端安装、测试、构建与产物复制自动接入 `processResources`，生成的页面资源位于 `src/main/resources/ui/`。构建需要 Node.js 22.12 或更高版本和 pnpm；发布的应用运行时不需要 Node。独立前端检查和产物构建：

```bash
cd xtream-codec-studio-ui
pnpm install --frozen-lockfile
pnpm test
pnpm run check
pnpm run build
```

前端构建产物在 UI 模块的 `dist/`。`xtream.frontend.build.xtream-codec-studio-ui.enabled` 在当前仓库配置中为 `false`，首次运行带页面的 Studio 或构建发布 JAR 时须显式开启；关闭时沿用已生成的页面资源，跳过前端安装、测试、构建和复制，没有页面资源也可执行普通 Java 构建与测试。前端开发时运行 `pnpm dev`，打开 [http://127.0.0.1:5173](http://127.0.0.1:5173)，Vite 自动代理 Java 后端并热更新页面。构建开关、代理地址和验收样例见 [工作台 README](../xtream-codec-studio-ui/README.md)。

## 部署配置与日志

系统属性优先于环境变量，未配置或值为空时使用默认值。原有位置参数仍支持 `java -jar app.jar 9000 your.allowed.package`，位置端口优先于系统属性及环境变量，后续参数为允许扫描的包名。

| 系统属性                               | 环境变量                               | 直接运行默认值                                           |
|----------------------------------------|----------------------------------------|----------------------------------------------------------|
| `xtream.studio.host`                   | `XTREAM_STUDIO_HOST`                   | `127.0.0.1`                                              |
| `xtream.studio.port`                   | `XTREAM_STUDIO_PORT`                   | `8765`，`0` 为自动分配端口                               |
| `xtream.studio.storage`                | `XTREAM_STUDIO_STORAGE`                | `file`，可选 `sqlite` 或自定义提供者                     |
| `xtream.studio.data-dir`               | `XTREAM_STUDIO_DATA_DIR`               | `~/.xtream-codec/studio`                                 |
| `xtream.studio.sqlite-path`            | `XTREAM_STUDIO_SQLITE_PATH`            | 数据目录下的 `workspace.db`                              |
| `xtream.studio.shutdown-grace-seconds` | `XTREAM_STUDIO_SHUTDOWN_GRACE_SECONDS` | `5`，`0` 为立即停止 HTTP 连接                            |
| `xtream.studio.log-level`              | `XTREAM_STUDIO_LOG_LEVEL`              | `INFO`，可设为 TRACE / DEBUG / INFO / WARN / ERROR / OFF |

端口必须在 0–65535 之间，HTTP 停机等待秒数必须为非负整数；无效配置或存储初始化失败会记录错误并以非零状态退出。正常终止时，HTTP 服务最多等待配置秒数结束正在处理的交换，再等待服务线程结束并释放工作区；未完成的保存请求可能失败，已提交内容保留。

```bash
XTREAM_STUDIO_HOST=0.0.0.0 XTREAM_STUDIO_PORT=9000 \
XTREAM_STUDIO_STORAGE=sqlite XTREAM_STUDIO_DATA_DIR=./studio-data \
XTREAM_STUDIO_LOG_LEVEL=DEBUG \
  java -jar xtream-codec-studio-app/build/libs/xtream-codec-studio-app-0.11.0-all.jar

java -Dxtream.studio.port=9000 -Dxtream.studio.log-level=WARN \
  -jar xtream-codec-studio-app/build/libs/xtream-codec-studio-app-0.11.0-all.jar
```

应用通过 SLF4J 使用 Logback，默认输出带时间、级别、线程及来源的 UTF-8 控制台日志，可直接通过 `docker logs` 查看。高级日志配置可使用 `-Dlogback.configurationFile=/path/to/logback.xml` 提供自己的 Logback 配置。JVM 参数使用 Java 原生 `JAVA_TOOL_OPTIONS`，例如 `JAVA_TOOL_OPTIONS="-Xms128m -Xmx512m"`；这些选项也可以直接放在 `java -jar` 前。

## Docker

### 多架构镜像构建与推送

在仓库根目录手动执行脚本：

```bash
JAVA_HOME="$(mise where java)" ./xtream-codec-studio-app/docker/build-image.sh
```

脚本与 Dashboard 的镜像构建流程一致：显式开启 Studio UI 构建和 Checkstyle，调用 Studio 模块的 `clean`、`check`、`shadowJar` 任务，再使用 Docker buildx 构建 `linux/amd64` 和 `linux/arm64` 镜像并推送到阿里云仓库。版本从根目录 `gradle.properties` 的 `projectVersion` 读取，同时推送以下两个标签：

- `registry.cn-hangzhou.aliyuncs.com/xtream-codec/xtream-codec-studio-app:latest`
- `registry.cn-hangzhou.aliyuncs.com/xtream-codec/xtream-codec-studio-app:<projectVersion>`

执行前需准备 JDK 21、Node.js 22.12+、pnpm，以及支持双架构构建的 Docker buildx builder，并通过 `docker login registry.cn-hangzhou.aliyuncs.com` 登录有推送权限的账号。脚本按自身位置解析目录，可从任意当前目录调用；任一步失败即停止。该脚本仅供手动执行，日常构建与提交前检查不会调用它。

### 本地构建与运行

先按上面的命令构建 fat JAR，再从仓库根目录构建本地镜像。构建上下文为 Studio 模块，`.dockerignore` 仅保留发布 JAR 与 Dockerfile：

```bash
docker build -f xtream-codec-studio-app/docker/Dockerfile \
  --build-arg JAR_FILE=build/libs/xtream-codec-studio-app-0.11.0-all.jar \
  -t xtream-codec-studio:0.11.0 xtream-codec-studio-app

docker run --name xtream-codec-studio \
  -p 127.0.0.1:8765:8765 \
  -v xtream-codec-studio-data:/data \
  -e XTREAM_STUDIO_STORAGE=sqlite \
  -e JAVA_TOOL_OPTIONS="-Xms128m -Xmx512m" \
  xtream-codec-studio:0.11.0
```

基础镜像使用 `m.daocloud.io/docker.io/library/eclipse-temurin:21-jre`，继承基础镜像默认用户，不额外创建或切换用户，直接启动 JVM 并接收容器停止信号。镜像默认设置 `XTREAM_STUDIO_HOST=0.0.0.0`、`XTREAM_STUDIO_PORT=8765` 和 `XTREAM_STUDIO_DATA_DIR=/data`，其余默认值与直接运行相同；可用 `-e` 覆盖。改变容器端口时也需同步修改 `-p` 的容器端口。命名卷保留已保存的工作区；若改为绑定宿主机目录，该目录须允许实际容器用户写入。

```bash
docker logs -f xtream-codec-studio
docker stop --time 15 xtream-codec-studio
docker start xtream-codec-studio
```

容器停止超时应大于配置的 HTTP 等待时间，并为存储关闭留出余量。工作区文件格式、存储配置及跨介质迁移见 [WORKSPACE.md](./WORKSPACE.md)。

## 实体类模式

选择“实体类”，在目录里搜索并选择已注册实体。默认提供一个协议无关的 `sample` 实体，包含 `sequence`（u16）和 `flags`（u8）。可指定实体版本，留空则使用 core 的全部版本。点击“载入初始值”得到 JSON 草稿；点击“编码”生成原始 HEX；把 HEX 输入解码框可反向得到 JSON 和字段 Trace。

若需要调试宿主项目实体，可在启动命令的端口之后传入一个或多个允许扫描的包名；或在自己的程序中构建 `DebugEntityCatalog` 显式注册实体。被扫描的类须已位于运行 classpath；单独传包名不会载入外部 JAR。HTTP 请求只能引用目录标识，不能按请求加载类。

## 自由组合模式

字段树支持新增、编辑、复制、删除、同级拖拽与上下移排序，以及递归 `struct` 子字段和 `array.item` 编辑。字段值可以直接在页面输入，数组可以新增和删除元素；数组元素结构中的子字段改名、复制、删除和新增会同步所有已有元素值。字段编辑器可设置位宽、大小端、位序和长度来源；复杂属性如条件、分支、派生表达式、填充与对齐及注册扩展可在“高级属性”中填写，也可切换到“完整字段 JSON”编辑。

整数类型统一为 `uN` / `iN`（N 为 1–64），如 `u16`、`i32`、`u3`，无需另填位宽；浮点数和原始位串仍填写 `bits`。早期 JSON 中的 `uint/int + bits` 需要迁移为对应名称。字段“其他属性 JSON”可填写 `versions: [2013, 2019]`；省略表示默认定义，同一标识的精确版本定义覆盖默认布局。页面顶部的目标协议版本也适用于自由组合模式，留空只使用默认定义，与 `schemaVersion` 无关。

侧栏提供位字段与嵌套集合、文本与字节序、自动生成与校验和及 808 0200 双向样例，点击示例在新标签打开。第一个样例的原始编码为 `b10702011234025678`。编码结果会放入解码框；解码使用同一份结构。可以导入或导出 `codec-schema.json` 分享结构定义，值与定义分离。页面支持明暗主题和九种主题色；标签、未完成 JSON 和最近结果自动备份到 IndexedDB，Ctrl/⌘+S 正式保存到后端。

表达式 AST 可直接用于 `derived.expression`、`when.expression` 与 `length.expression`。完整定义 JSON 的导入、导出会保留这些属性。默认应用不启用字符串表达式；导入含 `engine/source/bindings` 的定义后执行，会显示“未启用表达式引擎”的字段错误。宿主显式启用的方式见 [INTEGRATION.md](./INTEGRATION.md)，页面和 HTTP 请求均不能注册引擎。

## 结果与定位

运行后底部面板显示 JSON 值、原始 HEX、消费的 bit/byte 数、剩余输入和诊断。面板支持折叠、最大化、拖动或用方向键调整高度，随当前标签显示结果。点击 Trace 节点高亮对应字节及精确 bit；点击原始字节可定位相关节点。复制 HEX 只复制响应中的 `hex`，不添加协议帧边界。失败时，已经完成的节点与错误 bit 偏移仍可查看。

## API 与移植

工作区存储、配置与接口详见 [WORKSPACE.md](./WORKSPACE.md)。默认存磁盘，部署时可以切换 SQLite；保存结果快照和输入，清理浏览器数据后仍可从“我的报文”重新打开。

独立应用提供：

- `GET /api/v1/entities`：实体目录。
- `GET /api/v1/entities/{id}/template`：实体初始 JSON。
- `GET /api/v1/extensions`：宿主允许的扩展标识。
- `POST /api/v1/codec`：实体或动态编解码。

业务 DTO 与入口在 `xtream-codec-core` 的 `io.github.hylexus.xtream.codec.core.debugger` 包中，无需额外引入或发布调试服务模块。其他 HTTP 容器的调用方式见 [INTEGRATION.md](./INTEGRATION.md)；将来接入 808 Dashboard Starter 时可直接复用 core 中的调试入口与请求/响应合同。
