# 验证记录

## 2026-11-17 初始验证范围

初始验证基于 Java 仓库 `93f40aa6` 上的本变更工作区，项目版本为 `0.11.0`。验证环境为 macOS 27.2.1 / arm64、Temurin JDK 21.0.13、Gradle 9.8.2、Spring Boot 3.5.14。Rust 联调端使用模拟器仓库 `00d8f11` 源码重新构建的调试版 CLI，仅用于协议联调。

测试使用随机回环端口和隔离日志目录，结束后关闭测试服务。未修改、重启或部署已有 Java 服务，也未操作服务器上的长期任务。日志和完整报告保存在本机忽略目录，不纳入公共源码。

## 缺陷复现与回归

- 会话生命周期的 9 项回归在修复前全部失败，修复后全部通过，覆盖并发创建、重复与重入关闭、旧实例关闭、新请求的连接活动检查、创建中关闭、单请求会话稳定性、属性存储和通信时间更新。
- TCP 生命周期的 5 项回归中，旧实现有 3 项失败；修复后验证同连接顺序、跨连接并发、同步及异步异常、空报文和取消时的引用释放。
- 终端会话查询回归确认旧实现选取最早通信会话，修复后返回最近通信会话。
- 真实 UDP 回归检出同步解码异常会结束接收订阅，修复后验证后续合法请求及同数据报内其他合法帧继续处理；另有数据报、切分帧和解码正文的释放回归。

这些隔离复现证明对应缺陷存在，不将其直接归因为此前某次大规模接入失败。

## 自动检查

| 模块 | 测试数 | 失败 / 错误 / 跳过 |
| --- | ---: | --- |
| `xtream-codec-server-reactive` | 32 | 0 / 0 / 0 |
| `ext/jt/jt-808-server-spring-boot-starter-reactive` | 359 | 0 / 0 / 0 |
| `loadtest/jt/jt-808-server-loadtest` | 21 | 0 / 0 / 0 |
| 合计 | 412 | 0 / 0 / 0 |

三模块主源码和测试的 Checkstyle、许可头检查均通过。独立 `bootJar` 构建及既有 JT/T 808 Debug 应用的 `compileJava` 通过。压测 JAR 约 24.5 MiB，检查其打包内容不包含 Debug、Dashboard、数据库驱动或 WebFlux 服务端；正式框架传递依赖的 `spring-web` 工具包仍保留，应用以非 Web 模式启动。

集成测试覆盖临时 TCP/UDP 监听、三版协议的注册与公共消息、2013/2019 批量位置、错误鉴权、鉴权恢复、连续 TCP 请求、TCP 重连、错误校验码、同步解码和业务正文异常后的恢复，以及 100 个混合终端。诊断 profile 的回归确认真实原始收发日志写入隔离文件。

主要验证命令：

```sh
./gradlew \
  :xtream-codec-server-reactive:test \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:test \
  :loadtest:jt:jt-808-server-loadtest:test \
  :loadtest:jt:jt-808-server-loadtest:bootJar \
  :debug:jt:jt-808-server-spring-boot-starter-reactive-debug:compileJava \
  :xtream-codec-server-reactive:checkLicenses \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:checkLicenses \
  :loadtest:jt:jt-808-server-loadtest:checkLicenses \
  --offline

./gradlew \
  :xtream-codec-server-reactive:checkstyleMain \
  :xtream-codec-server-reactive:checkstyleTest \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:checkstyleMain \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:checkstyleTest \
  :loadtest:jt:jt-808-server-loadtest:checkstyleMain \
  :loadtest:jt:jt-808-server-loadtest:checkstyleTest \
  -Pxtream.backend.build.checkstyle.enabled=true --offline

openspec validate add-jt808-loadtest-and-harden-admission --strict
git diff --check
```

## 独立 JAR 与 Rust 模拟器联调

直接通过 `java -Xms128m -Xmx256m -jar ...` 启动产物，TCP/UDP 均绑定随机回环端口，汇总周期设为 1 秒。未使用测试 classpath 或 Debug 应用。

Rust CLI 同时启动 100 个终端，TCP/UDP 各 50 个；2011 共 32 个，2013 和 2019 各 34 个，运行 10 秒。68 个终端使用单条位置场景，32 个 2013/2019 终端使用每批 10 条的批量位置场景。

| 消息 | 成功应答数 |
| --- | ---: |
| `0x0100` 注册 | 100 |
| `0x0102` 鉴权 | 100 |
| `0x0002` 心跳 | 100 |
| `0x0200` 单条位置 | 680 |
| `0x0704` 批量位置 | 320 |
| 合计 | 1300 |

