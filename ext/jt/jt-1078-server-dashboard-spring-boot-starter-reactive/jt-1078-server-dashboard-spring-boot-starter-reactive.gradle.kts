dependencies {

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    api(project(":xtream-codec-base"))
    api(project(":ext:jt:jt-1078-server-spring-boot-starter-reactive"))

    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.springframework.boot:spring-boot-starter-validation")

    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("org.springframework.boot:spring-boot-starter-websocket")
    // runtime
    implementation("jakarta.annotation:jakarta.annotation-api")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core")
}
