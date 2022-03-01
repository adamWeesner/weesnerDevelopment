package com.weesnerdevelopment.billman.bill

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object BillHistoryTable : Table() {
    val bill = reference("bill", BillTable, ReferenceOption.CASCADE)
//    val history = reference("history", HistoryTable, ReferenceOption.CASCADE)
}