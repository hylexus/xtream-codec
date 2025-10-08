---
icon: gauge
article: false
---

# 接入Dashboard

::: tip

如果你不需要该组件，可以跳过本节。

:::

## 引入依赖

::: tip 提示

`jt-808-server-dashboard-spring-boot-starter-reactive` 中已经间接依赖了 `jt-808-server-spring-boot-starter-reactive`。

当然，两个依赖同时引入也没关系，版本不冲突即可。

:::

::: tabs#starter

@tab Maven

```xml

<dependency>
    <groupId>io.github.hylexus.xtream</groupId>
    <artifactId>jt-808-server-dashboard-spring-boot-starter-reactive</artifactId>
    <version>0.1.1</version>
</dependency>
```

@tab Gradle

```groovy

api("io.github.hylexus.xtream:jt-808-server-dashboard-spring-boot-starter-reactive:0.1.1")
```

:::

## 配置项

```yaml
server:
  ## 这个是 Dashboard 的 Web 端口( WebFlux/SpringMVC 的 HTTP 配置)
  port: 8888
jt808-server:
  ## ... 省略其他配置 ...
  features:
    dashboard:
      ### 是否启用 Dashboard
      enabled: true
      # 将 `/` 重定向(307) 到 `/dashboard-ui/`
      redirect-root-to-base-path: true
      # 将 `base-path` 开头的 404 请求转发到 `index.html`
      # 如果不开启这个配置，刷新 dashboard 页面时会出现404(也就是说只能刷新 `base-path` 根路由，其他子路由刷新会是 404)
      forward-not-found-to-index: true
```

## 访问

使用浏览器访问 [http://localhost:8888/dashboard-ui/](http://localhost:8888/dashboard-ui/) 就可以看到效果。
