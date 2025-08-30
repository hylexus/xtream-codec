dependencies {

    api(project(":xtream-codec-core"))
    api("io.netty:netty-buffer")
    api("io.projectreactor.netty:reactor-netty-core")
    api("org.springframework.boot:spring-boot-starter-logging"){
        exclude(group = "org.apache.logging.log4j")
        exclude(module = "jul-to-slf4j")
    }

    implementation("jakarta.annotation:jakarta.annotation-api")
    compileOnly("io.projectreactor:reactor-core-micrometer")
    compileOnly("com.lmax:disruptor")

}
