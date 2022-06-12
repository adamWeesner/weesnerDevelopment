package com.weesnerdevelopment.auth.router

import io.ktor.server.routing.*

interface UserRouter {
    fun setup(routing: Routing)
}