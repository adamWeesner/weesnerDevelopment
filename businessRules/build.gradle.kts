import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "1.1.0"

plugins {
    kotlin("jvm")
    kotlin("kapt")
}

sourceSets { sharedSources() }
repositories { sharedRepos() }
java { javaSource() }
tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = jvmVersion }

dependencies {
    implementation(kotlinJdk())

    implementation(ktorServer("netty"))
    implementation(ktorServer("core"))
    implementation(ktor("websockets"))
    implementation(ktorClient("websockets"))
    implementation(ktorClient("okhttp"))

    implementation(moshi())

    implementation(exposed())
}
