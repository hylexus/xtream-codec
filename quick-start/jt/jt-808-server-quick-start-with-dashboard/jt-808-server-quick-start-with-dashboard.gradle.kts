import io.github.hylexus.xtream.codec.gradle.plugins.XtreamCodecFastModePlugin

plugins {
    id("org.springframework.boot")
    application
}

application {
    mainClass.set("io.github.hylexus.xtream.quickstart.ext.jt808.withdashboard.Jt808ServerQuickStartWithDashboardApp")
}
tasks.bootJar {
    archiveFileName.set("${project.name}.jar")
    mainClass.set("io.github.hylexus.xtream.quickstart.ext.jt808.withdashboard.Jt808ServerQuickStartWithDashboardApp")
}
apply<XtreamCodecFastModePlugin>()

dependencies {

    // 加解密
    api("org.bouncycastle:bcprov-jdk18on")
    // api(project(":ext:jt:jt-808-server-spring-boot-starter-reactive"))
    api(project(":ext:jt:jt-808-server-dashboard-spring-boot-starter-reactive"))
    api("org.springframework.boot:spring-boot-starter-logging")

    implementation("jakarta.annotation:jakarta.annotation-api")

}
