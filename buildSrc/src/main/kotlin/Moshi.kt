object Moshi {
    private const val base = "com.squareup.moshi:moshi-"

    const val version = "1.9.2"
    const val ktorVersion = "1.0.1"

    const val core = "${base}kotlin:$version"
    const val adapters = "${base}adapters:$version"
    const val ktor = "com.ryanharter.ktor:ktor-moshi:$ktorVersion"
}
