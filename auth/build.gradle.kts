plugins {
    application
    `maven-publish`
}

group = Auth.group
version = Auth.version

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            setUrl("https://maven.pkg.github.com/adamWeesner/weesnerDevelopment")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

dependencies {
    implementation(project(BusinessRules.project))
    implementation(Dropwizard.metricsJmx)
    implementation(Exposed.core)
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
