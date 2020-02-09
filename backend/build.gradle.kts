import Versions.h2_version
import Versions.hikari_version
import Versions.ktor_moshi_version
import Versions.logback_version
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.weesnerdevelopment"
version = "1.0.0-SNAPSHOT"

plugins {
    application
    kotlin("jvm")
    kotlin("kapt")
}

repositories {
    mavenCentral()
    jcenter()
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

sourceSets {
    getByName("main").java.srcDirs("src")
    getByName("test").java.srcDirs("test")
    getByName("main").resources.srcDirs("resources")
    getByName("test").resources.srcDirs("testresources")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.jvmTarget = "1.8"
}

task("stage").dependsOn("installDist")

dependencies {
    implementation(project(":businessRules"))
    implementation(project(":taxFetcher"))

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation(ktor("server-netty"))
    implementation(ktor("server-core"))
    implementation(ktor("websockets"))
    implementation(ktor("client-websockets"))
    implementation(ktor("client-okhttp"))
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
