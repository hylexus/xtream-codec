import io.github.hylexus.xtream.codec.gradle.utils.XtreamConfig.xtreamConfig

plugins {
    id("java-platform")
    id("maven-publish")
    id("io.gitee.pkmer.pkmerboot-central-publisher") apply false
    id("signing")
}

javaPlatform {
    allowDependencies()
}

version = xtreamConfig.projectVersion

private fun DependencyConstraintHandlerScope.apiBom(module: String) {
    val group = xtreamConfig.projectGroupId
    val versionProp = "\${xtream-codec.version}"
    api("$group:$module:$versionProp")
}

dependencies {
    constraints {
        apiBom("xtream-codec-base")
        apiBom("xtream-codec-core")
        apiBom("xtream-codec-server-reactive")
        apiBom("jt-808-server-spring-boot-starter-reactive")
        apiBom("jt-808-server-dashboard-spring-boot-starter-reactive")
    }
}

val mavenRepoConfig = xtreamConfig.mavenRepoConfig
val stagingRepositoryPath = "/tmp/gradle/maven-tmp1"
if (xtreamConfig.centalPortalMavenRepoEnabled) {
    apply(plugin = "io.gitee.pkmer.pkmerboot-central-publisher")
    // 延迟配置，在插件完全应用后再执行
    afterEvaluate {
        project.extensions.findByType<io.gitee.pkmer.extension.PkmerBootPluginExtension>()?.apply {
            sonatypeMavenCentral {
                stagingRepository.set(file(stagingRepositoryPath))
                username.set(mavenRepoConfig.getProperty("maven-central-portal.username"))
                password.set(mavenRepoConfig.getProperty("maven-central-portal.password"))
                publishingType.set(io.gitee.pkmer.enums.PublishingType.USER_MANAGED)
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenBom") {
            from(components["javaPlatform"])

            groupId = xtreamConfig.projectGroupId
            artifactId = project.name
            version = version

            pom.withXml {
                val root = asNode()
                // 1. 添加 <properties> 节点
                val propsNode = root.appendNode("properties")
                propsNode.appendNode("xtream-codec.version", version)

                // 2. 找到 <dependencyManagement> 节点
                val dmNode = (root.get("dependencyManagement") as? List<*>)?.firstOrNull() as? groovy.util.Node

                // 3. 把 properties 节点移到 dependencyManagement 前面
                if (dmNode != null) {
                    root.remove(propsNode)
                    @Suppress("UNCHECKED_CAST")
                    val children = root.children() as MutableList<Any>
                    val dmIndex = children.indexOf(dmNode)
                    if (dmIndex >= 0) {
                        children.add(dmIndex, propsNode)
                    } else {
                        // fallback：如果没找到就重新 append 回去
                        root.append(propsNode)
                    }
                }
            }

            pom {
                name.set(project.name)
                url.set(xtreamConfig.projectHomePage)
                description.set("xtream-codec Dependencies")
                licenses {
                    license {
                        name.set(xtreamConfig.projectLicenseName)
                        url.set(xtreamConfig.projectLicenseUrl)
                    }
                }
                developers {
                    developer {
                        id.set(xtreamConfig.projectDeveloperId)
                        name.set(xtreamConfig.projectDeveloperName)
                        email.set(xtreamConfig.projectDeveloperEmail)
                    }
                }
                scm {
                    url.set(xtreamConfig.projectScmUrl)
                    connection.set(xtreamConfig.projectScmConnection)
                    developerConnection.set(xtreamConfig.projectScmDeveloperConnection)
                }
                issueManagement {
                    system.set(xtreamConfig.projectIssueManagementSystem)
                    url.set(xtreamConfig.projectIssueManagementUrl)
                }
            }

            repositories {
                // 1. 发布到你自己的私有仓库
                if (xtreamConfig.privateMavenRepoEnabled) {
                    maven {
                        name = "private"
                        url = uri(mavenRepoConfig.getProperty("privateRepo-release.url"))
                        credentials {
                            username = mavenRepoConfig.getProperty("privateRepo-release.username")
                            password = mavenRepoConfig.getProperty("privateRepo-release.password")
                        }
                    }
                }
                // 2. 发布到 GitHub Packages
                if (xtreamConfig.githubMavenRepoEnabled) {
                    maven {
                        name = "GitHubPackages"
                        url = uri(mavenRepoConfig.getProperty("github-pkg.url"))
                        credentials {
                            username = System.getenv("GITHUB_ACTOR")
                                ?: System.getProperty("gpr.user")
                                        ?: mavenRepoConfig.getProperty("github-pkg.username")

                            password = System.getenv("GITHUB_TOKEN")
                                ?: System.getProperty("gpr.key")
                                        ?: mavenRepoConfig.getProperty("github-pkg.password")
                        }
                    }
                }
                // 3. 发布到 Maven 中央仓库
                // 已废弃: 新版中央仓库发版参考 io.gitee.pkmer.pkmerboot-central-publisher
//                        maven {
//                            name = "centralPortal"
//                            url = uri(mavenRepoConfig.getProperty("sonatype-staging.url"))
//                            credentials {
//                                username = mavenRepoConfig.getProperty("sonatype-staging.username")
//                                password = mavenRepoConfig.getProperty("sonatype-staging.password")
//                            }
//                        }

                maven {
                    name = "localTmp1"
                    // Specify the local staging repo path in the configuration.
                    url = uri(stagingRepositoryPath)
                }
            }
        }
    }
}

if (xtreamConfig.needSign) {
    signing {
        sign(publishing.publications["mavenBom"])
    }
}
