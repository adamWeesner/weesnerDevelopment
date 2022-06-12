package com.weesnerdevelopment.auth.server

import com.weesnerdevelopment.auth.routes
import com.weesnerdevelopment.businessRules.*
import com.weesnerdevelopment.businessRules.auth.AuthProvider
import com.weesnerdevelopment.shared.Paths
import com.weesnerdevelopment.shared.base.Response
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kimchi.Kimchi
import kimchi.logger.defaultWriter
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.slf4j.event.Level

object AuthDevServer : Server {
    override fun start(app: Application) {
        with(app) {
            val appConfig by closestDI().instance<AppConfig>()
            val authProvider by closestDI().instance<AuthProvider>()

            Kimchi.addLog(defaultWriter)

            installDefaultHeaders()
            installCallLogging(Level.TRACE)
            installCORS(
                CORSHost("${appConfig.baseUrl}:${appConfig.sslPort}", HttpScheme.Https),
                CORSHost(appConfig.baseUrl, HttpScheme.Https),
                CORSHost("${appConfig.baseUrl}:${appConfig.port}", HttpScheme.Http),
                CORSHost("localhost:3000", HttpScheme.Http),
            )
            installContentNegotiation()
            installStatusPages()
            installAuthentication(authProvider)
            install(Locations)
            install(Routing) {
                route(Paths.User.health) {
                    get {
                        call.respond(
                            Response.Ok(
                                "Auth ${this.call.request.path().replace("/health", "")} is up and running"
                            )
                        )
                    }
                }

                routes()
            }
        }
    }
}