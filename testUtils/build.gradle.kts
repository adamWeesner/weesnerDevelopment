import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(Kotlin.jvm)
}

group = Base.group
version = TestUtils.version

sourceSets { sharedSources() }
repositories { sharedRepos() }
java { javaSource() }
tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = Jvm.version }

dependencies {
    implementation(project(BusinessRules.project))
    implementation(Kimchi.core)

    api(Ktor.serverTest)
    api(Kotlin.Test.core)
    api(Kotlin.Test.junit5)
    api(Junit.core)
}
