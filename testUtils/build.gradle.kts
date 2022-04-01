group = Base.group
version = TestUtils.version

dependencies {
    implementation(project(BusinessRules.project))
    implementation(Kimchi.core)

    api(Ktor.serverTest)
    api(Kotlin.Test.core)
    api(Kotlin.Test.junit5)
    api(Junit.core)
}
