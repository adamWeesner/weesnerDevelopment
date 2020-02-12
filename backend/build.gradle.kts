import Versions.h2_version
import Versions.hikari_version
import Versions.ktor_moshi_version
import Versions.logback_version
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "1.0.0-SNAPSHOT"

plugins {
    application
    kotlin("jvm")
    kotlin("kapt")
}

sourceSets { sharedSources() }
repositories { sharedRepos() }
java { javaSource() }
tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = jvmVersion }
tasks.withType<Test> { useJUnitPlatform() }

application { mainClassName = "io.ktor.server.netty.EngineMain" }

task("stage").dependsOn("installDist")

dependencies {
    implementation(project(":businessRules"))
    implementation(project(":taxFetcher"))

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

    implementation("com.h2database:h2:$h2_version")
    implementation("com.zaxxer:HikariCP:$hikari_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    testImplementation(ktor("server-tests"))
}
