package com.weesnerdevelopment.billman.bill

import io.ktor.routing.*

interface BillsRouter {
    fun setup(routing: Routing)
}