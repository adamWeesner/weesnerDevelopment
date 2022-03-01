package com.weesnerdevelopment.billman.income

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object IncomeHistoryTable : Table() {
    val income = reference("Income", IncomeTable, ReferenceOption.CASCADE)
//    val history = reference("history", HistoryTable, ReferenceOption.CASCADE)
}