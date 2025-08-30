import io.github.hylexus.xtream.codec.gradle.plugins.XtreamCodecFastModePlugin

plugins {
    id("org.springframework.boot")
    application
}

application {
    mainClass.set("io.github.hylexus.xtream.debug.ext.jt1078.Jt1078SpringBootStarterDebugApp")
}
tasks.bootJar {
    archiveFileName.set("${project.name}.jar")
    mainClass.set("io.github.hylexus.xtream.debug.ext.jt1078.Jt1078SpringBootStarterDebugApp")
}
apply<XtreamCodecFastModePlugin>()

dependencies {

//    api(project(":ext:jt:jt-1078-server-spring-boot-starter-reactive"))
    api(project(":ext:jt:jt-1078-server-dashboard-spring-boot-starter-reactive"))
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-logging")

    implementation("jakarta.annotation:jakarta.annotation-api")

}
