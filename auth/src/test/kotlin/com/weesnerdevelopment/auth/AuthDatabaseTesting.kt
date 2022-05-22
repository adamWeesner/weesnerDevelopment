package com.weesnerdevelopment.auth

import com.weesnerdevelopment.auth.database.AuthDatabase
import org.jetbrains.exposed.sql.Database

object AuthDatabaseTesting : AuthDatabase {
    override fun setup() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        createTables(true)
    }
}