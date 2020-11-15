object Kotlin {
    private const val base = "org.jetbrains.kotlin"

    const val version = "1.4.10"

    const val jvm = "$base.jvm"
    const val kapt = "$base.kapt"

    const val stdLib = "$base:kotlin-stdlib-jdk8:$version"
    const val reflect = "$base:kotlin-reflect:$version"

    object Test {
        const val core = "$base:kotlin-test:$version"
        const val junit5 = "$base:kotlin-test-junit5:$version"
    }
}
