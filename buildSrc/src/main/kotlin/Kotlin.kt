object Kotlin {
    private const val base = "org.jetbrains.kotlin"

    const val version = "1.3.71"

    const val jvm = "$base.jvm"
    const val kapt = "$base.kapt"

    const val stdLib = "$base:kotlin-stdlib-jdk8:$version"
    const val reflect = "$base:kotlin-reflect:$version"

    object Test {
        private const val testVersion = "3.4.2"

        const val runner = "io.kotlintest:kotlintest-runner-junit5:$testVersion"
    }
}
