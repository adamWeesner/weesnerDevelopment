import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.weesnerdevelopment"
version = "1.0"

plugins {
    kotlin("jvm")
    kotlin("kapt")
}

repositories {
    mavenCentral()
    jcenter()
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

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(ktor("server-netty"))
    implementation(ktor("server-core"))
    implementation(ktor("websockets"))
    implementation(ktor("client-websockets"))
    implementation(ktor("client-okhttp"))
    implementation(moshi())

    implementation(exposed())
}
