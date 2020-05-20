package com.weesnerdevelopment.routes

import generics.Ok
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route

fun Routing.serverRoutes() {
    route("/health") {
        get("/") {
            call.respond(Ok("Server is up and running"))
        }
    }
}
