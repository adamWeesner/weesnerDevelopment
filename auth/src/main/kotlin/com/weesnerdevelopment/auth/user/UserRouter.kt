package com.weesnerdevelopment.auth.user

import io.ktor.routing.*

interface UserRouter {
    fun setup(routing: Routing)
}