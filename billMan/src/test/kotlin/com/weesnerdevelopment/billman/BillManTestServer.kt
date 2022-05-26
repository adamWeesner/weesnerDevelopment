package com.weesnerdevelopment.billman

import com.weesnerdevelopment.businessRules.Server
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kimchi.Kimchi
import kotlinx.serialization.ExperimentalSerializationApi
import logging.StdOutLogger
import respondErrorServer

@OptIn(ExperimentalSerializationApi::class)
object BillManTestServer : Server {
    override fun start(app: Application) {
        with(app) {
            Kimchi.addLog(StdOutLogger)

            install(DefaultHeaders) {
                header(HttpHeaders.AcceptCharset, Charsets.UTF_8.toString())
                header(
                    HttpHeaders.Accept,
                    ContentType.Application.Json.withParameter("charset", Charsets.UTF_8.toString()).toString()
                )
            }
            install(CallLogging)
            install(ContentNegotiation) {
                json(com.weesnerdevelopment.shared.json {
                    prettyPrint = true
                    prettyPrintIndent = "  "
                    isLenient = true
                })
            }
            install(StatusPages) {
                exception<Throwable> { e ->
                    respondErrorServer(e)
                }
            }
            install(Locations)
            install(Routing) {
                routes()
            }
        }
    }
}