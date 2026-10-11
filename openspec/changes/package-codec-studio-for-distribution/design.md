## Context

Studio 使用 Gradle application、JDK HttpServer、虚拟线程和可复用 core 调试服务。工作区已支持系统属性、环境变量及 ServiceLoader；UI 构建通过 processResources 接入但默认关闭。core 已提供 SLF4J API，应用没有日志实现。见 proposal.md 的变更动机。

## Goals / Non-Goals

**Goals:** 使用相同产物交付 JAR 和镜像；保持 HTTP 与存储合同及位置参数兼容；在真实打包环境验证资源、服务发现、原生库和重启。

**Non-Goals:** 不引入 Spring Boot 运行时、账号或新的 Web 框架；不发布镜像到远程仓库；不修改 core 依赖；不新增配置文件格式。JVM 参数使用标准 JAVA_TOOL_OPTIONS。

## Decisions

1. 使用 com.gradleup.shadow 插件，保留 application。fat JAR 使用 `all` 分类，合并服务声明，移除失效签名与模块描述符，不启用依赖最小化或包重定位，保留反射扫描与 SQLite 原生资源。普通 jar 和 application 分发继续保留。关闭 Shadow 的 assemble 自动依赖，并从 assemble 中移除 Shadow 分发归档，fat JAR 仅由显式发布任务触发。
2. 发布 jar 校验 UI 入口及 Schema 资源；发布命令显式开启现有前端构建开关，不为普通 Java 测试强制改变仓库构建策略。
3. 应用配置使用小型不可变配置对象解析 host、port 和 shutdown-grace-seconds；系统属性优先于环境变量，缺失时使用默认值。旧位置端口优先，后续扫描包参数保持兼容。存储沿用 WorkspaceStores 的既有配置，不跨越工作区业务边界。
4. 日志使用 SLF4J 与 BOM 管理的 logback-classic；logback.xml 提供控制台格式与 `xtream.studio.log-level` / `XTREAM_STUDIO_LOG_LEVEL` 嵌套优先级，外部高级配置可使用原生 logback.configurationFile。初始化失败记录异常并以非零状态退出，关闭流程通过日志报告。
5. 独立应用默认 HTTP 停机等待 5 秒；嵌入服务的既有构造器保持立即停止语义。启动异常及时关闭工作区；正常关闭先结束 HTTP 服务，再关闭工作区。JVM 作为容器入口直接接收停止信号。
6. Dockerfile 使用 `m.daocloud.io/docker.io/library/eclipse-temurin:21-jre`、固定普通用户、/data 目录及 exec 形式 ENTRYPOINT。构建上下文为 Studio 模块，JAR_FILE 构建参数选择版本化产物；镜像默认覆盖 host 和 data-dir，应用自身保留本机默认值。运行数据卷必须可由容器用户写入。
7. 新增配置边界与优先级测试，注册独立 fatJarTest 任务，通过子 JVM 执行真实 java -jar；按用户要求保留为手动发布验证，不接入日常 check 或 build。普通 HTTP 测试仅验证后端 API 合同，不依赖生成的 UI；真实页面、脚本、样式和 JSON Schema 资源验收归入 fatJarTest。发布验证同时检查实体扫描、编解码、文件及 SQLite 存储重启和日志；Docker daemon 可用时再构建与运行容器，否则在验证记录中明确缺少容器验收。

## Risks / Trade-offs

- 展开依赖资源的冲突 → 合并服务文件、保留原生资源，并从发布 jar 实际测试 SQLite 与实体目录扫描。
- UI 开关关闭导致发布包缺页 → 打包前检查必需资源，文档给出显式开启 UI 的发布命令。
- 容器数据权限与停止信号 → 固定普通用户，记录挂载目录权限与 JAVA_TOOL_OPTIONS 用法，JVM 直接作为入口。
- 开发机无 Docker daemon → 完成 JAR 集成验证与 Docker 文件检查，记录容器构建和运行仍需可用 daemon。

## Migration Plan

原 run、installDist 与位置参数继续可用。构建 fat JAR 后可以复用已有数据目录或将容器数据挂载到 /data；不改变工作区数据格式。回滚应用版本时保留数据目录与已有备份。
