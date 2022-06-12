package com.weesnerdevelopment.billman.bill.occurrence

import io.ktor.server.routing.*

interface BillOccurrenceRouter {
    fun setup(routing: Routing)
}