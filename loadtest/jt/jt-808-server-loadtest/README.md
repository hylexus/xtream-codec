# JT/T 808 独立压测服务

本模块为终端接入、持续上报和长期运行提供固定的最小业务实现。应用直接依赖正式 JT/T 808 starter，经过真实的 TCP/UDP 接入、解帧、校验、消息体解码、路由、会话和响应编码链路。开发示例的处理器及配置不参与本应用运行。

应用不接入数据库，不存储位置历史，不包含 Dashboard、前端或 HTTP 服务。指标按固定消息类别聚合，不为终端号建立不断增长的指标集合。

## 构建与启动

在仓库根目录使用 JDK 21 构建：

```sh
JAVA_HOME="$(mise where java)" ./gradlew :loadtest:jt:jt-808-server-loadtest:bootJar
```

产物为 `loadtest/jt/jt-808-server-loadtest/build/libs/jt-808-server-loadtest.jar`。该模块的 `bootJar` 独立启用，不受 Debug 模块 FatJar 开关影响。缓存完整时可给 Gradle 命令追加 `--offline`。

默认监听 `0.0.0.0:3927/TCP` 和 `0.0.0.0:3721/UDP`。下面的本机示例改用其他端口，便于和开发应用同时运行：

```sh
java -Xms512m -Xmx512m \
  -jar loadtest/jt/jt-808-server-loadtest/build/libs/jt-808-server-loadtest.jar \
  --jt808-server.instruction-server.tcp-server.host=127.0.0.1 \
  --jt808-server.instruction-server.tcp-server.port=13927 \
  --jt808-server.instruction-server.udp-server.host=127.0.0.1 \
  --jt808-server.instruction-server.udp-server.port=13721 \
  --logging.file.path=logs/jt-808-server-loadtest/local
```

应用日志默认写入文件，Spring Boot 启动横幅输出到控制台。启动后可在日志文件中查看监听地址和周期汇总。

```sh
tail -F logs/jt-808-server-loadtest/local/server.log
```

前台使用 `Ctrl+C` 停止。使用进程管理器时，管理器负责启动和停止这个 JAR 即可，不需要额外启动统计或日志清理任务。

## 消息与鉴权

| 消息 | 处理行为 | 应答 |
| --- | --- | --- |
| `0x0100` 注册 | 解码注册信息，重置当前会话鉴权状态，返回测试鉴权码 | `0x8100` |
| `0x0102` 鉴权 | 校验测试鉴权码，成功设置已鉴权，失败清除已鉴权状态 | `0x8001` |
| `0x0002` 心跳 | 要求当前会话已经鉴权 | `0x8001` |
| `0x0200` 位置上报 | 解码一份位置模型，鉴权通过后计入位置记录 | `0x8001` |
| `0x0704` 批量位置 | 解码每条位置，检查声明数量和类型，鉴权通过后统计实际记录数 | `0x8001` |

默认测试鉴权码为 `jt808-loadtest`。终端可使用注册应答中的鉴权码，也可以使用已知测试码直接鉴权，便于验证重连。新 TCP 连接的会话默认为未鉴权。UDP 会话由框架按其会话键管理，在空闲到期后清理；关闭终端的 UDP socket 不会立即通知服务端销毁会话。

通用应答结果 `0` 表示成功，`1` 表示未鉴权或鉴权失败，`2` 表示批量位置数量或类型不合法。校验码错误的帧在分包合并和业务处理前丢弃，不返回成功应答。其他消息沿用框架的未匹配处理行为，本模块不提供相应业务实现。

鉴权码须为 1～255 字节的可见 ASCII 字符。这是固定的测试认证规则，用于控制压测变量；它不覆盖生产环境的凭据分配、账号系统或认证数据库开销。

### 协议版本

支持框架现有的 2011、2013、2019 消息模型和响应格式。2019 根据版本标识解析；2011/2013 的公共报文头相同，不能普遍仅凭头区分。本模块沿用框架检测规则：注册消息按消息体长度区分旧版格式，其他无版本标识报文按 2013 公共格式处理。自定义厂商注册体不能可靠按体长区分时，需要单独提供协议版本检测实现。

消息仍须符合所选协议版本的适用范围：`0x0704` 用于 2013/2019，2011 终端使用 `0x0200` 上报位置。旧版头无法可靠表明发送方选择的 2011/2013，上述框架检测行为不等于对消息版本适用性的完整校验。

TCP 同一连接的请求按接收顺序执行，不同连接保持并发。UDP 请求保持独立处理；终端应等待注册、鉴权应答后再进入后续业务阶段。

## 配置

配置可通过命令行、环境变量或外部 Spring Boot 配置文件传入。

