package com.weesnerdevelopment.auth.exposed

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object UserHistoryTable : Table() {
    val user = reference("user", UserTable, ReferenceOption.CASCADE)
//    val history = reference("history", HistoryTable, ReferenceOption.CASCADE)
}