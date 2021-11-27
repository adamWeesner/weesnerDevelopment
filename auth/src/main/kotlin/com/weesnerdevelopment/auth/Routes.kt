package com.weesnerdevelopment.auth

import auth.UserRouter
import io.ktor.routing.*
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

fun Routing.userRoutes() {
    val userRouter by kodein().instance<UserRouter>()

    userRouter.apply { setupRoutes() }
}
