package com.weesnerdevelopment.billman.category

import io.ktor.server.routing.*

interface CategoriesRouter {
    fun setup(route: Route)
}