package com.weesnerdevelopment.billman

import com.weesnerdevelopment.billman.database.BillManDatabase
import org.jetbrains.exposed.sql.Database

object BillManDatabaseTesting : BillManDatabase {
    override fun setup() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        createTables(false)
    }
}