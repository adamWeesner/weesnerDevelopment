package com.weesnerdevelopment.service

import auth.UsersTable
import billCategories.BillCategoriesTable
import billSharedUsers.BillsSharedUsersTable
import bills.BillsTable
import categories.CategoriesTable
import colors.ColorsTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import federalIncomeTax.FederalIncomeTaxesTable
import history.HistoryTable
import income.IncomeTable
import medicare.MedicareLimitsTable
import medicare.MedicareTable
import occurrences.OccurrencesTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import payments.PaymentsTable
import socialSecurity.SocialSecurityTable
import taxWithholding.TaxWithholdingTable

/**
 * The database factory for weesnerDevelopment.com.
 */
object DatabaseFactory {
    /**
     * Initializes the [DatabaseFactory] and creates the database tables if needed.
     */
    fun init() {
        Database.connect(hikari())
        transaction {
            addLogger(StdOutSqlLogger)

            // base tables
            create(UsersTable)
            create(HistoryTable)
            // tax fetcher
            create(
                SocialSecurityTable,
                MedicareTable,
                MedicareLimitsTable,
                TaxWithholdingTable,
                FederalIncomeTaxesTable
            )
            // bill man
            create(
                BillsTable,
                IncomeTable,
                CategoriesTable,
                ColorsTable,
                PaymentsTable,
                OccurrencesTable,
                BillCategoriesTable,
                BillsSharedUsersTable
            )
        }
    }

    private fun hikari() = HikariDataSource(
        HikariConfig().apply {
            driverClassName = "org.h2.Driver"
            jdbcUrl = "jdbc:h2:./server/database"
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
    )
}
