group = Base.group
version = BreathOfTheWild.version

dependencies {
    implementation(project(BusinessRules.project))
    implementation(Ktor.Server.core)
    implementation(Ktor.serialization)
    implementation(Exposed.core)
}
