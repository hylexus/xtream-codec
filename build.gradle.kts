import io.github.hylexus.xtream.codec.gradle.utils.XtreamConfig.xtreamConfig
import io.github.hylexus.xtream.codec.gradle.utils.logInfo2
import io.github.hylexus.xtream.codec.gradle.utils.logTip
import net.ltgt.gradle.errorprone.errorprone
import java.security.MessageDigest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

plugins {
    id("java-library")
    id("io.spring.dependency-management")
    id("maven-publish")
    id("io.gitee.pkmer.pkmerboot-central-publisher") apply false
    id("signing")
    id("checkstyle")
    id("net.minecraftforge.licenser") apply false
    id("com.github.jk1.dependency-license-report")
    id("net.ltgt.errorprone") apply false
}

val mavenPublications = setOf(
    "xtream-codec-base",
    "xtream-codec-core",
    "xtream-codec-server-reactive",
    "jt-808-server-spring-boot-starter-reactive",
    "jt-808-server-dashboard-spring-boot-starter-reactive",
)
val errorpronePluginEnabledProjects = setOf(
    "xtream-codec-base",
    "xtream-codec-core",
    "xtream-codec-server-reactive",
    "jt-808-server-spring-boot-starter-reactive",
    "jt-808-server-dashboard-spring-boot-starter-reactive",
    "jt-1078-server-spring-boot-starter-reactive",
    "jt-1078-server-dashboard-spring-boot-starter-reactive",
)

version = xtreamConfig.projectVersion
run {
    xtreamConfig.javaVersion
    xtreamConfig.defaultSpringBootBomVersion
    xtreamConfig.defaultSpringCloudBomVersion
    xtreamConfig.needSign
}
val mavenRepoConfig = xtreamConfig.mavenRepoConfig

// 上游版权按审查记录中的比较版本保留，避免统一格式化覆盖派生源码声明。
val upstreamCopyrights = mapOf(
    "XtreamInternalStringUtils.java" to "Copyright 2002-2024 the original author or authors.",
    "XtreamVersion.java" to "Copyright 2002-2019 the original author or authors.",
    "XtreamMethodParameter.java" to "Copyright 2002-2023 the original author or authors.",
    "XtreamExchange.java" to "Copyright 2002-2023 the original author or authors.",
    "XtreamFilterChain.java" to "Copyright 2002-2015 the original author or authors.",
    "XtreamExchangeDecorator.java" to "Copyright 2002-2023 the original author or authors.",
    "XtreamSessionManager.java" to "Copyright 2002-2016 the original author or authors.",
    "XtreamHandler.java" to "Copyright 2002-2016 the original author or authors.",
    "XtreamFilter.java" to "Copyright 2002-2015 the original author or authors.",
    "ExceptionHandlingXtreamHandler.java" to "Copyright 2002-2023 the original author or authors.",
    "DispatcherXtreamHandler.java" to "Copyright 2002-2023 the original author or authors.",
    "DefaultXtreamFilterChain.java" to "Copyright 2002-2021 the original author or authors.",
    "FilteringXtreamHandler.java" to "Copyright 2002-2018 the original author or authors.",
    "XtreamDispatchExceptionHandler.java" to "Copyright 2002-2023 the original author or authors.",
    "XtreamRequestExceptionHandler.java" to "Copyright 2002-2016 the original author or authors.",
    "XtreamHandlerMapping.java" to "Copyright 2002-2021 the original author or authors.",
    "XtreamHandlerResultHandler.java" to "Copyright 2002-2022 the original author or authors.",
    "XtreamHandlerAdapter.java" to "Copyright 2002-2022 the original author or authors.",
    "XtreamHandlerResult.java" to "Copyright 2002-2022 the original author or authors.",
    "XtreamReactorThreadFactory.java" to "Copyright (c) 2018-2021 VMware Inc. or its affiliates, All Rights Reserved.",
    "XtreamHandlerMethod.java" to "Copyright 2002-2024 the original author or authors.",
    "XtreamMappingDescriptionProvider.java" to "Copyright 2012-2023 the original author or authors.",
    "DispatcherHandlerXtreamMappingDescriptionProvider.java" to "Copyright 2012-2023 the original author or authors.",
    "XtreamWebProxyHttpHeaderFilter.java" to "Copyright 2014-2023 the original author or authors.",
    "XtreamWebProxy.java" to "Copyright 2014-2023 the original author or authors.",
    "XtreamWebProxyUtils.java" to "Copyright 2014-2023 the original author or authors.",
    "XtreamWebProxyBuiltinFilters.java" to "Copyright 2014-2023 the original author or authors.",
    "XtreamWebProxyExchangeFilter.java" to "Copyright 2014-2023 the original author or authors.",
    "XtreamIntervalChecker.java" to "Copyright 2014-2023 the original author or authors.",
    "DefaultJt808DashboardMetricsService.java" to "Copyright 2017 VMware, Inc.",
)

