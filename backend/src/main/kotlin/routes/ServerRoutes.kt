package com.weesnerdevelopment.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route

fun Routing.serverRoutes() {
    route("/health") {
        get("/") {
            call.respond(HttpStatusCode.OK, "Server is up and running")
        }
    }
}
