package com.weesnerdevelopment.routes

import auth.UserRouter
import io.ktor.routing.Routing
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

fun Routing.userRoutes() {
    val userRouter by kodein().instance<UserRouter>()

    userRouter.apply { setupRoutes() }
}
