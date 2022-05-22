package com.weesnerdevelopment.billman.database

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
import com.weesnerdevelopment.businessRules.WeesnerDevelopmentDatabase
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * The database factory for weesnerDevelopment.com/billMan.
 */
interface BillManDatabase : WeesnerDevelopmentDatabase {
    /**
     * Creates the [Database] tables.
     */
    fun createTables(addLogger: Boolean) {
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

            if (addLogger)
                addLogger(StdOutSqlLogger)
        }
    }
}