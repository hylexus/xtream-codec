# 验证记录

## 基线

环境为 JDK 21、Gradle 9.8.2、JaCoCo 0.8.13。原有 5 个测试类共 34 个用例，全部通过。覆盖率只采集 `xtream-codec-server-reactive:test`，统计模块全部生产字节码。后续各轮继续使用同一口径。

| 指标 | 基线 |
| --- | ---: |
| 行 | 556 / 1,520（36.58%） |
| 分支 | 67 / 294（22.79%） |

临时 JaCoCo 初始化文件为 `xtream-codec-server-reactive/build/coverage.init.gradle`。基线 XML 保存在同模块 `build/reports/jacoco/test/baseline.xml`，用例及覆盖率报告分别在 `build/reports/tests/test/` 和 `build/reports/jacoco/test/`。

```sh
./gradlew --offline -I xtream-codec-server-reactive/build/coverage.init.gradle \
  :xtream-codec-server-reactive:jacocoTestReport
```

## 第一轮：请求分发与出站

新增 27 个用例，覆盖组件优先级、无匹配与短路、同步和异步异常恢复、普通对象及响应式对象编码、编码失败后的引用释放、会话查找、TCP 分组写入和 UDP 数据报封装。

修复前 7 个用例失败。同步抛出的适配器与结果处理异常绕过各自的恢复回调。对象应答和主动下发在出站订阅前就编码，出站拒绝或取消时会遗留框架分配的缓冲区。修复后同步异常进入 Reactor 恢复链，框架分配的应答缓冲区在出站订阅时编码。修复前报告在本模块 `build/round-1-before-fix/`。

全量模块测试 61 个用例通过。行覆盖率 711 / 1,520（46.78%），分支 111 / 294（37.76%）。覆盖率 XML 保存在 `build/reports/jacoco/test/round-1.xml`。

## 第二轮：事件与生命周期

新增 10 个用例，覆盖订阅时机、重复订阅和重订阅、精确兴趣与通配过滤、取消与关闭、空闲 UDP 会话清理、TCP/UDP 的绑定和重启。

修复前的定向运行有 5 个用例失败。事件发布器把 Flux 的装配误计为有效订阅，复用同一 Flux 会绕开重复 ID 检查。默认发布器关闭时未销毁自行创建的调度器。TCP 和 UDP 服务重复启动会重新绑定并丢失旧通道的引用。修复将登记绑定到订阅周期，关闭发布调度器，并保证同一运行中的服务只保留一个监听通道。修复前报告在本模块 `build/round-2-before-fix/`。

全量模块测试 71 个用例通过。行覆盖率 870 / 1,527（56.97%），分支 157 / 302（51.99%）。覆盖率 XML 保存在 `build/reports/jacoco/test/round-2.xml`。

## 第三轮：请求构建、处理器参数与调度

新增 13 个用例，覆盖请求和交换对象的连续变更、同一实例与内容相同的不同缓冲区、带非零起始索引的请求体解码、缺少会话及不支持的参数、处理器调度切换、UDP 异步异常隔离和后续数据报处理。

修复前定向运行有 4 个用例失败。请求构建器忽略新版本，交换构建器从初始请求重新开始而丢弃前一步变更，重复设置同一个缓冲区会释放仍被请求持有的引用，替换响应后交换对象的分配器仍来自旧响应。修复分别使用构建器的当前状态、按缓冲区身份判断替换，并在替换响应时使用其分配器。仅修改请求时，委托对象自定义的分配器仍然生效。修复前报告在本模块 `build/round-3-before-fix/`。

全量模块测试 84 个用例通过。行覆盖率 1,023 / 1,527（66.99%），分支 205 / 308（66.56%）。覆盖率 XML 保存在 `build/reports/jacoco/test/round-3.xml`。

## 最终回归

| 模块 | 用例 | 失败 / 错误 / 跳过 |
| --- | ---: | ---: |
| `xtream-codec-server-reactive` | 84 | 0 / 0 / 0 |
| JT/T 808 reactive starter | 375 | 0 / 0 / 0 |
| JT/T 808 独立压测服务 | 22 | 0 / 0 / 0 |

通用注解服务 quick-start 主源码编译通过，该模块当前没有测试源码。共享 reactive server 模块的 Checkstyle、Error Prone 和许可头检查通过。没有变更文档引用的源码片段。

