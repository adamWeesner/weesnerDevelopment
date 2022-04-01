plugins {
    application
}

group = Base.group
version = Backend.version

dependencies {
    implementation(project(BusinessRules.project))
    implementation(project(TaxFetcher.project))
    implementation(project(BreathOfTheWild.project))
    implementation(project(SerialCabinet.project))
    implementation(Ktor.authJwt)
    implementation(Ktor.webSockets)
    implementation(Ktor.metrics)
    implementation(Dropwizard.metricsJmx)
    implementation(Ktor.Server.core)
    implementation(Ktor.Server.netty)
    implementation(Ktor.Client.webSockets)
    implementation(Ktor.Client.java)
    implementation(Ktor.serialization)
    implementation(Exposed.core)
    implementation(Exposed.jdbc)
    implementation(KodeIn.core)
    implementation(KodeIn.ktorServer)
    implementation(H2.database)
    implementation(Hikari.core)
    implementation(Logback.core)
    implementation(Kimchi.core)

    testImplementation(project(TestUtils.project))
}
