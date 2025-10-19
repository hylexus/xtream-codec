plugins {
    id("me.champeau.jmh")
}

dependencies {
    compileOnly("jakarta.annotation:jakarta.annotation-api")
    compileOnly("jakarta.validation:jakarta.validation-api")
    compileOnly("org.springframework.boot:spring-boot-starter-json")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-webflux")
    compileOnly("org.springframework.boot:spring-boot-starter-websocket")

    listOf("com.googlecode.aviator:aviator", "org.mvel:mvel2").forEach { dep ->
        compileOnly(dep)
        testCompileOnly(dep)
        testRuntimeOnly(dep)
        // 专门为 JMH 添加运行时依赖
        jmhImplementation(dep)
    }

    testCompileOnly("org.springframework.boot:spring-boot-starter-json")
    testCompileOnly("org.springframework.boot:spring-boot-starter-web")
    testCompileOnly("org.springframework.boot:spring-boot-starter-webflux")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
