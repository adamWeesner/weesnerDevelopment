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
    implementation(project(BusinessRules.project))
    implementation(project(Backend.project))

    implementation(Kimchi.core)
    implementation(KodeIn.core)
    implementation(KodeIn.ktorServer)
    implementation(Logback.core)
    implementation(Ktor.Server.core)
    implementation(Ktor.Client.java)

    testImplementation(project(TestUtils.project))
}