```sh
./gradlew --offline -I xtream-codec-server-reactive/build/coverage.init.gradle \
  :xtream-codec-server-reactive:jacocoTestReport \
  :xtream-codec-server-reactive:check :xtream-codec-server-reactive:checkLicenses \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:test \
  :loadtest:jt:jt-808-server-loadtest:test :quick-start:custom-annotation-server:test \
  -Pxtream.backend.build.checkstyle.enabled=true \
  -Pxtream.backend.build.errorprone.enabled=true

openspec validate improve-server-reactive-unit-test-coverage --strict
git diff --check
```

## 第四轮：出站缓冲区交接

新增 7 个用例。验证原始 ByteBuf 在应答和主动下发中被拒绝或取消时的引用计数、发送已接管后失败时的原始异常，以及 UDP 分组发送中途出错或取消时的队列清理。

修复前定向运行有 6 个用例失败。发送未接管的原始缓冲区现在由调用侧释放，已经被出站取走的引用由出站继续管理。UDP 分组发送增加对被丢弃缓冲区和数据报的释放。修复前 XML 保存在模块 `build/round-4-before-fix/`。

全量模块测试 91 个用例通过。行覆盖率 1,034 / 1,538（67.23%），分支 209 / 312（66.99%）。覆盖率 XML 为 `build/reports/jacoco/test/round-4.xml`。

## 第五轮：并发关闭与定时任务

新增 4 个用例。用 latch 验证默认发布器在投递中关闭、Disruptor 已入队事件投递完成及线程退出。定时检查用例验证失败后的退避、新一轮触发、取消在途检查和重新启动。

修复前定向运行有 2 个用例失败。默认发布器直接从关闭调用线程终止 sink 时，与投递并发会丢掉完成信号，改为在发布调度器上按排队顺序发出完成信号并释放线程。重复启动定时检查会覆盖已运行任务，以同一锁串行化启动与停止并复用活跃订阅。修复前 XML 保存在模块 `build/round-5-before-fix/`。

全量模块测试 95 个用例通过。行覆盖率 1,074 / 1,542（69.65%），分支 219 / 316（69.30%）。覆盖率 XML 为 `build/reports/jacoco/test/round-5.xml`。

## 第六轮：通用注解服务回环请求

新增 2 个真实 TCP 回环用例。使用临时端口，从测试包扫描组合注解处理器，验证请求分帧、映射、实体解码、应答字节、`Mono<Void>` 与 `Flux<Void>` 的执行，以及映射未命中后处理同连接下一帧。

修复前断连用例等待关闭事件超时，默认管理器未移除已断开的 TCP 会话。默认管理器在新会话登记后订阅通道关闭，按会话实例移除并通知 `CLOSED_BY_CLIENT`。修复前 XML 保存在模块 `build/round-6-before-fix/`。

全量模块测试 97 个用例通过。行覆盖率 1,138 / 1,548（73.51%），分支 237 / 318（74.53%）。覆盖率 XML 为 `build/reports/jacoco/test/round-6.xml`。

## 第四至六轮回归

共享 reactive server 的 97 个、JT/T 808 starter 的 375 个及独立压测服务的 22 个测试均通过，无失败、错误或跳过。通用注解 quick-start 和 reactive TCP/UDP debug 样例编译通过。模块 Checkstyle、Error Prone、许可头、OpenSpec 严格校验及空白检查通过。

在首次回归通过后，再以 `:xtream-codec-server-reactive:test --rerun` 检查完整模块，覆盖并发关闭、定时调度及临时端口用例。覆盖率使用基线相同的临时初始化脚本。

## 第七轮：真实传输和满队列关闭

新增 4 个用例，覆盖 TCP 带连接停服、通用 UDP 双客户端隔离，以及 Disruptor 满队列恢复和关闭。

- **TCP 停服**。修复前监听端口关闭后，已接入的客户端仍等不到 EOF。只关闭子通道也无法取消等待中的异步处理。现在停服关闭当前绑定的子通道，通道关闭信号会取消在途业务。回环测试确认客户端收到 EOF、请求收到取消、输入引用归零、TCP 会话移除，并可重新启动接收请求。
  - **待专项压测**。在 Linux 上逐档增加 TCP 长连接数，覆盖稳定长连接和集中重连。记录连接组的稳态堆占用、集中重连时的分配量和建连 P99，以及停服期间的峰值堆占用、客户端全部断开和监听端口可重新绑定的耗时。以当前 `ChannelGroup` 实现为基线，视实测开销决定是否对照随服务销毁的独占 event loop 方案。若调整实现，需验证空连接也能关闭、在途请求取消、缓冲区回收、同实例重启，以及共享 event loop 的其他服务仍可运行。
