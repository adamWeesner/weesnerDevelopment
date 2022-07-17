package com.weesnerdevelopment.billman.server

import com.codahale.metrics.jmx.JmxReporter
import com.weesnerdevelopment.billman.routes
import com.weesnerdevelopment.businessRules.*
import com.weesnerdevelopment.businessRules.auth.AuthProvider
import com.weesnerdevelopment.shared.Paths
import com.weesnerdevelopment.shared.base.Response
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.locations.*
import io.ktor.server.metrics.dropwizard.*
import io.ktor.server.plugins.hsts.*
import io.ktor.server.plugins.httpsredirect.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kimchi.Kimchi
import logging.StdOutLogger
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import java.util.concurrent.TimeUnit

object BillManProdServer : Server {
    override fun start(app: Application) {
        with(app) {
            val appConfig by closestDI().instance<AppConfig>()
            val authProvider by closestDI().instance<AuthProvider>()
//        val loggingService by kodein().instance<LoggingService>()

//        if (!appConfig.isTesting)
//            Kimchi.addLog(DbLogger.apply { service = loggingService })

            Kimchi.addLog(StdOutLogger)

            installDefaultHeaders()
            install(HSTS)
            install(HttpsRedirect)
            installCORS(
                CORSHost("${appConfig.baseUrl}:${appConfig.sslPort}", HttpScheme.Https),
                CORSHost(appConfig.baseUrl, HttpScheme.Https)
            )
            install(DropwizardMetrics) {
//            Slf4jReporter.forRegistry(registry)
//                .outputTo(log)
//                .convertRatesTo(TimeUnit.SECONDS)
//                .convertDurationsTo(TimeUnit.MILLISECONDS)
//                .build()
//                .start(10, TimeUnit.SECONDS)

                JmxReporter.forRegistry(registry)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build()
                    .start()
            }
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
                    routes()
                }
            }
        }
    }
}

