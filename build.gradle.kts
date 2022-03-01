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

buildscript {
    extra.apply {
        set("tcnative_version", "2.0.48.Final")

        val osName = System.getProperty("os.name").toLowerCase()
        val classifier = if (osName.contains("win")) {
            "windows-x86_64"
        } else if (osName.contains("linux")) {
            "linux-x86_64"
        } else if (osName.contains("mac")) {
            "osx-x86_64"
        } else {
            ""
        }
        set("tcnative_classifier", "osx-x86_64")
    }
}

allprojects {
    repositories { sharedRepos() }
    tasks.withType<KotlinCompile>().all {
        kotlinOptions.freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        kotlinOptions.jvmTarget = Jvm.version
    }
    tasks.withType<Test> { useJUnitPlatform() }
}

