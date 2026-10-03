import io.github.hylexus.xtream.codec.gradle.plugins.XtreamCodecFrontendBuildPlugin
import io.github.hylexus.xtream.codec.gradle.utils.XtreamConfig.xtreamConfig

plugins {
    application
    id("xtream-codec-frontend-build-plugin")
}

dependencies {
    implementation(project(":xtream-codec-core"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
}

application {
    mainClass.set("io.github.hylexus.xtream.codec.debugger.app.CodecDebuggerApplication")
}

xtreamCodecFrontendBuild {
    enabled.set(xtreamConfig.buildCodecDebuggerUi)
    group.set("codec-debugger-ui")
    description.set("通用编解码调试工作台")
    frontendProjectDir.set(layout.projectDirectory.dir("../xtream-codec-debugger-ui"))
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
