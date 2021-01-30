import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(Kotlin.jvm)
    application
}

group = Base.group
version = Generator.version

sourceSets { sharedSources() }
repositories { sharedRepos() }
java { javaSource() }
application { mainClassName = Generator.mainClass }
tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = Jvm.version }
tasks.withType<Jar> { manifest { attributes(mapOf("Main-Class" to application.mainClassName)) } }

dependencies {
    implementation(project(BusinessRules.project))
    implementation("com.github.ajalt.clikt:clikt:3.0.1")

    testImplementation(Kotlin.Test.core)
    testImplementation(Kotlin.Test.junit5)
    testImplementation(Junit.core)
}
