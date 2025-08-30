import io.github.hylexus.xtream.codec.gradle.plugins.XtreamCodecFastModePlugin
import org.cadixdev.gradle.licenser.LicenseExtension
import kotlin.jvm.java

plugins {
    id("org.springframework.boot")
    application
    // id("xtream-codec-fast-mode-plugin")
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

val quickStartUiStaticDir = project.file("src/main/resources/static")
// 前端打包生成的文件 不检测 License
extensions.configure(LicenseExtension::class.java) {
    exclude {
        it.file.startsWith(quickStartUiStaticDir)
    }
}

// ./gradlew clean build -P buildJt1078QuickstartUiNonblocking=true
val buildJt1078QuickstartUiNonblocking = getConfigAsBoolean("buildJt1078QuickstartUiNonblocking") || project.findProperty("buildJt1078QuickstartUiNonblocking") == "true"
val quickstartUiDir = file("../jt-1078-server-quick-start-ui")
val quickstartUiGroup = "jt1078-quickstart"

tasks.register<Exec>("buildJt1078QuickstartUiNonblocking") {
    onlyIf { buildJt1078QuickstartUiNonblocking }
    group = quickstartUiGroup
    description = "构建 jt-1078-server-quick-start-ui"
    workingDir = file(quickstartUiDir)
    commandLine(
        "sh", "-c",
        """
        echo "===> 开始构建 jt-1078-server-quick-start-ui"
        pnpm install --registry https://registry.npmmirror.com
        pnpm run build
        echo "===> 构建 jt-1078-server-quick-start-ui 成功"
        """.trimIndent()
    )
}

tasks.register<Copy>("copyJt1078QuickstartUiDistNonblocking") {
    onlyIf { buildJt1078QuickstartUiNonblocking }
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
    if (buildJt1078QuickstartUiNonblocking) {
        dependsOn(tasks.named("copyJt1078QuickstartUiDistNonblocking"))
    }
}

tasks.named("build").configure {
    if (buildJt1078QuickstartUiNonblocking) {
        dependsOn(tasks.named("buildJt1078QuickstartUiNonblocking"))
        dependsOn(tasks.named("copyJt1078QuickstartUiDistNonblocking"))
    }
}

fun getConfigAsBoolean(key: String) = project.ext.get(key)?.toString()?.toBoolean() ?: false
