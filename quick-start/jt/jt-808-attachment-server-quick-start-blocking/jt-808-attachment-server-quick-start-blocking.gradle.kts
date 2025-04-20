import io.github.hylexus.xtream.codec.gradle.plugins.XtreamCodecFastModePlugin

plugins {
    id("org.springframework.boot")
    application
}

application {
    mainClass.set("io.github.hylexus.xtream.quickstart.ext.jt808.attachment.blocking.Jt808AttachmentServerQuickStartBlockingApp")
}
tasks.bootJar {
    archiveFileName.set("${project.name}.jar")
    mainClass.set("io.github.hylexus.xtream.quickstart.ext.jt808.attachment.blocking.Jt808AttachmentServerQuickStartBlockingApp")
}
apply<XtreamCodecFastModePlugin>()

dependencies {
    // common start
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // common end

    // api(project(":ext:jt:jt-808-server-spring-boot-starter-reactive"))
    // 带 dashboard
    api(project(":ext:jt:jt-808-server-dashboard-spring-boot-starter-reactive"))
    api("org.springframework.boot:spring-boot-starter-logging")
    api("org.springframework.boot:spring-boot-starter-web")

    implementation("jakarta.annotation:jakarta.annotation-api")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core")

}
