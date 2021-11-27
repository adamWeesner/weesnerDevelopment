object Kotlin {
    private const val base = "org.jetbrains.kotlin"

    const val version = "1.5.31"

    const val jvm = "$base.jvm"
    const val kapt = "$base.kapt"

    object Test {
        const val core = "$base:kotlin-test:$version"
        const val junit5 = "$base:kotlin-test-junit5:$version"
    }
}
