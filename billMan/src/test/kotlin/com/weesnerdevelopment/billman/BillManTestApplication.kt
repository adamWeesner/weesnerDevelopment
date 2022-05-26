package com.weesnerdevelopment.billman

import com.weesnerdevelopment.billman.database.BillManDatabase
import com.weesnerdevelopment.businessRules.Server
import io.ktor.application.*
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

fun Application.main() {
    initKodein()

    val database by kodein().instance<BillManDatabase>()
    val server by kodein().instance<Server>()

    database.setup()
    server.start(this)
}