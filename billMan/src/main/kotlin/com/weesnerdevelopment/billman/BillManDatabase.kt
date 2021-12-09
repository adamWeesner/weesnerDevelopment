package com.weesnerdevelopment.billman

import com.weesnerdevelopment.billman.bill.BillHistoryTable
import com.weesnerdevelopment.billman.bill.BillSharedUsersTable
import com.weesnerdevelopment.billman.bill.BillTable
import com.weesnerdevelopment.billman.bill.BillsCategoriesTable
import com.weesnerdevelopment.billman.bill.occurrence.BillOccurrenceHistoryTable
import com.weesnerdevelopment.billman.bill.occurrence.BillOccurrencePaymentsTable
import com.weesnerdevelopment.billman.bill.occurrence.BillOccurrenceSharedUsersTable
import com.weesnerdevelopment.billman.bill.occurrence.BillOccurrenceTable
import com.weesnerdevelopment.billman.bill.occurrence.payment.PaymentHistoryTable
import com.weesnerdevelopment.billman.bill.occurrence.payment.PaymentTable
import com.weesnerdevelopment.billman.category.CategoryHistoryTable
import com.weesnerdevelopment.billman.category.CategoryTable
import com.weesnerdevelopment.billman.color.ColorHistoryTable
import com.weesnerdevelopment.billman.color.ColorTable
import com.weesnerdevelopment.billman.income.IncomeHistoryTable
import com.weesnerdevelopment.billman.income.IncomeTable
import com.weesnerdevelopment.billman.income.occurrence.IncomeOccurrenceHistoryTable
import com.weesnerdevelopment.billman.income.occurrence.IncomeOccurrenceTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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
            create(
                BillTable,
                BillHistoryTable,
                BillsCategoriesTable,
                BillSharedUsersTable,

                BillOccurrenceTable,
                BillOccurrenceHistoryTable,
                BillOccurrenceSharedUsersTable,
                BillOccurrencePaymentsTable,

                PaymentTable,
                PaymentHistoryTable,

                CategoryTable,
                CategoryHistoryTable,

                ColorTable,
                ColorHistoryTable,

                IncomeTable,
                IncomeHistoryTable,

                IncomeOccurrenceTable,
                IncomeOccurrenceHistoryTable
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
