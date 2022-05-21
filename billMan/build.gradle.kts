import com.weesnerdevelopment.ProjectGradleModule.BusinessRules
import com.weesnerdevelopment.ProjectGradleModule.TestUtils
import com.weesnerdevelopment.implementation
import com.weesnerdevelopment.testImplementation

plugins {
    application
}

group = "${Base.group}.billman"
version = "2.0.1"

dependencies {
    val tcnative_version = rootProject.extra["tcnative_version"]
    val tcnative_classifier = rootProject.extra["tcnative_classifier"]

    implementation(BusinessRules)

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

    implementation("io.netty:netty-tcnative:$tcnative_version")
    implementation("io.netty:netty-tcnative-boringssl-static:$tcnative_version")
    implementation("io.netty:netty-tcnative-boringssl-static:$tcnative_version:$tcnative_classifier")

    testImplementation(TestUtils)
}
