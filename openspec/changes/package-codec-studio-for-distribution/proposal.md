## Why

Studio 当前仅提供 Gradle application 分发，尚不能直接交付单文件可执行 JAR；固定的回环监听地址也不能用于 Docker 端口映射。统一部署配置、控制台日志和发布产物，使用户能够通过 `java -jar` 或 Docker 使用同一套工作台。

## What Changes

- 保留 JDK HTTP 服务，引入 Shadow 构建包含 UI 和运行依赖的可执行 fat JAR。
- 将监听地址、端口和停机等待时间纳入系统属性与环境变量配置，保留已有位置参数兼容性与工作区存储配置。
- 在应用中使用 SLF4J 与 Logback，默认输出控制台，日志级别支持系统属性与环境变量。
- 提供使用相同 fat JAR 的 Dockerfile，设置容器监听地址和数据目录，使用基础镜像默认用户直接启动 JVM。
- 补充配置测试、可执行 JAR 集成验证和发布运行文档。

## Capabilities

### New Capabilities

- `studio-distribution`: Studio 可执行 JAR、容器交付、部署配置、日志和停机行为。

### Modified Capabilities

无。既有编解码及工作区 HTTP 合同保持兼容。

## Impact

- 修改 `settings.gradle.kts` 中的 Shadow 插件版本声明和 Studio 模块构建脚本。
- 修改 Studio 启动入口、HTTP 服务生命周期，新增应用配置、Logback 资源和集成测试。
- 新增模块内 Docker 构建文件并更新 README 与 WORKSPACE 文档。
- Logback 仅作为 Studio 运行依赖；core 的依赖和业务边界保持现状。JVM 参数使用 Java 原生 `JAVA_TOOL_OPTIONS`。
