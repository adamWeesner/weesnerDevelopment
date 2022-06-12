package com.weesnerdevelopment.billman.income.occurrence

import io.ktor.server.routing.*

interface IncomeOccurrenceRouter {
    fun setup(routing: Routing)
}