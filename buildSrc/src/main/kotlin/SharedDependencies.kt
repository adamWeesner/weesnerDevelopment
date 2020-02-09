import Versions.exposed_version
import Versions.ktor_version
import Versions.moshi_version
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.kotlin

fun ktor(value: String) = "io.ktor:ktor-$value:$ktor_version"
fun exposed(value: String = "core") = "org.jetbrains.exposed:exposed-$value:$exposed_version"
fun moshi(value: String = "kotlin") = "com.squareup.moshi:moshi-$value:$moshi_version"

fun DependencyHandlerScope.kotlinJdk() = kotlin("stdlib-jdk8")
fun ktorServer(value: String) = ktor("server-$value")
fun ktorClient(value: String) = ktor("client-$value")

const val jvmVersion = "1.8"