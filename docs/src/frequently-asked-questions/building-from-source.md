---
icon: code
article: false
---

# 编译源码

::: info 提示

[![Maven Central](https://img.shields.io/maven-central/v/io.github.hylexus.xtream/xtream-codec-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.hylexus.xtream%22%20AND%20a:%22xtream-codec-core%22)

一般情况下，你无需编译源码。直接使用已经发布到中央仓库的 **jar包** 即可(点击上面的图标链接查看最新版)。

:::

## 要求

- **JDK**: **21+**
- **Gradle**: 直接使用项目中自带的 **Gradle Wrapper** 即可，<span style="color:red;font-weight:bold;">不建议</span> 自己安装
  **Gradle**

## 编译

::: tip

第一次编译会比较慢。

- 自动下载 **Gradle**，已经修改为使用国内镜像地址下载
- `net.minecraftforge.licenser` 插件会从 [gradlePluginPortal()](https://plugins.gradle.org) 下载
- 其他插件以及 **jar** 依赖都会从国内下载

:::

### 命令行编译

- Mac/Linux/Unix

```shell
./gradlew clean build
```

- Windows

```shell
.\gradlew.bat clean build
```

![命令行编译效果截图](/img/faq/building-from-source/command-line-gradle-build.png)

### 导入IDEA

- 1). 项目 **JDK** 版本配置

![当前项目 JDK 版本](/img/faq/building-from-source/idea-0.png)

- 2). **Gradle** 配置

![Gradle配置-1](/img/faq/building-from-source/idea-1.png)

![Gradle设置-2](/img/faq/building-from-source/idea-2.png)

- 3). 打包

![打包结果](/img/faq/building-from-source/idea-3.png)
