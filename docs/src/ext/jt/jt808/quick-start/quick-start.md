---
article: false
icon: rocket
tag:
  - JT/T 808
---

# 快速开始

此处将展示一个 最少配置 的 **808协议** 消息处理服务的搭建。

## 创建工程

创建一个空的 **spring-boot** 工程。

::: tip 传送门

可以使用 [Spring Initializer](https://start.spring.io) 快速初始化一个 Spring Boot 工程。

:::

## 添加依赖

引入为 **808协议** 提供的 `jt-808-server-spring-boot-starter-reactive`。

最新版本请点击
[![Maven Central](https://img.shields.io/maven-central/v/io.github.hylexus.xtream/jt-808-server-spring-boot-starter-reactive.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.hylexus.xtream%22%20AND%20a:%22jt-808-server-spring-boot-starter-reactive%22)
查看。

::: tabs#starter

@tab Maven

```xml

<dependency>
    <groupId>io.github.hylexus.xtream</groupId>
    <artifactId>jt-808-server-spring-boot-starter-reactive</artifactId>
    <version>0.0.1</version>
</dependency>
```

@tab Gradle

```groovy

api("io.github.hylexus.xtream:jt-808-server-spring-boot-starter-reactive:0.0.1")
```

:::

## 配置

该示例中，你可以不用做任何额外配置。

下面是几个关键配置的默认值(`application.yaml`)：

```yaml
jt808-server:
  ## 指令服务器
  instruction-server:
    ## 指令服务器-TCP
    tcp-server:
      # 绑定所有网卡(不只是127.0.0.1)
      host: 0.0.0.0
      port: 3927
    ## 指令服务器-UDP
    udp-server:
      # 绑定所有网卡(不只是127.0.0.1)
      host: 0.0.0.0
      port: 3721
  ## 附件服务器(苏标扩展)
  attachment-server:
    ## 附件服务器-TCP
    tcp-server:
      # 绑定所有网卡(不只是127.0.0.1)
      host: 0.0.0.0
      port: 3824
    ## 附件服务器-UDP
    udp-server:
      # 绑定所有网卡(不只是127.0.0.1)
      host: 0.0.0.0
      port: 3618
```

## 编写消息处理器

在 **spring-boot** 能扫描到的包中，随便新建一个 **java类**

本示例中命名为 `Jt808QuickStartRequestHandler.java`。内容如下：

```java
@Component // spring 环境下自动为该类创建单实例
@Jt808RequestHandler // 被该注解标记的类才会被扫描；类似于 spring 的 @Controller
public class Jt808QuickStartRequestHandler {

    /**
     * 位置上报(V2019)
     */
    // 类似于 spring 的 @RequestMapping；这里表示该方法支持处理 0x0200 指令，并且是 V2019 版本的指令
    @Jt808RequestHandlerMapping(messageIds = 0x0200, versions = Jt808ProtocolVersion.VERSION_2019)
    // 类似于 spring 的 @ResponseBody；这里表示该方法返回的数据将被封装成 0x8001 指令返回给客户端
    @Jt808ResponseBody(messageId = 0x8001)
    public Mono<ServerCommonReplyMessage> processMessage0200V2019(
            Jt808Session session,
            Jt808Request request,
            @Jt808RequestBody BuiltinMessage0200 body) {
        log.info("v2019-0x0200: {}", body);
        return this.processLocationMessage(session, body).map(result -> {
            // ...
            return ServerCommonReplyMessage.of(request, result);
        });
    }

    /**
     * 0:成功/确认; 1:失败; 2:消息有误; 3:不支持; 4:报警处理确认
     */
    private Mono<Byte> processLocationMessage(Jt808Session session, BuiltinMessage0200 body) {
        // 业务逻辑...
        return Mono.just((byte) 0);
    }
}

```

## 测试

### 启动项目

至此，对 **808消息** 的处理服务已经搭建完毕。启动你新建的 **spring-boot** 项目开始测试，下图是启动项目后的效果：

![quick-start-app-startup](/img/ext/jt/jt808/quick-start/quick-start-app-startup.png)

### 发报文

::: danger

不管你使用的是什么发包工具，都请确保你的报文是以 <span style="color:red;">十六进制</span> 格式发送的。

:::

发送这条测试报文：

```
7e02004086010000000001893094655200E4000000000000000101D907F2073D336C000000000000211124114808010400000026030200003001153101002504000000001404000000011504000000FA160400000000170200001803000000EA10FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF02020000EF0400000000F31B017118000000000000000000000000000000000000000000000000567e
```

下图是 Mac 环境下使用 [NetworkCat](https://apps.apple.com/cn/app/networkcat/id6503148471?mt=12) 发送的 **808协议** 位置上报报文截图。

![quick-start-app-startup](/img/ext/jt/jt808/quick-start/quick-start-app-network-cat-0200.png)

### 服务端日志

发送报文之后，你应该会看到类似下图的日志：

![quick-start-app-startup](/img/ext/jt/jt808/quick-start/quick-start-app-0200-log.png)