- **UDP 隔离**。两个真实源端口交错上报并收到各自的原数据和会话计数。通过指定其中一个会话的最后通信时间，由实际定时扫描使它到期。原 socket 随后创建新会话，另一端的会话和计数继续沿用。该用例修复前已通过。
- **Disruptor 满队列**。修复前等待入队的调用在启动关闭后仍然阻塞。改为仅在有容量时申请序号，关闭时停止接纳并唤醒等待的生产者，已入队事件仍排空。保持运行的对照用例验证队列腾出容量后能发布，并保留等待期间出现的线程中断标记。
  - **待优化事件发布器**。当前满队列时按 `wait(1L)` 重试，入队等待没有总上限。后续评估多生产者并发下的锁争用、满队列时的等待上限及到限后的处理策略，并覆盖消费线程上的回调向同一发布器重入发布时可能等待自身的问题。调整后验证停机能唤醒等待入队的生产者，已接纳事件仍按序排空并完成订阅。

修复前定向运行 12 个用例有 2 个失败，XML 在本模块 `build/round-7-before-fix/`。修复后回环与发布器定向测试连续复跑三次通过，最后的完整测试还覆盖了等待期间的中断标记。

本轮重新生成基线与最终 XML，分别为 `build/reports/jacoco/test/round-7-baseline.xml` 和 `round-7.xml`。

| 指标 | 本轮基线 | 本轮完成 |
| --- | ---: | ---: |
| 行 | 1,139 / 1,549（73.53%） | 1,206 / 1,570（76.82%） |
| 分支 | 237 / 318（74.53%） | 259 / 330（78.48%） |

| 回归模块 | 用例 | 失败 / 错误 / 跳过 |
| --- | ---: | ---: |
| `xtream-codec-server-reactive` | 101 | 0 / 0 / 0 |
| JT/T 808 reactive starter | 375 | 0 / 0 / 0 |
| JT/T 1078 reactive starter | 16 | 0 / 0 / 0 |
| JT/T 808 独立压测服务 | 22 | 0 / 0 / 0 |

通用注解 quick-start 和 reactive TCP/UDP debug 样例编译通过。模块 Checkstyle、Error Prone、许可头、OpenSpec 严格校验及空白检查通过。

## 第八轮：默认会话管理器与 TCP 出站断连

新增 4 个回环用例，并在已有 UDP 双客户端用例中核对停服时显式提供的管理器未被关闭。修复前定向运行 6 个用例有 4 个失败，XML 保存在模块的 `build/round-8-before-fix/`。

- **TCP 默认管理器**。处理 TCP 请求时存在 UDP 空闲检查任务。现在 TCP 构建器默认不创建该任务。
- **UDP 默认管理器**。首次绑定失败和成功停服后都留有活跃的检查调度器。现在成功绑定后才创建管理器，接收终止时取消检查订阅并释放调度器。绑定失败后可重试，同一客户端在重启后创建新会话，并由实际检查任务完成到期淘汰。显式提供的管理器由调用者负责关闭。
- **TCP 出站断连**。客户端先收到首字节，第二个缓冲区交给 Netty 后暂停 flush，第三个留在发布器内等待。每个缓冲区都保留独立的测试引用。修复前第三个缓冲区从 2 个引用降到 0，入站的丢弃钩子和 Reactor Netty 的发送清理各释放一次。现在只对已交给请求处理链的入站缓冲区执行 retain，并在处理终止时释放。断连后每个出站缓冲区都剩下 1 个引用，正在处理的入站缓冲区归零。已有入站测试中的模拟接收端也按实际 Netty 行为清理未交付报文。

修复后，TCP 入站及 TCP/UDP 回环定向测试共 11 个通过。UDP 重启用例使用默认 10 秒检查周期，定向和全量回归均观察到实际到期事件。

| 指标 | 第七轮 | 第八轮 |
| --- | ---: | ---: |
| 行 | 1,206 / 1,570（76.82%） | 1,218 / 1,582（76.99%） |
| 分支 | 259 / 330（78.48%） | 263 / 334（78.74%） |

JaCoCo XML 保存在模块的 `build/reports/jacoco/test/round-8.xml`。

