import io.github.hylexus.xtream.codec.gradle.plugins.XtreamCodecFastModePlugin


plugins {
    id("org.springframework.boot")
    application
    id("xtream-codec-frontend-build-plugin")
}

application {
    mainClass.set("io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.Jt808ServerQuickStartWithStorageBlockingApp")
}
tasks.bootJar {
    archiveFileName.set("${project.name}.jar")
    mainClass.set("io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.Jt808ServerQuickStartWithStorageBlockingApp")
}
apply<XtreamCodecFastModePlugin>()

dependencies {

    // 加解密
    api("org.bouncycastle:bcprov-jdk18on")
    // api(project(":ext:jt:jt-808-server-spring-boot-starter-reactive"))
    api(project(":ext:jt:jt-808-server-dashboard-spring-boot-starter-reactive"))
    api("org.springframework.boot:spring-boot-starter-logging")
    api("org.springframework.boot:spring-boot-starter-web")

    implementation("jakarta.annotation:jakarta.annotation-api")

    // region jdbc
    api("cn.mybatis-mp:mybatis-mp-spring-boot-starter")
    api("cn.mybatis-mp:mybatis-mp-datasource-routing")

    api("com.mysql:mysql-connector-j")
    api("org.postgresql:postgresql")
    api("com.clickhouse:clickhouse-jdbc")
    // endregion jdbc

    // 对象存储
    api("io.minio:minio")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core")

}

// ./gradlew clean build -P xtream.frontend.build.jt808-quickstart-ui.blocking.enabled=true
xtreamCodecFrontendBuild {
    enabled.set(xtreamConfig.buildJt808QuickstartUiBlocking)
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
