package io.github.hylexus.xtream.codec.gradle.utils

import io.github.hylexus.xtream.codec.gradle.utils.XtreamLoggerConfig.isLogEnabled
import org.gradle.api.Project

/**
 * Xtream 自定义日志工具
 *
 * - 日志等级：success/info/warning/error/debug
 * - debug 默认关闭，可通过 Gradle property 或系统 property 开启
 * - 支持图标 + ANSI 颜色
 */
object XtreamLoggerConfig {
    private const val PREFIX = "xtream.log."

    private val defaultEnabled = mapOf(
        "success" to true,
        "info" to true,
        "warning" to true,
        "error" to true,
        "debug" to false
    )

    fun Project.isLogEnabled(level: String): Boolean {
        val key = "$PREFIX$level"
        return try {
            val prop = (findProperty(key)?.toString()
                ?: System.getProperty(key))?.toBoolean()
            prop ?: defaultEnabled[level] ?: false
        } catch (_: Exception) {
            defaultEnabled[level] ?: false
        }
    }
}

private object XtreamLoggerIcons {
    const val SUCCESS = "[✅ XTREAM-SUCCESS]"
    const val SUCCESS_2 = "[\uD83D\uDE80 XTREAM-SUCCESS]"
    const val INFO = "[ℹ️ XTREAM-INFO]"
    const val INFO_2 = "[\uD83D\uDCA1 XTREAM-INFO]"
    const val INFO_3 = "[\uD83D\uDCA1 XTREAM-TIP]"
    const val INFO_4 = "[\uD83D\uDD14 XTREAM-TIP]"
    const val WARNING = "[⚠️ XTREAM-WARNING]"
    const val WARNING_2 = "[\uD83D\uDD14 XTREAM-WARNING]"
    const val ERROR = "[❌ XTREAM-ERROR]"
    const val ERROR_2 = "[\uD83D\uDED1 XTREAM-ERROR]"
    const val DEBUG = "[🐞 XTREAM-DEBUG]"
}

private object XtreamLoggerColors {
    const val RESET = "\u001B[0m"
    const val GREEN = "\u001B[32m"
    const val BLUE = "\u001B[34m"
    const val YELLOW = "\u001B[33m"
    const val RED = "\u001B[31m"
    const val CYAN = "\u001B[36m"
}

// -------------------- 扩展方法 --------------------
@Suppress("unused")
fun Project.logSuccess(msg: String) {
    if (isLogEnabled("success")) logger.lifecycle("${XtreamLoggerColors.GREEN}${XtreamLoggerIcons.SUCCESS} $msg${XtreamLoggerColors.RESET}")
}

@Suppress("unused")
fun Project.logSuccess2(msg: String) {
    if (isLogEnabled("success")) logger.lifecycle("${XtreamLoggerColors.GREEN}${XtreamLoggerIcons.SUCCESS_2} $msg${XtreamLoggerColors.RESET}")
}

fun Project.logInfo(msg: String) {
    if (isLogEnabled("info")) logger.lifecycle("${XtreamLoggerColors.BLUE}${XtreamLoggerIcons.INFO} $msg${XtreamLoggerColors.RESET}")
}

fun Project.logInfo2(msg: String) {
    if (isLogEnabled("info")) logger.lifecycle("${XtreamLoggerColors.BLUE}${XtreamLoggerIcons.INFO_2} $msg${XtreamLoggerColors.RESET}")
}

fun Project.logTip(msg: String) {
    if (isLogEnabled("info")) logger.lifecycle("${XtreamLoggerColors.BLUE}${XtreamLoggerIcons.INFO_3} $msg${XtreamLoggerColors.RESET}")
}

fun Project.logTip2(msg: String) {
    if (isLogEnabled("info")) logger.lifecycle("${XtreamLoggerColors.GREEN}${XtreamLoggerIcons.INFO_4} $msg${XtreamLoggerColors.RESET}")
}

@Suppress("unused")
fun Project.logWarning(msg: String) {
    if (isLogEnabled("warning")) logger.lifecycle("${XtreamLoggerColors.YELLOW}${XtreamLoggerIcons.WARNING} $msg${XtreamLoggerColors.RESET}")
}

fun Project.logWarning2(msg: String) {
    if (isLogEnabled("warning")) logger.lifecycle("${XtreamLoggerColors.YELLOW}${XtreamLoggerIcons.WARNING_2} $msg${XtreamLoggerColors.RESET}")
}

@Suppress("unused")
fun Project.logError(msg: String) {
    if (isLogEnabled("error")) logger.lifecycle("${XtreamLoggerColors.RED}${XtreamLoggerIcons.ERROR} $msg${XtreamLoggerColors.RESET}")
}

@Suppress("unused")
fun Project.logError2(msg: String) {
    if (isLogEnabled("error")) logger.lifecycle("${XtreamLoggerColors.RED}${XtreamLoggerIcons.ERROR_2} $msg${XtreamLoggerColors.RESET}")
}

fun Project.logDebug(msg: String) {
    if (isLogEnabled("debug")) logger.lifecycle("${XtreamLoggerColors.CYAN}${XtreamLoggerIcons.DEBUG} $msg${XtreamLoggerColors.RESET}")
}
