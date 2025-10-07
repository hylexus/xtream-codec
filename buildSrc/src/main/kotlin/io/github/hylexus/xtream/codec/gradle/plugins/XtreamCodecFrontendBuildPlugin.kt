package io.github.hylexus.xtream.codec.gradle.plugins

import io.github.hylexus.xtream.codec.gradle.utils.*
import org.cadixdev.gradle.licenser.LicenseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
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

        // 延迟计算前端模块名，并清理非法字符
        val buildTaskNameProvider = extension.frontendProjectDir.map {
            val suffix = it.asFile.name
                .replace(Regex("[^a-zA-Z0-9_-]"), "-")
            "xtreamCodecFrontendBuild__${suffix}"
        }

        // 1. 构建前端任务
        // 注意这个任务是注册在 **rootProject** 下的
        val buildTask: Provider<Exec> = buildTaskNameProvider.flatMap { taskName ->
            if (project.rootProject.tasks.findByPath(taskName) != null) {
                // 如果任务已存在，直接使用
                val existed: TaskProvider<Exec> = project.rootProject.tasks.named(taskName, Exec::class.java)
                return@flatMap existed
            } else {
                // 否则注册新任务
                val newly: TaskProvider<Exec> = project.rootProject.tasks.register<Exec>(taskName) {
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

                    doFirst {
                        val desc = extension.shortName.get()
                        project.logSuccess3("Building frontend: $desc")
                        project.logTip("Working Directory : $workingDir")
                        project.logTip("Output Directory  : ${outputs.files.singleFile}")
                        project.logTip("Build Command     : ${extension.buildCommand.get()}")
                    }

                    doLast {
                        val desc = extension.shortName.get()
                        if (distDir.exists() && distDir.isDirectory && distDir.listFiles().isNotEmpty()) {
                            val fileCount = distDir.walk().count { it.isFile }
                            project.logSuccess2("Frontend build succeeded: $desc. Output: ${distDir.name}/ (${fileCount} files)")
                        }
                    }
                }
                return@flatMap newly
            }
        }

        // 2. 复制构建产物任务
        // 注意这个任务 **并不是** 注册在 rootProject 下的
        val copyTask = project.tasks.register<Copy>("copyFrontendDist") {
            group = extension.group.get()
            description = "复制前端构建产物: ${project.relativePath(extension.frontendDistDir.get())} --> ${project.relativePath(extension.backendStaticDir.get().asFile)}"
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
            // dependsOn(buildTask)
            dependsOn(copyTask)
        }

        // 4. License 插件集成
        project.plugins.withId("com.github.joschi.licenser") {
            project.afterEvaluate {
                val staticDir = extension.backendStaticDir.get().asFile
                project.logInfo("Excluding frontend build output from License checks: ${project.relativePath(staticDir)}(${project.name})")

                project.extensions.configure(LicenseExtension::class.java) {
                    exclude {
                        it.file.startsWith(staticDir)
                    }
                }
            }
        }
    }
}
