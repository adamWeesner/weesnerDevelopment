package com.weesnerdevelopment.billman

import com.weesnerdevelopment.billman.database.BillManDatabase
import com.weesnerdevelopment.businessRules.Server
import io.ktor.server.application.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Application.main() {
    initKodein()

    val database by closestDI().instance<BillManDatabase>()
    val server by closestDI().instance<Server>()

    database.setup()
    server.start(this)
}