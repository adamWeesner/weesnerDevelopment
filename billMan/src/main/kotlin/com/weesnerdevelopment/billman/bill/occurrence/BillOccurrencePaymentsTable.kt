package com.weesnerdevelopment.billman.bill.occurrence

import com.weesnerdevelopment.billman.bill.occurrence.payment.PaymentTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object BillOccurrencePaymentsTable : Table() {
    val payment = reference("payment", PaymentTable, ReferenceOption.CASCADE)
    val occurrence = reference("occurrence", BillOccurrenceTable, ReferenceOption.CASCADE)
}