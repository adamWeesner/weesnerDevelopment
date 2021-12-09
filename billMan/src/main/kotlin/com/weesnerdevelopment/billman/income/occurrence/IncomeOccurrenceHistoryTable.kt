package com.weesnerdevelopment.billman.income.occurrence

import com.weesnerdevelopment.history.HistoryTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object IncomeOccurrenceHistoryTable : Table() {
    val occurrence = reference("occurrence", IncomeOccurrenceTable, ReferenceOption.CASCADE)
    val history = reference("history", HistoryTable, ReferenceOption.CASCADE)
}