val sourceLicenseHeaders = mapOf(
    "Message8105CodecSample.java" to listOf("MIT-SmallChi-JT808.txt"),
    "DefaultSpsDecoder.java" to listOf("MIT-SmallChi-JT1078.txt", "MIT-EyerH264Decoder.txt"),
    "AbstractG711AudioCodec.java" to listOf("MIT-maz-g711.txt"),
    "G711ALawJt1078AudioCodec.java" to listOf("MIT-maz-g711.txt"),
    "G711MuLawJt1078AudioCodec.java" to listOf("MIT-maz-g711.txt"),
    "DefaultPcmToMp3Encoder.java" to listOf("Apache-2.0-matrixy.txt"),
    "DefaultFlvEncoder.java" to listOf("Apache-2.0-matrixy.txt", "MIT-SmallChi-JT1078.txt"),
)
fun licenseSections(path: String): Map<String, String> =
    Regex("""(?ms)^===== BEGIN (.+?) =====\n(.*?)^===== END \1 =====$""")
        .findAll(file(path).readText())
        .associate { it.groupValues[1] to it.groupValues[2].trimEnd() }

val sourceLicenseTexts = licenseSections("licenses/source/open_source_licenses.txt")
val licenseHeaders = licenseSections("build-script/license/headers.txt")

