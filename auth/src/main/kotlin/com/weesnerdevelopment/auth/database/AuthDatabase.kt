package com.weesnerdevelopment.auth.database

import auth.UsersTable
import com.weesnerdevelopment.auth.user.UserHistoryTable
import com.weesnerdevelopment.businessRules.WeesnerDevelopmentDatabase
import history.HistoryTable
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction

interface AuthDatabase : WeesnerDevelopmentDatabase {
    fun createTables(addLogger: Boolean) {
        transaction {
            create(
                UsersTable,
                UserHistoryTable,

                HistoryTable
            )
        }
    }
}
