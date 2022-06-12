package com.weesnerdevelopment.auth

import com.weesnerdevelopment.businessRules.*
import com.weesnerdevelopment.businessRules.auth.AuthProvider
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.routing.*
import kimchi.Kimchi
import logging.StdOutLogger
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.slf4j.event.Level

object AuthTestServer : Server {
    override fun start(app: Application) {
        with(app) {
            val authProvider by closestDI().instance<AuthProvider>()

            Kimchi.addLog(StdOutLogger)

            installDefaultHeaders()
            installCallLogging(Level.TRACE)
            installContentNegotiation()
            installStatusPages()
            installAuthentication(authProvider)
            install(Locations)
            install(Routing) {
                routes()
            }
        }
    }
}