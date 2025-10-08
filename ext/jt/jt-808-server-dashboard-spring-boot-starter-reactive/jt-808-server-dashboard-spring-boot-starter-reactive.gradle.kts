plugins {
    id("xtream-codec-frontend-build-plugin")
}

dependencies {

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

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

// ./gradlew clean build -P xtream.frontend.build.jt-808-server-dashboard-ui.enabled=true
xtreamCodecFrontendBuild {
    enabled.set(xtreamConfig.buildJt808DashboardUi)
    group.set("jt808-dashboard-ui")
    description.set("jt808-dashboard-ui")
    frontendProjectDir.set(layout.projectDirectory.dir("../jt-808-server-dashboard-ui"))
    frontendBasePath.set("/dashboard-ui/")
    backendStaticDir.set(layout.projectDirectory.dir("src/main/resources/static/dashboard/808"))
//    commandLineConfigurer {
////        commandLine("wsl", "bash", "-c", "echo Hello")
////        commandLine("sh", "-c", "echo Hello111111111111111111")
//        commandLine(
//            "sh", "-c",
//            """
//            set -e
//            pnpm install --registry https://registry.npmmirror.com
//            pnpm run build --mode production
//            """.trimIndent()
//        )
//    }
}