| 回归模块 | 用例 | 失败 / 错误 / 跳过 |
| --- | ---: | ---: |
| `xtream-codec-server-reactive` | 105 | 0 / 0 / 0 |
| JT/T 808 reactive starter | 375 | 0 / 0 / 0 |
| JT/T 1078 reactive starter | 16 | 0 / 0 / 0 |
| JT/T 808 独立压测服务 | 22 | 0 / 0 / 0 |

通用注解 quick-start 和 reactive TCP/UDP debug 样例编译通过。模块 Checkstyle、Error Prone、许可检查、OpenSpec 严格校验及 `git diff --check` 通过。

## 第九轮：UDP 在途请求与 Disruptor 回调关闭

新增 2 个回归用例。两个用例分别在对应修复前超时，原始 XML 保存在模块的 `build/round-9-before-fix/`。修复后，两个新用例连续重新执行三次均通过。

- **UDP 停服**。两个回环客户端各发一个数据报，业务处理都已订阅并挂起。修复前监听通道关闭后，请求链仍等待在途处理结束。修复后由通道关闭信号取消请求，两条业务链均收到 `CANCEL`，两个数据报引用计数均归零。同一服务重启后，两个客户端都收到各自的应答。
- **Disruptor 回调关闭**。队列容量为二，订阅回调处理首个事件期间再入队一个事件。修复前，回调里的 `shutdown()` 等待消费序号推进，而序号在回调返回后才会推进。修复后，消费线程发起关闭时先停止接纳事件，由另一个线程排空队列。测试确认回调返回、已入队事件按序到达、订阅完成且消费线程退出。由外部线程发起关闭的同步路径继续由原有回归覆盖。

| 指标 | 第八轮 | 第九轮 |
| --- | ---: | ---: |
| 行 | 1,218 / 1,582（76.99%） | 1,231 / 1,595（77.18%） |
| 分支 | 263 / 334（78.74%） | 267 / 338（78.99%） |

JaCoCo XML 保存在模块的 `build/reports/jacoco/test/round-9.xml`。

| 回归模块 | 用例 | 失败 / 错误 / 跳过 |
| --- | ---: | ---: |
| `xtream-codec-server-reactive` | 107 | 0 / 0 / 0 |
| JT/T 808 reactive starter | 375 | 0 / 0 / 0 |
| JT/T 1078 reactive starter | 16 | 0 / 0 / 0 |
| JT/T 808 独立压测服务 | 22 | 0 / 0 / 0 |

通用注解 quick-start 和 reactive TCP/UDP debug 样例编译通过。模块 Checkstyle、Error Prone、许可检查、OpenSpec 严格校验及 `git diff --check` 通过。

## 第十轮：TCP 分组刷新与 Spring 容器关闭

新增 2 个回归用例。两项均通过严格编译下的定向运行与全量回归。

- **TCP 分组应答**。回环客户端收完第一组才允许第二组开始发出。随后在第二组持有缓冲区时断开连接，确认发送链收到 `CANCEL`，两个缓冲区均只剩测试保留的一个引用。现有 `writeAndFlushWith` 实现通过该用例。
- **JT/T 808 starter 关闭容器**。使用 starter 自动配置和默认资源工厂启动指令 TCP，再通过回环地址建立连接。修复前，仅关闭 Spring 容器会导致客户端读取超时、端口重新绑定失败、资源工厂未开始释放 event loop。失败 XML 保存在 starter 的 `build/round-10-before-fix/`。修复后，内置指令和附件 TCP/UDP 服务通过 Spring 销毁方法先停服，再释放其资源工厂创建的 event loop。指令 TCP 用例确认客户端读到 EOF、原端口可重新绑定，NIO selector 与 worker 都已终止。

| 指标 | 第九轮 | 第十轮 |
| --- | ---: | ---: |
| 行 | 1,231 / 1,595（77.18%） | 1,231 / 1,597（77.08%） |
| 分支 | 267 / 338（78.99%） | 267 / 338（78.99%） |

JaCoCo XML 保存在模块的 `build/reports/jacoco/test/round-10.xml`。新增的资源工厂销毁路径由 JT/T 808 starter 测试执行。共享模块的 JaCoCo 报告只统计本模块测试，因此覆盖行数相同，分母增加两行。

