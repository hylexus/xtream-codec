import io.github.hylexus.xtream.codec.gradle.plugins.XtreamCodecFastModePlugin
import org.cadixdev.gradle.licenser.LicenseExtension

plugins {
    id("org.springframework.boot")
    application
    // id("xtream-codec-fast-mode-plugin")
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

val quickStartUiStaticDir = project.file("src/main/resources/static")
// 前端打包生成的文件 不检测 License
extensions.configure(LicenseExtension::class.java) {
    exclude {
        it.file.startsWith(quickStartUiStaticDir)
    }
}

// ./gradlew clean build -P buildJt808QuickstartUiNonblocking=true
val buildJt808QuickstartUiNonblocking = getConfigAsBoolean("buildJt808QuickstartUiNonblocking") || project.findProperty("buildJt808QuickstartUiNonblocking") == "true"
val quickstartUiDir = file("../jt-808-server-quick-start-with-storage-ui")
val quickstartUiGroup = "jt808-quickstart"

tasks.register<Exec>("buildJt808QuickstartUiNonblocking") {
    onlyIf { buildJt808QuickstartUiNonblocking }
    group = quickstartUiGroup
    description = "构建 quickstart-ui"
    workingDir = file(quickstartUiDir)
    commandLine(
        "sh", "-c",
        """
        echo "===> 开始构建 quickstart-ui"
        pnpm install --registry https://registry.npmmirror.com
        pnpm run build
        echo "===> 构建 quickstart-ui 成功"
        """.trimIndent()
    )
}

tasks.register<Copy>("copyJt808QuickstartUiDistNonblocking") {
    onlyIf { buildJt808QuickstartUiNonblocking }
    group = quickstartUiGroup
    description = "复制 quickstart-ui 构建输出"
    from("${quickstartUiDir}/dist")
    into("src/main/resources/static/quickstart-ui/")
    include("**/*")

    doFirst {
        delete("src/main/resources/static/quickstart-ui/")
    }

    // 始终重新执行任务
    outputs.upToDateWhen { false }
}

tasks.named("processResources").configure {
    if (buildJt808QuickstartUiNonblocking) {
        dependsOn(tasks.named("copyJt808QuickstartUiDistNonblocking"))
    }
}

tasks.named("build").configure {
    if (buildJt808QuickstartUiNonblocking) {
        dependsOn(tasks.named("buildJt808QuickstartUiNonblocking"))
        dependsOn(tasks.named("copyJt808QuickstartUiDistNonblocking"))
    }
}

fun getConfigAsBoolean(key: String) = project.ext.get(key)?.toString()?.toBoolean() ?: false
