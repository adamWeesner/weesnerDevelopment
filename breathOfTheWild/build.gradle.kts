import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(Kotlin.jvm)
    id(Kotlin.kapt)
}

group = Base.group
version = BreathOfTheWild.version

sourceSets { sharedSources() }
repositories { sharedRepos() }
java { javaSource() }
tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = Jvm.version }

dependencies {
    implementation(project(BusinessRules.project))
    implementation(Ktor.Server.core)
    implementation(Moshi.core)
    implementation(Exposed.core)
}
