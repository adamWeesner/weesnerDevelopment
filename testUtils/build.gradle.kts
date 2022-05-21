import com.weesnerdevelopment.ProjectGradleModule.BusinessRules
import com.weesnerdevelopment.implementation

group = "${Base.group}.test"
version = "1.0.0"

dependencies {
    implementation(BusinessRules)
    implementation(Kimchi.core)

    api(Ktor.serverTest)
    api(Kotlin.Test.core)
    api(Kotlin.Test.junit5)
    api(Junit.core)
}
