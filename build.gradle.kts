import io.github.hylexus.xtream.codec.gradle.utils.XtreamConfig.xtreamConfig
import io.github.hylexus.xtream.codec.gradle.utils.logInfo2
import io.github.hylexus.xtream.codec.gradle.utils.logTip
import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
    id("java-library")
    id("io.spring.dependency-management")
    id("maven-publish")
    id("io.gitee.pkmer.pkmerboot-central-publisher") apply false
    id("signing")
    id("checkstyle")
    id("net.minecraftforge.licenser")
    id("com.github.jk1.dependency-license-report")
    id("com.namics.oss.gradle.license-enforce-plugin")
}

val mavenPublications = setOf(
    "xtream-codec-base",
    "xtream-codec-core",
    "xtream-codec-server-reactive",
    "jt-808-server-spring-boot-starter-reactive",
    "jt-808-server-dashboard-spring-boot-starter-reactive",
)

version = xtreamConfig.projectVersion
run {
    xtreamConfig.javaVersion
    xtreamConfig.defaultSpringBootBomVersion
    xtreamConfig.defaultSpringCloudBomVersion
    xtreamConfig.needSign
}
val mavenRepoConfig = xtreamConfig.mavenRepoConfig

// region Java
configure(subprojects) {

    version = xtreamConfig.projectVersion

    if (!isJavaProject(project)) {
        return@configure
    }
    logInfo2("configuring project: ${project.name}")

    apply(plugin = "java-library")
    java {
        sourceCompatibility = JavaVersion.toVersion(xtreamConfig.javaVersion)
        targetCompatibility = JavaVersion.toVersion(xtreamConfig.javaVersion)
    }
    tasks.test {
        useJUnitPlatform()
    }
    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
        options.release.set(xtreamConfig.javaVersion.toInt())
    }

    apply(plugin = "io.spring.dependency-management")
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
        }

        group = "xtream-codec"
        version = xtreamConfig.projectVersion
    }
    dependencies {
        // common start
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        testCompileOnly("org.projectlombok:lombok")
        testAnnotationProcessor("org.projectlombok:lombok")

        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        api("org.jspecify:jspecify")
        // common end
    }

    apply(plugin = "checkstyle")
    checkstyle {
        toolVersion = "10.23.0"
        configDirectory.set(rootProject.file("build-script/checkstyle/"))
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
    apply(plugin = "net.minecraftforge.licenser")
    configure<net.minecraftforge.licenser.LicenseExtension> {
        setHeader( rootProject.file("build-script/license/license-header"))
        skipExistingHeaders = false
        exclude("**/spring.factories")
        exclude("**/org.springframework.boot.autoconfigure.AutoConfiguration.imports")
    }

    apply(plugin = "com.github.jk1.dependency-license-report")
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
//        projects = project.subprojects.toTypedArray()
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
        renderers = arrayOf(com.github.jk1.license.render.TextReportRenderer("THIRD-PARTY-NOTICES.txt"))

        // This is for the allowed-licenses-file in checkLicense Task
        // Accepts File, URL or String path to local or remote file
        ////// ??? https://github.com/jk1/Gradle-License-Report/issues/252
        allowedLicensesFile = rootProject.file("build-script/license/allowed-licenses.json")
    }

    apply(plugin = "com.namics.oss.gradle.license-enforce-plugin")
    tasks.enforceLicenses {
        allowedCategories = listOf("Apache", "MIT")
        allowedLicenses = listOf("Mulan Permissive Software License, Version 2")
    }
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

    tasks.jar {
        dependsOn("generateLicenseReport")
        manifest {
            manifest.attributes["Implementation-Title"] = project.name
            manifest.attributes["Implementation-Version"] = xtreamConfig.projectVersion
            manifest.attributes["Automatic-Module-Name"] = project.name.replace('-', '.')
            manifest.attributes["Created-By"] = "${System.getProperty("java.version")} (${System.getProperty("java.vendor")})"
            manifest.attributes["X-Requires-Java-Version"] = xtreamConfig.javaVersion.toInt()
        }

        from(rootProject.projectDir) {
            include("LICENSE")
            into("META-INF")
            rename("LICENSE", "LICENSE.txt")
            // https://docs.gradle.org/current/userguide/working_with_files.html#sec:filtering_files
            expand(
                "copyright" to LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy")),
                "version" to xtreamConfig.projectVersion,
            )
        }

        from(project.projectDir.absolutePath + "/build/reports/dependency-license/") {
            include("THIRD-PARTY-NOTICES.txt")
            into("META-INF")
            rename("THIRD-PARTY-NOTICES.txt", "NOTICE.txt")
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

    val sourcesJar by tasks.registering(Jar::class) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveClassifier.set("sources")
        from(sourceSets.getByName("main").java.srcDirs)
    }

    val javaDocJar by tasks.registering(Jar::class) {
        archiveClassifier.set("javadoc")
        from(tasks.named("javadoc"))
    }

    apply(plugin = "maven-publish")
    val stagingRepositoryPath = "/tmp/gradle/maven-tmp"
    if (isMavenPublications(project)) {
        if (xtreamConfig.centalPortalMavenRepoEnabled) {
            apply(plugin = "io.gitee.pkmer.pkmerboot-central-publisher")
            tasks.withType<io.gitee.pkmer.tasks.BundleTask>().configureEach {
                dependsOn(tasks.test, tasks.checkstyleTest, tasks.checkstyleMain)
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
                            name = "localTmp"
                            // Specify the local staging repo path in the configuration.
                            url = uri(stagingRepositoryPath)
                        }
                    }
                }
            }

        }

        if (xtreamConfig.needSign) {
            apply(plugin = "signing")
            signing {
                // 如果需要签名
                // 记得将 build-script/gradle/debug-template.gradle.properties 中的 gpg 配置放到 ~/.gradle/gradle.properties
                sign(publishing.publications["maven"])
            }
        }
    }

}
// endregion Maven

fun isJavaProject(project: Project): Boolean {
    return project != rootProject
            && (
            mavenPublications.contains(project.name)
                    || setOf(
                "xtream-codec-core-debug",
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
            ).contains(project.name))
}


fun isMavenPublications(project: Project): Boolean {
    return mavenPublications.contains(project.name)
}
