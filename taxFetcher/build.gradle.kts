import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(Kotlin.jvm)
    id(Kotlin.kapt)
}

group = Base.group
version = TaxFetcher.version

sourceSets { sharedSources() }
repositories { sharedRepos() }
java { javaSource() }
tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = Jvm.version }

dependencies {
    implementation(fileTree(Base.jars))
    implementation(project(BusinessRules.project))
    implementation(Kotlin.stdLib)
    implementation(Ktor.Server.core)
    implementation(Moshi.core)
    implementation(Exposed.core)
}
