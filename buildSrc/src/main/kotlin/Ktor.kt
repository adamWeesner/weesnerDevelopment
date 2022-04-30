object Ktor {
    private const val base = "io.ktor:ktor-"

    const val version = "1.6.8"

    const val authJwt = "${base}auth-jwt:$version"
    const val metrics = "${base}metrics:$version"
    const val webSockets = "${base}websockets:$version"
    const val serverTest = "${base}server-tests:$version"
    const val serialization = "${base}serialization:$version"
    const val locations = "${base}locations:$version"

    object Server {
        const val mainClass = "io.ktor.server.netty.EngineMain"

        const val core = "${base}server-core:$version"
        const val netty = "${base}server-netty:$version"
    }

    object Client {
        const val webSockets = "${base}client-websockets:$version"
        const val java = "${base}client-java:$version"
        const val logging = "${base}client-logging:$version"
    }
}
