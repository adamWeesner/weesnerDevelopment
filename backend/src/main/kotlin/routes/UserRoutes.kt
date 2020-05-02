package com.weesnerdevelopment.routes

import auth.UserRouter
import com.weesnerdevelopment.utils.Path
import generics.route
import io.ktor.routing.Routing
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

fun Routing.userRoutes() {
    val userRouter by kodein().instance<UserRouter>()

    route(userRouter) { router ->
        (router as UserRouter).apply {
            login(Path.User.login)
            signUp(Path.User.signUp)
        }
    }
}
