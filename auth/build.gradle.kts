import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(Kotlin.jvm)
    id(Kotlin.kapt)
}

group = Auth.group
version = Auth.version

sourceSets { sharedSources() }
repositories { sharedRepos() }
java { javaSource() }
tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = Jvm.version }
tasks.withType<Test> { useJUnitPlatform() }
task("stage").dependsOn("installDist")

dependencies {
    implementation(project(BusinessRules.project))
    implementation(project(Backend.project))
    implementation(Exposed.core)
    implementation(Ktor.authJwt)
    implementation(KodeIn.core)
    implementation(KodeIn.ktorServer)
    implementation(H2.database)
    implementation(Hikari.core)
    implementation(Logback.core)
    implementation(Kimchi.core)

    testImplementation(project(TestUtils.project))
}
