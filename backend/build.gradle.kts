import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    id(Kotlin.jvm)
    id(Kotlin.kapt)
    id(ShadowJar.core) version ShadowJar.version
}

group = Base.group
version = Backend.version

sourceSets { sharedSources() }
repositories { sharedRepos() }
java { javaSource() }
tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = Jvm.version }
tasks.withType<Test> { useJUnitPlatform() }
application { mainClassName = Ktor.Server.mainClass }
tasks.withType<Jar> { manifest { attributes(mapOf("Main-Class" to application.mainClassName)) } }
task("stage").dependsOn("installDist")

dependencies {
    implementation(fileTree(Base.jars))
    implementation(project(BusinessRules.project))
    implementation(project(TaxFetcher.project))
    implementation(project(BillMan.project))
    implementation(Kotlin.stdLib)
    implementation(Kotlin.reflect)
    implementation(Ktor.authJwt)
    implementation(Ktor.webSockets)
    implementation(Ktor.Server.core)
    implementation(Ktor.Server.netty)
    implementation(Ktor.Client.webSockets)
    implementation(Ktor.Client.okHttp)
    implementation(Moshi.core)
    implementation(Moshi.adapters)
    implementation(Moshi.ktor)
    implementation(Exposed.core)
    implementation(Exposed.jdbc)
    implementation(KodeIn.core)
    implementation(KodeIn.ktorServer)
    implementation(H2.database)
    implementation(Hikari.core)
    implementation(Logback.core)
    implementation(Kimchi.core)

    testImplementation(Ktor.serverTest)
    testImplementation(Kotlin.Test.core)
    testImplementation(Kotlin.Test.junit5)
    testImplementation(Junit.core)
}
