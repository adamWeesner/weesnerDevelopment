val firebaseAdmin: String by project
val ktorServerCore: String by project
val ktorServerNetty: String by project
val ktorServerAuthJwt: String by project
val ktorServerCORS: String by project
val ktorServerLocations: String by project
val ktorServerWebsockets: String by project
val ktorServerSerialization: String by project
val ktorServerDefaultHeaders: String by project
val ktorServerCallLogging: String by project
val ktorServerContentNegotiation: String by project
val ktorServerStatusPages: String by project

plugins {
    kotlin("plugin.serialization") version Kotlin.version
}

group = "${Base.group}.rules"
version = "1.3.2"

dependencies {
    api(fileTree("../libs") { include("*.jar") })
    implementation(firebaseAdmin)
    implementation(ktorServerCore)
    implementation(ktorServerNetty)
    implementation(ktorServerDefaultHeaders)
    implementation(ktorServerCallLogging)
    implementation(ktorServerContentNegotiation)
    implementation(ktorServerLocations)
    implementation(ktorServerCORS)
    implementation(ktorServerWebsockets)
    implementation(ktorServerAuthJwt)
    implementation(Commons.base64)
    implementation(Ktor.Client.webSockets)
    implementation(Ktor.Client.java)
    implementation(ktorServerSerialization)
    implementation(ktorServerStatusPages)
    implementation(Exposed.core)
    implementation(Exposed.dao)
    implementation(Kimchi.core)
}
