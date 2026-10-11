# 验证记录

## 自动化检查

- 前端构建链路：pnpm install --frozen-lockfile、30 个前端测试、TypeScript 与脚本语法检查、Vite 生产构建全部通过。
- Studio 单元与既有 HTTP/存储测试共 22 项通过。
- fatJarTest 5 项通过：真实子 JVM 启动发布 JAR，检查页面和脚本、实体编解码、文件及 SQLite 文档重启恢复、系统属性与位置参数优先级、实体扫描、日志级别及启动失败后的工作区释放。
- checkstyleMain、checkstyleTest、checkLicenseMain 与 checkLicenseTest 通过。
- installDist 通过，保留既有 application 分发。
- git diff --check、变更严格验证和 11 个主规范验证全部通过。

执行的主要命令：

```bash
JAVA_HOME="$(mise where java)" ./gradlew :xtream-codec-studio-app:shadowJar :xtream-codec-studio-app:test \
  -Pxtream.frontend.build.xtream-codec-studio-ui.enabled=true

JAVA_HOME="$(mise where java)" ./gradlew :xtream-codec-studio-app:check :xtream-codec-studio-app:installDist \
  -Pxtream.backend.build.checkstyle.enabled=true

JAVA_HOME="$(mise where java)" ./gradlew :xtream-codec-studio-app:fatJarTest \
  :xtream-codec-studio-app:checkstyleTest :xtream-codec-studio-app:checkLicenseTest \
  -Pxtream.backend.build.checkstyle.enabled=true

openspec validate package-codec-studio-for-distribution --strict
openspec validate --specs
```

## 发布产物

- 可执行 JAR：xtream-codec-studio-app/build/libs/xtream-codec-studio-app-0.11.0-all.jar，约 22.67 MiB。
- 确认清单包含启动入口、项目版本和 Multi-Release 标识；包含 UI、NOTICE、SQLite 原生资源以及 JDBC 和 Logback 的服务声明。
- 本地 Docker 镜像：xtream-codec-studio:0.11.0，linux/arm64，约 138.2 MiB。
- 镜像使用 m.daocloud.io/docker.io/library/eclipse-temurin:21-jre；Docker Hub 认证访问超时后，按用户要求改用该源并成功构建。

## 容器运行

在本机 Docker Desktop 分别以 file 和 sqlite 配置创建临时容器及命名卷，验证：

- 实际 UID 为 10001，JVM 直接作为镜像入口，JAVA_TOOL_OPTIONS 可用。
- 宿主机映射端口可访问首页、Schema 资源及编解码 API。
- 保存正式文档后正常停止容器，停止日志可见，未触发强制结束或内存不足。
- 删除并重建容器、复用相同命名卷，文档身份与内容保留，文件锁可重新取得，Linux SQLite 原生库可加载。

临时容器与数据卷已清理，本地镜像保留。其他 CPU 架构未在本次验证。

## HTTP 路由重构验证

- 增加 8 组方法与路径组合的回归检查，覆盖未知 API、错误方法、未配置工作区和非 GET 静态资源请求的 404/405 状态码及错误消息。
- 重构前后分别运行 CodecStudioServerTest 与 WorkspaceTest，共 27 项测试均通过（HTTP 12 项、工作区 15 项）。
- 重构后 checkstyleMain、checkstyleTest 与 checkLicenses 通过。

```bash
JAVA_HOME="$(mise where java)" ./gradlew :xtream-codec-studio-app:test \
  --tests '*CodecStudioServerTest' --tests '*WorkspaceTest' \
  :xtream-codec-studio-app:checkstyleMain :xtream-codec-studio-app:checkstyleTest \
  :xtream-codec-studio-app:checkLicenses \
  -Pxtream.backend.build.checkstyle.enabled=true --console=plain
```

## 工作区路由重构验证

- 增加 8 组工作区方法与路径组合检查，确认固定接口的错误方法及未知路径返回 404，条目接口的不支持方法返回 405，状态码与错误消息保持一致。
- 在 file 与 sqlite HTTP 流程中增加删除条目并确认删除后读取返回 404 的检查。
- 使用上述命令在 workspace 重构前后验证，共 35 项测试均通过（HTTP 12 项、工作区 23 项）。
- 重构后 checkstyleMain、checkstyleTest、checkLicenses 与 git diff --check 通过。
- 按用户反馈将最终结构调整为外层方法 switch、GET 与 POST 内层路径 switch，固定路径使用字符串 case，动态条目读取保留 when 守卫；PUT 与 DELETE 独立处理。调整前后 35 项测试通过，样式和许可证检查通过。

## 按需发布构建验证

- 使用 before-commit.sh 相同参数对根项目 clean build 执行 dry-run，确认 Studio 普通 jar、application 分发、常规测试、样式与许可证检查仍在任务图中，shadowJar、startShadowScripts、shadowDistTar、shadowDistZip 和 fatJarTest 均未进入任务图。
- 首次实际 Studio clean build 因本地未生成 UI 资源而在既有首页测试失败；通过已有前端构建开关将 dist 复制到应用资源后，重新使用提交检查的相同参数执行 Studio clean build，38 项普通测试及 Checkstyle、许可证检查全部通过。
- 确认 build/libs 仅包含 xtream-codec-studio-app-0.11.0.jar，没有生成 all.jar。
- 对手动 shadowJar、fatJarTest、shadowDistTar 和 shadowDistZip 执行 dry-run，确认显式发布任务仍可用；本次没有实际运行发布打包或发布 JAR 集成验证。
- README 的完整发布检查命令现在显式列出 fatJarTest，check 默认仅执行常规检查。

## 后端测试不依赖 UI 的验证

- 将普通 HTTP 测试改名为 shouldServeVersionedCodecApi，保留实体目录、模板、动态及实体编解码、未知接口和资源回退检查；首页、脚本、样式及 JSON Schema 内容与响应类型的检查归入手动 fatJarTest。
- 暂存应用的 src/main/resources/ui 与前端 dist，关闭前端构建开关并使用提交检查的其余开关执行 Studio clean build。38 项普通测试全部执行并通过，没有跳过测试；Checkstyle 与许可证检查通过。
- 检查该次构建的 resources/main 中没有 ui 目录，build/libs 中没有 all.jar；前端构建与复制任务均跳过，发布打包和发布测试未执行。检查后恢复原有本地 UI 资源与 dist。
- 手动执行 fatJarTest 的 publishedJarShouldServeResourcesAndPersistDocumentsAcrossRestart，在 file 与 sqlite 两种存储下共 2 项发布 JAR 集成验证通过，确认迁移后的页面、脚本、样式与 JSON Schema 验收有效。

```bash
JAVA_HOME="$(mise where java)" ./gradlew :xtream-codec-studio-app:fatJarTest \
  --tests '*CodecStudioFatJarTest.publishedJarShouldServeResourcesAndPersistDocumentsAcrossRestart' \
  -Pxtream.frontend.build.xtream-codec-studio-ui.enabled=true --console=plain
```
