package com.weesnerdevelopment.billman.bill.occurrence.payment

import com.weesnerdevelopment.billman.color.ColorTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object PaymentHistoryTable : Table() {
    val color = reference("color", ColorTable, ReferenceOption.CASCADE)
    //val history = reference("history", HistoryTable, ReferenceOption.CASCADE)
}