package com.weesnerdevelopment.auth.database

import com.weesnerdevelopment.auth.user.UserTable
import com.weesnerdevelopment.businessRules.WeesnerDevelopmentDatabase
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

interface AuthDatabase : WeesnerDevelopmentDatabase {
    fun createTables(addLogger: Boolean) {
        transaction {
            create(
                UserTable,
//                UserHistoryTable,
//                HistoryTable
            )

            if (addLogger)
                addLogger(StdOutSqlLogger)
        }
    }
}