| 回归模块 | 用例 | 失败 / 错误 / 跳过 |
| --- | ---: | ---: |
| `xtream-codec-server-reactive` | 108 | 0 / 0 / 0 |
| JT/T 808 reactive starter | 376 | 0 / 0 / 0 |
| JT/T 1078 reactive starter | 16 | 0 / 0 / 0 |
| JT/T 808 独立压测服务 | 22 | 0 / 0 / 0 |

通用注解 quick-start 和 reactive TCP/UDP debug 样例编译通过。共享模块及 JT/T 808 starter 的 Checkstyle、Error Prone 编译与许可头检查通过。OpenSpec 严格校验和 `git diff --check` 通过。

## 第十一轮：TCP 主动下发与 JT/T 1078 容器关闭

新增 2 个回归用例。严格编译下的定向测试与全量回归均通过。

- **TCP 主动下发**。回环客户端发送初始请求后，等待处理完成和入站引用归零，再从请求链外走 `DefaultXtreamCommandSender.sendByteBuf`。发送器按会话 ID 查到已登记的会话，原连接收到指令字节，发送缓冲区只剩测试保留的引用。现有发送实现通过该用例。
- **JT/T 1078 starter 关闭容器**。在同一个 Spring 容器使用实际自动配置启动 TCP 和 UDP，连接 TCP 后关闭容器。修复前，客户端读取超时、两种协议的原端口都无法重新绑定，两组资源工厂均未开始释放 event loop。失败 XML 保存在 starter 的 `build/round-11-before-fix/`。修复后，Spring 先停止内置服务，再释放其资源工厂创建的 event loop。回环测试确认 TCP 客户端读到 EOF、两个端口可重新绑定，以及 TCP selector、TCP worker 与 UDP worker 均终止。

共享模块行覆盖率为 1,231 / 1,597（77.08%），分支覆盖率为 267 / 338（78.99%），与第十轮相同。新增用例验证的是已有代码路径在真实连接中的组合行为。JaCoCo XML 保存在 `build/reports/jacoco/test/round-11.xml`。

| 回归模块 | 用例 | 失败 / 错误 / 跳过 |
| --- | ---: | ---: |
| `xtream-codec-server-reactive` | 109 | 0 / 0 / 0 |
| JT/T 808 reactive starter | 376 | 0 / 0 / 0 |
| JT/T 1078 reactive starter | 17 | 0 / 0 / 0 |
| JT/T 808 独立压测服务 | 22 | 0 / 0 / 0 |

共享模块与 JT/T 1078 starter 的 Checkstyle、Error Prone 编译及许可头检查通过。通用注解 quick-start、reactive TCP/UDP debug 和 JT/T 1078 debug 编译任务通过。OpenSpec 严格校验和 `git diff --check` 通过。

## 第十二轮：JT/T 1078 启动失败回滚

新增 1 个回环回归用例。用临时 UDP socket 预占 `127.0.0.1` 上的端口，再通过 JT/T 1078 starter 的实际自动配置启动 TCP 和 UDP。TCP 的绑定回调记录已监听的地址，随后 UDP 启动因端口冲突失败。Spring 报告的启动异常保留了 `ChannelBindException` 和冲突端口。刷新失败后，测试确认 TCP 监听通道已关闭、原端口可立即重新绑定，而且 TCP selector、TCP worker 和 UDP worker 的 NIO event loop 组均终止。现有 Spring 销毁方法通过了该回归。

| 回归模块 | 用例 | 失败 / 错误 / 跳过 |
| --- | ---: | ---: |
| `xtream-codec-server-reactive` | 109 | 0 / 0 / 0 |
| JT/T 808 reactive starter | 376 | 0 / 0 / 0 |
| JT/T 1078 reactive starter | 18 | 0 / 0 / 0 |
| JT/T 808 独立压测服务 | 22 | 0 / 0 / 0 |

JT/T 1078 starter 的 Checkstyle、Error Prone 编译及许可头检查通过。OpenSpec 严格校验和差异空白检查通过。

## 第十三轮：处理器调度器的释放与排队取消

- **JT/T 808 starter 关闭容器**。通过实际自动配置启动内置 `parallel` 和 `boundedElastic` 调度器各一个工作线程。修复前，容器关闭后两个调度器的 `isDisposed()` 均为 `false`。失败 XML 保存在 starter 的 `build/round-13-before-fix/`。修复后，由创建调度器的 Spring 定制器 Bean 在销毁时释放资源。测试确认两个调度器已释放、实际工作线程终止，应用注册的共享调度器在容器关闭后仍能执行任务。
- **TCP 排队请求断连**。在单线程处理器调度器中阻塞唯一的工作线程，确认回环客户端的请求已进入调度队列后断开连接。在放开调度线程前，入站 `ByteBuf` 的引用计数已经归零。调度队列排空后，注解处理方法的调用次数为零。此用例在修复前的定向运行中已通过。

