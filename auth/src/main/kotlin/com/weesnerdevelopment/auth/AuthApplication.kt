package com.weesnerdevelopment.auth

import com.weesnerdevelopment.auth.database.AuthDatabase
import com.weesnerdevelopment.businessRules.Server
import io.ktor.server.application.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Application.main() {
    initKodein()

    val database by closestDI().instance<AuthDatabase>()
    val server by closestDI().instance<Server>()

    database.setup()
    server.start(this)
}
