import com.weesnerdevelopment.ProjectGradleModule.BusinessRules
import com.weesnerdevelopment.ProjectGradleModule.TestUtils
import com.weesnerdevelopment.implementation
import com.weesnerdevelopment.testImplementation

plugins {
    application
}

group = "${Base.group}.auth"
version = "1.0.0"

dependencies {
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
    implementation(Ktor.locations)
    implementation(Ktor.metrics)
    implementation(Ktor.webSockets)
    implementation(Ktor.serialization)
    implementation(Logback.core)

    testImplementation(TestUtils)
}
