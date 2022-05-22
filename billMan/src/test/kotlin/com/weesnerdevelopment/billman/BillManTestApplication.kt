package com.weesnerdevelopment.billman

import auth.AuthValidatorFake
import com.weesnerdevelopment.businessRules.AppConfig
import com.weesnerdevelopment.shared.Paths
import com.weesnerdevelopment.shared.base.Response
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kimchi.Kimchi
import logging.StdOutLogger
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import respond
import respondErrorServer

fun Application.main() {
    initKodein(AuthValidatorFake("e0f97212-4431-448b-bd97-f70235328cd1"))

    val appConfig by kodein().instance<AppConfig>()

    Kimchi.addLog(StdOutLogger)

    BillManDatabase.init(appConfig.isTesting)

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

        routes()
    }
}