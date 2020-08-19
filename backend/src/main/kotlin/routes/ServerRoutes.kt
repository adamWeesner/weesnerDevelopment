package com.weesnerdevelopment.routes

import com.weesnerdevelopment.utils.Path.Server.health
import com.weesnerdevelopment.validator.ValidatorRouter
import com.weesnerdevelopment.validator.complex.ComplexValidatorRouter
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import respond
import shared.base.Response.Companion.Ok

fun Routing.serverRoutes() {
    val validatorRouter by kodein().instance<ValidatorRouter>()
    val complexValidatorRouter by kodein().instance<ComplexValidatorRouter>()

    route("/$health") {
        get("/") {
            respond(Ok("Server is up and running"))
        }
    }

    validatorRouter.apply { setupRoutes() }
    complexValidatorRouter.apply { setupRoutes() }
}
