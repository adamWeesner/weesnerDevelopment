package com.weesnerdevelopment.billman.bill.occurrence

import io.ktor.routing.*

interface BillOccurrenceRouter {
    fun setup(routing: Routing)
}