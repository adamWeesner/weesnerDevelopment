package com.weesnerdevelopment.billman.category

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object CategoryHistoryTable : Table() {
    val category = reference("category", CategoryTable, ReferenceOption.CASCADE)
    //val history = reference("history", HistoryTable, ReferenceOption.CASCADE)
}