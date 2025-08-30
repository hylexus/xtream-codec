dependencies {

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    api(project(":xtream-codec-base"))
    api(project(":xtream-codec-server-reactive"))
    api("com.github.ben-manes.caffeine:caffeine")
    api("org.springframework.boot:spring-boot-starter")

    compileOnly("org.springframework.boot:spring-boot-starter-webflux")
    compileOnly("org.springframework.boot:spring-boot-starter-validation")
    compileOnly("org.bouncycastle:bcprov-jdk18on")

    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("io.projectreactor:reactor-core-micrometer")
    implementation("org.springframework.boot:spring-boot-starter-json")
    testImplementation("org.springframework.boot:spring-boot-starter-json")

    implementation("jakarta.annotation:jakarta.annotation-api")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core")
}
