import io.github.hylexus.xtream.codec.gradle.plugins.XtreamCodecFastModePlugin
import org.cadixdev.gradle.licenser.LicenseExtension

plugins {
    id("org.springframework.boot")
    application
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

val quickStartUiStaticDir = project.file("src/main/resources/static")
// 前端打包生成的文件 不检测 License
extensions.configure(LicenseExtension::class.java) {
    exclude {
        it.file.startsWith(quickStartUiStaticDir)
    }
}

// ./gradlew clean build -P buildJt808QuickstartUiBlocking=true
val buildJt808QuickstartUiBlocking = getConfigAsBoolean("buildJt808QuickstartUiBlocking") || project.findProperty("buildJt808QuickstartUiBlocking") == "true"
val quickstartUiDir = file("../jt-808-server-quick-start-with-storage-ui")
val quickstartUiGroup = "jt808-quickstart"

tasks.register<Exec>("buildJt808QuickstartUiBlocking") {
    onlyIf { buildJt808QuickstartUiBlocking }
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

tasks.register<Copy>("copyJt808QuickstartUiDistBlocking") {
    onlyIf { buildJt808QuickstartUiBlocking }
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
    if (buildJt808QuickstartUiBlocking) {
        dependsOn(tasks.named("copyJt808QuickstartUiDistBlocking"))
    }
}

tasks.named("build").configure {
    if (buildJt808QuickstartUiBlocking) {
        dependsOn(tasks.named("buildJt808QuickstartUiBlocking"))
        dependsOn(tasks.named("copyJt808QuickstartUiDistBlocking"))
    }
}

fun getConfigAsBoolean(key: String) = project.ext.get(key)?.toString()?.toBoolean() ?: false
