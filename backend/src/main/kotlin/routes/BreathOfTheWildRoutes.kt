package com.weesnerdevelopment.routes

import breathOfTheWild.CrittersRouter
import io.ktor.auth.*
import io.ktor.routing.*
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

fun Routing.breathOfTheWildRoutes() {
    val crittersRouter by kodein().instance<CrittersRouter>()

    crittersRouter.apply {
        authenticate {
            setupRoutes()
        }
    }
}
