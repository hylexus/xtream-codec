plugins {
    `kotlin-dsl`
    id("com.github.joschi.licenser") version "0.6.0" apply false
}
repositories {
    maven {
        url = uri("https://maven.aliyun.com/repository/gradle-plugin")
        //url = uri("https://maven.aliyun.com/repository/public")
        //url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public")
        //url = uri("https://repo.huaweicloud.com/repository/maven")
        name = "aliyunGradlePlugin"
        content {
            // 404: https://maven.aliyun.com/repository/gradle-plugin/gradle/plugin/com/github/joschi/licenser/licenser/0.6.0/licenser-0.6.0.jar
            excludeGroup("gradle.plugin.com.github.joschi.licenser")
        }
    }
    gradlePluginPortal()
    mavenCentral()
    mavenLocal()
}

dependencies {
//    compileOnly("com.github.joschi.licenser:com.github.joschi.licenser.gradle.plugin:0.6.1")
    implementation("com.github.joschi.licenser:com.github.joschi.licenser.gradle.plugin:0.6.1")
}

