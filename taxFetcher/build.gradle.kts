group = Base.group
version = TaxFetcher.version

dependencies {
    implementation(project(BusinessRules.project))
    implementation(Ktor.Server.core)
    implementation(Ktor.serialization)
    implementation(Exposed.core)
}