共享模块 JaCoCo 行覆盖率为 1,231 / 1,597（77.08%），分支覆盖率为 267 / 338（78.99%）。报告保存在模块的 `build/reports/jacoco/test/round-13.xml`。

| 回归模块 | 用例 | 失败 / 错误 / 跳过 |
| --- | ---: | ---: |
| `xtream-codec-server-reactive` | 110 | 0 / 0 / 0 |
| JT/T 808 reactive starter | 377 | 0 / 0 / 0 |
| JT/T 1078 reactive starter | 18 | 0 / 0 / 0 |
| JT/T 808 独立压测服务 | 22 | 0 / 0 / 0 |

共享模块与 JT/T 808 starter 的 Checkstyle、Error Prone 编译及许可头检查通过。OpenSpec 严格校验和差异空白检查通过。

## 第十四轮：JT/T 1078 音视频调度器回收

- **JT/T 1078 starter 关闭容器**。通过实际自动配置启动音视频编解码和订阅调度器，各执行一个任务并记录实际工作线程。修复前，关闭容器后两个调度器的 `isDisposed()` 均为 `false`。失败 XML 保存在 starter 的 `build/round-14-before-fix/`。修复后，Spring 销毁创建调度器的定制器 Bean 时释放资源，两个调度器均已释放，记录到的工作线程均在有界等待内终止。
- **共享模块 UDP 测试同步**。首次全量回归中，两条已有用例在通道注销后调用 `channel.eventLoop()` 并抛出异常，失败 XML 保存在共享模块的 `build/round-14-before-fix/`。测试现在在停服前取得 event loop，停服后在该线程提交屏障，继续检查会话扫描停止以及在途请求取消、缓冲区释放。定向和全量重跑均通过。

| 回归模块 | 用例 | 失败 / 错误 / 跳过 |
| --- | ---: | ---: |
| `xtream-codec-server-reactive` | 110 | 0 / 0 / 0 |
| JT/T 808 reactive starter | 377 | 0 / 0 / 0 |
| JT/T 1078 reactive starter | 19 | 0 / 0 / 0 |
| JT/T 808 独立压测服务 | 22 | 0 / 0 / 0 |

JT/T 1078 starter 及共享模块测试的 Checkstyle、Error Prone 编译和许可头检查通过。JT/T 1078 debug 编译、OpenSpec 严格校验和差异空白检查通过。

## 第十五轮：终端重连路由与满队列恢复

- **JT/T 808 按终端 ID 下发**。在旧会话存在时创建位置查询指令的 `Mono`，用同终端的新会话替换旧会话后再订阅。修复前，发送器尝试向已断开的旧会话写入，失败 XML 保存在 starter 的 `build/round-15-before-fix/`。修复后，`Mono.defer` 在订阅时查找终端会话并调用下发逻辑。单测确认旧会话未写入、新会话收到指令，且按应答消息 ID 和流水号返回结果。用例使用真实发送器与模拟会话和编码器。
- **TCP 处理器调度拒绝**。占满单线程执行器和一格队列后发送回环 TCP 请求。异常处理器收到 `RejectedExecutionException`，入站 `ByteBuf` 的引用计数归零，该帧的处理方法未执行。放开线程并排空队列后，同一连接收到下一帧的应答。定向运行和全量回归均通过。

共享模块 JaCoCo 行覆盖率为 1,231 / 1,597（77.08%），分支覆盖率为 267 / 338（78.99%）。报告保存在模块的 `build/reports/jacoco/test/round-15.xml`。

| 回归模块 | 用例 | 失败 / 错误 / 跳过 |
| --- | ---: | ---: |
| `xtream-codec-server-reactive` | 111 | 0 / 0 / 0 |
| JT/T 808 reactive starter | 378 | 0 / 0 / 0 |
| JT/T 808 独立压测服务 | 22 | 0 / 0 / 0 |

共享模块与 JT/T 808 starter 的 Checkstyle、Error Prone 编译和许可头检查通过。OpenSpec 严格校验和差异空白检查通过。

