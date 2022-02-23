import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    id(Kotlin.jvm)
    id(Kotlin.kapt)
    id(ShadowJar.core) version ShadowJar.version
}

group = BillMan.group
version = BillMan.version

sourceSets { sharedSources() }
repositories { sharedRepos() }
java { javaSource() }
tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = Jvm.version }
tasks.withType<Test> { useJUnitPlatform() }
application { mainClass.set(Ktor.Server.mainClass) }
tasks.withType<Jar> { manifest { attributes(mapOf("Main-Class" to application.mainClass)) } }
task("stage").dependsOn("installDist")
tasks {
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", application.mainClass))
        }
    }
}

dependencies {
    implementation(project(BusinessRules.project))

    implementation(Dropwizard.metricsJmx)
    implementation(Exposed.core)
    implementation(Exposed.jdbc)
    implementation(Exposed.dao)
    implementation(H2.database)
    implementation(Hikari.core)
    implementation(Kimchi.core)
    implementation(KodeIn.core)
    implementation(KodeIn.ktorServer)
    implementation(Ktor.authJwt)
    implementation(Ktor.metrics)
    implementation(Ktor.webSockets)
    implementation(Ktor.serialization)
    implementation(Ktor.locations)
    implementation(Ktor.Client.logging)
    implementation(Ktor.Server.core)
    implementation(Logback.core)

    testImplementation(project(TestUtils.project))
}
