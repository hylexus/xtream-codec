---
title: Studio 启动与部署
---

# Studio 启动与部署

Xtream Codec Studio 是独立的报文编辑器与编解码工作区，支持配置 DSL、填写编码值、编码和解码、查看 Trace，以及保存报文。它不依赖 Spring Boot 或 JT/T 808 应用，启动后即可在浏览器中使用。

本文先介绍单文件 JAR，再介绍 Docker 部署和前端开发。[界面预览](#界面预览)展示结构编辑、DSL JSON 与运行结果；启动成功后，可以继续[从协议表写出第一份 DSL](./quick-start.md)。

## 环境要求

| 使用方式                 | 需要准备                                                        |
|--------------------------|-----------------------------------------------------------------|
| 运行已构建的单文件 JAR   | Java 21                                                         |
| 从源码构建带页面的应用   | JDK 21、Node.js 22.12+、pnpm；Gradle 使用仓库自带的 `./gradlew` |
| 运行已构建的 Docker 镜像 | Docker                                                          |
| 从源码构建 Docker 镜像   | 源码构建环境和 Docker                                           |

Studio 前端的 `package.json` 声明 `pnpm@10.33.0`，构建时使用该版本。Java 的 `JAVA_HOME` 和 `PATH` 应指向 JDK 21；使用仓库的 mise 环境时，可在 Gradle 命令前加上 `JAVA_HOME="$(mise where java)"`。

以下源码构建和 Docker 构建命令均在**仓库根目录**执行。示例版本为 `0.11.0`，实际版本以根目录 `gradle.properties` 的 `projectVersion` 为准。

## 构建并运行单文件 JAR

### 构建包含页面的应用

```bash
./gradlew :xtream-codec-studio-app:shadowJar \
  -Pxtream.frontend.build.xtream-codec-studio-ui.enabled=true
```

该命令会安装前端依赖、运行前端测试并构建页面，然后把 UI 和运行依赖一起打入 JAR。产物为：

```text
xtream-codec-studio-app/build/libs/xtream-codec-studio-app-0.11.0-all.jar
```

前端构建开关在仓库中默认为 `false`，首次构建或更新页面后，应显式传入上述 `-P` 参数。`shadowJar` 会检查页面、脚本和 JSON Schema 资源是否齐全。

普通 `build`、`assemble` 或 `check` 不会自动生成这个单文件应用，部署时使用带 `-all` 后缀的 JAR。

### 启动和访问

```bash
java -jar xtream-codec-studio-app/build/libs/xtream-codec-studio-app-0.11.0-all.jar
```

打开 [http://127.0.0.1:8765](http://127.0.0.1:8765)。Java 服务同时提供页面和 API，运行时只需 Java 21，无需 Node.js、pnpm 或 Gradle。可以把这个 JAR 复制到另一台已安装 Java 21 的机器，按实际文件路径启动。

默认只监听本机。部署到服务器并让其他机器访问时，指定监听地址和持久化目录，例如：

```bash
XTREAM_STUDIO_HOST=0.0.0.0 \
XTREAM_STUDIO_PORT=9000 \
XTREAM_STUDIO_DATA_DIR=./studio-data \
  java -jar xtream-codec-studio-app/build/libs/xtream-codec-studio-app-0.11.0-all.jar
```

访问 `http://服务器地址:9000`，并保证服务器网络允许访问该端口。`0.0.0.0` 表示监听所有网卡，浏览器应填写实际 IP 或域名。当前 Studio 没有用户和报文归属概念，访问同一实例的人共用服务器工作区。

在前台运行时按 `Ctrl+C` 停止。应用会先等待正在处理的 HTTP 请求，再关闭工作区存储；已正式保存的报文会在下次启动时读取。

## 配置项

配置通过 Java 系统属性或环境变量传入，**系统属性优先于环境变量**，未配置或配置为空时使用默认值。`-D` 参数放在 `-jar` 之前，例如：

```bash
java -Dxtream.studio.port=9000 \
  -Dxtream.studio.storage=sqlite \
  -Dxtream.studio.data-dir=./studio-data \
  -jar xtream-codec-studio-app/build/libs/xtream-codec-studio-app-0.11.0-all.jar
```

| 系统属性                               | 环境变量                               | 直接运行 JAR 时的默认值及说明                                              |
|----------------------------------------|----------------------------------------|----------------------------------------------------------------------------|
| `xtream.studio.host`                   | `XTREAM_STUDIO_HOST`                   | `127.0.0.1`；允许远程访问时设为 `0.0.0.0` 或指定网卡地址                   |
| `xtream.studio.port`                   | `XTREAM_STUDIO_PORT`                   | `8765`；范围为 0–65535，`0` 表示自动分配端口，实际地址见启动日志           |
| `xtream.studio.storage`                | `XTREAM_STUDIO_STORAGE`                | `file`；内置存储可选 `file` 或 `sqlite`                                    |
| `xtream.studio.data-dir`               | `XTREAM_STUDIO_DATA_DIR`               | `~/.xtream-codec/studio`；默认文件存储目录，也是默认 SQLite 文件的所在目录 |
| `xtream.studio.sqlite-path`            | `XTREAM_STUDIO_SQLITE_PATH`            | 数据目录下的 `workspace.db`；仅在使用 SQLite 时生效                        |
| `xtream.studio.shutdown-grace-seconds` | `XTREAM_STUDIO_SHUTDOWN_GRACE_SECONDS` | `5`；HTTP 停机等待秒数，非负整数，`0` 为立即停止 HTTP 连接                 |
| `xtream.studio.log-level`              | `XTREAM_STUDIO_LOG_LEVEL`              | `INFO`；可选 `TRACE`、`DEBUG`、`INFO`、`WARN`、`ERROR`、`OFF`              |

应用也兼容把端口作为第一个位置参数，例如 `java -jar app.jar 9000`；位置端口的优先级高于系统属性和环境变量。选择一种方式配置端口即可。

相对数据路径以进程的当前工作目录为基准；服务器部署时可以改用固定的绝对路径。默认目录中的 `~` 表示运行 Java 进程的用户主目录。

日志默认输出到控制台。需要调整 JVM 内存时，可设置 `JAVA_TOOL_OPTIONS="-Xms128m -Xmx512m"`；需要自定义 Logback 配置时，使用 `-Dlogback.configurationFile=/path/to/logback.xml`。

## 工作区持久化与备份

### 选择文件或 SQLite

默认文件存储把报文和目录保存到 `~/.xtream-codec/studio`。同一个文件存储目录只允许一个应用实例使用；同时启动多个实例时，为它们分别设置端口和数据目录。

使用 SQLite 时，驱动已包含在单文件 JAR 中：

```bash
XTREAM_STUDIO_STORAGE=sqlite \
XTREAM_STUDIO_DATA_DIR=./studio-data \
  java -jar xtream-codec-studio-app/build/libs/xtream-codec-studio-app-0.11.0-all.jar
```

此时数据库为 `./studio-data/workspace.db`。也可用 `XTREAM_STUDIO_SQLITE_PATH` 单独指定文件位置。存储初始化失败会终止启动并记录原因。

浏览器的自动草稿备份保存在 IndexedDB；点击“保存”或按 `Ctrl/⌘+S` 才会正式写入服务器。清理浏览器站点数据会清除本地恢复副本，服务器中已保存的报文仍保留。更换访问域名或端口后，本地草稿不会自动迁移到新站点。

### 备份和更换存储

文档工具栏的菜单提供：

- **导出完整文档**：导出当前报文，包含尚未保存的编辑内容和最近运行快照。
- **导出服务器工作区**：导出服务器中正式保存的所有目录和报文。
- **导入文档 / 工作区**：导入上述文件；工作区备份放入新目录，保留已有内容。

切换 `file` / `sqlite` 或搬迁到另一实例时，先导出服务器工作区，再使用新配置重启，在新工作区导入。只修改存储配置不会自动迁移原数据；导入会创建新的报文身份，创建时间和更新时间从本次导入开始记录。

如果选择直接备份存储文件，应先停止应用，再复制整个文件存储目录或实际 SQLite 数据库文件；运行中的备份和跨介质迁移使用工作区导出。

## Docker 部署

### 构建本地镜像

先执行前面的[单文件 JAR 构建](#构建包含页面的应用)，再构建镜像：

```bash
docker build -f xtream-codec-studio-app/docker/Dockerfile \
  --build-arg JAR_FILE=build/libs/xtream-codec-studio-app-0.11.0-all.jar \
  -t xtream-codec-studio:0.11.0 xtream-codec-studio-app
```

构建上下文是 `xtream-codec-studio-app`，所以 `JAR_FILE` 相对于该目录填写。Dockerfile 使用 Java 21 JRE 基础镜像，复制已构建的 JAR。

### 启动并挂载数据卷

```bash
docker run -d --name xtream-codec-studio \
  --restart unless-stopped \
  -p 127.0.0.1:8765:8765 \
  -v xtream-codec-studio-data:/data \
  -e XTREAM_STUDIO_STORAGE=sqlite \
  -e JAVA_TOOL_OPTIONS="-Xms128m -Xmx512m" \
  xtream-codec-studio:0.11.0
```

打开 [http://127.0.0.1:8765](http://127.0.0.1:8765)。示例在后台运行，并设置 Docker 重启策略；数据写入命名卷 `xtream-codec-studio-data`，保留该卷即可在更换容器后继续使用工作区。

| 配置     | Docker 镜像默认值                       | 直接运行 JAR 默认值      |
|----------|-----------------------------------------|--------------------------|
| 监听地址 | `0.0.0.0`                               | `127.0.0.1`              |
| 监听端口 | `8765`                                  | `8765`                   |
| 数据目录 | `/data`                                 | `~/.xtream-codec/studio` |
| 存储介质 | `file`；上面的运行示例显式选用 `sqlite` | `file`                   |

其他配置同样可通过 `-e` 传入。例子中的 `-p 127.0.0.1:8765:8765` 只向宿主机本地开放；需要其他机器访问时改为 `-p 8765:8765`，通过 `http://宿主机地址:8765` 访问。

只改变外部访问端口时，可用 `-p 9000:8765`，容器内仍监听 `8765`。如果同时设置 `XTREAM_STUDIO_PORT`，则 `-p` 右侧也要使用修改后的容器端口。

若把命名卷改成宿主机目录挂载，例如 `-v /srv/xtream-studio:/data`，应保证该目录对实际容器用户可写。镜像继承基础镜像默认用户，没有额外创建或切换用户。单独配置 `XTREAM_STUDIO_SQLITE_PATH` 时，也要保证对应文件位于持久挂载中。

### 日志、停止和重启

```bash
docker logs -f xtream-codec-studio
docker stop --time 15 xtream-codec-studio
docker start xtream-codec-studio
```

Docker 停止等待时间应大于 `XTREAM_STUDIO_SHUTDOWN_GRACE_SECONDS`，并给存储关闭留出余量。重建或升级容器时继续挂载原数据卷，并保持原存储介质配置。

## 从源码开发

### 用 Gradle 直接启动

不需要手动打包 JAR 时，可以从仓库根目录运行：

```bash
./gradlew :xtream-codec-studio-app:run \
  -Pxtream.frontend.build.xtream-codec-studio-ui.enabled=true
```

打开 [http://127.0.0.1:8765](http://127.0.0.1:8765)。该方式同样构建页面，也使用前面介绍的环境变量和数据目录配置。

### 前端热更新

保持 Java 后端运行，在另一个终端启动 Vite：

```bash
cd xtream-codec-studio-ui
pnpm install --frozen-lockfile
pnpm dev
```

开发时打开 [http://127.0.0.1:5173](http://127.0.0.1:5173)。Vite 把 `/api` 代理到 `http://127.0.0.1:8765`，页面修改后会热更新。后端使用其他地址时，例如端口 `9000`，可改为：

```bash
CODEC_API_TARGET=http://127.0.0.1:9000 pnpm dev
```

`5173` 和 `8765` 的区别：前者是前端开发服务器，后者是提供编解码、工作区和已构建页面的 Java 应用。单文件 JAR 和 Docker 部署直接访问 Java 服务端口即可。

两个端口属于不同的浏览器站点，本地草稿分别保存；它们连接同一个 Java 后端时，服务器中正式保存的报文共用。

## 界面预览

下面以“位字段与嵌套集合”和“808 · 0200 位置汇报”两个内置样例展示操作界面。截图使用深色主题。

### 位字段与嵌套集合

**可视化结构与编码值。** 左侧编辑字段顺序、位宽和嵌套结构，右侧填写本次编码的 JSON 值；标记为 AUTO 的负载长度自动回填。左边栏管理服务器报文，顶部标签可同时打开多份文档。

![Studio 基础报文：可视化编辑位字段、嵌套结构和编码值](/img/readme/xtream-codec-studio-demo-01-01.png)

**DSL JSON。** 切换到“DSL JSON”可直接编辑完整定义，修改后点击“应用定义”；结构定义和右侧编码值分别维护。完整配置步骤见[入门教程](./quick-start.md)。

![Studio 基础报文：DSL JSON 定义与独立的编码值](/img/readme/xtream-codec-studio-demo-01-02.png)

**HEX 与字段跟踪。** 编码或解码后，在运行面板对照 HEX DUMP 和 FIELD TRACE。图中选中 `payload.items[1].value`，对应字节 `56 78`，还可查看 bit、不同字符编码及整数解释。

![Studio 基础报文：HEX 字节与嵌套数组字段联动定位](/img/readme/xtream-codec-studio-demo-01-03.png)

**完整响应。** 展开运行面板并切换到“完整响应”，可查看字段值、HEX、消费字节数和 Trace 等原始结果。

![Studio 基础报文：完整 JSON 响应与运行快照](/img/readme/xtream-codec-studio-demo-01-04.png)

### JT/T 808 0200 位置汇报

**多版本结构与业务值。** 0200 样例使用同一份定义支持 2013 / 2019，截图选择目标版本 `2019`；可编辑终端 ID、流水号和位置数据，消息 ID、消息体长度等字段自动生成。

![Studio JT/T 808 0200：2019 版可视化结构与位置汇报编码值](/img/readme/xtream-codec-studio-808-0200-01.png)

**查看协议 DSL。** 在 JSON 中查看固定值、版本条件和字段配置，再按自己的协议修改。更多配置方式见[常用报文配置示例](./examples.md)。

![Studio JT/T 808 0200：包含固定值和多版本字段的 DSL JSON](/img/readme/xtream-codec-studio-808-0200-02.png)

**运行结果与校验。** 运行面板展示校验结果、报文字节和字段树；选中 `body.direction` 可定位对应的两个字节，并对照字段值。

![Studio JT/T 808 0200：校验结果、位置字段和 HEX 联动定位](/img/readme/xtream-codec-studio-808-0200-03.png)

**位段与 TLV 明细。** 展开运行面板，可检查消息体长度的 10 bit 位段、附加信息的 Tag / Length / Value 以及校验码。图中高亮的 `bodyLength` 与其他消息体属性共享字节，bit 预览会标明它实际占用的位置。

![Studio JT/T 808 0200：消息体长度位段、TLV 附加信息和校验码](/img/readme/xtream-codec-studio-808-0200-04.png)

## 启动问题排查

| 现象                                      | 检查方式                                                                                             |
|-------------------------------------------|------------------------------------------------------------------------------------------------------|
| JAR 不存在或没有 `-all.jar`               | 确认执行了 `:xtream-codec-studio-app:shadowJar`，并按 `projectVersion` 使用实际产物名称              |
| 构建提示缺少 UI，或 Gradle 启动后没有页面 | 显式开启 `-Pxtream.frontend.build.xtream-codec-studio-ui.enabled=true` 后重新构建或启动              |
| Java 版本错误                             | 确认 `java -version` 与 `JAVA_HOME` 都指向 Java 21                                                   |
| 提示端口已占用                            | 停止已有实例，或通过 `XTREAM_STUDIO_PORT` 换端口；同时调整 Vite 代理或 Docker 端口映射               |
| 本机能访问，其他机器不能                  | 检查监听地址、服务器网络和 Docker 的 `-p` 绑定地址；直接运行默认只监听本机                           |
| 工作区目录已被其他进程使用                | 文件存储目录已被另一实例持有；停止原实例或为新实例指定不同数据目录                                   |
| 存储初始化失败或无法保存                  | 查看控制台 / `docker logs`，检查数据路径、目录写权限和磁盘空间；使用自定义 SQLite 路径时核对挂载位置 |
| `5173` 页面打开，但 API 请求失败          | 确认 Java 后端已启动，并检查 `CODEC_API_TARGET` 是否指向实际后端地址                                 |

编辑器可用后，继续[入门教程](./quick-start.md)，或从[常用报文配置示例](./examples.md)选择相近的协议结构。
