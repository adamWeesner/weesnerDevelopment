package com.weesnerdevelopment.billman.server

import com.weesnerdevelopment.billman.routes
import com.weesnerdevelopment.businessRules.*
import com.weesnerdevelopment.businessRules.auth.AuthProvider
import com.weesnerdevelopment.shared.Paths
import com.weesnerdevelopment.shared.base.Response
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.locations.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kimchi.Kimchi
import logging.StdOutLogger
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.slf4j.event.Level

object BillManDevServer : Server {
    override fun start(app: Application) {
        with(app) {
            val appConfig by closestDI().instance<AppConfig>()
            val authProvider by closestDI().instance<AuthProvider>()

            Kimchi.addLog(StdOutLogger)
            installDefaultHeaders()
            installCallLogging(Level.TRACE)
            installCORS(
                CORSHost("${appConfig.baseUrl}:${appConfig.sslPort}", HttpScheme.Https),
                CORSHost(appConfig.baseUrl, HttpScheme.Https),
                CORSHost("${appConfig.baseUrl}:${appConfig.port}", HttpScheme.Http),
                CORSHost("localhost:8081", HttpScheme.Http)
            )
            installContentNegotiation()
            installStatusPages()
            installAuthentication(authProvider)
            install(Locations)
            install(Routing) {
                route(Paths.BillMan.health) {
                    get {
                        respond(
                            Response.Ok(
                                "BillMan ${
                                    this.call.request.path().replace("/health", "")
                                } is up and running"
                            )
                        )
                    }
                }

                authenticate {
                    this@install.routes()
                }
            }
        }
    }
}