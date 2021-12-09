package com.weesnerdevelopment.billman.color

import com.weesnerdevelopment.history.HistoryTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object ColorHistoryTable : Table() {
    val color = reference("color", ColorTable, ReferenceOption.CASCADE)
    val history = reference("history", HistoryTable, ReferenceOption.CASCADE)
}