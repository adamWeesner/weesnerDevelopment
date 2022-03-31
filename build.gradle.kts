import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    id(Kotlin.jvm) version Kotlin.version
    id(ShadowJar.core) version ShadowJar.version
}

group = Base.group
version = Base.version

repositories {
    maven(url = "https://jitpack.io")
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
    "auth",
    "backendRouter",
    "billMan"
)

subprojects {
    repositories {
        maven(url = "https://jitpack.io")
        mavenCentral()
        jcenter()
    }

    apply(plugin = Kotlin.jvm)
    apply(plugin = Kotlin.kapt)

    if (project.name in updatedList)
        apply(plugin = ShadowJar.core)

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
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }

        if (project.name in updatedList) {
            shadowJar {
                manifest {
                    attributes(Pair("Main-Class", application.mainClass))
                }
            }

            afterEvaluate {
                withType<Jar> {
                    manifest {
                        attributes(mapOf("Main-Class" to application.mainClass))
                    }
                }
            }
        }
    }

    afterEvaluate {
        if (project.name in updatedList) {
            application {
                mainClass.set(Ktor.Server.mainClass)
            }
        }
    }
}
