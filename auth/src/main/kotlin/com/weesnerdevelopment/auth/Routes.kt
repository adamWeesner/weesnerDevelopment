package com.weesnerdevelopment.auth

import com.weesnerdevelopment.auth.user.UserRouter
import io.ktor.routing.*
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

fun Routing.routes() {
    val userRouter by kodein().instance<UserRouter>()

    userRouter.setup(this)
}
