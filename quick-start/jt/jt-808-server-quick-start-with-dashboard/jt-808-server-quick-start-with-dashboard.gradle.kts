import io.github.hylexus.xtream.codec.gradle.plugins.XtreamCodecFastModePlugin

plugins {
    id("org.springframework.boot")
    application
}

application {
    mainClass.set("io.github.hylexus.xtream.quickstart.ext.jt808.withdashboard.Jt808ServerQuickStartWithDashboardApp")
}
tasks.bootJar {
    archiveFileName.set("${project.name}.jar")
    mainClass.set("io.github.hylexus.xtream.quickstart.ext.jt808.withdashboard.Jt808ServerQuickStartWithDashboardApp")
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

    // 加解密
    api("org.bouncycastle:bcprov-jdk18on")
    // api(project(":ext:jt:jt-808-server-spring-boot-starter-reactive"))
    api(project(":ext:jt:jt-808-server-dashboard-spring-boot-starter-reactive"))
    api("org.springframework.boot:spring-boot-starter-logging")

    implementation("jakarta.annotation:jakarta.annotation-api")

}
