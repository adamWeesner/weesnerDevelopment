import org.gradle.api.JavaVersion

object Jvm {
    val javaVersion: JavaVersion = JavaVersion.VERSION_11
    val version: String = javaVersion.majorVersion
}
