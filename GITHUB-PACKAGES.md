# 使用 GitHub Packages 预发布版本

本项目的预发布版本（`alpha` / `beta` / `rc`
）会发布在 [GitHub Packages](https://docs.github.com/zh/packages/learn-github-packages/introduction-to-github-packages)
上。

- 虽然发布到 **GitHub** 的预发布版本是 `Public` 的
- 但是需要 `token` 才能下载(**GitHub** 的限制)

参考资料:

- [这里参考创建令牌的流程(read:packages权限)](https://docs.github.com/zh/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)

---

## 1️⃣ 生成 GitHub Token

1. 登录 [GitHub](https://github.com/)
2. 进入 **Settings → Developer settings → Personal access tokens → Tokens (classic) → Generate new token**
3. 勾选 **read:packages** 权限
4. 复制生成的 token（只显示一次）

---

## 2️⃣ Maven 用户快速配置

**在 `~/.m2/settings.xml` 中添加：**

```xml

<servers>
    <server>
        <!-- 注意这个 id 要和下面 respiratory 中一致 -->
        <id>github</id>
        <username>你的GitHub用户名</username>
        <password>你的Personal Access Token</password>
    </server>
</servers>
```

在 `pom.xml` 中添加 GitHub Packages 仓库：

```xml

<repositories>
    <repository>
        <!-- 注意这个 id 要和上面 servers 中一致 -->
        <id>github</id>
        <url>https://maven.pkg.github.com/hylexus/xtream-codec</url>
    </repository>
</repositories>
```

使用依赖示例：

```xml

<dependency>
    <groupId>com.github.hylexus</groupId>
    <artifactId>xtream-codec-core</artifactId>
    <version>0.1.1-alpha.11.git.4e97026</version>
</dependency>

```

---

## 3️⃣ Gradle (Kotlin DSL) 用户快速配置

在 `build.gradle.kts` 中添加仓库：

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/hylexus/xtream-codec")
        credentials {
            username = System.getenv("GITHUB_USERNAME") // 或在 gradle.properties 中配置
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}
```

使用依赖示例：

```kotlin
dependencies {
    api("com.github.hylexus:xtream-codec-core:0.1.1-alpha.11.git.4e97026")
}
```

---

## 4️⃣ 注意事项

- **正式版**（如 1.0.0、1.1.0）会发布到 Maven Central，无需 `token`。
- 预发布版（`alpha`/`beta`/`rc`）必须带 `token`，否则会报 `401 Unauthorized`。
