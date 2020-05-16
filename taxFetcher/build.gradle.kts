import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "1.0.2"

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

    implementation(project(":businessRules"))

    implementation(kotlinJdk())
    implementation(ktorServer("core"))
    implementation(moshi())
    implementation(exposed())
}
