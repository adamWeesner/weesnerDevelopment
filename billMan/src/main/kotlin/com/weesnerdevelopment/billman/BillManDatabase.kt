package com.weesnerdevelopment.billman

import com.weesnerdevelopment.billman.billCategories.BillCategoriesTable
import com.weesnerdevelopment.billman.billSharedUsers.BillsSharedUsersTable
import com.weesnerdevelopment.billman.bills.BillTable
import com.weesnerdevelopment.billman.bills.BillsTable
import com.weesnerdevelopment.billman.categories.CategoriesTable
import com.weesnerdevelopment.billman.colors.ColorsTable
import com.weesnerdevelopment.billman.income.IncomeTable
import com.weesnerdevelopment.billman.incomeOccurrences.IncomeOccurrencesTable
import com.weesnerdevelopment.billman.occurrences.BillOccurrencesTable
import com.weesnerdevelopment.billman.occurrencesSharedUsers.OccurrenceSharedUsersTable
import com.weesnerdevelopment.billman.payments.PaymentsTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import logging.LoggingTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * The database factory for weesnerDevelopment.com.
 */
object BillManDatabase {
    /**
     * Initializes the [Database] and creates the database tables if needed.
     */
    fun init(testing: Boolean) {
        if (testing)
            Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        else
            Database.connect(hikari())

        transaction {
            // logging
            create(LoggingTable)

            // bill man
            create(
                BillTable,
                BillsTable,
                IncomeTable,
                CategoriesTable,
                ColorsTable,
                PaymentsTable,
                BillOccurrencesTable,
                BillCategoriesTable,
                BillsSharedUsersTable,
                OccurrenceSharedUsersTable,
                IncomeOccurrencesTable
            )

            if (testing)
                addLogger(StdOutSqlLogger)
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