## 第十六轮：分包缓存回收与 TCP 空闲断连

- **分包到期与容量淘汰**。缓存只收到首包时增加一个正文引用。请求释放自己的引用后，分别缩短缓存过期时间与收紧容量上限，触发 `EXPIRED` 和 `SIZE`。两种情况下缓存持有的 `ByteBuf` 引用都归零。随后用同一终端先发第二包，再发首包，确认只合并新一组报文，正文按分包序号拼接并保留新首包流水号，合并和请求释放后两片引用归零。
- **TCP 读空闲断连**。经 JT/T 808 starter 自动配置设置两秒读空闲，在临时回环端口分别连接发送合法心跳帧和始终不发帧的客户端。两者都收到 EOF。前者的会话从管理器移除，监听器只收到一次 `EXPIRED`。后者没有触发会话创建或关闭通知。两个参数化测试共新增四个场景，定向运行和全量回归均通过。

| 回归模块 | 用例 | 失败 / 错误 / 跳过 | JaCoCo 行覆盖率 | JaCoCo 分支覆盖率 |
| --- | ---: | ---: | ---: | ---: |
| `xtream-codec-server-reactive` | 111 | 0 / 0 / 0 | 1,231 / 1,597（77.08%） | 267 / 338（78.99%） |
| JT/T 808 reactive starter | 382 | 0 / 0 / 0 | 4,010 / 5,616（71.40%） | 367 / 659（55.69%） |
| JT/T 808 独立压测服务 | 22 | 0 / 0 / 0 |  |  |

共享模块和 JT/T 808 starter 的报告分别保存在各自的 `build/reports/jacoco/test/round-16.xml`。JT/T 808 starter 的 Checkstyle、Error Prone 编译和许可头检查通过。OpenSpec 严格校验和差异空白检查通过。

## 第十七轮：TCP 倒序应答与分包缓存销毁

- **TCP 主动下发倒序应答**。同一个 JT/T 808 2019 终端发心跳建立会话后，服务端发出两条 `0x8201` 位置查询。测试从实际下发帧读取流水号，先回第二条 `0x0201`，核对对应查询完成且第一条仍在等待，再回第一条。注解处理器从入站应答正文取出查询流水号，两个调用分别收到对应流水号和纬度。修复前后的定向运行均通过。
- **Spring 容器关闭**。默认合并器保留一组未收齐的分包，另有一组已合并并交给请求。修复前，关闭容器后未收齐分包的正文引用计数仍为 1，失败 XML 保存在 JT/T 808 starter 的 `build/round-17-before-fix/`。修复后，Spring 调用默认合并器的 `close()`。关闭流程与分包合并互斥，释放仍在缓存中的分包，淘汰回调继续负责已淘汰的分包。关闭容器后，未收齐分包的正文引用立即归零，已合并正文仍可读取，两片引用在合并请求释放后归零。
  - **待专项压测**。在 Linux 上用多终端并发上送 JT/T 808 协议分包，记录吞吐量、请求 P99 和 JFR 中 `DefaultJt808RequestCombiner` 实例锁的等待时间。若锁争用成为瓶颈，再评估是否按分包组缩小锁粒度。若调整实现，验证关闭、合并和缓存淘汰时的 `ByteBuf` 引用计数。

| 回归模块 | 用例 | 失败 / 错误 / 跳过 | JaCoCo 行覆盖率 | JaCoCo 分支覆盖率 |
| --- | ---: | ---: | ---: | ---: |
| `xtream-codec-server-reactive` | 111 | 0 / 0 / 0 | 1,233 / 1,599（77.11%） | 267 / 338（78.99%） |
| JT/T 808 reactive starter | 384 | 0 / 0 / 0 | 4,082 / 5,628（72.53%） | 391 / 663（58.97%） |
| JT/T 808 独立压测服务 | 22 | 0 / 0 / 0 |  |  |

两模块的 JaCoCo XML 分别保存在各自的 `build/reports/jacoco/test/round-17.xml`。共享模块和 JT/T 808 starter 的 Checkstyle、Error Prone 编译和许可头检查通过。OpenSpec 严格校验和差异空白检查通过。

## Netty 资源工厂 Bean 兼容性复核

