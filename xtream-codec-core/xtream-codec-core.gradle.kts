plugins {
    id("me.champeau.jmh")
}

val testWithoutJacksonRuntime = configurations.create("testWithoutJacksonRuntime") {
    isCanBeConsumed = false
}

dependencies {
    api(project(":xtream-codec-base"))
    api("org.springframework:spring-expression")
    implementation("org.springframework:spring-context")
    api("io.netty:netty-buffer")
    api("org.slf4j:slf4j-api")
    api("io.github.classgraph:classgraph")
    api("com.fasterxml.jackson.core:jackson-annotations")
    compileOnly("com.fasterxml.jackson.core:jackson-databind")

    testImplementation("org.springframework.boot:spring-boot-starter-json")

    testImplementation("org.mockito:mockito-core")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation("com.googlecode.aviator:aviator")
    testImplementation("org.mvel:mvel2")

    testWithoutJacksonRuntime("org.junit.jupiter:junit-jupiter")
    testWithoutJacksonRuntime("org.junit.platform:junit-platform-launcher")
}

// 仅使用生产依赖和 JUnit，避免测试用的 Spring Boot starter 掩盖运行时依赖缺失。
val testWithoutJackson = tasks.register<Test>("testWithoutJackson") {
    description = "验证不含 Jackson 实现的运行环境"
    group = "verification"
    useJUnitPlatform()
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().output + sourceSets.main.get().runtimeClasspath + testWithoutJacksonRuntime
    include("**/JacksonOptionalDependencyTest.class")
}

tasks.test {
    exclude("**/JacksonOptionalDependencyTest.class")
}

tasks.check {
    dependsOn(testWithoutJackson)
}
