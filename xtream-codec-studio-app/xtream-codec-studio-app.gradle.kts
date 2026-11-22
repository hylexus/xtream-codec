import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.github.hylexus.xtream.codec.gradle.plugins.XtreamCodecFrontendBuildPlugin
import io.github.hylexus.xtream.codec.gradle.utils.XtreamConfig.xtreamConfig

plugins {
    application
    id("com.gradleup.shadow")
    id("xtream-codec-frontend-build-plugin")
}

dependencies {
    implementation(project(":xtream-codec-core"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.xerial:sqlite-jdbc:3.53.4.0")
    runtimeOnly("ch.qos.logback:logback-classic")
}

application {
    mainClass.set("io.github.hylexus.xtream.codec.studio.app.CodecStudioApplication")
}

shadow {
    addShadowJarToAssembleLifecycle.set(false)
}

tasks.assemble {
    // Shadow 分发归档也会通过 assemble 间接触发 fat JAR，发布时手动调用对应任务。
    setDependsOn(taskDependencies.getDependencies(this).filterNot {
        it.name in setOf("shadowDistTar", "shadowDistZip")
    })
}

val executableJar = tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("all")
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
    mergeServiceFiles()
    filesMatching(listOf("META-INF/services/**", "META-INF/*.kotlin_module")) {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    // 同名声明可能属于不同依赖，必须先保留再合并，不能按路径直接去重。
    val licenseResources = listOf(
        "META-INF/LICENSE", "META-INF/LICENSE.txt", "META-INF/LICENSE.md",
        "META-INF/NOTICE", "META-INF/NOTICE.txt", "META-INF/NOTICE.md",
        "META-INF/THIRD-PARTY-NOTICES.txt",
        "META-INF/THIRD-PARTY-SOURCE-NOTICES.txt",
        "LICENSE", "LICENSE.txt", "NOTICE", "NOTICE.txt",
    )
    licenseResources.forEach { append(it) }
    eachFile {
        if (licenseResources.any { it.equals(path, ignoreCase = true) }) {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }
    from(tasks.jar.map { zipTree(it.archiveFile.get().asFile) }) {
        include("META-INF/LICENSE.txt", "META-INF/NOTICE.txt", "META-INF/THIRD-PARTY-SOURCE-NOTICES.txt")
    }
    doFirst {
        val ui = layout.buildDirectory.dir("resources/main/ui").get().asFile
        check(ui.resolve("index.html").isFile && ui.resolve("dynamic-codec-schema.json").isFile) {
            "发布 JAR 缺少 UI 资源，请使用 -Pxtream.frontend.build.xtream-codec-studio-ui.enabled=true 构建"
        }
        check(ui.resolve("assets").walkTopDown().any { it.isFile && it.extension == "js" }) {
            "发布 JAR 缺少 UI 脚本，请重新构建前端"
        }
    }
}

tasks.test {
    exclude("**/CodecStudioFatJarTest.class")
}

tasks.register<Test>("fatJarTest") {
    description = "验证发布 JAR 的启动、资源、日志与工作区重启"
    group = "verification"
    dependsOn(executableJar)
    useJUnitPlatform()
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    include("**/CodecStudioFatJarTest.class")
    inputs.file(executableJar.flatMap { it.archiveFile })
    systemProperty("xtream.studio.test.jar", executableJar.get().archiveFile.get().asFile.absolutePath)
}

xtreamCodecFrontendBuild {
    enabled.set(xtreamConfig.buildCodecStudioUi)
    group.set("codec-studio-ui")
    description.set("通用编解码调试工作台")
    frontendProjectDir.set(layout.projectDirectory.dir("../xtream-codec-studio-ui"))
    backendStaticDir.set(layout.projectDirectory.dir("src/main/resources/ui"))
    cleanBackendStaticDir.set(true)
    additionalInputs.from(
        frontendProjectDir.file("index.html"),
        frontendProjectDir.file("pnpm-workspace.yaml"),
        frontendProjectDir.file("vite.config.ts"),
        frontendProjectDir.file("tsconfig.json"),
        frontendProjectDir.dir("test"),
        rootProject.file("xtream-codec-core/src/main/resources/io/github/hylexus/xtream/codec/core/dynamic/schema/dynamic-codec-schema.json")
    )
    buildCommand.set(
        """
        |pnpm install --frozen-lockfile
        |pnpm test
        |pnpm run build
        """.trimMargin()
    )
    commandLineConfigurer {
        environment("CI", "true")
        commandLine(XtreamCodecFrontendBuildPlugin.FrontendShellResolver.constructBuildCommand(buildCommand.get()))
    }
}
