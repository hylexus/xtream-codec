import io.github.hylexus.xtream.codec.gradle.plugins.XtreamCodecFastModePlugin

plugins {
    id("org.springframework.boot")
    application
    id("xtream-codec-frontend-build-plugin")
}

application {
    mainClass.set("io.github.hylexus.xtream.debug.ext.jt808.Jt808SpringBootStarterDebugApp")
}
tasks.bootJar {
    archiveFileName.set("${project.name}.jar")
    mainClass.set("io.github.hylexus.xtream.debug.ext.jt808.Jt808SpringBootStarterDebugApp")
}
apply<XtreamCodecFastModePlugin>()

dependencies {
    // 加解密
    api("org.bouncycastle:bcprov-jdk18on")
    // api(project(":ext:jt:jt-808-server-spring-boot-starter-reactive"))
    api(project(":ext:jt:jt-808-server-dashboard-spring-boot-starter-reactive"))
    api("io.projectreactor:reactor-core-micrometer")
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-logging")
    // jt808-server.event-publisher.publisher-type = disruptor
    api("com.lmax:disruptor")

    implementation("jakarta.annotation:jakarta.annotation-api")

}

// ./gradlew clean build -P xtream.frontend.build.jt-808-server-spring-boot-starter-debug-ui.enabled=true
xtreamCodecFrontendBuild {
    enabled.set(xtreamConfig.buildJt808DebugUi)
    group.set("jt808-debug-ui")
    description.set("jt808-debug-ui")
    frontendProjectDir.set(layout.projectDirectory.dir("../jt-808-server-spring-boot-starter-debug-ui"))
    backendStaticDir.set(layout.projectDirectory.dir("src/main/resources/static"))
}
