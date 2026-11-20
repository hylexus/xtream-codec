plugins {
    id("org.springframework.boot")
    application
}

application {
    mainClass.set("io.github.hylexus.xtream.loadtest.jt808.Jt808LoadtestApp")
}

tasks.bootJar {
    archiveFileName.set("${project.name}.jar")
}

dependencies {
    implementation(project(":ext:jt:jt-808-server-spring-boot-starter-reactive"))
    runtimeOnly("io.netty:netty-transport-native-kqueue::osx-aarch_64")
    runtimeOnly("io.netty:netty-transport-native-kqueue::osx-x86_64")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
