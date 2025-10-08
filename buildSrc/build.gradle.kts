plugins {
    `kotlin-dsl`
}
repositories {
    maven {
        url = uri("https://maven.aliyun.com/repository/public")
        url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public")
        url = uri("https://repo.huaweicloud.com/repository/maven")
        url = uri("https://maven.aliyun.com/repository/gradle-plugin")
        name = "aliyunGradlePlugin"
        content {
            // 404
            excludeGroup("net.minecraftforge.licenser")
        }
    }
    gradlePluginPortal()
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("net.minecraftforge.licenser:net.minecraftforge.licenser.gradle.plugin:1.2.0")
}

