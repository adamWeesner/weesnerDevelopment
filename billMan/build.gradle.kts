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
val h2database: String by project
val hikari: String by project

plugins {
    application
}

group = "${Base.group}.billman"
version = "2.0.0"

dependencies {
    val tcnative_version = rootProject.extra["tcnative_version"]
    val tcnative_classifier = rootProject.extra["tcnative_classifier"]

    implementation(ProjectGradleModule.BusinessRules)

    implementation(Dropwizard.metricsJmx)
    implementation(Exposed.core)
    implementation(Exposed.jdbc)
    implementation(Exposed.dao)
    implementation(Kimchi.core)
    implementation(Ktor.Client.logging)
    implementation(Logback.core)
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
    implementation(hikari)
    implementation(h2database)

    implementation("io.netty:netty-tcnative:$tcnative_version")
    implementation("io.netty:netty-tcnative-boringssl-static:$tcnative_version")
    implementation("io.netty:netty-tcnative-boringssl-static:$tcnative_version:$tcnative_classifier")

    testImplementation(ProjectGradleModule.TestUtils)
}
