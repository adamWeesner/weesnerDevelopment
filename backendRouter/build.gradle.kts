import com.weesnerdevelopment.ProjectGradleModule.BusinessRules
import com.weesnerdevelopment.ProjectGradleModule.TestUtils
import com.weesnerdevelopment.implementation
import com.weesnerdevelopment.testImplementation

plugins {
    application
}

group = "${Base.group}.router"
version = "1.0.0"

dependencies {
    val tcnative_version = rootProject.extra["tcnative_version"]
    val tcnative_classifier = rootProject.extra["tcnative_classifier"]

    implementation(BusinessRules)

    implementation(Kimchi.core)
    implementation(KodeIn.core)
    implementation(KodeIn.ktorServer)
    implementation(Logback.core)
    implementation(Ktor.serialization)
    implementation(Ktor.Client.java)
    implementation(Ktor.Server.core)
    implementation(Ktor.Server.netty)

    implementation("io.netty:netty-tcnative:$tcnative_version")
    implementation("io.netty:netty-tcnative-boringssl-static:$tcnative_version")
    implementation("io.netty:netty-tcnative-boringssl-static:$tcnative_version:$tcnative_classifier")

    testImplementation(TestUtils)
}
