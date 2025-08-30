dependencies {
    compileOnly("jakarta.annotation:jakarta.annotation-api")
    compileOnly("jakarta.validation:jakarta.validation-api")
    compileOnly("org.springframework.boot:spring-boot-starter-json")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-webflux")
    compileOnly("org.springframework.boot:spring-boot-starter-websocket")

    testCompileOnly("org.springframework.boot:spring-boot-starter-json")
    testCompileOnly("org.springframework.boot:spring-boot-starter-web")
    testCompileOnly("org.springframework.boot:spring-boot-starter-webflux")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
