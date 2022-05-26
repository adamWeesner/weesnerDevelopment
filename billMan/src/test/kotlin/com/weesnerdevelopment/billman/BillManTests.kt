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
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach

abstract class BillManTests {
    abstract val baseUrl: String
    internal val config = "application-test.conf"

    sealed class Testing {
        @AfterEach
        fun cleanUp() {
            transaction {
                SchemaUtils.drop(
                    BillTable,
                    BillHistoryTable,
                    BillsCategoriesTable,
                    BillSharedUsersTable,
                    BillOccurrenceTable,
                    BillOccurrenceSharedUsersTable,
                    BillOccurrencePaymentsTable,
                    BillOccurrenceHistoryTable,
                    IncomeTable,
                    IncomeHistoryTable,
                    IncomeOccurrenceTable,
                    IncomeOccurrenceHistoryTable,
                    ColorTable,
                    ColorHistoryTable,
                    CategoryTable,
                    CategoryHistoryTable,
                    PaymentTable,
                    PaymentHistoryTable,
                )
                SchemaUtils.create(
                    BillTable,
                    BillHistoryTable,
                    BillsCategoriesTable,
                    BillSharedUsersTable,
                    BillOccurrenceTable,
                    BillOccurrenceSharedUsersTable,
                    BillOccurrencePaymentsTable,
                    BillOccurrenceHistoryTable,
                    IncomeTable,
                    IncomeHistoryTable,
                    IncomeOccurrenceTable,
                    IncomeOccurrenceHistoryTable,
                    ColorTable,
                    ColorHistoryTable,
                    CategoryTable,
                    CategoryHistoryTable,
                    PaymentTable,
                    PaymentHistoryTable,
                )
            }
        }
    }
}