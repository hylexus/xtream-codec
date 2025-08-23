pluginManagement {
    val defaultSpringBootBomVersion: String by settings
    plugins {
        id("io.spring.dependency-management") version "1.1.7" apply false
        id("org.springframework.boot") version defaultSpringBootBomVersion apply false
        id("com.github.joschi.licenser") version "0.6.0" apply false
        id("com.github.jk1.dependency-license-report") version "2.9" apply false
        id("com.namics.oss.gradle.license-enforce-plugin") version "1.7.0" apply false
        id("io.gitee.pkmer.pkmerboot-central-publisher") version "1.1.1" apply false
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

}

rootProject.name = "xtream-codec"
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

setBuildFileName(rootProject)

fun setBuildFileName(project: ProjectDescriptor) {
    project.children.forEach {
        it.buildFileName = "${it.name}.gradle.kts"

        assert(it.projectDir.isDirectory())
        assert(it.buildFile.isFile())

        setBuildFileName(it)
    }
}
