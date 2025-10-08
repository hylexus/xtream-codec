import org.gradle.kotlin.dsl.maven

pluginManagement {
    val defaultSpringBootBomVersion: String by settings
    plugins {
        id("io.spring.dependency-management") version "1.1.7" apply false
        id("org.springframework.boot") version defaultSpringBootBomVersion apply false
        id("net.minecraftforge.licenser") version "1.2.0" apply false
        // @see https://github.com/jk1/Gradle-License-Report/issues/339
        id("com.github.jk1.dependency-license-report") version "2.9" apply false
        id("com.namics.oss.gradle.license-enforce-plugin") version "1.7.0" apply false
        id("io.gitee.pkmer.pkmerboot-central-publisher") version "1.1.1" apply false
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

}

rootProject.name = "xtream-codec"
include("xtream-codec-dependencies")
include("xtream-codec-base")
include("xtream-codec-core")
include("xtream-codec-server-reactive")

include("ext")
include("ext:jt")
include("ext:jt:jt-808-server-spring-boot-starter-reactive")
include("ext:jt:jt-808-server-dashboard-spring-boot-starter-reactive")
include("ext:jt:jt-1078-server-spring-boot-starter-reactive")
include("ext:jt:jt-1078-server-dashboard-spring-boot-starter-reactive")

include("debug")
include("debug:xtream-codec-core-debug")
include("debug:xtream-codec-server-reactive-debug-tcp")
include("debug:xtream-codec-server-reactive-debug-udp")
include("debug:jt")
include("debug:jt:jt-1078-server-spring-boot-starter-reactive-debug")
include("debug:jt:jt-808-server-spring-boot-starter-reactive-debug")

include("quick-start")
include("quick-start:jt")
include("quick-start:jt:jt-808-attachment-server-quick-start-blocking")
include("quick-start:jt:jt-808-attachment-server-quick-start-nonblocking")
include("quick-start:jt:jt-808-server-quick-start")
include("quick-start:jt:jt-808-server-quick-start-with-dashboard")
include("quick-start:jt:jt-808-server-quick-start-with-storage-nonblocking")
include("quick-start:jt:jt-808-server-quick-start-with-storage-blocking")
include("quick-start:jt:jt-1078-server-quick-start-nonblocking")
include("quick-start:jt:jt-1078-server-quick-start-blocking")

dependencyResolutionManagement {
    repositories {
        extraMavenRepositoryUrls().forEach {
            maven(it)
        }
        mavenCentral()
        mavenLocal()
    }
}

setBuildFileName(rootProject)

fun setBuildFileName(project: ProjectDescriptor) {
    project.children.forEach {
        it.buildFileName = "${it.name}.gradle.kts"

        assert(it.projectDir.isDirectory())
        assert(it.buildFile.isFile())

        setBuildFileName(it)
    }
}

fun extraMavenRepositoryUrls() = listOf(
//        "https://mirrors.cloud.tencent.com/nexus/repository/maven-public",
//        "https://repo.huaweicloud.com/repository/maven",
    "https://maven.aliyun.com/repository/central",
    "https://maven.aliyun.com/repository/public",
    "https://maven.aliyun.com/repository/google",
    "https://maven.aliyun.com/repository/spring",
    // Central
    "https://repo1.maven.org/maven2",
    "https://maven.aliyun.com/repository/spring-plugin",
    "https://maven.aliyun.com/repository/gradle-plugin",
    "https://maven.aliyun.com/repository/grails-core",
    "https://maven.aliyun.com/repository/apache-snapshots",
    "https://plugins.gradle.org/m2/",
    "https://repo.spring.io/release",
    "https://repo.spring.io/snapshot"
)
