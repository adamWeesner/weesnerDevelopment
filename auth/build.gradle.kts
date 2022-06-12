val firebaseAdmin: String by project
val kodeinKtor: String by project
val ktorServerAuthJwt: String by project
val ktorServerCORS: String by project
val ktorServerHSTS: String by project
val ktorServerHttpsRedirect: String by project
val ktorServerLocations: String by project
val ktorServerMetrics: String by project
val ktorServerWebsockets: String by project
val ktorServerSerialization: String by project
val ktorServerStatusPages: String by project
val ktorClientJava: String by project
val hikari: String by project
val h2database: String by project

plugins {
    application
    kotlin("plugin.serialization") version Kotlin.version
}

group = "${Base.group}.auth"
version = "1.0.0"

dependencies {
    implementation(ProjectGradleModule.BusinessRules)

    implementation("io.dropwizard.metrics:metrics-jmx:4.2.9")
    implementation(Exposed.core)
    implementation(Exposed.jdbc)
    implementation(Exposed.dao)
    implementation(hikari)
    implementation(h2database)
    implementation(Kimchi.core)
    implementation(kodeinKtor)
    implementation(ktorServerAuthJwt)
    implementation(ktorServerCORS)
    implementation(ktorServerHSTS)
    implementation(ktorServerHttpsRedirect)
    implementation(ktorServerLocations)
    implementation(ktorServerMetrics)
    implementation(ktorServerWebsockets)
    implementation(ktorServerSerialization)
    implementation(ktorServerStatusPages)
    implementation(ktorClientJava)
    implementation(Logback.core)

    testImplementation(ProjectGradleModule.TestUtils)
}