| 配置 | 默认值 | 含义 |
| --- | --- | --- |
| `jt808-server.instruction-server.tcp-server.enabled` | `true` | 是否监听 TCP |
| `jt808-server.instruction-server.tcp-server.host` / `port` | `0.0.0.0` / `3927` | TCP 地址和端口 |
| `jt808-server.instruction-server.udp-server.enabled` | `true` | 是否监听 UDP |
| `jt808-server.instruction-server.udp-server.host` / `port` | `0.0.0.0` / `3721` | UDP 地址和端口 |
| `loadtest.auth-code` | `jt808-loadtest` | 测试鉴权码，也可使用 `LOADTEST_AUTH_CODE` |
| `loadtest.tcp-backlog` | `2048` | 请求的 TCP 接入排队长度，也可使用 `LOADTEST_TCP_BACKLOG` |
| `loadtest.summary-interval` | `30s` | 汇总输出周期，须至少为 1ms |
| `logging.file.path` | `logs/jt-808-server-loadtest` | 日志目录，也可使用 `LOADTEST_LOG_PATH` |
| `jt808-server.instruction-server.tcp-server.session-idle-state-checker.all-idle-time` | `10m` | TCP 空闲检查 |
| `jt808-server.instruction-server.udp-server.session-idle-state-checker.max-idle-time` | `10m` | UDP 会话空闲时间 |

`backlog` 是操作系统接入排队参数，实际效果受系统限制影响，不代表最大在线终端数。其他 Netty loop、空闲扫描、分包重组及调度参数继续使用 starter 的配置契约。

本模块默认开启异步请求分发，并将非阻塞处理器调度器配置为 `immediate`。参数解析和本模块处理器在分发线程上执行，避免额外的业务线程切换。这个配置以处理器没有阻塞操作为前提；添加数据库、文件或阻塞外部调用时应重新选择调度策略。公共框架的默认调度配置没有改变。

### 对照 TCP 接入方式

启用 `--spring.profiles.active=dedicated-accept` 会把指令 TCP 的 `loop-resources.select-count` 设为 `1`，让接收新连接和处理子连接使用不同的事件循环组。默认配置继续沿用 starter。

压测 JAR 包含 macOS arm64 和 x86_64 的 Netty kqueue 依赖。在 macOS 上，默认配置优先使用可用的 kqueue。追加 `--jt808-server.instruction-server.tcp-server.loop-resources.prefer-native=false` 可以强制使用 NIO，便于用同一个 JAR 做对照。启动后检查 `TCP 压测监听` 日志中的 `channel`，确认实际使用 `NioServerSocketChannel`、`KQueueServerSocketChannel` 或 Linux 的原生通道。日志中的 `requestedBacklog` 是传给 Netty 的请求值。

例如，在系统上限至少为 128 的机器上用独立 accept 线程跑 NIO 对照：

```sh
java -Xms512m -Xmx512m \
  -jar loadtest/jt/jt-808-server-loadtest/build/libs/jt-808-server-loadtest.jar \
  --spring.profiles.active=dedicated-accept \
  --loadtest.tcp-backlog=128 --loadtest.summary-interval=1s \
  --jt808-server.instruction-server.tcp-server.host=127.0.0.1 \
  --jt808-server.instruction-server.tcp-server.port=13927 \
  --jt808-server.instruction-server.tcp-server.loop-resources.prefer-native=false \
  --jt808-server.instruction-server.udp-server.enabled=false \
  --logging.file.path=logs/jt-808-server-loadtest/tcp-admission
```

对照时每轮只改变一项。测试线程分离时保持 `prefer-native=false`，分别使用默认配置和 `dedicated-accept`。测试 kqueue 时保持同一个 select 配置，只切换 `prefer-native`。可在 macOS 用 `netstat -Lan -p tcp` 确认监听队列上限，并在上一轮的环回连接回收后再发起新的集中接入测试。

## 指标口径

汇总中的会话数是当前会话集合及 TCP/UDP 分类计数。消息统计按六个固定类别保存，未知消息归入 `OTHER`。

| 指标 | 含义 |
| --- | --- |
| `received` | 已完成帧/请求头解码的接收次数，包含随后校验码检查失败的帧；分包按每帧统计 |
| `accepted` | 消息处理器生成成功结果的次数 |
| `refused` | 消息处理器生成失败或消息有误结果的次数 |
| `preparedResponses` | 响应进入发送前回调的次数，不代表对端已经收到 |
| `processingErrors` | 进入本模块过滤器链后发生的处理异常，不包括此前的原始帧解码异常 |
| `invalidChecksums` | 校验码不匹配、被丢弃的帧数 |
| `locationRecords` | 业务接受的位置记录数，单条上报加 1，批量上报加实际条数 |

