package com.weesnerdevelopment.auth

import com.weesnerdevelopment.auth.router.UserRouter
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Routing.routes() {
    val userRouter by closestDI().instance<UserRouter>()

    userRouter.setup(this)
}