- JT/T 808 指令、JT/T 808 附件和 JT/T 1078 服务的六个工厂方法恢复为各自的 TCP 或 UDP 接口返回类型。默认工厂保留 `destroyMethod = "dispose"`，内置服务保留 `destroyMethod = "stop"`。JT/T 808 指令 TCP 和 JT/T 1078 TCP/UDP 的生命周期测试确认默认事件循环随应用上下文关闭而终止。
- 以 JT/T 808 指令 TCP 为例，用原 Bean 名称注册自定义接口实现，验证内置服务使用该实现的事件循环。关闭上下文后，监听通道关闭，自定义事件循环仍能执行任务。
- JT/T 808 starter 的 385 个测试和 JT/T 1078 starter 的 19 个测试全部通过。两模块的 Checkstyle、Error Prone 编译和许可头检查通过。OpenSpec 严格校验和差异空白检查通过。

### 在工厂接口声明销毁方法

- 公共接口新增默认空操作 `dispose()`，基础类继续覆盖此方法并等待它创建的事件循环终止。两个 starter 的生命周期用例确认容器关闭后默认事件循环终止。
- 同名自定义 TCP 工厂继续只实现原有方法。测试通过 TCP 接口调用 `dispose()`，并在关闭容器后向共享事件循环提交任务，确认事件循环仍可用。
- 共享 reactive server 模块 111 个测试、JT/T 808 starter 385 个测试和 JT/T 1078 starter 19 个测试全部通过。三模块的 Checkstyle、Error Prone 编译和许可头检查通过。OpenSpec 严格校验和差异空白检查通过。

## TCP 入站通道非空约束复核

- TCP 入站测试为每个模拟连接提供独立的 `EmbeddedChannel`，通过默认的 `InboundInfo.forTcp()` 获取通道。TCP 请求链直接订阅通道关闭信号，移除了为测试替身传入空通道所保留的分支。
- 8 个定向测试通过，覆盖 TCP 请求生命周期、真实连接断开与停服重启，以及 JT/T 808 容器关闭。共享模块主代码和测试代码的 Checkstyle 均通过。

## 提交前复核

- 共享 reactive server、JT/T 808 starter、JT/T 1078 starter 和独立压测服务分别通过 111、385、19 和 22 个测试。启用 Error Prone 编译，前三个模块主代码和测试代码的 Checkstyle 均通过。
- 用临时用例对照当前实现与 `HEAD`。使用默认会话管理器的通用 UDP 构建器跳过子类的 `createRequestHandler()`，直接传入或返回 `ByteBuf` 时跳过子类的编码入口。这两处兼容性问题待修复。
- 用 JT/T 808 starter 的实际 Spring 自动配置复现关停时序。阻塞默认发布线程、提交事件、关闭容器，再放开发布线程。订阅调度器已销毁，订阅以 `Scheduler unavailable` 结束。发布队列排空与订阅调度器销毁的顺序待修复。

## 提交前复核修复

- 新增四个用例，修复前均失败。通用 UDP 构建器没有调用覆写的处理器工厂，两条原始 ByteBuf 路径发送了未经覆写编码的字节，JT/T 808 容器在发布线程仍阻塞时就完成关闭。修复前的 XML 保存在共享模块和 JT/T 808 starter 的 `build/submit-review-before-fix/`。
- JT/T 808 关停用例分别阻塞发布线程和订阅回调。修复后，两个事件按序到达，订阅正常完成，容器随后关闭订阅调度器并终止工作线程。
- 两条 ByteBuf 回归确认覆写入口收到原始缓冲区、实际发送覆写后的字节且引用计数归零。已有用例继续验证提前拒绝和取消时的引用释放，以及通用 UDP 默认管理器在绑定失败、停服和重启时的生命周期。合计 50 个定向用例通过。

| 模块 | 通过用例 | JaCoCo 行覆盖率 | JaCoCo 分支覆盖率 |
| --- | ---: | ---: | ---: |
| 共享 reactive server | 114 | 1,260 / 1,636（77.02%） | 272 / 344（79.07%） |
| JT/T 808 reactive starter | 386 | 4,082 / 5,628（72.53%） | 391 / 663（58.97%） |
| JT/T 1078 reactive starter | 19 | — | — |
| JT/T 808 独立压测服务 | 22 | — | — |

全量回归无失败、错误或跳过。前三个模块的 Checkstyle 和许可头检查通过，构建启用了 Error Prone。两个覆盖率报告保存在各模块的 `build/reports/jacoco/test/submit-review-fixes.xml`。OpenSpec 严格校验和差异空白检查通过。