// region Java
configure(subprojects) {

    version = xtreamConfig.projectVersion

    if (!isJavaProject(project)) {
        return@configure
    }
    logInfo2("configuring project: ${project.name}")

    pluginManager.apply("java-library")
    if (project.errorPronePluginEnabled()) {
        pluginManager.apply("net.ltgt.errorprone")
    }
    java {
        sourceCompatibility = JavaVersion.toVersion(xtreamConfig.javaVersion)
        targetCompatibility = JavaVersion.toVersion(xtreamConfig.javaVersion)
    }
    tasks.test {
        useJUnitPlatform()
        // https://github.com/gradle/gradle/issues/7773
        // systemProperties(System.getProperties().map { (k, v) -> k.toString() to v }.toMap())
    }
    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
        options.release.set(xtreamConfig.javaVersion.toInt())

        if (project.errorPronePluginEnabled()) {
            options.errorprone {
                check("NullAway", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
                option("NullAway:AnnotatedPackages", "io.github.hylexus.xtream.codec")
                option(
                    "NullAway:ExcludedClasses",
                    listOf(
                        "io.github.hylexus.xtream.codec.ext.jt808.builtin.messages",
                        "io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto",
                        "io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.vo",
                        "io.github.hylexus.xtream.codec.ext.jt1078.dashboard.domain.dto",
                        "io.github.hylexus.xtream.codec.ext.jt1078.dashboard.domain.vo",
                    ).joinToString(separator = ",")
                )
                disable(
                    "MissingSummary",
                    "StringCaseLocaleUsage",
                    "StringSplitter",
                    "UnnecessaryParentheses",
                    "ImmutableEnumChecker",
                    "DefaultCharset",
                    // todo: 有几个注解名和jdk类名冲突 后期考虑提供一个别名?
                    "JavaLangClash",
                    "ArrayRecordComponent",
                    "JavaTimeDefaultTimeZone",
                )
            }

            // Include to disable NullAway on test code
            if (name.lowercase(Locale.getDefault()).contains("test")) {
                options.errorprone {
                    disable("NullAway")
                    disable("IntLiteralCast")
                }
            }
        }
    }

    pluginManager.apply("io.spring.dependency-management")
    dependencyManagement {
        resolutionStrategy {
            cacheChangingModulesFor(0, TimeUnit.SECONDS)
        }
        applyMavenExclusions(false)
        generatedPomCustomization {
            enabled(false)
        }
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${xtreamConfig.defaultSpringBootBomVersion}")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${xtreamConfig.defaultSpringCloudBomVersion}")
        }

        dependencies {
            // 其他依赖版本都由上面的 mavenBom 控制
            // 这里指定 mavenBom 中没有包含的依赖版本
            dependency("io.github.classgraph:classgraph:4.8.174")
            dependency("org.bouncycastle:bcprov-jdk18on:1.78.1")
            dependency("pro.chenggang:mybatis-r2dbc-spring:3.0.5.RELEASE")
            dependency("cn.mybatis-mp:mybatis-mp-spring-boot-starter:1.7.8-spring-boot3")
            dependency("cn.mybatis-mp:mybatis-mp-datasource-routing:1.0.2")
            dependency("com.clickhouse:clickhouse-jdbc:0.7.2")
            dependency("com.clickhouse:clickhouse-client:0.7.1")
            dependency("com.clickhouse:clickhouse-r2dbc:0.7.1")
            dependency("io.minio:minio:8.5.14")
            dependency("com.lmax:disruptor:4.0.0")
            dependency("org.jspecify:jspecify:1.0.0")
            dependency("org.jetbrains:annotations:26.0.2")

            xtreamConfig.thirdpartyDependencies().forEach {
                dependency(it.toIdentifier())
            }
        }

        group = "xtream-codec"
        version = xtreamConfig.projectVersion
    }
    dependencies {
        // common start
        // 参考 https://github.com/hylexus/xtream-codec/issues/12
        // 发布到中央仓库的代码 禁止 使用 Lombok
        if (project.name !in mavenPublications && project.name !in setOf("jt-1078-server-dashboard-spring-boot-starter-reactive", "jt-1078-server-spring-boot-starter-reactive")) {
            compileOnly("org.projectlombok:lombok")
            annotationProcessor("org.projectlombok:lombok")
        }
        // 测试代码允许使用 Lombok
        testCompileOnly("org.projectlombok:lombok")
        testAnnotationProcessor("org.projectlombok:lombok")

        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        api("org.jspecify:jspecify")
        api("org.jetbrains:annotations")
        if (project.errorPronePluginEnabled()) {
            val errorprone = configurations.getByName("errorprone")
            errorprone("com.uber.nullaway:nullaway:0.12.10")
            errorprone("com.google.errorprone:error_prone_core:2.42.0")
        }

        // common end
    }

    pluginManager.apply("checkstyle")
    checkstyle {
        toolVersion = "10.23.0"
        configDirectory.set(rootProject.file("build-script/checkstyle/"))
    }
    tasks.withType<Checkstyle>().configureEach {
        javaLauncher = javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(xtreamConfig.javaVersion))
        }
    }
    tasks.withType<Checkstyle> {
        // 严重影响构建时间
        onlyIf {
            val skip = xtreamConfig.skipCheckStyle
            if (skip) {
                logTip("Disabling task [checkstyle] in project [${project.name}] (xtream.backend.build.checkstyle.enabled == false)")
            }
            return@onlyIf !skip
        }
    }

    // 本项目开源协议头
    if (xtreamConfig.licenseCheckerEnabled) {
        pluginManager.apply("net.minecraftforge.licenser")
        val creationYear = DateTimeFormatter.ofPattern("yyyy").format(LocalDate.now())
        configure<net.minecraftforge.licenser.LicenseExtension> {
            header.set(rootProject.resources.text.fromFile("build-script/license/license-header"))
            skipExistingHeaders.set(false)
            matching("**/AdpcmImaJt1078AudioCodec.java", closureOf<net.minecraftforge.licenser.LicenseProperties> {
                header.set(rootProject.resources.text.fromString(licenseHeaders.getValue("adpcm-header")))
            })
            matching("**/Jt1078ByteToMessageDecoder.java", closureOf<net.minecraftforge.licenser.LicenseProperties> {
                header.set(rootProject.resources.text.fromString(licenseHeaders.getValue("sky-header")))
            })
            upstreamCopyrights.forEach { (fileName, copyright) ->
                matching("**/$fileName", closureOf<net.minecraftforge.licenser.LicenseProperties> {
                    header.set(rootProject.resources.text.fromString(
                        "$copyright\n" + rootProject.file("build-script/license/license-header").readText().replaceFirst(
                            "Copyright 2024-present the original author or authors.",
                            "Modifications Copyright 2024-present the xtream-codec authors."
                        )
                    ))
                })
            }
            sourceLicenseHeaders.forEach { (fileName, licenses) ->
                matching("**/$fileName", closureOf<net.minecraftforge.licenser.LicenseProperties> {
                    val upstreamNotices = licenses.joinToString("\n\n") {
                        sourceLicenseTexts.getValue(it)
                    }
                    val modifications = rootProject.file("build-script/license/license-header").readText().replaceFirst(
                        "Copyright 2024-present the original author or authors.",
                        "Modifications Copyright 2024-present the xtream-codec authors."
                    )
                    header.set(rootProject.resources.text.fromString("$upstreamNotices\n\nProject modifications:\n$modifications"))
                })
            }
            exclude("**/spring.factories")
            exclude("**/org.springframework.boot.autoconfigure.AutoConfiguration.imports")
            properties {
                set("creationYear", creationYear)
            }
        }
    }

    pluginManager.apply("com.github.jk1.dependency-license-report")
    // 第三方依赖 license
    licenseReport {
        // By default, this plugin will collect the union of all licenses from
        // the immediate pom and the parent poms. If your legal team thinks this
        // is too liberal, you can restrict collected licenses to only include the
        // those found in the immediate pom file
        // Defaults to: true
        unionParentPomLicenses = true

        // Select projects to examine for dependencies.
        // Defaults to current project and all its subprojects
        // projects = project.subprojects.toTypedArray()
        projects = arrayOf(project)

        // Don't include artifacts of project's own group into the report
        excludeOwnGroup = true

        // Don't exclude bom dependencies.
        // If set to true, then all boms will be excluded from the report
        excludeBoms = true

        // excludes = mavenPublications.map { "xtream-codec:$it" }.toTypedArray()
        excludes = mavenPublications.flatMap { listOf("xtream-codec:$it", "xtream-codec.ext.jt:$it") }.toTypedArray()

        // Set output directory for the report data.
        // Defaults to ${project.buildDir}/reports/dependency-license.
        outputDir = "${project.layout.projectDirectory}/build/reports/dependency-license"

        // Set custom report renderer, implementing ReportRenderer.
        // Yes, you can write your own to support any format necessary.
        renderers = arrayOf<com.github.jk1.license.render.ReportRenderer>(
            com.github.jk1.license.render.TextReportRenderer("THIRD-PARTY-NOTICES.txt")
        )

        // This is for the allowed-licenses-file in checkLicense Task
        // Accepts File, URL or String path to local or remote file
        ////// ??? https://github.com/jk1/Gradle-License-Report/issues/252
        allowedLicensesFile = rootProject.file("build-script/license/allowed-licenses.json")
    }

    // 保留原检查入口，共用运行时依赖扫描及白名单，避免两套规则得到不同结果。
    tasks.register("enforceLicenses") {
        group = "verification"
        description = "使用统一白名单检查运行时依赖许可。"
        dependsOn(tasks.named("checkLicense"))
    }
}

