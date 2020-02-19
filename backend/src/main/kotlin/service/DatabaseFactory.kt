package com.weesnerdevelopment.service

import auth.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import federalIncomeTax.FederalIncomeTaxesTable
import medicare.MedicareLimitsTable
import medicare.MedicareTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
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
            // tax fetcher
            create(
                SocialSecurityTable,
                MedicareTable,
                MedicareLimitsTable,
                TaxWithholdingTable,
                FederalIncomeTaxesTable
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