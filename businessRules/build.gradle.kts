import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "1.1.1"

plugins {
    kotlin("jvm")
    kotlin("kapt")
}

sourceSets { sharedSources() }
repositories { sharedRepos() }
java { javaSource() }
tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = jvmVersion }

dependencies {
    implementation(fileTree(mapOf("dir" to "../libs", "include" to listOf("*.jar"))))

    implementation(kotlinJdk())

    implementation(ktorServer("netty"))
    implementation(ktorServer("core"))
    implementation(ktor("websockets"))
    implementation(ktor("auth-jwt"))
    implementation(ktorClient("websockets"))
    implementation(ktorClient("okhttp"))

    implementation(moshi())

    implementation(exposed())
}