tasks.register("enforceLicenses") {
    group = "verification"
    description = "检查所有 Java 子模块的运行时依赖许可。"
    dependsOn(subprojects.filter { isJavaProject(it) }.map { it.tasks.named("checkLicense") })
}
// endregion Java


// region Maven
configure(subprojects) {
    if (!isJavaProject(project)) {
        return@configure
    }

    normalization {
        runtimeClasspath {
            ignore("META-INF/MANIFEST.MF")
        }
    }

    val projectLicenseMaterials = copySpec {
        from(rootProject.file("LICENSE")) {
            into("META-INF")
            rename { "LICENSE.txt" }
        }
        from(rootProject.file("NOTICE")) {
            into("META-INF")
            rename { "NOTICE.txt" }
        }
        from(rootProject.file("THIRD-PARTY-SOURCE-NOTICES.txt")) {
            into("META-INF")
        }
        from(rootProject.file("licenses/source/open_source_licenses.txt")) {
            into("META-INF/licenses/source")
        }
        if (project.name == "jt-1078-server-spring-boot-starter-reactive") {
            from(rootProject.file("licenses/MPL-2.0.txt")) {
                into("META-INF/licenses")
            }
            from(project.file("src/main/java")) {
                include("**/AdpcmImaJt1078AudioCodec.java")
                includeEmptyDirs = false
                into("META-INF/third-party-sources/adpcm")
            }
        }
    }

    // 仅在执行相应打包任务时解析运行时图，源码版本与实际 jump3r 构件保持一致。
    val jump3rRuntime = providers.provider {
        configurations.getByName("runtimeClasspath").resolvedConfiguration.resolvedArtifacts.filter {
            it.moduleVersion.id.group == "de.sciss" && it.name == "jump3r"
        }
    }
    val jump3rMaterialsDir = layout.buildDirectory.dir("generated/jump3r-license-materials")
    val prepareJump3rLicenseMaterials = tasks.register("prepareJump3rLicenseMaterials") {
        inputs.files(jump3rRuntime.map { artifacts -> artifacts.map { it.file } })
        inputs.file(rootProject.file("licenses/jump3r/NOTICE.txt"))
        inputs.file(rootProject.file("build-script/license/jump3r.py"))
        inputs.file(rootProject.file("licenses/LGPL-2.1.txt"))
        outputs.dir(jump3rMaterialsDir)
        doLast {
            val destination = jump3rMaterialsDir.get().asFile
            delete(destination)
            destination.mkdirs()
            jump3rRuntime.get().forEach { artifact ->
                val libraryVersion = artifact.moduleVersion.id.version
                val sourceConfiguration = configurations.detachedConfiguration(
                    dependencies.create("de.sciss:jump3r:$libraryVersion:sources@jar")
                ).apply { isTransitive = false }
                val sourceArchive = sourceConfiguration.singleFile
                val materials = destination.resolve("META-INF/third-party-sources/jump3r")
                materials.mkdirs()
                copy {
                    from(rootProject.file("licenses/jump3r/NOTICE.txt"))
                    from(rootProject.file("build-script/license/jump3r.py"))
                    from(sourceArchive)
                    into(materials)
                }
                copy {
                    from(rootProject.file("licenses/LGPL-2.1.txt"))
                    into(destination.resolve("META-INF/licenses"))
                }
                fun sha256(file: File): String = MessageDigest.getInstance("SHA-256")
                    .digest(file.readBytes()).joinToString("") { "%02x".format(it) }
                materials.resolve("ARTIFACTS.txt").writeText(
                    "de.sciss:jump3r:$libraryVersion (unmodified)\n" +
                        "${artifact.file.name} SHA-256 ${sha256(artifact.file)}\n" +
                        "${sourceArchive.name} SHA-256 ${sha256(sourceArchive)}\n"
                )
            }
        }
    }

    // 应用共用一份许可汇总，仅追加实际 EPL 版本的源码地址。
    val applicationEplRuntime = providers.provider {
        configurations.getByName("runtimeClasspath").resolvedConfiguration.resolvedArtifacts.filter {
            "${it.moduleVersion.id.group}:${it.name}" in setOf(
                "ch.qos.logback:logback-core", "ch.qos.logback:logback-classic",
                "jakarta.annotation:jakarta.annotation-api", "org.aspectj:aspectjweaver"
            )
        }.sortedBy { it.moduleVersion.id.toString() }
    }
    val applicationMaterialsDir = layout.buildDirectory.dir("generated/application-license-materials")
    val prepareApplicationLicenseMaterials = tasks.register("prepareApplicationLicenseMaterials") {
        inputs.files(applicationEplRuntime.map { artifacts -> artifacts.map { it.file } })
        inputs.file(rootProject.file("licenses/jvm/open_source_licenses.txt"))
        outputs.dir(applicationMaterialsDir)
        doLast {
            val destination = applicationMaterialsDir.get().asFile
            delete(destination)
            val metaInf = destination.resolve("META-INF").apply { mkdirs() }
            val notice = StringBuilder(rootProject.file("licenses/jvm/open_source_licenses.txt").readText(Charsets.UTF_8))
            notice.append("\n实际 EPL 依赖的源码获取地址\nSource availability for bundled EPL dependencies\n\n")
            applicationEplRuntime.get().forEach { artifact ->
                val id = artifact.moduleVersion.id
                val base = "https://repo.maven.apache.org/maven2/${id.group.replace('.', '/')}/${artifact.name}/${id.version}"
                notice.append("$id\n源码地址 / Source URL: $base/${artifact.name}-${id.version}-sources.jar\n\n")
            }
            metaInf.resolve("open_source_licenses.txt").writeText(notice.toString(), Charsets.UTF_8)
        }
    }

    tasks.jar {
        with(projectLicenseMaterials)
        manifest {
            manifest.attributes["Implementation-Title"] = project.name
            manifest.attributes["Implementation-Version"] = xtreamConfig.projectVersion
            manifest.attributes["Automatic-Module-Name"] = project.name.replace('-', '.')
            manifest.attributes["Created-By"] = "${System.getProperty("java.version")} (${System.getProperty("java.vendor")})"
            manifest.attributes["X-Requires-Java-Version"] = xtreamConfig.javaVersion.toInt()
        }
    }

    tasks.withType<Jar>().matching { it.name in setOf("bootJar", "shadowJar") }.configureEach {
        dependsOn(prepareApplicationLicenseMaterials, prepareJump3rLicenseMaterials)
        from(applicationMaterialsDir)
        from(jump3rMaterialsDir)
        if (name == "bootJar") {
            with(projectLicenseMaterials)
        }
    }

    tasks.javadoc {
        description = "Generates project-level javadoc for use in -javadoc jar"
        options.encoding = "UTF-8"
        options.memberLevel = JavadocMemberLevel.PROTECTED
        options.header = project.name
        options.source = "21"

        val docletOptions = options as StandardJavadocDocletOptions
        docletOptions.addBooleanOption("html5", true)
        docletOptions.version(true)
        docletOptions.links("https://docs.oracle.com/en/java/javase/21/docs/api")
        docletOptions.charSet("UTF-8")
        docletOptions.use(true)
        docletOptions.addStringOption("Xdoclint:none", "-quiet")

        isFailOnError = false
        version = xtreamConfig.projectVersion
        logging.captureStandardError(LogLevel.INFO)
        logging.captureStandardOutput(LogLevel.INFO)
    }

    val sourcesJar = tasks.register<Jar>("sourcesJar") {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles a jar archive containing the main source files."
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveClassifier.set("sources")
        with(projectLicenseMaterials)
        from(sourceSets.getByName("main").java.srcDirs)
    }

    val javaDocJar = tasks.register<Jar>("javaDocJar") {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles a jar archive containing the generated Javadoc."
        archiveClassifier.set("javadoc")
        with(projectLicenseMaterials)
        from(tasks.named("javadoc"))
    }

    pluginManager.apply("maven-publish")
    val stagingRepositoryPath = xtreamConfig.centralPortalArtifactsTempDir + "/${project.name}"
    if (isMavenPublications()) {
        if (xtreamConfig.centralPortalMavenRepoEnabled) {
            pluginManager.apply("io.gitee.pkmer.pkmerboot-central-publisher")
            tasks.withType<io.gitee.pkmer.tasks.BundleTask>().configureEach {
                dependsOn(tasks.test, tasks.checkstyleTest, tasks.checkstyleMain)
                // 只有部分模块有这两个任务
                dependsOn(tasks.matching { it.name in setOf("compileJmhJava", "checkstyleJmh") })
            }
            // 延迟配置，在插件完全应用后再执行
            afterEvaluate {
                project.extensions.findByType<io.gitee.pkmer.extension.PkmerBootPluginExtension>()?.apply {
                    sonatypeMavenCentral {
                        stagingRepository.set(file(stagingRepositoryPath))
                        username.set(mavenRepoConfig.getProperty("maven-central-portal.username"))
                        password.set(mavenRepoConfig.getProperty("maven-central-portal.password"))
                        publishingType.set(io.gitee.pkmer.enums.PublishingType.USER_MANAGED)
                    }
                }
            }
        }

        publishing {
            publications {
                create<MavenPublication>("maven") {

                    from(components["java"])
                    artifact(sourcesJar)
                    artifact(javaDocJar)

                    groupId = xtreamConfig.projectGroupId
                    artifactId = project.name
                    version = xtreamConfig.projectVersion

                    pom {
                        packaging = "jar"
                        description.set(project.name)
                        name.set(project.name)
                        url.set(xtreamConfig.projectHomePage)

                        licenses {
                            license {
                                name.set(xtreamConfig.projectLicenseName)
                                url.set(xtreamConfig.projectLicenseUrl)
                            }
                        }

                        developers {
                            developer {
                                id.set(xtreamConfig.projectDeveloperId)
                                name.set(xtreamConfig.projectDeveloperName)
                                email.set(xtreamConfig.projectDeveloperEmail)
                            }
                        }

                        versionMapping {
                            usage("java-api") {
                                fromResolutionOf("runtimeClasspath")
                            }
                            usage("java-runtime") {
                                fromResolutionResult()
                            }
                        }

                        scm {
                            url.set(xtreamConfig.projectScmUrl)
                            connection.set(xtreamConfig.projectScmConnection)
                            developerConnection.set(xtreamConfig.projectScmDeveloperConnection)
                        }

                        issueManagement {
                            system.set(xtreamConfig.projectIssueManagementSystem)
                            url.set(xtreamConfig.projectIssueManagementUrl)
                        }
                    }

                    repositories {
                        // 1. 发布到你自己的私有仓库
                        if (xtreamConfig.privateMavenRepoEnabled) {
                            maven {
                                name = "private"
                                url = uri(mavenRepoConfig.getProperty("privateRepo-release.url"))
                                credentials {
                                    username = mavenRepoConfig.getProperty("privateRepo-release.username")
                                    password = mavenRepoConfig.getProperty("privateRepo-release.password")
                                }
                            }
                        }
                        // 2. 发布到 GitHub Packages
                        if (xtreamConfig.githubMavenRepoEnabled) {
                            maven {
                                name = "GitHubPackages"
                                url = uri(mavenRepoConfig.getProperty("github-pkg.url"))
                                credentials {
                                    username = System.getenv("GITHUB_ACTOR")
                                        ?: System.getProperty("gpr.user")
                                                ?: mavenRepoConfig.getProperty("github-pkg.username")

                                    password = System.getenv("GITHUB_TOKEN")
                                        ?: System.getProperty("gpr.key")
                                                ?: mavenRepoConfig.getProperty("github-pkg.password")
                                }
                            }
                        }
                        // 3. 发布到 Maven 中央仓库
                        // 已废弃: 新版中央仓库发版参考 io.gitee.pkmer.pkmerboot-central-publisher
//                        maven {
//                            name = "centralPortal"
//                            url = uri(mavenRepoConfig.getProperty("sonatype-staging.url"))
//                            credentials {
//                                username = mavenRepoConfig.getProperty("sonatype-staging.username")
//                                password = mavenRepoConfig.getProperty("sonatype-staging.password")
//                            }
//                        }

                        maven {
                            name = "centralPortalLocalArtifacts"
                            // Specify the local staging repo path in the configuration.
                            url = uri(stagingRepositoryPath)
                        }
                    }
                }
            }

        }

        if (xtreamConfig.needSign) {
            pluginManager.apply("signing")
            signing {
                // 如果需要签名
                // 记得将 build-script/gradle/debug-template.gradle.properties 中的 gpg 配置放到 ~/.gradle/gradle.properties
                sign(publishing.publications["maven"])
            }
        }
    }

}
// endregion Maven

