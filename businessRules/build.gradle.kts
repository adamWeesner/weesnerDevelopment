plugins {
    kotlin("plugin.serialization") version Kotlin.version
}

group = "${Base.group}.rules"
version = "1.3.2"

dependencies {
    api(fileTree("../libs") { include("*.jar") })
    implementation(Ktor.Server.core)
    implementation(Ktor.Server.netty)
    implementation(Ktor.locations)
    implementation(Ktor.webSockets)
    implementation(Ktor.authJwt)
    implementation(Commons.base64)
    implementation(Ktor.Client.webSockets)
    implementation(Ktor.Client.java)
    implementation(Ktor.serialization)
    implementation(Exposed.core)
    implementation(Exposed.dao)
    implementation(Kimchi.core)
}
