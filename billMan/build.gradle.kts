import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(Kotlin.jvm)
}

group = Base.group
version = BillMan.version

sourceSets { sharedSources() }
repositories { sharedRepos() }
java { javaSource() }
tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = Jvm.version }

dependencies {
    implementation(project(BusinessRules.project))
    implementation(Moshi.core)
    implementation(Ktor.authJwt)
    implementation(Ktor.Server.core)
    implementation(Exposed.core)
    implementation(KodeIn.core)
}
