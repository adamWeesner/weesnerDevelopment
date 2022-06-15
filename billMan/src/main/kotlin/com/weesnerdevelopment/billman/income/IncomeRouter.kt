package com.weesnerdevelopment.billman.income

import io.ktor.server.routing.*

interface IncomeRouter {
    fun setup(route: Route)
}