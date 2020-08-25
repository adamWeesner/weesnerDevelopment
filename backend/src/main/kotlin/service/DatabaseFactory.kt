package com.weesnerdevelopment.service

import auth.UsersTable
import billCategories.BillCategoriesTable
import billSharedUsers.BillsSharedUsersTable
import bills.BillsTable
import categories.CategoriesTable
import colors.ColorsTable
import com.weesnerdevelopment.validator.ValidatorTable
import com.weesnerdevelopment.validator.complex.ComplexValidatorTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import federalIncomeTax.FederalIncomeTaxesTable
import history.HistoryTable
import income.IncomeTable
import incomeOccurrences.IncomeOccurrencesTable
import logging.LoggingTable
import medicare.MedicareLimitsTable
import medicare.MedicareTable
import occurrences.BillOccurrencesTable
import occurrencesSharedUsers.OccurrenceSharedUsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
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
            // validator tables
            create(ValidatorTable, ComplexValidatorTable)
            // logging
            create(LoggingTable)
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
                BillOccurrencesTable,
                BillCategoriesTable,
                BillsSharedUsersTable,
                OccurrenceSharedUsersTable,
                IncomeOccurrencesTable
            )
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
