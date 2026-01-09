dependencies {

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    api(project(":xtream-codec-server-reactive"))
    api("com.github.ben-manes.caffeine:caffeine")
    api("org.springframework.boot:spring-boot-starter")
    api("de.sciss:jump3r:1.0.5")

    compileOnly("org.springframework.boot:spring-boot-starter-webflux")
    compileOnly("org.springframework.boot:spring-boot-starter-validation")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

if (xtreamConfig.licenseCheckerEnabled) {
    // 不检测 License
    val jt1078MockFileDir = project.file("src/test/resources/mock-data")
    extensions.configure(net.minecraftforge.licenser.LicenseExtension::class.java) {
        exclude {
            it.file.startsWith(jt1078MockFileDir)
        }
    }
}
