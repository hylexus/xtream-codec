---
icon: info-circle
article: false
---

# 概览

默认配置都在 `default-xtream-jt808-server-config.yml` 文件中：

- [Gitee - default-xtream-jt808-server-config.yml](https://gitee.com/hylexus/xtream-codec/blob/main/ext/jt/jt-808-server-spring-boot-starter-reactive/src/main/resources/META-INF/default-xtream-jt808-server-config.yml)
- [Github - default-xtream-jt808-server-config.yml](https://github.com/hylexus/xtream-codec/blob/main/ext/jt/jt-808-server-spring-boot-starter-reactive/src/main/resources/META-INF/default-xtream-jt808-server-config.yml)

下面是配置项概览，后续文档中会详细介绍。

```yaml
xtream:
  codec:
    # 表达式引擎(v0.4.0)
    expression:
      type: spel
jt808-server:
  # 总开关
  enabled: true
  ## 功能特性开关
  features:
    ### 转发请求时使用指定的调度器(而不是上游默认的Reactor调度器)
    request-dispatcher-scheduler: { }
    ### 请求日志(仅仅用于调试)
    request-logger: { }
    ### 请求合并(如果不启用的话，你需要手动合并子包请求)
    request-combiner: { }
  ## 指令服务器
  instruction-server:
    # TCP 服务配置(指令服务器)
    tcp-server: { }
    # UDP 服务配置(指令服务器)
    udp-server: { }
  ## 附件服务器
  attachment-server:
    # TCP 服务配置(附件服务器)
    tcp-server: { }
    # UDP 服务配置(附件服务器)
    udp-server: { }
  ## Reactor 用到的调度器配置
  schedulers:
    ## [转发请求]的调度器
    request-dispatcher: { }
    ## [非阻塞型]处理器的调度器
    non-blocking-handler: { }
    ## [阻塞型]处理器的调度器
    blocking-handler: { }
    ## [事件发布器]的调度器
    event-publisher: { }
    # 上面 3 个 Scheduler 是必须的
    # 下面 是自定义 Scheduler 的初始化(非必须)
    custom-schedulers: { }
```