TCP 接入阶段单独汇总在 `tcpAdmission`。各阶段从应用启动累计，每条 TCP 连接在每个阶段最多计一次，重连作为新连接：

| 指标 | 含义 |
| --- | --- |
| `readyConnections` | Reactor Netty 已进入连接就绪回调的 TCP 连接数 |
| `firstDecodedFrames` | 已首次成功解出请求头的 TCP 连接数，包含首帧校验码随后被拒绝的连接 |
| `firstResponsesWritten` | 首个应答的 Netty 写入成功数 |
| `readyToFirstResponseP95Micros` / `readyToFirstResponseP99Micros` | 从连接就绪到首个应答写成功的近似 P95/P99，单位微秒，采用固定分桶上界，无样本时显示 0 |

注册与鉴权成功数分别为 `messages.REGISTER.accepted` 与 `messages.AUTH.accepted`，按业务消息累计。用纯 TCP 场景对照时，这两项仅包含 TCP。Netty 写入完成之后，客户端仍需实际收取应答，端到端延迟以客户端为准。

这些指标是应用本次启动后的累计值，停止终端后仍保留，重启应用后清零。读取多个并发计数得到的是近似同时的快照。应答成功率和往返延迟以模拟器实际收到的响应为准；服务端的 `accepted`、`preparedResponses` 不能替代客户端结果。

## 日志与诊断

默认关闭逐报文日志和会话 INFO 输出，保留应用启动、监听地址、周期汇总及异常。诊断时显式增加：

```sh
java -jar loadtest/jt/jt-808-server-loadtest/build/libs/jt-808-server-loadtest.jar \
  --spring.profiles.active=diagnostic \
  --jt808-server.instruction-server.tcp-server.port=13927 \
  --jt808-server.instruction-server.udp-server.port=13721
```

诊断 profile 会开启原始报文及相关 INFO 日志，包含测试鉴权报文，不能和默认模式的性能结果混用。

日志单文件按约 16 MiB 轮转，归档保留最多 7 天、总量目标约 128 MiB，启动时执行历史清理。归档上限不包含活动文件，正常保留规模约为 128 MiB 归档加一个活动文件。轮转和清理期间可能短暂超过这一规模。

异步日志队列容量为 8192，队列满时不阻塞协议处理，日志可能被丢弃，包括 WARN/ERROR。消息聚合计数独立维护；日志条数不能作为完整业务计数。需要改变日志保留策略时，可以通过 `--logging.config` 指定自己的配置文件。

## 对照与回归

集中接入和持续上报分开记录。集中接入关注全部上线耗时、连接/鉴权失败及重试；持续上报关注客户端应答率、往返 P95/P99、服务端 CPU、堆、分配和 GC。长期运行另外关注会话清理、断连重连与内存趋势。

安装 Rust 模拟器 CLI 后，可以先使用以下本机百终端短跑。目标与前面的服务端启动示例一致，TCP/UDP 各 50 个终端；未配置鉴权码时，内置场景从注册应答中提取并使用鉴权码。

```sh
mkdir -p logs/jt-808-server-loadtest/local
jt808 load --transport tcp --host 127.0.0.1 --port 13927 \
  --duration 60s --ramp-up 0s --scenario builtin:register-location \
  --group name=tcp-2019,version=2019,range=00000000013800000001-00000000013800000050,count=50 \
  --group name=udp-2019,version=2019,range=00000000013900000001-00000000013900000050,count=50,transport=udp,port=13721 \
  --output logs/jt-808-server-loadtest/local/loadtest-report.json
```

集中接入测试可逐步增加 `count` 并同步扩大号段范围，保持 `ramp-up=0s`；比较平滑接入时再调整 `ramp-up`。批量位置场景可改用 `builtin:batch-location-normal` 或 `builtin:batch-location-blind`，仅用于 2013/2019。报告是本地测试产物，使用后自行保留或清理，不加入源码仓库。

比较两个框架版本时，固定模拟器版本、终端数量和号段、版本及 TCP/UDP 比例、场景、发送间隔、接入速率、JDK、JVM 参数、日志模式和机器环境。先完成协议正确性验证，再逐步增加终端数量。需要定位 CPU 或分配热点时，单独采集有明确时长和文件容量限制的 JFR，记录采样对测试的影响。

```sh
JAVA_HOME="$(mise where java)" ./gradlew \
  :xtream-codec-server-reactive:test \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:test \
  :loadtest:jt:jt-808-server-loadtest:test
```

集成测试使用临时端口和临时日志目录，独立构造协议字节，覆盖三版 TCP/UDP、五类消息、鉴权失败、连续报文、错误恢复、重连及百终端混合交互。单元测试和短跑不代替规定时长的长跑，也不能直接证明某项优化的性能收益。
