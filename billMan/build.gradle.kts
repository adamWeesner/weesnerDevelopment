import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "1.0.0-SNAPSHOT"

plugins {
    kotlin("jvm")
    kotlin("kapt")
}

sourceSets { sharedSources() }
repositories { sharedRepos() }
java { javaSource() }
tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = jvmVersion }

dependencies {
    implementation(project(":businessRules"))

    implementation(kotlinJdk())
    implementation(moshi())
    implementation(exposed())
}
