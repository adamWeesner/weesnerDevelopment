plugins {
    application
    id(Kotlin.jvm)
    id(Kotlin.kapt)
    id(ShadowJar.core) version ShadowJar.version
}

group = Router.group
version = Router.version

sourceSets { sharedSources() }
java { javaSource() }
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
    val tcnative_version = rootProject.extra["tcnative_version"]
    val tcnative_classifier = rootProject.extra["tcnative_classifier"]

    implementation(project(BusinessRules.project))
    implementation(project(Backend.project))

    implementation(Kimchi.core)
    implementation(KodeIn.core)
    implementation(KodeIn.ktorServer)
    implementation(Logback.core)
    implementation(Ktor.serialization)
    implementation(Ktor.Client.java)
    implementation(Ktor.Server.core)
    implementation(Ktor.Server.netty)

    implementation("io.netty:netty-tcnative:$tcnative_version")
    implementation("io.netty:netty-tcnative-boringssl-static:$tcnative_version")
    implementation("io.netty:netty-tcnative-boringssl-static:$tcnative_version:$tcnative_classifier")

    testImplementation(project(TestUtils.project))
}
