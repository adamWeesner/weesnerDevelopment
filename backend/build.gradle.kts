import Versions.h2_version
import Versions.hikari_version
import Versions.ktor_moshi_version
import Versions.logback_version
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "1.2.0"

plugins {
    application
    kotlin("jvm")
    kotlin("kapt")
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

sourceSets { sharedSources() }
repositories { sharedRepos() }
java { javaSource() }
tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = jvmVersion }
tasks.withType<Test> { useJUnitPlatform() }

application { mainClassName = "io.ktor.server.netty.EngineMain" }

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to application.mainClassName
            )
        )
    }
}

task("stage").dependsOn("installDist")

dependencies {
    implementation(fileTree(mapOf("dir" to "../libs", "include" to listOf("*.jar"))))

    implementation(project(":businessRules"))
    implementation(project(":taxFetcher"))
    implementation(project(":billMan"))

    implementation(kotlinJdk())
    implementation(kotlin("reflect"))

    implementation(ktorServer("netty"))
    implementation(ktorServer("core"))
    implementation(ktor("websockets"))
    implementation(ktorClient("websockets"))
    implementation(ktorClient("okhttp"))

    implementation("com.ryanharter.ktor:ktor-moshi:$ktor_moshi_version")

    implementation(moshi())
    implementation(moshi("adapters"))

    implementation(exposed())
    implementation(exposed("jdbc"))

    implementation(kodein())
    implementation(kodein("framework-ktor-server"))

    implementation("com.h2database:h2:$h2_version")
    implementation("com.zaxxer:HikariCP:$hikari_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation(ktor("auth-jwt"))

    testImplementation(ktor("server-tests"))
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
}
