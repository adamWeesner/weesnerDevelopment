import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(Kotlin.jvm)
    id(Kotlin.kapt)
}

group = Base.group
version = BusinessRules.version

sourceSets { sharedSources() }
repositories { sharedRepos() }
java { javaSource() }
tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = Jvm.version }

dependencies {
    implementation(fileTree(Base.jars))
    implementation(Kotlin.stdLib)
    implementation(Ktor.Server.core)
    implementation(Ktor.Server.netty)
    implementation(Ktor.webSockets)
    implementation(Ktor.authJwt)
    implementation(Ktor.Client.webSockets)
    implementation(Ktor.Client.okHttp)
    implementation(Moshi.core)
    implementation(Exposed.core)
}