// region updateVersion
/**
 * 以 gradle.properties 中的 projectVersion 为单一来源，同步以下文件：
 *   1. package.json 的 "version" 字段
 *   2. docs/src 中 Maven/Gradle 依赖版本号（仅含 io.github.hylexus.xtream 坐标的）
 * 跳过 release-notes/ 目录（历史版本标题保留不动）。
 */
val semverRegex = Regex("""\d+\.\d+\.\d+(-[a-zA-Z]\w*(\.\w+)*)?""")

tasks.register("updateVersion") {
    group = "release"
    description = "同步 projectVersion 到 package.json 和文档中的依赖版本号（不含 release-notes）"

    doLast {
        val newVersion = xtreamConfig.projectVersion.trim()
        println("=== updateVersion: $newVersion ===")

        // 1. package.json —— 更新 "version" 字段
        fileTree(projectDir) {
            include("**/package.json")
            exclude("**/node_modules/**")
        }.forEach { pkg ->
            val text = pkg.readText()
            val updated = text.replace(
                Regex(""""version":\s*"\d+\.\d+\.\d+(-[a-zA-Z]\w*(\.\w+)*)?""" + "\""),
                "\"version\": \"$newVersion\""
            )
            if (updated != text) {
                pkg.writeText(updated)
                println("  [package.json] ${relativePath(pkg)}")
            }
        }

        // 2. docs 中的版本号 —— 仅替换项目自身 Maven/Gradle 依赖版本
        fileTree("$projectDir/docs/src") {
            include("**/*.md")
            exclude("**/release-notes/**", "**/node_modules/**")
        }.forEach { doc ->
            val text = doc.readText()
            var changed = false

            // Maven: <version>X.Y.Z</version> 仅当前面有项目 groupId 时替换
            val afterMaven = text.replace(
                Regex(
                    """(io\.github\.hylexus\.xtream[\w-]*[\s\S]{0,300}?<version>)$semverRegex(</version>)""",
                    setOf(RegexOption.DOT_MATCHES_ALL)
                )
            ) { "${it.groupValues[1]}$newVersion${it.groupValues[4]}" }
            if (afterMaven != text) changed = true

            // Gradle: io.github.hylexus.xtream:artifactId:X.Y.Z
            val afterGradle = afterMaven.replace(
                Regex("(io\\.github\\.hylexus\\.xtream:[\\w-]+:)$semverRegex")
            ) { "${it.groupValues[1]}$newVersion" }
            if (afterGradle != afterMaven) changed = true

            if (changed) {
                doc.writeText(afterGradle)
                println("  [docs       ] ${relativePath(doc)}")
            }
        }

        println("=== updateVersion done ===")
    }
}
// endregion updateVersion

