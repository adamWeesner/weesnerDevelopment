import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(Kotlin.jvm)
    id(Kotlin.kapt)
    kotlin("plugin.serialization") version Kotlin.version
}

group = Base.group
version = BusinessRules.version

sourceSets { sharedSources() }
repositories { sharedRepos() }
java { javaSource() }
tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = Jvm.version }

dependencies {
    api(fileTree("../libs") { include("*.jar") })
    implementation(Ktor.Server.core)
    implementation(Ktor.Server.netty)
    implementation(Ktor.locations)
    implementation(Ktor.webSockets)
    implementation(Ktor.authJwt)
    implementation(Commons.base64)
    implementation(Ktor.Client.webSockets)
    implementation(Ktor.Client.okHttp)
    implementation(Ktor.serialization)
    implementation(Exposed.core)
    implementation(Kimchi.core)
}
