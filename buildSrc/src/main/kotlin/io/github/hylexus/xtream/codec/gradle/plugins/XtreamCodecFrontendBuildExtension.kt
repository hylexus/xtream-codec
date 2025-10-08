package io.github.hylexus.xtream.codec.gradle.plugins

import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecSpec
import javax.inject.Inject

/**
 * 前端构建扩展配置
 *
 * 每个子项目都可以单独配置自己的前端项目路径、构建输出、目标静态目录等。
 */
open class XtreamCodecFrontendBuildExtension @Inject constructor(objects: ObjectFactory) {

    /** 是否启用前端构建 */
    val enabled: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    /** 分组 */
    val group: Property<String> = objects.property(String::class.java).convention("frontend")

    /** 描述信息 */
    val description: Property<String> = objects.property(String::class.java).convention("构建前端项目")

    /** 前端项目所在目录（package.json 所在目录） */
    val frontendProjectDir: DirectoryProperty = objects.directoryProperty()

    /** 前端构建输出目录（例如 dist） */
    val frontendDistDir: DirectoryProperty = objects.directoryProperty().convention(
        frontendProjectDir.map { it.dir("dist") }
    )

    /** 后端静态资源目录（前端构建产物要复制到这里） */
    val backendStaticDir: DirectoryProperty = objects.directoryProperty()

    /** Vite base path，例如 `/dashboard-ui/` */
    val frontendBasePath: Property<String> = objects.property(String::class.java).convention("/")

    /** 前端项目短名称(仅仅用于日志输出) */
    val shortName: Property<String> = objects.property(String::class.java).convention(
        frontendProjectDir.map { it.asFile.name }
    )

    /** 构建命令，可以自定义（默认 pnpm） */
    val buildCommand: Property<String> = objects.property(String::class.java).convention(
        """
        |pnpm install --registry https://registry.npmmirror.com
        |pnpm run build --mode production
        """.trimMargin()
    )

    /**
     * 自定义前端构建命令执行逻辑。
     * 用户可以通过 DSL 自己配置 ExecSpec，比如使用 WSL、PowerShell、cmd 等。
     */
    val commandLineConfigurer: Property<Action<ExecSpec>> = objects.property()

    /**
     * Kotlin DSL 便捷方法，允许用户用 lambda 配置 commandLineConfigurer。
     *
     * 例如：
     *
     * - Shell:
     *
     * ```sh
     * xtreamCodecFrontendBuild {
     *     commandLineConfigurer {
     *         commandLine(
     *             "sh", "-c",
     *             """
     *             set -e
     *             pnpm install --registry https://registry.npmmirror.com
     *             pnpm run build --mode production
     *             """.trimIndent()
     *         )
     *     }
     * }
     * ```
     *
     * - WSL：
     *
     * ```kotlin
     * xtreamCodecFrontendBuild {
     *     commandLineConfigurer {
     *         commandLine("wsl", "bash", "-c", "echo Hello")
     *     }
     * }
     * ```
     * - PowerShell：
     * ```kotlin
     * xtreamCodecFrontendBuild {
     *     commandLineConfigurer {
     *         commandLine("powershell", "-Command", """
     *             $ErrorActionPreference = 'Stop'
     *             pnpm install
     *             pnpm run build
     *         """.trimIndent())
     *      }
     * }
     * ```
     */
    fun commandLineConfigurer(configure: ExecSpec.() -> Unit) {
        commandLineConfigurer.set(Action(configure))
    }

    /** 复制前是否清空目标目录 */
    val cleanBackendStaticDir: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    /** 如果目标目录不存在，是否自动创建 */
    val createBackendStaticDirIfMissing: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

}
