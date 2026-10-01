plugins {
    application
}

dependencies {
    implementation(project(":xtream-codec-core"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
}

application {
    mainClass.set("io.github.hylexus.xtream.codec.debugger.app.CodecDebuggerApplication")
}

// 原生静态页面直接随应用打包，无需引入额外前端运行时。
tasks.processResources {
    from("../xtream-codec-debugger-ui/src") {
        into("ui")
        include("index.html", "app.js", "completion.js", "style.css")
    }
    from("../../xtream-codec-core/src/main/resources/io/github/hylexus/xtream/codec/core/dynamic/schema") {
        into("ui")
        include("dynamic-codec-schema.json")
    }
}
