dependencies {
    api("org.springframework:spring-expression")
    api("io.netty:netty-buffer")
    api("org.slf4j:slf4j-api")
    api("io.github.classgraph:classgraph")

    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("org.springframework.boot:spring-boot-starter-json")
    testImplementation("org.springframework.boot:spring-boot-starter-json")

    testImplementation("org.mockito:mockito-core")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
