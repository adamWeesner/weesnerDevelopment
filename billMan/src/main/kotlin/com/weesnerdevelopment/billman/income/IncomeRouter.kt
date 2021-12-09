package com.weesnerdevelopment.billman.income

import io.ktor.routing.*

interface IncomeRouter {
    fun setup(routing: Routing)
}