plugins {
    `kotlin-dsl`
}
repositories {
    listOf(
        "https://maven.aliyun.com/repository/public",
        "https://mirrors.cloud.tencent.com/nexus/repository/maven-public",
        "https://repo.huaweicloud.com/repository/maven",
        "https://maven.aliyun.com/repository/gradle-plugin",
    ).map {
        maven {
            url = uri(it)
            name = it
            content {
                // 上面几个镜像都没这个依赖
                excludeGroup("net.minecraftforge.licenser")
            }
        }
    }
    gradlePluginPortal()
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("net.minecraftforge.licenser:net.minecraftforge.licenser.gradle.plugin:1.2.0")
}

