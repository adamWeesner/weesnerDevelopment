package com.weesnerdevelopment.auth

import auth.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import history.HistoryTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * The database factory for weesnerDevelopment.com.
 */
object AuthDatabase {
    /**
     * Initializes the [DatabaseFactory] and creates the database tables if needed.
     */
    fun init(testing: Boolean) {
        if (testing)
            Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        else
            Database.connect(hikari())

        transaction {
            // base tables
            create(UsersTable)
            create(HistoryTable)
        }
    }

    private fun hikari() = HikariDataSource(
        HikariConfig().apply {
            driverClassName = "org.h2.Driver"
            jdbcUrl = "jdbc:h2:./server/database;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false"
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
    )
}
