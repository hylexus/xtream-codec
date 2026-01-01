dependencies {

    api(project(":xtream-codec-core"))
    api("org.springframework.boot:spring-boot-starter-logging")
    api("com.github.ben-manes.caffeine:caffeine")

    testImplementation("com.googlecode.aviator:aviator")
    testImplementation("org.mvel:mvel2")

}
