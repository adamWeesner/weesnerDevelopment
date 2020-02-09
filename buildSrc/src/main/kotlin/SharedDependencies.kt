import Versions.exposed_version
import Versions.ktor_version
import Versions.moshi_version

fun ktor(value: String) = "io.ktor:ktor-$value:$ktor_version"
fun exposed(value: String = "core") = "org.jetbrains.exposed:exposed-$value:$exposed_version"
fun moshi(value: String = "kotlin") = "com.squareup.moshi:moshi-$value:$moshi_version"

object Versions {
    const val ktor_version = "1.2.6"
    const val kotlin_version = "1.3.60"
    const val logback_version = "1.2.1"
    const val exposed_version = "0.18.1"
    const val h2_version = "1.4.200"
    const val hikari_version = "3.4.1"
    const val ktor_moshi_version = "1.0.1"
    const val moshi_version = "1.9.2"
}
