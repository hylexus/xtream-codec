import org.cadixdev.gradle.licenser.LicenseExtension

dependencies {

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // common start
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // common end

    api(project(":xtream-codec-base"))
    api(project(":ext:jt:jt-808-server-spring-boot-starter-reactive"))

    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.springframework.boot:spring-boot-starter-validation")

    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    // runtime
    implementation("jakarta.annotation:jakarta.annotation-api")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core")
}

val jt808DashboardUiStaticDir = project.file("src/main/resources/static")
// 前端打包生成的文件 不检测 License
extensions.configure(LicenseExtension::class.java) {
    exclude {
        it.file.startsWith(jt808DashboardUiStaticDir)
    }
}

// ./gradlew clean build -P buildJt808DashboardUi=true
val buildJt808DashboardUi = getConfigAsBoolean("buildJt808DashboardUi") || project.findProperty("buildJt808DashboardUi") == "true"
val dashboardUiDir = file("../jt-808-server-dashboard-ui")
val jt808DashboardUiGroup = "jt808-dashboard-ui"

tasks.register<Exec>("buildJt808DashboardUi") {
    onlyIf { buildJt808DashboardUi }
    group = jt808DashboardUiGroup
    description = "构建jt808-dashboard-ui"
    workingDir = file(dashboardUiDir)
    // @see io.github.hylexus.xtream.codec.ext.jt808.dashboard.boot.properties.XtreamJt808ServerDashboardProperties.basePath
    environment("VITE_BASE_PATH", "/dashboard-ui/")
    commandLine(
        "sh", "-c",
        """
        pnpm install --registry https://registry.npmmirror.com \
        && pnpm run build --mode production
        """.trimIndent()
    )
}

tasks.register<Copy>("copyJt808DashboardUiDist") {
    onlyIf { buildJt808DashboardUi }
    group = jt808DashboardUiGroup
    description = "复制jt808-dashboard-ui构建输出"
    from("${dashboardUiDir}/dist")
    into("src/main/resources/static/dashboard/808/")
    include("**/*")

    doFirst {
        delete("src/main/resources/static/dashboard/808/")
    }

    // 始终重新执行任务
    outputs.upToDateWhen { false }
}

tasks.named("processResources").configure {
    if (buildJt808DashboardUi) {
        dependsOn(tasks.named("copyJt808DashboardUiDist"))
    }
}

tasks.named("build").configure {
    if (buildJt808DashboardUi) {
        dependsOn(tasks.named("buildJt808DashboardUi"))
        dependsOn(tasks.named("copyJt808DashboardUiDist"))
    }
}

fun getConfigAsBoolean(key: String) = project.ext.get(key)?.toString()?.toBoolean() ?: false
