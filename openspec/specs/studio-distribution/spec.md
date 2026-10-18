# studio-distribution Specification

## Purpose

统一 Studio 的单文件与容器交付方式，让用户在没有项目构建工具和前端运行环境的机器上直接使用工作台，并通过一致的配置、日志和持久化行为管理应用。

## Requirements

### Requirement: 单文件应用交付

系统 SHALL 提供包含页面资源和运行依赖的可执行 JAR，用户仅提供 Java 21 运行环境即可通过 `java -jar` 启动。发布构建 SHALL 在缺少必要 UI 资源时失败。

fat JAR 打包、Shadow 分发及发布 JAR 集成验证 SHALL 通过显式任务按需执行，日常 build、assemble、check 及提交前构建 SHALL 不自动触发这些任务。

常规 Java 构建与测试 SHALL 不以生成 UI 资源为前提，真实首页、脚本、样式与 JSON Schema 资源的验收 SHALL 在手动发布集成验证中执行。

#### Scenario: 未构建前端时执行后端检查

- **WHEN** 用户首次检出项目且未生成 Studio UI 资源，使用默认关闭的前端构建开关执行常规 Java 构建或测试
- **THEN** Studio 后端 API 和工作区测试 SHALL 正常执行且不要求安装 Node.js 或 pnpm，不因缺少 UI 产物失败

#### Scenario: 日常构建与手动发布

- **WHEN** 用户执行日常 build、check 或 before-commit.sh
- **THEN** 构建 SHALL 保留常规测试与检查且不执行 Studio 的 fat JAR 打包及集成验证
- **WHEN** 用户显式执行 shadowJar 或 fatJarTest
- **THEN** 构建 SHALL 分别生成发布 JAR 或先生成发布 JAR 再执行真实启动验证

#### Scenario: 从发布 JAR 启动

- **WHEN** 用户在没有项目源码与 Node.js 的环境中执行发布 JAR
- **THEN** 应用 SHALL 提供首页、编解码 API 和工作区 API，文件与 SQLite 存储均可使用

### Requirement: 部署配置

系统 SHALL 支持使用系统属性或环境变量设置监听地址、端口、工作区存储类型、数据目录、SQLite 路径和 HTTP 停机等待秒数，系统属性 SHALL 优先于环境变量。默认 SHALL 监听 `127.0.0.1:8765`，使用已有文件存储目录，HTTP 停机等待默认 SHALL 为 5 秒。已有位置端口参数 SHALL 优先于端口配置且保留后续包名扫描参数。无效端口或负数停机等待 SHALL 明确终止启动。

#### Scenario: 系统属性覆盖容器环境

- **WHEN** 用户同时提供不同的系统属性和环境变量值
- **THEN** 应用 SHALL 使用系统属性值；显式位置端口参数 SHALL 进一步覆盖端口系统属性

#### Scenario: 环境变量配置网络与存储

- **WHEN** 用户仅使用环境变量配置监听地址、端口、SQLite 存储与数据目录
- **THEN** 应用 SHALL 使用这些配置启动，重启后 SHALL 保留已提交的工作区内容

### Requirement: 控制台日志

应用 SHALL 默认向控制台输出带有时间、级别和来源的运行日志，启动、停止及关闭失败 SHALL 通过统一日志输出。日志级别 SHALL 支持系统属性与环境变量配置，默认 SHALL 为 INFO，系统属性 SHALL 优先于环境变量。

#### Scenario: 配置日志级别

- **WHEN** 用户设置日志级别为 WARN
- **THEN** 应用 SHALL 不输出 INFO 级别启动日志，运行错误 SHALL 仍通过控制台报告

### Requirement: 容器交付与持久化

系统 SHALL 提供使用相同发布 JAR 的 Docker 构建文件，容器默认 SHALL 监听 `0.0.0.0:8765`、使用 `/data` 保存工作区、继承基础镜像默认用户直接启动 JVM，并允许覆盖部署配置与 JVM 参数。

#### Scenario: 使用基础镜像默认用户

- **WHEN** 用户构建并运行 Studio 容器且未指定运行用户
- **THEN** Dockerfile SHALL 不创建或切换用户，容器 SHALL 继承基础镜像默认用户，并保留 `/app`、`/data` 目录及直接启动 JVM 的入口

#### Scenario: 映射端口并重启容器

- **WHEN** 用户映射容器端口并挂载数据卷，保存工作区后停止并重新创建容器
- **THEN** 用户 SHALL 能通过映射端口访问页面，并从数据卷读取之前提交的工作区

#### Scenario: 手动构建并推送多架构镜像

- **WHEN** 用户从任意当前目录执行 Studio 的 `docker/build-image.sh`
- **THEN** 脚本 SHALL 定位仓库根目录，显式构建 UI、执行模块常规检查并生成 fat JAR，从根目录 `projectVersion` 读取版本，通过 buildx 构建 `linux/amd64,linux/arm64` 并向 `registry.cn-hangzhou.aliyuncs.com/xtream-codec/xtream-codec-studio-app` 推送 `latest` 和版本标签；JAR 文件 SHALL 与版本匹配，构建失败 SHALL 中止后续步骤，日常构建 SHALL 不触发该脚本

### Requirement: 停机生命周期

应用 SHALL 在正常进程终止时停止接收 HTTP 请求，按照配置等待正在处理的 HTTP 交换，并释放工作区资源。完成提交的文档 SHALL 在重启后可读。

#### Scenario: 停止并重启发布应用

- **WHEN** 用户保存工作区后向应用发送正常终止信号，再使用相同数据目录启动
- **THEN** 应用 SHALL 能重新取得工作区资源并读取已保存内容
