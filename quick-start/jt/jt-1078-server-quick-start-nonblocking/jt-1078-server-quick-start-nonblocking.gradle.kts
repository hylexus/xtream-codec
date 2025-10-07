import io.github.hylexus.xtream.codec.gradle.plugins.XtreamCodecFastModePlugin

plugins {
    id("org.springframework.boot")
    application
    id("xtream-codec-frontend-build-plugin")
}

application {
    mainClass.set("io.github.hylexus.xtream.quickstart.ext.jt1078.nonblocking.Jt1078ServerQuickStartNonblockingApp")
}
tasks.bootJar {
    archiveFileName.set("${project.name}.jar")
    mainClass.set("io.github.hylexus.xtream.quickstart.ext.jt1078.nonblocking.Jt1078ServerQuickStartNonblockingApp")
}

apply<XtreamCodecFastModePlugin>()

dependencies {
    // api(project(":ext:jt:jt-1078-server-spring-boot-starter-reactive"))
    api(project(":ext:jt:jt-1078-server-dashboard-spring-boot-starter-reactive"))
    api("org.springframework.boot:spring-boot-starter-logging")

    implementation("jakarta.annotation:jakarta.annotation-api")
}

// ./gradlew clean build -P xtream.frontend.build.jt-1078-server-quick-start-ui.enabled=true
xtreamCodecFrontendBuild {
    enabled.set(xtreamConfig.buildJt1078QuickstartUi)
    group.set("jt1078-quickstart")
    description.set("jt1078-quickstart-ui")
    frontendProjectDir.set(layout.projectDirectory.dir("../jt-1078-server-quick-start-ui"))
    backendStaticDir.set(layout.projectDirectory.dir("src/main/resources/static/quickstart-ui/"))
}
