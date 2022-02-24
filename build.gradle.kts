import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(Kotlin.jvm) version Kotlin.version
}

group = Base.group
version = Base.version

repositories { sharedRepos() }
java { javaSource() }
tasks.withType<KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    kotlinOptions.jvmTarget = Jvm.version
}

allprojects {
    repositories { sharedRepos() }
    tasks.withType<KotlinCompile>().all {
        kotlinOptions.freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        kotlinOptions.jvmTarget = Jvm.version
    }
    tasks.withType<Test> { useJUnitPlatform() }
}

