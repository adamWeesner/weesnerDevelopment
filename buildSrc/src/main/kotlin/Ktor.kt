object Ktor {
    private const val base = "io.ktor:ktor-"

    const val version = "1.2.6"

    const val authJwt = "${base}auth-jwt:$version"
    const val webSockets = "${base}websockets:$version"
    const val serverTest = "${base}server-tests:$version"

    object Server {
        const val mainClass = "io.ktor.server.netty.EngineMain"

        const val core = "${base}server-core:$version"
        const val netty = "${base}server-netty:$version"
    }

    object Client {
        const val webSockets = "${base}client-websockets:$version"
        const val okHttp = "${base}client-okhttp:$version"
    }
}
