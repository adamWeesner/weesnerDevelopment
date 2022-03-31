plugins {
    application
}

group = Base.group
version = Generator.version

dependencies {
    implementation(project(BusinessRules.project))
    implementation("com.github.ajalt.clikt:clikt:3.0.1")

    testImplementation(project(TestUtils.project))
}