fun isJavaProject(project: Project): Boolean {
    return project != rootProject
            && (
            mavenPublications.contains(project.name)
                    || setOf(
                "xtream-codec-core-debug",
                "xtream-codec-studio-app",
                "xtream-codec-server-reactive-debug-tcp",
                "xtream-codec-server-reactive-debug-udp",
                "jt-808-server-spring-boot-starter-reactive-debug",
                "jt-808-attachment-server-quick-start-blocking",
                "jt-808-attachment-server-quick-start-nonblocking",
                "jt-808-server-quick-start",
                "jt-808-server-quick-start-with-dashboard",
                "jt-808-server-quick-start-with-storage-nonblocking",
                "jt-808-server-quick-start-with-storage-blocking",
                "jt-1078-server-spring-boot-starter-reactive",
                "jt-1078-server-dashboard-spring-boot-starter-reactive",
                "jt-1078-server-spring-boot-starter-reactive-debug",
                "jt-1078-server-quick-start-nonblocking",
                "jt-1078-server-quick-start-blocking",
                "custom-annotation-server",
            ).contains(project.name))
}


fun Project.isMavenPublications(): Boolean {
    return mavenPublications.contains(project.name)
}

private fun Project.errorPronePluginEnabled(): Boolean {
    return xtreamConfig.errorproneEnabled && project.name in errorpronePluginEnabledProjects
}
