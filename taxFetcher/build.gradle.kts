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
    implementation(project(":businessRules"))

    implementation(kotlin("stdlib-jdk8"))

    implementation(moshi())

    implementation(exposed())
}
