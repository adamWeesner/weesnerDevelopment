package com.weesnerdevelopment.billman

import com.weesnerdevelopment.businessRules.*
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.routing.*
import kimchi.Kimchi
import logging.StdOutLogger
import org.slf4j.event.Level

object BillManTestServer : Server {
    override fun start(app: Application) {
        with(app) {
            Kimchi.addLog(StdOutLogger)

            installDefaultHeaders()
            installCallLogging(Level.TRACE)
            installContentNegotiation()
            installStatusPages()
            install(Locations)
            install(Routing) {
                routes()
            }
        }
    }
}