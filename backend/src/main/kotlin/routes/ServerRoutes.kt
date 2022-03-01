package com.weesnerdevelopment.routes

import Path.Server.health
import com.weesnerdevelopment.shared.base.Response.Companion.Ok
import com.weesnerdevelopment.validator.ValidatorRouter
import com.weesnerdevelopment.validator.complex.ComplexValidatorRouter
import io.ktor.auth.*
import io.ktor.routing.*
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import respond

fun Routing.serverRoutes() {
    val validatorRouter by kodein().instance<ValidatorRouter>()
    val complexValidatorRouter by kodein().instance<ComplexValidatorRouter>()

    route("/$health") {
        get("/") {
            respond(Ok("Server is up and running"))
        }
    }

    validatorRouter.apply { setupRoutes() }
    authenticate {
        complexValidatorRouter.apply { setupRoutes() }
    }
}
