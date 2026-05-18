pluginManagement {
    val defaultSpringBootBomVersion: String by settings
    plugins {
        id("io.spring.dependency-management") version "1.1.7" apply false
        id("org.springframework.boot") version defaultSpringBootBomVersion apply false
        id("net.minecraftforge.licenser") version "1.2.0" apply false
        // @see https://github.com/jk1/Gradle-License-Report/issues/339
        id("com.github.jk1.dependency-license-report") version "3.0.1" apply false
        id("com.namics.oss.gradle.license-enforce-plugin") version "1.7.0" apply false
        id("io.gitee.pkmer.pkmerboot-central-publisher") version "1.1.1" apply false
        // https://github.com/melix/jmh-gradle-plugin
        id("me.champeau.jmh") version "0.7.3" apply false
        // https://github.com/tbroyer/gradle-errorprone-plugin
        id("net.ltgt.errorprone") version "4.3.0" apply false
    }

    repositories {
        mavenLocal()
        listOf(
            "https://maven.aliyun.com/repository/public",
            "https://maven.aliyun.com/repository/gradle-plugin",
            "https://mirrors.cloud.tencent.com/nexus/repository/maven-public",
            "https://repo.huaweicloud.com/repository/maven",
        ).forEach {
            maven {
                url = uri(it)
                name = it
                content {
                    // 上面几个镜像都没这个依赖
                    excludeGroup("net.minecraftforge.licenser")
                    excludeGroup("com.github.jk1")
                }
            }
        }
        gradlePluginPortal()
        mavenCentral()
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
    @Suppress("unstableApiUsage")
    repositories {
        extraMavenRepositoryUrls().forEach {
            maven(url = it) {
                content {
                    if (it.contains("aliyun")) {
                        excludeModule("io.projectreactor", "reactor-core-micrometer")
                    }
                }
            }
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
    // 国内镜像优先
    "https://maven.aliyun.com/repository/central",
    "https://maven.aliyun.com/repository/public",
    "https://maven.aliyun.com/repository/google",
    "https://maven.aliyun.com/repository/spring",
    "https://maven.aliyun.com/repository/spring-plugin",
    "https://maven.aliyun.com/repository/gradle-plugin",
    "https://maven.aliyun.com/repository/grails-core",
    "https://maven.aliyun.com/repository/apache-snapshots",
    "https://repo.huaweicloud.com/repository/maven",
    "https://mirrors.cloud.tencent.com/nexus/repository/maven-public",
    // 官方源兜底
    "https://repo1.maven.org/maven2",
    "https://plugins.gradle.org/m2/",
    "https://repo.spring.io/release",
    "https://repo.spring.io/snapshot"
)
