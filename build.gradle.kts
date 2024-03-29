import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    id(Kotlin.jvm) version Kotlin.version
    id(ShadowJar.core) version ShadowJar.version
}

group = Base.group
version = Base.version

repositories {
    mavenCentral()
    jcenter()
}
java {
    sourceCompatibility = Jvm.javaVersion
}
tasks.withType<KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    kotlinOptions.jvmTarget = Jvm.version
}

buildscript {
    extra.apply {
        set("tcnative_version", "2.0.48.Final")

        val osName = System.getProperty("os.name").toLowerCase()
        val classifier = if (osName.contains("win")) {
            "windows-x86_64"
        } else if (osName.contains("linux")) {
            "linux-x86_64"
        } else if (osName.contains("mac")) {
            "osx-x86_64"
        } else {
            ""
        }
        set("tcnative_classifier", classifier)
    }
}

val updatedList = listOf(
    ProjectGradleModule.Auth.project.replace(":", ""),
    ProjectGradleModule.BackendRouter.project.replace(":", ""),
    ProjectGradleModule.BillMan.project.replace(":", ""),
)

subprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = Kotlin.jvm)
    apply(plugin = Kotlin.kapt)

    if (project.name in updatedList) {
        apply(plugin = ShadowJar.core)
        apply(plugin = "maven-publish")

        configure<PublishingExtension> {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/adamWeesner/weesnerDevelopment")
                    credentials {
                        username = project.findProperty("gpr.user") as? String? ?: System.getenv("GITHUB_ACTOR")
                        password = project.findProperty("gpr.key") as? String? ?: System.getenv("GITHUB_TOKEN")
                    }
                }
            }
            publications {
                register<MavenPublication>("gpr") {
                    from(components["java"])
                }
            }
        }
    }

    task("stage").dependsOn("installDist")

    tasks {
        withType<KotlinCompile>().all {
            kotlinOptions.freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
            kotlinOptions.jvmTarget = Jvm.version

            sourceSets {
                getByName("main").java.srcDirs("src/main/kotlin")
                getByName("test").java.srcDirs("src/test/kotlin")
                getByName("main").resources.srcDirs("resources")
                getByName("test").resources.srcDirs("testresources")
            }
            java {
                sourceCompatibility = Jvm.javaVersion
            }
        }
        withType<Test> {
            useJUnitPlatform()
        }
        withType<Copy> {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
        withType<Tar> {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
        withType<Jar> {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
        withType<Zip> {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

        if (project.name in updatedList) {
            afterEvaluate {
                application {
                    mainClass.set(Ktor.Server.mainClass)
                }
                shadowJar {
                    manifest {
                        attributes(Pair("Main-Class", application.mainClass))
                    }
                }
                withType<Jar> {
                    manifest {
                        attributes(mapOf("Main-Class" to application.mainClass))
                    }
                }
            }
        }
    }
}
