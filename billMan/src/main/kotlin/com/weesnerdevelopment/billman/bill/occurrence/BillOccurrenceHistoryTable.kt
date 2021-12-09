package com.weesnerdevelopment.billman.bill.occurrence

import com.weesnerdevelopment.history.HistoryTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object BillOccurrenceHistoryTable : Table() {
    val occurrence = reference("occurrence", BillOccurrenceTable, ReferenceOption.CASCADE)
    val history = reference("history", HistoryTable, ReferenceOption.CASCADE)
}