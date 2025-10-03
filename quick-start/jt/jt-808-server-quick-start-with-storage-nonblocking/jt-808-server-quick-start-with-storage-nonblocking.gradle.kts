import io.github.hylexus.xtream.codec.gradle.plugins.XtreamCodecFastModePlugin

plugins {
    id("org.springframework.boot")
    application
    id("xtream-codec-frontend-build-plugin")
}

application {
    mainClass.set("io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.Jt808ServerQuickStartWithStorageNonblockingApp")
}
tasks.bootJar {
    archiveFileName.set("${project.name}.jar")
    mainClass.set("io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.Jt808ServerQuickStartWithStorageNonblockingApp")
}
apply<XtreamCodecFastModePlugin>()

dependencies {
    // 加解密
    api("org.bouncycastle:bcprov-jdk18on")
    // api(project(":ext:jt:jt-808-server-spring-boot-starter-reactive"))
    api(project(":ext:jt:jt-808-server-dashboard-spring-boot-starter-reactive"))
    api("org.springframework.boot:spring-boot-starter-logging")

    implementation("jakarta.annotation:jakarta.annotation-api")

    // region r2dbc
    api("pro.chenggang:mybatis-r2dbc-spring")
    // mybatis-r2dbc-spring 的依赖
    api("org.apache.commons:commons-lang3")

    api("io.asyncer:r2dbc-mysql")
    api("org.postgresql:r2dbc-postgresql")
    api("com.clickhouse:clickhouse-client")
    api("com.clickhouse:clickhouse-r2dbc")
    // endregion r2dbc

    // 对象存储
    api("io.minio:minio")
}

// ./gradlew clean build -P xtream.frontend.build.jt808-quickstart-ui.non-blocking.enabled=true
xtreamCodecFrontendBuild {
    enabled.set(xtreamConfig.buildJt808QuickstartUiNonblocking)
    group.set("jt808-quickstart")
    description.set("jt808-quickstart-ui")
    frontendProjectDir.set(layout.projectDirectory.dir("../jt-808-server-quick-start-with-storage-ui"))
    frontendDistDir.set(layout.projectDirectory.dir("../jt-808-server-quick-start-with-storage-ui/dist"))
    frontendBasePath.set("/")
    buildCommand = """
        pnpm install --registry https://registry.npmmirror.com \
        && pnpm run build
        """.trimIndent()
    backendStaticDir.set(layout.projectDirectory.dir("src/main/resources/static/quickstart-ui/"))
    cleanBackendStaticDir.set(true)
    createBackendStaticDirIfMissing.set(true)
}
