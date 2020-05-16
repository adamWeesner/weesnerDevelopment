import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "1.0.0"

plugins {
    kotlin("jvm")
}

sourceSets { sharedSources() }
repositories { sharedRepos() }
java { javaSource() }
tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = jvmVersion }

dependencies {
    implementation(fileTree(mapOf("dir" to "../libs", "include" to listOf("*.jar"))))
    implementation(project(":businessRules"))

    implementation(kotlinJdk())

    implementation(moshi())

    implementation(ktorServer("core"))
    implementation(ktor("auth-jwt"))

    implementation(exposed())

    implementation(kodein())
}
