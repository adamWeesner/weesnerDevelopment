package com.weesnerdevelopment.service

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import federalIncomeTax.FederalIncomeTaxes
import medicare.MedicareLimits
import medicare.Medicares
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import socialSecurity.SocialSecuritys
import taxWithholding.TaxWithholdings

object DatabaseFactory {
    fun init() {
        Database.connect(hikari())
        transaction {
            addLogger(StdOutSqlLogger)
            // tax fetcher
            create(SocialSecuritys, Medicares, MedicareLimits, TaxWithholdings, FederalIncomeTaxes)
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig().apply {
            driverClassName = "org.h2.Driver"
            jdbcUrl = "jdbc:h2:./server/database"
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        return HikariDataSource(config)
    }
}