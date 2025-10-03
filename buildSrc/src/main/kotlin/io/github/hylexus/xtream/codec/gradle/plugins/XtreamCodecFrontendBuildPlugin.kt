package io.github.hylexus.xtream.codec.gradle.plugins

import io.github.hylexus.xtream.codec.gradle.utils.logInfo
import io.github.hylexus.xtream.codec.gradle.utils.logInfo2
import org.cadixdev.gradle.licenser.LicenseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register

/**
 * 前端构建插件
 * - 支持多模块配置不同前端目录
 * - 自动注册 buildFrontend / copyFrontendDist 任务
 * - 自动集成 License 插件（排除前端静态资源）
 *
 * @author hylexus
 */
class XtreamCodecFrontendBuildPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "xtreamCodecFrontendBuild",
            XtreamCodecFrontendBuildExtension::class.java
        )

        // 1. 构建前端任务
        val buildTask = project.tasks.register<Exec>("buildFrontend") {
            group = extension.group.get()
            description = "构建前端项目: ${extension.description.get()}"
            onlyIf { extension.enabled.get() }

            val projectDir = extension.frontendProjectDir.get().asFile
            val distDir = extension.frontendDistDir.get().asFile

            workingDir = projectDir
            environment("VITE_BASE_PATH", extension.frontendBasePath.get())
            commandLine("sh", "-c", extension.buildCommand.get())

            // 前端项目发生变化才执行
            inputs.dir(projectDir.resolve("src"))
            inputs.file(projectDir.resolve("package.json"))
            inputs.file(projectDir.resolve("pnpm-lock.yaml"))
            outputs.dir(distDir)
        }


        // 2. 复制构建产物任务
        val copyTask = project.tasks.register<Copy>("copyFrontendDist") {
            group = extension.group.get()
            description = "复制前端构建产物: ${extension.description.get()}"
            onlyIf { extension.enabled.get() }

            val distDir = extension.frontendDistDir.get().asFile
            val targetDir = extension.backendStaticDir.get().asFile

            // 声明输入输出，Gradle 会根据时间戳/签名来判断是否需要执行
            inputs.dir(distDir)
            outputs.dir(targetDir)

            from(distDir)
            into(targetDir)
            include("**/*")

            doFirst {
                if (extension.cleanBackendStaticDir.get()) {
                    project.logInfo2("Cleaning backend static dir: $targetDir")
                    project.delete(targetDir)
                }
                if (extension.createBackendStaticDirIfMissing.get()) {
                    if (!targetDir.exists()) {
                        project.logInfo2("Creating backend static dir: $targetDir")
                        targetDir.mkdirs()
                    }
                }
            }
        }
        copyTask.configure {
            dependsOn(buildTask)
        }

        // 3. 任务依赖集成
        project.tasks.named("processResources") {
            dependsOn(copyTask)
        }
        project.tasks.named("build") {
            dependsOn(buildTask)
            dependsOn(copyTask)
        }

        // 4. License 插件集成
        project.plugins.withId("com.github.joschi.licenser") {
            project.afterEvaluate {
                val staticDir = extension.backendStaticDir.get().asFile
                project.logInfo("Excluding frontend build output from License checks: $staticDir")

                project.extensions.configure(LicenseExtension::class.java) {
                    exclude {
                        it.file.startsWith(staticDir)
                    }
                }
            }
        }
    }
}
