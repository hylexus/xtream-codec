# 补充 reactive server 单元测试

## Why

`xtream-codec-server-reactive` 现有测试主要覆盖构建器、调度器注册和近期的会话与入站资源回归。请求分发、应答与主动下发的资源所有权、事件订阅和服务停止仍需要独立的行为验证。

## What Changes

- 对模块测试采集 JaCoCo 基线，按活跃入口和实际覆盖缺口分轮补测。
- 优先覆盖分发失败与恢复、TCP/UDP 出站、会话事件和服务生命周期。对可复现且违背既有契约的缺陷保留回归测试并修复。
- 跳过废弃 API。每轮在 `verification.md` 记录用例、缺陷、覆盖率和验证结果。
- 第四至六轮补测出站引用交接、事件发布与关闭的并发、定时检查及通用注解服务的回环请求。共享模块为 Disruptor 测试添加 `testRuntimeOnly` 依赖。
- 第七轮验证带连接停服、真实 UDP 端点隔离与会话到期，以及 Disruptor 满队列时的关闭。
- 第八轮验证通用服务默认会话管理器的生命周期，以及 TCP 写入期间断连的缓冲区交接。
- 第九轮验证 UDP 停服时的在途请求清理，以及 Disruptor 从订阅回调发起关闭后的排空。
- 第十轮验证真实 TCP 分组应答的刷新和断连，以及 JT/T 808 starter 关闭 Spring 容器时的资源回收。
- 第十一轮验证真实 TCP 会话在请求结束后的主动下发，以及 JT/T 1078 starter 关闭 Spring 容器时的资源回收。
- 第十二轮验证 JT/T 1078 starter 在 TCP 已启动、UDP 绑定失败时的 Spring 启动回滚。
- 第十三轮验证 JT/T 808 starter 创建的 Reactor 调度器随容器关闭释放，以及排队中的 TCP 请求在断连后取消。
- 第十四轮验证 JT/T 1078 starter 创建的音视频编解码与订阅调度器随容器关闭释放。
- 第十五轮验证 JT/T 808 终端重连后的指令路由，以及通用 TCP 处理器调度队列满时的拒绝与恢复。
- 第十六轮验证 JT/T 808 未收齐分包的缓存回收与重试，以及 TCP 读空闲时的连接和会话清理。
- 第十七轮验证 JT/T 808 主动查询的 TCP 倒序应答，以及 Spring 容器关闭时回收未收齐的分包。
- 在 Netty 资源工厂接口上声明可选的销毁方法，保留原有 Bean 返回类型，并验证同名自定义工厂提供的共享事件循环在容器关闭后仍可使用。
- 修复提交前复核发现的 JT/T 808 事件关停顺序，以及通用 UDP 处理链和原始 ByteBuf 编码的覆写兼容性。

## Capabilities

### New Capabilities

无。

### Modified Capabilities

无。补测及回归修复依据既有契约，使用 `skip_specs: true`。

## Impact

实现与测试覆盖 `xtream-codec-server-reactive` 及 JT/T 808、JT/T 1078 starter 的内置服务销毁配置。运行 JT/T 808 starter、JT/T 1078 starter 与独立压测模块作为下游回归。覆盖率通过临时 Gradle 初始化脚本采集。
