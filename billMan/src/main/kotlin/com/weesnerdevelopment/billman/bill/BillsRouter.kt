package com.weesnerdevelopment.billman.bill

import io.ktor.server.routing.*

interface BillsRouter {
    fun setup(route: Route)
}