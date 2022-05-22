package com.weesnerdevelopment.billman.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object BillManDatabaseProd : BillManDatabase {
    override fun setup() {
        val database = HikariDataSource(
            HikariConfig().apply {
                driverClassName = "org.h2.Driver"
                jdbcUrl = "jdbc:h2:./server/billMan-database;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false"
                maximumPoolSize = 3
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                validate()
            }
        )

        Database.connect(database)
        createTables(true)
    }
}