100 个终端均完成注册和鉴权；客户端发送、匹配应答、成功操作均为 1300，业务失败、超时、解码错误、重试和失败样本均为 0。客户端确认位置点数为 3880，与服务端 `locationRecords` 一致。服务端消息接受数与客户端结果一致，`preparedResponses=1300`、`processingErrors=0`、`invalidChecksums=0`。

客户端结束后，服务端 TCP 会话归零，50 个 UDP 会话按空闲策略保留，符合 UDP 无显式断开通知的行为。测试 Java 进程已停止。

首次联调配置误把 `0x0704` 分配给 2011，Rust 在预检阶段正确拒绝、未启动终端。随后按适用版本调整分组，并同步明确文档和自动测试的版本边界；未通过原始发送绕过校验。

## 2026-11-17 初始验证结论

初始验证完成了缺陷回归、构建、代码风格、许可头、规格校验和真实协议短跑。当日未执行万终端集中接入、规定时长的长跑、Linux 部署、JFR 或 CPU/GC/P95/P99 对照测量。长期资源占用和性能改善幅度仍需在固定终端配置、日志模式、JVM 参数和运行环境后测量，按模块 README 分别记录接入与稳态结果。

## 2026-11-19 TCP 接入对照

本轮独立压测模块的 22 个测试全部通过。新增真实协议回归核对无首帧、首帧校验失败、正常写出首应答、后续应答和 UDP 的指标口径。JAR 构建、Checkstyle、Error Prone、许可头检查和 OpenSpec 严格校验通过。检查 JAR 确认包含 macOS arm64 和 x86_64 的 kqueue 库。

macOS arm64、JDK 21.0.13，在同机使用同一个 JAR 与 Rust Release CLI。每轮使用隔离回环端口和日志，JVM 堆为 512 MiB，请求且确认实际 backlog 为 128，汇总周期为 1 秒。以 200 台预热后启动 10,000 台 2019 TCP 终端，零间隔，运行 16 秒，场景为注册、鉴权、心跳和位置上报。启动日志确认了三种通道配置，独立 accept 模式的绑定回调在独立 select 线程上执行。

| 接入方式 | 唯一上线终端 | 场景失败 | 累计重连次数 | 监听队列采样峰值 |
| --- | ---: | ---: | ---: | ---: |
| NIO，共享事件循环组 | 9,999 | 1 | 121 | 33 |
| NIO，独立 accept | 9,921 | 65 | 249 | 8 |
| kqueue，共享事件循环组 | 9,979 | 20 | 350 | 21 |

三轮均记录了首应答写入及延迟分位。每种接入方式执行一次，实际终端启动分布有波动，结果用于核对模式切换和指标输出。kqueue 的运行验证范围为 macOS arm64。未进行本次改动的长期基准或多轮吞吐对照。

### 本机 NIO 基线

同日在相同机器上使用上述带接入指标的 JAR 和 Release CLI 进行一次短跑。指令 TCP 显式设置 `loop-resources.prefer-native=false`、`loop-resources.select-count=-1` 和 `loadtest.tcp-backlog=2048`。启动日志显示 `NioServerSocketChannel`，监听队列空闲时实测为 `0/0/2048`。

预热 200 台后以零间隔运行 10,000 台终端 16 秒，约 2.23 秒内发起全部终端。10,000 台均完成注册和鉴权并上线，场景失败、重连、服务端处理异常和校验错误均为 0。监听队列采样峰值为 117。

含预热在内，连接就绪、首帧解码和首个应答写成功均为 10,200 次。就绪到首应答写成功的 P95 和 P99 分桶上界均为 524,287 微秒。这组设置用于后续本机零间隔接入对照。

## 2026-11-19 下发、分包与会话回调回归

定向回归覆盖写入完成前应答、重复应答、等待键冲突、发送失败和取消后的清理。分包测试验证顺序及乱序到达时通用应答引用第 1 包的流水号，并检查正常完成、合并回调和下游同步异常、取消后的引用计数。TCP 和 UDP 附件入口分别验证解码通知失败后的正文释放。会话测试覆盖创建回调重入关闭、并发按键关闭和同键替换。

共享 reactive server、JT/T 808 starter 和独立压测模块分别通过 34、375 和 22 个测试，合计 431 个。Checkstyle、Error Prone、许可头检查和独立压测 JAR 构建通过。此次验证包括独立压测模块的三版 TCP/UDP 真协议回归，未重跑万终端性能基准。
