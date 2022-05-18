plugins {
    application
}

group = Auth.group
version = Auth.version

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
    implementation(Ktor.locations)
    implementation(Ktor.metrics)
    implementation(Ktor.webSockets)
    implementation(Ktor.serialization)
    implementation(Logback.core)

    testImplementation(project(TestUtils.project))
}
