val ktorServerTest: String by project

group = "${Base.group}.test"
version = "1.0.0"

dependencies {
    implementation(ProjectGradleModule.BusinessRules)
    implementation(Kimchi.core)

    api(ktorServerTest)
    api(Kotlin.Test.core)
    api(Kotlin.Test.junit5)
    api(Junit.core)
}
